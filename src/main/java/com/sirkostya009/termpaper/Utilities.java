package com.sirkostya009.termpaper;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;

public final class Utilities {
    public static FileChooser genericChooser(String title) {
        var chooser = new FileChooser();

        chooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath().toString()));
        chooser.setInitialFileName("unnamed_state");
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data Files", "*.state", "*.dat", "*.bin")
        );

        return chooser;
    }

    public static Image imageFrom(String name) {
        return new Image(Objects.requireNonNull(Main.class.getResourceAsStream(name + ".png")));
    }

    public static <T extends Serializable> T loadState(File file) throws IOException, ClassNotFoundException {
        return (T) new ObjectInputStream(new FileInputStream(file)).readObject();
    }

    public static <T extends Serializable> void saveState(File file, T state) throws IOException {
        new ObjectOutputStream(new FileOutputStream(file)).writeObject(state);
    }
}
