package elovaire.music.droidbeauty.app.ui.screens

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
    AppLanguage.Bulgarian -> "Оформление на началната страница"
    AppLanguage.Catalan -> "Disseny de la pàgina inicial"
    AppLanguage.Finnish -> "Etusivun asettelu"
    AppLanguage.Romanian -> "Aspectul paginii principale"
    AppLanguage.Slovenian -> "Postavitev domače strani"
    AppLanguage.Bosnian -> "Raspored početne stranice"
    AppLanguage.Indonesian -> "Tata letak halaman utama"
    AppLanguage.Punjabi -> "ਮੁੱਖ ਪੰਨੇ ਦਾ ਖਾਕਾ"
    AppLanguage.Albanian -> "Pamja e faqes kryesore"
    AppLanguage.ChineseSimplified -> "首页布局"
    AppLanguage.Croatian -> "Raspored početne stranice"
    AppLanguage.Czech -> "Rozložení domovské stránky"
    AppLanguage.Danish -> "Startsidens layout"
    AppLanguage.Dutch -> "Indeling van startpagina"
    AppLanguage.Bengali -> "হোম পৃষ্ঠার বিন্যাস"
    AppLanguage.Estonian -> "Avalehe paigutus"
    AppLanguage.Polish -> "Układ ekranu głównego"
    AppLanguage.German -> "Startseitenlayout"
    AppLanguage.French -> "Disposition de l’accueil"
    AppLanguage.Spanish -> "Diseño de inicio"
    AppLanguage.Greek -> "Διάταξη αρχικής σελίδας"
    AppLanguage.Hindi -> "होम पेज का लेआउट"
    AppLanguage.Hungarian -> "Kezdőlap elrendezése"
    AppLanguage.Italian -> "Layout della pagina iniziale"
    AppLanguage.Japanese -> "ホーム画面のレイアウト"
    AppLanguage.Korean -> "홈 화면 레이아웃"
    AppLanguage.Latin -> "Dispositio paginae initialis"
    AppLanguage.Latvian -> "Sākumlapas izkārtojums"
    AppLanguage.Lithuanian -> "Pradžios puslapio išdėstymas"
    AppLanguage.Malay -> "Susun atur halaman utama"
    AppLanguage.Macedonian -> "Распоред на почетната страница"
    AppLanguage.Norwegian -> "Oppsett av startsiden"
    AppLanguage.Portuguese -> "Esquema da página inicial"
    AppLanguage.Russian -> "Расположение главного экрана"
    AppLanguage.Slovak -> "Rozloženie domovskej stránky"
    AppLanguage.Serbian -> "Распоред почетне странице"
    AppLanguage.Swedish -> "Startsidelayout"
    AppLanguage.Thai -> "การจัดวางหน้าหลัก"
    AppLanguage.Ukrainian -> "Розташування головного екрана"
    AppLanguage.Urdu -> "ہوم صفحے کی ترتیب"
    AppLanguage.English -> "Home page layout"
}

internal fun homeLayoutSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> "Променете изгледа на началния екран"
    AppLanguage.Catalan -> "Personalitza l’aspecte de la pantalla principal"
    AppLanguage.Finnish -> "Mukauta aloitusnäytön ulkoasua"
    AppLanguage.Romanian -> "Personalizează aspectul ecranului principal"
    AppLanguage.Slovenian -> "Prilagodite videz začetnega zaslona"
    AppLanguage.Bosnian -> "Prilagodite izgled početnog ekrana"
    AppLanguage.Indonesian -> "Sesuaikan tampilan halaman utama"
    AppLanguage.Punjabi -> "ਮੁੱਖ ਸਕ੍ਰੀਨ ਦੀ ਦਿੱਖ ਬਦਲੋ"
    AppLanguage.Albanian -> "Përshtat pamjen e ekranit kryesor"
    AppLanguage.ChineseSimplified -> "自定义主屏幕的外观"
    AppLanguage.Croatian -> "Prilagodite izgled glavnog zaslona"
    AppLanguage.Czech -> "Upravte vzhled hlavní obrazovky"
    AppLanguage.Danish -> "Tilpas hovedskærmens udseende"
    AppLanguage.Dutch -> "Pas de weergave van het hoofdscherm aan"
    AppLanguage.Bengali -> "মূল পর্দার চেহারা সাজান"
    AppLanguage.Estonian -> "Kohanda põhikuva välimust"
    AppLanguage.Polish -> "Dostosuj wygląd ekranu głównego"
    AppLanguage.German -> "Das Aussehen der Startseite anpassen"
    AppLanguage.French -> "Personnaliser l’écran d’accueil"
    AppLanguage.Spanish -> "Personaliza la pantalla de inicio"
    AppLanguage.Greek -> "Προσαρμόστε την εμφάνιση της κύριας οθόνης"
    AppLanguage.Hindi -> "मुख्य स्क्रीन का रूप बदलें"
    AppLanguage.Hungarian -> "A főképernyő megjelenésének testreszabása"
    AppLanguage.Italian -> "Personalizza l’aspetto della schermata principale"
    AppLanguage.Japanese -> "メイン画面の表示をカスタマイズ"
    AppLanguage.Korean -> "메인 화면의 모습을 설정합니다"
    AppLanguage.Latin -> "Speciem paginae principalis compone"
    AppLanguage.Latvian -> "Pielāgojiet galvenā ekrāna izskatu"
    AppLanguage.Lithuanian -> "Tinkinkite pagrindinio ekrano išvaizdą"
    AppLanguage.Malay -> "Sesuaikan rupa skrin utama"
    AppLanguage.Macedonian -> "Приспособете го изгледот на главниот екран"
    AppLanguage.Norwegian -> "Tilpass utseendet på hovedskjermen"
    AppLanguage.Portuguese -> "Personalize o aspeto do ecrã principal"
    AppLanguage.Russian -> "Настройте вид главного экрана"
    AppLanguage.Slovak -> "Upravte vzhľad hlavnej obrazovky"
    AppLanguage.Serbian -> "Прилагодите изглед главног екрана"
    AppLanguage.Swedish -> "Anpassa huvudskärmens utseende"
    AppLanguage.Thai -> "ปรับแต่งรูปลักษณ์ของหน้าจอหลัก"
    AppLanguage.Ukrainian -> "Налаштуйте вигляд головного екрана"
    AppLanguage.Urdu -> "مرکزی اسکرین کی شکل حسبِ ضرورت بنائیں"
    AppLanguage.English -> "Adjust how the main screen looks"
}

private data class HomeLayoutEditorCopy(val hint: String, val lastPlayed: String, val fixed: String, val up: String, val down: String, val reorder: String)

private fun homeLayoutEditorCopy(language: AppLanguage): HomeLayoutEditorCopy = when (language) {
    AppLanguage.Estonian -> HomeLayoutEditorCopy("Hoia jaotist all ja lohista selle järjestuse muutmiseks", "Viimati esitatud", "Fikseeritud asukoht", "Liiguta üles", "Liiguta alla", "Muuda järjestust")
    AppLanguage.Greek -> HomeLayoutEditorCopy("Κρατήστε πατημένη μια ενότητα και σύρετέ την για αλλαγή σειράς", "Τελευταία αναπαραγωγή", "Σταθερή θέση", "Μετακίνηση πάνω", "Μετακίνηση κάτω", "Αλλαγή σειράς")
    AppLanguage.Hindi -> HomeLayoutEditorCopy("क्रम बदलने के लिए किसी अनुभाग को दबाकर खींचें", "पिछली बार चलाया गया", "निश्चित स्थान", "ऊपर ले जाएँ", "नीचे ले जाएँ", "क्रम बदलें")
    AppLanguage.Hungarian -> HomeLayoutEditorCopy("Tarts lenyomva egy szakaszt, majd húzd a kívánt helyre", "Legutóbb lejátszott", "Rögzített hely", "Mozgatás fel", "Mozgatás le", "Sorrend módosítása")
    AppLanguage.Latin -> HomeLayoutEditorCopy("Sectionem preme et trahe ut ordinem mutes", "Novissime auditum", "Locus fixus", "Sursum move", "Deorsum move", "Ordinem muta")
    AppLanguage.Latvian -> HomeLayoutEditorCopy("Turi sadaļu un velc to, lai mainītu secību", "Pēdējoreiz atskaņotais", "Fiksēta pozīcija", "Pārvietot augšup", "Pārvietot lejup", "Mainīt secību")
    AppLanguage.Lithuanian -> HomeLayoutEditorCopy("Palaikykite skiltį ir vilkite, kad pakeistumėte jos vietą", "Paskutinį kartą leista", "Fiksuota vieta", "Perkelti aukštyn", "Perkelti žemyn", "Keisti tvarką")
    AppLanguage.Malay -> HomeLayoutEditorCopy("Sentuh dan tahan bahagian, kemudian seret untuk mengubah susunan", "Terakhir dimainkan", "Kedudukan tetap", "Alih ke atas", "Alih ke bawah", "Susun semula")
    AppLanguage.Macedonian -> HomeLayoutEditorCopy("Задржете дел и повлечете го за да го промените редоследот", "Последно пуштено", "Фиксна положба", "Помести нагоре", "Помести надолу", "Промени редослед")
    AppLanguage.Thai -> HomeLayoutEditorCopy("แตะส่วนค้างไว้แล้วลากเพื่อเปลี่ยนลำดับ", "เล่นล่าสุด", "ตำแหน่งคงที่", "เลื่อนขึ้น", "เลื่อนลง", "จัดลำดับใหม่")
    AppLanguage.ChineseSimplified -> HomeLayoutEditorCopy("长按并拖动模块以调整顺序", "上次播放", "固定位置", "上移", "下移", "调整顺序")
    AppLanguage.Czech -> HomeLayoutEditorCopy("Podržte část a přetažením změňte její pořadí", "Naposledy přehrané", "Pevná pozice", "Přesunout nahoru", "Přesunout dolů", "Změnit pořadí")
    AppLanguage.Danish -> HomeLayoutEditorCopy("Hold et afsnit nede, og træk det for at ændre rækkefølgen", "Senest afspillet", "Fast placering", "Flyt op", "Flyt ned", "Omarranger")
    AppLanguage.Dutch -> HomeLayoutEditorCopy("Houd een onderdeel vast en sleep om de volgorde te wijzigen", "Laatst afgespeeld", "Vaste positie", "Omhoog", "Omlaag", "Volgorde wijzigen")
    AppLanguage.Italian -> HomeLayoutEditorCopy("Tieni premuta una sezione e trascinala per riordinarla", "Ultima riproduzione", "Posizione fissa", "Sposta su", "Sposta giù", "Riordina")
    AppLanguage.Japanese -> HomeLayoutEditorCopy("セクションを長押ししてドラッグすると並べ替えられます", "最後に再生", "固定位置", "上へ移動", "下へ移動", "並べ替え")
    AppLanguage.Korean -> HomeLayoutEditorCopy("항목을 길게 누른 뒤 드래그하여 순서를 바꾸세요", "최근 재생", "고정 위치", "위로 이동", "아래로 이동", "순서 변경")
    AppLanguage.Norwegian -> HomeLayoutEditorCopy("Hold på en seksjon og dra den for å endre rekkefølgen", "Sist spilt", "Fast plassering", "Flytt opp", "Flytt ned", "Endre rekkefølge")
    AppLanguage.Portuguese -> HomeLayoutEditorCopy("Mantenha uma secção premida e arraste-a para alterar a ordem", "Última reprodução", "Posição fixa", "Mover para cima", "Mover para baixo", "Reordenar")
    AppLanguage.Russian -> HomeLayoutEditorCopy("Удерживайте раздел и перетащите его, чтобы изменить порядок", "Последнее воспроизведение", "Закреплённая позиция", "Переместить вверх", "Переместить вниз", "Изменить порядок")
    AppLanguage.Slovak -> HomeLayoutEditorCopy("Podržte sekciu a presunutím zmeňte jej poradie", "Naposledy prehrané", "Pevná pozícia", "Presunúť nahor", "Presunúť nadol", "Zmeniť poradie")
    AppLanguage.Swedish -> HomeLayoutEditorCopy("Håll ned en sektion och dra den för att ändra ordningen", "Senast spelat", "Fast position", "Flytta upp", "Flytta ned", "Ändra ordning")
    AppLanguage.Ukrainian -> HomeLayoutEditorCopy("Утримуйте розділ і перетягніть його, щоб змінити порядок", "Востаннє відтворено", "Фіксована позиція", "Перемістити вгору", "Перемістити вниз", "Змінити порядок")
    AppLanguage.Bulgarian -> HomeLayoutEditorCopy("Задръжте и плъзнете раздел, за да го преместите", "Последно възпроизведено", "Фиксирана позиция", "Премести нагоре", "Премести надолу", "Промени реда")
    AppLanguage.Catalan -> HomeLayoutEditorCopy("Mantén premuda una secció i arrossega-la per reordenar-la", "Última reproducció", "Posició fixa", "Mou amunt", "Mou avall", "Reordena")
    AppLanguage.Finnish -> HomeLayoutEditorCopy("Pidä osiota painettuna ja järjestä se vetämällä", "Viimeksi soitettu", "Kiinteä sijainti", "Siirrä ylös", "Siirrä alas", "Järjestä")
    AppLanguage.Romanian -> HomeLayoutEditorCopy("Ține apăsată o secțiune și trage-o pentru a o muta", "Redat ultima dată", "Poziție fixă", "Mută în sus", "Mută în jos", "Reordonează")
    AppLanguage.Slovenian -> HomeLayoutEditorCopy("Pridržite razdelek in ga povlecite na drugo mesto", "Nazadnje predvajano", "Fiksen položaj", "Premakni navzgor", "Premakni navzdol", "Spremeni vrstni red")
    AppLanguage.Albanian -> HomeLayoutEditorCopy("Mbaje shtypur një seksion dhe tërhiqe për ta zhvendosur", "Dëgjuar së fundi", "Pozicion i fiksuar", "Zhvendos lart", "Zhvendos poshtë", "Rendit")
    AppLanguage.Bengali -> HomeLayoutEditorCopy("ক্রম বদলাতে একটি অংশ চেপে ধরে টেনে নিন", "সর্বশেষ চালানো", "স্থির অবস্থান", "উপরে সরান", "নিচে সরান", "ক্রম বদলান")
    AppLanguage.Croatian -> HomeLayoutEditorCopy("Pritisnite i zadržite odjeljak pa ga povucite na novo mjesto", "Posljednje reproducirano", "Fiksni položaj", "Premjesti gore", "Premjesti dolje", "Promijeni redoslijed")
    AppLanguage.Serbian -> HomeLayoutEditorCopy("Притисните и задржите одељак, па га превуците на ново место", "Последње пуштено", "Фиксни положај", "Помери горе", "Помери доле", "Промени редослед")
    AppLanguage.Urdu -> HomeLayoutEditorCopy("ترتیب بدلنے کے لیے حصے کو دبائے رکھیں اور کھینچیں", "آخری بار چلایا گیا", "مقررہ جگہ", "اوپر لے جائیں", "نیچے لے جائیں", "ترتیب بدلیں")
    AppLanguage.Bosnian -> HomeLayoutEditorCopy("Zadržite i povucite odjeljak da mu promijenite položaj", "Zadnje slušano", "Fiksna pozicija", "Pomjeri gore", "Pomjeri dolje", "Promijeni redoslijed")
    AppLanguage.Indonesian -> HomeLayoutEditorCopy("Sentuh dan tahan bagian, lalu geser untuk mengurutkannya", "Terakhir diputar", "Posisi tetap", "Pindah ke atas", "Pindah ke bawah", "Ubah urutan")
    AppLanguage.Punjabi -> HomeLayoutEditorCopy("ਕ੍ਰਮ ਬਦਲਣ ਲਈ ਭਾਗ ਨੂੰ ਦਬਾਈ ਰੱਖ ਕੇ ਖਿੱਚੋ", "ਪਿਛਲੀ ਵਾਰ ਚਲਾਇਆ", "ਪੱਕੀ ਥਾਂ", "ਉੱਪਰ ਲਿਜਾਓ", "ਹੇਠਾਂ ਲਿਜਾਓ", "ਕ੍ਰਮ ਬਦਲੋ")
    AppLanguage.Polish -> HomeLayoutEditorCopy("Przytrzymaj sekcję i przeciągnij, aby zmienić jej położenie", "Ostatni album lub playlista", "Stała pozycja", "Przenieś w górę", "Przenieś w dół", "Zmień położenie")
    AppLanguage.German -> HomeLayoutEditorCopy("Halte einen Abschnitt gedrückt und ziehe ihn an eine neue Position", "Letztes Album oder letzte Playlist", "Feste Position", "Nach oben", "Nach unten", "Anordnen")
    AppLanguage.French -> HomeLayoutEditorCopy("Maintenez une section puis faites-la glisser pour la déplacer", "Dernière lecture", "Position fixe", "Monter", "Descendre", "Déplacer")
    AppLanguage.Spanish -> HomeLayoutEditorCopy("Mantén pulsada una sección y arrástrala para cambiar su orden", "Última reproducción", "Posición fija", "Subir", "Bajar", "Mover")
    AppLanguage.English -> HomeLayoutEditorCopy("Touch and hold a section, then drag to reorder it", "Last played", "Fixed position", "Move up", "Move down", "Reorder")
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
