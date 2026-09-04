package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.CyanGlow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextGray

@Composable
fun AboutScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BlackBackground).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ABOUT TRUTH SCANNER", color = CyanGlow, fontSize = 24.sp)
            Text(
                "This is an experimental behavioral-analysis app. Results are estimates based on facial features and simulated metrics. It is not a scientific lie detector and must not be used for medical, legal, employment, or other high-stakes decisions.",
                color = TextGray,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
