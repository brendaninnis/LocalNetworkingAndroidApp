package com.example.localnetworkingandroidapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.localnetworkingandroidapp.model.ScreenViewModel
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import com.example.localnetworkingandroidapp.ui.theme.CenterComposable

@Composable
fun Screen(vm: ScreenViewModel) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        WifiConnectionState.init(context)
    }

    val connectionButtonText by remember { vm.connectionButton }.collectAsState()
    ConstraintLayout(vm.constraints, Modifier.fillMaxSize()) {
        Box(
            Modifier
                .layoutId(IdProvider.Header)
                .background(MaterialTheme.colors.primary)
        )
        Text(
            modifier = Modifier.layoutId(IdProvider.AppTitle),
            text = "Belgariad Chat v2",
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
                    text = connectionButtonText,
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

    val textInput by remember { vm.textInput }.collectAsState()
    val enableBar by remember { vm.bottomBarEnable }.collectAsState()

    ConstraintLayout(vm.constraints, Modifier.fillMaxSize()) {
        Box(
            Modifier
                .layoutId(IdProvider.BottomBar)
        ) { }
        OutlinedTextField(
            modifier = Modifier.layoutId(IdProvider.TextField),
            value = textInput,
            onValueChange = { vm.handleKeyboardInput(it) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = MaterialTheme.colors.secondary,
                focusedBorderColor = MaterialTheme.colors.secondary,
                unfocusedBorderColor = MaterialTheme.colors.error,
                textColor = MaterialTheme.colors.error,
            ),
        )
        Box(
            Modifier
                .layoutId(IdProvider.SendButton)
                .clickable(enabled = enableBar) {
                    vm.handleSendButtonClick()
                }
        ) {
            Text(
                text = "Send",
                color = if (enableBar) MaterialTheme.colors.secondary else MaterialTheme.colors.error,
                style = TextStyle( fontSize = 18.sp, )
            )
        }
    }
}