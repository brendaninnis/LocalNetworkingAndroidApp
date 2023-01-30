package com.example.localnetworkingandroidapp.ui.screen.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.localnetworkingandroidapp.model.ScreenViewModel
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import com.example.localnetworkingandroidapp.ui.screen.IdProvider

@Composable
fun BottomBar(vm: ScreenViewModel) {
    val textInput by remember { vm.textInput }.collectAsState()
    val enableBar by remember { WifiConnectionState.bottomBarEnable }.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocussed by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocussed) {
        when (isFocussed) {
            false -> {}
            true -> {
                if (!vm.textFieldTouched) {
                    vm.resetKeyboardInput()
                    vm.textFieldTouched = true
                }
            }
        }
    }

    ConstraintLayout(vm.constraints, Modifier.fillMaxSize()) {
        Box( Modifier
            .layoutId(IdProvider.BottomBar)
        )
        OutlinedTextField(
            modifier = Modifier.layoutId(IdProvider.TextField) ,
            value = textInput,
            onValueChange = { vm.handleKeyboardInput(it) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = MaterialTheme.colors.secondary,
                focusedBorderColor = MaterialTheme.colors.secondary,
                unfocusedBorderColor = MaterialTheme.colors.error,
            ),
            interactionSource = interactionSource,
            enabled = enableBar,
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