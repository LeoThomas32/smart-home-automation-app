package com.example.homeautomation.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val Colors = darkColorScheme(

    background =
        Background,

    surface =
        Surface,

    primary =
        Accent,

    secondary =
        Accent2,

    onBackground =
        Text,

    onSurface =
        Text

)

@Composable
fun HomeAutomationTheme(

    content:

    @Composable ()->Unit

){

    MaterialTheme(

        colorScheme=
            Colors,

        content=
            content

    )

}