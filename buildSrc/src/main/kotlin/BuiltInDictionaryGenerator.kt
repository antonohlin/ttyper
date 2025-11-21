import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class BuiltInDictionaryGenerator : DefaultTask() {
    @get:InputDirectory
    abstract val outputPath: DirectoryProperty

    @get:InputFile
    abstract val sourceText: RegularFileProperty

    private val wordListTypeName = List::class.asClassName().parameterizedBy(String::class.asClassName())

    @TaskAction
    fun generateDictionaryClass(): Path {
        val codeBlock =
            CodeBlock
                .builder()
                .add("listOf(\n")
        sourceText.get().asFile.reader().forEachLine {
            codeBlock.add("\"$it\",\n")
        }
        codeBlock.add(")")

        val file =
            FileSpec
                .builder("", "DefaultDict")
                .addProperty(
                    PropertySpec
                        .builder("DEFAULT_DICTIONARY", wordListTypeName)
                        .initializer(
                            codeBlock.build(),
                        ).build(),
                ).build()
        val path = outputPath.asFile.get()
        path.mkdirs()
        val written = file.writeTo(Path.of(path.toURI()))
        println("generated default dictionary in : $written")
        return written
    }
}
