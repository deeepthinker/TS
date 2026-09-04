package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val result: String,
    val score: Int,
    val eyeLeft: Float,
    val eyeRight: Float,
    val smileLevel: Float,
    val voiceAnalysis: Float,
    val stressLevel: Float,
    val handMovement: Float,
    val bodyLanguage: Float
)
