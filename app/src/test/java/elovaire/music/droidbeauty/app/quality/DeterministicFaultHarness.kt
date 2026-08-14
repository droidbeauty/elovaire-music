package elovaire.music.droidbeauty.app.quality

/** Test-only failure policy used by backend tests; it has no production global state. */
internal class DeterministicFaultHarness {
    private val rules = linkedMapOf<String, Rule>()

    fun failNext(operation: String, cause: Throwable = IllegalStateException(operation)) {
        rules[operation] = Rule.Remaining(1, cause)
    }

    fun failOnCall(operation: String, call: Int, cause: Throwable = IllegalStateException(operation)) {
        require(call > 0)
        rules[operation] = Rule.Remaining(call, cause)
    }

    fun malformed(operation: String) {
        rules[operation] = Rule.Malformed
    }

    fun <T> invoke(operation: String, block: () -> T): T {
        when (val rule = rules[operation]) {
            is Rule.Remaining -> {
                if (rule.callsRemaining == 1) {
                    rules.remove(operation)
                    throw rule.cause
                }
                rules[operation] = rule.copy(callsRemaining = rule.callsRemaining - 1)
            }
            Rule.Malformed -> error("Malformed result requested for $operation")
            null -> Unit
        }
        return block()
    }

    private sealed interface Rule {
        data class Remaining(val callsRemaining: Int, val cause: Throwable) : Rule
        data object Malformed : Rule
    }
}

