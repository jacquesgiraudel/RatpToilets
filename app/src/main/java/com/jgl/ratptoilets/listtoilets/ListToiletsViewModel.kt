package com.jgl.ratptoilets.listtoilets

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.google.android.gms.location.*
import com.jgl.ratptoilets.MainActivity
import com.jgl.ratptoilets.data.ToiletsRepository
import com.jgl.ratptoilets.data.model.Toilet
import kotlinx.coroutines.launch

class ListToiletsViewModel(private val app: Application, lifecycle: Lifecycle, private val savedStateHandle: SavedStateHandle): AndroidViewModel(app), DefaultLifecycleObserver {

    // request to the toilets api
    private var _status: LiveData<Result> = MutableLiveData(Result.Pending)
    val toilets: LiveData<Result> = _status

    // accessible_only filter state
    private var _accessibleOnly: LiveData<Boolean> = MutableLiveData(false)
    val accessibleOnly: LiveData<Boolean> = _accessibleOnly

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = false

    init {

        if (savedStateHandle.contains(MainActivity.SAVE_TOILETS)){
            var savedToilets = savedStateHandle.getLiveData<List<Toilet>>(MainActivity.SAVE_TOILETS).value
            if (savedStateHandle.contains(MainActivity.SAVE_ACCESSIBLE_ONLY)){
                _accessibleOnly = savedStateHandle.getLiveData<Boolean>(MainActivity.SAVE_ACCESSIBLE_ONLY)
                if (_accessibleOnly.value == true){
                    savedToilets = savedToilets!!.filter { it.fields!!.accesPmr == "Oui" }
                    (accessibleOnly as MutableLiveData).value = true
                }

            }
            (toilets as MutableLiveData).value =
                Result.Success(savedToilets)
        }
        else {
            viewModelScope.launch {
                try {
                    val toiletsRequest = ToiletsRepository().getToilets()
                    (_status as MutableLiveData).value = Result.Success(toiletsRequest.records)
                    savedStateHandle[MainActivity.SAVE_TOILETS] = (toilets.value as Result.Success).toilets
                }
                catch (exception: Exception){
                    (_status as MutableLiveData).value = Result.Error(Result.ErrorMsg.DEFAULT)
                }
            }
        }

        // synchronize activity lifecycle to the view model to catch events
        lifecycle.addObserver(this)
    }

    /**
     * Toggle between accessible only toilets and all toilets
     */
    fun toggleAccessibility(){
        if (_status.value is Result.Success) {

            var toilets = savedStateHandle.getLiveData<List<Toilet>>(MainActivity.SAVE_TOILETS).value
            val accessibleOnlyNewValue = ! (accessibleOnly.value as Boolean)
            if (accessibleOnlyNewValue){
                toilets = toilets!!.filter { it.fields!!.accesPmr == "Oui" }
            }
            savedStateHandle[MainActivity.SAVE_ACCESSIBLE_ONLY] = accessibleOnlyNewValue
            (accessibleOnly as MutableLiveData).value = accessibleOnlyNewValue
            (_status as MutableLiveData).value = Result.Success(toilets)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (toilets.value !is Result.Success) return

                val toilets = (toilets.value as Result.Success).toilets
                toilets ?: return

                val lastPosition = locationResult.lastLocation
                toilets.map {
                    if (it.fields!!.geoPoint2d != null) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            lastPosition!!.latitude,
                            lastPosition.longitude,
                            it.fields.geoPoint2d!![0],
                            it.fields.geoPoint2d[1],
                            results
                        )
                        it.fields.distanceFromHereMeter = results[0].toInt()
                    }
                }

                (_status as MutableLiveData).value = Result.Success(toilets)
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        if (
            ContextCompat.checkSelfPermission(
                app.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (! requestingLocationUpdates) {
                startLocationUpdates()
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        requestingLocationUpdates = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(app.applicationContext)
        val locationRequest = LocationRequest.create().apply {
            interval = TIME_BETWEEN_2_STATIONS
            fastestInterval = TIME_BETWEEN_2_STATIONS
            priority = Priority.PRIORITY_LOW_POWER
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        if (this::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            requestingLocationUpdates = false
        }
    }

    sealed class Result {
        data class Success(val toilets: List<Toilet>?): Result(){
            override fun equals(other: Any?): Boolean {
                return this === other
            }

            override fun hashCode(): Int {
                return toilets?.hashCode() ?: 0
            }
        }
        data class Error(val errorMsg: ErrorMsg) : Result()
        object Pending : Result()
        enum class ErrorMsg{DEFAULT}
    }

    companion object{
        const val TIME_BETWEEN_2_STATIONS = 20000L
    }
}