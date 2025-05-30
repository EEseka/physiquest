package com.eseka.physiquest.app

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MainEventBus {
    private val _events = Channel<MainEvent>(Channel.Factory.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun send(event: MainEvent) {
        _events.send(event)
    }
}