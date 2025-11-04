import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val green = TextColor.RGB(100, 200, 100)
val red = TextColor.RGB(250, 90, 90)
val white = TextColor.RGB(255, 255, 255)

fun main() {
    val terminal = DefaultTerminalFactory().createTerminal()
    val screen = TerminalScreen(terminal)
    val settingsManager = SettingsManager()
    val colSize = screen.terminalSize.columns
    val rowSize = screen.terminalSize.rows
    val numberOfWordsToType = 20
    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        settingsManager.settings.collectLatest { value ->
            screen.drawSettings(value)
            screen.refresh()
        }
    }
    screen.startScreen()
    var running = true
    while (running) {
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
        val startPosition =
            screen.cursorPosition.withColumn(colSize / 2 - printableWidth / 2).withRow((rowSize / 2.5).roundToInt())
        val lines = splitCharArrayByWidth(wordsFromFile, printableWidth)
        var letter = 0
        var line = 0
        screen.drawSettings(settingsManager.settings.value)
        screen.drawWords(lines, startPosition)
        screen.cursorPosition = startPosition
        screen.refresh()
        while (line < lines.size) {
            while (letter < lines[line].size) {
                val key = screen.readInput()
                if (key.character.isDigit()) {
                    settingsManager.toggleSetting()
                    break
                }
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
                        screen.cursorPosition =
                            screen.cursorPosition.withColumn(startPosition.column).withRelativeRow(+1)
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
        val endGameStats = getEndGameStats(
            startTime,
            numberOfWordsToType,
            wordsFromFile.size,
            errorCount,
        )
        terminal.resetColorAndSGR()
        screen.drawWpmResult(
            totalWords = numberOfWordsToType,
            finalTime = endGameStats.time,
            wpm = endGameStats.wpm,
            accuracy = endGameStats.accuracy,
            position = screen.cursorPosition.withRelativeRow(+1).also { screen.cursorPosition = it }
        )
        if (endGameStats.accuracy < 100) {
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
        var awaitingTerminationInput = true
        while (awaitingTerminationInput) {
            val endPromptInput = screen.readInput()
            when (endPromptInput.character.lowercaseChar()) {
                in '0'..'9' -> {
                    settingsManager.toggleSetting()
                }

                'q' -> {
                    awaitingTerminationInput = false
                    running = false
                }

                'r' -> {
                    screen.clear()
                    awaitingTerminationInput = false
                }
            }
        }
    }
    screen.stopScreen()
}

data class EndGameStats(
    val wpm: Int,
    val time: Long,
    val accuracy: Int,
)