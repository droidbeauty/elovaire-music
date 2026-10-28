package elovaire.music.droidbeauty.app.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkBitmap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.settings.albumOfDayId
import elovaire.music.droidbeauty.app.data.settings.elovaireSettingsDataStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.albumOfDayTitle
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import elovaire.music.droidbeauty.app.ui.motion.elovaireContentChangeMotion
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.MutedSectionHeader
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import java.io.IOException
import java.time.LocalDate
import kotlin.random.Random

@Composable
internal fun rememberAlbumOfDay(albums: List<Album>, isLibraryAuthoritative: Boolean): Album? {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    var date by remember { mutableStateOf(LocalDate.now()) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    DisposableEffect(context, lifecycleOwner) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                date = LocalDate.now()
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) date = LocalDate.now()
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            context.unregisterReceiver(receiver)
        }
    }
    val albumIds = remember(albums) { albums.map(Album::id) }
    LaunchedEffect(albumIds, date, isLibraryAuthoritative) {
        // Don't replace the saved choice with an incomplete library during a scan.
        if (isLibraryAuthoritative) {
            selectedId = try {
                context.elovaireSettingsDataStore().albumOfDayId(albumIds, date)
            } catch (failure: IOException) {
                Log.w("AlbumOfDay", "Unable to persist the daily album", failure)
                albumIds.takeIf { it.isNotEmpty() }?.random(Random(date.toEpochDay()))
            }
        }
    }
    return remember(albums, selectedId) { albums.firstOrNull { it.id == selectedId } }
}

@Composable
internal fun AlbumOfDayModule(
    album: Album,
    onOpen: (ExpandOrigin) -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val screenSize = screenContainerSizePx()
    var bounds by remember { mutableStateOf<Rect?>(null) }
    val open = { onOpen(bounds.toExpandOrigin(screenSize.width.toFloat(), screenSize.height.toFloat())) }
    val artwork = rememberArtworkBitmap(album.artUri, size = 512).value
    val metadata = remember(album.songs) {
        listOfNotNull(
            album.songs.firstNotNullOfOrNull { it.releaseYear }?.toString(),
            album.songs.firstOrNull { it.genre.isNotBlank() && it.genre != "Unknown Genre" }?.genre,
        ).joinToString("  •  ")
    }
    ModuleCard(
        modifier = modifier
            .elovaireContentChangeMotion(album.id)
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .elovaireClickable(onClick = open),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box {
        if (artwork != null) {
            Image(
                bitmap = artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = 0.4f), 0.5f to Color.White.copy(alpha = 0.1f), 0.65f to Color.Transparent), blendMode = BlendMode.DstIn)
                    }.blur(40.dp),
            )
        }
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MutedSectionHeader(
                title = albumOfDayTitle(language),
                iconResId = R.drawable.ic_lucide_disc_3,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(
                    uri = album.artUri,
                    title = album.title,
                    modifier = Modifier.size(100.dp),
                    cornerRadius = ElovaireRadii.artwork,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap),
                ) {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = readableSecondaryTextColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (metadata.isNotEmpty()) Text(
                        text = metadata,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = readableSecondaryTextColor(),
                    )
                }
                AlbumHeaderActionButton(
                    iconResId = R.drawable.ic_lucide_play,
                    contentDescription = rootUiCopy(language).playAlbum,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    iconSize = 18.dp,
                    onClick = onPlay,
                )
            }
        }
        }
    }
}
