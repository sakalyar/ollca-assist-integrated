package com.ollcassist.onboardassistant

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // List of items you want to display when prompted as a test
    private val itemList = mutableStateListOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
    private var showList = mutableStateOf(false)


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
                        // API Buttons
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

                        Spacer(modifier = Modifier.weight(1f))

                        LaunchAssistantButton()

                        // Display the list here
                        ItemListDisplay()

                        // Text displaying API responses
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
            text.value = response.bodyAsText()
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

            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'show the list'")
            // Specifying the language model and language
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")  // Set language to English
        }

        // Start the activity for speech recognition
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val recognizedSpeech = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            recognizedSpeech?.let { results ->
                for (result in results) {
                    when {
                        result.equals("show the list", ignoreCase = true) -> {
                            showList.value = true
                        }
                        result.startsWith("add ", ignoreCase = true) && result.contains(" to the list", ignoreCase = true) -> {
                            val itemToAdd = result
                                .removePrefix("add ")
                                .removeSuffix(" to the list")
                                .trim()

                            if (itemToAdd.isNotEmpty()) {
                                itemList.add(itemToAdd)
                                showList.value = true
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ItemListDisplay() {
        if (showList.value) {
            Column {
                itemList.forEach { item ->
                    Text(text = item)
                }
            }
        }
    }


}
