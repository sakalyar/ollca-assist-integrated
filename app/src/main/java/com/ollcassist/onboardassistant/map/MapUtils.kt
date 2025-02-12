package com.ollcassist.onboardassistant.map

import android.content.Context
import android.util.Log
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import org.osmdroid.views.overlay.Polygon


fun MapView.addMarker(geoPoint: GeoPoint, context: Context) {
    val marker = Marker(this).apply {
        position = geoPoint
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Point: ${geoPoint.latitude}, ${geoPoint.longitude}"
    }
    overlays.add(marker)
    Log.d("addMarker", "Marker ajouté à lat=${geoPoint.latitude}, lon=${geoPoint.longitude}")
}

fun MapView.addPolygon(geoPoints: List<GeoPoint>, context: Context) {
    val polygon = Polygon().apply {
        points = geoPoints
        setFillColor(0x12121212)
        outlinePaint.color = android.graphics.Color.RED
        outlinePaint.strokeWidth = 5f
    }
    overlays.add(polygon)
}

fun MapView.centerOnPoints(geoPoints: List<GeoPoint>) {
    if (geoPoints.isNotEmpty()) {
        val mapController = this.controller
        val bounds = BoundingBox.fromGeoPointsSafe(geoPoints)
        mapController.setCenter(bounds.center)
        mapController.zoomToSpan(bounds.latitudeSpan, bounds.longitudeSpan)
    }
}



fun MapView.centerAndZoomOnPoints(points: List<GeoPoint>) {
    if (points.isNotEmpty()) {
        val boundingBox = BoundingBox.fromGeoPoints(points)
        controller.setCenter(boundingBox.center)
        controller.zoomToSpan(
            boundingBox.latitudeSpan,
            boundingBox.longitudeSpan
        )
    }
}

