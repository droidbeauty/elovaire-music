package elovaire.music.droidbeauty.app.data.settings

internal sealed interface PersistenceFailure {
    data class MigrationFailed(val store: String, val cause: Throwable?) : PersistenceFailure
    data class CorruptLegacyValue(val key: String) : PersistenceFailure
    data class ReadFailed(val store: String, val cause: Throwable?) : PersistenceFailure
    data class WriteFailed(val store: String, val cause: Throwable?) : PersistenceFailure
}
