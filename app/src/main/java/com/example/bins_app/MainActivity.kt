package com.example.bins_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bins_app.ui.VinceAppMainScreen
import com.example.bins_app.ui.theme.BinsappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BinsappTheme {
                VinceAppMainScreen()
            }
        }
    }
}

