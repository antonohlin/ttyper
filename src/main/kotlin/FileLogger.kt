import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writer
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

object FileLogger {
    val path = Path("logger")
    val writer = PrintWriter(path.writer())

    init {
        if (!path.exists()) {
            path.createFile()
        }
    }

    @OptIn(ExperimentalTime::class)
    fun log(message: String) {
        val timestamp = Clock.System.now().toJavaInstant()
        val format = SimpleDateFormat("HH:mm:ss.SSS")
        val date = format.format(Date.from(timestamp))
        writer.apply {
            println("$date $message")
        }.flush()
    }
}