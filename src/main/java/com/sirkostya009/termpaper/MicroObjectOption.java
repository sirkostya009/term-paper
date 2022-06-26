package com.sirkostya009.termpaper;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class MicroObjectOption extends HBox {
    public MicroObjectOption(MicroObject reference) {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPrefSize(200, 50);

        getChildren().addAll(new ImageView(reference.getImage()), new Text(reference.name), new CaptainButton(reference));
    }
}
