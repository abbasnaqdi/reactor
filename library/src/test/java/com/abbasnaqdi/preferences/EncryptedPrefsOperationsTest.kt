package com.abbasnaqdi.preferences // Ensure package is correct

import android.content.SharedPreferences
// No longer need to import specific key types like intPreferencesKey directly for method calls
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

    // Define key names
    private val intKeyName = "test_int"
    private val longKeyName = "test_long"
    private val floatKeyName = "test_float"
    private val booleanKeyName = "test_boolean"
    private val stringKeyName = "test_string"
    private val stringSetKeyName = "test_string_set"


    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockSharedPreferences = mockk(relaxUnitFun = true)
        mockEditor = mockk(relaxUnitFun = true)
        every { mockSharedPreferences.edit() } returns mockEditor
        // Default mock for contains, can be overridden in specific tests
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
        encryptedPrefsOperations.putValue(intKeyName, value) // Use name
        coVerify { mockEditor.putInt(intKeyName, value) }
        coVerify { mockEditor.apply() }
    }

    @Test
    fun `putValue Long correctly calls editor`() = runBlocking {
        val value = 123L
        encryptedPrefsOperations.putValue(longKeyName, value)
        coVerify { mockEditor.putLong(longKeyName, value) }
    }

    @Test
    fun `putValue Float correctly calls editor`() = runBlocking {
        val value = 1.23f
        encryptedPrefsOperations.putValue(floatKeyName, value)
        coVerify { mockEditor.putFloat(floatKeyName, value) }
    }

    @Test
    fun `putValue Boolean correctly calls editor`() = runBlocking {
        val value = true
        encryptedPrefsOperations.putValue(booleanKeyName, value)
        coVerify { mockEditor.putBoolean(booleanKeyName, value) }
    }

    @Test
    fun `putValue String correctly calls editor`() = runBlocking {
        val value = "test"
        encryptedPrefsOperations.putValue(stringKeyName, value)
        coVerify { mockEditor.putString(stringKeyName, value) }
    }

    @Test
    fun `putValue SetString correctly calls editor`() = runBlocking {
        val value = setOf("a", "b")
        encryptedPrefsOperations.putValue(stringSetKeyName, value)
        coVerify { mockEditor.putStringSet(stringSetKeyName, value) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putValue throws for unsupported Set type`() = runBlocking {
        val value = setOf(1, 2) // Set<Int>
        encryptedPrefsOperations.putValue("unsupported_set_key", value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putValue throws for unsupported custom type`() = runBlocking {
        data class MyCustom(val a: Int)
        encryptedPrefsOperations.putValue("custom_type_key", MyCustom(1))
    }

    @Test
    fun `removeValue calls editor remove`() = runBlocking {
        encryptedPrefsOperations.removeValue(stringKeyName, String::class.java)
        coVerify { mockEditor.remove(stringKeyName) }
        coVerify { mockEditor.apply() }
    }

    @Test
    fun `getValueFlow Int returns correct value`() = runBlocking {
        every { mockSharedPreferences.contains(intKeyName) } returns true
        every { mockSharedPreferences.getInt(intKeyName, 0) } returns 42
        val flow = encryptedPrefsOperations.getValueFlow(intKeyName, Int::class.java, 0)
        assertEquals(42, flow.first())
    }

    @Test
    fun `getValueFlow returns default when key not present`() = runBlocking {
        every { mockSharedPreferences.contains(stringKeyName) } returns false
        every { mockSharedPreferences.getString(stringKeyName, "default") } returns "default"
        val flow = encryptedPrefsOperations.getValueFlow(stringKeyName, String::class.java, "default")
        assertEquals("default", flow.first())
    }

    @Test
    fun `getValueFlow returns null for String when key not present and default is null`() = runBlocking {
        every { mockSharedPreferences.contains(stringKeyName) } returns false
        every { mockSharedPreferences.getString(stringKeyName, null) } returns null
        val flow = encryptedPrefsOperations.getValueFlow(stringKeyName, String::class.java, null)
        assertNull(flow.first())
    }

    @Test
    fun `readValueOnce Int returns success with value`() = runBlocking {
        every { mockSharedPreferences.contains(intKeyName) } returns true
        every { mockSharedPreferences.getInt(intKeyName, 0) } returns 42
        val result = encryptedPrefsOperations.readValueOnce(intKeyName, Int::class.java, 0)
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `readValueOnce returns default when key not present`() = runBlocking {
        every { mockSharedPreferences.contains(stringKeyName) } returns false
        every { mockSharedPreferences.getString(stringKeyName, "default") } returns "default"
        val result = encryptedPrefsOperations.readValueOnce(stringKeyName, String::class.java, "default")
        assertTrue(result.isSuccess)
        assertEquals("default", result.getOrNull())
    }

    @Test
    fun `readValueOnce returns failure on SharedPreferences exception`() = runBlocking {
        every { mockSharedPreferences.contains(stringKeyName) } returns true
        every { mockSharedPreferences.getString(stringKeyName, null) } throws RuntimeException("Disk error")
        val result = encryptedPrefsOperations.readValueOnce(stringKeyName, String::class.java, null)
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
