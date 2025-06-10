package com.abbasnaqdi.core // Ensure package is correct

import android.content.Context
import androidx.datastore.core.DataMigration // Add import
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences // Add import
// com.abbasnaqdi.preferences.PreferencesHandler and com.abbasnaqdi.proto.ProtoHandler are classes under test via NewDataStore
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
// import org.junit.Assert.assertTrue // Not used in this snippet, but fine if present
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import androidx.datastore.preferences.preferencesDataStoreFile // For mocking context extension
import androidx.datastore.dataStoreFile // For mocking context extension


// Minimal Proto classes for testing caching (remain the same)
private data class CacheTestProto1(val id: Int = 0)
private object CacheTestProto1Serializer : Serializer<CacheTestProto1> {
    override val defaultValue: CacheTestProto1 = CacheTestProto1()
    override suspend fun readFrom(input: InputStream): CacheTestProto1 = CacheTestProto1(try{input.read()}catch(e: Exception){0})
    override suspend fun writeTo(t: CacheTestProto1, output: OutputStream) = output.write(t.id)
}

private data class CacheTestProto2(val name: String = "")
private object CacheTestProto2Serializer : Serializer<CacheTestProto2> {
    override val defaultValue: CacheTestProto2 = CacheTestProto2()
    override suspend fun readFrom(input: InputStream): CacheTestProto2 = CacheTestProto2(try{input.bufferedReader().readLine() ?: ""}catch(e:Exception){""})
    override suspend fun writeTo(t: CacheTestProto2, output: OutputStream) = output.write(t.name.toByteArray())
}


class NewDataStoreTest {

    private lateinit var mockContext: Context
    private lateinit var newDataStore: NewDataStore // Instance for testing instance methods

    private val emptyPrefsMigrations: List<DataMigration<Preferences>> = emptyList()
    private val emptyProto1Migrations: List<DataMigration<CacheTestProto1>> = emptyList()
    private val emptyProto2Migrations: List<DataMigration<CacheTestProto2>> = emptyList()


    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext // Important for NewDataStore.initializeDefaultInstance

        val baseFileDir = File("test_files_dir")
        if (!baseFileDir.exists()) baseFileDir.mkdirs()

        every { mockContext.preferencesDataStoreFile(any()) } answers { File(baseFileDir, firstArg<String>() + ".preferences_pb") }
        every { mockContext.dataStoreFile(any()) } answers { File(baseFileDir, firstArg<String>()) }

        // Initialize default instance for testing companion methods too
        NewDataStore.initializeDefaultInstance(mockContext) // Uses applicationContext
        newDataStore = NewDataStore(mockContext) // For testing instance methods

        // Clear caches for both default and specific instance
        NewDataStore.clearDefaultInstanceCaches()
        newDataStore.clearCaches()
    }

    // --- Instance method tests ---
    @Test
    fun `preferences returns cached instance for same name, encryption, and migrations (instance)`() {
        val handler1 = newDataStore.preferences("prefsA", false, emptyPrefsMigrations)
        val handler2 = newDataStore.preferences("prefsA", false, emptyPrefsMigrations)
        assertSame("Expected same instance for same params", handler1, handler2)
    }

    @Test
    fun `preferences returns different instances for different names (instance)`() {
        val handler1 = newDataStore.preferences("prefsX", false, emptyPrefsMigrations)
        val handler2 = newDataStore.preferences("prefsY", false, emptyPrefsMigrations)
        assertNotSame(handler1, handler2)
    }

    @Test
    fun `preferences returns different instances for different encryption (instance)`() {
        val handler1 = newDataStore.preferences("prefsSameName", false, emptyPrefsMigrations)
        val handler2 = newDataStore.preferences("prefsSameName", true, emptyPrefsMigrations)
        assertNotSame(handler1, handler2)
    }


    @Test
    fun `proto returns cached instance for same params (instance)`() {
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "protoA.pb", false, emptyProto1Migrations)
        val handler2 = newDataStore.proto(CacheTestProto1Serializer, "protoA.pb", false, emptyProto1Migrations)
        assertSame(handler1, handler2)
    }

    @Test
    fun `proto returns different instances for different filenames (instance)`() {
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "protoX.pb", false, emptyProto1Migrations)
        val handler2 = newDataStore.proto(CacheTestProto1Serializer, "protoY.pb", false, emptyProto1Migrations)
        assertNotSame(handler1, handler2)
    }

    @Test
    fun `proto returns different instances for different serializers (instance)`() {
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "common.pb", false, emptyProto1Migrations)
        val handler2 = newDataStore.proto(CacheTestProto2Serializer, "common.pb", false, emptyProto2Migrations) // Different serializer, different migration list type
        assertNotSame(handler1, handler2)
    }

    @Test
    fun `proto returns different instances for different encryption (instance)`() {
        val handler1 = newDataStore.proto(CacheTestProto1Serializer, "common_enc.pb", false, emptyProto1Migrations)
        val handler2 = newDataStore.proto(CacheTestProto1Serializer, "common_enc.pb", true, emptyProto1Migrations)
        assertNotSame(handler1, handler2)
    }

    @Test
    fun `clearCaches allows new instances to be created (instance)`() {
        val prefsHandler1 = newDataStore.preferences("cache_test_prefs", false, emptyPrefsMigrations)
        val protoHandler1 = newDataStore.proto(CacheTestProto1Serializer, "cache_test_proto.pb", false, emptyProto1Migrations)

        newDataStore.clearCaches()

        val prefsHandler2 = newDataStore.preferences("cache_test_prefs", false, emptyPrefsMigrations)
        val protoHandler2 = newDataStore.proto(CacheTestProto1Serializer, "cache_test_proto.pb", false, emptyProto1Migrations)

        assertNotSame("PreferencesHandler should be a new instance after clearing cache (instance)", prefsHandler1, prefsHandler2)
        assertNotSame("ProtoHandler should be a new instance after clearing cache (instance)", protoHandler1, protoHandler2)
    }

    // --- Companion object (default instance) method tests ---
    @Test
    fun `getDefaultPreferences returns cached instance`() {
        val handler1 = NewDataStore.getDefaultPreferences("prefsDefaultA", false, emptyPrefsMigrations)
        val handler2 = NewDataStore.getDefaultPreferences("prefsDefaultA", false, emptyPrefsMigrations)
        assertSame("Expected same instance for default prefs with same params", handler1, handler2)
    }

    @Test
    fun `getDefaultPreferences returns different for different names`() {
        val handler1 = NewDataStore.getDefaultPreferences("prefsDefaultX", false, emptyPrefsMigrations)
        val handler2 = NewDataStore.getDefaultPreferences("prefsDefaultY", false, emptyPrefsMigrations)
        assertNotSame("Expected different instances for default prefs with different names", handler1, handler2)
    }

    @Test
    fun `getDefaultProtoStore returns cached instance`() {
        val handler1 = NewDataStore.getDefaultProtoStore(CacheTestProto1Serializer, "protoDefaultA.pb", false, emptyProto1Migrations)
        val handler2 = NewDataStore.getDefaultProtoStore(CacheTestProto1Serializer, "protoDefaultA.pb", false, emptyProto1Migrations)
        assertSame("Expected same instance for default proto with same params", handler1, handler2)
    }

    @Test
    fun `getDefaultProtoStore returns different for different filenames`() {
        val handler1 = NewDataStore.getDefaultProtoStore(CacheTestProto1Serializer, "protoDefaultX.pb", false, emptyProto1Migrations)
        val handler2 = NewDataStore.getDefaultProtoStore(CacheTestProto1Serializer, "protoDefaultY.pb", false, emptyProto1Migrations)
        assertNotSame("Expected different instances for default proto with different filenames", handler1, handler2)
    }

    @Test
    fun `clearDefaultInstanceCaches allows new instances for default`() {
        val handler1 = NewDataStore.getDefaultPreferences("cache_default_prefs", false, emptyPrefsMigrations)
        val protoHandler1 = NewDataStore.getDefaultProtoStore(CacheTestProto1Serializer, "cache_default_proto.pb", false, emptyProto1Migrations)

        NewDataStore.clearDefaultInstanceCaches()

        val handler2 = NewDataStore.getDefaultPreferences("cache_default_prefs", false, emptyPrefsMigrations)
        val protoHandler2 = NewDataStore.getDefaultProtoStore(CacheTestProto1Serializer, "cache_default_proto.pb", false, emptyProto1Migrations)

        assertNotSame("Default PreferencesHandler should be new after clearing default cache", handler1, handler2)
        assertNotSame("Default ProtoHandler should be new after clearing default cache", protoHandler1, protoHandler2)
    }
}
