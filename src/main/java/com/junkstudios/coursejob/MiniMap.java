package com.junkstudios.coursejob;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

public class MiniMap {
    static final double divisor = 4.0;

    public final World parent;
    public final ImageView view;
    public final Rectangle camera;

    public MiniMap(@NotNull World parent) {
        this.parent = parent;

        view = new ImageView(parent.view.getImage());

        view.setFitHeight(parent.height / divisor);
        view.setFitWidth(parent.width / divisor);

        camera = new Rectangle(view.getFitWidth() / divisor, view.getFitHeight() / divisor);
        camera.setFill(new Color(1, 1, 1, .3));

        view.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            var x = (mouseEvent.getSceneX() /
                    view.getFitWidth() *
                    parent.view.getImage().getWidth()) -
                    parent.width / 2;

            var y = (mouseEvent.getSceneY() /
                    view.getFitHeight() *
                    parent.view.getImage().getHeight()) -
                    parent.height / 2;

            parent.setPos(x, y);

            x = mouseEvent.getSceneX() - camera.getWidth() / 2;
            y = mouseEvent.getSceneY() - camera.getHeight() / 2;

            camera.setLayoutX(x);
            camera.setLayoutY(y);
        });

        parent.addChildren(view, camera);
    }

    public void updateCamera(double x, double y) {
        camera.setLayoutX(camera.getLayoutX() - x / Math.pow(divisor, 2));
        camera.setLayoutY(camera.getLayoutY() - y / Math.pow(divisor, 2));
    }

    public void push(MicroObject microObject) {
        var object = microObject.clone();
        object.setScaleX(microObject.getScaleX() / divisor);
        object.setScaleY(microObject.getScaleY() / divisor);
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
