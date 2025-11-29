import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val green = TextColor.RGB(100, 200, 100)
val red = TextColor.RGB(250, 90, 90)
val white = TextColor.RGB(255, 255, 255)

suspend fun main(args: Array<String>) {
    val argumentManager = ArgumentManager(args)
    when (argumentManager.command) {
        is ArgCommand.Version -> {
            println(argumentManager.command.version)
            return
        }

        is ArgCommand.Unknown -> {
            println(argumentManager.getHelpContent())
            return
        }

        else -> {}
    }
    val terminal = DefaultTerminalFactory().createTerminal()
    val screen = TerminalScreen(terminal)
    val settingsManager = SettingsManager()
    val colSize = screen.terminalSize.columns
    val rowSize = screen.terminalSize.rows
    var abortGameRound = false
    val scope = CoroutineScope(Dispatchers.IO)
    val printableWidth =
        when {
            colSize * 0.7 > 100 -> 100
            colSize * 0.7 > 80 -> 80
            else -> 60
        }
    val startPosition =
        screen.cursorPosition.withColumn(colSize / 2 - printableWidth / 2).withRow((rowSize / 3))
    val healthPosition = startPosition.withRelativeRow(-1)
    var settings = settingsManager.settings.first()
    scope.launch {
        settingsManager.settings.collectLatest { value ->
            settings = value
            screen.drawSettings(value)
            screen.drawHealth(healthPosition, value.health.totalHealth, value.health.totalHealth)
            screen.refresh()
        }
    }
    screen.startScreen()
    var running = true
    while (running) {
        val wordsFromFile =
            readDictionary(
                numberOfWordsToType = settings.numberOfWords,
                difficulty = settings.difficulty,
            ).joinToString(separator = " ").toCharArray()
        var timerHasBeenStarted = false
        var startTime: Long = 0
        val rawInput = StringBuilder()
        var errorCount = 0
        val lines = splitCharArrayByWidth(wordsFromFile, printableWidth)
        var letter = 0
        var line = 0
        var gameOver = false
        screen.drawSettings(settings)
        if (settings.health != Health.DISABLED) {
            screen.drawHealth(healthPosition, settings.health.totalHealth, settings.health.totalHealth)
        }
        val endPosition = screen.drawWords(lines, startPosition)
        var currentHealth = settings.health.totalHealth
        screen.cursorPosition = startPosition
        screen.refresh()
        while (line < lines.size && !abortGameRound && !gameOver) {
            while (letter < lines[line].size) {
                val key = screen.readInput()
                if (key.character.isDigit()) {
                    val setting = key.character.toSetting()
                    settingsManager.toggleSetting(setting)
                    if (setting in roundEndingSettings) {
                        abortGameRound = true
                    }
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
                        if (settings.health != Health.DISABLED) {
                            currentHealth--
                            screen.drawHealth(healthPosition, settings.health.totalHealth, currentHealth)
                            if (currentHealth < 1) {
                                gameOver = true
                                break
                            }
                        }
                    }
                    screen.cursorPosition = screen.cursorPosition.withRelativeColumn(1)
                    if (letter + 1 == lines[line].size) {
                        screen.cursorPosition =
                            screen.cursorPosition.withColumn(startPosition.column).withRelativeRow(1)
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
                            screen.cursorPosition
                                .withColumn(startPosition.column + lines[line].size - 1)
                                .withRelativeRow(-1)
                        screen.drawCharacter(lines[line][letter], screen.cursorPosition, white)
                    }
                }
                screen.refresh()
            }
        }
        if (!abortGameRound) {
            val endGameStats =
                getEndGameStats(
                    startTime,
                    settings.numberOfWords,
                    wordsFromFile.size,
                    errorCount,
                )
            terminal.resetColorAndSGR()
            if (!gameOver) {
                screen.drawWpmResult(
                    totalWords = settings.numberOfWords,
                    finalTime = endGameStats.time,
                    wpm = endGameStats.wpm,
                    accuracy = endGameStats.accuracy,
                    position = endPosition.withRelativeRow(1).also { screen.cursorPosition = it },
                )
            }
            val shouldDrawExpectedActualResult =
                endGameStats.accuracy < 100 && settings.detailedResult
            if (shouldDrawExpectedActualResult) {
                screen.drawExpectedActualResult(
                    actual = rawInput.toString(),
                    printableWidth = printableWidth,
                    position =
                        if (gameOver) {
                            endPosition.withRelativeRow(1).also { screen.cursorPosition = it }
                        } else {
                            screen.cursorPosition.withRelativeRow(2).also { screen.cursorPosition = it }
                        },
                )
            }
            val rowBump = if (shouldDrawExpectedActualResult || gameOver) 1 else 2
            screen.drawEndPrompt(
                if (gameOver && !shouldDrawExpectedActualResult) {
                    endPosition.withRelativeRow(rowBump)
                } else {
                    screen.cursorPosition.withRelativeRow(rowBump)
                },
            )
            screen.refresh()
            var awaitingTerminationInput = true
            while (awaitingTerminationInput) {
                val endPromptInput = screen.readInput()
                endPromptInput.character.lowercaseChar().apply {
                    when {
                        this.isDigit() -> {
                            val setting = this.toSetting()
                            settingsManager.toggleSetting(setting)
                        }

                        this == 'q' -> {
                            awaitingTerminationInput = false
                            running = false
                        }

                        this == 'r' -> {
                            screen.clear()
                            awaitingTerminationInput = false
                        }
                    }
                }
            }
        }
        screen.clear()
        abortGameRound = false
    }
    screen.stopScreen()
}

data class EndGameStats(
    val wpm: Int,
    val time: Long,
    val accuracy: Int,
)
