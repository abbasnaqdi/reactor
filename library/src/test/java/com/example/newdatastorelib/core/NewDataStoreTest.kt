package com.example.newdatastorelib.core

import android.content.Context
import androidx.datastore.core.Serializer
import com.example.newdatastorelib.preferences.PreferencesHandler
import com.example.newdatastorelib.proto.ProtoHandler // Assuming TestProto and TestProtoSerializer are accessible or redefined for test
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
// import org.junit.Assert.assertTrue // Not used directly, can be removed if no other assertions need it.
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import androidx.datastore.preferences.preferencesDataStoreFile // For mocking context extension
import androidx.datastore.dataStoreFile // For mocking context extension

// Minimal Proto classes for testing caching with different serializer instances/types
private data class CacheTestProto1(val id: Int = 0)
private object CacheTestProto1Serializer : Serializer<CacheTestProto1> {
    override val defaultValue: CacheTestProto1 = CacheTestProto1()
    override suspend fun readFrom(input: InputStream): CacheTestProto1 = CacheTestProto1(try { input.read() } catch (e: Exception) {0}) // Basic read
    override suspend fun writeTo(t: CacheTestProto1, output: OutputStream) = output.write(t.id)
}

private data class CacheTestProto2(val name: String = "")
private object CacheTestProto2Serializer : Serializer<CacheTestProto2> {
    override val defaultValue: CacheTestProto2 = CacheTestProto2()
    override suspend fun readFrom(input: InputStream): CacheTestProto2 = CacheTestProto2(try {input.bufferedReader().readLine() ?: ""} catch (e: Exception) {""}) // Basic read
    override suspend fun writeTo(t: CacheTestProto2, output: OutputStream) = output.write(t.name.toByteArray())
}


class NewDataStoreTest {

    private lateinit var mockContext: Context
    private lateinit var newDataStore: NewDataStore

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        // Mock appContext.preferencesDataStoreFile and appContext.dataStoreFile
        // to return unique file paths to avoid issues if DataStore instances were actually created.
        // Although for caching tests, actual DataStore creation might not happen if mocks are used,
        // it's good practice for context mocking.

        // It's important to mock the extension functions on Context correctly.
        // We need to ensure that the `Context` instance used by `NewDataStore` is the one we're mocking.
        val baseFileDir = File("test_files_dir")
        if (!baseFileDir.exists()) baseFileDir.mkdirs() // Ensure dir exists for File creations

        every { mockContext.applicationContext } returns mockContext
        every { mockContext.filesDir } returns baseFileDir // Though not directly used by datastore extensions

        // For Preferences: appContext.preferencesDataStoreFile(name)
        // The extension function is preferencesDataStoreFile(this Context, String)
        // We need to mock it for the specific mockContext instance.
        every { mockContext.preferencesDataStoreFile(any()) } answers { File(baseFileDir, firstArg<String>() + ".preferences_pb") }
        // For Proto: appContext.dataStoreFile(fileName)
        every { mockContext.dataStoreFile(any()) } answers { File(baseFileDir, firstArg<String>()) }


        newDataStore = NewDataStore(mockContext)
        newDataStore.clearCaches() // Ensure clean state before each test
    }

    @Test
    fun `preferences returns cached instance for same name and encryption status`() {
        val handler1 = newDataStore.preferences("prefsA", false)
        val handler2 = newDataStore.preferences("prefsA", false)
        assertSame("Expected same instance for same name and unencrypted", handler1, handler2)

        val handler3 = newDataStore.preferences("prefsB", true)
        val handler4 = newDataStore.preferences("prefsB", true)
        assertSame("Expected same instance for same name and encrypted", handler3, handler4)
    }

    @Test
    fun `preferences returns different instances for different names`() {
        val handler1 = newDataStore.preferences("prefsX", false)
        val handler2 = newDataStore.preferences("prefsY", false)
        assertNotSame("Expected different instances for different names (unencrypted)", handler1, handler2)

        val handler3 = newDataStore.preferences("prefsX", true)
        val handler4 = newDataStore.preferences("prefsY", true)
        assertNotSame("Expected different instances for different names (encrypted)", handler3, handler4)
    }

    @Test
    fun `preferences returns different instances for different encryption statuses`() {
        val handler1 = newDataStore.preferences("prefsZ", false)
        val handler2 = newDataStore.preferences("prefsZ", true)
        assertNotSame("Expected different instances for different encryption statuses", handler1, handler2)
    }

    @Test
    fun `proto returns cached instance for same params`() {
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "protoA.pb", false)
        val handler2 = newDataStore.proto(CacheTestProto1Serializer, "protoA.pb", false)
        assertSame("Expected same ProtoHandler instance for same params (unencrypted)", handler1, handler2)

        val handler3 = newDataStore.proto(CacheTestProto2Serializer, "protoB.pb", true)
        val handler4 = newDataStore.proto(CacheTestProto2Serializer, "protoB.pb", true)
        assertSame("Expected same ProtoHandler instance for same params (encrypted)", handler3, handler4)
    }

    @Test
    fun `proto returns different instances for different filenames`() {
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "protoX.pb", false)
        val handler2 = newDataStore.proto(CacheTestProto1Serializer, "protoY.pb", false)
        assertNotSame(handler1, handler2)
    }

    @Test
    fun `proto returns different instances for different serializers (classes)`() {
        // Cache key uses serializer.javaClass.name
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "common.pb", false)
        val handler2 = newDataStore.proto(CacheTestProto2Serializer, "common.pb", false) // Same filename, different serializer
        assertNotSame("Expected different instances for different serializer types", handler1, handler2)
    }

    @Test
    fun `proto returns different instances for different encryption statuses`() {
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "common_enc.pb", false)
        val handler2 = newDataStore.proto(CacheTestProto1Serializer, "common_enc.pb", true)
        assertNotSame(handler1, handler2)
    }

    @Test
    fun `clearCaches allows new instances to be created`() {
        val prefsHandler1 = newDataStore.preferences("cache_test_prefs", false)
        val protoHandler1 = newDataStore.proto(CacheTestProto1Serializer, "cache_test_proto.pb", false)

        newDataStore.clearCaches()

        val prefsHandler2 = newDataStore.preferences("cache_test_prefs", false)
        val protoHandler2 = newDataStore.proto(CacheTestProto1Serializer, "cache_test_proto.pb", false)

        assertNotSame("PreferencesHandler should be a new instance after clearing cache", prefsHandler1, prefsHandler2)
        assertNotSame("ProtoHandler should be a new instance after clearing cache", protoHandler1, protoHandler2)

        // Verify that caching still works after clearing and getting new instances
        val prefsHandler3 = newDataStore.preferences("cache_test_prefs", false)
        val protoHandler3 = newDataStore.proto(CacheTestProto1Serializer, "cache_test_proto.pb", false)
        assertSame(prefsHandler2, prefsHandler3)
        assertSame(protoHandler2, protoHandler3)
    }
}
