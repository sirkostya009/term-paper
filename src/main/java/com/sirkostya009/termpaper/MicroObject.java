package com.sirkostya009.termpaper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.sirkostya009.termpaper.World.INSTANCE;

public abstract class MicroObject extends ImageView implements Cloneable {
    private static final double spacing = 5;
    private static int counter = 0;
    public static final int SPEED = 5;

    protected final int id = counter++;
    public final String name;
    public final double scale;

    public final Text text;

    public boolean isActive = false;

    protected MicroObject miniMapVersion = null, captainObj = null;
    protected MacroObject objective = null;

    public final ArrayList<MicroObject> privateObjects = new ArrayList<>();
    public boolean isCaptain = false, isLinedUp = false;

    public enum LEVEL { NIGGER, SLAVER, MERCHANT, TOTAL }
    public final LEVEL level;

    protected MicroObject(String name, double scale, double x, double y, boolean isActive) {
        setImage(Utilities.imageFrom(getTexture()));
        this.name = name;
        this.text = new Text(name);
        this.scale = scale;

        setScaleX(scale);
        setScaleY(scale);

        move(x, y);

        if (isActive) clickAction();

        if (this instanceof Merchant) level = LEVEL.MERCHANT;
        else if (this instanceof Slaver) level = LEVEL.SLAVER;
        else if (this instanceof Nigger) level = LEVEL.NIGGER;
        else level = LEVEL.TOTAL;
    }

    public static class Nigger extends MicroObject {
        public Slaver master = null;

        public Nigger(String name, double scale, double x, double y, boolean isActive) {
            super(name, scale, x, y, isActive);
        }

        private Nigger(Nigger nigger) {
            this(nigger.name, nigger.getScaleX(), nigger.getLayoutX(), nigger.getLayoutY(), nigger.isActive);
            master = nigger.master;
        }

        @Override
        public String getTexture() {
            return "негр";
        }

        @Override
        public void doBusiness() {
            if (master == null)
                 objective = (INSTANCE.tradeShip.localMerchant != null) ? INSTANCE.tradeShip : INSTANCE.auctionHouse;
            else objective = master.objective;

            moveToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Nigger(this);
        }
    }

    public static class Slaver extends Nigger {
        public final ArrayList<Nigger> niggers = new ArrayList<>();

        public Slaver(String name, double scale, double x, double y, boolean isActive) {
            super(name, scale, x, y, isActive);
            master = this;
        }

        private Slaver(MicroObject object) {
            this(object.name, object.getScaleX(), object.getLayoutX(), object.getLayoutY(), object.isActive);
        }

        @Override
        public String getTexture() {
            return "рабовласник";
        }

        @Override
        public void doBusiness() {
            if  (objective == null) if (niggers.isEmpty())
                 objective = (INSTANCE.tradeShip.objects.isEmpty()) ? INSTANCE.auctionHouse : INSTANCE.tradeShip;
            else objective = INSTANCE.getHut();

            moveToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Slaver(super.clone());
        }
    }

    public static class Merchant extends Slaver {
        public Merchant(String name, double scale, double x, double y, boolean isActive) {
            super(name, scale, x, y, isActive);
        }

        private Merchant(MicroObject object) {
            this(object.name, object.getScaleX(), object.getLayoutX(), object.getLayoutY(), object.isActive);
        }

        @Override
        public String getTexture() {
            return "купець";
        }

        @Override
        public void doBusiness() {
            if (objective == null)
                objective = INSTANCE.tradeShip;

            moveToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Merchant(super.clone());
        }
    }

    public void clickAction() {
        isActive = !isActive;
        setImage(isActive
                ? Utilities.imageFrom(getTexture() + "A")
                : Utilities.imageFrom(getTexture())
        );
    }

    public void deselect() {
        if (isActive) clickAction();
    }

    public void move(KeyCode code) {
        if (!isActive) return;

        var x = getLayoutX();
        var y = getLayoutY();

        switch (code) {
            case W -> {
                if (-INSTANCE.view.getY() + y <= 0) return;

                for (var macro : INSTANCE.macroObjects)
                    if (macro.contains(x, y) && macro.contains(x + getImage().getWidth(), y)) return;

                move(0, -SPEED, true);
            }
            case A -> {
                if (INSTANCE.view.getX() - x > 0) return;

                for (var macro : INSTANCE.macroObjects)
                    if (macro.contains(x, y) && macro.contains(x, y + getImage().getHeight())) return;

                move(-SPEED, 0, true);
            }
            case S -> {
                if (-INSTANCE.view.getY() + y + getImage().getHeight() > INSTANCE.view.getImage().getHeight()) return;

                for (var macro : INSTANCE.macroObjects)
                    if (macro.contains(x + getImage().getWidth(), y + getImage().getHeight())
                     && macro.contains(x, y + getImage().getHeight())) return;

                move(0, SPEED, true);
            }
            case D -> {
                if (-INSTANCE.view.getX() + x + getImage().getWidth() > INSTANCE.view.getImage().getWidth()) return;

                for (var macro : INSTANCE.macroObjects)
                    if (macro.contains(x + getImage().getWidth(), y + getImage().getHeight())
                     && macro.contains(x + getImage().getWidth(), y)) return;

                move(SPEED, 0, true);
            }
        }

        miniMapVersion.move((getLayoutX() - x) / MiniMap.divisor,
                            (getLayoutY() - y) / MiniMap.divisor,
                            true);
    }

    public void moveToCaptain() {
        if (isCaptain|| isLinedUp) return;

        if (captainObj == null)
            INSTANCE.microObjects.forEach(microObject -> {
                if (microObject.isCaptain && microObject.level == level) {
                    captainObj = microObject;
                    captainObj.privateObjects.add(this);
                }
            });

        double x = 0, y = 0;

        if (absoluteY() > captainObj.absoluteY()) y = -SPEED;
        if (absoluteY() < captainObj.absoluteY()) y = SPEED;

        var position = captainObj.privateObjects.indexOf(this) + 1;
        var destinationX = captainObj.absoluteX() + (captainObj.getImage().getWidth() * position) + (spacing * position);

        if (absoluteX() > destinationX) x = -SPEED;
        if (absoluteX() < destinationX) x = SPEED;

        if (Math.abs(captainObj.absoluteY() - absoluteY()) < 5 && Math.abs(absoluteX() - destinationX) < 5)
            isLinedUp = true;

        move(x, y, true);
        miniMapVersion.move(x / MiniMap.divisor, y / MiniMap.divisor);
    }

    private final ArrayList<Image> animationTextures = makeAnimationTextures();
    private long time = new Date().getTime();
    private int textureIndex = 0;
    private static final int swapThreshold = 150;

    private ArrayList<Image> makeAnimationTextures() {
        var result = new ArrayList<Image>(4);

        for (int i = 1; i <= 4; ++i)
            result.add(Utilities.imageFrom(getTexture() + i));

        return result;
    }

    public void move(double x, double y) {
        setLayoutY(getLayoutY() + y);
        setLayoutX(getLayoutX() + x);

        text.setLayoutY(getLayoutY());
        text.setLayoutX(getLayoutX());
    }

    public void move(double x, double y, boolean animate) {
        if (animate) {
            var timer = new Date();
            if (timer.getTime() - time > swapThreshold) {
                time = timer.getTime();
                setImage(animationTextures.get(textureIndex++));
                if (textureIndex == 4) textureIndex = 0;
            }
        }

        move(x, y);
    }

    public void moveToObjective() {
        var x = 0.;
        var y = 0.;
        var centerX = centerX();
        var centerY = centerY();

        if (centerX < objective.absoluteX() ||
            centerX > objective.absoluteX() + objective.getImage().getWidth()) {
            x = objective.absoluteX() - centerX;
        }

        if (centerY < objective.absoluteY() ||
            centerY > objective.absoluteY() + objective.getImage().getHeight()) {
            y = objective.absoluteY() - centerY;
        }

        if (x == 0 && y == 0) return;

        if      (x < -SPEED) x = -SPEED;
        else if (x > SPEED) x = SPEED;
        if      (y < -SPEED) y = -SPEED;
        else if (y > SPEED) y = SPEED;

        miniMapVersion.move(x / MiniMap.divisor, y / MiniMap.divisor, true);
        move(x, y, true);
    }

    public void nullifyObjective() {
        objective = null;
    }

    public boolean isNotWithinRange(MacroObject with) {
        return !(with.absoluteX() - absoluteX() <= getImage().getWidth()  * 2) ||
               !(with.absoluteY() - absoluteY() <= getImage().getHeight() * 2);
    }

    public double centerX() {
        return absoluteX() + getImage().getWidth() / 2;
    }

    public double centerY() {
        return absoluteY() + getImage().getHeight() / 2;
    }

    public double absoluteX() {
        return Math.abs(INSTANCE.view.getX() - getLayoutX());
    }

    public double absoluteY() {
        return Math.abs(INSTANCE.view.getY() - getLayoutY());
    }

    abstract public String getTexture();

    abstract public void doBusiness();

    @Override
    public boolean contains(double x, double y) {
        var width  = getImage().getWidth() * getScaleX();
        var height = getImage().getHeight()* getScaleY();

        return x >= getLayoutX() && x <= getLayoutX() + width && y >= getLayoutY() && y <= getLayoutY() + height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MicroObject object)
            return id == object.id;

        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{name='" + name +
                "', scale=" + scale +
                ", isMini=" + (miniMapVersion != null) +
                ", objective=" + objective +
                ", isCaptain=" + isCaptain + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    abstract public MicroObject clone();

    public record MicroConfig(
            String className,
            String name,
            double x, double y,
            double scale,
            Boolean isActive,
            String objective
    ) implements Serializable {
        public MicroObject convert() {
            var x = INSTANCE.view.getX() + x();
            var y = INSTANCE.view.getY() + y();

            var result = switch (className) {
                case "Nigger" -> new Nigger(name, scale, x, y, isActive);
                case "Slaver" -> new Slaver(name, scale, x, y, isActive);
                case "Merchant" -> new Merchant(name, scale, x, y, isActive);
                default -> null;
            };

            if (result == null) return null;

            result.objective = switch (objective) {
                case "AuctionHouse" -> INSTANCE.auctionHouse;
                case "NiggerHut" -> INSTANCE.getHut();
                case "TradeShip" -> INSTANCE.tradeShip;
                default -> null;
            };

            return result;
        }
    }

    public MicroConfig convertToConfig() {
        return new MicroConfig(
                getClass().getSimpleName(),
                name,
                absoluteX(),
                absoluteY(),
                scale,
                isActive,
                objective.getClass().getSimpleName()
        );
    }
}