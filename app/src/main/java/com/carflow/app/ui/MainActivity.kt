package com.carflow.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.carflow.app.ui.navigation.CarflowNavGraph
import com.carflow.app.ui.theme.CarflowTheme
import com.carflow.app.ui.theme.carflowColors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarflowTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(carflowColors.bg),
                ) {
                    CarflowNavGraph()
                }
            }
        }
    }
}
