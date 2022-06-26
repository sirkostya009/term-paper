package com.sirkostya009.termpaper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public abstract class MicroObject extends ImageView implements Cloneable {
    private static final double spacing = 5;

    public enum LEVEL {
        NIGGER, SLAVER, MERCHANT, TOTAL
    }

    boolean linedUp = false;

    ArrayList<MicroObject> privateObjects = new ArrayList<>();

    LEVEL level;
    public static final int speed = 5;
    private static int counter = 0;

    protected final int id = counter++;
    public final String name;
    public final double scale;

    public final Text text;

    boolean isActive = false;

    public final World world;

    protected MicroObject miniMapVersion = null;
    protected MacroObject objective = null;
    protected MicroObject captainObj = null;

    protected boolean captain = false;
    public void setCaptain(boolean captain) {
        this.captain = captain;
    }

    public boolean isCaptain() {
        return captain;
    }

    public void moveToCaptain() {
        if (isCaptain()) return;
        if (linedUp) return;

        if (captainObj == null)
            world.microObjects.forEach(microObject -> {
                if (microObject.isCaptain() && microObject.level == level) {
                    captainObj = microObject;
                    captainObj.privateObjects.add(this);
                }
            });

        double x = 0, y = 0;

        if (absoluteY() > captainObj.absoluteY()) y = -speed;
        if (absoluteY() < captainObj.absoluteY()) y =  speed;

        var position = captainObj.privateObjects.indexOf(this) + 1;
        var destinationX = captainObj.absoluteX() + (captainObj.getImage().getWidth() * position) + (spacing * position);

        if (absoluteX() > destinationX) x = -speed;
        if (absoluteX() < destinationX) x =  speed;

        if (Math.abs(captainObj.absoluteY() - absoluteY()) < 5 && Math.abs(absoluteX() - destinationX) < 5)
            linedUp = true;

        move(x, y, true);
        miniMapVersion.move(x / MiniMap.divisor, y / MiniMap.divisor);
    }

    public static class Nigger extends MicroObject {
        Slaver master = null;

        public Nigger(String _name, double scale, double x, double y, boolean isActive, World parent) {
            super(_name, scale, x, y, isActive, parent);
            level = LEVEL.NIGGER;
        }

        private Nigger(Nigger nigger) {
            this(nigger.name, nigger.getScaleX(), nigger.getLayoutX(), nigger.getLayoutY(), nigger.isActive, nigger.world);
            master = nigger.master;
        }

        @Override
        public String getTexture() {
            return "негр";
        }

        @Override
        public void doBusiness() {
            if (master == null)
                objective = (((MacroObject.TradeShip) world.getShip()).localMerchant != null) ? world.getShip() : world.getAuctionHouse();
            else objective = master.objective;

            goToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Nigger(this);
        }
    }

    public static class Slaver extends Nigger {
        public final ArrayList<Nigger> niggers = new ArrayList<>();

        public Slaver(String _name, double scale, double x, double y, boolean isActive, World parent) {
            super(_name, scale, x, y, isActive, parent);
            master = this;
            level = LEVEL.SLAVER;
        }

        private Slaver(Slaver slaver) {
            this(slaver.name, slaver.getScaleX(), slaver.getLayoutX(), slaver.getLayoutY(), slaver.isActive, slaver.world);
        }

        @Override
        public String getTexture() {
            return "рабовласник";
        }

        @Override
        public void doBusiness() {
            if  (objective == null) if (niggers.isEmpty())
                 objective = (world.getShip().objects.isEmpty()) ? world.getAuctionHouse() : world.getShip();
            else objective = world.getHut();

            goToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Slaver(this);
        }
    }

    public static class Merchant extends Slaver {
        public Merchant(String _name, double scale, double x, double y, boolean isActive, World parent) {
            super(_name, scale, x, y, isActive, parent);
            level = LEVEL.MERCHANT;
        }

        private Merchant(Merchant merchant) {
            this(merchant.name, merchant.getScaleX(), merchant.getLayoutX(), merchant.getLayoutY(), merchant.isActive, merchant.world);
        }

        @Override
        public String getTexture() {
            return "купець";
        }

        @Override
        public void doBusiness() {
            if (objective == null)
                objective = world.getShip();

            goToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Merchant(this);
        }
    }

    protected MicroObject(String _name, double scale, double x, double y, boolean isActive, World parent) {
        setImage(Utilities.imageFrom(getTexture()));
        world = parent;
        name = _name;

        setLayoutX(x);
        setLayoutY(y);

        this.scale = scale;
        setScaleX(scale);
        setScaleY(scale);

        text = new Text(name);
        text.setLayoutX(x);
        text.setLayoutY(y);

        if (isActive) clickAction();
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
                if (-world.view.getY() + y <= 0) return;

                for (var macro : world.macroObjects)
                    if (macro.contains(x, y) && macro.contains(x + getImage().getWidth(), y)) return;

                move(0, -speed, true);
            }
            case A -> {
                if (world.view.getX() - x > 0) return;

                for (var macro : world.macroObjects)
                    if (macro.contains(x, y) && macro.contains(x, y + getImage().getHeight())) return;

                move(-speed, 0, true);
            }
            case S -> {
                if (-world.view.getY() + y + getImage().getHeight() > world.view.getImage().getHeight()) return;

                for (var macro : world.macroObjects)
                    if (macro.contains(x + getImage().getWidth(), y + getImage().getHeight())
                     && macro.contains(x, y + getImage().getHeight())) return;

                move(0, speed, true);
            }
            case D -> {
                if (-world.view.getX() + x + getImage().getWidth() > world.view.getImage().getWidth()) return;

                for (var macro : world.macroObjects)
                    if (macro.contains(x + getImage().getWidth(), y + getImage().getHeight())
                     && macro.contains(x + getImage().getWidth(), y)) return;

                move(speed, 0, true);
            }
        }

        miniMapVersion.move((getLayoutX() - x) / MiniMap.divisor,
                            (getLayoutY() - y) / MiniMap.divisor,
                            true);
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

    public boolean isWithinRange(MacroObject with) {
        return with.absoluteX() - absoluteX() <= getImage().getWidth()  * 2 &&
               with.absoluteY() - absoluteY() <= getImage().getHeight() * 2;
    }

    public double centerX() {
        return absoluteX() + getImage().getWidth() / 2;
    }

    public double centerY() {
        return absoluteY() + getImage().getHeight() / 2;
    }

    public void goToObjective() {
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

        if      (x < -speed) x = -speed;
        else if (x >  speed) x =  speed;
        if      (y < -speed) y = -speed;
        else if (y >  speed) y =  speed;

        miniMapVersion.move(x / MiniMap.divisor, y / MiniMap.divisor, true);
        move(x, y, true);
    }

    public double absoluteX() {
        return Math.abs(world.view.getX() - getLayoutX());
    }

    public double absoluteY() {
        return Math.abs(world.view.getY() - getLayoutY());
    }

    abstract public String getTexture();

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
                "{id=" + id +
                ", name='" + name + '\'' +
                ", scale=" + scale +
                ", text='" + text.getText() + '\'' +
                ", isActive=" + isActive +
                ", destination=" + objective +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    abstract public void doBusiness();

    public void nullifyObjective() {
        objective = null;
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
        public MicroObject convert(World parent) {
            var x = parent.view.getX() + x();
            var y = parent.view.getY() + y();

            var result = switch (className) {
                case "Nigger" -> new Nigger(name, scale, x, y, isActive, parent);
                case "Slaver" -> new Slaver(name, scale, x, y, isActive, parent);
                case "Merchant" -> new Merchant(name, scale, x, y, isActive, parent);
                default -> null;
            };

            if (result == null) return null;

            result.objective = switch (objective) {
                case "AuctionHouse" -> parent.getAuctionHouse();
                case "NiggerHut" -> parent.getHut();
                case "TradeShip" -> parent.getShip();
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