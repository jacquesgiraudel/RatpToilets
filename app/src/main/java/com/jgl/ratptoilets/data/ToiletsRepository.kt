package com.jgl.ratptoilets.data

import com.jgl.ratptoilets.data.model.ToiletsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToiletsRepository {
    private val toiletsDao = ToiletsDao()

    suspend fun getToilets(): ToiletsRequest {
        return withContext(Dispatchers.IO){
            toiletsDao.ratpService.getToilets()
        }
    }
}