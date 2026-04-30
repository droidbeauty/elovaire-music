# Lyrics Pipeline

Elovaire resolves lyrics through one shared `LyricsService` so the player and lyrics overlay use a single lookup path and a single cache.

## Resolution order

1. In-memory cache
2. Embedded synced lyrics
3. Embedded plain lyrics
4. Sidecar `.lrc`
5. Sidecar `.txt`
6. Remote lrclib direct match
7. Remote lrclib search fallback

The service emits the first valid local result immediately and only touches the network when local sources miss.

## Cache behavior

- Successful lookups are cached without expiry for the current process lifetime.
- Confirmed not-found results use a moderate TTL.
- Offline and timeout misses use shorter TTLs so we do not poison the cache after transient failures.
- Requests for the same track are coalesced so only one lookup runs at a time.

## Matching

Remote matching scores normalized artist, title, album, synced availability, and duration proximity. Low-confidence matches are rejected to avoid showing lyrics for the wrong song.

## Cancellation and staleness

- Each lookup is keyed by a stable track cache key.
- Duplicate lookups share the same in-flight request.
- The UI cancels obsolete in-flight requests when the queue focus changes.
- Cached lyrics remain available when the user leaves and re-enters the player or lyrics UI.

## Parsing support

- Standard LRC timestamps: `mm:ss.xx`, `mm:ss.xxx`, `hh:mm:ss.xx`
- Multiple timestamps on a single line
- LRC metadata tags: `ar`, `ti`, `al`, `by`, `offset`
- Embedded MP3 `USLT` and `SYLT`
- Embedded FLAC/Vorbis `LYRICS`, `SYNCEDLYRICS`, and related comment fields
- UTF-8 BOM, UTF-16 LE/BE BOM, CRLF and LF line endings

## Performance notes

- Disk, parsing, and network work run off the main thread.
- Local sources are resolved before remote lookup.
- Remote lookup uses short timeouts and multiple query variants.
- Debug builds log total resolution time, local time, remote time, cache hits, and source.
