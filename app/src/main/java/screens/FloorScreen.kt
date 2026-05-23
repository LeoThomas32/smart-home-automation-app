package com.example.homeautomation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FloorScreen(

    onFloorSelected:()->Unit

){

    Column(

        Modifier
            .fillMaxSize()
            .padding(20.dp)

    ){

        Text(

            "🏢 Floors",

            style=

                MaterialTheme
                    .typography
                    .displaySmall

        )

        Spacer(
            Modifier.height(
                20.dp
            )
        )

        listOf(

            "Ground Floor",

            "First Floor"

        ).forEach{

            ElevatedCard(

                shape=

                    RoundedCornerShape(
                        28.dp
                    ),

                modifier=

                    Modifier
                        .fillMaxWidth()
                        .padding(
                            10.dp
                        )

            ){

                Button(

                    modifier=

                        Modifier
                            .fillMaxWidth(),

                    onClick=

                        onFloorSelected

                ){

                    Text(
                        it
                    )

                }

            }

        }

    }

}