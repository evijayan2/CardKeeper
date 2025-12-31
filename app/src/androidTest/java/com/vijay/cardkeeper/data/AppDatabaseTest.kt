package com.vijay.cardkeeper.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun getDatabase_should_return_a_non_null_instance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instance = AppDatabase.getDatabase(context, ByteArray(0))
        assertThat(instance).isNotNull()
    }

    @Test
    fun financialAccountDao_should_not_be_null() {
        assertThat(db.financialAccountDao()).isNotNull()
    }

    @Test
    fun identityDocumentDao_should_not_be_null() {
        assertThat(db.identityDocumentDao()).isNotNull()
    }
}
