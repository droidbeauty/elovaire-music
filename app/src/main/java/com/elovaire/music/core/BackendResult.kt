package elovaire.music.droidbeauty.app.core

internal sealed interface BackendResult<out T, out E> {
    data class Success<T>(val value: T) : BackendResult<T, Nothing>
    data class Failure<E>(val error: E) : BackendResult<Nothing, E>
}
