internal fun normalizeGuardrailPath(path: String): String = path.replace('\\', '/')

internal fun isGuardrailPathAllowed(path: String, allowedSuffixes: Collection<String>): Boolean {
    val normalizedPath = normalizeGuardrailPath(path)
    return allowedSuffixes.any { normalizedPath.endsWith(normalizeGuardrailPath(it)) }
}

internal fun coreImportsUi(path: String, source: String): Boolean {
    return "/core/" in normalizeGuardrailPath(path) &&
        Regex("(?m)^import\\s+elovaire[.]music[.]droidbeauty[.]app[.]ui[.]").containsMatchIn(source)
}
