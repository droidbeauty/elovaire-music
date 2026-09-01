internal fun normalizeGuardrailPath(path: String): String = path.replace('\\', '/')

internal fun isGuardrailPathAllowed(path: String, allowedSuffixes: Collection<String>): Boolean {
    val normalizedPath = normalizeGuardrailPath(path)
    return allowedSuffixes.any { normalizedPath.endsWith(normalizeGuardrailPath(it)) }
}
