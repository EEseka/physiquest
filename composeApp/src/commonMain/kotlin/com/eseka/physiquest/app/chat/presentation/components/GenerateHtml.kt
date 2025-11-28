package com.eseka.physiquest.app.chat.presentation.components

import androidx.compose.ui.graphics.Color
import com.eseka.physiquest.app.chat.presentation.utils.MessageContentPart
import com.eseka.physiquest.app.chat.presentation.utils.toHex

fun generateHtmlFromPartsWithHeightReporting(
    parts: List<MessageContentPart>,
    assets: LatexAssets,
    textColor: Color,
    linkColor: Color
): String {
    val textColorHex = textColor.toHex()
    val linkColorHex = linkColor.toHex()

    val bodyContent = buildString {
        parts.forEach { part ->
            when (part) {
                is MessageContentPart.Text -> {
                    var textContent = part.text
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")

                    textContent =
                        textContent.replace(Regex("""\*\*(.*?)\*\*"""), "<strong>$1</strong>")
                    textContent = textContent.replace(Regex("""\*(.*?)\*"""), "<em>$1</em>")
                    textContent = textContent.replace(
                        Regex("""\[(.*?)]\((.*?)\)"""),
                        """<a href="$2">$1</a>"""
                    )
                    textContent = textContent.replace("\n", "<br>")

                    append(textContent)
                }

                is MessageContentPart.Latex -> {
                    if (part.isDisplayMode) {
                        append("""<div class="latex-block">${part.content}</div>""")
                    } else {
                        append("""<span class="latex-inline">${part.content}</span>""")
                    }
                }
            }
        }
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
            ${assets.css}
            html, body {
                background-color: transparent !important; 
                margin: 0; 
                padding: 0;
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                font-size: 16px; 
                line-height: 1.6; 
                color: $textColorHex;
            }
            a { color: $linkColorHex; text-decoration: underline; }
            strong { font-weight: 600; }
            em { font-style: italic; }
            .latex-inline { display: inline-block; vertical-align: middle; }
            .latex-block { display: block; overflow-x: auto; padding: 0.5em 0; }
        </style>
        <script>${assets.js}</script>
    </head>
    <body>
        ${bodyContent}
        <script>
            function updateWebViewHeight() {
                const body = document.body;
                const html = document.documentElement;
                const height = Math.max(
                    body.scrollHeight,
                    body.offsetHeight,
                    html.clientHeight,
                    html.scrollHeight,
                    html.offsetHeight
                );
                
                // Send height to native code
                if (window.kmpJsBridge && window.kmpJsBridge.callNative) {
                    window.kmpJsBridge.callNative("updateHeight", height.toString(), function(response) {
                        console.log("Height update response: " + response);
                    });
                }
            }
            
            document.addEventListener('DOMContentLoaded', function() {
                // Render LaTeX
                document.querySelectorAll('.latex-inline').forEach(e => 
                    katex.render(e.textContent || "", e, { throwOnError: false, displayMode: false })
                );
                document.querySelectorAll('.latex-block').forEach(e => 
                    katex.render(e.textContent || "", e, { throwOnError: false, displayMode: true })
                );
                
                // Update height after rendering
                setTimeout(updateWebViewHeight, 100);
            });
            
            // Also update on window resize
            window.addEventListener('resize', updateWebViewHeight);
            
            // Update on image load
            document.querySelectorAll('img').forEach(img => {
                img.addEventListener('load', updateWebViewHeight);
            });
        </script>
    </body>
    </html>
    """.trimIndent()
}

//@Composable
//fun LatexView(
//    latex: String,
//    modifier: Modifier = Modifier,
//    displayMode: Boolean = false,
//    textColor: Color,
//) {
//    val assetsResult = rememberLatexAssets()
//
//    // --- START: THE FIX ---
//    // Get the font size of the surrounding text to calculate a matching height for inline equations.
//    val surroundingTextStyle = MaterialTheme.typography.bodyMedium
//    val inlineEquationHeight = with(LocalDensity.current) {
//        // Give it a bit of extra vertical space (e.g., 1.4x the font size)
//        // to prevent clipping of taller characters like superscripts.
//        (surroundingTextStyle.fontSize * 1.4f).toDp()
//    }
//    // --- END: THE FIX ---
//
//
//    Box(
//        modifier = modifier.then(
//            // --- START: THE FIX ---
//            // Apply different sizing logic for block vs. inline.
//            if (displayMode) {
//                // Block equations get their own line and a minimum height.
//                Modifier.fillMaxWidth().heightIn(min = 48.dp)
//            } else {
//                // Inline equations get a height that matches the text and a flexible width.
//                Modifier
//                    .height(inlineEquationHeight)
//                    .wrapContentWidth()
//            }
//            // --- END: THE FIX ---
//        ),
//        contentAlignment = Alignment.Center
//    ) {
//        when {
//            assetsResult == null -> {
//                CircularProgressIndicator(modifier = Modifier.size(24.dp))
//            }
//
//            assetsResult.isFailure -> {
//                Icon(
//                    imageVector = Icons.Default.ErrorOutline,
//                    contentDescription = "Error loading LaTeX assets",
//                    modifier = Modifier.size(24.dp),
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
//
//            assetsResult.isSuccess -> {
//                val assets = assetsResult.getOrThrow()
//                val webViewState = rememberWebViewState("about:blank")
//                val navigator = rememberWebViewNavigator()
//                val html = remember(latex, displayMode, textColor, assets) {
//                    generateLatexHtml(latex, displayMode, textColor, assets)
//                }
//                LaunchedEffect(html) {
//                    navigator.loadHtml(html)
//                }
//                WebView(
//                    state = webViewState,
//                    navigator = navigator,
//                    modifier = Modifier.matchParentSize(),
//                    onCreated = { nativeWebView ->
//                        nativeWebView.setTransparent()
//                    }
//                )
//            }
//        }
//    }
//}

//fun generateLatexHtml(
//    latex: String,
//    displayMode: Boolean,
//    textColor: Color,
//    assets: LatexAssets
//): String {
//    val escapedLatex = latex
//        .replace("\\", "\\\\")
//        .replace("'", "\\'")
//        .replace("`", "\\`")
//        .replace("\n", "\\n")
//        .replace("\r", "")
//
//    val colorHex = textColor.toHex()
//
//    return """
//    <!DOCTYPE html>
//    <html>
//    <head>
//        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
//        <style>
//            ${assets.css}
//            html, body {
//                background-color: transparent !important;
//                margin: 0;
//                padding: 0;
//                color: $colorHex;
//                display: flex;
//                justify-content: center;
//                align-items: center;
//                height: 100%;
//                width: 100%;
//            }
//            .katex {
//                font-size: ${if (displayMode) "1.2em" else "1.0em"} !important;
//                overflow-x: auto;
//                padding: 0.2em;
//                -ms-overflow-style: none;
//                scrollbar-width: none;
//            }
//            .katex::-webkit-scrollbar {
//                display: none;
//            }
//        </style>
//        <script>${assets.js}</script>
//    </head>
//    <body>
//        <div id="katex-container"></div>
//
//        <script>
//            // Wait for the DOM to be fully loaded before trying to access elements.
//            document.addEventListener('DOMContentLoaded', function() {
//                try {
//                    // This code is now guaranteed to run after the div exists.
//                    katex.render("$escapedLatex", document.getElementById('katex-container'), {
//                        throwOnError: false,
//                        displayMode: $displayMode,
//                        strict: false,
//                        output: 'html'
//                    });
//                } catch (e) {
//                    // Error handling remains the same.
//                    document.getElementById('katex-container').innerText = 'Render Error';
//                }
//            });
//        </script>
//        </body>
//    </html>
//    """.trimIndent()
//}