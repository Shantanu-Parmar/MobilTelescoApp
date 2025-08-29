package com.example.mobiltelesco

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import org.tensorflow.lite.support.common.ops.NormalizeOp

data class DetectionResult(val box: RectF, val label: String, val score: Float)

class NanoDetTFLite(context: Context) {
    private val interpreter: Interpreter
    private val labels: List<String>
    private val inputSize = 416
    private val numDetections = 3598
    private val outputSize = 40 // 4 (box) + 1 (score) + 35 (classes)

    init {
        try {
            val model = FileUtil.loadMappedFile(context, "nanodet.tflite")
            interpreter = Interpreter(model)
            labels = FileUtil.loadLabels(context, "labels.txt")
            if (labels.size != 35) {
                android.util.Log.e("NanoDetTFLite", "labels.txt has ${labels.size} labels, expected 35")
            }
            android.util.Log.d("NanoDetTFLite", "Model and labels loaded successfully")
        } catch (e: Exception) {
            android.util.Log.e("NanoDetTFLite", "Error loading model or labels", e)
            throw e
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        try {
            // Ensure RGB input
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputSize, inputSize, ResizeMethod.NEAREST_NEIGHBOR))
                .add(NormalizeOp(0f, 255f)) // Normalize to [0,1] if model expects it
                .build()
            val processedImage = imageProcessor.process(tensorImage)

            // Verify input tensor size
            val inputBuffer = processedImage.buffer
            android.util.Log.d("NanoDetTFLite", "Input buffer size: ${inputBuffer.capacity()} bytes")
            if (inputBuffer.capacity() != 416 * 416 * 3 * 4) {
                android.util.Log.e("NanoDetTFLite", "Expected input size: ${416 * 416 * 3 * 4}, got: ${inputBuffer.capacity()}")
            }

            val outputBuffer = ByteBuffer.allocateDirect(numDetections * outputSize * 4)
                .order(ByteOrder.nativeOrder())

            interpreter.run(inputBuffer, outputBuffer)
            return parseOutput(outputBuffer)
        } catch (e: Exception) {
            android.util.Log.e("NanoDetTFLite", "Inference error", e)
            return emptyList()
        }
    }

    private fun parseOutput(buffer: ByteBuffer): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        buffer.rewind()
        val outputArray = FloatArray(numDetections * outputSize)
        buffer.asFloatBuffer().get(outputArray)

        for (i in 0 until numDetections) {
            val score = outputArray[i * outputSize + 4]
            if (score > 0.5f) {
                val xMin = outputArray[i * outputSize]
                val yMin = outputArray[i * outputSize + 1]
                val xMax = outputArray[i * outputSize + 2]
                val yMax = outputArray[i * outputSize + 3]
                val box = RectF(xMin, yMin, xMax, yMax)

                var maxClassScore = 0f
                var classIdx = -1
                for (j in 0 until outputSize - 5) {
                    val classScore = outputArray[i * outputSize + 5 + j]
                    if (classScore > maxClassScore) {
                        maxClassScore = classScore
                        classIdx = j
                    }
                }

                if (classIdx >= 0 && classIdx < labels.size) {
                    results.add(DetectionResult(box, labels[classIdx], score))
                }
            }
        }
        android.util.Log.d("NanoDetTFLite", "Detected ${results.size} objects")
        return results
    }
}