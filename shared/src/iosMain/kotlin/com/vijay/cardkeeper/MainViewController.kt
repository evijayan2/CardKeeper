package com.vijay.cardkeeper

import androidx.compose.runtime.*
import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.vijay.cardkeeper.data.repository.*
import com.vijay.cardkeeper.ui.home.HomeScreen
import com.vijay.cardkeeper.ui.viewmodel.*
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import com.vijay.cardkeeper.ui.item.AddItemScreen
import com.vijay.cardkeeper.ui.item.ScanRequestType
import com.vijay.cardkeeper.ui.item.forms.*
import com.vijay.cardkeeper.domain.ExpirationScheduler
import com.vijay.cardkeeper.ui.item.*
import com.vijay.cardkeeper.ui.search.SearchScreen
import com.vijay.cardkeeper.ui.settings.SettingsScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import okio.Path.Companion.toPath
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize

// No-op implementation for iOS background work primarily
class IosExpirationScheduler : ExpirationScheduler {
    override fun scheduleExpirationCheck() {
        println("IOS: Scheduling expiration check (Background tasks not fully implemented)")
    }
}

// DataStore helper
@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            val path = requireNotNull(documentDirectory).path + "/user_preferences.preferences_pb"
            path.toPath()
        }
    )
}

fun interface ScanResultCallback {
    fun onScanResult(path: String, ocrText: List<String>?, barcode: String?)
}

fun MainViewController(
    onLaunchCamera: (ScanResultCallback) -> Unit
) = ComposeUIViewController {
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var homeViewModel: HomeViewModel? by remember { mutableStateOf(null) }
    var addItemViewModel: AddItemViewModel? by remember { mutableStateOf(null) }
    var viewItemViewModel: ViewItemViewModel? by remember { mutableStateOf(null) }
    var searchViewModel: SearchViewModel? by remember { mutableStateOf(null) }
    var settingsViewModel: SettingsViewModel? by remember { mutableStateOf(null) }

    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    val financialState = rememberFinancialFormState()
    val rewardsState = rememberRewardsFormState(null)
    val identityState = rememberIdentityFormState(null)
    val passportState = rememberPassportFormState(null)
    val greenCardState = rememberGreenCardFormState(null)
    val aadharCardState = rememberAadharCardFormState(null)
    val giftCardState = rememberGiftCardFormState(null)
    val panCardState = rememberPanCardFormState(null)
    val insuranceState = rememberInsuranceCardFormState(null)

    val userPreferencesRepository = remember { 
         UserPreferencesRepository(createDataStore())
    }

    val driver = remember {
        try {
            NativeSqliteDriver(SqlDelightDatabase.Schema, "cardkeeper.db")
        } catch (e: Exception) {
            errorMsg = "DB Error: ${e.message}"
            null
        }
    }

    if (driver != null) {
        val database = remember { SqlDelightDatabase(driver) }
        val financialRepository = remember { FinancialRepository(database) }
        val identityRepository = remember { IdentityRepository(database) }
        val passportRepository = remember { PassportRepository(database) }
        val greenCardRepository = remember { GreenCardRepository(database) }
        val aadharCardRepository = remember { AadharCardRepository(database) }
        val giftCardRepository = remember { GiftCardRepositoryImpl(database) }
        val panCardRepository = remember { PanCardRepository(database) }
        val rewardCardRepository = remember { RewardCardRepositoryImpl(database) }
        val insuranceCardRepository = remember { InsuranceCardRepository(database) }
        
        val searchRepository = remember {
            SearchRepository(
                financialRepository, identityRepository, passportRepository,
                greenCardRepository, aadharCardRepository, rewardCardRepository,
                giftCardRepository, panCardRepository
            )
        }

        homeViewModel = remember {
            HomeViewModel(
                financialRepository, identityRepository, passportRepository,
                greenCardRepository, aadharCardRepository, giftCardRepository,
                panCardRepository, rewardCardRepository, insuranceCardRepository
            )
        }

        addItemViewModel = remember {
            AddItemViewModel(
                financialRepository, identityRepository, passportRepository,
                greenCardRepository, aadharCardRepository, giftCardRepository,
                panCardRepository, rewardCardRepository, insuranceCardRepository,
                IosExpirationScheduler()
            )
        }
        
        viewItemViewModel = remember {
            ViewItemViewModel(
                financialRepository, giftCardRepository, identityRepository,
                passportRepository, greenCardRepository, aadharCardRepository,
                panCardRepository, rewardCardRepository, insuranceCardRepository
            )
        }
        
        searchViewModel = remember { SearchViewModel(searchRepository) }
        settingsViewModel = remember { SettingsViewModel(userPreferencesRepository) }
    }

    if (errorMsg != null) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("Init Error: ${errorMsg}")
        }
    } else if (homeViewModel != null && addItemViewModel != null && viewItemViewModel != null && searchViewModel != null && settingsViewModel != null) {
        when (val currentScreen = screen) {
            is Screen.Home -> {
                HomeScreen(
                    navigateToItemEntry = { category, type -> 
                        screen = Screen.AddItem(category, type ?: "CREDIT_CARD") 
                    },
                    navigateToItemView = { id -> screen = Screen.ViewItem(id) },
                    navigateToIdentityView = { id -> screen = Screen.ViewIdentity(id) },
                    navigateToPassportView = { id -> screen = Screen.ViewPassport(id) },
                    navigateToGreenCardView = { id -> screen = Screen.ViewGreenCard(id) },
                    navigateToAadharView = { id -> screen = Screen.ViewAadhar(id) },
                    navigateToPanCardView = { id -> screen = Screen.ViewPanCard(id) },
                    navigateToGiftCardView = { id -> screen = Screen.ViewGiftCard(id) },
                    navigateToRewardsView = { id -> screen = Screen.ViewRewards(id) },
                    navigateToInsuranceView = { id -> screen = Screen.ViewInsurance(id) },
                    navigateToSearch = { screen = Screen.Search },
                    navigateToSettings = { screen = Screen.Settings },
                    viewModel = homeViewModel!!,
                    onCopyContent = { content -> println("Copy Content: $content") }
                )
            }
            is Screen.AddItem -> {
                com.vijay.cardkeeper.ui.item.AddItemScreen(
                    financialState = financialState,
                    rewardsState = rewardsState,
                    identityState = identityState,
                    passportState = passportState,
                    greenCardState = greenCardState,
                    aadharCardState = aadharCardState,
                    giftCardState = giftCardState,
                    panCardState = panCardState,
                    insuranceState = insuranceState,
                    selectedCategory = currentScreen.category,
                    onCategorySelected = { /* Handle category switch logic */ },

                    onScanRequest = { category, requestType -> 
                             val listener = object : ScanResultCallback {
                                 override fun onScanResult(path: String, ocrText: List<String>?, barcode: String?) {
                                    // Always update image path first
                                    when (category) {
                                        0 -> { // Financial
                                            if (requestType == ScanRequestType.FRONT) { financialState.frontPath = path; financialState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { financialState.backPath = path; financialState.hasBackImage = true }
                                            
                                            // Parse OCR Text for Credit Card
                                            if (ocrText != null) {
                                                 val parser = com.vijay.cardkeeper.domain.utils.CardTextParser()
                                                 val details = parser.parse(ocrText)
                                                 if (details.number.isNotEmpty()) financialState.number = details.number
                                                 if (details.expiryDate.isNotEmpty()) financialState.expiry = details.expiryDate
                                                 if (details.ownerName.isNotEmpty()) financialState.holder = details.ownerName
                                                 if (details.scheme.isNotEmpty() && details.scheme != "Unknown") financialState.network = details.scheme
                                                 if (details.securityCode.isNotEmpty()) financialState.cvv = details.securityCode
                                            }
                                        }
                                        1 -> { // Identity (Driver License)
                                            if (requestType == ScanRequestType.FRONT) { identityState.frontPath = path; identityState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { 
                                                identityState.backPath = path; identityState.hasBackImage = true 
                                                // Parse Barcode for DL Back
                                                if (barcode != null) {
                                                    val parser = com.vijay.cardkeeper.domain.utils.AamvaParser()
                                                    val details = parser.parse(barcode)
                                                    if (details.docNumber.isNotEmpty()) identityState.number = details.docNumber
                                                    if (details.name.isNotEmpty()) {
                                                        identityState.firstName = details.name.substringBefore(" ")
                                                        identityState.lastName = details.name.substringAfterLast(" ", "")
                                                    }
                                                    if (details.dob.isNotEmpty()) identityState.rawDob = details.dob.filter { it.isDigit() }
                                                    if (details.expiryDate.isNotEmpty()) identityState.rawExpiry = details.expiryDate.filter { it.isDigit() }
                                                    if (details.address.isNotEmpty()) identityState.address = details.address
                                                    if (details.sex.isNotEmpty()) identityState.sex = details.sex
                                                    if (details.eyeColor.isNotEmpty()) identityState.eyeColor = details.eyeColor
                                                    if (details.height.isNotEmpty()) identityState.height = details.height
                                                    if (details.state.isNotEmpty()) identityState.region = details.state
                                                }
                                            }
                                        }
                                        2 -> { // Passport
                                            if (requestType == ScanRequestType.FRONT) { passportState.frontPath = path; passportState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { passportState.backPath = path; passportState.hasBackImage = true }
                                        }
                                        3 -> { // Rewards
                                             if (requestType == ScanRequestType.FRONT) { rewardsState.frontPath = path; rewardsState.hasFrontImage = true }
                                             else if (requestType == ScanRequestType.BACK) { rewardsState.backPath = path; rewardsState.hasBackImage = true }
                                             // Rewards OCR
                                            if (ocrText != null) {
                                                 val parser = com.vijay.cardkeeper.domain.utils.CardTextParser()
                                                 val details = parser.parse(ocrText)
                                                 // Use number as barcode/card number fallback
                                                 if (details.number.isNotEmpty()) rewardsState.barcode = details.number // Heuristic
                                            }
                                        }
                                        4 -> { // Green Card
                                            if (requestType == ScanRequestType.FRONT) { greenCardState.frontPath = path; greenCardState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { greenCardState.backPath = path; greenCardState.hasBackImage = true }
                                        }
                                        5 -> { // Aadhaar
                                            if (requestType == ScanRequestType.FRONT) { aadharCardState.frontPath = path; aadharCardState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { aadharCardState.backPath = path; aadharCardState.hasBackImage = true }
                                        }
                                        6 -> { // Gift Card
                                            if (requestType == ScanRequestType.FRONT) { giftCardState.frontPath = path; giftCardState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { giftCardState.backPath = path; giftCardState.hasBackImage = true }
                                        }
                                        7 -> { // PAN Card
                                            if (requestType == ScanRequestType.FRONT) { panCardState.frontPath = path; panCardState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { panCardState.backPath = path; panCardState.hasBackImage = true }
                                        }
                                        8 -> { // Insurance
                                            if (requestType == ScanRequestType.FRONT) { insuranceState.frontPath = path; insuranceState.hasFrontImage = true }
                                            else if (requestType == ScanRequestType.BACK) { insuranceState.backPath = path; insuranceState.hasBackImage = true }
                                        }
                                    }
                                 }
                             }
                             
                             onLaunchCamera(listener)
                    },
                    onSave = {
                          when (currentScreen.category) {
                             0 -> addItemViewModel!!.saveFinancialAccount(
                                 type = financialState.type,
                                 institution = financialState.institution,
                                 name = financialState.accName,
                                 holder = financialState.holder,
                                 number = financialState.number,
                                 routing = financialState.routing,
                                 ifsc = financialState.ifsc,
                                 swift = financialState.swift,
                                 expiryDate = financialState.formattedExpiry,
                                 cvv = financialState.cvv,
                                 pin = financialState.pin,
                                 notes = financialState.notes,
                                 contact = financialState.contact,
                                 cardNetwork = financialState.network,
                                 frontImagePath = financialState.frontPath,
                                 backImagePath = financialState.backPath
                             )
                             1 -> addItemViewModel!!.saveIdentityDocument(
                                 type = identityState.type,
                                 country = identityState.country,
                                 docNumber = identityState.number,
                                 holder = "${identityState.firstName} ${identityState.lastName}".trim(),
                                 expiryDate = identityState.expiry,
                                 dob = identityState.dob,
                                 address = identityState.address,
                                 state = identityState.region,
                                 sex = identityState.sex,
                                 eyeColor = identityState.eyeColor,
                                 height = identityState.height,
                                 licenseClass = identityState.licenseClass,
                                 restrictions = identityState.restrictions,
                                 endorsements = identityState.endorsements,
                                 issuingAuthority = identityState.issuingAuthority,
                                 frontImagePath = identityState.frontPath,
                                 backImagePath = identityState.backPath
                             )
                             2 -> addItemViewModel!!.savePassport(
                                 firstName = passportState.givenNames,
                                 lastName = passportState.surname,
                                 passportNumber = passportState.passportNumber,
                                 nationality = passportState.nationality,
                                 dob = passportState.dob,
                                 dateOfIssue = passportState.dateOfIssue,
                                 dateOfExpiry = passportState.dateOfExpiry,
                                 placeOfIssue = passportState.placeOfIssue,
                                 placeOfBirth = passportState.placeOfBirth,
                                 sex = passportState.sex,
                                 authority = passportState.authority,
                                 frontImagePath = passportState.frontPath,
                                 backImagePath = passportState.backPath
                             )
                             3 -> addItemViewModel!!.saveRewardCard(
                                 merchantName = rewardsState.institution,
                                 cardNumber = rewardsState.barcode,
                                 pin = null, // Not in state
                                 expiryDate = null, // Not in state
                                 points = null, // Not in state
                                 email = null, // Not in state
                                 phone = rewardsState.linkedPhone,
                                 website = null, // Not in state
                                 notes = rewardsState.notes,
                                 barcodeType = "CODE_128", 
                                 barcodeValue = rewardsState.barcode,
                                 frontImagePath = rewardsState.frontPath,
                                 backImagePath = rewardsState.backPath
                             )
                             4 -> addItemViewModel!!.saveGreenCard(
                                 firstName = greenCardState.givenName,
                                 lastName = greenCardState.surname,
                                 uscisNumber = greenCardState.uscisNumber,
                                 category = greenCardState.category,
                                 countryOfBirth = greenCardState.countryOfBirth,
                                 dob = greenCardState.dob,
                                 dateOfExpiry = greenCardState.expiryDate,
                                 residentSince = greenCardState.residentSince,
                                 sex = greenCardState.sex,
                                 frontImagePath = greenCardState.frontPath,
                                 backImagePath = greenCardState.backPath
                             )
                             5 -> addItemViewModel!!.saveAadharCard(
                                 name = aadharCardState.holderName,
                                 aadharNumber = aadharCardState.uid,
                                 dob = aadharCardState.dob,
                                 gender = aadharCardState.gender,
                                 address = aadharCardState.address,
                                 vid = aadharCardState.vid,
                                 frontImagePath = aadharCardState.frontPath,
                                 backImagePath = aadharCardState.backPath
                             )
                             6 -> addItemViewModel!!.saveGiftCard(
                                 merchantName = giftCardState.providerName,
                                 cardNumber = giftCardState.cardNumber,
                                 pin = giftCardState.pin,
                                 balance = null, // Form state doesn't have balance
                                 expiryDate = null, // Form state doesn't have expiry
                                 frontImagePath = giftCardState.frontPath,
                                 backImagePath = giftCardState.backPath
                             )
                             7 -> addItemViewModel!!.savePanCard(
                                 name = panCardState.holderName,
                                 fatherName = panCardState.fatherName,
                                 panNumber = panCardState.panNumber,
                                 dob = panCardState.dob,
                                 category = null, // No category in form state
                                 frontImagePath = panCardState.frontPath,
                                 backImagePath = panCardState.backPath
                             )
                             8 -> addItemViewModel!!.saveInsuranceCard(
                                 provider = insuranceState.providerName,
                                 policyNumber = insuranceState.policyNumber,
                                 groupNumber = insuranceState.groupNumber,
                                 holderName = insuranceState.policyHolderName,
                                 validTill = insuranceState.expiryDate,
                                 notes = insuranceState.notes,
                                 frontImagePath = insuranceState.frontPath,
                                 backImagePath = insuranceState.backPath
                             )
                         }
                         screen = Screen.Home 
                    },
                    onNavigateBack = { screen = Screen.Home },
                    isEditing = false,
                    showCategoryTabs = currentScreen.type == null,
                    title = when (currentScreen.type) {
                        "CREDIT_CARD" -> "Add Credit/Debit Card"
                        "BANK_ACCOUNT" -> "Add Bank Account"
                        "DRIVER_LICENSE" -> "Add Driver License"
                        "PASSPORT" -> "Add Passport"
                        "GREEN_CARD" -> "Add Green Card"
                        "AADHAR" -> "Add Aadhaar Card"
                        "PAN" -> "Add PAN Card"
                        "GIFT_CARD" -> "Add Gift Card"
                        "REWARDS_CARD" -> "Add Rewards Card"
                        "INSURANCE" -> "Add Insurance Card"
                        else -> "Add Item"
                    }
                )
            }
            is Screen.ViewItem -> {
                 com.vijay.cardkeeper.ui.item.ViewItemScreen(
                     itemId = currentScreen.id,
                     navigateBack = { screen = Screen.Home },
                     onEditClick = { id -> println("Edit item: $id") },
                     onCopyContent = { label, content -> println("Copy $label: $content") },
                     onLaunchUrl = { url -> println("Launch URL: $url") },
                     onDialNumber = { number -> println("Dial: $number") },
                     barcodeContent = { content, format -> Text("Barcode: $content") },
                     viewModel = viewItemViewModel!!
                 )
            }
            is Screen.ViewIdentity -> {
                com.vijay.cardkeeper.ui.item.ViewIdentityScreen(
                    documentId = currentScreen.id,
                    navigateBack = { screen = Screen.Home },
                     onEditClick = { id -> println("Edit item: $id") },
                     viewModel = viewItemViewModel!!
                )
            }
            is Screen.ViewPassport -> {
                com.vijay.cardkeeper.ui.item.ViewPassportScreen(
                    passportId = currentScreen.id,
                    navigateBack = { screen = Screen.Home },
                    onEditClick = { id -> println("Edit item: $id") },
                    onCopyContent = { label, content -> println("Copy $label: $content") },
                    viewModel = viewItemViewModel!!
                )
            }
            is Screen.ViewGreenCard -> {
                com.vijay.cardkeeper.ui.item.ViewGreenCardScreen(
                    greenCardId = currentScreen.id,
                    navigateBack = { screen = Screen.Home },
                    onEditClick = { id -> println("Edit item: $id") },
                    onCopyContent = { label, content -> println("Copy $label: $content") },
                    viewModel = viewItemViewModel!!
                )
            }
            is Screen.ViewAadhar -> {
                com.vijay.cardkeeper.ui.item.ViewAadharCardScreen(
                    aadharCardId = currentScreen.id,
                    navigateBack = { screen = Screen.Home },
                    onEditClick = { id -> println("Edit item: $id") },
                    onCopyContent = { label, content -> println("Copy $label: $content") },
                    onDecodeBase64 = { null },
                    onGenerateQrCode = { null },
                    viewModel = viewItemViewModel!!
                )
            }
            is Screen.ViewPanCard -> {
                com.vijay.cardkeeper.ui.item.ViewPanCardScreen(
                    panCardId = currentScreen.id,
                    navigateBack = { screen = Screen.Home },
                    onEditClick = { id -> println("Edit item: $id") },
                    onCopyContent = { label, content -> println("Copy $label: $content") },
                    viewModel = viewItemViewModel!!
                )
            }
            is Screen.ViewGiftCard -> {
                com.vijay.cardkeeper.ui.item.ViewGiftCardScreen(
                    giftCardId = currentScreen.id,
                    navigateBack = { screen = Screen.Home },
                    onEdit = { id -> println("Edit item: $id") },
                    onCopyContent = { content, label -> println("Copy $label: $content") },
                    qrCodeContent = { content -> Text("QR Code: $content") },
                    barcodeContent = { content, format -> Text("Barcode: $content") },
                    viewModel = viewItemViewModel!!
                )
            }
             is Screen.ViewRewards -> {
                com.vijay.cardkeeper.ui.item.ViewRewardsScreen(
                    itemId = currentScreen.id,
                    navigateBack = { screen = Screen.Home },
                     onEditClick = { id -> println("Edit item: $id") },
                     onGenerateBarcode = { type, value -> null },
                     onDialNumber = { println("Dial: $it") },
                     onSetBrightness = { println("Brightness: $it") },
                     viewModel = viewItemViewModel!!
                )
            }
            is Screen.ViewInsurance -> {
                LaunchedEffect(currentScreen.id) { viewItemViewModel!!.loadInsuranceCard(currentScreen.id) }
                val insuranceCard by viewItemViewModel!!.selectedInsuranceCard.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                if (insuranceCard != null) {
                    com.vijay.cardkeeper.ui.item.ViewInsuranceCardScreen(
                        card = insuranceCard!!,
                        onBackClick = { screen = Screen.Home },
                        onEditClick = { println("Edit Insurance: ${insuranceCard!!.id}") },
                        onDeleteClick = { 
                            viewItemViewModel!!.deleteInsuranceCard(insuranceCard!!)
                            screen = Screen.Home
                        },
                        onCopyContent = { content, label -> println("Copy $label: $content") },
                        viewModel = viewItemViewModel!!,
                        snackbarHostState = snackbarHostState
                    )
                } else {
                     androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                         androidx.compose.material3.CircularProgressIndicator()
                    }
                }
            }
             is Screen.Search -> {
                com.vijay.cardkeeper.ui.search.SearchScreen(
                    onNavigateBack = { screen = Screen.Home },
                    onResultClick = { id, type -> 
                        println("Navigate from search: $id, $type")
                        when {
                            type.contains("Passport", ignoreCase = true) -> screen = Screen.ViewPassport(id)
                            type.contains("Green", ignoreCase = true) -> screen = Screen.ViewGreenCard(id)
                            type.contains("Aadhar", ignoreCase = true) -> screen = Screen.ViewAadhar(id)
                            type.contains("Pan", ignoreCase = true) -> screen = Screen.ViewPanCard(id)
                            type.contains("Gift", ignoreCase = true) -> screen = Screen.ViewGiftCard(id)
                            type.contains("Reward", ignoreCase = true) -> screen = Screen.ViewRewards(id)
                            type.contains("Insurance", ignoreCase = true) -> screen = Screen.ViewInsurance(id)
                            type.contains("Identity", ignoreCase = true) -> screen = Screen.ViewIdentity(id)
                            else -> screen = Screen.ViewItem(id)
                        }
                    },
                    viewModel = searchViewModel!!
                )
            }
             is Screen.Settings -> {
                com.vijay.cardkeeper.ui.settings.SettingsScreen(
                    navigateBack = { screen = Screen.Home },
                    viewModel = settingsViewModel!!
                )
            }
        }
    }
}

sealed interface Screen {
    data object Home : Screen
    data class AddItem(val category: Int, val type: String?) : Screen
    data class ViewItem(val id: Int) : Screen
    data class ViewIdentity(val id: Int) : Screen
    data class ViewPassport(val id: Int) : Screen
    data class ViewGreenCard(val id: Int) : Screen
    data class ViewAadhar(val id: Int) : Screen
    data class ViewPanCard(val id: Int) : Screen
    data class ViewGiftCard(val id: Int) : Screen
    data class ViewRewards(val id: Int) : Screen
    data class ViewInsurance(val id: Int) : Screen
    data object Search : Screen
    data object Settings : Screen
}
