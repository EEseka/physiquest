package com.eseka.physiquest.app.chat.presentation.utils

sealed class MessageContentPart {
    data class Text(val text: String) : MessageContentPart()

    // A single class for LaTeX that knows if it should be displayed as a block or inline
    data class Latex(val content: String, val isDisplayMode: Boolean) : MessageContentPart()
}

/**
 * Parses a string containing mixed text and LaTeX blocks (delimited by $$)
 * into a list of structured parts in a Kotlin Multiplatform-compatible way.
 */
// In com.eseka.physiquest.app.chat.presentation.utils/parseMessageContent.kt

fun parseMessageContent(content: String): List<MessageContentPart> {

    // Define regex for each delimiter type. Note the escaped backslashes for Kotlin strings.
    val displayRegex1 = Regex("""\$\$([\s\S]*?)\$\$""")      // For $$...$$
    val displayRegex2 = Regex("""\\\[([\s\S]*?)\\\]""")      // For \[...\]
    val inlineRegex = Regex("""\\\(([\s\S]*?)\\\)""")        // For \(...\)

    // Data class to hold found matches before sorting
    data class FoundToken(val range: IntRange, val content: String, val isDisplay: Boolean)

    // Find all matches for all types and add them to a single list
    val allTokens = mutableListOf<FoundToken>()
    displayRegex1.findAll(content).forEach { match ->
        allTokens.add(FoundToken(match.range, match.groupValues[1], isDisplay = true))
    }
    displayRegex2.findAll(content).forEach { match ->
        allTokens.add(FoundToken(match.range, match.groupValues[1], isDisplay = true))
    }
    inlineRegex.findAll(content).forEach { match ->
        allTokens.add(FoundToken(match.range, match.groupValues[1], isDisplay = false))
    }

    // Sort tokens by their starting position in the original text
    allTokens.sortBy { it.range.first }

    val parts = mutableListOf<MessageContentPart>()
    var lastIndex = 0

    // Iterate through the sorted tokens, adding text and LaTeX parts in order
    allTokens.forEach { token ->
        // Add the text that comes before this token
        if (token.range.first > lastIndex) {
            val text = content.substring(lastIndex, token.range.first)
            parts.add(MessageContentPart.Text(text))
        }

        // Add the LaTeX part itself
        parts.add(MessageContentPart.Latex(token.content.trim(), token.isDisplay))

        // Update our position
        lastIndex = token.range.last + 1
    }

    // Add any remaining text after the last token
    if (lastIndex < content.length) {
        parts.add(MessageContentPart.Text(content.substring(lastIndex)))
    }

    return parts
}