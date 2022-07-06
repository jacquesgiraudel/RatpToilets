package com.jgl.ratptoilets.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
@Json(name = "Record")
data class Toilet (
    val datasetid: String?,
    val recordid: String?,
    val fields: Fields?,
    val geometry: Geometry?,
    val recordTimestamp: String?
) : Parcelable