package com.sirkostya009.termpaper;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.sirkostya009.termpaper.World.INSTANCE;

public class MiniMap {
    public final static double divisor = 11;

    public final ImageView view;
    public final Rectangle camera;

    public MiniMap(final World instance) {
        view = new ImageView(instance.view.getImage());

        view.setFitHeight(view.getImage().getHeight() / divisor);
        view.setFitWidth(view.getImage().getWidth() / divisor);

        camera = new Rectangle(instance.getWidth() / divisor, instance.getHeight() / divisor);
        camera.setFill(new Color(1, 1, 1, .3));

        view.setOnMouseClicked(mouseEvent -> {
            var sceneWidth = INSTANCE.getWidth();
            var sceneHeight = INSTANCE.getHeight();
            var viewWidth = INSTANCE.view.getImage().getWidth();
            var viewHeight = INSTANCE.view.getImage().getHeight();

            var x = (mouseEvent.getSceneX() / view.getFitWidth() * viewWidth) - sceneWidth / 2;
            var y = (mouseEvent.getSceneY() / view.getFitHeight() * viewHeight) - sceneHeight / 2;

            if (x + sceneWidth > viewWidth)
                x = viewWidth - sceneWidth;
            else if (x < 0) x = 0;

            if (y + sceneHeight > viewHeight)
                y = viewHeight - sceneHeight;
            else if (y < 0) y = 0;

            INSTANCE.setPos(x, y);

            x = mouseEvent.getSceneX() - camera.getWidth() / 2;
            y = mouseEvent.getSceneY() - camera.getHeight() / 2;

            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x + camera.getWidth() > view.getFitWidth())
                x = view.getFitWidth() - camera.getWidth();
            if (y + camera.getHeight() > view.getFitHeight())
                y = view.getFitHeight() - camera.getHeight();

            camera.setLayoutX(x);
            camera.setLayoutY(y);
        });
        view.setOnMouseDragged(view.getOnMouseClicked());

        camera.setOnMouseClicked(view.getOnMouseClicked());
        camera.setOnMouseDragged(view.getOnMouseClicked());
    }

    public void updateCamera(double x, double y) {
        camera.setLayoutX(camera.getLayoutX() - x / divisor);
        camera.setLayoutY(camera.getLayoutY() - y / divisor);
    }

    public void push(MicroObject microObject) {
        var object = microObject.clone();
        object.setScaleX(microObject.getScaleX() / (divisor / 2));
        object.setScaleY(microObject.getScaleY() / (divisor / 2));
        object.setLayoutX(microObject.absoluteX() / INSTANCE.view.getImage().getWidth()
                * view.getFitWidth() - object.getImage().getWidth() / 2);
        object.setLayoutY(microObject.absoluteY() / INSTANCE.view.getImage().getHeight()
                * view.getFitHeight() - object.getImage().getHeight() / 2);
        microObject.miniMapVersion = object;
    }

    public void toFront() {
        view.toFront();
        INSTANCE.microObjects.forEach(micro -> micro.miniMapVersion.toFront());
        camera.toFront();
    }
}
