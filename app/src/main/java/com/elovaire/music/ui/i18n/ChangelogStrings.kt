package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import java.util.Locale

private const val UI_CHANGES = "UI tweaks in NAS configuration, tag editing system and settings layout"
private const val SETUP_CHANGES = "UI tweaks on the initial setup splash screen"
private const val NEW_LANGUAGES = "Added support for 11 new languages - Albanian, Bosnian, Bulgarian, Catalan, Finnish, Indonesian, Romanian, Serbian, Slovenian, Punjabi and Urdu"
private const val BUG_FIXES = "General bug fixes"

private data class ChangelogCopy(val ui: String, val languages: String, val fixes: String)

internal fun localizedChangelogItem(item: String, language: AppLanguage): String {
    if (item == SETUP_CHANGES) return localizedSetupChangelogItem(language)
    if (language == AppLanguage.English || (item != UI_CHANGES && item != NEW_LANGUAGES && item != BUG_FIXES)) return item
    val copy = when (language) {
        AppLanguage.Albanian -> ChangelogCopy("Përmirësime të pamjes së konfigurimit NAS, redaktimit të etiketave dhe cilësimeve", "U shtua mbështetja për 11 gjuhë të reja", "Rregullime të përgjithshme të gabimeve")
        AppLanguage.Bengali -> ChangelogCopy("NAS কনফিগারেশন, ট্যাগ সম্পাদনা ও সেটিংসের বিন্যাসে পরিবর্তন", "১১টি নতুন ভাষার সমর্থন যোগ করা হয়েছে", "সাধারণ ত্রুটি সংশোধন")
        AppLanguage.Bosnian -> ChangelogCopy("Dorade izgleda NAS postavki, uređivanja oznaka i rasporeda postavki", "Dodana podrška za 11 novih jezika", "Opće ispravke grešaka")
        AppLanguage.Bulgarian -> ChangelogCopy("Промени в интерфейса за NAS, редактиране на етикети и настройките", "Добавена е поддръжка за 11 нови езика", "Общи поправки на грешки")
        AppLanguage.Catalan -> ChangelogCopy("Retocs de la interfície de configuració NAS, edició d’etiquetes i paràmetres", "S’ha afegit compatibilitat amb 11 idiomes nous", "Correccions generals d’errors")
        AppLanguage.ChineseSimplified -> ChangelogCopy("优化 NAS 配置、标签编辑和设置界面的布局", "新增对 11 种语言的支持", "常规错误修复")
        AppLanguage.Croatian -> ChangelogCopy("Dorade izgleda NAS postavki, uređivanja oznaka i rasporeda postavki", "Dodana podrška za 11 novih jezika", "Opći ispravci grešaka")
        AppLanguage.Czech -> ChangelogCopy("Úpravy vzhledu nastavení NAS, úpravy značek a rozložení nastavení", "Přidána podpora 11 nových jazyků", "Obecné opravy chyb")
        AppLanguage.Danish -> ChangelogCopy("Justeringer af NAS-konfiguration, tagredigering og indstillingernes layout", "Understøttelse af 11 nye sprog er tilføjet", "Generelle fejlrettelser")
        AppLanguage.Dutch -> ChangelogCopy("Aanpassingen aan NAS-configuratie, tagbewerking en de indelingen van instellingen", "Ondersteuning voor 11 nieuwe talen toegevoegd", "Algemene foutoplossingen")
        AppLanguage.English -> error("English changelog entries are returned unchanged")
        AppLanguage.Estonian -> ChangelogCopy("NAS-i seadistuse, siltide muutmise ja sätete paigutuse täiustused", "Lisatud 11 uue keele tugi", "Üldised veaparandused")
        AppLanguage.Finnish -> ChangelogCopy("NAS-asetusten, tunnisteiden muokkauksen ja asetusten asettelun käyttöliittymämuutoksia", "Lisätty tuki 11 uudelle kielelle", "Yleisiä virhekorjauksia")
        AppLanguage.French -> ChangelogCopy("Ajustements de la configuration NAS, de l’édition des tags et de la présentation des paramètres", "Prise en charge de 11 nouvelles langues", "Corrections générales de bugs")
        AppLanguage.German -> ChangelogCopy("Anpassungen an NAS-Konfiguration, Tag-Bearbeitung und Einstellungslayout", "Unterstützung für 11 neue Sprachen hinzugefügt", "Allgemeine Fehlerbehebungen")
        AppLanguage.Greek -> ChangelogCopy("Βελτιώσεις στην εμφάνιση της ρύθμισης NAS, της επεξεργασίας ετικετών και των ρυθμίσεων", "Προστέθηκε υποστήριξη για 11 νέες γλώσσες", "Γενικές διορθώσεις σφαλμάτων")
        AppLanguage.Hindi -> ChangelogCopy("NAS कॉन्फ़िगरेशन, टैग संपादन और सेटिंग के लेआउट में सुधार", "11 नई भाषाओं के लिए समर्थन जोड़ा गया", "सामान्य त्रुटियों में सुधार")
        AppLanguage.Hungarian -> ChangelogCopy("A NAS-beállítások, a címkeszerkesztő és a beállítások elrendezésének finomítása", "11 új nyelv támogatása", "Általános hibajavítások")
        AppLanguage.Indonesian -> ChangelogCopy("Penyempurnaan tampilan konfigurasi NAS, pengeditan tag, dan tata letak pengaturan", "Menambahkan dukungan untuk 11 bahasa baru", "Perbaikan bug umum")
        AppLanguage.Italian -> ChangelogCopy("Ritocchi all’interfaccia di configurazione NAS, modifica dei tag e impostazioni", "Aggiunto il supporto per 11 nuove lingue", "Correzioni generali di errori")
        AppLanguage.Japanese -> ChangelogCopy("NAS 設定、タグ編集、設定画面のレイアウトを調整", "新たに11言語に対応", "全般的な不具合を修正")
        AppLanguage.Korean -> ChangelogCopy("NAS 설정, 태그 편집 및 설정 화면 레이아웃 개선", "새로운 언어 11개 지원 추가", "일반 버그 수정")
        AppLanguage.Latvian -> ChangelogCopy("NAS konfigurācijas, tagu rediģēšanas un iestatījumu izkārtojuma uzlabojumi", "Pievienots atbalsts 11 jaunām valodām", "Vispārīgi kļūdu labojumi")
        AppLanguage.Lithuanian -> ChangelogCopy("Patobulinta NAS konfigūracijos, žymų redagavimo ir nustatymų išvaizda", "Pridėtas 11 naujų kalbų palaikymas", "Bendri klaidų taisymai")
        AppLanguage.Macedonian -> ChangelogCopy("Подобрувања на изгледот на NAS поставките, уредувањето ознаки и поставките", "Додадена е поддршка за 11 нови јазици", "Општи поправки на грешки")
        AppLanguage.Malay -> ChangelogCopy("Penambahbaikan rupa konfigurasi NAS, penyuntingan tag dan susun atur tetapan", "Sokongan untuk 11 bahasa baharu ditambah", "Pembaikan pepijat umum")
        AppLanguage.Norwegian -> ChangelogCopy("Justeringer av NAS-konfigurasjon, taggredigering og oppsettet for innstillinger", "Støtte for 11 nye språk er lagt til", "Generelle feilrettinger")
        AppLanguage.Polish -> ChangelogCopy("Poprawki wyglądu konfiguracji NAS, edytora tagów i układu ustawień", "Dodano obsługę 11 nowych języków", "Ogólne poprawki błędów")
        AppLanguage.Portuguese -> ChangelogCopy("Ajustes na configuração NAS, na edição de etiquetas e na disposição das definições", "Adicionado suporte para 11 novos idiomas", "Correções gerais de erros")
        AppLanguage.Punjabi -> ChangelogCopy("NAS ਸੰਰਚਨਾ, ਟੈਗ ਸੋਧ ਅਤੇ ਸੈਟਿੰਗਾਂ ਦੇ ਖਾਕੇ ਵਿੱਚ ਸੁਧਾਰ", "11 ਨਵੀਆਂ ਭਾਸ਼ਾਵਾਂ ਲਈ ਸਹਾਇਤਾ ਜੋੜੀ ਗਈ", "ਆਮ ਗਲਤੀਆਂ ਠੀਕ ਕੀਤੀਆਂ")
        AppLanguage.Romanian -> ChangelogCopy("Ajustări ale interfeței pentru configurarea NAS, editarea etichetelor și setări", "A fost adăugat suport pentru 11 limbi noi", "Remedieri generale de erori")
        AppLanguage.Russian -> ChangelogCopy("Улучшен интерфейс настройки NAS, редактирования тегов и раздела настроек", "Добавлена поддержка 11 новых языков", "Общие исправления ошибок")
        AppLanguage.Serbian -> ChangelogCopy("Дораде изгледа NAS подешавања, уређивања ознака и распореда подешавања", "Додата подршка за 11 нових језика", "Опште исправке грешака")
        AppLanguage.Slovak -> ChangelogCopy("Úpravy vzhľadu nastavení NAS, úpravy značiek a rozloženia nastavení", "Pridaná podpora 11 nových jazykov", "Všeobecné opravy chýb")
        AppLanguage.Slovenian -> ChangelogCopy("Izboljšave videza nastavitev NAS, urejanja oznak in razporeditve nastavitev", "Dodana podpora za 11 novih jezikov", "Splošni popravki napak")
        AppLanguage.Spanish -> ChangelogCopy("Ajustes de la interfaz de configuración NAS, edición de etiquetas y ajustes", "Se ha añadido compatibilidad con 11 idiomas nuevos", "Correcciones generales de errores")
        AppLanguage.Swedish -> ChangelogCopy("Justeringar av NAS-konfiguration, taggredigering och inställningarnas layout", "Stöd för 11 nya språk har lagts till", "Allmänna felrättningar")
        AppLanguage.Thai -> ChangelogCopy("ปรับแต่งหน้าตาการตั้งค่า NAS การแก้ไขแท็ก และการจัดวางการตั้งค่า", "เพิ่มการรองรับภาษาใหม่ 11 ภาษา", "แก้ไขข้อผิดพลาดทั่วไป")
        AppLanguage.Ukrainian -> ChangelogCopy("Удосконалено вигляд налаштування NAS, редагування тегів і розділу налаштувань", "Додано підтримку 11 нових мов", "Загальні виправлення помилок")
        AppLanguage.Urdu -> ChangelogCopy("NAS کی ترتیب، ٹیگ میں ترمیم اور ترتیبات کے لے آؤٹ کی ظاہری بہتری", "11 نئی زبانوں کی معاونت شامل کی گئی", "عمومی خرابیوں کی اصلاح")
    }
    return when (item) {
        UI_CHANGES -> copy.ui
        BUG_FIXES -> copy.fixes
        else -> {
            val locale = language.appLocale()
            val separator = if (language == AppLanguage.Japanese || language == AppLanguage.ChineseSimplified) "、" else ", "
            val names = listOf("sq", "bs", "bg", "ca", "fi", "id", "ro", "sr", "sl", "pa", "ur")
                .joinToString(separator) { Locale.forLanguageTag(it).getDisplayLanguage(locale) }
            "${copy.languages}: $names"
        }
    }
}

private fun localizedSetupChangelogItem(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Përmirësime të pamjes së ekranit të konfigurimit fillestar"
    AppLanguage.Bengali -> "প্রাথমিক সেটআপ স্ক্রিনের চেহারায় পরিবর্তন"
    AppLanguage.Bosnian -> "Dorade izgleda početnog ekrana za postavljanje"
    AppLanguage.Bulgarian -> "Промени във външния вид на екрана за първоначална настройка"
    AppLanguage.Catalan -> "Retocs de la pantalla de configuració inicial"
    AppLanguage.ChineseSimplified -> "调整初始设置界面的外观"
    AppLanguage.Croatian -> "Dorade izgleda početnog zaslona za postavljanje"
    AppLanguage.Czech -> "Úpravy vzhledu obrazovky úvodního nastavení"
    AppLanguage.Danish -> "Justeringer af skærmen til første opsætning"
    AppLanguage.Dutch -> "Aanpassingen aan het scherm voor de eerste installatie"
    AppLanguage.English -> SETUP_CHANGES
    AppLanguage.Estonian -> "Algseadistuse kuva välimuse täiustused"
    AppLanguage.Finnish -> "Alkuasetusten näytön ulkoasun muutoksia"
    AppLanguage.French -> "Ajustements de l’écran de configuration initiale"
    AppLanguage.German -> "Anpassungen am Bildschirm für die Ersteinrichtung"
    AppLanguage.Greek -> "Βελτιώσεις στην εμφάνιση της οθόνης αρχικής ρύθμισης"
    AppLanguage.Hindi -> "शुरुआती सेटअप स्क्रीन के रूप में सुधार"
    AppLanguage.Hungarian -> "A kezdeti beállítás képernyőjének finomítása"
    AppLanguage.Indonesian -> "Penyempurnaan tampilan layar pengaturan awal"
    AppLanguage.Italian -> "Ritocchi alla schermata di configurazione iniziale"
    AppLanguage.Japanese -> "初期設定画面のデザインを調整"
    AppLanguage.Korean -> "초기 설정 화면 디자인 개선"
    AppLanguage.Latvian -> "Sākotnējās iestatīšanas ekrāna izskata uzlabojumi"
    AppLanguage.Lithuanian -> "Patobulinta pradinės sąrankos ekrano išvaizda"
    AppLanguage.Macedonian -> "Подобрувања на изгледот на екранот за почетно поставување"
    AppLanguage.Malay -> "Penambahbaikan rupa skrin persediaan awal"
    AppLanguage.Norwegian -> "Justeringer av skjermen for førstegangsoppsett"
    AppLanguage.Polish -> "Poprawki wyglądu ekranu konfiguracji początkowej"
    AppLanguage.Portuguese -> "Ajustes no ecrã de configuração inicial"
    AppLanguage.Punjabi -> "ਸ਼ੁਰੂਆਤੀ ਸੈਟਅੱਪ ਸਕ੍ਰੀਨ ਦੀ ਦਿੱਖ ਵਿੱਚ ਸੁਧਾਰ"
    AppLanguage.Romanian -> "Ajustări ale aspectului ecranului de configurare inițială"
    AppLanguage.Russian -> "Улучшен внешний вид экрана первоначальной настройки"
    AppLanguage.Serbian -> "Дораде изгледа екрана за почетно подешавање"
    AppLanguage.Slovak -> "Úpravy vzhľadu obrazovky úvodného nastavenia"
    AppLanguage.Slovenian -> "Izboljšave videza zaslona za začetno nastavitev"
    AppLanguage.Spanish -> "Ajustes en la pantalla de configuración inicial"
    AppLanguage.Swedish -> "Justeringar av skärmen för första konfigurationen"
    AppLanguage.Thai -> "ปรับแต่งหน้าตาหน้าจอการตั้งค่าเริ่มต้น"
    AppLanguage.Ukrainian -> "Удосконалено вигляд екрана початкового налаштування"
    AppLanguage.Urdu -> "ابتدائی سیٹ اپ اسکرین کی ظاہری بہتری"
}

internal fun AppLanguage.appLocale(): Locale = Locale.forLanguageTag(when (this) {
    AppLanguage.Albanian -> "sq"; AppLanguage.Bengali -> "bn"; AppLanguage.Bosnian -> "bs"
    AppLanguage.Bulgarian -> "bg"; AppLanguage.Catalan -> "ca"; AppLanguage.ChineseSimplified -> "zh-CN"
    AppLanguage.Croatian -> "hr"; AppLanguage.Czech -> "cs"; AppLanguage.Danish -> "da"; AppLanguage.Dutch -> "nl"
    AppLanguage.English -> "en"; AppLanguage.Estonian -> "et"; AppLanguage.Finnish -> "fi"; AppLanguage.French -> "fr"
    AppLanguage.German -> "de"; AppLanguage.Greek -> "el"; AppLanguage.Hindi -> "hi"; AppLanguage.Hungarian -> "hu"
    AppLanguage.Indonesian -> "id"; AppLanguage.Italian -> "it"; AppLanguage.Japanese -> "ja"; AppLanguage.Korean -> "ko"
    AppLanguage.Latvian -> "lv"; AppLanguage.Lithuanian -> "lt"; AppLanguage.Macedonian -> "mk"
    AppLanguage.Malay -> "ms"; AppLanguage.Norwegian -> "nb"; AppLanguage.Polish -> "pl"; AppLanguage.Portuguese -> "pt"
    AppLanguage.Punjabi -> "pa"; AppLanguage.Romanian -> "ro"; AppLanguage.Russian -> "ru"; AppLanguage.Serbian -> "sr"
    AppLanguage.Slovak -> "sk"; AppLanguage.Slovenian -> "sl"; AppLanguage.Spanish -> "es"; AppLanguage.Swedish -> "sv"
    AppLanguage.Thai -> "th"; AppLanguage.Ukrainian -> "uk"; AppLanguage.Urdu -> "ur"
})
