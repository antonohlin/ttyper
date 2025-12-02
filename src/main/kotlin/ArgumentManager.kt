class ArgumentManager(args: Array<String>) {
    val command = getCommandFromArgs(args)

    private fun getCommandFromArgs(args: Array<String>): ArgCommand {
        return when {
            args.isEmpty() -> ArgCommand.None
            "-v" in args || "--version" in args -> ArgCommand.Version(Version.version)
            else -> ArgCommand.Unknown
        }
    }
}

sealed interface ArgCommand {
    data class Version (val version: String) : ArgCommand
    data object None : ArgCommand
    data object Unknown : ArgCommand
}
