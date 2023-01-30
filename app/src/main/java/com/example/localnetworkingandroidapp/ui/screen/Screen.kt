package com.example.localnetworkingandroidapp.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.localnetworkingandroidapp.model.ScreenViewModel
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import com.example.localnetworkingandroidapp.ui.screen.layout.BottomBar
import com.example.localnetworkingandroidapp.ui.screen.layout.ChatBody
import com.example.localnetworkingandroidapp.ui.screen.layout.Header

@Composable
fun Screen(vm: ScreenViewModel) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        WifiConnectionState.init(context)
    }

    Header(vm)
    ChatBody(vm)
    BottomBar(vm)
}