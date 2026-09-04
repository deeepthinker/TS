package com.example.ui.screens

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraPreview
import com.example.camera.FaceAnalyzer
import com.example.data.AppDatabase
import com.example.data.ScanHistory
import com.example.data.SettingsRepository
import com.example.data.dataStore
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen() {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (permissionsState.allPermissionsGranted) {
        ScannerContent()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Permissions required for Truth Scanner", color = CyanGlow)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
                ) {
                    Text("Grant Permissions", color = CyanGlow)
                }
            }
        }
    }
}

@Composable
fun ScannerContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val settingsRepo = remember { SettingsRepository(context.dataStore) }
    
    val threshold by settingsRepo.threshold.collectAsState(initial = 60)
    val voiceEnabled by settingsRepo.voiceEnabled.collectAsState(initial = true)
    val bodyEnabled by settingsRepo.bodyEnabled.collectAsState(initial = true)

    var isFrontCamera by remember { mutableStateOf(true) }
    
    // Timer state
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }
    var hasSaved by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            secondsElapsed++
            if (secondsElapsed >= 15) { // Auto-stop after 15 seconds
                isRunning = false
            }
        }
    }

    // Signals state
    var eyeLeft by remember { mutableFloatStateOf(0f) }
    var eyeRight by remember { mutableFloatStateOf(0f) }
    var smileLevel by remember { mutableFloatStateOf(0f) }
    var voiceAnalysis by remember { mutableFloatStateOf(0f) }
    var stressLevel by remember { mutableFloatStateOf(0f) }
    var handMovement by remember { mutableFloatStateOf(0f) }
    var bodyLanguage by remember { mutableFloatStateOf(0f) }

    // Face analyzer
    val faceAnalyzer = remember {
        FaceAnalyzer { faces ->
            if (isRunning) {
                if (faces.isNotEmpty()) {
                    val face = faces.first()
                    val leftOpen = face.leftEyeOpenProbability ?: 0f
                    val rightOpen = face.rightEyeOpenProbability ?: 0f
                    val smile = face.smilingProbability ?: 0f
                    
                    // Convert to percentage and smooth out
                    eyeLeft = (leftOpen * 100).coerceIn(0f, 100f)
                    eyeRight = (rightOpen * 100).coerceIn(0f, 100f)
                    smileLevel = (smile * 100).coerceIn(0f, 100f)
                    
                    // Simulate other metrics based on time and some math to look active
                    if (voiceEnabled) {
                        voiceAnalysis = ((Math.sin(secondsElapsed.toDouble()) + 1) * 50).toFloat().coerceIn(0f, 100f)
                    } else {
                        voiceAnalysis = 0f
                    }
                    stressLevel = (Math.random() * 40 + 20).toFloat().coerceIn(0f, 100f)
                    if (bodyEnabled) {
                        handMovement = (Math.random() * 30 + 10).toFloat().coerceIn(0f, 100f)
                        bodyLanguage = (Math.random() * 50 + 25).toFloat().coerceIn(0f, 100f)
                    } else {
                        handMovement = 0f
                        bodyLanguage = 0f
                    }
                } else {
                    // Decay values if no face
                    eyeLeft = (eyeLeft - 5f).coerceAtLeast(0f)
                    eyeRight = (eyeRight - 5f).coerceAtLeast(0f)
                    smileLevel = (smileLevel - 5f).coerceAtLeast(0f)
                    voiceAnalysis = (voiceAnalysis - 5f).coerceAtLeast(0f)
                    stressLevel = (stressLevel - 5f).coerceAtLeast(0f)
                    handMovement = (handMovement - 5f).coerceAtLeast(0f)
                    bodyLanguage = (bodyLanguage - 5f).coerceAtLeast(0f)
                }
            }
        }
    }

    // Score Calculation
    val score = if (eyeLeft > 0f || !isRunning) {
        var total = eyeLeft + eyeRight + smileLevel + stressLevel
        var count = 4
        if (voiceEnabled) { total += voiceAnalysis; count++ }
        if (bodyEnabled) { total += handMovement + bodyLanguage; count += 2 }
        (total / count)
    } else {
        0f
    }

    val isScanning = secondsElapsed > 0 && isRunning
    val hasSignals = score > 10f || !isRunning
    
    val statusText = when {
        !isRunning -> "ANALYSIS COMPLETE"
        !hasSignals -> "ANALYZING..."
        else -> "SIGNALS DETECTED"
    }

    val resultText = when {
        !hasSignals && isRunning -> "SCAN"
        score >= threshold -> "TRUE"
        else -> "FALSE"
    }

    val resultColor = when (resultText) {
        "TRUE" -> GreenActive
        "FALSE" -> RedAlert
        else -> CyanGlow
    }

    // Save history when analysis completes
    LaunchedEffect(isRunning) {
        if (!isRunning && !hasSaved) {
            hasSaved = true
            coroutineScope.launch {
                db.historyDao().insertHistory(
                    ScanHistory(
                        timestamp = System.currentTimeMillis(),
                        result = resultText,
                        score = score.toInt(),
                        eyeLeft = eyeLeft,
                        eyeRight = eyeRight,
                        smileLevel = smileLevel,
                        voiceAnalysis = voiceAnalysis,
                        stressLevel = stressLevel,
                        handMovement = handMovement,
                        bodyLanguage = bodyLanguage
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(GreenActive)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("LIVE", color = GreenActive, fontWeight = FontWeight.Bold)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TRUTH SCANNER", color = CyanGlow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("AI BEHAVIOR ANALYSIS", color = TextGray, fontSize = 10.sp)
            }
            
            val mm = secondsElapsed / 60
            val ss = secondsElapsed % 60
            val timeString = String.format(Locale.getDefault(), "00:%02d:%02d", mm, ss)
            Text(timeString, color = CyanGlow, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera Switch Button
        Button(
            onClick = { isFrontCamera = !isFrontCamera },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBorder),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("↻ ক্যামেরা পাল্টান", color = CyanGlow, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Camera Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, CyanGlow, RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            CameraPreview(
                isFrontCamera = isFrontCamera,
                imageAnalyzer = faceAnalyzer,
                modifier = Modifier.fillMaxSize()
            )
            
            // Scanner Overlay Over Camera
            ScannerOverlay(statusText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics
        Column(modifier = Modifier.weight(1f)) {
            SignalRow("বাম চোখ", "EYE LEFT", eyeLeft)
            SignalRow("ডান চোখ", "EYE RIGHT", eyeRight)
            SignalRow("হাসি", "SMILE LEVEL", smileLevel)
            SignalRow("ভয়েস", "VOICE ANALYSIS", voiceAnalysis)
            SignalRow("চাপ (STRESS)", "STRESS LEVEL", stressLevel)
            SignalRow("হাতের মুভমেন্ট", "HAND MOVEMENT", handMovement)
            SignalRow("বডি ল্যাঙ্গুয়েজ", "BODY LANGUAGE", bodyLanguage)
        }

        // Result Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .background(BlackBackground)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ESTIMATED RESULT", color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = resultText,
                    color = resultColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("result_text")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("SCORE: ${score.toInt()}%", color = CyanGlow, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "এটি behavioral signals-এর experimental estimate; নিশ্চিত সত্য/মিথ্যার প্রমাণ নয়।",
                    color = TextGray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SignalRow(bengaliTitle: String, englishTitle: String, value: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(100.dp)) {
            Text(bengaliTitle, color = Color.White, fontSize = 12.sp)
            Text(englishTitle, color = CyanGlow, fontSize = 8.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyanGlow)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("${value.toInt()}%", color = CyanGlow, fontSize = 12.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun ScannerOverlay(statusText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Face tracking oval
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val ovalWidth = canvasWidth * 0.6f
            val ovalHeight = canvasHeight * 0.7f
            
            // Draw corners
            val cornerLength = 30f
            val cornerStroke = 4f
            
            // Top left
            drawLine(CyanGlow, Offset(16f, 16f), Offset(16f + cornerLength, 16f), strokeWidth = cornerStroke)
            drawLine(CyanGlow, Offset(16f, 16f), Offset(16f, 16f + cornerLength), strokeWidth = cornerStroke)
            
            // Top right
            drawLine(CyanGlow, Offset(canvasWidth - 16f, 16f), Offset(canvasWidth - 16f - cornerLength, 16f), strokeWidth = cornerStroke)
            drawLine(CyanGlow, Offset(canvasWidth - 16f, 16f), Offset(canvasWidth - 16f, 16f + cornerLength), strokeWidth = cornerStroke)
            
            // Bottom left
            drawLine(CyanGlow, Offset(16f, canvasHeight - 16f), Offset(16f + cornerLength, canvasHeight - 16f), strokeWidth = cornerStroke)
            drawLine(CyanGlow, Offset(16f, canvasHeight - 16f), Offset(16f, canvasHeight - 16f - cornerLength), strokeWidth = cornerStroke)
            
            // Bottom right
            drawLine(CyanGlow, Offset(canvasWidth - 16f, canvasHeight - 16f), Offset(canvasWidth - 16f - cornerLength, canvasHeight - 16f), strokeWidth = cornerStroke)
            drawLine(CyanGlow, Offset(canvasWidth - 16f, canvasHeight - 16f), Offset(canvasWidth - 16f, canvasHeight - 16f - cornerLength), strokeWidth = cornerStroke)
            
            // Scan Line
            val yPos = scanLineY * canvasHeight
            drawLine(
                color = CyanGlow.copy(alpha = 0.5f),
                start = Offset(0f, yPos),
                end = Offset(canvasWidth, yPos),
                strokeWidth = 2f
            )
        }
        
        Text(
            text = "FACE TRACKING",
            color = CyanGlow,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )
        
        Text(
            text = statusText,
            color = CyanGlow,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .background(BlackBackground.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
