package elovaire.music.droidbeauty.app.ui.screens.tags

internal enum class TagValidationKind(val english: String) {
    AlbumTitle("Album title cannot be empty"),
    AlbumArtist("Album artist cannot be empty"),
    ReleaseYear("Release year must be between 1 and 9999"),
    TrackNumbers("track and disc numbers must be between 1 and 9999"),
    TrackTitle("track title cannot be empty"),
    TrackArtist("track artist cannot be empty"),
    BookTitle("Book title cannot be empty"),
    Author("Author cannot be empty"),
    ChapterTitle("chapter title cannot be empty"),
    ChapterAuthor("author cannot be empty"),
}

internal data class TagValidationIssue(val kind: TagValidationKind, val fileName: String? = null)
