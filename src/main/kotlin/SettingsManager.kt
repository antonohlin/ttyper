import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.hocon.encodeToConfig
import kotlin.io.path.*

@OptIn(ExperimentalSerializationApi::class)
class SettingsManager {
    val hocon = Hocon {
        encodeDefaults = true
    }
    val path = Path("settings.conf")
    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    init {
        _settings.value = getSettingsFromFile()
    }

    fun toggleSetting(option: Char) {
        // todo: pass an enum or something
        if (option == '1') {
            val updatedSettings = _settings.value.copy(detailedResult = !_settings.value.detailedResult)
            _settings.update { updatedSettings }
            saveSettingsToFile(updatedSettings)
        }
    }

    private fun getSettingsFromFile(): Settings {
        val settings = if (path.exists()) {
            val config = ConfigFactory.parseReader(path.reader())
            hocon.decodeFromConfig<Settings>(config)
        } else {
            path.createFile()
            val defaultSettings = Settings()
            saveSettingsToFile(defaultSettings)
            defaultSettings
        }
        return settings
    }

    private fun saveSettingsToFile(settings: Settings) {
        val config = hocon.encodeToConfig(settings)
        path.writer().apply {
            write(config.root().render())
        }.close()
    }
}

@Serializable
data class Settings(
    val detailedResult: Boolean = false,
)
