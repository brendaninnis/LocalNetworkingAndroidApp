package com.example.localnetworkingandroidapp.ui.screen

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension

fun getScreenLayoutConstraints(): ConstraintSet = ConstraintSet {
    val header = createRefFor(IdProvider.Header)
    val appTitle = createRefFor(IdProvider.AppTitle)
    val connectionButton = createRefFor(IdProvider.ConnectionButton)
    val bottomBar = createRefFor(IdProvider.BottomBar)
    val textField = createRefFor(IdProvider.TextField)
    val sendButton = createRefFor(IdProvider.SendButton)
    val messageList = createRefFor(IdProvider.MessageList)

    constrain(header) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        width = Dimension.fillToConstraints
        height = Dimension.value(58.dp)
    }
    constrain(appTitle) {
        top.linkTo(header.top)
        start.linkTo(parent.start, margin = 15.dp)
        bottom.linkTo(header.bottom)
        width = Dimension.wrapContent
        height = Dimension.wrapContent
    }
    constrain(connectionButton) {
        top.linkTo(header.top)
        end.linkTo(parent.end)
        bottom.linkTo(header.bottom)
        width = Dimension.value(58.dp)
        height = Dimension.fillToConstraints
    }

    constrain(messageList) {
        top.linkTo(header.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(bottomBar.top)
        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
    }

    constrain(bottomBar) {
        start.linkTo(parent.start, margin = 15.dp)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
        width = Dimension.fillToConstraints
        height = Dimension.value(100.dp)
    }
    constrain(textField) {
        top.linkTo(bottomBar.top)
        start.linkTo(bottomBar.start)
        bottom.linkTo(bottomBar.bottom)
        width = Dimension.wrapContent
        height = Dimension.wrapContent
    }
    constrain(sendButton) {
        top.linkTo(bottomBar.top)
        start.linkTo(textField.end, margin = 15.dp)
        bottom.linkTo(bottomBar.bottom)
    }
}