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

    fun toggleSetting(setting: Setting?) {
        setting?.let {
            val updatedSettings = when (it) {
                Setting.DETAILED_RESULT -> {
                   _settings.value.copy(detailedResult = !_settings.value.detailedResult)
                }

                Setting.NUMBER_OF_WORDS -> {
                    val newNumberOfWords = when (_settings.value.numberOfWords) {
                        20 -> 35
                        35 -> 50
                        else -> 20
                    }
                    _settings.value.copy(numberOfWords = newNumberOfWords)
                }

                Setting.DIFFICULTY -> {
                    val newDifficulty = when (_settings.value.difficulty) {
                        Difficulty.EASY -> Difficulty.MEDIUM
                        Difficulty.MEDIUM -> Difficulty.HARD
                        Difficulty.HARD -> Difficulty.EASY
                    }
                   _settings.value.copy(difficulty = newDifficulty)
                }
            }
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
    val numberOfWords: Int = 20,
    val difficulty: Difficulty = Difficulty.MEDIUM,
)

enum class Setting {
    DETAILED_RESULT,
    NUMBER_OF_WORDS,
    DIFFICULTY,
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
}