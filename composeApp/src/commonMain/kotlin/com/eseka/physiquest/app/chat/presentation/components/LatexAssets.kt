package com.eseka.physiquest.app.chat.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import com.diamondedge.logging.logging
import com.eseka.physiquest.core.domain.utils.readResourceFile

data class LatexAssets(val css: String, val js: String)

/**
 * Loads KaTeX assets from resources and provides them as a `Result`.
 * This handles loading, success, and failure states gracefully.
 */
@Composable
fun rememberLatexAssets(): Result<LatexAssets>? {
    return produceState<Result<LatexAssets>?>(initialValue = null) {
        value = try {
            // These calls will throw an exception if files are not found
            val css = readResourceFile("katex.min.css")
            val js = readResourceFile("katex.min.js")
            Result.success(LatexAssets(css = css, js = js))
        } catch (e: Exception) {
            val log = logging("LatexAssets")
            log.e { "Error loading LaTeX assets: ${e.message}" }
            Result.failure(e)
        }
    }.value
}