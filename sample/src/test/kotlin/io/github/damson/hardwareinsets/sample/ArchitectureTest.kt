package io.github.damson.hardwareinsets.sample

import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * The same three layers as the library it calls, asserted the same way.
 *
 * - `model` is what the viewer is set to and what it is showing. No Android,
 *   no Compose beyond the annotations that make a value observable.
 * - `ui` is every composable. It may read the model and may not touch the
 *   framework: a screen that reached for an Activity would be doing the
 *   app layer's job somewhere nobody would look for it.
 * - `app` is the window and the things only an Activity can do, including
 *   carrying the options across a recreation.
 *
 * A sample is where a reader looks to see what a library expects of them, so
 * it is held to the shape it is demonstrating rather than excused from it.
 */
class ArchitectureTest {

    private val sourceRoot: File =
        generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .map { File(it, "sample/src/main/kotlin") }
            .firstOrNull { it.isDirectory }
            ?: error("sample/src/main/kotlin not found above ${System.getProperty("user.dir")}")

    private val sources: List<File> =
        sourceRoot.walkTopDown().filter { it.extension == "kt" }.toList()

    @Test
    fun `Should be reading the sample's sources`() {
        assertThat(sources).isNotEmpty
        assertThat(layer(MODEL).map { it.name }).contains("ViewerOptions.kt", "Gallery.kt")
        assertThat(layer(UI).map { it.name }).contains("ViewerScreen.kt")
        assertThat(layer(APP).map { it.name }).contains("ViewerActivity.kt")
    }

    @Test
    fun `Should keep the framework out of the model and the ui`() {
        // The drawable and string ids a plate carries are ints; the R class is
        // generated rather than framework, and carrying an id is not the same
        // as touching a window.
        assertNoImports(importedFrom = "android.", inLayer = MODEL)
        // `Rect` crosses into the ui for the same reason the library lets it
        // cross into its own domain: it is a box of four ints. It is here so a
        // preview can be laid out against a cutout the preview window does not
        // have, which is the whole claim the library makes.
        assertNoImports(
            importedFrom = "android.",
            inLayer = UI,
            except = { it == "android.graphics.Rect" },
        )
        assertNoImports(importedFrom = "androidx.activity", inLayer = MODEL)
        assertNoImports(importedFrom = "androidx.activity", inLayer = UI)
    }

    @Test
    fun `Should keep the model free of anything that has to be drawn`() {
        // A plate names its artwork and a set of options names its policy;
        // neither needs a Modifier, a theme or a layout to be either of those.
        assertNoImports(importedFrom = "androidx.compose.ui", inLayer = MODEL)
        assertNoImports(importedFrom = "androidx.compose.foundation", inLayer = MODEL)
        assertNoImports(importedFrom = "androidx.compose.material3", inLayer = MODEL)
    }

    @Test
    fun `Should point every dependency inward`() {
        // A model may name its own labels and its own artwork: a resource id
        // is an int, and R is generated rather than a layer.
        assertNoImports(importedFrom = SAMPLE, inLayer = MODEL, except = { it == "$SAMPLE.R" })
        assertNoImports(
            importedFrom = SAMPLE,
            inLayer = UI,
            except = { it.startsWith("$SAMPLE.model.") || it == "$SAMPLE.R" },
        )
    }

    @Test
    fun `Should ask the library for the window through the app layer only`() {
        // Every call that changes the window itself goes through one file, so
        // "what does this sample do to the window" has a single answer.
        assertOnlyInLayer(importedFrom = "io.github.damson.hardwareinsets.platform", layer = APP)
    }

    private fun layer(prefix: String): List<File> =
        sources.filter { it.relativeTo(sourceRoot).path.startsWith(prefix) }

    private fun imports(file: File): List<String> =
        file.readLines()
            .filter { it.startsWith("import ") }
            .map { it.removePrefix("import ").substringBefore(" as ").trim() }

    private fun assertNoImports(
        importedFrom: String,
        inLayer: String,
        except: (String) -> Boolean = { false },
    ) {
        val offences = layer(inLayer).flatMap { file ->
            imports(file)
                .filter { it.startsWith(importedFrom) && !except(it) }
                .map { "${file.relativeTo(sourceRoot).path} imports $it" }
        }
        assertThat(offences).describedAs("$inLayer may not import $importedFrom").isEmpty()
    }

    private fun assertOnlyInLayer(importedFrom: String, layer: String) {
        val offences = sources
            .filterNot { it.relativeTo(sourceRoot).path.startsWith(layer) }
            .flatMap { file ->
                imports(file)
                    .filter { it.startsWith(importedFrom) }
                    .map { "${file.relativeTo(sourceRoot).path} imports $it" }
            }
        assertThat(offences).describedAs("only $layer may import $importedFrom").isEmpty()
    }

    private companion object {
        const val SAMPLE = "io.github.damson.hardwareinsets.sample"
        const val MODEL = "io/github/damson/hardwareinsets/sample/model"
        const val UI = "io/github/damson/hardwareinsets/sample/ui"
        const val APP = "io/github/damson/hardwareinsets/sample/app"
    }
}
