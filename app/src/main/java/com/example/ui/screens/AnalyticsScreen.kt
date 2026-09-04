package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.TextGray

@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val historyList by db.historyDao().getAllHistory().collectAsState(initial = emptyList())

    val totalScans = historyList.size
    val trueCount = historyList.count { it.result == "TRUE" }
    val falseCount = historyList.count { it.result == "FALSE" }
    val avgScore = if (totalScans > 0) historyList.sumOf { it.score } / totalScans else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        Text("ANALYTICS", color = CyanGlow, fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))
        
        AnalyticsCard(title = "Total Scans", value = "$totalScans")
        Spacer(modifier = Modifier.height(16.dp))
        AnalyticsCard(title = "Average Score", value = "$avgScore%")
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.weight(1f)) {
                AnalyticsCard(title = "TRUE Results", value = "$trueCount")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                AnalyticsCard(title = "FALSE Results", value = "$falseCount")
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = TextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = CyanGlow, fontSize = 32.sp)
        }
    }
}
