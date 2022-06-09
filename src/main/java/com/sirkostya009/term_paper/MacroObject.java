package com.sirkostya009.term_paper;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

abstract public class MacroObject extends ImageView {
    ArrayList<MicroObject> objects = new ArrayList<>();
    World world;

    protected MacroObject(double scale, int posX, int posY, World parent, Image texture) {
        super(texture);
        world = parent;

        setScaleX(scale);
        setScaleY(scale);

        setLayoutX(posX);
        setLayoutY(posY);
    }

    @Override
    public boolean contains(double x, double y) {
        var width = getImage().getWidth() * getScaleX();
        var height = getImage().getHeight() * getScaleY();

        return x >= getLayoutX() && x <= getLayoutX() + width && y >= getLayoutY() && y <= getLayoutY() + height;
    }

    ContextMenu menu(int x, int y) {
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

    void freeMicro(MicroObject micro, double x, double y) {
        micro.setLayoutX(x);
        micro.setLayoutY(y);

        micro.text.setLayoutX(x);
        micro.text.setLayoutY(y);

        world.addChildren(micro, micro.text);
        world.microObjects.add(micro);
        world.miniMap.push(micro);
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

    MenuItem newMicroObject(int x, int y) {
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
            nigger.awaiting = false;
            nigger.objective = newMaster.objective;
            nigger.reachedObjective = false;
            nigger.master = newMaster;
            newMaster.niggers.add(nigger);
            freeMicro(nigger, x[0], y);
            x[0] += newMaster.getImage().getWidth();
            nigger.awaiting = false;
            return true;
        });
    }

    public double absoluteX() {
        return Math.abs(world.view.getX() - getLayoutX());
    }

    public double absoluteY() {
        return Math.abs(world.view.getY() - getLayoutY());
    }

    static class AuctionHouse extends MacroObject {

        public AuctionHouse(double scale, int posX, int posY, World world) {
            super(scale, posX, posY, world, Utilities.imageFrom("хата"));
        }

        @Override
        public void interactWith(MicroObject object) {
            if (object instanceof MicroObject.Slaver slaver) {
                if (objects.isEmpty()) return;
                slaver.objective = (slaver instanceof MicroObject.Merchant) ? world.getShip() : world.getHut();
                slaver.reachedObjective = false;
                slaver.awaiting = false;
                getNiggersOut(slaver);
            }
            else if (object instanceof MicroObject.Nigger nigger) {
                objects.add(nigger);
                nigger.world.removeMicro(nigger);
                nigger.awaiting = true;
                nigger.objective = null;
            }
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    static class TradeShip extends MacroObject {

        public TradeShip(double scale, int posX, int posY, World world) {
            super(scale, posX, posY, world, Utilities.imageFrom("корабель"));
        }

        @Override
        public void interactWith(MicroObject object) {
            if (object instanceof MicroObject.Merchant merchant) {
                objects.add(merchant);
                merchant.world.removeMicro(merchant);
                merchant.reachedObjective = false;
                merchant.objective = world.getAuctionHouse();
            }
            else if (object instanceof MicroObject.Slaver slaver) {
                if (objects.isEmpty()) return;
                slaver.objective = world.getHut();
                slaver.reachedObjective = false;
                slaver.awaiting = false;
                getNiggersOut(slaver);
            }
        }

        @Override
        public Point2D getStartingPos() {
            return new Point2D(getLayoutX() - 30,getLayoutY() - 40);
        }

    }

    static class NiggerHut extends MacroObject {

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

}