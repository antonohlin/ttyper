class ArgumentManager(args: Array<String>) {
    val command = getCommandFromArgs(args)

    private fun getCommandFromArgs(args: Array<String>): ArgCommand {
        return when {
            args.isEmpty() -> ArgCommand.None
            "-v" in args -> ArgCommand.Version(Version.version)
            "-s" in args -> ArgCommand.Seed(getSeed(args))
            else -> ArgCommand.Unknown
        }
    }

    private fun getSeed(args: Array<String>): String? {
        return try {
            val seedIndex = args.indexOf("-s") + 1
            args.elementAt(seedIndex)
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

}

sealed interface ArgCommand {
    data class Version(val version: String) : ArgCommand
    data class Seed(val seed: String?) : ArgCommand
    data object None : ArgCommand
    data object Unknown : ArgCommand
}
