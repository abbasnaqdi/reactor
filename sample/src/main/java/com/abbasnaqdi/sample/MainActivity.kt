package com.abbasnaqdi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalContext // Not explicitly used in this version
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abbasnaqdi.core.NewDataStore // Correct import
import com.abbasnaqdi.preferences.PreferencesHandler // Correct import
import kotlinx.coroutines.launch

// No longer need to define MY_TEXT_PREF as Preferences.Key globally
const val MY_TEXT_PREF_NAME = "my_text_preference" // Use a const string for the key name

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dataStore = remember { NewDataStore(applicationContext) }
            // Get PreferencesHandler once
            val preferencesHandler = remember { dataStore.preferences(name = "sample_prefs", encrypted = false) }
            SampleAppUI(preferencesHandler) // Pass the handler
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleAppUI(preferencesHandler: PreferencesHandler) { // Accept PreferencesHandler
    // val context = LocalContext.current // Not strictly needed for this UI logic
    val scope = rememberCoroutineScope()

    var textToSave by remember { mutableStateOf("") }

    // Observe the preference value from DataStore using the new API
    val savedTextFlow = remember { preferencesHandler.get<String>(MY_TEXT_PREF_NAME, "No value saved yet") }
    val savedText by savedTextFlow.collectAsStateWithLifecycle(initialValue = "Loading...")


    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("DataStore Library Sample (Simplified API)") })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Enter text to save in DataStore:")

                OutlinedTextField(
                    value = textToSave,
                    onValueChange = { textToSave = it },
                    label = { Text("Your Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        scope.launch {
                            // Use new put API
                            preferencesHandler.put(MY_TEXT_PREF_NAME, textToSave)
                            textToSave = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save to Preferences")
                }

                // Example for removing the preference
                Button(
                    onClick = {
                        scope.launch {
                            preferencesHandler.remove<String>(MY_TEXT_PREF_NAME)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove Preference")
                }


                Spacer(modifier = Modifier.height(20.dp))

                Text("Value currently in DataStore (Preferences):")
                Text(
                    text = savedText ?: "N/A",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
