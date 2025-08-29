package com.example.mobiltelesco

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.mobiltelesco.ui.theme.MobilTelescoTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import kotlin.random.Random

class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobilTelescoTheme {
                IntroScreen {
                    startActivity(Intent(this, LoadingActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun IntroScreen(onGetStartedClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Canvas for static night sky splatters
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw multiple splatters with Van Gogh-like texture
            repeat(15) {
                val x = Random.nextFloat() * width
                val y = Random.nextFloat() * height
                val size = Random.nextFloat() * 50f + 20f // Random size between 20 and 70
                val color = when (Random.nextInt(4)) {
                    0 -> Color(0xFF1E90FF) // Dodger Blue
                    1 -> Color.White
                    2 -> Color(0xFFFFFF99) // Light Yellow
                    else -> Color(0xFF4682B4) // Steel Blue
                }

                // Draw irregular circle with stroke for texture
                drawCircle(
                    color = color,
                    radius = size / 2,
                    center = Offset(x, y),
                    style = Stroke(width = Random.nextFloat() * 5f + 2f)
                )

                // Add smaller swirls or dots around it
                repeat(3) {
                    val swirlX = x + Random.nextFloat() * 30f - 15f
                    val swirlY = y + Random.nextFloat() * 30f - 15f
                    val swirlSize = Random.nextFloat() * 15f + 5f
                    drawCircle(
                        color = color.copy(alpha = 0.7f),
                        radius = swirlSize / 2,
                        center = Offset(swirlX, swirlY),
                        style = Stroke(width = Random.nextFloat() * 3f + 1f)
                    )
                }
            }
        }

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black), // Ensure black background over splatters
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MobilTelesco",
                fontSize = 32.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Capture and track celestial wonders.",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onGetStartedClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Get Started", color = Color.Black, fontSize = 16.sp)
            }
        }
    }
}










//
//package com.example.mobiltelesco
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.AnimationVector1D
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.draw.scale
//import kotlinx.coroutines.delay
//import kotlin.random.Random
//
//private data class Star(
//    val x: Animatable<Float, AnimationVector1D>,
//    val y: Animatable<Float, AnimationVector1D>,
//    val speed: Float,       // Small speed factor
//    val direction: Float    // Controls x/y split
//)
//
//private data class Planet(
//    val x: Animatable<Float, AnimationVector1D>,
//    val y: Animatable<Float, AnimationVector1D>
//)
//
//class IntroActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            AstronomyLandingPage {
//                startActivity(Intent(this, CameraActivity::class.java))
//                finish()
//            }
//        }
//    }
//}
//
//@Composable
//fun AstronomyLandingPage(onStart: () -> Unit) {
//    // Store normalized positions (0..1) to multiply by canvas size in draw
//    val stars = remember {
//        List(50) {
//            Star(
//                x = Animatable(Random.nextFloat()),
//                y = Animatable(Random.nextFloat()),
//                speed = Random.nextFloat() * 0.5f + 0.05f,
//                direction = Random.nextFloat()
//            )
//        }
//    }
//
//    val planets = remember {
//        List(3) {
//            Planet(
//                x = Animatable(Random.nextFloat()),
//                y = Animatable(Random.nextFloat())
//            )
//        }
//    }
//
//    var logoScale by remember { mutableStateOf(1f) }
//
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val w = size.width
//            val h = size.height
//
//            // Background
//            drawRect(color = Color.Black, size = size)
//
//            // Draw stars (small)
//            stars.forEach { star ->
//                val cx = star.x.value * w
//                val cy = star.y.value * h
//                drawCircle(color = Color.White, radius = 2f, center = Offset(cx, cy))
//            }
//
//            // Draw planets (bigger)
//            planets.forEach { planet ->
//                val cx = planet.x.value * w
//                val cy = planet.y.value * h
//                drawCircle(color = Color(0xFF6B4E31), radius = 12f, center = Offset(cx, cy))
//            }
//        }
//
//        // Logo
//        Image(
//            painter = painterResource(id = android.R.drawable.sym_def_app_icon),
//            contentDescription = "App Logo",
//            modifier = Modifier
//                .align(Alignment.TopCenter)
//                .scale(logoScale)
//        )
//
//        // Logo pulsing loop
//        LaunchedEffect(Unit) {
//            while (true) {
//                logoScale = 1.12f
//                delay(500)
//                logoScale = 1f
//                delay(500)
//            }
//        }
//
//        // Start button
//        Button(onClick = onStart, modifier = Modifier.align(Alignment.BottomCenter)) {
//            Text("Start Exploring", fontSize = 20.sp, color = Color.White)
//        }
//    }
//
//    // Animate planets: slowly move to new random positions
//    planets.forEachIndexed { index, planet ->
//        LaunchedEffect(planet) {
//            while (true) {
//                val newX = Random.nextFloat()
//                val newY = Random.nextFloat()
//                val duration = 5000 + index * 1000
//                planet.x.animateTo(newX, animationSpec = tween(durationMillis = duration))
//                planet.y.animateTo(newY, animationSpec = tween(durationMillis = duration))
//                delay(300) // Small pause before next target
//            }
//        }
//    }
//
//    // Animate stars: small incremental moves that wrap around (using normalized coords)
//    stars.forEach { star ->
//        LaunchedEffect(star) {
//            while (true) {
//                val step = star.speed * 0.02f
//                var newX = (star.x.value + step * star.direction)
//                var newY = (star.y.value + step * (1f - star.direction))
//
//                // Wrap around normalized coordinates
//                if (newX > 1f) newX -= 1f
//                else if (newX < 0f) newX += 1f
//                if (newY > 1f) newY -= 1f
//                else if (newY < 0f) newY += 1f
//
//                // Animate each coordinate
//                star.x.animateTo(newX, animationSpec = tween(durationMillis = 800))
//                star.y.animateTo(newY, animationSpec = tween(durationMillis = 800))
//
//                // Small delay to pace the movement
//                delay(200)
//            }
//        }
//    }
//}