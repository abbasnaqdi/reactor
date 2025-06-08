package com.example.newdatastorelib.preferences

import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.mockk.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EncryptedPrefsOperationsTest {

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var encryptedPrefsOperations: EncryptedPrefsOperations

    // Define keys
    private val intKey = intPreferencesKey("test_int")
    private val longKey = longPreferencesKey("test_long")
    private val floatKey = floatPreferencesKey("test_float")
    private val booleanKey = booleanPreferencesKey("test_boolean")
    private val stringKey = stringPreferencesKey("test_string")
    private val stringSetKey = stringSetPreferencesKey("test_string_set")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher()) // For withContext(Dispatchers.IO)

        mockSharedPreferences = mockk(relaxUnitFun = true) // relaxUnitFun for edit().apply()
        mockEditor = mockk(relaxUnitFun = true) // relaxUnitFun for putX().apply()

        every { mockSharedPreferences.edit() } returns mockEditor
        // Ensure 'contains' returns false by default for read tests unless specified
        every { mockSharedPreferences.contains(any()) } returns false


        encryptedPrefsOperations = EncryptedPrefsOperations(mockSharedPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `putValue Int correctly calls editor`() = runBlocking {
        val value = 123
        encryptedPrefsOperations.putValue(intKey, value)
        coVerify { mockEditor.putInt(intKey.name, value) }
        coVerify { mockEditor.apply() }
    }

    @Test
    fun `putValue Long correctly calls editor`() = runBlocking {
        val value = 123L
        encryptedPrefsOperations.putValue(longKey, value)
        coVerify { mockEditor.putLong(longKey.name, value) }
    }

    @Test
    fun `putValue Float correctly calls editor`() = runBlocking {
        val value = 1.23f
        encryptedPrefsOperations.putValue(floatKey, value)
        coVerify { mockEditor.putFloat(floatKey.name, value) }
    }

    @Test
    fun `putValue Boolean correctly calls editor`() = runBlocking {
        val value = true
        encryptedPrefsOperations.putValue(booleanKey, value)
        coVerify { mockEditor.putBoolean(booleanKey.name, value) }
    }

    @Test
    fun `putValue String correctly calls editor`() = runBlocking {
        val value = "test"
        encryptedPrefsOperations.putValue(stringKey, value)
        coVerify { mockEditor.putString(stringKey.name, value) }
    }

    @Test
    fun `putValue SetString correctly calls editor`() = runBlocking {
        val value = setOf("a", "b")
        encryptedPrefsOperations.putValue(stringSetKey, value)
        coVerify { mockEditor.putStringSet(stringSetKey.name, value) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putValue throws for unsupported Set type`() = runBlocking {
        val value = setOf(1, 2) // Set<Int>
        encryptedPrefsOperations.putValue(intPreferencesKey("unsupported_set_key"), value) // Key type doesn't prevent this, runtime check does
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putValue throws for unsupported type (custom class)`() = runBlocking {
        data class MyClass(val id: Int)
        val value = MyClass(1)
        // Need a key for this, but Preferences.Key is generic. The runtime check is on 'value'.
        encryptedPrefsOperations.putValue(stringPreferencesKey("custom_class_key"), value)
    }


    @Test
    fun `removeValue calls editor remove`() = runBlocking {
        encryptedPrefsOperations.removeValue(stringKey)
        coVerify { mockEditor.remove(stringKey.name) }
        coVerify { mockEditor.apply() }
    }

    @Test
    fun `getValueFlow Int returns correct value`() = runBlocking {
        every { mockSharedPreferences.contains(intKey.name) } returns true
        every { mockSharedPreferences.getInt(intKey.name, 0) } returns 42 // Default passed to getInt
        val flow = encryptedPrefsOperations.getValueFlow(intKey, 0) // Pass default to method
        assertEquals(42, flow.first())
    }

    @Test
    fun `getValueFlow String returns null when not present and no default`() = runBlocking {
        every { mockSharedPreferences.contains(stringKey.name) } returns false
        // For String, the default for getString can be null
        every { mockSharedPreferences.getString(stringKey.name, null) } returns null
        val flow = encryptedPrefsOperations.getValueFlow(stringKey, null) // Default is null
        assertNull(flow.first())
    }

    @Test
    fun `getValueFlow returns default value when key not present`() = runBlocking {
        every { mockSharedPreferences.contains(stringKey.name) } returns false
        val defaultValue = "default"
        // Mocking based on how EncryptedPrefsOperations calls get with defaultValue
        every { mockSharedPreferences.getString(stringKey.name, defaultValue) } returns defaultValue
        val flow = encryptedPrefsOperations.getValueFlow(stringKey, defaultValue)
        assertEquals(defaultValue, flow.first())
    }


    @Test
    fun `readValueOnce Int returns success with value`() = runBlocking {
        every { mockSharedPreferences.contains(intKey.name) } returns true
        every { mockSharedPreferences.getInt(intKey.name, 0) } returns 42
        val result = encryptedPrefsOperations.readValueOnce(intKey, 0)
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `readValueOnce returns default when key not present`() = runBlocking {
        every { mockSharedPreferences.contains(stringKey.name) } returns false
        every { mockSharedPreferences.getString(stringKey.name, "default") } returns "default" // Match the call
        val result = encryptedPrefsOperations.readValueOnce(stringKey, "default")
        assertTrue(result.isSuccess)
        assertEquals("default", result.getOrNull())
    }

    @Test
    fun `readValueOnce returns failure on SharedPreferences exception`() = runBlocking {
        every { mockSharedPreferences.contains(stringKey.name) } returns true
        every { mockSharedPreferences.getString(any(), any()) } throws RuntimeException("Disk error")
        val result = encryptedPrefsOperations.readValueOnce(stringKey, null) // Default is null
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `clearAll calls editor clear`() = runBlocking {
        encryptedPrefsOperations.clearAll()
        coVerify { mockEditor.clear() }
        coVerify { mockEditor.apply() }
    }
}
