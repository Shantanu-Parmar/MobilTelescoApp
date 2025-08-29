//package com.example.mobiltelesco
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Size
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import java.util.concurrent.Executors
//import android.graphics.Bitmap
//import android.graphics.ImageFormat
//import android.graphics.YuvImage
//import java.io.ByteArrayOutputStream
//import androidx.camera.core.ImageProxy
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.ui.text.drawText
//import androidx.compose.ui.text.rememberTextMeasurer
//
//class CameraActivity : ComponentActivity() {
//    private lateinit var nanoDet: NanoDetTFLite
//    private val permissionState = mutableStateOf(false)
//    private val detectionResults = mutableStateOf<List<DetectionResult>>(emptyList())
//    private lateinit var cameraProvider: ProcessCameraProvider
//    private lateinit var previewView: PreviewView
//    private var isCameraBound = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        nanoDet = NanoDetTFLite(this)
//        previewView = PreviewView(this).apply {
//            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//            scaleType = PreviewView.ScaleType.FILL_CENTER
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
//            permissionState.value = false
//        } else {
//            permissionState.value = true
//        }
//        setContent {
//            if (permissionState.value) {
//                CameraPreview(previewView, detectionResults.value)
//            } else {
//                PermissionPrompt {
//                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
//                }
//            }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        if (permissionState.value && !isCameraBound) {
//            startCamera()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (::cameraProvider.isInitialized) {
//            cameraProvider.unbindAll()
//            isCameraBound = false
//            android.util.Log.d("CameraActivity", "Camera unbound in onStop")
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            permissionState.value = true
//            startCamera()
//        }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            try {
//                cameraProvider = cameraProviderFuture.get()
//                val preview = Preview.Builder()
//                    .setTargetResolution(Size(1080, 1920))
//                    .build()
//                    .also {
//                        previewView.post {
//                            it.setSurfaceProvider(previewView.surfaceProvider)
//                            android.util.Log.d("CameraActivity", "Surface provider set")
//                        }
//                    }
//                val imageAnalysis = ImageAnalysis.Builder()
//                    .setTargetResolution(Size(416, 416))
//                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
//                    .build()
//                    .also {
//                        it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
//                            try {
//                                android.util.Log.d("CameraActivity", "Analyzer running")
//                                val bitmap = imageProxy.toBitmap()
//                                android.util.Log.d("CameraActivity", "Bitmap created: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")
//                                val results = nanoDet.detect(bitmap)
//                                android.util.Log.d("CameraActivity", "Inference results: ${results.size}")
//                                detectionResults.value = results
//                            } catch (e: Exception) {
//                                android.util.Log.e("CameraActivity", "Analyzer error", e)
//                            } finally {
//                                imageProxy.close()
//                            }
//                        }
//                    }
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
//                isCameraBound = true
//                android.util.Log.d("CameraActivity", "Camera started successfully")
//            } catch (e: Exception) {
//                android.util.Log.e("CameraActivity", "Camera start error", e)
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//}
//
//@Composable
//fun PermissionPrompt(onRequestPermission: () -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Camera permission is required to use this feature.",
//            fontSize = 18.sp,
//            color = Color.White,
//            style = TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
//        )
//        Button(onClick = onRequestPermission) {
//            Text("Grant Permission")
//        }
//    }
//}
//
//@Composable
//fun CameraPreview(previewView: PreviewView, results: List<DetectionResult>) {
//    val textMeasurer = rememberTextMeasurer()
//    val context = LocalContext.current
//    val previewWidth = context.resources.displayMetrics.widthPixels.toFloat()
//    val previewHeight = context.resources.displayMetrics.heightPixels.toFloat()
//    Box(modifier = Modifier.fillMaxSize()) {
//        AndroidView(
//            factory = { previewView },
//            modifier = Modifier.fillMaxSize(),
//            update = { view ->
//                view.scaleType = PreviewView.ScaleType.FILL_CENTER
//                view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//                android.util.Log.d("CameraActivity", "PreviewView updated")
//            }
//        )
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            results.forEach { result ->
//                // Scale bounding box coordinates (model outputs for 416x416)
//                val scaleX = previewWidth / 416f
//                val scaleY = previewHeight / 416f
//                val left = result.box.left * scaleX
//                val top = result.box.top * scaleY
//                val width = result.box.width() * scaleX
//                val height = result.box.height() * scaleY
//
//                drawRect(
//                    color = Color.Green,
//                    topLeft = Offset(left, top),
//                    size = androidx.compose.ui.geometry.Size(width, height),
//                    style = Stroke(width = 2f)
//                )
//                drawText(
//                    textMeasurer = textMeasurer,
//                    text = "${result.label} (${String.format("%.2f", result.score)})",
//                    style = TextStyle(fontSize = 12.sp, color = Color.White),
//                    topLeft = Offset(left, top - 10f)
//                )
//            }
//        }
//        Text(
//            text = "Tracking: all",
//            fontSize = 16.sp,
//            color = Color.White,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.align(Alignment.BottomCenter)
//        )
//        Text(
//            text = "Sensor: N/A",
//            fontSize = 14.sp,
//            color = Color.White,
//            modifier = Modifier.align(Alignment.TopEnd)
//        )
//    }
//}
//
//fun ImageProxy.toBitmap(): Bitmap {
//    try {
//        if (image == null) {
//            android.util.Log.e("CameraActivity", "ImageProxy has null image")
//            throw IllegalStateException("Null image in ImageProxy")
//        }
//        val yBuffer = planes[0].buffer
//        val uBuffer = planes[1].buffer
//        val vBuffer = planes[2].buffer
//
//        val ySize = yBuffer.remaining()
//        val uSize = uBuffer.remaining()
//        val vSize = vBuffer.remaining()
//
//        val nv21 = ByteArray(ySize + uSize + vSize)
//        yBuffer.get(nv21, 0, ySize)
//        vBuffer.get(nv21, ySize, vSize)
//        uBuffer.get(nv21, ySize + vSize, uSize)
//
//        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
//        val out = ByteArrayOutputStream()
//        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
//        val imageBytes = out.toByteArray()
//        out.close()
//        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//        if (bitmap == null) {
//            android.util.Log.e("CameraActivity", "Failed to decode bitmap")
//            throw IllegalStateException("Bitmap decoding failed")
//        }
//        return bitmap
//    } catch (e: Exception) {
//        android.util.Log.e("CameraActivity", "Error converting ImageProxy to Bitmap", e)
//        throw e
//    }
//}
//
//
//package com.example.mobiltelesco
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.os.Bundle
//import android.util.Size
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import java.util.concurrent.Executors
//import android.graphics.Bitmap
//import android.graphics.ImageFormat
//import android.graphics.YuvImage
//import java.io.ByteArrayOutputStream
//import androidx.camera.core.ImageProxy
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.ui.text.style.TextAlign
//
//class CameraActivity : ComponentActivity(), SensorEventListener {
//    private lateinit var nanoDet: NanoDetTFLite
//    private val permissionState = mutableStateOf(false)
//    private val detectionResults = mutableStateOf<List<DetectionResult>>(emptyList())
//    private lateinit var cameraProvider: ProcessCameraProvider
//    private lateinit var previewView: PreviewView
//    private var isCameraBound = false
//    private lateinit var sensorManager: SensorManager
//    private var accelerometerValues by mutableStateOf(floatArrayOf(0f, 0f, 0f))
//    private var gyroscopeValues by mutableStateOf(floatArrayOf(0f, 0f, 0f))
//    private var compassAngle by mutableStateOf(0f)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        nanoDet = NanoDetTFLite(this)
//        previewView = PreviewView(this).apply {
//            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//            scaleType = PreviewView.ScaleType.FILL_CENTER
//        }
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
//            permissionState.value = false
//        } else {
//            permissionState.value = true
//        }
//        setContent {
//            if (permissionState.value) {
//                CameraPreview(previewView, detectionResults.value, accelerometerValues, gyroscopeValues, compassAngle)
//            } else {
//                PermissionPrompt {
//                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
//                }
//            }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        sensorManager.registerListener(
//            this,
//            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//            SensorManager.SENSOR_DELAY_NORMAL
//        )
//        sensorManager.registerListener(
//            this,
//            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
//            SensorManager.SENSOR_DELAY_NORMAL
//        )
//        sensorManager.registerListener(
//            this,
//            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//            SensorManager.SENSOR_DELAY_NORMAL
//        )
//        if (permissionState.value && !isCameraBound) {
//            startCamera()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        sensorManager.unregisterListener(this)
//        if (::cameraProvider.isInitialized) {
//            cameraProvider.unbindAll()
//            isCameraBound = false
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            permissionState.value = true
//            startCamera()
//        }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            try {
//                cameraProvider = cameraProviderFuture.get()
//                val preview = Preview.Builder()
//                    .setTargetResolution(Size(1080, 1920))
//                    .build()
//                    .also {
//                        previewView.post {
//                            it.setSurfaceProvider(previewView.surfaceProvider)
//                        }
//                    }
//                val imageAnalysis = ImageAnalysis.Builder()
//                    .setTargetResolution(Size(416, 416))
//                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
//                    .build()
//                    .also {
//                        it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
//                            try {
//                                val bitmap = imageProxy.toBitmap()
//                                val results = nanoDet.detect(bitmap)
//                                detectionResults.value = results
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            } finally {
//                                imageProxy.close()
//                            }
//                        }
//                    }
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
//                isCameraBound = true
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        when (event?.sensor?.type) {
//            Sensor.TYPE_ACCELEROMETER -> accelerometerValues = event.values.clone()
//            Sensor.TYPE_GYROSCOPE -> gyroscopeValues = event.values.clone()
//            Sensor.TYPE_MAGNETIC_FIELD -> {
//                val gravity = accelerometerValues
//                val geomagnetic = event.values
//                val rotationMatrix = FloatArray(9)
//                val orientation = FloatArray(3)
//                SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
//                SensorManager.getOrientation(rotationMatrix, orientation)
//                compassAngle = Math.toDegrees(orientation[0].toDouble()).toFloat() // Azimuth (0° = North)
//            }
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        // Not used
//    }
//}
//
//@Composable
//fun PermissionPrompt(onRequestPermission: () -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Camera permission is required to use this feature.",
//            fontSize = 18.sp,
//            color = Color.White,
//            textAlign = TextAlign.Center
//        )
//        Button(onClick = onRequestPermission) {
//            Text("Grant Permission", color = Color.Black)
//        }
//    }
//}
//
//@Composable
//fun CameraPreview(
//    previewView: PreviewView,
//    results: List<DetectionResult>,
//    accelerometerValues: FloatArray,
//    gyroscopeValues: FloatArray,
//    compassAngle: Float
//) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        AndroidView(
//            factory = { previewView },
//            modifier = Modifier.fillMaxSize()
//        )
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            results.forEach { result ->
//                val scaleX = size.width / 416f
//                val scaleY = size.height / 416f
//                val left = result.box.left * scaleX
//                val top = result.box.top * scaleY
//                val width = result.box.width() * scaleX
//                val height = result.box.height() * scaleY
//
//                drawRect(
//                    color = Color.Green,
//                    topLeft = Offset(left, top),
//                    size = androidx.compose.ui.geometry.Size(width, height),
//                    style = Stroke(width = 2.dp.toPx())
//                )
//            }
//        }
//        Column(
//            modifier = Modifier
//                .align(Alignment.TopStart)
//                .padding(16.dp)
//        ) {
//            // Sensor Readings
//            Row {
//                Text(
//                    text = "Accel: X=${String.format("%.2f", accelerometerValues[0])} Y=${String.format("%.2f", accelerometerValues[1])} Z=${String.format("%.2f", accelerometerValues[2])} m/s²",
//                    style = TextStyle(color = Color.White, fontSize = 14.sp)
//                )
//            }
//            Row {
//                Text(
//                    text = "Gyro: X=${String.format("%.2f", gyroscopeValues[0])} Y=${String.format("%.2f", gyroscopeValues[1])} Z=${String.format("%.2f", gyroscopeValues[2])} rad/s",
//                    style = TextStyle(color = Color.White, fontSize = 14.sp)
//                )
//            }
//            Row {
//                Text(
//                    text = "Compass: ${String.format("%.1f", compassAngle)}°",
//                    style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                )
//            }
//        }
//    }
//}
//
//fun ImageProxy.toBitmap(): Bitmap {
//    val yBuffer = planes[0].buffer
//    val uBuffer = planes[1].buffer
//    val vBuffer = planes[2].buffer
//
//    val ySize = yBuffer.remaining()
//    val uSize = uBuffer.remaining()
//    val vSize = vBuffer.remaining()
//
//    val nv21 = ByteArray(ySize + uSize + vSize)
//    yBuffer.get(nv21, 0, ySize)
//    vBuffer.get(nv21, ySize, vSize)
//    uBuffer.get(nv21, ySize + vSize, uSize)
//
//    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
//    val out = ByteArrayOutputStream()
//    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
//    val imageBytes = out.toByteArray()
//    out.close()
//    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//}











/////////////////////////////////////////////////////////////////////////
package com.example.mobiltelesco

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextAlign
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraActivity : ComponentActivity(), SensorEventListener {
    private lateinit var nanoDet: NanoDetTFLite
    private val permissionState = mutableStateOf(false)
    private val detectionResults = mutableStateOf<List<DetectionResult>>(emptyList())
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var previewView: PreviewView
    private var isCameraBound = false
    private lateinit var sensorManager: SensorManager
    private var accelerometerValues by mutableStateOf(floatArrayOf(0f, 0f, 0f))
    private var gyroscopeValues by mutableStateOf(floatArrayOf(0f, 0f, 0f))
    private var compassAngle by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nanoDet = NanoDetTFLite(this)
        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            permissionState.value = false
        } else {
            permissionState.value = true
        }
        setContent {
            if (permissionState.value) {
                CameraPreview(previewView, detectionResults.value, accelerometerValues, gyroscopeValues, compassAngle)
            } else {
                PermissionPrompt {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        if (permissionState.value && !isCameraBound) {
            startCamera()
        }
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
            isCameraBound = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionState.value = true
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .setTargetResolution(Size(1080, 1920))
                    .build()
                    .also {
                        previewView.post {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(416, 416))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also {
                        it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            try {
                                val bitmap = imageProxy.toBitmap()
                                val results = nanoDet.detect(bitmap)
                                detectionResults.value = results
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                imageProxy.close()
                            }
                        }
                    }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                isCameraBound = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerValues = event.values.clone()
            Sensor.TYPE_GYROSCOPE -> gyroscopeValues = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val gravity = accelerometerValues
                val geomagnetic = event.values
                val rotationMatrix = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                SensorManager.getOrientation(rotationMatrix, orientation)
                compassAngle = Math.toDegrees(orientation[0].toDouble()).toFloat() // Azimuth
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}

@Composable
fun PermissionPrompt(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera permission is required to use this feature.",
            fontSize = 18.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRequestPermission) {
            Text("Grant Permission", color = Color.Black)
        }
    }
}

@Composable
fun CameraPreview(
    previewView: PreviewView,
    results: List<DetectionResult>,
    accelerometerValues: FloatArray,
    gyroscopeValues: FloatArray,
    compassAngle: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            results.forEach { result ->
                val scaleX = size.width / 416f
                val scaleY = size.height / 416f
                val left = result.box.left * scaleX
                val top = result.box.top * scaleY
                val width = result.box.width() * scaleX
                val height = result.box.height() * scaleY

                drawRect(
                    color = Color.Green,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(width, height),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        // Sensor Readings at Top Left
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Row {
                Text(
                    text = "Accel: X=${String.format("%.2f", accelerometerValues[0])} Y=${String.format("%.2f", accelerometerValues[1])} Z=${String.format("%.2f", accelerometerValues[2])} m/s²",
                    style = TextStyle(color = Color.White, fontSize = 14.sp)
                )
            }
            Row {
                Text(
                    text = "Gyro: X=${String.format("%.2f", gyroscopeValues[0])} Y=${String.format("%.2f", gyroscopeValues[1])} Z=${String.format("%.2f", gyroscopeValues[2])} rad/s",
                    style = TextStyle(color = Color.White, fontSize = 14.sp)
                )
            }
            Row {
                Text(
                    text = "Compass: ${String.format("%.1f", compassAngle)}°",
                    style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
        // Control Buttons at Top Right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Button(onClick = { sendCommandToPi("UP") }) {
                Text("Up", color = Color.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = { sendCommandToPi("LEFT") }) {
                    Text("Left", color = Color.Black, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { sendCommandToPi("RIGHT") }) {
                    Text("Right", color = Color.Black, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { sendCommandToPi("DOWN") }) {
                Text("Down", color = Color.Black, fontSize = 16.sp)
            }
        }
    }
}

fun sendCommandToPi(command: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("http://192.168.1.9:5000/test")
            val connection = withContext(Dispatchers.IO) {
                url.openConnection() as HttpURLConnection
            }
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val json = "{\"command\": \"$command\"}"
            connection.outputStream.write(json.toByteArray())
            connection.connect()
            val responseCode = connection.responseCode
            withContext(Dispatchers.Main) {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Command $command sent successfully")
                } else {
                    println("Failed to send command $command, response: $responseCode")
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
    val imageBytes = out.toByteArray()
    out.close()
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}










