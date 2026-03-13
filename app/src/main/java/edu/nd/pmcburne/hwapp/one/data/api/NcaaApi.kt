package edu.nd.pmcburne.hwapp.one.data.api

import retrofit2.http.GET
import retrofit2.http.Path

interface NcaaApi {

    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getGameInfo(
        @Path("gender") gender: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): ApiResponse
}