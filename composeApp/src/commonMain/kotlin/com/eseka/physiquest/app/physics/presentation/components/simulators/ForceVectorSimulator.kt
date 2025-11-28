@file:OptIn(ExperimentalAnimationApi::class, ExperimentalTextApi::class)

package com.eseka.physiquest.app.physics.presentation.components.simulators

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.physics.domain.models.ForceVector
import com.eseka.physiquest.app.physics.domain.models.Point
import com.eseka.physiquest.app.physics.presentation.utils.formatDecimal
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * A composable for visualizing a vector field of forces. It animates the vectors
 * growing from their base positions and allows for interactive exploration after completion.
 *
 * @param forceVectors A list of force vectors, each with a position, direction, and magnitude.
 * @param modifier The modifier to be applied to the component.
 * @param title A title for the simulation card.
 * @param xLabel A label for the horizontal axis.
 * @param yLabel A label for the vertical axis.
 * @param isAnimating A boolean flag to control the entry animation. Setting to true plays the animation.
 * @param animationDuration The duration of the entry animation in milliseconds.
 * @param onAnimationComplete A callback lambda invoked when the animation finishes.
 * @param showGrid Toggles the visibility of the background grid lines.
 * @param showAxes Toggles the visibility of the X and Y axes.
 * @param vectorColorGradient A list of colors to create a gradient based on vector magnitude.
 * @param enableSlider Toggles the user's ability to drag on the graph to inspect vectors.
 */
@Composable
fun ForceVectorSimulator(
    forceVectors: List<ForceVector>,
    modifier: Modifier = Modifier,
    title: String = "Force Vector Field",
    xLabel: String = "X-Position (m)",
    yLabel: String = "Y-Position (m)",
    isAnimating: Boolean = false,
    animationDuration: Int = 1500,
    onAnimationComplete: () -> Unit = {},
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    vectorColorGradient: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    ),
    enableSlider: Boolean = true
) {
    var manualSelectedIndex by remember { mutableIntStateOf(-1) }
    var isManuallySelecting by remember { mutableStateOf(false) }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration)
            )
            onAnimationComplete()
        }
    }


    // Reset interaction state when the vector data changes
    LaunchedEffect(forceVectors) {
        manualSelectedIndex = -1
        isManuallySelecting = false
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            ) {
                if (forceVectors.isNotEmpty()) {
                    ForceVectorCanvas(
                        forceVectors = forceVectors,
                        animationProgress = animationProgress.value,
                        manualSelectedIndex = manualSelectedIndex,
                        xLabel = xLabel,
                        yLabel = yLabel,
                        showGrid = showGrid,
                        showAxes = showAxes,
                        vectorColorGradient = vectorColorGradient,
                        enableSlider = enableSlider && animationProgress.value == 1f, // Enable only when complete
                        onVectorSelected = { index ->
                            if (enableSlider && animationProgress.value == 1f) {
                                isManuallySelecting = true
                                manualSelectedIndex = index
                            }
                        },
                        onSelectionEnd = { isManuallySelecting = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No force vector data to display",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ForceVectorInfoFooter(
                forceVectors = forceVectors,
                isManuallySelecting = isManuallySelecting,
                manualSelectedIndex = manualSelectedIndex,
                animationProgress = animationProgress.value
            )
        }
    }
}

@Composable
private fun ForceVectorInfoFooter(
    forceVectors: List<ForceVector>,
    isManuallySelecting: Boolean,
    manualSelectedIndex: Int,
    animationProgress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Vectors: ${forceVectors.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            val infoText: Pair<String, Color> = when {
                animationProgress > 0f && animationProgress < 1.0f -> {
                    "Animating..." to MaterialTheme.colorScheme.primary
                }

                isManuallySelecting && manualSelectedIndex in forceVectors.indices -> {
                    val vector = forceVectors[manualSelectedIndex]
                    "Magnitude: ${vector.magnitude.formatForGraph()} N/C" to MaterialTheme.colorScheme.secondary
                }

                animationProgress == 1f -> { // Show only when complete
                    "Drag on graph to explore vectors" to MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )
                }

                else -> {
                    "" to Color.Transparent
                }
            }
            Text(
                text = infoText.first,
                style = MaterialTheme.typography.bodySmall,
                color = infoText.second,
                minLines = 2
            )
        }

        val status: Pair<String, Color>? = when {
            animationProgress > 0f && animationProgress < 1.0f -> "Animating" to MaterialTheme.colorScheme.primary
            animationProgress == 1.0f && !isManuallySelecting -> "Complete" to MaterialTheme.colorScheme.tertiary
            isManuallySelecting -> "Exploring" to MaterialTheme.colorScheme.secondary
            else -> null
        }

        status?.let { (text, color) ->
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}


@OptIn(ExperimentalTextApi::class)
@Composable
private fun ForceVectorCanvas(
    forceVectors: List<ForceVector>,
    animationProgress: Float,
    manualSelectedIndex: Int,
    xLabel: String,
    yLabel: String,
    showGrid: Boolean,
    showAxes: Boolean,
    vectorColorGradient: List<Color>,
    enableSlider: Boolean,
    onVectorSelected: (Int) -> Unit,
    onSelectionEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val highlightColor = MaterialTheme.colorScheme.secondary
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = onSurfaceColor.copy(alpha = 0.6f),
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        textAlign = TextAlign.Center
    )

    var mappedPositions by remember { mutableStateOf(emptyList<Offset>()) }

    Canvas(
        modifier = modifier.then(
            if (enableSlider) {
                Modifier.pointerInput(forceVectors) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            findNearestVectorIndex(offset, mappedPositions, 40f)?.let(
                                onVectorSelected
                            )
                        },
                        onDrag = { change, _ ->
                            findNearestVectorIndex(change.position, mappedPositions, 60f)?.let(
                                onVectorSelected
                            )
                        },
                        onDragEnd = { onSelectionEnd() },
                        onDragCancel = { onSelectionEnd() }
                    )
                }
            } else Modifier
        )
    ) {
        if (forceVectors.isEmpty()) return@Canvas

        val allPoints = forceVectors.map { it.position } + Point(0.0, 0.0)
        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }

        // 👇 IMPROVED: Robust, percentage-based padding for bounds
        val xRange = (maxX - minX).takeIf { it > 0.0 } ?: 1.0
        val yRange = (maxY - minY).takeIf { it > 0.0 } ?: 1.0
        val xPadding = max(xRange * 0.1, 0.1)
        val yPadding = max(yRange * 0.1, 0.1)
        val adjMinX = minX - xPadding
        val adjMaxX = maxX + xPadding
        val adjMinY = minY - yPadding
        val adjMaxY = maxY + yPadding

        val yAxisLabelWidth = textMeasurer.measure(
            text = adjMaxY.formatForGraph(),
            style = textStyle
        ).size.width.toFloat()
        val xAxisLabelHeight =
            textMeasurer.measure(text = "0", style = textStyle).size.height.toFloat()
        val xLabelHeight =
            textMeasurer.measure(text = xLabel, style = textStyle).size.height.toFloat()
        val yLabelHeight =
            textMeasurer.measure(text = yLabel, style = textStyle).size.height.toFloat()

        val paddingLeft = yAxisLabelWidth + 8.dp.toPx()
        val paddingBottom = xAxisLabelHeight + xLabelHeight + 12.dp.toPx()
        val paddingTop = yLabelHeight + 8.dp.toPx()
        val paddingRight = 16.dp.toPx()

        val plotWidth = size.width - paddingLeft - paddingRight
        val plotHeight = size.height - paddingTop - paddingBottom

        if (plotWidth <= 0 || plotHeight <= 0) return@Canvas

        fun mapX(x: Double): Float =
            (paddingLeft + ((x - adjMinX) / (adjMaxX - adjMinX)) * plotWidth).toFloat()

        fun mapY(y: Double): Float =
            (size.height - paddingBottom - ((y - adjMinY) / (adjMaxY - adjMinY)) * plotHeight).toFloat()

        mappedPositions = forceVectors.map { Offset(mapX(it.position.x), mapY(it.position.y)) }

        val plotBounds = VectorPlotBounds(
            paddingLeft,
            paddingTop,
            size.width - paddingRight,
            size.height - paddingBottom
        )

        if (showGrid) {
            drawGrid(
                plotBounds,
                adjMinX,
                adjMaxX,
                adjMinY,
                adjMaxY,
                ::mapX,
                ::mapY,
                outlineColor.copy(alpha = 0.2f),
                textMeasurer,
                textStyle
            )
        }
        if (showAxes) {
            drawAxes(
                plotBounds,
                xLabel,
                yLabel,
                onSurfaceColor.copy(alpha = 0.8f),
                textMeasurer,
                textStyle
            )
        }

        drawCircle(onSurfaceColor, 5.dp.toPx(), Offset(mapX(0.0), mapY(0.0)))

        drawForceVectors(
            forceVectors = forceVectors,
            mappedPositions = mappedPositions,
            animationProgress = animationProgress,
            vectorColorGradient = vectorColorGradient,
            highlightColor = highlightColor,
            manualSelectedIndex = manualSelectedIndex
        )
    }
}

private fun findNearestVectorIndex(
    touchOffset: Offset,
    positions: List<Offset>,
    threshold: Float
): Int? {
    return positions
        .asSequence()
        .mapIndexed { index, pos -> index to (touchOffset - pos).getDistance() }
        .filter { it.second <= threshold }
        .minByOrNull { it.second }
        ?.first
}

// A simple lerp function for Floats
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

private fun DrawScope.drawForceVectors(
    forceVectors: List<ForceVector>,
    mappedPositions: List<Offset>,
    animationProgress: Float,
    vectorColorGradient: List<Color>,
    highlightColor: Color,
    manualSelectedIndex: Int
) {
    if (animationProgress == 0f) return

    val maxMagnitude = forceVectors.maxOfOrNull { it.magnitude } ?: 1.0
    val minMagnitude = forceVectors.minOfOrNull { it.magnitude } ?: 0.0
    val magnitudeRange = (maxMagnitude - minMagnitude).takeIf { it > 0 } ?: 1.0

    val maxArrowLength = 40.dp.toPx()

    forceVectors.forEachIndexed { index, vector ->
        val start = mappedPositions[index]
        val direction = Offset(vector.forceDirection.x.toFloat(), vector.forceDirection.y.toFloat())

        val length = lerp(
            start = 10.dp.toPx(), // Minimum length for visibility
            stop = maxArrowLength,
            fraction = ((vector.magnitude - minMagnitude) / magnitudeRange).toFloat()
        )
        val end = start + direction * length * animationProgress

        val magnitudeRatio = ((vector.magnitude - minMagnitude) / magnitudeRange).toFloat()
        val color = getColorFromGradient(magnitudeRatio, vectorColorGradient)
        val animatedColor = color.copy(alpha = color.alpha * animationProgress)

        val strokeWidth = if (index == manualSelectedIndex) 4.dp.toPx() else 2.dp.toPx()
        val finalColor = if (index == manualSelectedIndex) highlightColor else animatedColor

        drawLine(
            color = finalColor,
            start = start,
            end = end,
            strokeWidth = strokeWidth
        )
        drawArrowhead(start, end, finalColor)
    }
}

private fun DrawScope.drawArrowhead(start: Offset, end: Offset, color: Color) {
    val vector = end - start
    if (vector.getDistanceSquared() < 1e-6f) return

    val normal = vector.getNormalized()
    val arrowSize = 8.dp.toPx()

    val p1 = end - normal * arrowSize + Offset(normal.y, -normal.x) * arrowSize * 0.5f
    val p2 = end - normal * arrowSize - Offset(normal.y, -normal.x) * arrowSize * 0.5f

    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path, color)
}

// Helper to normalize an Offset, avoiding division by zero
private fun Offset.getNormalized(): Offset {
    val distance = getDistance()
    return if (distance == 0f) Offset.Zero else this / distance
}


private fun getColorFromGradient(ratio: Float, colors: List<Color>): Color {
    if (colors.isEmpty()) return Color.Black
    if (colors.size == 1) return colors.first()
    val index = (ratio * (colors.size - 1)).coerceIn(0f, (colors.size - 2).toFloat())
    val lowerIndex = floor(index).toInt()
    val upperIndex = ceil(index).toInt()
    val t = index - lowerIndex

    return lerp(colors[lowerIndex], colors[upperIndex], t)
}

private data class VectorPlotBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawGrid(
    bounds: VectorPlotBounds, minX: Double, maxX: Double, minY: Double, maxY: Double,
    mapX: (Double) -> Float, mapY: (Double) -> Float,
    color: Color, textMeasurer: TextMeasurer, textStyle: TextStyle
) {
    val yGridLines = 5
    val yStep = (maxY - minY) / yGridLines
    val xGridLines = 5
    val xStep = (maxX - minX) / xGridLines

    for (i in 0..xGridLines) {
        val xValue = minX + i * xStep
        val xPos = mapX(xValue)
        drawLine(color, Offset(xPos, bounds.top), Offset(xPos, bounds.bottom), 1.dp.toPx())
        val label = xValue.formatForGraph()
        val measured = textMeasurer.measure(text = label, style = textStyle)
        drawText(
            measured,
            topLeft = Offset(xPos - (measured.size.width / 2), bounds.bottom + 4.dp.toPx())
        )
    }

    for (i in 0..yGridLines) {
        val yValue = minY + i * yStep
        val yPos = mapY(yValue)
        drawLine(color, Offset(bounds.left, yPos), Offset(bounds.right, yPos), 1.dp.toPx())
        val label = yValue.formatForGraph()
        val measured =
            textMeasurer.measure(text = label, style = textStyle.copy(textAlign = TextAlign.End))
        drawText(
            measured,
            topLeft = Offset(
                bounds.left - measured.size.width - 4.dp.toPx(),
                yPos - (measured.size.height / 2)
            )
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawAxes(
    bounds: VectorPlotBounds, xLabel: String, yLabel: String,
    color: Color, textMeasurer: TextMeasurer, textStyle: TextStyle
) {
    val axisStroke = 2.dp.toPx()
    drawLine(
        color,
        Offset(bounds.left, bounds.bottom),
        Offset(bounds.right, bounds.bottom),
        axisStroke
    )
    drawLine(color, Offset(bounds.left, bounds.top), Offset(bounds.left, bounds.bottom), axisStroke)

    val xLabelMeasured = textMeasurer.measure(text = xLabel, style = textStyle.copy(color = color))
    drawText(
        xLabelMeasured,
        topLeft = Offset(
            bounds.left + (bounds.right - bounds.left) / 2 - xLabelMeasured.size.width / 2,
            bounds.bottom + 16.dp.toPx()
        )
    )

    val yLabelMeasured = textMeasurer.measure(text = yLabel, style = textStyle.copy(color = color))
    drawText(
        yLabelMeasured,
        topLeft = Offset(
            bounds.left - yLabelMeasured.size.width / 2,
            bounds.top - yLabelMeasured.size.height - 4.dp.toPx()
        )
    )
}

private fun Double.formatForGraph(): String {
    if (this == 0.0) return "0"
    val digits = when {
        abs(this) >= 100 -> 0
        abs(this) >= 10 -> 1
        else -> 2
    }
    return this.formatDecimal(digits)
}