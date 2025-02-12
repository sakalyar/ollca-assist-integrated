package com.ollcassist.onboardassistant.map

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("all-marketplaces")
    suspend fun getAllMarketplaces(
        @Query("online") online: Boolean,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("specified") specified: Boolean,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String
    ): Response<MarketplaceResponse>
}
