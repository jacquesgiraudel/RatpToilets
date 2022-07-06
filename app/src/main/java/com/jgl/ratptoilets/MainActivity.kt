package com.jgl.ratptoilets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.jgl.ratptoilets.listtoilets.ListToiletsScreen

class MainActivity : ComponentActivity() {

    // to catch lifecycle event for an observer in the view model
    private lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleRegistry.markState(Lifecycle.State.CREATED)

        setContent {
            ListToiletsScreen(this, lifecycle, application)
        }
    }

    public override fun onStart() {
        super.onStart()
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    public override fun onResume() {
        super.onResume()
        lifecycleRegistry.markState(Lifecycle.State.RESUMED)
    }

    override fun getLifecycle(): Lifecycle {
        if (! this::lifecycleRegistry.isInitialized){
            lifecycleRegistry = LifecycleRegistry(this)
        }

        return lifecycleRegistry
    }

    companion object{
        const val SAVE_TOILETS = "toilets"
        const val SAVE_ACCESSIBLE_ONLY = "accessibleOnly"
    }
}

