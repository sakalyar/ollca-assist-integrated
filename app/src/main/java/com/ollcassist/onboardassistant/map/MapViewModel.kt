package com.ollcassist.onboardassistant.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel : ViewModel() {
    private val _marketplaces = MutableStateFlow<List<Marketplace>>(emptyList())
    val marketplaces = _marketplaces.asStateFlow()

    init {
        Log.d("MapViewModel", "Initializing MapViewModel and fetching marketplaces")
        fetchMarketplaces()
    }

    fun fetchMarketplaces() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllMarketplaces(true, 1.8544713, 50.952844, true, 1, 10, "name")
                Log.d("MapViewModel", "Raw Response: ${response.raw()}")  // Ajout du log pour la réponse brute
                if (response.isSuccessful) {
                    val data = response.body()?.content ?: emptyList()
                    Log.d("MapViewModel", "Data fetched successfully: $data")
                    _marketplaces.value = data
                    Log.d("MapViewModel", "Marketplaces updated in StateFlow")
                } else {
                    Log.e("MapViewModel", "API Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching data", e)
            }
        }
    }


    fun extractGeoPoints(polygon: GeoJsonPolygon): List<GeoPoint> {
        val geoPoints = mutableListOf<GeoPoint>()
        polygon.coordinates.forEach { multiPolygon ->
            multiPolygon.forEach { polygon ->
                polygon.forEach { point ->
                    if (point.size >= 2) {
                        val longitude = point[0]
                        val latitude = point[1]
                        if (latitude in -90.0..90.0 && longitude in -180.0..180.0) {
                            geoPoints.add(GeoPoint(latitude, longitude))
                            Log.d("MapViewModel", "Valid GeoPoint: lat=$latitude, lon=$longitude")
                        } else {
                            Log.e("MapViewModel", "Invalid GeoPoint: lat=$latitude, lon=$longitude")
                        }
                    }
                }
            }
        }
        return geoPoints
    }



}
