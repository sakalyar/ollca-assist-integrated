package com.ollcassist.onboardassistant

import android.content.Intent
import android.os.Bundle
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

    //ATTRIBUTES

    private val selectedPage = mutableIntStateOf(0)
    private val text = mutableStateOf("")

    // FUNCTIONS:
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
                    }
                }
            }
        }
    }

    private suspend fun callAPI(url: String) {
        val client = HttpClient(Android)
        val response: HttpResponse = client.get(url)
        println(response.bodyAsText())
        if (response.status.value in 200..299) {
            text.value = response.bodyAsText()
        } else {
            text.value = "error while calling API"
        }
    }

    //COMPONENTS
    @Composable
    fun LaunchAssistantButton() {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    launchGoogleAssistant()
                }
            ) {
                Text("Launch Google Assistant")
            }
        }
    }

    private fun launchGoogleAssistant() {
        // Launch Google Assistant when the button is clicked
        val intent = Intent(Intent.ACTION_VOICE_COMMAND)
        startActivity(intent)
    }


    //NAVIGATION BAR

    @Composable
    fun BottomNavigationBar() {
        var selectedItem by remember { mutableIntStateOf(0) }
        val items = listOf("API tests", "Map")
        val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.LocationOn)
        val unsSelectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.LocationOn)
        NavigationBar {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedItem == index) selectedIcons[index] else unsSelectedIcons[index],
                            contentDescription = item
                        )
                    },
                    label = { Text(item) },
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        selectedPage.intValue = index
                    }
                )
            }
        }
    }

    //PAGES

    @Composable
    fun Page(innerPadding: PaddingValues, page: Int) {
        if (page == 0) APIPage(innerPadding)
        else if (page == 1) MapPage()
    }

    @Composable
    fun APIPage(innerPadding: PaddingValues) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Row (modifier = Modifier.padding(innerPadding).fillMaxWidth(1f).fillMaxHeight(0.1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                APIButton("PMS", "http://10.0.2.2:8081/products")
                APIButton("CMS", "http://10.0.2.2:8081/carts")
            }
            ResponseText()
        }
    }

    @Composable
    fun MapPage() {
        val mapViewModel = viewModel<MapViewModel>()
        MapScreen(mapViewModel)
    }

    //API CALLING

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

    @Composable
    fun ResponseText() {
        val responseText by text
        Text(responseText)
    }
}