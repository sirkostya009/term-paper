package com.sirkostya009.term_paper;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Utilities {
    public static final int spacing = 15;
    public static final Pos alignment = Pos.CENTER;
    public static final boolean fillWidth = false;

    static @NotNull Button genericCreateButton() {
        var res = new Button("Create");
        res.setDefaultButton(true);
        return res;
    }

    static @NotNull Button genericCancelButton(@NotNull Stage stage) {
        var res = new Button("Cancel");
        res.setCancelButton(true);
        res.setOnAction(actionEvent -> stage.close());
        return res;
    }

    static @NotNull VBox genericVBox(Node... nodes) {
        var res = new VBox(nodes);
        res.setFillWidth(fillWidth);
        res.setSpacing(spacing);
        res.setAlignment(alignment);
        return res;
    }

    static @NotNull HBox genericHBox(Node... nodes) {
        var res = new HBox(nodes);
        res.setAlignment(alignment);
        res.setSpacing(spacing);
        res.setFillHeight(fillWidth);
        return res;
    }

    @Contract("_ -> new")
    static public @NotNull Image imageFrom(String name) {
        return new Image(Objects.requireNonNull(MacroObject.class.getResourceAsStream(name + ".png")));
    }

}
