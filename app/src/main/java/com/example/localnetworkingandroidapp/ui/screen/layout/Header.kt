package com.example.localnetworkingandroidapp.ui.screen.layout

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.localnetworkingandroidapp.data.LinkStates
import com.example.localnetworkingandroidapp.model.ScreenViewModel
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import com.example.localnetworkingandroidapp.ui.screen.IdProvider
import com.example.localnetworkingandroidapp.ui.theme.CenterComposable

@Composable
fun Header(vm: ScreenViewModel) {
    val linkState by remember { WifiConnectionState.linkState }.collectAsState()

    ConstraintLayout(vm.constraints, Modifier.fillMaxSize()) {
        Box(
            Modifier
                .layoutId(IdProvider.Header)
                .background(MaterialTheme.colors.primary)
        )
        Text(
            modifier = Modifier.layoutId(IdProvider.AppTitle),
            text = "Belgariad Chat",
            color = Color.White,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default
            )
        )
        Box(
            Modifier
                .layoutId(IdProvider.ConnectionButton)
                .clickable { vm.connectionButtonClick() }
        ) {
            CenterComposable( id = IdProvider.ConnectionButtonText ) {
                Text(
                    text = if (linkState.name == LinkStates.NotConnected.name) "JOIN" else "LEAVE" ,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Default
                    )
                )
            }
        }
    }
}