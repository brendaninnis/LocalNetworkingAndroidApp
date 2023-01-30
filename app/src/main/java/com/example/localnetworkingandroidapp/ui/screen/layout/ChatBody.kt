package com.example.localnetworkingandroidapp.ui.screen.layout

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.example.localnetworkingandroidapp.data.LinkStates
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.Names
import com.example.localnetworkingandroidapp.model.ScreenViewModel
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import com.example.localnetworkingandroidapp.ui.screen.IdProvider

@Composable
fun ChatBody(vm: ScreenViewModel) {

    val messages by remember { vm.canonicalThread.messageList }.collectAsState()
    val linkState by remember { WifiConnectionState.linkState }.collectAsState()

    // Creates an [InfiniteTransition] instance for managing child animations.
    val infiniteTransition = rememberInfiniteTransition()
    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val pointNumber by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            // Infinitely repeating a 3000 tween animation using default easing curve.
            animation = tween(3000),
            // After each iteration of the animation (i.e. every 3000), the animation will
            // start again from the [initialValue] defined above.
            repeatMode = RepeatMode.Restart
        )
    )
    val animatedText = when (pointNumber.toInt()) {
        0 -> " "
        1 -> " ."
        2 -> " .."
        3 -> " ..."
        else -> " ERROR"
    }

    val constraints = remember {
        ConstraintSet {
            val connectingText = createRefFor(IdProvider.ConnectingText)
            val pointsText = createRefFor(IdProvider.PointsText)

            constrain( connectingText ) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
            }
            constrain( pointsText ) {
                start.linkTo(connectingText.end)
                bottom.linkTo(connectingText.bottom)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
            }
        }
    }

    ConstraintLayout(vm.constraints, Modifier.fillMaxSize()) {
        Box( Modifier.layoutId(IdProvider.Header) )
        Box( Modifier.layoutId(IdProvider.BottomBar) )
        Box( Modifier .layoutId(IdProvider.MessageList) ) {
            if (linkState == LinkStates.Connecting) {
                ConstraintLayout(constraints, Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.layoutId(IdProvider.ConnectingText),
                        text = "Connecting",
                        textAlign = TextAlign.Center,
                        style = TextStyle( fontWeight = FontWeight.Light, )
                    )
                    Text(
                        modifier = Modifier.layoutId(IdProvider.PointsText),
                        text = animatedText,
                        textAlign = TextAlign.Center,
                        style = TextStyle( fontWeight = FontWeight.Light, )
                    )
                }
            } else {
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
    }
}