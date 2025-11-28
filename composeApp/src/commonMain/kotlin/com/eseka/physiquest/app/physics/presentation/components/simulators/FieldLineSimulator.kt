@file:OptIn(ExperimentalAnimationApi::class, ExperimentalTextApi::class)

package com.eseka.physiquest.app.physics.presentation.components.simulators

import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.physics.domain.models.FieldLine
import com.eseka.physiquest.app.physics.presentation.utils.formatDecimal
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * A sophisticated and interactive KMP composable for visualizing electric or magnetic field lines.
 * It features color-coded field strength, optional direction arrows, drag-to-inspect
 * functionality, and a clean, informative UI.
 *
 * @param fieldLines The list of [FieldLine] data to be plotted.
 * @param modifier The modifier to be applied to the component.
 * @param title A title for the simulation card.
 * @param xLabel A label for the horizontal axis.
 * @param yLabel A label for the vertical axis.
 * @param showGrid Toggles the visibility of the background grid lines.
 * @param showAxes Toggles the visibility of the X and Y axes.
 * @param showDirection Toggles drawing of arrowheads to indicate field line direction.
 * @param lineColorGradient A list of colors to use for the field strength gradient.
 * The first color corresponds to the minimum strength, the last to the maximum.
 * @param enableSlider Toggles the user's ability to drag on the graph to inspect lines.
 */
@Composable
fun FieldLineSimulator(
    fieldLines: List<FieldLine>,
    modifier: Modifier = Modifier,
    title: String = "Field Line Simulation",
    xLabel: String = "X-Position (m)",
    yLabel: String = "Y-Position (m)",
    isAnimating: Boolean = false,
    animationDuration: Int = 1500,
    onAnimationComplete: () -> Unit = {},
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    showDirection: Boolean = true,
    lineColorGradient: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    ),
    enableSlider: Boolean = true,
) {
    var manualSelectedIndex by remember { mutableIntStateOf(-1) }
    var isManuallySelecting by remember { mutableStateOf(false) }

    var animationProgress by remember { mutableFloatStateOf(0f) }
    var hasAnimationRun by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isAnimating) {
        if (isAnimating) {
            // If starting animation, reset progress if it was already complete
            if (hasAnimationRun) {
                animationProgress = 0f
            }
            hasAnimationRun = true
            val startTime = withFrameNanos { it }

            // Animate until progress reaches 1f
            while (animationProgress < 1f) {
                val playTime = withFrameNanos { it } - startTime
                val newProgress = (playTime.toFloat() / 1_000_000f) / animationDuration.toFloat()
                animationProgress = newProgress.coerceIn(0f, 1f)

                // This ensures the animation loop doesn't block the UI thread
                delay(1)
            }

            // Once the loop is complete, signal completion
            onAnimationComplete()
        }
    }

    // Reset selection state if the data changes
    LaunchedEffect(fieldLines) {
        manualSelectedIndex = -1
        isManuallySelecting = false
        animationProgress = 0f
        hasAnimationRun = false
    }

    // Card and outer layout remains the same...
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
                if (fieldLines.isNotEmpty()) {
                    FieldLineCanvas(
                        fieldLines = fieldLines,
                        animationProgress = if (hasAnimationRun) animationProgress else 0f,
                        manualSelectedIndex = manualSelectedIndex,
                        xLabel = xLabel,
                        yLabel = yLabel,
                        showGrid = showGrid,
                        showAxes = showAxes,
                        showDirection = showDirection,
                        lineColorGradient = lineColorGradient,
                        enableSlider = enableSlider && !isAnimating,
                        onLineSelected = { index ->
                            if (enableSlider && !isAnimating) {
                                isManuallySelecting = true
                                manualSelectedIndex = index
                            }
                        },
                        onSelectionEnd = {
                            isManuallySelecting = false
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No field line data to display",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FieldLineInfoFooter(
                fieldLines = fieldLines,
                isManuallySelecting = isManuallySelecting,
                manualSelectedIndex = manualSelectedIndex,
                isAnimating = isAnimating,
                animationProgress = animationProgress
            )
        }
    }
}

@Composable
private fun FieldLineInfoFooter(
    fieldLines: List<FieldLine>,
    isManuallySelecting: Boolean,
    manualSelectedIndex: Int,
    isAnimating: Boolean,
    animationProgress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Field Lines: ${fieldLines.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            val infoText: Pair<String, Color> = when {
                isAnimating && animationProgress < 1.0f -> {
                    "Drawing field... (${(animationProgress * 100).toInt()}%)" to MaterialTheme.colorScheme.primary
                }

                isManuallySelecting && manualSelectedIndex in fieldLines.indices -> {
                    val line = fieldLines[manualSelectedIndex]
                    "Strength: ${line.fieldStrength.formatForGraph()}" to MaterialTheme.colorScheme.secondary
                }

                else -> {
                    "Drag on graph to explore lines" to MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )
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
            isAnimating && animationProgress < 1.0f -> "Animating" to MaterialTheme.colorScheme.primary
            animationProgress == 1.0f && isAnimating -> "Complete" to MaterialTheme.colorScheme.tertiary
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
private fun FieldLineCanvas(
    fieldLines: List<FieldLine>,
    animationProgress: Float,
    manualSelectedIndex: Int,
    xLabel: String,
    yLabel: String,
    showGrid: Boolean,
    showAxes: Boolean,
    showDirection: Boolean,
    lineColorGradient: List<Color>,
    enableSlider: Boolean,
    onLineSelected: (Int) -> Unit,
    onSelectionEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = onSurfaceColor.copy(alpha = 0.6f),
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        textAlign = TextAlign.Center
    )

    var mappedLines by remember { mutableStateOf(emptyList<Pair<Offset, Offset>>()) }
    var lastSelectedIndex by remember { mutableIntStateOf(-1) }

    Canvas(
        modifier = modifier
            .then(
                if (enableSlider) {
                    Modifier.pointerInput(fieldLines) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                findNearestLineIndex(offset, mappedLines, threshold = 30f)?.let {
                                    lastSelectedIndex = it
                                    onLineSelected(it)
                                }
                            },
                            onDrag = { change, _ ->
                                findNearestLineIndex(
                                    change.position,
                                    mappedLines,
                                    threshold = 50f
                                )?.let {
                                    if (it != lastSelectedIndex) {
                                        lastSelectedIndex = it
                                        onLineSelected(it)
                                    }
                                }
                            },
                            onDragEnd = { onSelectionEnd() },
                            onDragCancel = { onSelectionEnd() }
                        )
                    }
                } else Modifier
            )
    ) {
        if (fieldLines.isEmpty()) return@Canvas

        val allPoints = fieldLines.flatMap { listOf(it.startPoint, it.endPoint) }
        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }

        val xRange = (maxX - minX).takeIf { it > 0.0 } ?: 1.0
        val yRange = (maxY - minY).takeIf { it > 0.0 } ?: 1.0

        val xPadding = max(xRange * 0.08, 0.1)
        val yPadding = max(yRange * 0.08, 0.1)

        val adjMinX = if (minX >= 0 && minX < xRange * 0.1) 0.0 else minX - xPadding
        val adjMaxX = maxX + xPadding
        val adjMinY = if (minY >= 0 && minY < yRange * 0.1) 0.0 else minY - yPadding
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

        // This is important for the drag detection to work
        mappedLines = fieldLines.map {
            Pair(
                Offset(mapX(it.startPoint.x), mapY(it.startPoint.y)),
                Offset(mapX(it.endPoint.x), mapY(it.endPoint.y))
            )
        }

        val plotBounds = FieldPlotBounds(
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
                textStyle,
                plotWidth
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

        drawFieldLines(
            fieldLines = fieldLines,
            mappedLines = mappedLines,
            animationProgress = animationProgress,
            manualSelectedIndex = manualSelectedIndex,
            lineColorGradient = lineColorGradient,
            showDirection = showDirection,
            highlightColor = secondaryColor
        )
    }
}

private fun DrawScope.drawFieldLines(
    fieldLines: List<FieldLine>,
    mappedLines: List<Pair<Offset, Offset>>,
    animationProgress: Float,
    manualSelectedIndex: Int,
    lineColorGradient: List<Color>,
    showDirection: Boolean,
    highlightColor: Color
) {
    if (fieldLines.isEmpty() || lineColorGradient.isEmpty() || animationProgress == 0f) return

    val minStrength = fieldLines.minOfOrNull { it.fieldStrength } ?: 0.0
    val maxStrength = fieldLines.maxOfOrNull { it.fieldStrength } ?: 1.0
    val strengthRange = (maxStrength - minStrength).takeIf { it > 0 } ?: 1.0

    // Draw all lines
    fieldLines.forEachIndexed { index, line ->
        val start = mappedLines[index].first
        val fullEnd = mappedLines[index].second

        // Animate the end point based on progress
        val animatedEnd = lerp(start, fullEnd, animationProgress)

        val strengthRatio = ((line.fieldStrength - minStrength) / strengthRange).toFloat()
        val color = getColorFromGradient(strengthRatio, lineColorGradient)

        drawLine(
            color = color,
            start = start,
            end = animatedEnd,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Show arrowhead only when the line is fully drawn
        if (showDirection && animationProgress == 1f) {
            drawArrowhead(start, fullEnd, color)
        }
    }

    if (manualSelectedIndex in mappedLines.indices && animationProgress == 1f) {
        val (start, end) = mappedLines[manualSelectedIndex]
        drawLine(
            color = highlightColor.copy(alpha = 0.5f),
            start = start,
            end = end,
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = highlightColor,
            start = start,
            end = end,
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawArrowhead(start: Offset, end: Offset, color: Color) {
    val vector = end - start
    if (vector.getDistanceSquared() < 1e-6f) return // Avoid division by zero for zero-length lines

    val normal = vector / vector.getDistance()
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


private fun getColorFromGradient(ratio: Float, colors: List<Color>): Color {
    if (colors.size == 1) return colors.first()
    val index = (ratio * (colors.size - 1)).coerceIn(0f, (colors.size - 2).toFloat())
    val lowerIndex = floor(index).toInt()
    val upperIndex = ceil(index).toInt()
    val t = index - lowerIndex

    val startColor = colors[lowerIndex]
    val endColor = colors[upperIndex]

    return androidx.compose.ui.graphics.lerp(startColor, endColor, t)
}

// --- Utility Functions (some copied from your TrajectorySimulator for completeness) ---

/**
 * Finds the index of the line segment closest to a given touch point.
 */
private fun findNearestLineIndex(
    touchOffset: Offset,
    lines: List<Pair<Offset, Offset>>,
    threshold: Float
): Int? {
    return lines
        .asSequence()
        .mapIndexed { index, line ->
            val (start, end) = line
            val dist = pointToLineSegmentDistance(touchOffset, start, end)
            index to dist
        }
        .filter { it.second <= threshold }
        .minByOrNull { it.second }
        ?.first
}

/**
 * Calculates the shortest distance from a point to a line segment.
 */
private fun pointToLineSegmentDistance(p: Offset, a: Offset, b: Offset): Float {
    val ab = b - a
    val ap = p - a
    val l2 = ab.getDistanceSquared()
    if (l2 == 0f) return (p - a).getDistance()
    val t = max(0f, min(1f, (ap.x * ab.x + ap.y * ab.y) / l2))
    val projection = a + ab * t
    return (p - projection).getDistance()
}

// Your existing helper classes and functions that are still needed
private data class FieldPlotBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

// --- Copied and verified from your TrajectorySimulator ---

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawGrid(
    bounds: FieldPlotBounds, minX: Double, maxX: Double, minY: Double, maxY: Double,
    mapX: (Double) -> Float, mapY: (Double) -> Float,
    color: Color, textMeasurer: TextMeasurer, textStyle: TextStyle, plotWidth: Float
) {
    val yGridLines = 5
    val yStep = (maxY - minY) / yGridLines

    val sampleLabelWidth =
        textMeasurer.measure(text = "-8.8×10^-8", style = textStyle).size.width.toFloat()
    val maxLabels = max(2, (plotWidth / (sampleLabelWidth + 16.dp.toPx())).toInt())
    val xGridLines = (2..5).firstOrNull { maxLabels >= it } ?: maxLabels
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
    bounds: FieldPlotBounds, xLabel: String, yLabel: String,
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
    return this.formatDecimal(digits) // Assuming this expect/actual function exists
}