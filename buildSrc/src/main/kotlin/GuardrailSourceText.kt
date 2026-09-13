internal fun stripCommentsPreservingLiterals(source: String): String {
    val result = StringBuilder(source.length)
    var lineComment = false
    var blockCommentDepth = 0
    var quoted = false
    var character = false
    var rawString = false
    var index = 0

    fun appendBlank(value: Char) {
        result.append(if (value == '\n' || value == '\r') value else ' ')
    }

    while (index < source.length) {
        val current = source[index]
        val next = source.getOrNull(index + 1)
        when {
            lineComment -> {
                appendBlank(current)
                if (current == '\n') lineComment = false
                index++
            }
            blockCommentDepth > 0 -> {
                if (current == '/' && next == '*') {
                    appendBlank(current)
                    appendBlank(next)
                    blockCommentDepth++
                    index += 2
                } else if (current == '*' && next == '/') {
                    appendBlank(current)
                    appendBlank(next)
                    blockCommentDepth--
                    index += 2
                } else {
                    appendBlank(current)
                    index++
                }
            }
            rawString -> {
                result.append(current)
                if (current == '"' && source.startsWith("\"\"\"", index)) {
                    result.append(source.substring(index + 1, index + 3))
                    rawString = false
                    index += 3
                } else {
                    index++
                }
            }
            quoted -> {
                result.append(current)
                if (current == '\\') {
                    source.getOrNull(index + 1)?.let(result::append)
                    index += 2
                } else {
                    if (current == '"') quoted = false
                    index++
                }
            }
            character -> {
                result.append(current)
                if (current == '\\') {
                    source.getOrNull(index + 1)?.let(result::append)
                    index += 2
                } else {
                    if (current == '\'') character = false
                    index++
                }
            }
            current == '/' && next == '/' -> {
                appendBlank(current)
                appendBlank(next)
                lineComment = true
                index += 2
            }
            current == '/' && next == '*' -> {
                appendBlank(current)
                appendBlank(next)
                blockCommentDepth = 1
                index += 2
            }
            current == '"' && source.startsWith("\"\"\"", index) -> {
                result.append("\"\"\"")
                rawString = true
                index += 3
            }
            current == '"' -> {
                result.append(current)
                quoted = true
                index++
            }
            current == '\'' -> {
                result.append(current)
                character = true
                index++
            }
            else -> {
                result.append(current)
                index++
            }
        }
    }
    return result.toString()
}

internal fun stripCommentsAndStringLiterals(source: String): String {
    return stripStringLiterals(stripCommentsPreservingLiterals(source))
}

internal fun stripStringLiterals(withoutComments: String): String {
    val result = StringBuilder(withoutComments.length)
    var quoted = false
    var character = false
    var rawString = false
    var index = 0

    fun appendBlank(value: Char) {
        result.append(if (value == '\n' || value == '\r') value else ' ')
    }

    while (index < withoutComments.length) {
        val current = withoutComments[index]
        when {
            rawString -> {
                appendBlank(current)
                if (current == '"' && withoutComments.startsWith("\"\"\"", index)) {
                    appendBlank(withoutComments[index + 1])
                    appendBlank(withoutComments[index + 2])
                    rawString = false
                    index += 3
                } else {
                    index++
                }
            }
            quoted -> {
                appendBlank(current)
                if (current == '\\') {
                    withoutComments.getOrNull(index + 1)?.let(::appendBlank)
                    index += 2
                } else {
                    if (current == '"') quoted = false
                    index++
                }
            }
            character -> {
                appendBlank(current)
                if (current == '\\') {
                    withoutComments.getOrNull(index + 1)?.let(::appendBlank)
                    index += 2
                } else {
                    if (current == '\'') character = false
                    index++
                }
            }
            current == '"' && withoutComments.startsWith("\"\"\"", index) -> {
                appendBlank(current)
                appendBlank(withoutComments[index + 1])
                appendBlank(withoutComments[index + 2])
                rawString = true
                index += 3
            }
            current == '"' -> {
                appendBlank(current)
                quoted = true
                index++
            }
            current == '\'' -> {
                appendBlank(current)
                character = true
                index++
            }
            else -> {
                result.append(current)
                index++
            }
        }
    }
    return result.toString()
}
