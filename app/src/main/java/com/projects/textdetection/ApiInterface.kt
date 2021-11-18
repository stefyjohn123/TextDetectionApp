package com.projects.textdetection

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("/s/search.html")
    fun getTGAData(
        @Query("query") query: String,
        @Query("collection") collection: String
    ): Call<ResponseBody>

}