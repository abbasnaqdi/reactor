package com.abbasnaqdi.preferences

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
import kotlinx.serialization.Serializable // New import
import org.junit.After
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Test

@Serializable
private data class UserPreferenceTestEnc(val userId: String, val theme: String, val itemsPerPage: Int)

@OptIn(ExperimentalCoroutinesApi::class)
class EncryptedPrefsOperationsTest {

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var encryptedPrefsOperations: EncryptedPrefsOperations

    // Define key names
    private val intKeyName = "test_int_enc"
    private val longKeyName = "test_long_enc"
    private val floatKeyName = "test_float_enc"
    private val booleanKeyName = "test_boolean_enc"
    private val stringKeyName = "test_string_enc"
    private val stringSetKeyName = "test_string_set_enc"
    private val userPrefKeyName = "user_pref_object_encrypted"
    private val userDefault = UserPreferenceTestEnc("defaultEncUser", "light", 25)


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

    // --- Primitive Type Tests (existing structure, updated key names for clarity) ---
    @Test
    fun `putValue Int correctly calls editor`() = runBlocking {
        val value = 123
        encryptedPrefsOperations.putValue(intKeyName, value)
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
        val value = "test_encrypted"
        encryptedPrefsOperations.putValue(stringKeyName, value)
        coVerify { mockEditor.putString(stringKeyName, value) }
    }

    @Test
    fun `putValue Set_String stores value directly via SP putStringSet`() = runBlocking {
        val setValue = setOf("encA", "encB")
        encryptedPrefsOperations.putValue(stringSetKeyName, setValue)
        coVerify { mockEditor.putStringSet(stringSetKeyName, setValue) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putValue throws for unsupported Set type`() = runBlocking {
        val value = setOf(1, 2) // Set<Int>
        encryptedPrefsOperations.putValue("unsupported_set_key_enc", value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putValue throws for unsupported custom type (if not handled by JSON)`() = runBlocking {
        // This test is valid if we assume MyCustom is not @Serializable or there's a specific check
        data class MyCustom(val a: Int)
        encryptedPrefsOperations.putValue("custom_type_key_enc", MyCustom(1))
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
    @Suppress("UNCHECKED_CAST")
    fun `getValueFlow Set_String returns flow of Set_String values via SP getStringSet`() = runBlocking {
        val setValue = setOf("encX", "encY")
        every { mockSharedPreferences.contains(stringSetKeyName) } returns true
        every { mockSharedPreferences.getStringSet(stringSetKeyName, null) } returns setValue

        val flow = encryptedPrefsOperations.getValueFlow(stringSetKeyName, Set::class.java as Class<Set<String>>, null)
        assertEquals(setValue, flow.first())
    }

    @Test
    fun `readValueOnce returns default when key not present (Int)`() = runBlocking {
        every { mockSharedPreferences.contains(intKeyName) } returns false
        every { mockSharedPreferences.getInt(intKeyName, 101) } returns 101 // Default for getInt
        val result = encryptedPrefsOperations.readValueOnce(intKeyName, Int::class.java, 101)
        assertTrue(result.isSuccess)
        assertEquals(101, result.getOrNull())
    }

    @Test
    fun `clearAll calls editor clear`() = runBlocking {
        encryptedPrefsOperations.clearAll()
        coVerify { mockEditor.clear() }
        coVerify { mockEditor.apply() }
    }

    // --- Tests for JSON Serialization of Custom Objects ---
    @Test
    fun `putValue custom object serializes to JSON string via SP putString`() = runBlocking {
        val user = UserPreferenceTestEnc("encUser123", "dark", 50)
        encryptedPrefsOperations.putValue(userPrefKeyName, user)
        coVerify { mockEditor.putString(eq(userPrefKeyName), any<String>()) }
        coVerify { mockEditor.apply() }
    }

    @Test
    fun `getValueFlow custom object deserializes from JSON string via SP getString`() = runBlocking {
        val originalUser = UserPreferenceTestEnc("encUser456", "auto", 10)
        val jsonString = """{"userId":"encUser456","theme":"auto","itemsPerPage":10}"""

        every { mockSharedPreferences.contains(userPrefKeyName) } returns true
        every { mockSharedPreferences.getString(userPrefKeyName, null) } returns jsonString

        val flow = encryptedPrefsOperations.getValueFlow(userPrefKeyName, UserPreferenceTestEnc::class.java, null)
        val resultUser = flow.first()

        assertNotNull(resultUser)
        assertEquals(originalUser, resultUser)
    }

    @Test
    fun `getValueFlow custom object returns default if JSON is invalid`() = runBlocking {
        every { mockSharedPreferences.contains(userPrefKeyName) } returns true
        every { mockSharedPreferences.getString(userPrefKeyName, null) } returns "{invalid_json"

        val flow = encryptedPrefsOperations.getValueFlow(userPrefKeyName, UserPreferenceTestEnc::class.java, userDefault)
        assertEquals("Default user should be returned on JSON error", userDefault, flow.first())
    }

    @Test
    fun `getValueFlow custom object returns default if key not present`() = runBlocking {
        every { mockSharedPreferences.contains(userPrefKeyName) } returns false
        // getString will be called with null default, then our logic should provide userDefault
        every { mockSharedPreferences.getString(userPrefKeyName, null) } returns null

        val flow = encryptedPrefsOperations.getValueFlow(userPrefKeyName, UserPreferenceTestEnc::class.java, userDefault)
        assertEquals("Default user should be returned if key not present", userDefault, flow.first())
    }

    @Test
    fun `readValueOnce custom object deserializes from JSON string`() = runBlocking {
        val originalUser = UserPreferenceTestEnc("encUser789", "system", 5)
        val jsonString = """{"userId":"encUser789","theme":"system","itemsPerPage":5}"""

        every { mockSharedPreferences.contains(userPrefKeyName) } returns true
        every { mockSharedPreferences.getString(userPrefKeyName, null) } returns jsonString

        val result = encryptedPrefsOperations.readValueOnce(userPrefKeyName, UserPreferenceTestEnc::class.java, null)
        assertTrue(result.isSuccess)
        assertEquals(originalUser, result.getOrNull())
    }

    @Test
    fun `readValueOnce custom object returns default if JSON is invalid`() = runBlocking {
        every { mockSharedPreferences.contains(userPrefKeyName) } returns true
        every { mockSharedPreferences.getString(userPrefKeyName, null) } returns "{invalid_json_for_read_once"

        val result = encryptedPrefsOperations.readValueOnce(userPrefKeyName, UserPreferenceTestEnc::class.java, userDefault)
        // The current implementation of readValueOnce for EncryptedPrefsOperations, when JSON deserialization fails,
        // will fall back to defaultValue and return Result.success(defaultValue).
        assertTrue("Result should be success (returning default) even if JSON is invalid", result.isSuccess)
        assertEquals("Default user should be returned on JSON error for readOnce", userDefault, result.getOrNull())
    }

    @Test
    fun `readValueOnce custom object returns default if key not present`() = runBlocking {
        every { mockSharedPreferences.contains(userPrefKeyName) } returns false
        every { mockSharedPreferences.getString(userPrefKeyName, null) } returns null

        val result = encryptedPrefsOperations.readValueOnce(userPrefKeyName, UserPreferenceTestEnc::class.java, userDefault)
        assertTrue(result.isSuccess)
        assertEquals("Default user should be returned if key not present for readOnce", userDefault, result.getOrNull())
    }


    @Test
    fun `removeValue for custom object calls editor remove`() = runBlocking {
        encryptedPrefsOperations.removeValue(userPrefKeyName, UserPreferenceTestEnc::class.java)
        coVerify { mockEditor.remove(userPrefKeyName) }
    }
}
