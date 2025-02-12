package com.ollcassist.onboardassistant

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ollcassist.onboardassistant.map.MapScreen
import com.ollcassist.onboardassistant.map.MapViewModel
import com.ollcassist.onboardassistant.ui.theme.OnBoardAssistantTheme
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig

class MainActivity : ComponentActivity() {

    // List of items you want to display when prompted
    private val itemList = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")

    private val text = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == "com.ollca.launch.Ollca") {
            println("App launched by Google Assistant!")
        }

        enableEdgeToEdge()
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        setContent {
            OnBoardAssistantTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar() }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // First Row for API buttons
                        Row(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            APIButton("PMS", "http://10.0.2.2:8081/products")
                            APIButton("CMS", "http://10.0.2.2:8081/carts")
                        }

                        // Add some space between the rows
                        Spacer(modifier = Modifier.weight(1f))

                        // LaunchAssistantButton in the center
                        LaunchAssistantButton()

                        // Text displaying the response (like the list of items)
                        Text(text.value)
                    }
                }
            }
        }
    }

    @Composable
    fun APIButton(title: String, url: String) {
        Button(onClick = {
            runBlocking {
                launch {
                    callAPI(url)
                }
            }
        }) {
            Text(title)
        }
    }

    private suspend fun callAPI(url: String) {
        val client = HttpClient(Android)
        val response: HttpResponse = client.get(url)
        if (response.status.value in 200..299) {
            text.value = response.bodyAsText() // Display the response
        } else {
            text.value = "Error while calling API"
        }
    }

    @Composable
    fun BottomNavigationBar() {
        var selectedItem by remember { mutableStateOf(0) }
        val items = listOf("API tests", "Map")
        val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.LocationOn)
        val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.LocationOn)

        NavigationBar {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                            contentDescription = item
                        )
                    },
                    label = { Text(item) },
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                    }
                )
            }
        }
    }

    // This is the LaunchAssistantButton that will trigger Google Assistant
    @Composable
    fun LaunchAssistantButton() {
        Button(
            onClick = {
                launchGoogleAssistant()
            }
        ) {
            Text("Launch Google Assistant")
        }
    }

    private fun launchGoogleAssistant() {
        // Create an intent to start speech recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Provide a prompt for the user
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'show the list'")
            // Optional: specify the language model and language
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")  // Set language to English
        }

        // Start the activity for speech recognition
        startActivityForResult(intent, 100)  // This starts the assistant and we can handle the result in onActivityResult
    }


    // You can override onActivityResult to handle the response from Google Assistant
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Here, we handle the response from Google Assistant
            val recognizedSpeech = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            recognizedSpeech?.let {
                if (it.contains("show the list")) {
                    // If Google Assistant recognized "show the list", show the list of items
                    text.value = itemList.joinToString("\n") // Join the list as a string and display it
                }
            }
        }
    }

    // You need to add necessary permission for speech recognition in the manifest:
    /*
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    */
}
