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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.physics.domain.models.Point
import com.eseka.physiquest.app.physics.presentation.utils.formatDecimal
import kotlin.math.abs

/**
 * A composable for visualizing a set of equipotential lines.
 * It animates the drawing of each line and allows for interactive exploration.
 *
 * @param equipotentialLines A list of lines, where each line is a list of points.
 * @param isAnimating A boolean flag to control the entry animation.
 * @param onAnimationComplete A callback lambda invoked when the animation finishes.
 */
@Composable
fun EquipotentialSimulator(
    equipotentialLines: List<List<Point>>,
    modifier: Modifier = Modifier,
    title: String = "Equipotential Lines",
    xLabel: String = "X-Position (m)",
    yLabel: String = "Y-Position (m)",
    isAnimating: Boolean = false,
    animationDuration: Int = 1500,
    onAnimationComplete: () -> Unit = {},
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    lineColor: Color = MaterialTheme.colorScheme.tertiary,
    highlightColor: Color = MaterialTheme.colorScheme.secondary
) {
    // State for interactivity, inspired by TrajectorySimulator
    var manualSelectedIndex by remember { mutableIntStateOf(-1) }
    var isManuallySelecting by remember { mutableStateOf(false) }

    val animatable = remember { Animatable(0f) }
    val animationProgress = animatable.value

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            animatable.snapTo(0f)
            animatable.animateTo(1f, animationSpec = tween(durationMillis = animationDuration))
            onAnimationComplete()
        }
    }


    // Reset interaction state when the data changes
    LaunchedEffect(equipotentialLines) {
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
                if (equipotentialLines.isNotEmpty()) {
                    EquipotentialCanvas(
                        equipotentialLines = equipotentialLines,
                        animationProgress = animationProgress,
                        manualSelectedIndex = manualSelectedIndex,
                        xLabel = xLabel,
                        yLabel = yLabel,
                        showGrid = showGrid,
                        showAxes = showAxes,
                        lineColor = lineColor,
                        highlightColor = highlightColor,
                        // Interaction is disabled during animation for a cleaner experience
                        enableInteraction = animationProgress == 1f,
                        onLineSelected = { index ->
                            isManuallySelecting = true
                            manualSelectedIndex = index
                        },
                        onSelectionEnd = { isManuallySelecting = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No equipotential data to display",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            EquipotentialInfoFooter(
                linesCount = equipotentialLines.size,
                isManuallySelecting = isManuallySelecting,
                manualSelectedIndex = manualSelectedIndex,
                animationProgress = animationProgress
            )
        }
    }
}

@Composable
private fun EquipotentialInfoFooter(
    linesCount: Int,
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
                text = "Lines: $linesCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // 👇 Updated info text for interactivity
            val infoText: Pair<String, Color> = when {
                animationProgress > 0f && animationProgress < 1.0f -> {
                    "Animating..." to MaterialTheme.colorScheme.primary
                }

                isManuallySelecting && manualSelectedIndex != -1 -> {
                    "Selected: Line ${manualSelectedIndex + 1}" to MaterialTheme.colorScheme.secondary
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
private fun EquipotentialCanvas(
    equipotentialLines: List<List<Point>>,
    animationProgress: Float,
    manualSelectedIndex: Int,
    xLabel: String,
    yLabel: String,
    showGrid: Boolean,
    showAxes: Boolean,
    lineColor: Color,
    highlightColor: Color,
    enableInteraction: Boolean,
    onLineSelected: (Int) -> Unit,
    onSelectionEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = onSurfaceColor.copy(alpha = 0.6f),
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        textAlign = TextAlign.Center
    )

    // Store mapped points for touch detection
    var mappedLines by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }

    Canvas(
        modifier = modifier.then(
            // 👇 ADDED: Pointer input for interactivity
            if (enableInteraction) {
                Modifier.pointerInput(equipotentialLines) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            findNearestLineIndex(offset, mappedLines, 40f)?.let(onLineSelected)
                        },
                        onDrag = { change, _ ->
                            findNearestLineIndex(change.position, mappedLines, 60f)?.let(
                                onLineSelected
                            )
                        },
                        onDragEnd = { onSelectionEnd() },
                        onDragCancel = { onSelectionEnd() }
                    )
                }
            } else Modifier
        )
    ) {
        if (equipotentialLines.isEmpty() || equipotentialLines.all { it.isEmpty() }) return@Canvas

        val allPoints = equipotentialLines.flatten()
        val minX = allPoints.minOfOrNull { it.x } ?: -1.0
        val maxX = allPoints.maxOfOrNull { it.x } ?: 1.0
        val minY = allPoints.minOfOrNull { it.y } ?: -1.0
        val maxY = allPoints.maxOfOrNull { it.y } ?: 1.0

        val xRange = (maxX - minX).takeIf { it > 0.0 } ?: 1.0
        val yRange = (maxY - minY).takeIf { it > 0.0 } ?: 1.0

        val centeredPoints = allPoints + Point(0.0, 0.0)
        val adjMinX = (centeredPoints.minOfOrNull { it.x } ?: minX) - xRange * 0.1
        val adjMaxX = (centeredPoints.maxOfOrNull { it.x } ?: maxX) + xRange * 0.1
        val adjMinY = (centeredPoints.minOfOrNull { it.y } ?: minY) - yRange * 0.1
        val adjMaxY = (centeredPoints.maxOfOrNull { it.y } ?: maxY) + yRange * 0.1


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

        // Update mapped lines for hit detection
        mappedLines = equipotentialLines.map { line ->
            line.map { point ->
                Offset(
                    mapX(point.x),
                    mapY(point.y)
                )
            }
        }

        val plotBounds = EquipotentialPlotBounds(
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

        drawEquipotentialLines(
            equipotentialLines,
            animationProgress,
            manualSelectedIndex,
            ::mapX,
            ::mapY,
            lineColor,
            highlightColor
        )
    }
}


private fun DrawScope.drawEquipotentialLines(
    lines: List<List<Point>>,
    animationProgress: Float,
    manualSelectedIndex: Int,
    mapX: (Double) -> Float,
    mapY: (Double) -> Float,
    color: Color,
    highlightColor: Color
) {
    if (animationProgress == 0f) return

    lines.forEachIndexed { index, line ->
        if (line.size < 2) return@forEachIndexed

        val path = Path()
        path.moveTo(mapX(line.first().x), mapY(line.first().y))

        val pointsToDraw = (line.size * animationProgress).toInt().coerceAtMost(line.size)

        for (i in 1 until pointsToDraw) {
            path.lineTo(mapX(line[i].x), mapY(line[i].y))
        }

        // 👇 Determine color and stroke based on selection
        val isSelected = index == manualSelectedIndex
        val strokeColor = if (isSelected) highlightColor else color
        val strokeWidth = if (isSelected) 4.dp.toPx() else 2.dp.toPx()

        drawPath(
            path,
            color = strokeColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// 👇 ADDED: New helper function to find the closest line
/**
 * Finds the index of the line closest to a given touch offset.
 */
private fun findNearestLineIndex(
    touchOffset: Offset,
    mappedLines: List<List<Offset>>,
    threshold: Float
): Int? {
    var nearestLineIndex: Int? = null
    var minDistance = Float.MAX_VALUE

    mappedLines.forEachIndexed { lineIndex, line ->
        line.forEach { point ->
            val distance = (touchOffset - point).getDistance()
            if (distance < minDistance) {
                minDistance = distance
                nearestLineIndex = lineIndex
            }
        }
    }

    return if (minDistance <= threshold) {
        nearestLineIndex
    } else {
        null
    }
}


// --- Common Utility Functions (can be extracted to a shared file) ---
private data class EquipotentialPlotBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawGrid(
    bounds: EquipotentialPlotBounds, minX: Double, maxX: Double, minY: Double, maxY: Double,
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
    bounds: EquipotentialPlotBounds, xLabel: String, yLabel: String,
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
    // Assuming you have this expect/actual function in your project
    return this.formatDecimal(digits)
}