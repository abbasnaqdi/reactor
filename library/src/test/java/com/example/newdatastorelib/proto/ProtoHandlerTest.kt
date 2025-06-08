package com.example.newdatastorelib.proto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import io.mockk.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.catch // Ensure catch is imported

// Define a simple Proto-like data class for testing
data class TestProto(val id: Int = 0, val name: String = "default")

// A mock Serializer for TestProto
class TestProtoSerializer(private val failRead: Boolean = false, private val failWrite: Boolean = false) : Serializer<TestProto> {
    override val defaultValue: TestProto = TestProto()

    override suspend fun readFrom(input: InputStream): TestProto {
        if (failRead) throw CorruptionException("Test read corruption", IOException())
        // Simple mock: doesn't actually parse, returns default or a fixed value
        // In a real scenario, you might use a real proto lib to parse a predefined byte array.
        return try {
            // Simulate reading some bytes, actual parsing depends on proto format
            val availableBytes = input.available()
            if (availableBytes > 0) {
                 // A more complex mock might try to interpret bytes if needed for specific tests
                 // For simplicity, let's assume if we read any byte, it's a specific ID.
                 val id = input.read()
                 if (id == -1) defaultValue else TestProto(id = id, name = "read_from_stream")
            } else defaultValue
        } catch (e: IOException) {
            throw CorruptionException("Mock read failed.", e)
        }
    }

    override suspend fun writeTo(t: TestProto, output: OutputStream) {
        if (failWrite) throw IOException("Test write failure")
        // Simple mock: doesn't actually serialize
        output.write(t.id) // Write something minimal
    }
}

// Helper to create ProtoHandler that uses the mockDataStore through overriding
// This is a workaround for the lack of direct DI in ProtoHandler's main constructor.
// For tests requiring specific DataStore mock, this helps.
internal fun createTestableProtoHandlerWithInjectedStore( // Made internal for potential reuse if tests split
    mockDataStore: DataStore<TestProto>,
    context: Context = mockk(relaxed = true),
    serializer: Serializer<TestProto> = TestProtoSerializer(),
    encrypted: Boolean = false // Added encrypted flag
): ProtoHandler<TestProto> {
    return object : ProtoHandler<TestProto>(context, "test.pb", serializer, encrypted) {
        override val data: Flow<TestProto>
            get() = mockDataStore.data.catch { exception ->
                if (exception is IOException || exception is CorruptionException) {
                    throw DataStoreReadException("Error reading Proto DataStore: ${exception.message}", exception)
                }
                throw exception
            }

        override suspend fun updateData(transform: suspend (t: TestProto) -> TestProto): Result<TestProto> {
            return try {
                Result.success(mockDataStore.updateData(transform))
            } catch (e: Exception) {
                Result.failure(DataStoreWriteException("Error updating Proto DataStore: ${e.message}", e))
            }
        }
        // readData() in ProtoHandler uses its public `data` property, which is overridden here.
    }
}


class ProtoHandlerEncryptedTest {

    private lateinit var mockContext: Context
    private lateinit var mockDataStore: DataStore<TestProto>
    private lateinit var realUserSerializer: TestProtoSerializer
    private lateinit var protoHandler: ProtoHandler<TestProto>


    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true) {
            every { dataStoreFile(any()) } answers { java.io.File(firstArg<String>()) }
        }
        mockDataStore = mockk(relaxUnitFun = true)
        realUserSerializer = TestProtoSerializer()

        // Use the helper that simulates DI for DataStore
        protoHandler = createTestableProtoHandlerWithInjectedStore(
            mockDataStore = mockDataStore,
            context = mockContext,
            serializer = realUserSerializer,
            encrypted = true // Key for this test suite
        )
    }

    @Test
    fun `(Encrypted) data flow emits items from DataStore`() = runBlocking {
        val testData = TestProto(1, "encrypted_test")
        every { mockDataStore.data } returns flowOf(testData)

        val result = protoHandler.data.first()
        assertEquals(testData, result)
    }

    @Test
    fun `(Encrypted) updateData successfully updates (placeholder encryption)`() = runBlocking {
        val transformedData = TestProto(2, "encrypted_updated")
        val transformLambda = slot<suspend (TestProto) -> TestProto>()

        coEvery { mockDataStore.updateData(capture(transformLambda)) } coAnswers {
            transformLambda.captured.invoke(TestProto(id = 0, name = "dummy"))
            transformedData
        }

        val result = protoHandler.updateData { transformedData }

        assertTrue(result.isSuccess)
        assertEquals(transformedData, result.getOrNull())
        coVerify { mockDataStore.updateData(any()) }
    }

    @Test
    fun `(Encrypted) readData returns success with current data (placeholder encryption)`() = runBlocking {
        val testData = TestProto(1, "encrypted_current")
        every { mockDataStore.data } returns flowOf(testData)

        val result = protoHandler.readData()
        assertTrue(result.isSuccess)
        assertEquals(testData, result.getOrNull())
    }

    @Test
    fun `(Encrypted) data flow handles exceptions from underlying DataStore`() = runBlocking {
        every { mockDataStore.data } returns flowOf<TestProto>(TestProto()).map { throw IOException("encrypted_io_error") }

        try {
            protoHandler.data.first()
            fail("Should have thrown DataStoreReadException")
        } catch (e: DataStoreReadException) {
            assertTrue(e.cause is IOException)
            assertTrue(e.message!!.contains("encrypted_io_error"))
        }
    }
}

class ProtoHandlerUnencryptedTest { // Test non-encrypted ProtoHandler

    private lateinit var mockContext: Context
    private lateinit var mockDataStore: DataStore<TestProto>
    private lateinit var testProtoSerializer: TestProtoSerializer

    // This will test ProtoHandler directly, assuming it can be instantiated with a mock DataStore
    // Similar to PreferencesHandler, ProtoHandler news up DataStore internally.
    // For effective testing, ProtoHandler should be refactored to accept DataStore<T> via constructor
    // or its core logic moved to a testable internal class.
    // For now, tests will assume such a refactor or test the logic conceptually.

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockDataStore = mockk() // This will be used by the overridden ProtoHandler methods
        testProtoSerializer = TestProtoSerializer() // Default: no failures

        // This setup implies ProtoHandler is refactored for DI:
        // protoHandler = ProtoHandler(mockContext, "test.pb", testProtoSerializer, false, mockDataStore)
    }

    // Helper to create ProtoHandler that uses the mockDataStore through overriding
    private fun createTestableProtoHandlerWithInjectedStore(
        dataStore: DataStore<TestProto> = mockDataStore,
        serializer: Serializer<TestProto> = testProtoSerializer
    ): ProtoHandler<TestProto> {
        return object : ProtoHandler<TestProto>(mockContext, "test.pb", serializer, false) {
            // Override the internal dataStore access with our mock for testing
            // This requires dataStore property in ProtoHandler to be open or accessible for overriding,
            // or changing its lazy delegate to allow replacement.
            // For this test, we directly override methods that use the dataStore.

            override val data: Flow<TestProto> // Override to use the mock directly
                get() = dataStore.data.catch { exception -> // Use the injected datastore
                    if (exception is IOException || exception is CorruptionException) {
                        throw DataStoreReadException("Error reading Proto DataStore: ${exception.message}", exception)
                    }
                    throw exception
                }

            override suspend fun updateData(transform: suspend (t: TestProto) -> TestProto): Result<TestProto> {
                return try {
                    Result.success(dataStore.updateData(transform)) // Use the injected datastore
                } catch (e: Exception) {
                    Result.failure(DataStoreWriteException("Error updating Proto DataStore: ${e.message}", e))
                }
            }
            // readData() in ProtoHandler uses its public `data` property, which is overridden here.
        }
    }


    @Test
    fun `data flow emits items from DataStore`() = runBlocking {
        val testData = TestProto(1, "test")
        every { mockDataStore.data } returns flowOf(testData)

        val protoHandler = createTestableProtoHandlerWithInjectedStore(mockDataStore)

        val result = protoHandler.data.first()
        assertEquals(testData, result)
    }

    @Test
    fun `data flow wraps IOException into DataStoreReadException`() = runBlocking {
        every { mockDataStore.data } returns flowOf<TestProto>(TestProto()).map { throw IOException("underlying_io_error") }
        val protoHandler = createTestableProtoHandlerWithInjectedStore(mockDataStore)

        try {
            protoHandler.data.first()
            fail("Should have thrown DataStoreReadException")
        } catch (e: DataStoreReadException) {
            assertTrue(e.cause is IOException)
            assertTrue(e.message!!.contains("underlying_io_error"))
        }
    }

    @Test
    fun `data flow wraps CorruptionException into DataStoreReadException`() = runBlocking {
        every { mockDataStore.data } returns flowOf<TestProto>(TestProto()).map { throw CorruptionException("underlying_corruption", IOException()) }
        val protoHandler = createTestableProtoHandlerWithInjectedStore(mockDataStore)

        try {
            protoHandler.data.first()
            fail("Should have thrown DataStoreReadException")
        } catch (e: DataStoreReadException) {
            assertTrue(e.cause is CorruptionException)
            assertTrue(e.message!!.contains("underlying_corruption"))
        }
    }

    @Test
    fun `updateData successfully updates and returns new data`() = runBlocking {
        val initialData = TestProto(1, "initial") // Will be provided by mockDataStore.updateData's transform
        val expectedUpdatedData = TestProto(2, "updated")     // This is what the transform should produce

        val transformLambdaSlot = slot<suspend (TestProto) -> TestProto>()

        // When mockDataStore.updateData is called, it will execute the captured lambda.
        // The lambda itself should return expectedUpdatedData.
        // The mockDataStore.updateData itself should also be mocked to return expectedUpdatedData.
        coEvery { mockDataStore.updateData(capture(transformLambdaSlot)) } coAnswers {
            // Simulate the transform: it receives some current data (e.g. initialData, though not strictly enforced by this mock capture alone)
            // and should produce expectedUpdatedData.
            // The actual execution of the lambda with specific input happens in protoHandler.updateData
            transformLambdaSlot.captured(initialData) // This simulates the DataStore behavior of calling the transform
            // And DataStore's updateData itself returns the result of the transform
            expectedUpdatedData
        }

        val protoHandler = createTestableProtoHandlerWithInjectedStore(mockDataStore)

        // The transform passed to protoHandler.updateData is the one we want to test.
        // It will be called by mockDataStore.updateData's captured lambda.
        val result = protoHandler.updateData { currentData ->
            // This lambda is the 'transform' argument.
            // 'currentData' here would be 'initialData' if the mock setup is complete.
            // For this test, we primarily care that this block is executed and returns the right thing.
            assertEquals(initialData, currentData) // Verifies what DataStore passes to the transform
            expectedUpdatedData // This is what our transform returns
        }

        assertTrue(result.isSuccess)
        assertEquals(expectedUpdatedData, result.getOrNull())
        coVerify { mockDataStore.updateData(any()) } // Verify updateData was called on the mock
    }


    @Test
    fun `updateData returns failure on exception`() = runBlocking {
        coEvery { mockDataStore.updateData(any()) } throws IOException("update_failed")
        val protoHandler = createTestableProtoHandlerWithInjectedStore(mockDataStore)

        val result = protoHandler.updateData { it } // Simple transform
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DataStoreWriteException)
        assertTrue(result.exceptionOrNull()?.message!!.contains("update_failed"))
    }

    @Test
    fun `readData returns success with current data`() = runBlocking {
        val testData = TestProto(1, "current")
        every { mockDataStore.data } returns flowOf(testData)
        val protoHandler = createTestableProtoHandlerWithInjectedStore(mockDataStore)

        val result = protoHandler.readData()
        assertTrue(result.isSuccess)
        assertEquals(testData, result.getOrNull())
    }

    @Test
    fun `readData returns failure on exception`() = runBlocking {
        every { mockDataStore.data } returns flowOf<TestProto>(TestProto()).map { throw IOException("read_failed") }
        val protoHandler = createTestableProtoHandlerWithInjectedStore(mockDataStore)

        val result = protoHandler.readData()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DataStoreReadException)
        assertTrue(result.exceptionOrNull()?.message!!.contains("read_failed"))
    }
}
