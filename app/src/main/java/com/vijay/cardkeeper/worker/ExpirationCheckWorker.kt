package com.vijay.cardkeeper.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vijay.cardkeeper.CardKeeperApplication
import com.vijay.cardkeeper.R
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.util.DateNormalizer
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private const val TAG = "ExpirationCheckWorker"

class ExpirationCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting ExpirationCheckWorker")
        val appContainer = (applicationContext as CardKeeperApplication).container
        val userPrefs = appContainer.userPreferencesRepository

        // 1. Check if notifications are enabled
        val notificationsEnabled = userPrefs.notificationsEnabled.first()
        Log.d(TAG, "Notifications enabled: $notificationsEnabled")
        if (!notificationsEnabled) {
            return Result.success()
        }

        // 2. Get Reminder Days
        val reminder1 = userPrefs.reminder1Days.first()
        val reminder2 = userPrefs.reminder2Days.first()
        val reminder3 = userPrefs.reminder3Days.first()
        val reminders = listOf(reminder1, reminder2, reminder3)
        Log.d(TAG, "Reminders configured for days: $reminders")

        // 3. Fetch Items
        val identityDocs = appContainer.identityRepository.allDocuments.first()
        val passports = appContainer.passportRepository.allPassports.first()
        val financialAccounts = appContainer.financialRepository.allAccounts.first()

        val today = LocalDate.now()
        Log.d(TAG, "Today is: $today")
        val expiringItems = mutableListOf<String>()

        // 4. Check Identity Documents
        for (doc in identityDocs) {
            val readableType = formatDocumentType(doc.type)
            checkExpiry(doc.expiryDate, "$readableType (${doc.country})", reminders, today)?.let {
                Log.d(TAG, "Expiring Identity Doc Found: $it")
                expiringItems.add(it)
            }
        }

        // 5. Check Passports
        for (passport in passports) {
            checkExpiry(passport.dateOfExpiry, "Passport (${passport.countryCode})", reminders, today)?.let {
                Log.d(TAG, "Expiring Passport Found: $it")
                expiringItems.add(it)
            }
        }

        // 6. Check Financial Accounts
        for (account in financialAccounts) {
            val readableType = formatAccountType(account.type)
            val last4 = if (account.number.length >= 4) account.number.takeLast(4) else account.number
            checkFinancialExpiry(account.expiryDate, "${account.institutionName} $readableType (...$last4)", reminders, today)?.let {
                Log.d(TAG, "Expiring Financial Account Found: $it")
                expiringItems.add(it)
            }
        }

        // 7. Calculate Total "Expiring Soon" Count for Badge
        // We define "Expiring Soon" as within 30 days or expired.
        var badgeCount = 0
        val badgeThreshold = 30L
        
        // Helper to check for badge count
        fun incrementIfExpiring(expiryDateStr: String?) {
            if (expiryDateStr.isNullOrBlank()) return
            // Handle both formats
            val date = try {
                 if (expiryDateStr.matches(Regex("\\d{2}/\\d{2}"))) {
                    val inputFormat = DateTimeFormatter.ofPattern("MM/yy")
                    val yearMonth = YearMonth.parse(expiryDateStr, inputFormat)
                    yearMonth.atEndOfMonth()
                } else {
                    DateNormalizer.parseStrict(expiryDateStr)
                }
            } catch (e: Exception) {
                null
            }

            if (date != null) {
                 // Check if date is today or in the future (not expired)
                 // AND within the threshold
                 if (!date.isBefore(today) && (date.isBefore(today.plusDays(badgeThreshold)) || date.isEqual(today.plusDays(badgeThreshold)))) {
                     badgeCount++
                 }
            }
        }

        // Count all items
        identityDocs.forEach { incrementIfExpiring(it.expiryDate) }
        passports.forEach { incrementIfExpiring(it.dateOfExpiry) }
        financialAccounts.forEach { incrementIfExpiring(it.expiryDate) }

        Log.d(TAG, "Total expiring items for badge: $badgeCount")

        // 8. Send Notification
        if (expiringItems.isNotEmpty()) {
            Log.d(TAG, "Sending notification for ${expiringItems.size} items")
            sendNotification(expiringItems, badgeCount)
        } else if (badgeCount > 0) {
            // Force a notification if there are pending expiring items to ensure badge is visible.
            Log.d(TAG, "Sending summary notification for badge ($badgeCount items)")
            val summary = listOf("$badgeCount items are expiring within 30 days")
            sendNotification(summary, badgeCount)
        } else {
            Log.d(TAG, "No expiring items found.")
        }

        return Result.success()
    }

    private fun formatDocumentType(type: com.vijay.cardkeeper.data.entity.DocumentType): String {
        return type.name.split("_")
            .joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
    }

    private fun formatAccountType(type: com.vijay.cardkeeper.data.entity.AccountType): String {
        return type.name.split("_")
            .joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
    }

    private fun checkExpiry(expiryDateStr: String?, itemName: String, reminders: List<Int>, today: LocalDate): String? {
        if (expiryDateStr.isNullOrBlank()) return null
        
        val expiryDate = try {
            DateNormalizer.parseStrict(expiryDateStr)
        } catch (e:Exception) {
            Log.e(TAG, "Failed to parse date: $expiryDateStr error: ${e.message}")
            null
        } ?: return null
        
        if (expiryDate.isEqual(today)) {
             return "$itemName expires TODAY"
        }

        for (days in reminders) {
            val targetDate = today.plusDays(days.toLong())
            if (expiryDate.isEqual(targetDate)) {
                 return "$itemName expires in $days days"
            }
        }
        return null
    }

    private fun checkFinancialExpiry(expiryDateStr: String?, itemName: String, reminders: List<Int>, today: LocalDate): String? {
        if (expiryDateStr.isNullOrBlank()) return null

        // Handle MM/YY
        val expiryDate = try {
            val parts = expiryDateStr.split("/")
            if (parts.size == 2) {
                val inputFormat = DateTimeFormatter.ofPattern("MM/yy")
                val yearMonth = YearMonth.parse(expiryDateStr, inputFormat)
                yearMonth.atEndOfMonth()
            } else {
                 DateNormalizer.parseStrict(expiryDateStr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse financial date: $expiryDateStr error: ${e.message}")
            null
        } ?: return null

        if (expiryDate.isEqual(today)) {
             return "$itemName expires TODAY"
        }

        for (days in reminders) {
            val targetDate = today.plusDays(days.toLong())
             if (expiryDate.isEqual(targetDate)) {
                 return "$itemName expires in $days days"
            }
        }
        return null
    }

    private fun sendNotification(items: List<String>, badgeCount: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // UPDATED Channel ID to force recreation with badge settings
        val channelId = "expiration_reminders_v2" 

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Expiration Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            channel.setShowBadge(true) // Ensure badges are enabled
            notificationManager.createNotificationChannel(channel)
        }

        val message = if (items.size == 1) {
            items.first()
        } else {
            "${items.size} items are expiring soon"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Kards Expiration Alert")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(items.joinToString("\n")))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setNumber(badgeCount) // Set badge count
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
