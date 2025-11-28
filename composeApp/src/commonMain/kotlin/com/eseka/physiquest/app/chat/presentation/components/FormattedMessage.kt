package com.eseka.physiquest.app.chat.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.chat.presentation.utils.parseMessageContent
import com.eseka.physiquest.app.chat.presentation.utils.setTransparent
import com.eseka.physiquest.core.domain.utils.getPlatform
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

@Composable
fun FormattedMessage(
    content: String,
    textColor: Color,
    linkColor: Color,
    modifier: Modifier = Modifier
) {
    val assetsResult = rememberLatexAssets()
    // Track the calculated height for iOS
    var webViewHeight by remember { mutableStateOf(100.dp) }
    val density = LocalDensity.current
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (getPlatform().contains("IOS", ignoreCase = true)) {
                    Modifier.height(webViewHeight)
                } else {
                    Modifier.wrapContentHeight()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            assetsResult == null -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
            assetsResult.isFailure -> Text("[Error rendering message]")
            assetsResult.isSuccess -> {
                val assets = assetsResult.getOrThrow()
                val webViewState = rememberWebViewState("about:blank")
                val navigator = rememberWebViewNavigator(
                    requestInterceptor = object : RequestInterceptor {
                        override fun onInterceptUrlRequest(
                            request: WebRequest,
                            navigator: WebViewNavigator,
                        ): WebRequestInterceptResult {
                            // Check if the URL is a standard web link.
                            if (request.url.startsWith("http://") || request.url.startsWith("https://")) {
                                // Use the uriHandler to open the link in the default browser.
                                uriHandler.openUri(request.url)
                                // Tell the WebView to IGNORE the request.
                                return WebRequestInterceptResult.Reject
                            }
                            // For all other requests (like "about:blank"), allow them.
                            return WebRequestInterceptResult.Allow
                        }
                    }
                )

                // Create a JS bridge to get height updates from WebView
                val jsBridge = rememberWebViewJsBridge()

                LaunchedEffect(jsBridge) {
                    // Register handler for height updates
                    jsBridge.register(object : IJsMessageHandler {
                        override fun methodName(): String = "updateHeight"
                        override fun handle(
                            message: JsMessage, navigator: WebViewNavigator?,
                            callback: (String) -> Unit
                        ) {
                            try {
                                val height = message.params.toFloatOrNull() ?: 100f
                                webViewHeight = with(density) { height.toDp() }
                                callback("success")
                            } catch (e: Exception) {
                                callback("error")
                            }
                        }
                    })
                }

                val fullHtml = remember(content, assets, textColor, linkColor) {
                    val parts = parseMessageContent(content)
                    generateHtmlFromPartsWithHeightReporting(parts, assets, textColor, linkColor)
                }

                LaunchedEffect(fullHtml) {
                    navigator.loadHtml(fullHtml)
                }

                WebView(
                    state = webViewState,
                    navigator = navigator,
                    webViewJsBridge = jsBridge,
                    modifier = Modifier.fillMaxSize(),
                    onCreated = { nativeWebView ->
                        nativeWebView.setTransparent()
                    }
                )
            }
        }
    }
}

//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun FormattedMessage(
//    content: String,
//    modifier: Modifier = Modifier
//) {
//    val contentParts = remember(content) { parseMessageContent(content) }
//    val textColor = MaterialTheme.colorScheme.onSurface
//
//    FlowRow(
//        modifier = modifier,
//        verticalArrangement = Arrangement.Center,
//        horizontalArrangement = Arrangement.Start
//    ) {
//        contentParts.forEach { part ->
//            when (part) {
//                is MessageContentPart.Text -> {
//                    ClickableFormattedText(
//                        text = part.text,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = textColor
//                    )
//                }
//
//                is MessageContentPart.Latex -> {
//                    LatexView(
//                        latex = part.content,
//                        displayMode = part.isDisplayMode,
//                        textColor = textColor
//                    )
//                }
//            }
//        }
//    }
//}

//@Composable
//fun ClickableFormattedText(
//    text: String,
//    modifier: Modifier = Modifier,
//    style: TextStyle = LocalTextStyle.current,
//    color: Color = LocalContentColor.current
//) {
//    val uriHandler = LocalUriHandler.current
//    val annotatedString = buildAnnotatedString {
//        var currentIndex = 0
//        val patterns = mapOf(
//            Regex("""(?:###|##)\s*(.*?)(?=\n|$)""") to "heading",
//            Regex("""\*\*(.*?)\*\*""") to "bold",
//            Regex("""\*(.*?)\*""") to "italic",
//            Regex("""`(.*?)`""") to "code",
//            Regex("""~~(.*?)~~""") to "strikethrough",
//            Regex("""_(.*?)_""") to "underline",
//            Regex("""\[(.*?)]\((.*?)\)""") to "link"
//        )
//
//        while (currentIndex < text.length) {
//            val matches = patterns.map { (regex, type) ->
//                regex.find(text, currentIndex)?.let {
//                    Triple(it.range.first, type, it)
//                }
//            }
//
//            val firstMatch = matches.filterNotNull().minByOrNull { it.first }
//
//            if (firstMatch == null) {
//                append(text.substring(currentIndex))
//                break
//            }
//
//            append(text.substring(currentIndex, firstMatch.first))
//
//            when (firstMatch.second) {
//                "heading" -> {
//                    append("\n")
//                    withStyle(
//                        SpanStyle(
//                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
//                            fontWeight = FontWeight.Bold,
//                            letterSpacing = 0.5.sp
//                        )
//                    ) {
//                        append(firstMatch.third.groupValues[1].trim())
//                    }
//                    append("\n")
//                }
//
//                "link" -> {
//                    val linkText = firstMatch.third.groupValues[1]
//                    val linkUrl = firstMatch.third.groupValues[2]
//                    pushStringAnnotation(
//                        tag = "URL",
//                        annotation = linkUrl
//                    )
//                    withStyle(
//                        SpanStyle(
//                            color = MaterialTheme.colorScheme.primary,
//                            textDecoration = TextDecoration.Underline
//                        )
//                    ) {
//                        append(linkText)
//                    }
//                    pop()
//                }
//
//                "code" -> {
//                    withStyle(
//                        SpanStyle(
//                            background = MaterialTheme.colorScheme.surfaceVariant,
//                            fontFamily = FontFamily.Monospace,
//                            fontSize = style.fontSize * 0.9,
//                            letterSpacing = 0.sp
//                        )
//                    ) {
//                        append(" ${firstMatch.third.groupValues[1]} ")
//                    }
//                }
//
//                "bold" -> {
//                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
//                        append(firstMatch.third.groupValues[1])
//                    }
//                }
//
//                "italic" -> {
//                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
//                        append(firstMatch.third.groupValues[1])
//                    }
//                }
//
//                "strikethrough" -> {
//                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
//                        append(firstMatch.third.groupValues[1])
//                    }
//                }
//
//                "underline" -> {
//                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
//                        append(firstMatch.third.groupValues[1])
//                    }
//                }
//            }
//            currentIndex = firstMatch.third.range.last + 1
//        }
//    }
//
//    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
//
//    BasicText(
//        text = annotatedString,
//        modifier = modifier.pointerInput(Unit) {
//            detectTapGestures { offset ->
//                layoutResult.value?.let { layoutResult ->
//                    val position = layoutResult.getOffsetForPosition(offset)
//                    annotatedString
//                        .getStringAnnotations(tag = "URL", start = position, end = position)
//                        .firstOrNull()
//                        ?.let { annotation -> uriHandler.openUri(annotation.item) }
//                }
//            }
//        },
//        style = style.copy(color = color),
//        onTextLayout = { layoutResult.value = it }
//    )
//}