package com.example.mobiltelesco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyAssetsToInternalStorage(this)
        // Navigate to CameraActivity
        startActivity(Intent(this, CameraActivity::class.java))
        finish() // Optional: Finish MainActivity to prevent returning to it
        setContent {
            // Optional: Add a placeholder UI if needed
        }
    }

    private fun copyAssetsToInternalStorage(context: Context) {
        val assets = listOf("nanodet_m_416.tflite", "labels.txt")
        assets.forEach { asset ->
            try {
                context.assets.open(asset).use { input ->
                    File(context.filesDir, asset).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                android.util.Log.d("MainActivity", "Successfully copied $asset")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to copy $asset", e)
            }
        }
    }
}