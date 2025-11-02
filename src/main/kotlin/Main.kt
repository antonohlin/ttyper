import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TextCharacter.fromCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.File
import kotlin.math.roundToInt

fun main() {
    val terminal = DefaultTerminalFactory().createTerminal()
    val screen = TerminalScreen(terminal)
    val colSize = screen.terminalSize.columns
    val rowSize = screen.terminalSize.rows
    val numberOfWordsToType = 20
    val green = TextColor.RGB(100, 200, 100)
    val red = TextColor.RGB(250, 90, 90)
    val white = TextColor.RGB(255, 255, 255)
    val wordsFromFile = readDictionary(numberOfWordsToType).joinToString(separator = " ").toCharArray()
    var timerHasBeenStarted = false
    var startTime: Long = 0
    val rawInput = StringBuilder()
    var errorCount = 0
    val printableWidth = when {
        colSize * 0.7 > 100 -> 100
        colSize * 0.7 > 80 -> 80
        else -> 60
    }
    screen.startScreen()
    val startPosition =
        screen.cursorPosition.withColumn(colSize / 2 - printableWidth / 2).withRow((rowSize / 2.5).roundToInt())
    val lines = splitCharArrayByWidth(wordsFromFile, printableWidth)
    screen.drawWords(lines, startPosition)
    var letter = 0
    var line = 0
    screen.cursorPosition = startPosition
    screen.refresh()
    while (line < lines.size) {
        while (letter < lines[line].size) {
            val key = screen.readInput()
            if (!timerHasBeenStarted) {
                startTime = System.currentTimeMillis()
                timerHasBeenStarted = true
            }
            if (key.keyType != KeyType.Backspace) {
                rawInput.append(key.character)
                if (key.character == lines[line][letter]) {
                    screen.drawCharacter(key.character, screen.cursorPosition, green)
                } else {
                    errorCount++
                    screen.drawCharacter(lines[line][letter], screen.cursorPosition, red)
                }
                screen.cursorPosition = screen.cursorPosition.withRelativeColumn(+1)
                if (letter + 1 == lines[line].size) {
                    screen.cursorPosition = screen.cursorPosition.withColumn(startPosition.column).withRelativeRow(+1)
                    letter = 0
                    line++
                    screen.refresh()
                    break
                } else {
                    letter++
                }
            } else {
                rawInput.append('·')
                if (letter > 0) {
                    letter--
                    screen.cursorPosition = screen.cursorPosition.withRelativeColumn(-1)
                    screen.drawCharacter(lines[line][letter], screen.cursorPosition, white)
                } else if (line > 0) {
                    line--
                    letter = lines[line].size - 1
                    screen.cursorPosition =
                        screen.cursorPosition.withColumn(startPosition.column + lines[line].size - 1)
                            .withRelativeRow(-1)
                    screen.drawCharacter(lines[line][letter], screen.cursorPosition, white)
                }
            }
            screen.refresh()
        }
    }
    val endTime = System.currentTimeMillis()
    val elapsedTimeInSeconds = (endTime - startTime) / 1000
    val wpm = (numberOfWordsToType.toDouble() / elapsedTimeInSeconds.toDouble()) * 60
    val totalCharacters = wordsFromFile.size
    val rawAccuracy = ((totalCharacters - errorCount).toDouble() / totalCharacters.toDouble() * 100)
    val accuracy = rawAccuracy.coerceIn(0.0, 100.0).roundToInt()

    terminal.resetColorAndSGR()
    screen.drawWpmResult(
        totalWords = numberOfWordsToType,
        finalTime = elapsedTimeInSeconds,
        wpm = wpm.roundToInt(),
        accuracy = accuracy,
        position = screen.cursorPosition.withRelativeRow(+1).also { screen.cursorPosition = it }
    )
    if (accuracy < 100) {
        screen.drawExpectedActualResult(
            expected = lines,
            actual = rawInput.toString(),
            printableWidth = printableWidth,
            position = screen.cursorPosition.withRelativeRow(+2).also { screen.cursorPosition = it }
        )
    }
    screen.drawEndPrompt(
        screen.cursorPosition.withRelativeRow(+1)
    )
    screen.refresh()
    screen.readInput()
    screen.stopScreen()
}

fun Screen.drawEndPrompt(position: TerminalPosition) {
    val prompt = "Press any key to quit..."
    val size = this.terminalSize.columns
    val centeredCol = (size / 2) - (prompt.length / 2)
    this.newTextGraphics().putString(
        position.withColumn(centeredCol).also { this.cursorPosition = it.withRelativeColumn(prompt.length) },
        prompt,
    )
}

fun Screen.drawExpectedActualResult(
    expected: List<List<Char>>,
    actual: String,
    printableWidth: Int,
    position: TerminalPosition
) {
    this.newTextGraphics().putString(position, "Expected: ")
    this.drawWords(
        expected,
        this.cursorPosition.withColumn(this.terminalSize.columns / 2 - printableWidth / 2).withRelativeRow(+1)
    )
    val actualSplitByWidth = splitCharArrayByWidth(actual.toCharArray(), printableWidth)
    this.newTextGraphics().putString(this.cursorPosition.withRelativeRow(+1), "Actual: ")
    this.drawWords(
        actualSplitByWidth,
        this.cursorPosition.withColumn(this.terminalSize.columns / 2 - printableWidth / 2).withRelativeRow(+2)
    )
}

fun Screen.drawWpmResult(
    totalWords: Int,
    finalTime: Long,
    wpm: Int,
    accuracy: Int,
    position: TerminalPosition
) {
    val resultString = "$totalWords words typed in $finalTime seconds - WPM: $wpm - Acc: $accuracy%"
    val size = this.terminalSize.columns
    val centeredCol = (size / 2) - (resultString.length / 2)
    this.newTextGraphics().putString(position.withColumn(centeredCol), resultString)
}

fun Screen.drawWords(words: List<List<Char>>, position: TerminalPosition) {
    val text = this.newTextGraphics()
    var position = position
    words.forEach {
        text.putString(position, it.joinToString(separator = ""))
        position = position.withRelativeRow(+1)
    }
    this.cursorPosition = position
}

fun Screen.drawCharacter(char: Char, position: TerminalPosition, color: TextColor.RGB) {
    val tc = fromCharacter(char)
    tc.firstOrNull()?.let {
        this.setCharacter(position, it.withForegroundColor(color))
    }
}

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
