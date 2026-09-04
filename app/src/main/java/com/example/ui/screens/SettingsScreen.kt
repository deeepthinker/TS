package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsRepository
import com.example.data.dataStore
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DarkBorder
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository(context.dataStore) }
    val coroutineScope = rememberCoroutineScope()
    
    val threshold by settingsRepo.threshold.collectAsState(initial = 60)
    val voiceEnabled by settingsRepo.voiceEnabled.collectAsState(initial = true)
    val bodyEnabled by settingsRepo.bodyEnabled.collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        Text("SETTINGS", color = CyanGlow, fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))
        
        // Threshold
        Text("Result Threshold: $threshold%", color = CyanGlow)
        Slider(
            value = threshold.toFloat(),
            onValueChange = { 
                coroutineScope.launch { settingsRepo.setThreshold(it.toInt()) }
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = CyanGlow,
                activeTrackColor = CyanGlow,
                inactiveTrackColor = DarkBorder
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Voice Enabled
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable Voice Analysis", color = CyanGlow)
            Switch(
                checked = voiceEnabled,
                onCheckedChange = { 
                    coroutineScope.launch { settingsRepo.setVoiceEnabled(it) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanGlow,
                    checkedTrackColor = DarkBorder
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Body Enabled
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable Body/Hand Tracking", color = CyanGlow)
            Switch(
                checked = bodyEnabled,
                onCheckedChange = { 
                    coroutineScope.launch { settingsRepo.setBodyEnabled(it) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanGlow,
                    checkedTrackColor = DarkBorder
                )
            )
        }
    }
}
