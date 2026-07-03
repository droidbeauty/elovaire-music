package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.privacySafetyCopy
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.theme.AboutCardButtonAccent
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ChangelogScreen(
    releases: List<ChangelogRelease>,
    onBack: () -> Unit,
) {
    val listState = rememberLazyListState()
    val release = remember(releases) {
        releases.firstOrNull { it.version == BuildConfig.VERSION_NAME } ?: releases.firstOrNull()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                bottom = navigationBarInsetDp() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.changelog_header),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(354.dp),
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = miscPhrase(LocalAppLanguage.current, MiscPhrase.WhatsNew),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Surface(
                        shape = RoundedCornerShape(ElovaireRadii.pill),
                        color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
                            Color.White.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        },
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                    ModuleCard {
                        ChangelogReleaseContent(
                            release = release,
                            contentHorizontalPadding = 2.dp,
                        )
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = settingsCopy(LocalAppLanguage.current).changelog,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
internal fun ChangelogBottomSheetOverlay(
    releases: List<ChangelogRelease>,
    onDismiss: () -> Unit,
) {
    val listState = rememberLazyListState()
    val release = remember(releases) {
        releases.firstOrNull { it.version == BuildConfig.VERSION_NAME } ?: releases.firstOrNull()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        DynamicBackdropSurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            overlayAlpha = 0.6f,
            borderColor = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = miscPhrase(LocalAppLanguage.current, MiscPhrase.WhatsNew),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Surface(
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ) {
                            Text(
                                text = BuildConfig.VERSION_NAME,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_x),
                            contentDescription = "Close changelog",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = navigationBarInsetDp() + 18.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(ElovaireRadii.card))
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        LazyColumn(
                            state = listState,
                            overscrollEffect = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .ensureSingleItemRubberBand(listState),
                            contentPadding = PaddingValues(
                                top = 18.dp,
                                bottom = 18.dp,
                            ),
                        ) {
                            item {
                                ChangelogReleaseContent(
                                    release = release,
                                    contentHorizontalPadding = 20.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChangelogReleaseContent(
    release: ChangelogRelease?,
    contentHorizontalPadding: Dp = 20.dp,
) {
    val changes = release?.changes?.filter { it.isNotBlank() }.orEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = contentHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (changes.isEmpty()) {
            Text(
                text = "No changelog entries yet",
                style = MaterialTheme.typography.bodyLarge,
                color = readableSecondaryTextColor(),
            )
        } else {
            changes.forEachIndexed { index, change ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = change,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (index != changes.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        )
                    }
                }
                if (index != changes.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
internal fun AboutScreen(
    onBack: () -> Unit,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val aboutModel = remember(context) { context.loadAboutScreenModel() }
    val listState = rememberElovaireLazyListState("about_screen")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.topBarToFirstContentGap,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            itemsIndexed(aboutModel.sections, key = { index, section -> "${section.title}_$index" }) { index, section ->
                AboutSectionCard(
                    section = section,
                    renderOnBackground = index == 0,
                    showEntryLogo = index == 0,
                )
            }
        }
        PinnedBackTopBar(
            title = uiPhrase(language, UiPhrase.About),
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
internal fun PrivacySafetyScreen(
    onBack: () -> Unit,
    bottomPadding: Dp,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) {
        privacySafetyCopy(
            language = language,
        )
    }
    val listState = rememberElovaireLazyListState("privacy_safety_screen")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.topBarToFirstContentGap,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            itemsIndexed(copy.sections, key = { index, section -> "${section.title}_$index" }) { index, section ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (index != copy.sections.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        )
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = copy.title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun AboutSectionCard(
    section: AboutSection,
    renderOnBackground: Boolean = false,
    showEntryLogo: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            section.entries.forEachIndexed { index, entry ->
                AboutEntryBlock(
                    entry = entry,
                    horizontalScrollableLinks = renderOnBackground,
                    useCardAccentButtons = !renderOnBackground,
                    useRoseAccentButtons = renderOnBackground,
                    showLogo = showEntryLogo && index == 0,
                )
                if (index != section.entries.lastIndex) {
                    DividerLine()
                }
            }
        }
    }
    if (renderOnBackground) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
        ) {
            content()
        }
    } else {
        ModuleCard {
            content()
        }
    }
}

@Composable
private fun AboutEntryBlock(
    entry: AboutEntry,
    horizontalScrollableLinks: Boolean = false,
    useCardAccentButtons: Boolean = false,
    useRoseAccentButtons: Boolean = false,
    showLogo: Boolean = false,
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (showLogo) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AboutEntryLogo(
                    logoUri = entry.logoUri,
                    title = entry.title,
                )
                AboutEntryTextStack(
                    entry = entry,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            AboutEntryTextStack(entry = entry)
        }
        if (entry.links.isNotEmpty()) {
            if (horizontalScrollableLinks) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    entry.links.forEach { link ->
                        AboutLinkPill(
                            link = link,
                            useCardAccent = useCardAccentButtons,
                            useRoseAccent = useRoseAccentButtons,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    entry.links.forEach { link ->
                        Box(modifier = Modifier.weight(1f)) {
                            AboutLinkPill(
                                link = link,
                                useCardAccent = useCardAccentButtons,
                                useRoseAccent = useRoseAccentButtons,
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutEntryTextStack(
    entry: AboutEntry,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = localizedAboutTitle(entry.title, language),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(22f)),
            color = MaterialTheme.colorScheme.onSurface,
        )
        entry.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = localizedAboutDescription(entry.title, description, language),
                style = secondaryBodyTextStyle(),
                color = readableSecondaryTextColor(),
            )
        }
    }
}

@Composable
private fun AboutEntryLogo(
    logoUri: String?,
    title: String,
) {
    val context = LocalContext.current
    val drawableRes = remember(context, logoUri) {
        context.resolveAboutLogoDrawableRes(logoUri)
    }
    val remoteBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = logoUri?.trim()?.let(aboutLogoImageCache::get),
        key1 = logoUri,
        key2 = drawableRes,
    ) {
        val source = logoUri?.trim()?.takeIf { it.isNotBlank() } ?: return@produceState
        if (drawableRes != null) return@produceState
        aboutLogoImageCache[source]?.let {
            value = it
            return@produceState
        }
        value = null
        value = withContext(Dispatchers.IO) {
            runCatching {
                when {
                    source.startsWith("http://", ignoreCase = true) ||
                        source.startsWith("https://", ignoreCase = true) -> {
                        URL(source).openConnection().run {
                            connectTimeout = 2_500
                            readTimeout = 2_500
                            getInputStream().use { BitmapFactory.decodeStream(it) }
                        }
                    }

                    else -> context.contentResolver.openInputStream(Uri.parse(source))?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }?.asImageBitmap()?.also { bitmap ->
                    aboutLogoImageCache[source] = bitmap
                }
            }.getOrNull()
        }
    }
    val uri = remember(logoUri, drawableRes, remoteBitmap) {
        logoUri
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { drawableRes == null && remoteBitmap == null }
            ?.let(Uri::parse)
    }
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            drawableRes != null -> {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            remoteBitmap != null -> {
                Image(
                    bitmap = remoteBitmap!!,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            else -> {
                ArtworkImage(
                    uri = uri,
                    title = title,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 30.dp,
                    requestedSizePx = 160,
                )
            }
        }
    }
}

private fun Context.resolveAboutLogoDrawableRes(logoUri: String?): Int? {
    val source = logoUri?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val drawableName = when {
        source.startsWith("@drawable/") -> source.substringAfter("@drawable/")
        source.startsWith("drawable/") -> source.substringAfter("drawable/")
        source.startsWith("android.resource://") && "/drawable/" in source -> source.substringAfterLast("/drawable/")
        else -> null
    }
        ?.substringBefore('?')
        ?.substringBefore('#')
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: return null
    return resources.getIdentifier(drawableName, "drawable", packageName)
        .takeIf { it != 0 }
}

@Composable
private fun AboutLinkPill(
    link: AboutLink,
    useCardAccent: Boolean = false,
    useRoseAccent: Boolean = false,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val containerColor = when {
        useRoseAccent -> RoseAccent.copy(alpha = 0.72f)
        useCardAccent -> AboutCardButtonAccent
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
    }
    val contentColor = if (containerColor.luminance() > 0.42f) InkText else Color.White
    Surface(
        modifier = Modifier,
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = aboutIconForUrl(link.url)),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = localizedAboutLinkLabel(link.label, language),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@DrawableRes
private fun aboutIconForUrl(url: String): Int {
    val normalizedUrl = url.lowercase()
    return when {
        "instagram.com" in normalizedUrl -> R.drawable.ic_about_instagram
        "twitter.com" in normalizedUrl || "x.com" in normalizedUrl -> R.drawable.ic_about_twitter
        "github.com" in normalizedUrl -> R.drawable.ic_about_github
        "ko-fi.com" in normalizedUrl || "kofi.com" in normalizedUrl -> R.drawable.ic_about_coffee
        else -> R.drawable.ic_about_globe
    }
}

private fun localizedAboutTitle(
    title: String,
    language: AppLanguage,
): String = when (title) {
    "Droid Beauty" -> title
    "Elovaire" -> title
    "Resources" -> when (language) {
        AppLanguage.Polish -> "Zasoby"
        AppLanguage.ChineseSimplified -> "资源"
        AppLanguage.Croatian -> "Resursi"
        AppLanguage.Czech -> "Zdroje"
        AppLanguage.French -> "Ressources"
        AppLanguage.German -> "Ressourcen"
        AppLanguage.Italian -> "Risorse"
        AppLanguage.Japanese -> "リソース"
        AppLanguage.Korean -> "리소스"
        AppLanguage.Malay -> "Sumber"
        AppLanguage.Portuguese -> "Recursos"
        AppLanguage.Russian -> "Ресурсы"
        AppLanguage.Slovak -> "Zdroje"
        AppLanguage.Spanish -> "Recursos"
        AppLanguage.Ukrainian -> "Ресурси"
        AppLanguage.Bengali -> "রিসোর্স"
        AppLanguage.Urdu -> "وسائل"
        else -> title
    }
    else -> title
}

private fun localizedAboutDescription(
    title: String,
    description: String,
    language: AppLanguage,
): String = when (title) {
    "Droid Beauty" -> when (language) {
        AppLanguage.Polish -> "Minimalnie zaprojektowane aplikacje i doświadczenia dla piękniejszego Androida"
        AppLanguage.ChineseSimplified -> "以极简设计打造更美好的 Android 应用与体验"
        AppLanguage.Croatian -> "Minimalno dizajnirane aplikacije i iskustva za ljepši Android"
        AppLanguage.Czech -> "Minimalisticky navržené aplikace a zážitky pro krásnější Android"
        AppLanguage.French -> "Des applications et expériences au design minimal pour embellir Android"
        AppLanguage.German -> "Minimal gestaltete Apps und Erlebnisse für ein schöneres Android"
        AppLanguage.Italian -> "App ed esperienze dal design minimale per rendere Android più bello"
        AppLanguage.Japanese -> "Android をより美しくする、ミニマルに設計されたアプリと体験"
        AppLanguage.Korean -> "더 아름다운 Android를 위한 미니멀한 앱과 경험"
        AppLanguage.Malay -> "Aplikasi dan pengalaman berreka bentuk minimal untuk Android yang lebih indah"
        AppLanguage.Portuguese -> "Apps e experiências de design minimal para tornar o Android mais bonito"
        AppLanguage.Russian -> "Минималистичные приложения и впечатления для более красивого Android"
        AppLanguage.Slovak -> "Minimalisticky navrhnuté aplikácie a zážitky pre krajší Android"
        AppLanguage.Spanish -> "Apps y experiencias de diseño minimalista para hacer Android más bello"
        AppLanguage.Ukrainian -> "Мінімалістично створені застосунки й враження для красивішого Android"
        AppLanguage.Bengali -> "আরও সুন্দর Android-এর জন্য ন্যূনতম নকশার অ্যাপ ও অভিজ্ঞতা"
        AppLanguage.Urdu -> "زیادہ خوبصورت Android کے لیے کم سے کم انداز میں ڈیزائن کردہ ایپس اور تجربات"
        else -> description
    }
    "Elovaire" -> when (language) {
        AppLanguage.Polish -> "Elegancki odtwarzacz offline stworzony z myślą o Twojej lokalnej muzyce"
        AppLanguage.ChineseSimplified -> "为你的本地音乐打造的优雅离线播放器"
        AppLanguage.Croatian -> "Elegantni offline reproduktor stvoren za vašu lokalnu glazbu"
        AppLanguage.Czech -> "Elegantní offline přehrávač vytvořený pro vaši místní hudbu"
        AppLanguage.French -> "Un lecteur hors ligne élégant conçu pour votre musique locale"
        AppLanguage.German -> "Ein eleganter Offline-Player für deine lokale Musik"
        AppLanguage.Italian -> "Un player offline elegante creato per la tua musica locale"
        AppLanguage.Japanese -> "ローカル音楽のために作られた、エレガントなオフラインプレーヤー"
        AppLanguage.Korean -> "로컬 음악을 위해 만들어진 우아한 오프라인 플레이어"
        AppLanguage.Malay -> "Pemain luar talian yang elegan untuk muzik tempatan anda"
        AppLanguage.Portuguese -> "Um reprodutor offline elegante feito para a sua música local"
        AppLanguage.Russian -> "Элегантный офлайн-плеер для вашей локальной музыки"
        AppLanguage.Slovak -> "Elegantný offline prehrávač vytvorený pre vašu lokálnu hudbu"
        AppLanguage.Spanish -> "Un reproductor sin conexión elegante hecho para tu música local"
        AppLanguage.Ukrainian -> "Елегантний офлайн-програвач для вашої локальної музики"
        AppLanguage.Bengali -> "আপনার লোকাল সঙ্গীতের জন্য তৈরি একটি মার্জিত অফলাইন প্লেয়ার"
        AppLanguage.Urdu -> "آپ کی مقامی موسیقی کے لیے بنایا گیا ایک نفیس آف لائن پلیئر"
        else -> description
    }
    "Resources" -> when (language) {
        AppLanguage.Polish -> "Projekty, narzędzia i biblioteki, które pomagają tworzyć Elovaire"
        AppLanguage.ChineseSimplified -> "帮助打造 Elovaire 的项目、工具和库"
        AppLanguage.Croatian -> "Projekti, alati i biblioteke koji pomažu stvarati Elovaire"
        AppLanguage.Czech -> "Projekty, nástroje a knihovny, které pomáhají tvořit Elovaire"
        AppLanguage.French -> "Projets, outils et bibliothèques qui aident à créer Elovaire"
        AppLanguage.German -> "Projekte, Werkzeuge und Bibliotheken, die Elovaire ermöglichen"
        AppLanguage.Italian -> "Progetti, strumenti e librerie che aiutano a creare Elovaire"
        AppLanguage.Japanese -> "Elovaire の制作を支えるプロジェクト、ツール、ライブラリ"
        AppLanguage.Korean -> "Elovaire를 만드는 데 도움을 주는 프로젝트, 도구, 라이브러리"
        AppLanguage.Malay -> "Projek, alat dan pustaka yang membantu membina Elovaire"
        AppLanguage.Portuguese -> "Projetos, ferramentas e bibliotecas que ajudam a criar o Elovaire"
        AppLanguage.Russian -> "Проекты, инструменты и библиотеки, которые помогают создавать Elovaire"
        AppLanguage.Slovak -> "Projekty, nástroje a knižnice, ktoré pomáhajú vytvárať Elovaire"
        AppLanguage.Spanish -> "Proyectos, herramientas y bibliotecas que ayudan a crear Elovaire"
        AppLanguage.Ukrainian -> "Проєкти, інструменти та бібліотеки, що допомагають створювати Elovaire"
        AppLanguage.Bengali -> "Elovaire তৈরিতে সহায়ক প্রকল্প, টুল এবং লাইব্রেরি"
        AppLanguage.Urdu -> "وہ منصوبے، اوزار اور لائبریریاں جو Elovaire بنانے میں مدد کرتی ہیں"
        else -> description
    }
    else -> description
}

private fun localizedAboutLinkLabel(
    label: String,
    language: AppLanguage,
): String = when (label.lowercase()) {
    "website", "play store" -> when (language) {
        AppLanguage.Polish -> if (label.equals("Play Store", true)) "Sklep Play" else "Strona"
        AppLanguage.ChineseSimplified -> if (label.equals("Play Store", true)) "Play 商店" else "网站"
        AppLanguage.Croatian -> if (label.equals("Play Store", true)) "Play trgovina" else "Web"
        AppLanguage.Czech -> if (label.equals("Play Store", true)) "Obchod Play" else "Web"
        AppLanguage.French -> if (label.equals("Play Store", true)) "Play Store" else "Site web"
        AppLanguage.German -> if (label.equals("Play Store", true)) "Play Store" else "Website"
        AppLanguage.Italian -> if (label.equals("Play Store", true)) "Play Store" else "Sito web"
        AppLanguage.Japanese -> if (label.equals("Play Store", true)) "Play ストア" else "ウェブサイト"
        AppLanguage.Korean -> if (label.equals("Play Store", true)) "Play 스토어" else "웹사이트"
        AppLanguage.Malay -> if (label.equals("Play Store", true)) "Play Store" else "Laman web"
        AppLanguage.Portuguese -> if (label.equals("Play Store", true)) "Play Store" else "Site"
        AppLanguage.Russian -> if (label.equals("Play Store", true)) "Play Маркет" else "Сайт"
        AppLanguage.Slovak -> if (label.equals("Play Store", true)) "Obchod Play" else "Web"
        AppLanguage.Spanish -> if (label.equals("Play Store", true)) "Play Store" else "Sitio web"
        AppLanguage.Ukrainian -> if (label.equals("Play Store", true)) "Play Маркет" else "Сайт"
        AppLanguage.Bengali -> if (label.equals("Play Store", true)) "Play Store" else "ওয়েবসাইট"
        AppLanguage.Urdu -> if (label.equals("Play Store", true)) "پلے اسٹور" else "ویب سائٹ"
        else -> label
    }
    "twitter" -> if (language == AppLanguage.Japanese) "X" else label
    "instagram", "github", "ko-fi" -> label
    else -> label
}
