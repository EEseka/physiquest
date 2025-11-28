@file:OptIn(ExperimentalAnimationApi::class, ExperimentalTextApi::class)

package com.eseka.physiquest.app.physics.presentation.components.simulators

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.StrokeJoin
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max

private enum class SimulationState {
    IDLE,
    RUNNING,
    COMPLETED
}

/**
 * A sophisticated and interactive KMP composable for visualizing physics trajectory simulations.
 * It features animated path drawing, drag-to-inspect data points, and a clean,
 * informative UI suitable for both Android and iOS. This component is stateful and handles
 * its own animation logic based on the [isSimulating] flag.
 *
 * @param points The list of [Point] data to be plotted. The simulation animates through this list.
 * @param modifier The modifier to be applied to the component.
 * @param title A title for the simulation card.
 * @param xLabel A label for the horizontal axis.
 * @param yLabel A label for the vertical axis.
 * @param isSimulating A boolean flag to control the simulation.
 * - Setting to `true` starts the simulation (or resumes a paused one).
 * - Setting to `false` pauses the simulation.
 * - Toggling `false` -> `true` on a completed simulation restarts it.
 * @param simulationSpeed A multiplier for the animation speed. Higher is faster.
 * @param showGrid Toggles the visibility of the background grid lines.
 * @param showAxes Toggles the visibility of the X and Y axes.
 * @param startMarkerColor The color of the marker for the first point in the trajectory.
 * @param endMarkerColor The color of the marker for the last point in the trajectory.
 * @param enableSlider Toggles the user's ability to drag on the graph to inspect points.
 * This is automatically disabled during simulation.
 * @param onSimulationComplete A callback lambda that is invoked when the animation finishes.
 */
@Composable
fun TrajectorySimulator(
    points: List<Point>,
    modifier: Modifier = Modifier,
    title: String = "Physics Simulation",
    xLabel: String = "X-Displacement (m)",
    yLabel: String = "Y-Displacement (m)",
    isSimulating: Boolean = false,
    simulationSpeed: Float = 1f,
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    startMarkerColor: Color = MaterialTheme.colorScheme.tertiary,
    endMarkerColor: Color = MaterialTheme.colorScheme.error,
    enableSlider: Boolean = true,
    onSimulationComplete: () -> Unit = {}
) {
    var animationProgress by remember { mutableFloatStateOf(0f) }
    var currentPointIndex by remember { mutableIntStateOf(0) }
    var simulationState by remember { mutableStateOf(SimulationState.IDLE) }
    var isManuallySelecting by remember { mutableStateOf(false) }
    var manualSelectedIndex by remember { mutableIntStateOf(-1) }

    val stepDuration = (50L / simulationSpeed).toLong().coerceAtLeast(1L)

    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(
            durationMillis = if (isSimulating) stepDuration.toInt() else 0,
            easing = LinearEasing
        ),
        label = "trajectory_animation"
    )

    // Reset all state when the input points data changes
    LaunchedEffect(points) {
        animationProgress = 0f
        currentPointIndex = 0
        simulationState = SimulationState.IDLE
        isManuallySelecting = false
        manualSelectedIndex = -1
    }

    // Core simulation logic controller
    LaunchedEffect(isSimulating) {
        if (isSimulating && points.isNotEmpty()) {
            // 👇 THE FIX: Reset if the simulation is complete and is asked to start again.
            if (simulationState == SimulationState.COMPLETED) {
                currentPointIndex = 0
                animationProgress = 0f
            }

            isManuallySelecting = false // Disable manual selection during simulation
            simulationState = SimulationState.RUNNING

            // Loop from the current point to the end
            for (i in currentPointIndex until points.size) {
                currentPointIndex = i
                // Animate progress smoothly
                animationProgress = (i + 1).toFloat() / points.size
                delay(stepDuration)
            }

            // Mark as complete only when the loop finishes naturally
            if (currentPointIndex == points.size - 1) {
                simulationState = SimulationState.COMPLETED
                onSimulationComplete()
            }
        } else {
            // If simulation is stopped, update state but preserve progress
            if (simulationState == SimulationState.RUNNING) {
                simulationState = SimulationState.IDLE
            }
        }
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
                if (points.isNotEmpty()) {
                    TrajectoryCanvas(
                        points = points,
                        animationProgress = animatedProgress, // Use the animated value for smoothness
                        currentPointIndex = currentPointIndex,
                        manualSelectedIndex = manualSelectedIndex,
                        simulationState = simulationState,
                        xLabel = xLabel,
                        yLabel = yLabel,
                        showGrid = showGrid,
                        showAxes = showAxes,
                        startMarkerColor = startMarkerColor,
                        endMarkerColor = endMarkerColor,
                        enableSlider = enableSlider && !isSimulating,
                        onPointSelected = { index ->
                            if (enableSlider && !isSimulating) {
                                isManuallySelecting = true
                                manualSelectedIndex = index
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No data to display",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoFooter(
                points = points,
                simulationState = simulationState,
                isManuallySelecting = isManuallySelecting,
                currentPointIndex = currentPointIndex,
                manualSelectedIndex = manualSelectedIndex
            )
        }
    }
}

@Composable
private fun InfoFooter(
    points: List<Point>,
    simulationState: SimulationState,
    isManuallySelecting: Boolean,
    currentPointIndex: Int,
    manualSelectedIndex: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Points: ${points.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            val infoText: Pair<String, Color> = when {
                simulationState == SimulationState.RUNNING && currentPointIndex < points.size -> {
                    val pt = points[currentPointIndex]
                    "Simulating: (${pt.x.formatForGraph()}, ${pt.y.formatForGraph()})" to MaterialTheme.colorScheme.primary
                }

                isManuallySelecting && manualSelectedIndex in points.indices -> {
                    val pt = points[manualSelectedIndex]
                    "Selected: (${pt.x.formatForGraph()}, ${pt.y.formatForGraph()}) [${manualSelectedIndex + 1}/${points.size}]" to MaterialTheme.colorScheme.secondary
                }

                simulationState != SimulationState.RUNNING -> {
                    "Drag on graph to explore points" to MaterialTheme.colorScheme.onSurface.copy(
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
            simulationState == SimulationState.RUNNING -> "Simulating..." to MaterialTheme.colorScheme.primary
            simulationState == SimulationState.COMPLETED -> "Complete" to MaterialTheme.colorScheme.tertiary
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
private fun TrajectoryCanvas(
    points: List<Point>,
    animationProgress: Float,
    currentPointIndex: Int,
    manualSelectedIndex: Int,
    simulationState: SimulationState,
    xLabel: String,
    yLabel: String,
    showGrid: Boolean,
    showAxes: Boolean,
    startMarkerColor: Color,
    endMarkerColor: Color,
    enableSlider: Boolean,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = onSurfaceColor.copy(alpha = 0.6f),
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        textAlign = TextAlign.Center
    )

    var mappedPoints by remember { mutableStateOf(emptyList<Offset>()) }
    var isDragging by remember { mutableStateOf(false) }
    var lastSelectedIndex by remember { mutableIntStateOf(-1) }

    Canvas(
        modifier = modifier
            .then(
                if (enableSlider) {
                    Modifier.pointerInput(points) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                findNearestPointIndex(offset, mappedPoints, threshold = 50f)?.let {
                                    lastSelectedIndex = it
                                    onPointSelected(it)
                                }
                            },
                            onDrag = { change, _ ->
                                if (isDragging) {
                                    findNearestPointIndex(
                                        change.position,
                                        mappedPoints,
                                        threshold = 80f
                                    )?.let {
                                        if (it != lastSelectedIndex) {
                                            lastSelectedIndex = it
                                            onPointSelected(it)
                                        }
                                    }
                                }
                            },
                            onDragEnd = { isDragging = false }
                        )
                    }
                } else Modifier
            )
    ) {
        if (points.isEmpty()) return@Canvas

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

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

        mappedPoints = points.map { Offset(mapX(it.x), mapY(it.y)) }

        val plotBounds = PlotBounds(
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

        drawTrajectory(
            points,
            animationProgress,
            currentPointIndex,
            manualSelectedIndex,
            simulationState,
            ::mapX,
            ::mapY,
            primaryColor,
            secondaryColor,
            onSurfaceColor,
            startMarkerColor,
            endMarkerColor
        )
    }
}


private fun findNearestPointIndex(
    touchOffset: Offset,
    mappedPoints: List<Offset>,
    threshold: Float
): Int? {
    return mappedPoints
        .asSequence()
        .mapIndexed { index, point -> index to (touchOffset - point).getDistance() }
        .filter { it.second <= threshold }
        .minByOrNull { it.second }
        ?.first
}

private data class PlotBounds(val left: Float, val top: Float, val right: Float, val bottom: Float)

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawGrid(
    bounds: PlotBounds, minX: Double, maxX: Double, minY: Double, maxY: Double,
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
    bounds: PlotBounds, xLabel: String, yLabel: String,
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

private fun DrawScope.drawTrajectory(
    points: List<Point>,
    animationProgress: Float,
    currentPointIndex: Int,
    manualSelectedIndex: Int,
    simulationState: SimulationState,
    mapX: (Double) -> Float,
    mapY: (Double) -> Float,
    primaryColor: Color,
    secondaryColor: Color,
    onSurfaceColor: Color,
    startMarkerColor: Color,
    endMarkerColor: Color
) {
    val visiblePointCount = (points.size * animationProgress).toInt().coerceAtMost(points.size)
    if (visiblePointCount < 1) return

    if (visiblePointCount > 1) {
        val path = Path().apply {
            moveTo(mapX(points.first().x), mapY(points.first().y))
            for (i in 1 until visiblePointCount) {
                lineTo(mapX(points[i].x), mapY(points[i].y))
            }
        }
        drawPath(
            path,
            primaryColor,
            style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    val isExploring = manualSelectedIndex != -1
    val isSimulating = simulationState == SimulationState.RUNNING
    val highlightedIndex =
        if (isExploring) manualSelectedIndex else if (isSimulating) currentPointIndex else -1

    if (highlightedIndex in points.indices) {
        val point = points[highlightedIndex]
        val center = Offset(mapX(point.x), mapY(point.y))
        val highlightColor = if (isExploring) secondaryColor else primaryColor
        drawCircle(highlightColor.copy(alpha = 0.2f), 14.dp.toPx(), center)
        drawCircle(highlightColor.copy(alpha = 0.4f), 10.dp.toPx(), center)
    }

    points.firstOrNull()?.let {
        val center = Offset(mapX(it.x), mapY(it.y))
        drawCircle(
            Color.Black.copy(alpha = 0.15f),
            8.dp.toPx(),
            center.copy(x = center.x + 1.dp.toPx(), y = center.y + 1.dp.toPx())
        )
        drawCircle(startMarkerColor, 7.dp.toPx(), center)
        drawCircle(onSurfaceColor, 3.5.dp.toPx(), center)
    }

    if (visiblePointCount == points.size && points.size > 1) {
        points.last().let {
            val center = Offset(mapX(it.x), mapY(it.y))
            drawCircle(
                Color.Black.copy(alpha = 0.15f),
                8.dp.toPx(),
                center.copy(x = center.x + 1.dp.toPx(), y = center.y + 1.dp.toPx())
            )
            drawCircle(endMarkerColor, 7.dp.toPx(), center)
            drawCircle(onSurfaceColor, 3.5.dp.toPx(), center)
        }
    }
}


/**
 * A smart wrapper that uses your existing `formatDecimal` expect/actual function.
 * It fixes the "0.0 x 10^0" bug and provides cleaner formatting for graphs.
 */
private fun Double.formatForGraph(): String {
    // 1. Handle the zero case directly to fix the bug
    if (this == 0.0) return "0"

    // 2. Decide the best number of decimal places for readability
    val digits = when {
        abs(this) >= 100 -> 0
        abs(this) >= 10 -> 1
        else -> 2 // Default for smaller numbers and for your scientific notation
    }

    // 3. Call your existing, platform-specific function
    return this.formatDecimal(digits)
}