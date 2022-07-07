package com.jgl.ratptoilets.data

import com.jgl.ratptoilets.data.model.ToiletsRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class ToiletsDao {

    private var ratpService: RatpService

    init{
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.NONE

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        ratpService = retrofit.create(RatpService::class.java)
    }

    suspend fun getToilets(): ToiletsRequest{
        return withContext(Dispatchers.IO){
            ratpService.getToilets()
        }
    }

    companion object{
        private const val TOILETS_DATASET = "sanisettesparis2011"
        private const val BASE_URL = "https://data.ratp.fr/api/records/1.0/"
    }

    interface RatpService {
        @GET("search/")
        suspend fun getToilets(@Query("dataset") dataSet: String = TOILETS_DATASET): ToiletsRequest
    }
}