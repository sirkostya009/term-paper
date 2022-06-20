package com.sirkostya009.termpaper;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

public class Utilities {
    public static final int spacing = 15;
    public static final Pos alignment = Pos.CENTER;
    public static final boolean fillWidth = false;

    public static Button genericCreateButton() {
        var res = new Button("Create");
        res.setDefaultButton(true);
        return res;
    }

    public static Button genericCancelButton( Stage stage) {
        var res = new Button("Cancel");
        res.setCancelButton(true);
        res.setOnAction(actionEvent -> stage.close());
        return res;
    }

    public static VBox genericVBox(Node... nodes) {
        var res = new VBox(nodes);
        res.setFillWidth(fillWidth);
        res.setSpacing(spacing);
        res.setAlignment(alignment);
        return res;
    }

    public static HBox genericHBox(Node... nodes) {
        var res = new HBox(nodes);
        res.setAlignment(alignment);
        res.setSpacing(spacing);
        res.setFillHeight(fillWidth);
        return res;
    }

    public static FileChooser genericChooser() {
        var chooser = new FileChooser();

        chooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath().toString()));
        chooser.setInitialFileName("unnamed_state");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data Files", "*.state", "*.dat", "*.bin")
        );

        return chooser;
    }

    static public Image imageFrom(String name) {
        return new Image(Objects.requireNonNull(MacroObject.class.getResourceAsStream(name + ".png")));
    }

}
