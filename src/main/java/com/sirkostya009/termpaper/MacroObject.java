package com.sirkostya009.termpaper;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

abstract public class MacroObject extends ImageView {
    public final ArrayList<MicroObject> objects = new ArrayList<>();
    public final World world;

    protected MacroObject(double scale, int posX, int posY, World parent, Image texture) {
        super(texture);
        world = parent;

        setScaleX(scale);
        setScaleY(scale);

        setLayoutX(posX);
        setLayoutY(posY);

        setOnMouseClicked(mouseEvent -> menu(mouseEvent.getSceneX(), mouseEvent.getSceneY())
                                            .show(world.getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY()));
    }

    public static class AuctionHouse extends MacroObject {
        public AuctionHouse(int posX, int posY, World world) {
            super(1, posX, posY, world, Utilities.imageFrom("хата"));
        }

        @Override
        public void interactWith(MicroObject object) {
            if (!object.isWithinRange(this)) return;

            if (object instanceof MicroObject.Slaver slaver) {
                if (objects.isEmpty()) return;
                slaver.objective = (slaver instanceof MicroObject.Merchant) ? world.getShip() : world.getHut();
                getNiggersOut(slaver);
            }
            else if (object instanceof MicroObject.Nigger nigger) {
                objects.add(nigger);
                nigger.world.removeMicro(nigger);
                nigger.objective = null;
            }
        }
    }

    public static class TradeShip extends MacroObject {
        public TradeShip(int posX, int posY, World world) {
            super(1, posX, posY, world, Utilities.imageFrom("корабель"));
        }

        @Override
        public void interactWith(MicroObject object) {
            if (object instanceof MicroObject.Merchant merchant) {
                objects.add(merchant);
                merchant.world.removeMicro(merchant);
                merchant.objective = world.getAuctionHouse();
            }
            else if (object instanceof MicroObject.Slaver slaver) {
                if (objects.isEmpty()) return;
                slaver.objective = world.getHut();
                getNiggersOut(slaver);
            }
        }

        @Override
        public Point2D getStartingPos() {
            return new Point2D(getLayoutX() - 30,getLayoutY() - 40);
        }
    }

    public static class NiggerHut extends MacroObject {
        public NiggerHut(double scale, int posX, int posY, World world) {
            super(scale, posX, posY, world, Utilities.imageFrom("халупа"));
        }

        @Override
        public void interactWith(MicroObject object) {
            if (object instanceof MicroObject.Slaver) return;
            objects.add(object);
            object.world.removeMicro(object);
        }
    }

    @Override
    public boolean contains(double x, double y) {
        var width = getImage().getWidth() * getScaleX();
        var height = getImage().getHeight() * getScaleY();

        return x >= getLayoutX() && x <= getLayoutX() + width && y >= getLayoutY() && y <= getLayoutY() + height;
    }

    ContextMenu menu(double x, double y) {
        return new ContextMenu(
                   newMicroObject(x, y),
                   getAllMicrosOut(),
                   interactWithSelected()
        );
    }

    private MenuItem interactWithSelected() {
        var res = new MenuItem("Interact with selected");
        res.setOnAction(actionEvent -> {
            var toInteract = new ArrayList<MicroObject>();

            for (var micro : world.microObjects)
                if (micro.isActive) toInteract.add(micro);

            toInteract.forEach(this::interactWith);
        });
        return res;
    }

    private void freeMicro(MicroObject micro, double x, double y) {
        micro.setLayoutX(x);
        micro.setLayoutY(y);

        micro.text.setLayoutX(x);
        micro.text.setLayoutY(y);

        world.microObjects.add(micro);
        world.miniMap.push(micro);
        world.addChildren(micro, micro.text, micro.miniMapVersion);
    }

    private MenuItem getAllMicrosOut() {
        var res = new MenuItem("Free all micros");

        res.setOnAction(actionEvent -> {
            var x = getStartingPos().getX();
            var y = getStartingPos().getY();

            for (var micro : objects) {
                freeMicro(micro, x, y);
                x += micro.getImage().getWidth();
            }
        });

        return res;
    }

    private MenuItem newMicroObject(double x, double y) {
        var res = new MenuItem("New MicroObject here...");

        res.setOnAction(actionEvent -> {
            var creator = new MicroObjectCreator(x, y);
            creator.setCreateAction(actionEvent1 -> {
                var object = creator.makeMicroObject(world);
                objects.add(object);
                creator.close();
            });
        });

        return res;
    }

    abstract public void interactWith(MicroObject object);

    public Point2D getStartingPos() {
        return new Point2D(
                getLayoutX() + getImage().getWidth() / 2 - (30 * (objects.size() - 1)),
                getLayoutY() + getImage().getHeight()
        );
    }

    public void move(double x, double y) {
        setLayoutY(getLayoutY() + y);
        setLayoutX(getLayoutX() + x);
    }

    protected void getNiggersOut(MicroObject.Slaver newMaster) {
        final double[] x = {getStartingPos().getX()};
        var y = getStartingPos().getY();

        objects.removeIf(object1 -> {
            if (!(object1 instanceof MicroObject.Nigger nigger)) return false;
            nigger.objective = newMaster.objective;
            nigger.master = newMaster;
            newMaster.niggers.add(nigger);
            freeMicro(nigger, x[0], y);
            x[0] += newMaster.getImage().getWidth();
            return true;
        });
    }

    public double absoluteX() {
        return Math.abs(world.view.getX() - getLayoutX());
    }

    public double absoluteY() {
        return Math.abs(world.view.getY() - getLayoutY());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}