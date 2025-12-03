@file:Suppress("ktlint:standard:no-wildcard-imports")

import com.typesafe.config.ConfigFactory
import dev.dirs.ProjectDirectories
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
    val hocon =
        Hocon {
            encodeDefaults = true
        }
    private val basepath = ProjectDirectories.from("se", "antonohlin", "ttyper").configDir
    private val path = Path(basepath, "settings.conf")
    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    init {
        _settings.value = getSettingsFromFile()
    }

    fun toggleSetting(setting: Setting?) {
        setting?.let {
            val updatedSettings =
                when (it) {
                    Setting.DETAILED_RESULT -> {
                        _settings.value.copy(detailedResult = !_settings.value.detailedResult)
                    }

                    Setting.NUMBER_OF_WORDS -> {
                        val newNumberOfWords =
                            when (_settings.value.numberOfWords) {
                                20 -> 35
                                35 -> 50
                                else -> 20
                            }
                        _settings.value.copy(numberOfWords = newNumberOfWords)
                    }

                    Setting.DIFFICULTY -> {
                        val newDifficulty =
                            when (_settings.value.difficulty) {
                                Difficulty.EASY -> Difficulty.MEDIUM
                                Difficulty.MEDIUM -> Difficulty.HARD
                                Difficulty.HARD -> Difficulty.EASY
                            }
                        _settings.value.copy(difficulty = newDifficulty)
                    }

                    Setting.HEALTH -> {
                        val newHealth =
                            when (_settings.value.health) {
                                Health.DISABLED -> Health.HEALTH_THREE
                                Health.HEALTH_ONE -> Health.DISABLED
                                Health.HEALTH_TWO -> Health.HEALTH_ONE
                                Health.HEALTH_THREE -> Health.HEALTH_TWO
                            }
                        _settings.value.copy(health = newHealth)
                    }
                }
            _settings.update { updatedSettings }
            saveSettingsToFile(updatedSettings)
        }
    }

    private fun getSettingsFromFile(): Settings {
        return runCatching {
            if (path.exists()) {
                val config = ConfigFactory.parseReader(path.reader())
                hocon.decodeFromConfig<Settings>(config)
            } else {
                Path(basepath).apply {
                    if (!exists() || !isDirectory()) {
                        createDirectory()
                    }
                }
                path.createFile()
                val defaultSettings = Settings()
                saveSettingsToFile(defaultSettings)
                defaultSettings
            }
        }.getOrDefault(Settings())
    }

    private fun saveSettingsToFile(settings: Settings) {
        runCatching {
            val config = hocon.encodeToConfig(settings)
            path
                .writer()
                .apply {
                    write(config.root().render())
                }.close()
        }
    }
}

@Serializable
data class Settings(
    val detailedResult: Boolean = false,
    val numberOfWords: Int = 20,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val health: Health = Health.DISABLED,
)

enum class Setting {
    DETAILED_RESULT,
    NUMBER_OF_WORDS,
    DIFFICULTY,
    HEALTH,
}

val roundEndingSettings = listOf(Setting.NUMBER_OF_WORDS, Setting.DIFFICULTY, Setting.HEALTH)

enum class Health(
    val totalHealth: Int,
) {
    DISABLED(0),
    HEALTH_ONE(1),
    HEALTH_TWO(2),
    HEALTH_THREE(3),
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
}
