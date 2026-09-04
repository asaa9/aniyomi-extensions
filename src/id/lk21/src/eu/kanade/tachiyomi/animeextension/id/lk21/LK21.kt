package eu.kanade.tachiyomi.animeextension.id.lk21

import eu.kanade.tachiyomi.animesource.model.*
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.network.GET
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class LK21 : ParsedAnimeHttpSource() {
    override val name = "LK21"
    override val baseUrl = "https://tv12.lk21official.cc"
    override val lang = "id"
    override val supportsLatest = true

    // --- POPULAR ---
    override fun popularAnimeRequest(page: Int): Request = GET("$baseUrl/populer/page/$page", headers)
    override fun popularAnimeSelector(): String = "article.item-movies, div.grid-archive > div"
    override fun popularAnimeFromElement(element: Element): SAnime = SAnime.create().apply {
        title = element.select("h2, h3").text()
        setUrlWithoutDomain(element.select("a").attr("href"))
        thumbnail_url = element.select("img").attr("src").let {
            if (it.startsWith("//")) "https:$it" else it
        }
    }
    override fun popularAnimeNextPageSelector(): String = "a.next, a.pagination-next"

    // --- LATEST ---
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/latest/page/$page", headers)
    override fun latestUpdatesSelector(): String = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    // --- SEARCH ---
    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request = GET("$baseUrl/search/$query/page/$page", headers)
    override fun searchAnimeSelector(): String = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // --- DETAILS ---
    override fun animeDetailsParse(document: Document): SAnime = SAnime.create().apply {
        title = document.select("h1, h2.title").text()
        description = document.select("div.sinopsis, div.summary").text()
        genre = document.select("div.genres a").joinToString { it.text() }
        thumbnail_url = document.select("div.poster img").attr("src")
    }

    // --- EPISODES (Karena ini film, anggap 1 episode) ---
    override fun episodeListSelector(): String = "div.action-player" // Bagian tombol play
    override fun episodeFromElement(element: Element): SEpisode = SEpisode.create().apply {
        name = "Full Movie"
        episode_number = 1f
        setUrlWithoutDomain(element.baseUri())
    }

    // --- VIDEO URL ---
    override fun videoListParse(document: Document): List<Video> {
        // LK21 biasanya menggunakan iframe pihak ketiga (seperti Fembed, GDrive, dll)
        val iframeUrl = document.select("iframe").attr("src")
        
        // Catatan: Jika iframe dienkripsi, Anda butuh VideoExtractor khusus di sini
        return listOf(Video(iframeUrl, "Streaming Link (External)", iframeUrl))
    }
}
