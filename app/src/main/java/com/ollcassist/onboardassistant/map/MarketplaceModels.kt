package com.ollcassist.onboardassistant.map

import com.google.gson.annotations.SerializedName


data class MarketplaceResponse(
    @SerializedName("content") val content: List<Marketplace>,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("last") val last: Boolean,
    @SerializedName("numberOfElements") val numberOfElements: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("first") val first: Boolean,
    @SerializedName("empty") val empty: Boolean,
    @SerializedName("pageable") val pageable: Pageable,
    @SerializedName("sort") val sort: Sort
)

data class Sort(
    @SerializedName("unsorted") val unsorted: Boolean,
    @SerializedName("sorted") val sorted: Boolean,
    @SerializedName("empty") val empty: Boolean
)


data class Pageable(
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("paged") val paged: Boolean,
    @SerializedName("unpaged") val unpaged: Boolean,
    @SerializedName("sort") val sort: Sort
)

data class Marketplace(
    @SerializedName("areas") val areas: List<Area>,
    @SerializedName("description") val description: String,
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("online") val online: Boolean,
    @SerializedName("productCatalogs") val productCatalogs: List<Any>,  // Remplacez `Any` par le type approprié si nécessaire
    @SerializedName("shopCatalogs") val shopCatalogs: List<Any>         // Remplacez `Any` par le type approprié si nécessaire
)


data class Area(
    @SerializedName("area") val area: GeoJsonPolygon,
    @SerializedName("description") val description: String,
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String
)

data class GeoJsonPolygon(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<List<List<List<Double>>>>
)
