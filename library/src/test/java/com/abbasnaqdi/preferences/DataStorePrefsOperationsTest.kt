package com.abbasnaqdi.preferences // Ensure package is correct

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.* // ktlint-disable no-wildcard-imports
import io.mockk.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlinx.coroutines.flow.map // ensure map is imported


class DataStorePrefsOperationsTest {

    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var dataStorePrefsOperations: DataStorePrefsOperations

    // Define key names and types for testing
    private val strKeyName = "test_string"
    private val intKeyName = "test_int"
    private val boolKeyName = "test_bool"
    private val floatKeyName = "test_float"
    private val longKeyName = "test_long"
    private val doubleKeyName = "test_double"
    private val stringSetKeyName = "test_string_set"


    @Before
    fun setUp() {
        mockDataStore = mockk()
        dataStorePrefsOperations = DataStorePrefsOperations(mockDataStore)
    }

    @Test
    fun `putValue stores value correctly`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()

        val testValue = "hello world"
        dataStorePrefsOperations.putValue(strKeyName, testValue) // Use name and value

        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        // Verify internal key creation and set operation
        verify { mockPrefs[stringPreferencesKey(strKeyName)] = testValue }
    }

    @Test
    fun `putValue stores Int value correctly`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()
        val testValue = 123
        dataStorePrefsOperations.putValue(intKeyName, testValue)
        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs[intPreferencesKey(intKeyName)] = testValue }
    }

    @Test
    fun `putValue stores Boolean value correctly`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()
        val testValue = true
        dataStorePrefsOperations.putValue(boolKeyName, testValue)
        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs[booleanPreferencesKey(boolKeyName)] = testValue }
    }

    @Test
    fun `putValue stores Float value correctly`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()
        val testValue = 1.23f
        dataStorePrefsOperations.putValue(floatKeyName, testValue)
        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs[floatPreferencesKey(floatKeyName)] = testValue }
    }

    @Test
    fun `putValue stores Long value correctly`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()
        val testValue = 123L
        dataStorePrefsOperations.putValue(longKeyName, testValue)
        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs[longPreferencesKey(longKeyName)] = testValue }
    }

    @Test
    fun `putValue stores Double value correctly`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()
        val testValue = 1.23
        dataStorePrefsOperations.putValue(doubleKeyName, testValue)
        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs[doublePreferencesKey(doubleKeyName)] = testValue }
    }

    @Test
    fun `putValue stores SetString value correctly`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()
        val testValue = setOf("a", "b")
        dataStorePrefsOperations.putValue(stringSetKeyName, testValue)
        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs[stringSetPreferencesKey(stringSetKeyName)] = testValue }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putValue throws for unsupported Set type`() = runBlocking {
        val testValue = setOf(1, 2) // Set<Int>
        dataStorePrefsOperations.putValue("unsupported_set", testValue)
    }


    @Test
    fun `removeValue calls edit to remove key by name and type`() = runBlocking {
        val slot = slot<suspend (Preferences.MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(slot)) } returns emptyPreferences()

        dataStorePrefsOperations.removeValue(strKeyName, String::class.java) // Use name and type

        val mockPrefs = mockk<Preferences.MutablePreferences>(relaxed = true)
        slot.captured.invoke(mockPrefs)
        verify { mockPrefs.remove(stringPreferencesKey(strKeyName)) }
    }

    @Test
    fun `getValueFlow returns flow of values`() = runBlocking {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey(strKeyName)] } returns "test_value"
        every { mockDataStore.data } returns flowOf(prefs)

        val flow = dataStorePrefsOperations.getValueFlow(strKeyName, String::class.java, null)
        assertEquals("test_value", flow.first())
    }

    @Test
    fun `getValueFlow returns default value if key not present`() = runBlocking {
        every { mockDataStore.data } returns flowOf(emptyPreferences())
        val flow = dataStorePrefsOperations.getValueFlow(strKeyName, String::class.java, "default")
        assertEquals("default", flow.first())
    }

    @Test
    fun `getValueFlow handles IOExceptions and emits empty for default`() = runBlocking {
        every { mockDataStore.data } returns flowOf<Preferences>(mockk()).map<Preferences, Preferences> { throw IOException("Test") }
        val resultFlow = dataStorePrefsOperations.getValueFlow(strKeyName, String::class.java, "defaultValue")
        assertEquals("defaultValue", resultFlow.first())
    }

    @Test
    fun `readValueOnce returns success with value`() = runBlocking {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey(strKeyName)] } returns "test_value"
        every { mockDataStore.data } returns flowOf(prefs)
        val result = dataStorePrefsOperations.readValueOnce(strKeyName, String::class.java, null)
        assertTrue(result.isSuccess)
        assertEquals("test_value", result.getOrNull())
    }

    @Test
    fun `readValueOnce returns success with default value`() = runBlocking {
        every { mockDataStore.data } returns flowOf(emptyPreferences())
        val result = dataStorePrefsOperations.readValueOnce(strKeyName, String::class.java, "default")
        assertTrue(result.isSuccess)
        assertEquals("default", result.getOrNull())
    }

    @Test
    fun `readValueOnce returns failure on exception`() = runBlocking {
        every { mockDataStore.data } returns flowOf<Preferences>(mockk()).map<Preferences, Preferences> { throw IOException("Test Exception") }
        val result = dataStorePrefsOperations.readValueOnce(strKeyName, String::class.java, null)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
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
