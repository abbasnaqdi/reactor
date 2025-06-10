# Simple DataStore Abstraction Library (com.abbasnaqdi)

A Kotlin-based library providing a simplified and robust abstraction layer for Jetpack DataStore, supporting both Preferences and Proto DataStore with optional encryption.

## Features

-   **Simplified API:** Hides the complexity of setting up and using Jetpack DataStore.
-   **Preferences DataStore:** Easy-to-use API for key-value storage, similar to SharedPreferences but with the benefits of DataStore.
-   **Proto DataStore:** Store typed objects using Protocol Buffers.
-   **Optional Encryption:**
    -   **Preferences:** Securely encrypts/decrypts keys and values using `EncryptedSharedPreferences` (backed by Android Keystore).
    -   **Proto:** Structural support for encryption is present. **IMPORTANT: The current Proto encryption implementation is a NON-FUNCTIONAL PLACEHOLDER and SHOULD NOT be used for sensitive data in production until a robust cryptographic solution (e.g., Google Tink) is integrated.**
-   **Kotlin First:** Leverages Kotlin, Coroutines, and Flow for asynchronous and reactive data handling.
-   **Type Safe:** For Proto DataStore and typed Preferences keys.

## Setup

To use this library in your Android project (assuming it's a module within your project):

1.  Ensure your project's root `build.gradle.kts` (or `build.gradle`) includes `google()` and `mavenCentral()` in its repositories block.
2.  Add the library module as a dependency in your app module's `build.gradle.kts` (or `build.gradle`):

    ```kotlin
    dependencies {
        implementation(project(":library")) // Or the specific name of this library module
        // ... other dependencies
    }
    ```

## Initialization

First, get an instance of `NewDataStore` (ideally as a singleton, managed by your DI framework or Application class):

```kotlin
import com.abbasnaqdi.core.NewDataStore

// In your Application class, ViewModel, or DI setup
val dataStoreManager = NewDataStore(applicationContext)
```

## Default Instance (Singleton Access)

For applications that primarily use a single set of DataStore configurations, you can initialize a default instance of `NewDataStore` for easier access throughout your app.

**1. Initialize in your Application class:**

```kotlin
// In your Application's onCreate()
import com.abbasnaqdi.core.NewDataStore
import android.app.Application // Import Application

class MyApplication : Application() { // Extend Application
    override fun onCreate() {
        super.onCreate()
        NewDataStore.initializeDefaultInstance(this)
    }
}
```

**2. Access default stores anywhere:**

```kotlin
// Get default unencrypted preferences
val defaultPrefsHandler = NewDataStore.getDefaultPreferences(name = "my_global_settings")

// Get default unencrypted proto store
// val defaultUserPrefsHandler = NewDataStore.getDefaultProtoStore(
//     serializer = UserPreferencesSerializer, // Your serializer
//     fileName = "global_user_prefs.pb"
// )
```
The `name`, `encrypted`, and `migrations` parameters are also available on these default accessor methods.

## Usage Examples

### Preferences DataStore

Provides a simple key-value storage mechanism.

**1. Get a Preferences Handler:**

```kotlin
// For unencrypted Preferences (recommended for non-sensitive data)
val prefsHandler = dataStoreManager.preferences(name = "my_app_settings", encrypted = false)

// For encrypted Preferences (recommended for sensitive data)
val securePrefsHandler = dataStoreManager.preferences(name = "my_secure_settings", encrypted = true)
```

**Providing Migrations (e.g., from SharedPreferences):**

You can provide a list of `DataMigration<Preferences>` to handle migrations, such as migrating from an old SharedPreferences file.

```kotlin
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataMigration
import android.content.Context // For context in migration

// Assuming applicationContext is available
val sharedPrefsToDataStoreMigration = SharedPreferencesMigration(
    context = applicationContext, // Typically from your Application or DI
    sharedPreferencesName = "my_old_shared_prefs"
    // keysToMigrate = setOf("old_key1", "old_key2") // Optional: specify keys
)

val prefsHandlerWithMigration = dataStoreManager.preferences(
    name = "my_app_settings",
    encrypted = false,
    migrations = listOf(sharedPrefsToDataStoreMigration)
)
```
**Note:** Migrations are primarily for the standard (unencrypted) Preferences DataStore. They do not directly apply when `encrypted = true` (which uses `EncryptedSharedPreferences`), as `EncryptedSharedPreferences` has its own storage format not managed by DataStore's migration system. Manual data transfer would be needed if migrating to/from encrypted preferences with a different scheme.

**2. Saving Data (`put`):**

`put` is a suspend function. The type of the value is used by the library to store it appropriately.

```kotlin
import kotlinx.coroutines.launch
// import kotlinx.coroutines.runBlocking // Or use a CoroutineScope

// Inside a coroutine scope
scope.launch {
    prefsHandler.put("user_name", "Jane Doe") // String
    prefsHandler.put("login_count", 5)      // Int
    securePrefsHandler.put("is_subscribed", true) // Boolean
    prefsHandler.put("feature_flags", setOf("alpha", "beta")) // Set<String>
}
```

**3. Reading Data (`get` Flow):**

`get<T>` returns a `Flow<T?>` that emits when the data changes. You must specify the expected type using reified generics.

```kotlin
import kotlinx.coroutines.flow.map // Not strictly needed for direct get, but often used with flows
import androidx.lifecycle.compose.collectAsStateWithLifecycle // For Compose UI

// In your ViewModel or Composable:
val userNameFlow: Flow<String?> = prefsHandler.get<String>("user_name", "Default User")
val loginCountFlow: Flow<Int?> = prefsHandler.get<Int>("login_count")
val isSubscribedFlow: Flow<Boolean?> = securePrefsHandler.get<Boolean>("is_subscribed", false)
val featureFlagsFlow: Flow<Set<String>?> = prefsHandler.get<Set<String>>("feature_flags", emptySet())


// Example in a Composable
val userName by userNameFlow.collectAsStateWithLifecycle()
Text("User: ${userName ?: "Not set"}")
```

**4. Reading Data Once (`readOnce`):**

`readOnce<T>` is a suspend function that returns `Result<T?>`. You must specify the expected type using reified generics.

```kotlin
scope.launch {
    val nameResult = prefsHandler.readOnce<String>("user_name", "Guest")
    nameResult.onSuccess { name ->
        println("Current user: ${name ?: "Not available"}")
    }.onFailure { exception ->
        println("Failed to read user name: $exception")
    }

    val flagsResult = prefsHandler.readOnce<Set<String>>("feature_flags")
    flagsResult.onSuccess { flags ->
        println("Feature flags: ${flags ?: "Not set"}")
    }
}
```

**5. Removing Data (`remove`):**

`remove<T>` is a suspend function. You need to specify the expected type of the key using reified generics to ensure the correct underlying `Preferences.Key` is targeted for removal.

```kotlin
scope.launch {
    prefsHandler.remove<String>("user_name")
    prefsHandler.remove<Int>("login_count")
    prefsHandler.remove<Set<String>>("feature_flags")
}
```

**6. Clearing All Preferences in a Handler (`clear`):**

`clear` is a suspend function.

```kotlin
scope.launch {
    prefsHandler.clear() // Clears all preferences managed by this specific handler instance ("my_app_settings")
}
```

### Proto DataStore

Allows you to store typed objects using Protocol Buffers.

**1. Define your Proto schema:**

Create a `.proto` file in your `app/src/main/proto` directory (e.g., `user_prefs.proto`):

```protobuf
syntax = "proto3";

option java_package = "com.abbasnaqdi.sample.datastore"; // Adjust to your sample app's package
option java_multiple_files = true;

message UserPreferences {
  string user_id = 1;
  bool notifications_enabled = 2;
  Theme theme = 3;
}

enum Theme {
  THEME_UNSPECIFIED = 0;
  LIGHT = 1;
  DARK = 2;
  SYSTEM = 3;
}
```
(Ensure you have the protobuf-gradle-plugin configured in your app module to generate Kotlin classes from this schema).

**2. Create a Serializer for your Proto type:**

```kotlin
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import com.abbasnaqdi.sample.datastore.UserPreferences // Import your generated class

object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return UserPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) = t.writeTo(output)
}
```

**3. Get a Proto Handler:**

```kotlin
// For unencrypted Proto DataStore
val userPrefsHandler = dataStoreManager.proto(
    serializer = UserPreferencesSerializer,
    fileName = "user_prefs.pb",
    encrypted = false
)

// For encrypted Proto DataStore
val secureUserPrefsHandler = dataStoreManager.proto(
    serializer = UserPreferencesSerializer,
    fileName = "secure_user_prefs.pb",
    encrypted = true // REMINDER: Current Proto encryption is a non-functional placeholder!
)
```

**Providing Migrations (e.g., for schema changes):**

You can provide a list of `DataMigration<YourProtoType>` to handle schema updates or other data transformations.

```kotlin
import androidx.datastore.core.DataMigration
import com.abbasnaqdi.sample.datastore.UserPreferences // Assuming this is your proto class

// Example: A conceptual migration for UserPreferences
// val userPrefsMigration1To2 = object : DataMigration<UserPreferences> {
//     override suspend fun shouldMigrate(currentData: UserPreferences): Boolean {
//         // Logic to check if this version of data needs migration
//         // return currentData.version < 2 // Assuming a version field in your proto
//         return false // Placeholder
//     }
//     override suspend fun migrate(currentData: UserPreferences): UserPreferences {
//         // Logic to transform currentData to the new schema
//         // return currentData.toBuilder().setNewField("defaultValue").setVersion(2).build()
//         return currentData // Placeholder
//     }
//     override suspend fun cleanUp() { /* Optional: Clean up old data if needed */ }
// }

val userPrefsHandlerWithMigration = dataStoreManager.proto(
    serializer = UserPreferencesSerializer,
    fileName = "user_prefs.pb",
    encrypted = false, // Or true, migrations apply before encryption wrapper if any
    // migrations = listOf(userPrefsMigration1To2) // Pass your migration list
    migrations = listOf() // Example with an empty list
)
```

**4. Reading Data (`data` Flow):**

The `data` property returns a `Flow<T>`.

```kotlin
val userPreferencesFlow: Flow<UserPreferences> = userPrefsHandler.data

// Example in a Composable
val userPrefs by userPreferencesFlow.collectAsStateWithLifecycle(initialValue = UserPreferencesSerializer.defaultValue)
if (userPrefs.notificationsEnabled) {
    Text("Notifications are ON")
}
```

**5. Updating Data (`updateData`):**

`updateData` is a suspend function that transactionally updates the stored object. It returns `Result<T>`.

```kotlin
scope.launch {
    val updateResult = userPrefsHandler.updateData { currentPrefs ->
        currentPrefs.toBuilder()
            .setNotificationsEnabled(true)
            .setTheme(UserPreferences.Theme.DARK)
            .build()
    }
    updateResult.onSuccess { updatedPrefs ->
        println("Successfully updated prefs: $updatedPrefs")
    }.onFailure { exception ->
        println("Failed to update prefs: $exception")
    }
}
```

**6. Reading Data Once (`readData`):**

`readData` is a suspend function that returns the current state as `Result<T>`.

```kotlin
scope.launch {
    val currentPrefsResult = userPrefsHandler.readData()
    currentPrefsResult.onSuccess { prefs ->
        println("Current user preferences: $prefs")
    }.onFailure { exception ->
        println("Failed to read user preferences: $exception")
    }
}
```

## Error Handling

-   **Suspend functions** (like `put`, `remove`, `clear` for Preferences; `updateData`, `readData` for Proto; `readOnce` for Preferences) return `kotlin.Result<T>` where applicable for operations that might fail directly (like I/O or serialization). Check `result.isSuccess` or use `result.onSuccess{...}.onFailure{...}`.
-   **Flows** (`get` for Preferences, `data` for Proto) emit values asynchronously. Errors encountered during data collection (like `IOException` or `CorruptionException`) should be handled using the `.catch{}` operator on the Flow in the collector's scope. The library wraps some specific exceptions (e.g., `DataStoreReadException`, `DataStoreWriteException` for Proto) for more context.

## Concurrency

-   All DataStore operations are main-safe (I/O is performed on `Dispatchers.IO` internally by Jetpack DataStore or by this library for `EncryptedSharedPreferences`).
-   The library uses `ConcurrentHashMap` for caching DataStore instances, making the retrieval of handlers thread-safe.
-   Jetpack DataStore guarantees read-after-write consistency within a single process.

## Important Note on Proto DataStore Encryption

The `encrypted = true` option for `ProtoHandler` currently uses a **NON-FUNCTIONAL PLACEHOLDER** for its encryption mechanism (`EncryptedProtoSerializer`). While the structure is in place, it **DOES NOT ACTUALLY ENCRYPT THE DATA** and should not be relied upon for sensitive information in its current state. A robust cryptographic solution (e.g., integrating Google Tink) is required to make this feature production-ready.

---
*This library is currently under development.*
