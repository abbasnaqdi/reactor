package com.abbasnaqdi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abbasnaqdi.core.NewDataStore
import com.abbasnaqdi.preferences.PreferencesHandler
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable // Import for @Serializable

// Define a sample @Serializable data class
@Serializable
data class AppSettings(
    val theme: String = "system",
    val notificationsEnabled: Boolean = true,
    val itemsPerPage: Int = 10
)

const val MY_TEXT_PREF_NAME = "my_text_preference"
const val MY_SETTINGS_PREF_NAME = "my_app_settings_object" // Key for the custom object

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dataStore = remember { NewDataStore(applicationContext) }
            val preferencesHandler = remember { dataStore.preferences(name = "sample_app_prefs", encrypted = false) }
            SampleAppUI(preferencesHandler)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleAppUI(preferencesHandler: PreferencesHandler) {
    val scope = rememberCoroutineScope()

    // --- Simple Text Preference State ---
    var textToSave by remember { mutableStateOf("") }
    val savedTextFlow = remember { preferencesHandler.get<String>(MY_TEXT_PREF_NAME, "No text saved yet") }
    val savedText by savedTextFlow.collectAsStateWithLifecycle(initialValue = "Loading text...")

    // --- Custom Object (AppSettings) Preference State ---
    val defaultSettings = AppSettings()
    var currentThemeInput by remember { mutableStateOf(defaultSettings.theme) }
    var currentNotificationsInput by remember { mutableStateOf(defaultSettings.notificationsEnabled) }

    val savedSettingsFlow = remember { preferencesHandler.get<AppSettings>(MY_SETTINGS_PREF_NAME, defaultSettings) }
    // Use a different initial value for collectAsState to see loading vs default
    val savedSettings by savedSettingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings(theme="loading", itemsPerPage = 0, notificationsEnabled = false))


    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("DataStore Library Demo") })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Added for scrollability
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- Simple Text Preference Section ---
                Text("Simple Text Preference", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = textToSave,
                    onValueChange = { textToSave = it },
                    label = { Text("Your Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { scope.launch { preferencesHandler.put(MY_TEXT_PREF_NAME, textToSave); textToSave = "" } }) {
                        Text("Save Text")
                    }
                    Button(onClick = { scope.launch { preferencesHandler.remove<String>(MY_TEXT_PREF_NAME) } }) {
                        Text("Remove Text")
                    }
                }
                Text("Saved Text: ${savedText ?: "N/A"}", style = MaterialTheme.typography.bodyLarge)

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // --- Custom Object (AppSettings) Preference Section ---
                Text("Custom Object (AppSettings) Preference", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = currentThemeInput,
                    onValueChange = { currentThemeInput = it },
                    label = { Text("Theme (e.g., dark, light, system)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = currentNotificationsInput,
                        onCheckedChange = { currentNotificationsInput = it }
                    )
                    Text("Enable Notifications")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val newSettings = AppSettings(
                                theme = currentThemeInput,
                                notificationsEnabled = currentNotificationsInput,
                                itemsPerPage = savedSettings?.itemsPerPage ?: defaultSettings.itemsPerPage // Retain other settings
                            )
                            preferencesHandler.put(MY_SETTINGS_PREF_NAME, newSettings)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save AppSettings Object")
                }
                 Button(
                    onClick = {
                        scope.launch {
                            preferencesHandler.remove<AppSettings>(MY_SETTINGS_PREF_NAME)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove AppSettings Object")
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Saved AppSettings:", style = MaterialTheme.typography.titleSmall)
                Text("Theme: ${savedSettings?.theme ?: "N/A"}", style = MaterialTheme.typography.bodyLarge)
                Text("Notifications: ${if (savedSettings?.notificationsEnabled == true) "Enabled" else "Disabled"}", style = MaterialTheme.typography.bodyLarge)
                Text("Items Per Page: ${savedSettings?.itemsPerPage ?: "N/A"}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
