package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.ScanHistory
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val historyList by db.historyDao().getAllHistory().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SCAN HISTORY", color = CyanGlow, fontSize = 24.sp)
            Button(
                onClick = {
                    coroutineScope.launch { db.historyDao().clearHistory() }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
            ) {
                Text("CLEAR", color = CyanGlow)
            }
        }

        if (historyList.isEmpty()) {
            Text("No scans recorded yet.", color = TextGray)
        } else {
            LazyColumn {
                items(historyList) { history ->
                    HistoryCard(history)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryCard(history: ScanHistory) {
    val dateString = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(history.timestamp))
    val resultColor = if (history.result == "TRUE") GreenActive else RedAlert

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dateString, color = TextGray, fontSize = 12.sp)
                Text(history.result, color = resultColor, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Score: ${history.score}%", color = CyanGlow, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Eye: ${(history.eyeLeft + history.eyeRight) / 2}%", color = TextGray, fontSize = 10.sp)
                Text("Smile: ${history.smileLevel}%", color = TextGray, fontSize = 10.sp)
                Text("Stress: ${history.stressLevel}%", color = TextGray, fontSize = 10.sp)
            }
        }
    }
}
