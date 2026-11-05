package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.screens.AlbumSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode

private data class SortNames(val song: String, val artist: String, val album: String, val albumName: String)

private fun sortNames(language: AppLanguage): SortNames = when (language) {
    AppLanguage.Albanian -> SortNames("Emri i këngës", "Emri i artistit", "Albumi", "Emri i albumit")
    AppLanguage.Bosnian -> SortNames("Naziv pjesme", "Ime izvođača", "Album", "Naziv albuma")
    AppLanguage.Bulgarian -> SortNames("Име на песента", "Име на изпълнителя", "Албум", "Име на албума")
    AppLanguage.Catalan -> SortNames("Nom de la cançó", "Nom de l’artista", "Àlbum", "Nom de l’àlbum")
    AppLanguage.Finnish -> SortNames("Kappaleen nimi", "Esittäjän nimi", "Albumi", "Albumin nimi")
    AppLanguage.Romanian -> SortNames("Numele melodiei", "Numele artistului", "Album", "Numele albumului")
    AppLanguage.Slovenian -> SortNames("Ime skladbe", "Ime izvajalca", "Album", "Ime albuma")
    AppLanguage.Indonesian -> SortNames("Nama lagu", "Nama artis", "Album", "Nama album")
    AppLanguage.Punjabi -> SortNames("ਗੀਤ ਦਾ ਨਾਮ", "ਕਲਾਕਾਰ ਦਾ ਨਾਮ", "ਐਲਬਮ", "ਐਲਬਮ ਦਾ ਨਾਮ")
    AppLanguage.Bengali -> SortNames("গানের নাম", "শিল্পীর নাম", "অ্যালবাম", "অ্যালবামের নাম")
    AppLanguage.ChineseSimplified -> SortNames("歌曲名", "艺人名", "专辑", "专辑名")
    AppLanguage.Croatian -> SortNames("Naziv pjesme", "Ime izvođača", "Album", "Naziv albuma")
    AppLanguage.Czech -> SortNames("Název skladby", "Jméno umělce", "Album", "Název alba")
    AppLanguage.Danish -> SortNames("Sangtitel", "Kunstnernavn", "Album", "Albumtitel")
    AppLanguage.Dutch -> SortNames("Nummernaam", "Artiestennaam", "Album", "Albumnaam")
    AppLanguage.English -> SortNames("Song name", "Artist name", "Album", "Album name")
    AppLanguage.Estonian -> SortNames("Loo nimi", "Artisti nimi", "Album", "Albumi nimi")
    AppLanguage.French -> SortNames("Titre du morceau", "Nom de l’artiste", "Album", "Titre de l’album")
    AppLanguage.German -> SortNames("Songname", "Künstlername", "Album", "Albumname")
    AppLanguage.Greek -> SortNames("Τίτλος τραγουδιού", "Όνομα καλλιτέχνη", "Άλμπουμ", "Τίτλος άλμπουμ")
    AppLanguage.Hindi -> SortNames("गाने का नाम", "कलाकार का नाम", "एल्बम", "एल्बम का नाम")
    AppLanguage.Hungarian -> SortNames("Szám címe", "Előadó neve", "Album", "Album címe")
    AppLanguage.Italian -> SortNames("Titolo del brano", "Nome artista", "Album", "Titolo dell’album")
    AppLanguage.Japanese -> SortNames("曲名", "アーティスト名", "アルバム", "アルバム名")
    AppLanguage.Korean -> SortNames("곡 이름", "아티스트 이름", "앨범", "앨범 이름")
    AppLanguage.Latvian -> SortNames("Dziesmas nosaukums", "Izpildītāja vārds", "Albums", "Albuma nosaukums")
    AppLanguage.Lithuanian -> SortNames("Dainos pavadinimas", "Atlikėjo vardas", "Albumas", "Albumo pavadinimas")
    AppLanguage.Macedonian -> SortNames("Име на песна", "Име на изведувач", "Албум", "Име на албум")
    AppLanguage.Malay -> SortNames("Nama lagu", "Nama artis", "Album", "Nama album")
    AppLanguage.Norwegian -> SortNames("Sangtittel", "Artistnavn", "Album", "Albumtittel")
    AppLanguage.Polish -> SortNames("Nazwa utworu", "Nazwa artysty", "Album", "Nazwa albumu")
    AppLanguage.Portuguese -> SortNames("Nome da música", "Nome do artista", "Álbum", "Nome do álbum")
    AppLanguage.Russian -> SortNames("Название песни", "Имя исполнителя", "Альбом", "Название альбома")
    AppLanguage.Serbian -> SortNames("Назив песме", "Име извођача", "Албум", "Назив албума")
    AppLanguage.Slovak -> SortNames("Názov skladby", "Meno interpreta", "Album", "Názov albumu")
    AppLanguage.Spanish -> SortNames("Nombre de la canción", "Nombre del artista", "Álbum", "Nombre del álbum")
    AppLanguage.Swedish -> SortNames("Låttitel", "Artistnamn", "Album", "Albumtitel")
    AppLanguage.Thai -> SortNames("ชื่อเพลง", "ชื่อศิลปิน", "อัลบั้ม", "ชื่ออัลบั้ม")
    AppLanguage.Ukrainian -> SortNames("Назва пісні", "Ім’я виконавця", "Альбом", "Назва альбому")
    AppLanguage.Urdu -> SortNames("گانے کا نام", "آرٹسٹ کا نام", "البم", "البم کا نام")
}

internal fun songSortLabel(language: AppLanguage, mode: SongSortMode): String = sortNames(language).let {
    when (mode) {
        SongSortMode.Title -> it.song
        SongSortMode.Artist -> it.artist
        SongSortMode.Album -> it.album
    }
}

internal fun albumSortLabel(language: AppLanguage, mode: AlbumSortMode): String = sortNames(language).let {
    when (mode) {
        AlbumSortMode.Artist -> it.artist
        AlbumSortMode.Album -> it.albumName
    }
}
