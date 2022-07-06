package com.jgl.ratptoilets.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeoShape (
    val coordinates: List<List<Double>>?,
    val type: String?
) : Parcelable