package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.ui.i18n.*

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.settings.elovaireSettingsDataStore
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.albumOfDayTitle
import elovaire.music.droidbeauty.app.ui.i18n.homeCopy
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal enum class HomeModule { LastPlayed, RecentlyAdded, RecentlyPlayed, AlbumOfDay, FavoriteAlbums }
internal val defaultHomeLayout = listOf(HomeModule.LastPlayed, HomeModule.RecentlyAdded, HomeModule.RecentlyPlayed, HomeModule.AlbumOfDay, HomeModule.FavoriteAlbums)
private val homeLayoutKey = stringPreferencesKey("home_module_order")

internal fun parseHomeLayout(raw: String?): List<HomeModule> {
    val movable = raw.orEmpty().split(',').mapNotNull { name -> HomeModule.entries.firstOrNull { it.name == name } }
        .filterNot { it == HomeModule.LastPlayed }.distinct().toMutableList()
    defaultHomeLayout.filterNot { it == HomeModule.LastPlayed }.forEach { if (it !in movable) movable.add(it) }
    movable.add(0, HomeModule.LastPlayed)
    return movable
}

internal fun moveHomeModule(order: List<HomeModule>, module: HomeModule, direction: Int): List<HomeModule> {
    if (module == HomeModule.LastPlayed || direction == 0) return order
    val movable = order.filterNot { it == HomeModule.LastPlayed }.toMutableList()
    val from = movable.indexOf(module)
    if (from < 0) return order
    val to = (from + direction.coerceIn(-1, 1)).coerceIn(0, movable.lastIndex)
    if (from == to) return order
    movable.add(to, movable.removeAt(from))
    movable.add(0, HomeModule.LastPlayed)
    return movable
}

internal fun homeLayoutTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Catalan -> CatalanTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Finnish -> FinnishTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Romanian -> RomanianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Bosnian -> BosnianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Albanian -> AlbanianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Croatian -> CroatianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Czech -> CzechTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Danish -> DanishTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Dutch -> DutchTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Bengali -> BengaliTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Estonian -> EstonianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Polish -> PolishTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.German -> GermanTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.French -> FrenchTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Spanish -> SpanishTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Greek -> GreekTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Hindi -> HindiTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Hungarian -> HungarianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Italian -> ItalianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Japanese -> JapaneseTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Korean -> KoreanTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Latvian -> LatvianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Malay -> MalayTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Russian -> RussianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Slovak -> SlovakTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Serbian -> SerbianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Swedish -> SwedishTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Thai -> ThaiTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.Urdu -> UrduTranslations.homeLayoutScreen_homeLayoutTitle_1
    AppLanguage.English -> EnglishTranslations.homeLayoutScreen_homeLayoutTitle_1
}

internal fun homeLayoutSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Catalan -> CatalanTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Finnish -> FinnishTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Romanian -> RomanianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Bosnian -> BosnianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Albanian -> AlbanianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Croatian -> CroatianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Czech -> CzechTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Danish -> DanishTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Dutch -> DutchTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Bengali -> BengaliTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Estonian -> EstonianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Polish -> PolishTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.German -> GermanTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.French -> FrenchTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Spanish -> SpanishTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Greek -> GreekTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Hindi -> HindiTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Hungarian -> HungarianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Italian -> ItalianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Japanese -> JapaneseTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Korean -> KoreanTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Latvian -> LatvianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Malay -> MalayTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Russian -> RussianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Slovak -> SlovakTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Serbian -> SerbianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Swedish -> SwedishTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Thai -> ThaiTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.Urdu -> UrduTranslations.homeLayoutScreen_homeLayoutSubtitle_1
    AppLanguage.English -> EnglishTranslations.homeLayoutScreen_homeLayoutSubtitle_1
}

internal data class HomeLayoutEditorCopy(val hint: String, val lastPlayed: String, val fixed: String, val up: String, val down: String, val reorder: String)

internal fun homeLayoutEditorCopy(language: AppLanguage): HomeLayoutEditorCopy = when (language) {
    AppLanguage.Estonian -> EstonianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Greek -> GreekTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Hindi -> HindiTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Latvian -> LatvianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Malay -> MalayTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Thai -> ThaiTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Czech -> CzechTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Danish -> DanishTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Dutch -> DutchTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Italian -> ItalianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Korean -> KoreanTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Russian -> RussianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Slovak -> SlovakTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Swedish -> SwedishTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Catalan -> CatalanTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Finnish -> FinnishTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Romanian -> RomanianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Bengali -> BengaliTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Croatian -> CroatianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Serbian -> SerbianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Urdu -> UrduTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Polish -> PolishTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.German -> GermanTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.French -> FrenchTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.Spanish -> SpanishTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
    AppLanguage.English -> EnglishTranslations.homeLayoutScreen_homeLayoutEditorCopy_1
}

@Composable
internal fun rememberHomeLayout(): List<HomeModule> {
    val context = LocalContext.current.applicationContext
    val flow = remember(context) {
        context.elovaireSettingsDataStore().data.catch { failure ->
            if (failure is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw failure
        }.map { parseHomeLayout(it[homeLayoutKey]) }
    }
    return flow.collectAsStateWithLifecycle(initialValue = defaultHomeLayout).value
}

@Composable
internal fun HomeLayoutScreen(bottomPadding: Dp, onBack: () -> Unit) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { homeLayoutEditorCopy(language) }
    val persisted = rememberHomeLayout()
    val context = LocalContext.current.applicationContext
    val scope = LocalLifecycleOwner.current.lifecycleScope
    val listState = rememberElovaireLazyListState("home_layout_editor")
    val specs = rememberMotionSpecs()
    var order by remember { mutableStateOf(persisted) }
    var dragged by remember { mutableStateOf<HomeModule?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveRevision by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    LaunchedEffect(persisted, dragged, saving) { if (dragged == null && !saving) order = persisted }
    fun save(next: List<HomeModule>) {
        order = next
        saving = true
        val revision = ++saveRevision
        scope.launch {
            try {
                context.elovaireSettingsDataStore().edit { it[homeLayoutKey] = next.joinToString(",") { module -> module.name } }
            } catch (failure: IOException) {
                Log.w("HomeLayout", "Unable to save home layout", failure)
            } finally { if (revision == saveRevision) saving = false }
        }
    }
    val threshold = with(LocalDensity.current) { 55.dp.toPx() }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = topBarOccupiedHeight() + 20.dp, bottom = bottomPadding + 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item(key = "hint") {
                Text(copy.hint, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = readableSecondaryTextColor(), textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))
            }
            items(order, key = { it.name }) { module ->
                val movable = module != HomeModule.LastPlayed
                val active = dragged == module
                val scale by animateFloatAsState(if (active) 1.12f else 1f, specs.spring(), label = "home_layout_handle_scale")
                val alpha by animateFloatAsState(if (active) 1f else 0.5f, specs.tween(), label = "home_layout_handle_alpha")
                val title = when (module) {
                    HomeModule.LastPlayed -> copy.lastPlayed
                    HomeModule.RecentlyAdded -> miscPhrase(language, MiscPhrase.RecentlyAdded)
                    HomeModule.RecentlyPlayed -> homeCopy(language).recentlyPlayedSongsTitle
                    HomeModule.AlbumOfDay -> albumOfDayTitle(language)
                    HomeModule.FavoriteAlbums -> homeCopy(language).favoriteAlbumsTitle
                }
                var distance by remember(module) { mutableFloatStateOf(0f) }
                Column(modifier = Modifier
                    .zIndex(if (active) 1f else 0f)
                    .animateItem(placementSpec = if (active) null else specs.listPlacement())
                    .graphicsLayer { translationY = if (active) distance else 0f }
                    .then(
                    if (!movable) Modifier else Modifier.semantics {
                        customActions = listOf(
                            CustomAccessibilityAction(copy.up) { save(moveHomeModule(order, module, -1)); true },
                            CustomAccessibilityAction(copy.down) { save(moveHomeModule(order, module, 1)); true },
                        )
                    }.pointerInput(module, threshold) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragged = module; distance = 0f },
                            onDragEnd = { val next = order; dragged = null; distance = 0f; save(next) },
                            onDragCancel = { val next = order; dragged = null; distance = 0f; save(next) },
                        ) { change, delta ->
                            change.consume()
                            distance += delta.y
                            val layout = listState.layoutInfo
                            val row = layout.visibleItemsInfo.firstOrNull { it.key == module.name }
                            if (row != null) {
                                val center = row.offset + row.size / 2f + distance
                                val edge = threshold
                                if ((delta.y < 0f && center < layout.viewportStartOffset + edge) ||
                                    (delta.y > 0f && center > layout.viewportEndOffset - edge)
                                ) {
                                    scope.launch { listState.scrollBy(delta.y.coerceIn(-edge / 3f, edge / 3f)) }
                                }
                            }
                            if (kotlin.math.abs(distance) >= threshold) {
                                val direction = if (distance > 0f) 1 else -1
                                order = moveHomeModule(order, module, direction)
                                distance -= direction * threshold
                            }
                        }
                    },
                )) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 18.dp).graphicsLayer { if (!movable) this.alpha = 0.5f }, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            if (!movable) Text(copy.fixed, style = MaterialTheme.typography.labelLarge, color = readableSecondaryTextColor())
                        }
                        if (movable) Icon(painterResource(R.drawable.ic_home_layout_reorder), contentDescription = "${copy.reorder} $title",
                            modifier = Modifier.size(24.dp).graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha })
                    }
                    if (module != order.last()) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            DividerLine(Modifier.fillMaxWidth(0.9f))
                        }
                    }
                }
            }
        }
        PinnedBackTopBar(title = homeLayoutTitle(language), onBack = onBack, modifier = Modifier.align(Alignment.TopCenter))
    }
}
