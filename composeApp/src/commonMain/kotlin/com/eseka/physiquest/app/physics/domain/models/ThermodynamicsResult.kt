package com.eseka.physiquest.app.physics.domain.models

data class ThermodynamicsResult(
    val pressure: Double?,
    val volume: Double?,
    val temperature: Double?,
    val numberOfMoles: Double?,
    val heatTransfer: Double?,
    val work: Double?,
    val internalEnergyChange: Double?,
    val entropy: Double?,
    val efficiency: Double?,
    // For simulation - temperature vs time, PV diagram points
    val temperatureTimePoints: List<Point>,
    val pvDiagramPoints: List<Point>
)