package com.maheswara660.tuneora.core.media.audio

import android.util.Xml
import androidx.media3.common.util.Log
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.extractor.text.CuesWithTiming
import androidx.media3.extractor.text.SubtitleParser
import androidx.media3.extractor.text.subrip.SubripParser
import com.maheswara660.tuneora.core.common.model.SemanticLyrics
import com.maheswara660.tuneora.core.common.model.SpeakerEntity
import com.maheswara660.tuneora.core.common.model.UsltFrameDecoder
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min

private const val TAG = "LyricsParser"

private sealed class SyntacticLrc {
    data class SyncPoint(val timestamp: ULong) : SyntacticLrc()
    data class SpeakerTag(val speaker: SpeakerEntity) : SyntacticLrc()
    data class WordSyncPoint(val timestamp: ULong) : SyntacticLrc()
    data class Metadata(val name: String, val value: String) : SyntacticLrc()
    data class LyricText(val text: String) : SyntacticLrc()
    data class InvalidText(val text: String) : SyntacticLrc()
    open class NewLine : SyntacticLrc() {
        class SyntheticNewLine : NewLine()
    }

    companion object {
        val timeMarksRegex = "\\[(\\d+):(\\d{2})([.:]\\d+)?]".toRegex()
        val timeMarksAfterWsRegex = "([ \t]+)\\[(\\d+):(\\d{2})([.:]\\d+)?]".toRegex()
        val timeWordMarksRegex = "<(\\d+):(\\d{2})([.:]\\d+)?>".toRegex()
        val metadataRegex = "\\[([a-zA-Z#]+):([^]]*)]".toRegex()

        private fun parseTime(match: MatchResult): ULong {
            val minute = match.groupValues[1].toULong()
            val milliseconds = ((match.groupValues[2] + match.groupValues[3]
                .replace(':', '.')).toDouble() * 1000L).toULong()
            return minute * 60u * 1000u + milliseconds
        }

        fun parseLrc(text: String, multiLineEnabled: Boolean): List<SyntacticLrc>? {
            if (text.isBlank()) return null
            var pos = 0
            val out = mutableListOf<SyntacticLrc>()
            var isBgSpeaker = false
            while (pos < text.length) {
                var pendingBgNewLine = false
                if (isBgSpeaker && text[pos] == ']') {
                    pos++
                    isBgSpeaker = false
                    pendingBgNewLine = true
                }
                if (pos < text.length && pos + 1 < text.length && text.regionMatches(
                        pos,
                        "\r\n",
                        0,
                        2
                    )
                ) {
                    out.add(NewLine())
                    pos += 2
                    continue
                }
                if (pos < text.length && (text[pos] == '\n' || text[pos] == '\r')) {
                    out.add(NewLine())
                    pos++
                    continue
                }
                if (pendingBgNewLine) {
                    out.add(NewLine.SyntheticNewLine())
                    continue
                }
                val tmMatch = timeMarksRegex.matchAt(text, pos)
                if (tmMatch != null) {
                    val lastOrNull = out.lastOrNull()
                    if (!(lastOrNull is NewLine? || lastOrNull is SyncPoint
                                || (lastOrNull is SpeakerTag && lastOrNull.speaker.isBackground)))
                        out.add(NewLine.SyntheticNewLine())
                    out.add(SyncPoint(parseTime(tmMatch)))
                    pos += tmMatch.value.length
                    continue
                }
                val tmwMatch = timeMarksAfterWsRegex.matchAt(text, pos)
                if (out.lastOrNull() is SyncPoint && pos + 7 < text.length && tmwMatch != null) {
                    pos += tmwMatch.groupValues[1].length
                    continue
                }
                if (out.lastOrNull() is SyncPoint) {
                    if (pos + 2 < text.length && text.regionMatches(pos, "v1:", 0, 3)) {
                        out.add(SpeakerTag(SpeakerEntity.Voice1))
                        pos += 3
                        continue
                    }
                    if (pos + 2 < text.length && text.regionMatches(pos, "v2:", 0, 3)) {
                        out.add(SpeakerTag(SpeakerEntity.Voice2))
                        pos += 3
                        continue
                    }
                    if (pos + 2 < text.length && text.regionMatches(pos, "v3:", 0, 3)) {
                        out.add(SpeakerTag(SpeakerEntity.Group))
                        pos += 3
                        continue
                    }
                }
                if (pos + 3 < text.length && text.regionMatches(pos, "[bg:", 0, 4)) {
                    if (out.isNotEmpty() && out.last() !is NewLine)
                        out.add(NewLine.SyntheticNewLine())
                    val lastSpeaker = if (out.isNotEmpty()) out.subList(0, out.size - 1)
                        .indexOfLast { it is NewLine }.let { if (it < 0) null else it }?.let {
                            (out.subList(it, out.size - 1).findLast { it is SpeakerTag }
                                    as SpeakerTag?)?.speaker
                        } else null
                    out.add(
                        SpeakerTag(
                            when {
                                lastSpeaker?.isGroup == true -> SpeakerEntity.GroupBackground
                                lastSpeaker?.isVoice2 == true -> SpeakerEntity.Voice2Background
                                else -> SpeakerEntity.Voice1Background
                            }
                        )
                    )
                    pos += 4
                    isBgSpeaker = true
                    continue
                }
                if (out.isEmpty() || out.last() is NewLine) {
                    val mmMatch = metadataRegex.matchAt(text, pos)
                    if (mmMatch != null) {
                        out.add(Metadata(mmMatch.groupValues[1], mmMatch.groupValues[2]))
                        pos += mmMatch.value.length
                        continue
                    }
                }
                val wmMatch = timeWordMarksRegex.matchAt(text, pos)
                if (wmMatch != null) {
                    out.add(WordSyncPoint(parseTime(wmMatch)))
                    pos += wmMatch.value.length
                    continue
                }
                val firstUnsafeCharPos = (text.substring(pos).indexOfFirst {
                    it == '[' ||
                            it == '<' || it == '\r' || it == '\n' || (isBgSpeaker && it == ']')
                } + pos)
                    .let { if (it == pos - 1) text.length else it }
                    .let { if (it == pos) it + 1 else it }
                val subText = text.substring(pos, firstUnsafeCharPos)
                val last = out.lastOrNull()
                if (out.indexOfLast { it is NewLine } <
                    out.indexOfLast { it is SyncPoint || it is WordSyncPoint }) {
                    if (last is LyricText) {
                        out[out.size - 1] = LyricText(last.text + subText)
                    } else {
                        out.add(LyricText(subText))
                    }
                } else {
                    if (last is InvalidText) {
                        out[out.size - 1] = InvalidText(last.text + subText)
                    } else {
                        out.add(InvalidText(subText))
                    }
                }
                pos = firstUnsafeCharPos
            }
            if (out.lastOrNull() is SyncPoint)
                out.add(InvalidText(""))
            if (out.isNotEmpty() && out.last() !is NewLine)
                out.add(NewLine.SyntheticNewLine())
            return out.let {
                if (it.find {
                        it is SyncPoint && it.timestamp > 0u
                                || it is WordSyncPoint && it.timestamp > 0u
                    } == null)
                    it.flatMap {
                        when (it) {
                            is InvalidText -> listOf(it)
                            is SpeakerTag -> listOf(it)
                            is LyricText -> listOf(InvalidText(it.text))
                            else -> listOf()
                        }
                    }
                else it
            }.let {
                if (multiLineEnabled) {
                    val a = AtomicReference<String?>(null)
                    it.flatMap {
                        val aa = a.get()
                        when {
                            it is LyricText -> {
                                if (aa == null)
                                    a.set(it.text)
                                else
                                    a.set(aa + it.text)
                                listOf()
                            }
                            it is InvalidText && aa != null -> {
                                a.set(aa + it.text)
                                listOf()
                            }
                            it is NewLine && aa != null -> {
                                a.set(aa + "\n")
                                listOf()
                            }
                            aa != null -> {
                                a.set(null)
                                var aaa: String = aa
                                var i = 0
                                while (aaa.isNotEmpty() && aaa.last() == '\n') {
                                    i++
                                    aaa = aaa.dropLast(1)
                                }
                                listOf(LyricText(aaa)).let {
                                    var aaaa: List<SyntacticLrc> = it
                                    while (i-- > 0)
                                        aaaa = aaaa + listOf(NewLine())
                                    aaaa
                                } + it
                            }
                            else -> listOf(it)
                        }
                    }.let {
                        val aa = a.get()
                        if (aa != null)
                            it + if (aa.isNotEmpty() && aa.last() == '\n')
                                listOf(LyricText(aa.dropLast(1)), NewLine())
                            else
                                listOf(LyricText(aa))
                        else it
                    }
                } else it
            }
        }
    }
}

private fun splitBidirectionalWords(syncedLyrics: SemanticLyrics.SyncedLyrics) {
    syncedLyrics.text.forEach { line ->
        val words = line.words
        if (words.isNullOrEmpty()) return@forEach
        val bidirectionalBarriers = findBidirectionalBarriers(line.text)
        var lastWasRtl = false
        bidirectionalBarriers.forEach { barrier ->
            val evilWordIndex =
                if (barrier.first == -1) -1 else words.indexOfFirst {
                    it.charRange.contains(barrier.first) && it.charRange.first != barrier.first
                }
            if (evilWordIndex == -1) {
                val wordIndex = if (barrier.first == -1) 0 else
                    words.indexOfFirst { it.charRange.first == barrier.first }
                if (wordIndex != -1) {
                    for (i in wordIndex until words.size) {
                        words[i].isRtl = barrier.second
                    }
                }
                lastWasRtl = barrier.second
                return@forEach
            }
            val evilWord = words[evilWordIndex]
            val evilWordEndInclusive = evilWord.endInclusive ?: 0uL
            val barrierTime = min(evilWord.begin + ((words.map {
                (it.endInclusive?.minus(it.begin)?.toFloat() ?: 0f) / it.charRange.count().toFloat()
            }.average().let { if (it.isNaN()) 100.0 else it } * (barrier.first -
                    evilWord.charRange.first))).toULong(), evilWordEndInclusive.coerceAtLeast(1uL) - 1uL)
            val firstPart = SemanticLyrics.Word(
                charRange = evilWord.charRange.first..<barrier.first,
                begin = evilWord.begin,
                endInclusive = barrierTime,
                isRtl = lastWasRtl
            )
            val secondPart = SemanticLyrics.Word(
                charRange = barrier.first..evilWord.charRange.last,
                begin = barrierTime,
                endInclusive = evilWord.endInclusive,
                isRtl = barrier.second
            )
            words[evilWordIndex] = firstPart
            words.add(evilWordIndex + 1, secondPart)
            lastWasRtl = barrier.second
        }
    }
}

private val ltr =
    arrayOf(
        Character.DIRECTIONALITY_LEFT_TO_RIGHT,
        Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
        Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE
    )
private val rtl =
    arrayOf(
        Character.DIRECTIONALITY_RIGHT_TO_LEFT,
        Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
        Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
        Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
    )

fun findBidirectionalBarriers(text: CharSequence): List<Pair<Int, Boolean>> {
    val barriers = mutableListOf<Pair<Int, Boolean>>()
    if (text.isEmpty()) return barriers
    var previousDirection = text.find {
        val dir = Character.getDirectionality(it)
        dir in ltr || dir in rtl
    }?.let { Character.getDirectionality(it) in rtl } == true
    barriers.add(Pair(-1, previousDirection))
    for (i in 0 until text.length) {
        val currentDirection = Character.getDirectionality(text[i])
        val isRtl = currentDirection in rtl
        if (currentDirection !in ltr && !isRtl)
            continue
        if (previousDirection != isRtl)
            barriers.add(Pair(i, isRtl))
        previousDirection = isRtl
    }
    return barriers
}

fun parseLrc(lyricText: String, trimEnabled: Boolean, multiLineEnabled: Boolean): SemanticLyrics? {
    val lyricSyntax = SyntacticLrc.parseLrc(lyricText, multiLineEnabled)
        ?: return null
    if (lyricSyntax.find { it is SyntacticLrc.SyncPoint || it is SyntacticLrc.WordSyncPoint } == null) {
        var lastSpeakerTag: SpeakerEntity? = null
        val out = mutableListOf<Pair<String, SpeakerEntity?>>()
        for (element in lyricSyntax) {
            when (element) {
                is SyntacticLrc.SpeakerTag -> {
                    lastSpeakerTag = element.speaker
                }

                is SyntacticLrc.InvalidText -> {
                    out += element.text to lastSpeakerTag
                    lastSpeakerTag = null
                }

                else -> {}
            }
        }
        while (out.firstOrNull()?.first?.isBlank() == true)
            out.removeAt(0)
        return SemanticLyrics.UnsyncedLyrics(out)
    }
    val out = mutableListOf<SemanticLyrics.LyricLine>()
    var offset = 0L
    var lastSyncPoint: ULong? = null
    var lastWordSyncPoint: ULong? = null
    var speaker: SpeakerEntity? = null
    var hadVoice2 = false
    var hadLyricSinceWordSync = true
    var hadWordSyncSinceNewLine = false
    val currentLine = mutableListOf<Pair<ULong, String?>>()
    var syncPointStreak = 0
    val compressed = mutableListOf<ULong>()
    for (element in lyricSyntax) {
        if (element is SyntacticLrc.SyncPoint)
            syncPointStreak++
        else
            syncPointStreak = 0
        when (element) {
            is SyntacticLrc.Metadata -> if (element.name == "offset") {
                offset = element.value.toLongOrNull()?.times(-1) ?: 0L
            }

            is SyntacticLrc.SyncPoint -> {
                val ts = (element.timestamp.toLong() + offset).coerceAtLeast(0).toULong()
                if (syncPointStreak > 1) {
                    compressed.add(ts)
                } else {
                    lastSyncPoint = ts
                }
            }

            is SyntacticLrc.SpeakerTag -> {
                speaker = element.speaker
                if (element.speaker.isVoice2) {
                    hadVoice2 = true
                }
            }

            is SyntacticLrc.WordSyncPoint -> {
                if (!hadLyricSinceWordSync && lastWordSyncPoint != null)
                    currentLine.add(Pair(lastWordSyncPoint, null))
                lastWordSyncPoint = (element.timestamp.toLong() + offset).coerceAtLeast(0).toULong()
                if (lastSyncPoint == null)
                    lastSyncPoint = lastWordSyncPoint
                hadLyricSinceWordSync = false
                hadWordSyncSinceNewLine = true
            }

            is SyntacticLrc.LyricText -> {
                hadLyricSinceWordSync = true
                currentLine.add(Pair(lastWordSyncPoint ?: lastSyncPoint!!, element.text))
            }

            is SyntacticLrc.NewLine -> {
                val words = if (currentLine.size > 1 || hadWordSyncSinceNewLine) {
                    val wout = mutableListOf<SemanticLyrics.Word>()
                    var idx = 0
                    for (i in currentLine.indices) {
                        val current = currentLine[i]
                        if (current.second == null)
                            continue
                        val oIdx = idx
                        idx += current.second!!.length
                        val textWithoutStartWhitespace = current.second!!.trimStart()
                        val startWhitespaceLength =
                            current.second!!.length - textWithoutStartWhitespace.length
                        val textWithoutWhitespaces = textWithoutStartWhitespace.trimEnd()
                        val endWhitespaceLength =
                            textWithoutStartWhitespace.length - textWithoutWhitespaces.length
                        val startIndex = oIdx + startWhitespaceLength
                        val endIndex = idx - endWhitespaceLength
                        if (startIndex >= endIndex)
                            continue
                        val endInclusive = if (i + 1 < currentLine.size) {
                            currentLine[i + 1].first - 1uL
                        } else if (lastWordSyncPoint != null &&
                            lastWordSyncPoint > current.first
                        ) {
                            lastWordSyncPoint - 1uL
                        } else null
                        if (endInclusive == null || endInclusive > current.first)
                            wout.add(
                                SemanticLyrics.Word(
                                    current.first, endInclusive,
                                    startIndex..<endIndex,
                                    isRtl = false
                                )
                            )
                    }
                    wout
                } else null
                if (currentLine.isNotEmpty() || lastWordSyncPoint != null || lastSyncPoint != null) {
                    var text = currentLine.joinToString("") { it.second ?: "" }
                    if (trimEnabled) {
                        val orig = text
                        text = orig.trimStart()
                        val startDiff = orig.length - text.length
                        text = text.trimEnd()
                        val currentWords = words
                        if (currentWords != null) {
                            val iter = currentWords.listIterator()
                            while (iter.hasNext()) {
                                val it = iter.next()
                                if (it.charRange.last.toLong() - startDiff < 0
                                    || it.charRange.first.toLong() - startDiff >= text.length
                                )
                                    iter.remove()
                                else
                                    it.charRange = (it.charRange.first - startDiff.toInt())
                                        .coerceAtLeast(0)..(it.charRange.last - startDiff.toInt())
                                        .coerceAtMost(text.length - 1)
                            }
                        }
                    }
                    val start = if (currentLine.isNotEmpty()) lastSyncPoint
                        ?: currentLine.first().first
                    else lastWordSyncPoint ?: lastSyncPoint!!
                    val lastWordSyncForEnd = lastWordSyncPoint?.let { it - 1uL }
                    val lastWordBegin = words?.lastOrNull()?.begin
                    val end = if (lastWordSyncForEnd != null && lastWordBegin != null &&
                        lastWordBegin < lastWordSyncForEnd
                    ) lastWordSyncForEnd else null
                    out.add(
                        SemanticLyrics.LyricLine(
                            text, start, end ?: 0uL,
                            end == null, words?.toMutableList(), speaker, false
                        )
                    )
                    compressed.forEach {
                        val diff = it - start
                        out.add(
                            out.last().copy(
                                start = it,
                            end = end?.plus(diff) ?: 0uL,
                            words = words?.map { word ->
                                word.copy(
                                    begin = word.begin + diff,
                                    endInclusive = word.endInclusive?.plus(diff)
                                )
                            }?.toMutableList()
                        )
                        )
                    }
                }
                compressed.clear()
                currentLine.clear()
                lastSyncPoint = null
                lastWordSyncPoint = null
                hadWordSyncSinceNewLine = false
                speaker = null
                hadLyricSinceWordSync = true
            }

            else -> {}
        }
    }
    out.sortBy { it.start }
    var previousLyric: SemanticLyrics.LyricLine? = null
    out.forEach { lyric ->
        if (!hadVoice2) {
            lyric.speaker = when (lyric.speaker) {
                SpeakerEntity.Voice1 -> SpeakerEntity.Voice
                SpeakerEntity.Voice1Background -> SpeakerEntity.VoiceBackground
                else -> lyric.speaker
            }
        }
        val mainEnd = if (lyric.start == previousLyric?.start) out.firstOrNull {
            it.start == lyric.start && !it.endIsImplicit
        }?.end else null
        val words = lyric.words
        val wordWithoutEnd = words?.lastOrNull()
        if (wordWithoutEnd != null && wordWithoutEnd.endInclusive == null) {
            wordWithoutEnd.endInclusive = mainEnd?.takeIf { it > wordWithoutEnd.begin }
                ?: out.find { it.start > lyric.start }?.start?.minus(1uL)
                    ?.takeIf { it > wordWithoutEnd.begin }
                        ?: run {
                    wordWithoutEnd.begin + (words.subList(0, words.size - 1)
                        .map { (it.endInclusive?.minus(it.begin)?.toFloat() ?: 0f) / it.charRange.count().toFloat() }
                        .average().let { if (it.isNaN()) 100.0 else it } *
                            lyric.text.substring(wordWithoutEnd.charRange).length).toULong()
                }
        }
        if (lyric.endIsImplicit) {
            if (mainEnd != null) {
                lyric.end = mainEnd
                lyric.endIsImplicit = false
            } else {
                lyric.end = wordWithoutEnd?.endInclusive
                    ?: out.find { it.start > lyric.start }?.start?.minus(1uL)
                            ?: Long.MAX_VALUE.toULong()
                lyric.endIsImplicit = wordWithoutEnd == null
            }
        }
        lyric.isTranslated = lyric.start == previousLyric?.start &&
                (previousLyric?.text?.isNotBlank() == true || lyric.text.isBlank())
        previousLyric = lyric
    }
    while (out.firstOrNull()?.text?.isBlank() == true)
        out.removeAt(0)
    return SemanticLyrics.SyncedLyrics(out).also { splitBidirectionalWords(it) }
}

fun parseTtml(audioMimeType: String?, lyricText: String): SemanticLyrics? {
    val formattedLyricText = lyricText.replace(Regex("&(?!#?[a-zA-Z0-9]+;)"), "&amp;")
    val parser = Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
    parser.setInput(StringReader(formattedLyricText))
    try {
        parser.nextTag()
        if (parser.name != "tt") return null
    } catch (_: Exception) {
        return null
    }
    return null
}

fun parseSrt(lyricText: String, trimEnabled: Boolean): SemanticLyrics? {
    if (!lyricText.startsWith("1\n") && !lyricText.startsWith("1\r")) return null
    val cues = mutableListOf<CuesWithTiming>()
    val parser = SubripParser()
    try {
        parser.parse(lyricText.toByteArray(), SubtitleParser.OutputOptions.allCues()) { cues.add(it) }
    } catch (e: Exception) {
        return null
    }
    var lastTs: ULong? = null
    return SemanticLyrics.SyncedLyrics(cues.map {
        val ts = (it.startTimeUs / 1000).toULong()
        val l = lastTs == ts
        lastTs = ts
        SemanticLyrics.LyricLine(
            it.cues[0].text!!.toString().let { text -> if (trimEnabled) text.trim() else text },
            ts, (it.endTimeUs / 1000).toULong(), false, null, null, l
        )
    })
}

fun UsltFrameDecoder.Result.Sylt.toSyncedLyrics(trimEnabled: Boolean): SemanticLyrics.SyncedLyrics {
    val out = mutableListOf<SemanticLyrics.LyricLine>()
    var i = 0
    while (i < lines.size) {
        var j = i + 1
        while (j < lines.size &&
            !lines[j].text.trimStart { it == '\t' || it == ' ' || it == '\r' }.startsWith("\n")
        ) {
            j++
        }
        var idx = 0
        val wout = mutableListOf<SemanticLyrics.Word>()
        for (k in i until j) {
            val it = lines[k]
            val next = if (k + 1 < j) lines[k + 1] else null
            val oIdx = idx
            idx += it.text.length
            val textWithoutStartWhitespace = it.text.trimStart()
            val startWhitespaceLength = it.text.length - textWithoutStartWhitespace.length
            val textWithoutWhitespaces = textWithoutStartWhitespace.trimEnd()
            val endWhitespaceLength = textWithoutStartWhitespace.length - textWithoutWhitespaces.length
            val startIndex = oIdx + startWhitespaceLength
            val endIndex = idx - endWhitespaceLength
            if (startIndex >= endIndex) continue
            val endInclusive = if (next != null && next.timestamp > 0u) next.timestamp.toULong() - 1uL else null
            if (endInclusive == null || endInclusive > it.timestamp.toULong())
                wout.add(SemanticLyrics.Word(it.timestamp.toULong(), endInclusive, startIndex..<endIndex, isRtl = false))
        }
        var string = lines.subList(i, j).joinToString("") { it.text }
        val nli1 = string.indexOf('\n')
        if (nli1 != -1 && string.take(nli1).trimStart { it == '\t' || it == ' ' || it == '\r' }.isEmpty()) {
            string = string.substring(nli1 + 1)
        }
        if (j < lines.size) {
            var nli = lines[j].text.indexOf('\n')
            if (nli != -1) {
                if (nli > 0 && lines[j].text[nli - 1] == '\r') nli--
                string += lines[j].text.substring(0, nli)
            }
        }
        if (trimEnabled) {
            val orig = string
            string = orig.trimStart()
            val startDiff = orig.length - string.length
            string = string.trimEnd()
            val iter = wout.listIterator()
            while (iter.hasNext()) {
                val it = iter.next()
                if (it.charRange.last.toLong() - startDiff < 0 || it.charRange.first.toLong() - startDiff >= string.length)
                    iter.remove()
                else
                    it.charRange = (it.charRange.first - startDiff.toInt()).coerceAtLeast(0)..(it.charRange.last - startDiff.toInt()).coerceAtMost(string.length - 1)
            }
        }
        val explicitEnd = if (i < j - 1 && lines[j - 1].text.isBlank()) lines[j - 1].timestamp.toULong() - 1uL
        else if (wout.size > 1) wout.last().endInclusive else null
        out.add(SemanticLyrics.LyricLine(string, lines[i].timestamp.toULong(), explicitEnd ?: 0uL, explicitEnd == null, if (wout.size > 1) wout else null, null, false))
        i = j
    }
    out.sortBy { it.start }
    var previousTimestamp = ULong.MAX_VALUE
    out.forEach { lyric ->
        val words = lyric.words
        val mainEnd = if (lyric.start == previousTimestamp) out.firstOrNull { it.start == lyric.start && !it.endIsImplicit }?.end else null
        val wordWithoutEnd = words?.lastOrNull()
        if (wordWithoutEnd != null && wordWithoutEnd.endInclusive == null) {
            wordWithoutEnd.endInclusive = mainEnd?.takeIf { it > wordWithoutEnd.begin }
                ?: out.find { it.start > lyric.start }?.start?.minus(1uL)
                    ?.takeIf { it > wordWithoutEnd.begin }
                        ?: run {
                    wordWithoutEnd.begin + (words.subList(0, words.size - 1)
                        .map { (it.endInclusive?.minus(it.begin)?.toFloat() ?: 0f) / it.charRange.count().toFloat() }
                        .average().let { if (it.isNaN()) 100.0 else it } *
                            lyric.text.substring(wordWithoutEnd.charRange).length).toULong()
                }
        }
        if (lyric.endIsImplicit) {
            if (mainEnd != null) {
                lyric.end = mainEnd
                lyric.endIsImplicit = false
            } else {
                lyric.end = wordWithoutEnd?.endInclusive ?: out.find { it.start > lyric.start }?.start?.minus(1uL) ?: Long.MAX_VALUE.toULong()
                lyric.endIsImplicit = wordWithoutEnd == null
            }
        }
        lyric.isTranslated = lyric.start == previousTimestamp
        previousTimestamp = lyric.start
    }
    while (out.firstOrNull()?.text?.isBlank() == true) out.removeAt(0)
    return SemanticLyrics.SyncedLyrics(out).also { splitBidirectionalWords(it) }
}
