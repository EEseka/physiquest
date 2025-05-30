package com.eseka.physiquest.authentication.presentation.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.app_name
import physiquest.composeapp.generated.resources.physi_quest

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Animations for alpha (fade-in), scale (zoom-in), and exit alpha (fade-out)
    val alphaAnimation = remember { Animatable(0f) }
    val scaleAnimation = remember { Animatable(0.5f) }
    val exitAlpha = remember { Animatable(1f) }

    // Physics-inspired oscillation animation (pendulum-like effect)
    val infiniteTransition = rememberInfiniteTransition(label = "physics_animations")

    // Gentle oscillation (like a pendulum)
    val oscillation = infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "oscillation"
    )

    // Energy wave pulse effect
    val waveScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_pulse"
    )

    // Launching animations for fade-in and zoom-in
    LaunchedEffect(Unit) {
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Launching fade-out animation and triggering the next screen
    LaunchedEffect(Unit) {
        delay(2500) // Slightly longer to appreciate the physics animations
        exitAlpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 500))
        onSplashFinished() // Trigger the next screen when animation ends
    }

    // Main container for the splash screen content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceDim,
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            )
            .alpha(exitAlpha.value), // Applying fade-out effect
        contentAlignment = Alignment.Center
    ) {
        // Animated wave effect (physics energy ripple)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f * alphaAnimation.value),
            contentAlignment = Alignment.Center
        ) {
            // Multiple ripple waves
            repeat(3) { index ->
                val waveAlpha = infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 2000,
                            delayMillis = index * 650,
                            easing = LinearOutSlowInEasing
                        )
                    ),
                    label = "wave_alpha_$index"
                )

                val waveSize = infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 2000,
                            delayMillis = index * 650,
                            easing = LinearOutSlowInEasing
                        )
                    ),
                    label = "wave_size_$index"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize(waveSize.value * 0.3f)
                        .alpha(waveAlpha.value)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Physics-style animated logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            ),
                            radius = 200f
                        ),
                        shape = CircleShape
                    )
                    .wrapContentSize()
            ) {
                Image(
                    painter = painterResource(Res.drawable.physi_quest),
                    contentDescription = stringResource(Res.string.app_name),
                    modifier = Modifier
                        .alpha(alphaAnimation.value) // Fade-in effect
                        .scale(scaleAnimation.value * waveScale.value) // Zoom-in with pulsing
                        .rotate(oscillation.value) // Pendulum-like gentle oscillation
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated app name text
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(alphaAnimation.value) // Fade-in effect
                    .scale(scaleAnimation.value) // Zoom-in effect
            )
        }
    }
}