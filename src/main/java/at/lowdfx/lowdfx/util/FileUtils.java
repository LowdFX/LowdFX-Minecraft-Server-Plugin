package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Objects;

public final class FileUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(Object object, String file) {
        LowdFX.DATA_DIR.resolve(file).toFile().delete();
        try (FileWriter writer = new FileWriter(LowdFX.DATA_DIR.resolve(file).toFile())) {
            GSON.toJson(object, writer);
        } catch (Exception e) {
            LowdFX.LOG.error("Fehler beim Speichern von: {}", file, e);
        }
    }

    public static <T> @Nullable T load(String file) {
        if (Files.notExists(LowdFX.DATA_DIR.resolve(file)))
            return null;

        try (FileReader reader = new FileReader(LowdFX.DATA_DIR.resolve(file).toFile())) {
            return GSON.fromJson(reader, new TypeToken<>() {});
        } catch (Exception e) {
            LowdFX.LOG.error("Fehler beim Laden von: {}", file, e);
        }

        return null;
    }

    public static <T> @NotNull T load(String file, @NotNull T def) {
        return Objects.requireNonNullElse(load(file), def);
    }
}
