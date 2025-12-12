import java.io.File
import kotlin.math.roundToInt
import kotlin.random.Random

fun readDictionary(
    numberOfWordsToType: Int,
    difficulty: Difficulty,
    seed: String? = null,
): List<String> {
    val wordsToReturn = mutableListOf<String>()
    val seedData = getSeedData(seed)
    val dictionary = getDictionaryResource()
    val difficulty = seedData?.difficulty ?: difficulty
    val requiredWordLength =
        when (difficulty) {
            Difficulty.EASY -> 0..6
            Difficulty.MEDIUM -> 4..8
            Difficulty.HARD -> 6..Int.MAX_VALUE
        }
    var failedAddAttempts = 0
    val seed = seedData?.seed ?: System.currentTimeMillis().toInt()
    val random = Random(seed)
    while (wordsToReturn.size < numberOfWordsToType) {
        val word = dictionary.random(random).lowercase()
        if (word.length in requiredWordLength && word !in wordsToReturn) {
            wordsToReturn.add(word)
            failedAddAttempts = 0
        } else {
            if (failedAddAttempts == 5) {
                wordsToReturn.add(word)
                failedAddAttempts = 0
            } else {
                failedAddAttempts++
            }
        }
    }
    return wordsToReturn
}

fun getSeedData(seed: String?): DictionarySeed? {
    val seed = seed?.lowercase() ?: return null
    return try {
        val difficultyIdentifier = seed.first()
        val difficulty = when (difficultyIdentifier) {
            'e' -> Difficulty.EASY
            'm' -> Difficulty.MEDIUM
            'h' -> Difficulty.HARD
            else -> throw IllegalArgumentException("Unexpected difficulty identifier in seed")
        }
        val seedValue = seed.drop(1).toInt()
        DictionarySeed(
            difficulty = difficulty,
            seed = seedValue,
        )
    } catch (e: Exception) {
        null
    }
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

fun generateSeed(difficulty: Difficulty): String {
    val difficultyChar = when (difficulty) {
        Difficulty.EASY -> 'e'
        Difficulty.MEDIUM -> 'm'
        Difficulty.HARD -> 'h'
    }
    val seed = buildString {
        append(difficultyChar)
        append(System.currentTimeMillis().toInt())
    }
   return seed
}

fun getDictionaryResource(): List<String> {
    val path = "dictionary"
    val input = File(path)
    return if (input.exists() && input.isFile && input.canRead()) {
        input.readLines()
    } else {
        Thread.currentThread().contextClassLoader.getResource(path)?.readText()?.lines()
            ?: getFallbackDictionary()
    }
}

fun getFallbackDictionary() = listOf(
    "no",
    "dictionary",
    "found",
    "and",
    "that",
    "is",
    "very",
    "unfortunate",
    "but",
    "at",
    "least",
    "you",
    "can",
    "still",
    "enjoy",
    "this",
    "fallback",
    "list",
    "of",
    "words"
)

data class DictionarySeed(
    val difficulty: Difficulty,
    val seed: Int,
)