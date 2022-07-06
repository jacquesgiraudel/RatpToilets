package com.jgl.ratptoilets.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fields (
    val complementAdresse: String?,
    val geoShape: GeoShape?,
    val horaire: String?,
    @Json(name = "acces_pmr")
    val accesPmr: String?,
    val arrondissement: Int?,
    @Json(name = "geo_point_2d")
    val geoPoint2d: List<Double>?,
    var distanceFromHereMeter: Int?,
    val source: String?,
    val gestionnaire: String?,
    val adresse: String?,
    val type: String?,
    val urlFicheEquipement: String?,
    val relaisBebe: String?
) : Parcelable