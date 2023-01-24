package com.example.localnetworkingandroidapp.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension

@Composable
fun CenterComposable(id: String, content: @Composable () -> Unit) { Center(id, CenterType.Center, content) }

@Composable
private fun Center(id: String, type: CenterType, content: @Composable () -> Unit) {
    val constraints = remember { getCenterConstraintSet(id, type) }

    ConstraintLayout(constraints, Modifier.fillMaxSize() ) {
        Box(Modifier.layoutId(id)) {
            content.invoke()
        }
    }
}

private fun getCenterConstraintSet(id: String, type: CenterType) = ConstraintSet {
    val draw = createRefFor(id)

    constrain( draw ) {
        if (type == CenterType.Center || type == CenterType.CenterVertically) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        }
        if (type == CenterType.Center || type == CenterType.CenterHorizontally) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        width = Dimension.wrapContent
        height = Dimension.wrapContent
    }
}

private enum class CenterType {
    Center, CenterHorizontally , CenterVertically
}
