package com.ollcassist.onboardassistant.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

@Composable
fun MapScreen(viewModel: MapViewModel) {
    var refreshState by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val marketplacesState = viewModel.marketplaces.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(
            onClick = {
                refreshState = !refreshState
                viewModel.fetchMarketplaces()
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.5f)
        ) {
            Text("Rafraîchir la carte")
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                    controller.setCenter(GeoPoint(48.8583, 2.2944)) // Centre par défaut
                    controller.setZoom(9.5)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                val allGeoPoints = mutableListOf<GeoPoint>()
                marketplacesState.value.forEach { marketplace ->
                    marketplace.areas.forEach { area ->
                        val geoPoints = viewModel.extractGeoPoints(area.area)
                        if (geoPoints.isNotEmpty()) {
                            allGeoPoints.addAll(geoPoints)
                            geoPoints.forEach { geoPoint ->
                                mapView.addMarker(geoPoint, context)
                            }
                            mapView.addPolygon(geoPoints, context)
                        }
                    }
                }
                mapView.invalidate()
                if (allGeoPoints.isNotEmpty()) {
                    mapView.centerAndZoomOnPoints(allGeoPoints)
                }
            }
        )
    }
}
