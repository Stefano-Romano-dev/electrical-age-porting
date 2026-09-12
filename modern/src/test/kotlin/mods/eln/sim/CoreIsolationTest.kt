package mods.eln.sim

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension

class CoreIsolationTest {
    @Test
    fun `simulation core has no Minecraft or NeoForge imports`() {
        val projectRoot = Path.of(System.getProperty("eln.projectDir", "."))
        val sourceRoots = listOf(
            projectRoot.resolve(Path.of("src", "main", "kotlin", "mods", "eln", "sim")),
            projectRoot.resolve(Path.of("src", "main", "kotlin", "mods", "eln", "misc")),
        )
        sourceRoots.forEach { sourceRoot ->
            assertTrue(Files.isDirectory(sourceRoot), "Missing pure source root: $sourceRoot")
            Files.walk(sourceRoot).use { paths ->
                paths.filter { Files.isRegularFile(it) && it.extension == "kt" }
                    .forEach { source ->
                        val text = Files.readString(source)
                        assertFalse(text.contains("import net.minecraft"), "$source imports Minecraft")
                        assertFalse(text.contains("import net.neoforged"), "$source imports NeoForge")
                    }
            }
        }
    }
}
