package com.example.localnetworkingandroidapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.Names
import com.example.localnetworkingandroidapp.model.ScreenViewModel
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import com.example.localnetworkingandroidapp.ui.theme.CenterComposable

@Composable
fun Screen(vm: ScreenViewModel) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        WifiConnectionState.init(context)
    }

    val messages by remember { vm.canonicalThread.messageList }.collectAsState()
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

    ConstraintLayout(vm.constraints, Modifier.fillMaxSize()) {
        Box( Modifier.layoutId(IdProvider.Header) )
        Box( Modifier.layoutId(IdProvider.BottomBar) )
        Box( Modifier .layoutId(IdProvider.MessageList) ) {
            LazyColumn() {
                itemsIndexed( messages ) { index, message ->
                    val height =
                        if (message.sender == Message.SERVER_MSG_SENDER)
                            Modifier.height(30.dp)
                        else
                            Modifier.wrapContentHeight()
                    Column(
                        Modifier
                            .then(height)
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = if (message.sender == Message.SERVER_MSG_SENDER) message.text
                            else message.sender ,
                            textAlign = when (message.sender) {
                                Message.SERVER_MSG_SENDER -> TextAlign.Center
                                Names.deviceName -> TextAlign.End
                                else -> TextAlign.Start
                            },
                            style = TextStyle(
                                fontWeight = FontWeight.Light,
                            )
                        )
                        if (message.sender != Message.SERVER_MSG_SENDER) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = message.text,
                                textAlign = when (message.sender) {
                                    Message.SERVER_MSG_SENDER -> TextAlign.Center
                                    Names.deviceName -> TextAlign.End
                                    else -> TextAlign.Start
                                },
                                style = TextStyle( fontSize = 18.sp, )
                            )
                            Spacer(modifier = Modifier.height(25.dp))
                        }
                    }
                }
            }
        }
    }


    val textInput by remember { vm.textInput }.collectAsState()
    val enableBar by remember { WifiConnectionState.bottomBarEnable }.collectAsState()
    ConstraintLayout(vm.constraints, Modifier.fillMaxSize()) {
        Box( Modifier .layoutId(IdProvider.BottomBar) )
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