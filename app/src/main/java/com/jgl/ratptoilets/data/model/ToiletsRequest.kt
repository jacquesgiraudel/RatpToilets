package com.jgl.ratptoilets.data.model

data class ToiletsRequest (
    val nhits: Int?,
    val parameters: Parameters?,
    val records: List<Toilet>?
)