import java.io.File
import kotlin.math.roundToInt

fun readDictionary(
    numberOfWordsToType: Int,
    difficulty: Difficulty,
): List<String> {
    val input = File("dictionary")
    val wordsToReturn = mutableListOf<String>()

    val dictionary =
        if (input.exists() && input.isFile && input.canRead()) {
            input.readLines()
        } else {
            DEFAULT_DICTIONARY
        }

    val requiredWordLength =
        when (difficulty) {
            Difficulty.EASY -> 0..6
            Difficulty.MEDIUM -> 4..8
            Difficulty.HARD -> 6..Int.MAX_VALUE
        }

    while (wordsToReturn.size < numberOfWordsToType) {
        val word = dictionary.random().lowercase()
        if (word.length in requiredWordLength) {
            wordsToReturn.add(word)
        }
    }
    return wordsToReturn
}

fun splitCharArrayByWidth(
    input: CharArray,
    maxPrintableWidth: Int,
): List<List<Char>> {
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
    errorCount: Int,
): EndGameStats {
    val endTime = System.currentTimeMillis()
    val elapsedTimeInSeconds = (endTime - startTime) / 1000
    val wpm = ((numberOfWordsToType.toDouble() / elapsedTimeInSeconds.toDouble()) * 60).roundToInt()
    val rawAccuracy = ((totalCharacters - errorCount).toDouble() / totalCharacters.toDouble() * 100)
    val accuracy = rawAccuracy.coerceIn(0.0, 100.0).roundToInt()
    return EndGameStats(wpm, elapsedTimeInSeconds, accuracy)
}

fun Char.toSetting(): Setting? =
    when (this) {
        '1' -> Setting.DETAILED_RESULT
        '2' -> Setting.NUMBER_OF_WORDS
        '3' -> Setting.DIFFICULTY
        '4' -> Setting.HEALTH
        else -> null
    }
