package com.jgl.ratptoilets.listtoilets

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.jgl.ratptoilets.R
import com.jgl.ratptoilets.data.model.Toilet
import com.jgl.ratptoilets.listtoilets.ListToiletsViewModel.Result.*
import com.jgl.ratptoilets.ui.theme.RatpToiletsTheme
import com.jgl.ratptoilets.ui.theme.Typography

@Composable
fun ListToiletsScreen(owner: SavedStateRegistryOwner, lifeCycle: Lifecycle, application: Application) {

    val context = LocalContext.current

    // factory to manage the saved state and be able to observe the activity lifecycle in the view model
    val viewModel: ListToiletsViewModel = viewModel(
        factory = object : AbstractSavedStateViewModelFactory(owner, null) {

            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                val viewModel = ListToiletsViewModel(application, lifeCycle, handle)

                return viewModel as T
            }
        }
    )

    val result = viewModel.toilets.observeAsState()
    val accessibleOnly = viewModel.accessibleOnly.observeAsState()
    val onToggleAccessibility: () -> Unit = {
        viewModel.toggleAccessibility()
    }
    var showDistancePermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                showDistancePermission = true
                viewModel.startLocationUpdates()
            }
        }
    }

    LaunchedEffect("") {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                showDistancePermission = true
                viewModel.startLocationUpdates()
            }
            else -> {
                // Asking for permission
                launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    RatpToiletsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            ToiletsListContainer(result = result.value!!, accessibleOnly.value!!, onToggleAccessibility) {
                ToiletsList((result.value as Success).toilets!!, showDistancePermission)
            }
        }
    }
}

@Composable
fun ToiletsListContainer(result: ListToiletsViewModel.Result, accessibleOnly: Boolean, toggleClicker: () -> Unit, content: @Composable () -> Unit){
    Scaffold(
        modifier = Modifier
            .focusable(true),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.listtoilets_title)) },
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.onSecondary,
                actions = {
                    AccessibilityToggleButton(accessibleOnly, toggleClicker)
                }
            )
            }
        ){
        when (result) {
            is Success -> {
                content()
            }

            is Error -> {
                Row(Modifier.padding(16.dp)) {
                    Text(stringResource(id = R.string.all_error))
                }
            }

            is Pending -> {
                Box (contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }
        }

    }
}

@Composable
fun AccessibilityToggleButton(accessibleOnly: Boolean, toggleClicker: () -> Unit){
    IconToggleButton(checked = accessibleOnly, onCheckedChange = { toggleClicker() }) {
        val accessibleIconId = if (accessibleOnly) R.drawable.ic_not_accessible else R.drawable.ic_accessible
        val contentDescription: Int = if (accessibleOnly) R.string.listtoilets_row_icon_notaccessible else R.string.listtoilets_row_icon_accessible

        Icon(painter = painterResource(id = accessibleIconId), modifier = Modifier.size(24.dp),
            contentDescription = stringResource(id = contentDescription))
    }
}

@Composable
fun ToiletsList(toilets: List<Toilet>, showDistance: Boolean){
    LazyColumn {
        items(toilets) { toilet ->
            key(toilet){
                ToiletRow(toilet, showDistance)
            }
        }
    }
}

@Composable
fun ToiletRow(toilet: Toilet, showDistance: Boolean){
    Column (modifier = Modifier.padding(16.dp)){
        Row (verticalAlignment = Alignment.CenterVertically){
           Text(text = toilet.fields!!.type ?: "", style = Typography.body1)
           AccessibleIcon(accessPmr = toilet.fields.accesPmr)
        }
        DistanceLine(showDistance, toilet)
        Row {
            AddressLine(zipCode = toilet.fields!!.arrondissement?.toString(), address = toilet.fields.adresse)
        }
        Text(text = toilet.fields!!.horaire ?: "", style = Typography.body2)
    }
}

@Composable
fun DistanceLine(showDistance: Boolean, toilet: Toilet) {
    if (showDistance && toilet.fields!!.distanceFromHereMeter != null) {
        val meters = toilet.fields.distanceFromHereMeter!! < 1000
        val stringResToUse = if (meters) R.string.listtoilets_row_distance_meter else R.string.listtoilets_row_distance_km
        val distanceRef = toilet.fields.distanceFromHereMeter!!.toFloat()
        val distance = if (meters) distanceRef else distanceRef/1000

        Text(text = stringResource(stringResToUse, distance), style = Typography.body2, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun AccessibleIcon(accessPmr: String?){

    if (accessPmr == null)
        return

    val accessibleIconId = if (accessPmr == "Oui") R.drawable.ic_accessible else R.drawable.ic_not_accessible
    val contentDescription = if (accessPmr == "Oui") R.string.listtoilets_row_icon_accessible else R.string.listtoilets_row_icon_notaccessible

    Icon(
        painter = painterResource(id = accessibleIconId),
        tint = MaterialTheme.colors.secondary,
        modifier = Modifier
            .offset(4.dp)
            .size(18.dp),
        contentDescription = stringResource(id = contentDescription)
    )
}

@Composable
fun AddressLine(zipCode: String?, address: String?){
    if (zipCode != null){
        Text(text = stringResource(R.string.listtoilets_row_address, zipCode, address as String), style = Typography.body2)
    }
    else {
        Text(text = address ?: "", style = Typography.body2)
    }
}