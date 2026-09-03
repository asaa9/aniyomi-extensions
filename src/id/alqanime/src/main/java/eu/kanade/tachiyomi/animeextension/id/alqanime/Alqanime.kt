package eu.kanade.tachiyomi.animeextension.id.alqanime

import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.Exception

class Alqanime : ParsedAnimeHttpSource() {

    override val name = "Alqanime"
    override val baseUrl = "https://alqanime.net"
    override val lang = "id"
    override val supportsLatest = true

    override fun popularAnimeRequest(page: Int): Request =
        GET("$baseUrl/anime/page/$page/", headers)

    override fun popularAnimeSelector(): String = "article.bs, div.animepost, div.listupd article"

    override fun popularAnimeFromElement(element: Element): SAnime = SAnime.create().apply {
        val link = element.selectFirst("a")
        setUrlWithoutDomain(link?.attr("href") ?: "")
        val rawTitle = element.selectFirst(".tt, .title, h2, h3")?.text() ?: link?.attr("title") ?: ""
        title = rawTitle.replace(Regex("""(?i)\s*episode.*"""), "").trim()
        thumbnail_url = element.selectFirst("img")?.attr("src")
    }

    override fun popularAnimeNextPageSelector(): String = "a.next, div.pagination a.next"

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/anime/page/$page/?order=update", headers)

    override fun latestUpdatesSelector(): String = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request =
        GET("$baseUrl/page/$page/?s=$query", headers)

    override fun searchAnimeSelector(): String = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    override fun animeDetailsParse(document: Document): SAnime = SAnime.create().apply {
        title = document.selectFirst("h1.entry-title, .entry-header h1")?.text().orEmpty()
        thumbnail_url = document.selectFirst(".thumb img, .entry-content img")?.attr("src")
        description = document.select(".entry-content p, .synopsis").text()
        genre = document.select(".genre-info a, a[rel=tag]").joinToString(", ") { it.text() }
        status = when {
            document.text().contains("Completed", ignoreCase = true) -> SAnime.COMPLETED
            document.text().contains("Ongoing", ignoreCase = true) -> SAnime.ONGOING
            else -> SAnime.UNKNOWN
        }
    }

    override fun episodeListSelector(): String = "div.eplister li, div.episodelist li, ul.lstepsiode li, div.bxcl li"

    override fun episodeFromElement(element: Element): SEpisode = SEpisode.create().apply {
        val link = element.selectFirst("a")
        setUrlWithoutDomain(link?.attr("href") ?: "")
        name = element.selectFirst(".epl-title, .epl-num, .epdl")?.text() ?: link?.text() ?: "Episode"
        val epMatch = Regex("""(?:Episode|\bEp\.?)\s*(\d+)""", RegexOption.IGNORE_CASE).find(name)
        episode_number = epMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0F
    }

    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        val videoList = mutableListOf<Video>()

        for (source in document.select("video source")) {
            val videoUrl = source.attr("src")
            val quality = source.attr("label").ifEmpty { "Default" }
            if (videoUrl.isNotEmpty()) {
                videoList.add(Video(videoUrl, quality, videoUrl))
            }
        }

        val iframeUrl = document.selectFirst("iframe[src]")?.attr("abs:src")
        if (!iframeUrl.isNullOrEmpty()) {
            try {
                videoList.add(Video(iframeUrl, "Web Player", iframeUrl))
            } catch (_: Exception) {}
        }

        return videoList
    }

    override fun videoListSelector(): String = throw UnsupportedOperationException("Not used")
    override fun videoFromElement(element: Element): Video = throw UnsupportedOperationException("Not used")
    override fun videoUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")
}
