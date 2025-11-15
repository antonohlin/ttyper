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

fun Screen.drawSettings(settings: Settings) {
    val padding = 3
    val detailedResultSetting = "1. Detailed Result"
    this.newTextGraphics().putString(0, 0, detailedResultSetting)
    this.newTextGraphics().putString(padding, 1, "[${settings.detailedResult}] ")
    val numberOfWordsSetting = "2. Words"
    this.newTextGraphics().putString(detailedResultSetting.length + padding, 0, numberOfWordsSetting)
    this.newTextGraphics().putString(detailedResultSetting.length + (padding*2), 1, "[${settings.numberOfWords}]")
}