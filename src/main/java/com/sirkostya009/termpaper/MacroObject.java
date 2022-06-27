package com.sirkostya009.termpaper;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;

import static com.sirkostya009.termpaper.World.INSTANCE;

abstract public class MacroObject extends ImageView {
    public final ArrayList<MicroObject> objects = new ArrayList<>();

    protected MacroObject(double scale, double posX, double posY) {
        setImage(Utilities.imageFrom(getTexture()));

        setScaleX(scale);
        setScaleY(scale);

        setLayoutX(posX);
        setLayoutY(posY);

        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY)
                menu(mouseEvent.getSceneX(), mouseEvent.getSceneY())
                    .show(INSTANCE.getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
        });
    }

    protected MacroObject(double x, double y) {
        this(1, x, y);
    }

    abstract public void interactWith(MicroObject object);

    abstract public String getTexture();

    public static class AuctionHouse extends MacroObject {
        public AuctionHouse(int posX, int posY) {
            super(posX, posY);
        }

        @Override
        public void interactWith(MicroObject object) { // house interaction
            if (object.isNotWithinRange(this)) return;

            if (object instanceof MicroObject.Slaver slaver) {
                if (objects.isEmpty()) return;
                slaver.objective = (slaver instanceof MicroObject.Merchant) ? INSTANCE.tradeShip : INSTANCE.getHut();
                getNiggersOut(slaver);
            } else if (object instanceof MicroObject.Nigger nigger) {
                objects.add(nigger);
                INSTANCE.removeMicro(nigger);
                nigger.objective = null;
            }
        }

        @Override
        public String getTexture() {
            return "хата";
        }
    }

    public static class TradeShip extends MacroObject {
        public TradeShip(double posX, double posY) {
            super(posX, posY);
        }

        public MicroObject.Merchant localMerchant = null;

        @Override
        public void interactWith(MicroObject object) { // ship interaction
            if (object.isNotWithinRange(this)) return;

            if (object instanceof MicroObject.Merchant merchant) {
                localMerchant = merchant;
                if (objects.isEmpty()) return;
                merchant.objective = INSTANCE.auctionHouse;
                getNiggersOut(merchant);
                localMerchant = null;
            } else if (object instanceof MicroObject.Slaver slaver) {
                if (objects.isEmpty()) return;
                slaver.objective = INSTANCE.getHut();
                getNiggersOut(slaver);
            } else if (object instanceof MicroObject.Nigger nigger) {
                objects.add(nigger);
                INSTANCE.removeMicro(nigger);
            }
        }

        @Override
        public String getTexture() {
            return "корабель";
        }

        @Override
        public Point2D getStartingPos() {
            return new Point2D(getLayoutX() - 30,getLayoutY() - 40);
        }
    }

    public static class NiggerHut extends MacroObject {
        public NiggerHut(double scale, double posX, double posY) {
            super(scale, posX, posY);
        }

        @Override
        public void interactWith(MicroObject object) { // hut interaction
            if (object.isNotWithinRange(this)) return;

            if (object instanceof MicroObject.Slaver slaver) {
                var hut = INSTANCE.getHut();
                slaver.objective = (slaver.objective == hut) ? INSTANCE.getHut() : hut;
                getNiggersOut(slaver);
            } else {
                objects.add(object);
                INSTANCE.removeMicro(object);
            }
        }

        @Override
        public String getTexture() {
            return "халупа";
        }
    }

    public Point2D getStartingPos() {
        return new Point2D(
                getLayoutX() + getImage().getWidth() / 2 - (30 * (objects.size() - 1)),
                getLayoutY() + getImage().getHeight()
        );
    }

    public double absoluteX() {
        return Math.abs(INSTANCE.view.getX() - getLayoutX());
    }

    public double absoluteY() {
        return Math.abs(INSTANCE.view.getY() - getLayoutY());
    }

    @Override
    public boolean contains(double x, double y) {
        var width = getImage().getWidth() * getScaleX();
        var height = getImage().getHeight() * getScaleY();

        return x >= getLayoutX() && x <= getLayoutX() + width && y >= getLayoutY() && y <= getLayoutY() + height;
    }

    public void move(double x, double y) {
        setLayoutY(getLayoutY() + y);
        setLayoutX(getLayoutX() + x);
    }

    public void freeMicro(MicroObject micro, double x, double y) {
        micro.setLayoutX(x);
        micro.setLayoutY(y);

        micro.text.setLayoutX(x);
        micro.text.setLayoutY(y);

        INSTANCE.miniMap.push(micro);
        INSTANCE.addChildren(micro, micro.text, micro.miniMapVersion);
    }

    protected void getNiggersOut(MicroObject.Slaver newMaster) {
        final double[] x = {getStartingPos().getX()};
        var y = getStartingPos().getY();

        objects.removeIf(object1 -> {
            if (!(object1 instanceof MicroObject.Nigger nigger)) return false;
            if (newMaster instanceof MicroObject.Merchant && getClass() != TradeShip.class) return false;
            System.out.println("aha!");
            nigger.objective = newMaster.objective;
            nigger.master = newMaster;
            newMaster.niggers.add(nigger);
            freeMicro(nigger, x[0], y);
            x[0] += newMaster.getImage().getWidth();
            return true;
        });
    }

    public ContextMenu menu(double x, double y) {
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

            for (var micro : INSTANCE.microObjects)
                if (micro.isActive) toInteract.add(micro);

            toInteract.forEach(this::interactWith);
        });
        return res;
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

        res.setOnAction(actionEvent -> MicroObjectCreator.call(x, y, objects::add));

        return res;
    }
}
