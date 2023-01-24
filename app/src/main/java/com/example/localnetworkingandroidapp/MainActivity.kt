package com.mobilegame.localnetworkingandroidapp

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.localnetworkingandroidapp.model.ScreenViewModel
import com.example.localnetworkingandroidapp.ui.screen.Screen
import com.mobilegame.localnetworkingandroidapp.ui.theme.LocalNetworkingAndroidAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            LocalNetworkingAndroidAppTheme {
                Screen(ScreenViewModel())
            }
        }
    }
}