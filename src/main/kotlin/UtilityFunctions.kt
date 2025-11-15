import java.io.File
import kotlin.math.roundToInt

fun readDictionary(numberOfWordsToType: Int): List<String> {
    val input = File("src/main/kotlin/dictionary")
    val dictionary = input.readLines()
    val words = mutableListOf<String>()
    repeat(numberOfWordsToType) {
        words.add(dictionary.random().lowercase())
    }
    return words
}

fun splitCharArrayByWidth(input: CharArray, maxPrintableWidth: Int): List<List<Char>> {
    val result = mutableListOf<List<Char>>()
    var startIndex = 0
    while (startIndex < input.size) {
        var endIndex = (startIndex + maxPrintableWidth).coerceAtMost(input.size)
        if (endIndex < input.size) {
            var lastWhitespace = -1
            for (i in startIndex until endIndex) {
                if (input[i].isWhitespace()) {
                    lastWhitespace = i
                }
            }
            if (lastWhitespace != -1) {
                endIndex = lastWhitespace + 1
            }
        }
        result.add(input.slice(startIndex until endIndex))
        startIndex = endIndex
    }
    return result
}

fun getEndGameStats(
    startTime: Long,
    numberOfWordsToType: Int,
    totalCharacters: Int,
    errorCount: Int
): EndGameStats {
    val endTime = System.currentTimeMillis()
    val elapsedTimeInSeconds = (endTime - startTime) / 1000
    val wpm = ((numberOfWordsToType.toDouble() / elapsedTimeInSeconds.toDouble()) * 60).roundToInt()
    val rawAccuracy = ((totalCharacters - errorCount).toDouble() / totalCharacters.toDouble() * 100)
    val accuracy = rawAccuracy.coerceIn(0.0, 100.0).roundToInt()
    return EndGameStats(wpm, elapsedTimeInSeconds, accuracy)
}

fun Char.toSetting(): Setting? {
    return when (this) {
        '1' -> Setting.DETAILED_RESULT
        '2' -> Setting.NUMBER_OF_WORDS
        else -> null
    }
}