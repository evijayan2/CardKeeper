package com.vijay.cardkeeper.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.vijay.cardkeeper.data.AppDatabase
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FinancialAccountDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FinancialAccountDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.financialAccountDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAccount_and_getAccountById_should_work_correctly() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", holderName = "John Doe", number = "12345", type = AccountType.BANK_ACCOUNT, accountName = "Test Account")
        dao.insertAccount(account)
        val retrieved = dao.getAccountById(1)
        assertThat(retrieved).isEqualTo(account)
    }

    @Test
    fun getAllAccounts_should_return_a_sorted_list_of_accounts() = runBlocking {
        val account1 = FinancialAccount(id = 1, institutionName = "Bank B", holderName = "Jane Doe", number = "222", type = AccountType.CREDIT_CARD, accountName = "Test Card")
        val account2 = FinancialAccount(id = 2, institutionName = "Bank A", holderName = "John Doe", number = "111", type = AccountType.BANK_ACCOUNT, accountName = "Test Account")
        dao.insertAccount(account1)
        dao.insertAccount(account2)

        val accounts = dao.getAllAccounts().first()
        assertThat(accounts).containsExactly(account2, account1).inOrder()
    }

    @Test
    fun updateAccount_should_modify_the_account() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", holderName = "John Doe", number = "12345", type = AccountType.BANK_ACCOUNT, accountName = "Test Account")
        dao.insertAccount(account)
        val updatedAccount = account.copy(holderName = "Jane Doe")
        dao.updateAccount(updatedAccount)
        val retrieved = dao.getAccountById(1)
        assertThat(retrieved).isEqualTo(updatedAccount)
    }

    @Test
    fun deleteAccount_should_remove_the_account() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", holderName = "John Doe", number = "12345", type = AccountType.BANK_ACCOUNT, accountName = "Test Account")
        dao.insertAccount(account)
        dao.deleteAccount(account)
        val retrieved = dao.getAccountById(1)
        assertThat(retrieved).isNull()
    }
}
