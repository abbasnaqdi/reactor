package com.abbasnaqdi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abbasnaqdi.core.NewDataStore // Import from our library
import kotlinx.coroutines.launch

// Define a Preferences Key for our example
val MY_TEXT_PREF = androidx.datastore.preferences.core.stringPreferencesKey("my_text_preference")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Get an instance of our DataStore library
            // In a real app, this might be injected via Hilt/Koin
            val dataStore = remember { NewDataStore(applicationContext) }
            SampleAppUI(dataStore)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleAppUI(dataStore: NewDataStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for the TextField
    var textToSave by remember { mutableStateOf("") }

    // Observe the preference value from DataStore
    // Using the non-encrypted preferences store named "sample_prefs"
    val preferencesHandler = remember { dataStore.preferences(name = "sample_prefs", encrypted = false) }

    val savedTextFlow = remember { preferencesHandler.get(MY_TEXT_PREF, "No value saved yet") }
    val savedText by savedTextFlow.collectAsStateWithLifecycle(initialValue = "Loading...")


    MaterialTheme { // Using MaterialTheme (default M3 if correctly set up)
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("DataStore Library Sample") })
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
                            preferencesHandler.put(MY_TEXT_PREF, textToSave)
                            // Optionally clear field or give feedback
                            textToSave = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save to Preferences")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Value currently in DataStore (Preferences):")
                Text(
                    text = savedText ?: "N/A", // Handle null case from flow if not defaulted
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
