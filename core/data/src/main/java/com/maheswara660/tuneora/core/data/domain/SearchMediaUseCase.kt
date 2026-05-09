package com.maheswara660.tuneora.core.data.domain

import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SearchResults(
    val songs: List<Song> = emptyList(),
) {
    val isEmpty: Boolean get() = songs.isEmpty()
    val totalCount: Int get() = songs.size
}

class SearchMediaUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(query: String): Flow<SearchResults> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return flowOf(SearchResults())
        }

        val searchMatcher = SearchMatcher(normalizedQuery)

        return musicRepository.songs.map { songs ->
            val scoredSongs = songs.mapNotNull { song ->
                val score = searchMatcher.calculateScore(song.title, song.artist, song.album)
                if (score > 0) song to score else null
            }.sortedByDescending { it.second }.map { it.first }

            SearchResults(songs = scoredSongs)
        }
    }
}

private class SearchMatcher(query: String) {
    private val queryLower = query.lowercase()
    private val queryWords = query.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }

    private val wordsInOrderPattern: Regex? = if (queryWords.size > 1) {
        Regex(queryWords.joinToString(".*") { Regex.escape(it) }, RegexOption.IGNORE_CASE)
    } else {
        null
    }

    fun calculateScore(vararg texts: String): Int {
        var bestScore = 0
        for (text in texts) {
            val textLower = text.lowercase()
            val score = calculateTextScore(textLower)
            if (score > bestScore) bestScore = score
        }
        return bestScore
    }

    private fun calculateTextScore(textLower: String): Int {
        if (textLower.contains(queryLower)) {
            val wordBoundaryBonus = if (isWordBoundaryMatch(textLower, queryLower)) 50 else 0
            val startBonus = if (textLower.startsWith(queryLower)) 30 else 0
            return 1000 + wordBoundaryBonus + startBonus
        }

        if (queryWords.size <= 1) return 0

        wordsInOrderPattern?.let { pattern ->
            if (pattern.containsMatchIn(textLower)) return 500
        }

        val allWordsPresent = queryWords.all { word -> textLower.contains(word) }
        if (allWordsPresent) {
            val boundaryMatches = queryWords.count { word -> isWordBoundaryMatch(textLower, word) }
            return 200 + (boundaryMatches * 20)
        }

        return 0
    }

    private fun isWordBoundaryMatch(text: String, query: String): Boolean {
        val index = text.indexOf(query)
        if (index == -1) return false
        val isAtStart = index == 0
        val isPrecededByBoundary = index > 0 && text[index - 1].isWordBoundary()
        return isAtStart || isPrecededByBoundary
    }

    private fun Char.isWordBoundary(): Boolean {
        return this in listOf(' ', '_', '-', '.', '/', '\\', '[', ']', '(', ')')
    }
}
