import com.typesafe.config.ConfigRenderOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.encodeToConfig
import java.io.File
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream

class SettingsManager {
    private val _settings = MutableStateFlow(false)
    val settings = _settings.asStateFlow()

    init {
        _settings.value = getSettingsFromFile()
    }

    fun toggleSetting() {
        _settings.update { current ->
            val newValue = !current
            saveSettingsToFile(newValue)
            newValue
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getSettingsFromFile(): Boolean {
        val path = Path("settings.conf")
        if (!path.exists()) {
            path.createFile()
            val conf = Hocon.encodeToConfig(Settings())
            conf.root().render(ConfigRenderOptions.defaults().setFormatted(true))
        }
        val props = Properties()
        path.inputStream().use { props.load(it) }
        return props.getProperty("detailedResult")?.toBoolean() ?: false
    }

    private fun saveSettingsToFile(value: Boolean) {
        val props = Properties().apply { setProperty("detailedResult", value.toString()) }
        File("src/main/kotlin/settings.properties").outputStream().use { props.store(it, null) }
    }
}

@Serializable
data class Settings(
    val detailedResult: Boolean = false,
)
