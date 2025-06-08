package com.abbasnaqdi.preferences // Updated package

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.* // ktlint-disable no-wildcard-imports
import io.mockk.* // ktlint-disable no-wildcard-imports
// DataStorePrefsOperations is in the same package, so no import needed if it's internal/public
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlinx.coroutines.flow.map // ensure map is imported

class DataStorePrefsOperationsTest { // Renamed for clarity

    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var dataStorePrefsOperations: DataStorePrefsOperations

    private val stringKey = stringPreferencesKey("test_string")
    private val intKey = intPreferencesKey("test_int")

    @Before
    fun setUp() {
        mockDataStore = mockk()
        dataStorePrefsOperations = com.abbasnaqdi.preferences.DataStorePrefsOperations(mockDataStore) // Explicitly use new package
    }

    @Test
    fun `putValue calls edit with correct key-value`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()

        val testValue = "hello world"
        dataStorePrefsOperations.putValue(stringKey, testValue)

        // Verify the lambda behavior
        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs) // Execute the captured lambda
        verify { mockPrefs[stringKey] = testValue } // Verify the set operation
    }

    @Test
    fun `removeValue calls edit to remove key`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()

        dataStorePrefsOperations.removeValue(stringKey)

        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs.remove(stringKey) }
    }

    @Test
    fun `getValueFlow returns flow of values`() = runBlocking {
        val prefs = mockk<Preferences>()
        every { prefs[stringKey] } returns "test_value"
        every { mockDataStore.data } returns flowOf(prefs)

        val flow = dataStorePrefsOperations.getValueFlow(stringKey, null)
        assertEquals("test_value", flow.first())
    }

    @Test
    fun `getValueFlow returns default value if key not present`() = runBlocking {
        every { mockDataStore.data } returns flowOf(emptyPreferences())
        val flow = dataStorePrefsOperations.getValueFlow(stringKey, "default")
        assertEquals("default", flow.first())
    }

    @Test
    fun `getValueFlow handles IOExceptions and emits empty for default`() = runBlocking {
        every { mockDataStore.data } returns flowOf<Preferences>(mockk()).map<Preferences, Preferences> { throw IOException("Test") }
        val resultFlow = dataStorePrefsOperations.getValueFlow(stringKey, "defaultValue")
        // The catch in DataStorePrefsOperations emits emptyPreferences, then map uses defaultValue
        assertEquals("defaultValue", resultFlow.first())
    }

    @Test
    fun `readValueOnce returns success with value`() = runBlocking {
        val prefs = mockk<Preferences>()
        every { prefs[stringKey] } returns "test_value"
        every { mockDataStore.data } returns flowOf(prefs)
        val result = dataStorePrefsOperations.readValueOnce(stringKey, null)
        assertTrue(result.isSuccess)
        assertEquals("test_value", result.getOrNull())
    }

    @Test
    fun `readValueOnce returns success with default value`() = runBlocking {
        every { mockDataStore.data } returns flowOf(emptyPreferences())
        val result = dataStorePrefsOperations.readValueOnce(stringKey, "default")
        assertTrue(result.isSuccess)
        assertEquals("default", result.getOrNull())
    }

    @Test
    fun `readValueOnce returns failure on exception from data first()`() = runBlocking {
        // Simulate error when .first() is called on the data flow
        every { mockDataStore.data } returns flowOf<Preferences>(mockk()).map<Preferences, Preferences> {
            throw IOException("Test Exception from data.first()")
        }
        val result = dataStorePrefsOperations.readValueOnce(stringKey, null)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `readValueOnce returns failure on general exception during read`() = runBlocking {
        // Simulate a non-IOException to ensure it's also caught
        every { mockDataStore.data } returns flowOf<Preferences>(mockk()).map<Preferences, Preferences> {
            throw RuntimeException("Another Test Exception")
        }
        val result = dataStorePrefsOperations.readValueOnce(stringKey, null)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }


    @Test
    fun `clearAll calls edit to clear preferences`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()

        dataStorePrefsOperations.clearAll()

        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs.clear() }
    }
}
