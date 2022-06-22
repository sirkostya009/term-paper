package com.sirkostya009.termpaper;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MiniMap {
    public final static double divisor = 11;

    public final World parent;
    public final ImageView view;
    public final Rectangle camera;

    public MiniMap(World parent) {
        this.parent = parent;

        view = new ImageView(parent.view.getImage());

        view.setFitHeight(parent.view.getImage().getHeight() / divisor);
        view.setFitWidth(parent.view.getImage().getWidth() / divisor);

        camera = new Rectangle(parent.getWidth() / divisor, parent.getHeight() / divisor);
        camera.setFill(new Color(1, 1, 1, .3));

        view.setOnMouseClicked(mouseEvent -> {
            var x = (mouseEvent.getSceneX() /
                    view.getFitWidth() *
                    parent.view.getImage().getWidth()) -
                    parent.getWidth() / 2;

            var y = (mouseEvent.getSceneY() /
                    view.getFitHeight() *
                    parent.view.getImage().getHeight()) -
                    parent.getHeight() / 2;

            if (x + parent.getWidth() > parent.view.getImage().getWidth())
                x = parent.view.getImage().getWidth() - parent.getWidth();
            else if (x < 0) x = 0;

            if (y + parent.getHeight() > parent.view.getImage().getHeight())
                y = parent.view.getImage().getHeight() - parent.getHeight();
            else if (y < 0) y = 0;

            parent.setPos(x, y);

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

        camera.setOnMouseClicked(view.getOnMouseClicked());

        parent.addChildren(view, camera);
    }

    public void updateCamera(double x, double y) {
        camera.setLayoutX(camera.getLayoutX() - x / divisor);
        camera.setLayoutY(camera.getLayoutY() - y / divisor);
    }

    public void push(MicroObject microObject) {
        var object = microObject.clone();
        object.setScaleX(microObject.getScaleX() / (divisor / 2));
        object.setScaleY(microObject.getScaleY() / (divisor / 2));
        object.setLayoutX((microObject.getLayoutX() - parent.view.getX()) /
                parent.view.getImage().getWidth() * view.getFitWidth() - object.getImage().getWidth() / 2);
        object.setLayoutY((microObject.getLayoutY() - parent.view.getY()) /
                parent.view.getImage().getHeight() * view.getFitHeight() - object.getImage().getHeight() / 2);
        microObject.miniMapVersion = object;
    }

    public void toFront() {
        view.toFront();
        parent.microObjects.forEach(micro -> micro.miniMapVersion.toFront());
        camera.toFront();
    }
}
