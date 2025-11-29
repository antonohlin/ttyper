class ArgumentManager(args: Array<String>) {
    val command = getCommandFromArgs(args)

    private fun getCommandFromArgs(args: Array<String>): ArgCommand {
        return when {
            args.isEmpty() -> ArgCommand.None
            args.size > 1 -> ArgCommand.TooMany
            args.contains("-v") ||
                    args.contains("--version".lowercase()) -> ArgCommand.Version(Version.version)
            else -> ArgCommand.Unknown
        }
    }

    fun getHelpContent(): String {
        return """./run.sh to run
./run.sh -b to build and run
./run.sh -v or --version to print version""".trimIndent()
    }
}

sealed interface ArgCommand {
    data class Version (val version: String) : ArgCommand
    data object None : ArgCommand
    data object TooMany : ArgCommand
    data object Unknown : ArgCommand
}
