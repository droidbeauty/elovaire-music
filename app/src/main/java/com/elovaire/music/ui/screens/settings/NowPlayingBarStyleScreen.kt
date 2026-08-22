package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.NowPlayingBarStyle
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.nowPlayingBarStyleCopy
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.InkText

@Composable
internal fun NowPlayingBarStyleScreen(
    selectedStyle: NowPlayingBarStyle,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onStyleSelected: (NowPlayingBarStyle) -> Unit,
) {
    val listState = remember { LazyListState() }
    val copy = nowPlayingBarStyleCopy(LocalAppLanguage.current)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                end = 18.dp,
                bottom = bottomPadding + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                StyleOptionCard(
                    title = copy.floating,
                    subtitle = "A floating player above the navigation bar",
                    style = NowPlayingBarStyle.Floating,
                    selectedStyle = selectedStyle,
                    onClick = { onStyleSelected(NowPlayingBarStyle.Floating) },
                )
            }
            item {
                StyleOptionCard(
                    title = copy.compact,
                    subtitle = "An edge-to-edge player directly above the navigation bar",
                    style = NowPlayingBarStyle.Compact,
                    selectedStyle = selectedStyle,
                    onClick = { onStyleSelected(NowPlayingBarStyle.Compact) },
                )
            }
        }
        PinnedBackTopBar(
            title = copy.title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 8.dp,
            bottomInset = bottomPadding + buttonNavigationScrollBoost(),
        )
    }
}

@Composable
private fun StyleOptionCard(
    title: String,
    subtitle: String,
    style: NowPlayingBarStyle,
    selectedStyle: NowPlayingBarStyle,
    onClick: () -> Unit,
) {
    val selected = style == selectedStyle
    val shape = RoundedCornerShape(ElovaireRadii.module)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
            NowPlayingStylePreview(style = style)
        }
    }
}

@Composable
private fun NowPlayingStylePreview(style: NowPlayingBarStyle) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val previewBackground = if (darkTheme) Color(0xFF080B0B) else Color(0xFFE9ECEC)
    val previewSurface = if (darkTheme) Color(0xFF202424) else Color.White
    val previewText = if (darkTheme) Color.White else InkText
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(previewBackground),
    ) {
        if (style == NowPlayingBarStyle.Floating) {
            PreviewBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp),
                surface = previewSurface,
                textColor = previewText,
                rounded = true,
            )
        } else {
            PreviewBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                surface = previewSurface,
                textColor = previewText,
                rounded = false,
            )
        }
    }
}

@Composable
private fun PreviewBar(
    modifier: Modifier,
    surface: Color,
    textColor: Color,
    rounded: Boolean,
) {
    val shape = if (rounded) RoundedCornerShape(12.dp) else RoundedCornerShape(0.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(shape)
            .background(surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(textColor.copy(alpha = 0.78f)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(textColor.copy(alpha = 0.38f)),
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_play),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
