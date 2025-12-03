import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TextCharacter.fromCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.Screen

fun Screen.drawEndPrompt(position: TerminalPosition) {
    val prompt = "Press Q to quit or R to replay"
    val size = this.terminalSize.columns
    val centeredCol = (size / 2) - (prompt.length / 2)
    this.newTextGraphics().putString(
        position.withColumn(centeredCol).also { this.cursorPosition = it.withRelativeColumn(prompt.length) },
        prompt,
    )
}

fun Screen.drawExpectedActualResult(
    actual: String,
    printableWidth: Int,
    position: TerminalPosition
) {
    val actualSplitByWidth = splitCharArrayByWidth(actual.toCharArray(), printableWidth)
    this.newTextGraphics().putString(position, "Actual: ")
    this.drawWords(
        actualSplitByWidth,
        this.cursorPosition.withColumn(this.terminalSize.columns / 2 - printableWidth / 2).withRelativeRow(1)
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

/**
 * @return end position
 */
fun Screen.drawWords(words: List<List<Char>>, position: TerminalPosition): TerminalPosition {
    val text = this.newTextGraphics()
    var position = position
    words.forEach {
        text.putString(position, it.joinToString(separator = ""))
        position = position.withRelativeRow(+1)
    }
    this.cursorPosition = position
    return position
}

fun Screen.drawCharacter(char: Char, position: TerminalPosition, color: TextColor.RGB) {
    val tc = fromCharacter(char)
    tc.firstOrNull()?.let {
        this.setCharacter(position, it.withForegroundColor(color))
    }
}

fun Screen.drawHealth(position: TerminalPosition, totalHealth: Int, currentHealth: Int) {
    val tc = this.newTextGraphics()
    tc.foregroundColor = red
    val healthBar = getHealthBar(currentHealth)
    this.newTextGraphics().putString(position, "         ")
    if (totalHealth == 0) return
    tc.putString(position, healthBar)
}

fun getHealthBar(currentHealth: Int): String {
    FileLogger.log("currentHealth: $currentHealth")
    val healthBar = buildString {
        repeat(currentHealth) {
            append("\u2665 ")
        }
    }
    return healthBar
}

fun Screen.drawSettings(settings: Settings) {
    val padding = 3
    var columnPositon = 0
    val detailedResultSetting = "1. Detailed Result"
    this.newTextGraphics().putString(columnPositon, 0, detailedResultSetting)
    this.newTextGraphics().putString(columnPositon + padding, 1, "[${settings.detailedResult}] ")
    val numberOfWordsSetting = "2. Words"
    columnPositon += detailedResultSetting.length
    this.newTextGraphics().putString(columnPositon + padding, 0, numberOfWordsSetting)
    this.newTextGraphics().putString(columnPositon + (padding * 2), 1, "[${settings.numberOfWords}]")
    val difficultySetting = "3. Difficulty"
    columnPositon += numberOfWordsSetting.length
    val difficultyValue = when (settings.difficulty) {
        Difficulty.EASY -> "[ea]"
        Difficulty.MEDIUM -> "[me]"
        Difficulty.HARD -> "[ha]"
    }
    this.newTextGraphics().putString(columnPositon + (padding * 2), 0, difficultySetting)
    this.newTextGraphics().putString(columnPositon + (padding * 3), 1, difficultyValue)
    columnPositon += difficultySetting.length
    val healthSetting = "4. Health"
    val healthValue = when (settings.health) {
        Health.DISABLED -> "[-]"
        Health.HEALTH_ONE -> "[1]"
        Health.HEALTH_TWO -> "[2]"
        Health.HEALTH_THREE -> "[3]"
    }
    this.newTextGraphics().putString(columnPositon + (padding * 3), 0, healthSetting)
    this.newTextGraphics().putString(columnPositon + (padding * 4), 1, healthValue)
}