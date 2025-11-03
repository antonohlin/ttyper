import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.Properties

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

    private fun getSettingsFromFile(): Boolean {
        val file = File("src/main/kotlin/settings.properties")
        if (!file.exists()) return false
        val props = Properties()
        file.inputStream().use { props.load(it) }
        return props.getProperty("detailedResult")?.toBoolean() ?: false
    }

    private fun saveSettingsToFile(value: Boolean) {
        val props = Properties().apply { setProperty("detailedResult", value.toString()) }
        File("src/main/kotlin/settings.properties").outputStream().use { props.store(it, null) }
    }
}
