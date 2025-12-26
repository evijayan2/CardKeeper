package com.vijay.cardkeeper.di

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultAppContainerTest {

    private lateinit var container: AppContainer
    private val context: Context = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        container = DefaultAppContainer(context)
    }

    @Test
    fun `financialRepository should not be null`() {
        assertThat(container.financialRepository).isNotNull()
    }

    @Test
    fun `identityRepository should not be null`() {
        assertThat(container.identityRepository).isNotNull()
    }
}
