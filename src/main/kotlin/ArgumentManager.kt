
class ArgumentManager(val args: Array<String>) {
    val command = getCommandFromArgs(args)

    fun getCommandFromArgs(args: Array<String>): ArgCommand {
        return when {
            args.isEmpty() -> ArgCommand.None
            args.size > 1 -> ArgCommand.TooMany
            args.contains("-v") -> ArgCommand.Version()
            else -> ArgCommand.None
        }
    }
}

sealed interface ArgCommand {
    data class Version (val version: String) : ArgCommand
    data object None : ArgCommand
    data object TooMany : ArgCommand
}
