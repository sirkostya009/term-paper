package com.junkstudios.coursejob;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public abstract class MicroObject extends ImageView implements Comparable<MicroObject>, Cloneable {

    private final ArrayList<Image> animationTextures = new ArrayList<>(4);
    private long time = new Date().getTime();
    private int textureIndex = 0;
    private static final int swapThreshold = 150;

    public static final int speed = 5;
    private static int counter = 0;

    protected final int id = counter++;
    public final String name;
    public final double scale;

    public final Text text;

    boolean isActive = false;
    boolean reachedObjective = false;

    public final World world;

    protected MicroObject miniMapVersion = null;
    protected MacroObject objective = null;
    protected boolean awaiting = false;

    static {}
    {}

    protected MicroObject(String _name, double scale, int x, int y, boolean isActive, World parent, Image image) {
        super(image);
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

        for (int i = 1; i <= 4; ++i)
            animationTextures.add(Utilities.imageFrom(getTexture() + i));
    }

    private MicroObject() {
        this(null, 0.0, 0, 0, false, null, null);
    } // will crash btw

    public void clickAction() {
        isActive = !isActive;
        if (isActive)
             setImage(Utilities.imageFrom(getTexture() + "A"));
        else setImage(Utilities.imageFrom(getTexture()));
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

                move(0, -speed);
            }
            case A -> {
                if (world.view.getX() - x > 0) return;

                for (var macro : world.macroObjects)
                    if (macro.contains(x, y) && macro.contains(x, y + getImage().getHeight())) return;

                move(-speed, 0);
            }
            case S -> {
                if (-world.view.getY() + y + getImage().getHeight() > world.view.getImage().getHeight()) return;

                for (var macro : world.macroObjects)
                    if (macro.contains(x + getImage().getWidth(), y + getImage().getHeight())
                            && macro.contains(x, y + getImage().getHeight())) return;

                move(0, speed);
            }
            case D -> {
                if (-world.view.getX() + x + getImage().getWidth() > world.view.getImage().getWidth()) return;

                for (var macro : world.macroObjects)
                    if (macro.contains(x + getImage().getWidth(), y + getImage().getHeight())
                            && macro.contains(x + getImage().getWidth(), y)) return;

                move(speed, 0);
            }
            default -> { return; }
        }

        miniMapVersion.move((getLayoutX() - x) / Math.pow(MiniMap.divisor, 2),
                            (getLayoutY() - y) / Math.pow(MiniMap.divisor, 2));
    }

    public void move(double x, double y) {
        var timer = new Date();
        if (timer.getTime() - time > swapThreshold) {
            time = timer.getTime();
            setImage(animationTextures.get(textureIndex++));
            if (textureIndex == 4) textureIndex = 0;
        }

        setLayoutY(getLayoutY() + y);
        setLayoutX(getLayoutX() + x);

        text.setLayoutY(getLayoutY());
        text.setLayoutX(getLayoutX());
    }

    public boolean isWithinRange(@NotNull MacroObject with) {
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
        int x = 0, y = 0;
        double centerX = centerX();
        double centerY = centerY();

        if (centerX < objective.absoluteX() ||
                centerX > objective.absoluteX() + objective.getImage().getWidth()) {
            x = (int) (objective.absoluteX() - centerX);
        }

        if (centerY < objective.absoluteY() ||
                centerY > objective.absoluteY() + objective.getImage().getHeight()) {
            y = (int) (objective.absoluteY() - centerY);
        }

        if (x == 0 && y == 0) {
            reachedObjective = true;
            return;
        }

        if      (x < -speed) x = -speed;
        else if (x >  speed) x =  speed;
        if      (y < -speed) y = -speed;
        else if (y >  speed) y =  speed;

        miniMapVersion.move(x / Math.pow(MiniMap.divisor, 2), y / Math.pow(MiniMap.divisor, 2));
        move(x, y);
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
    public int compareTo(@NotNull MicroObject o) {
        return id - o.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MicroObject that = (MicroObject) o;

        if (Double.compare(that.scale, scale) != 0) return false;
        if (!Objects.equals(name, that.name)) return false;
        return text != null ? text.getText().equals(that.text.getText()) : that.text == null;
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

    @Override
    abstract public MicroObject clone();

    static class Nigger extends MicroObject {
        static final String textureString = "негр";

        Slaver master = null;

        protected Nigger(String _name, double scale, int x, int y, boolean isActive, World parent, Image image) {
            super(_name, scale, x, y, isActive, parent, image);
        }

        public Nigger(String _name, double scale, int x, int y, boolean isActive, World parent) {
            super(_name, scale, x, y, isActive, parent, Utilities.imageFrom(textureString));
        }

        public Nigger(Nigger nigger) {
            this(nigger.name, nigger.getScaleX(), (int) nigger.getLayoutX(), (int) nigger.getLayoutY(), nigger.isActive, nigger.world);
            master = nigger.master;
        }

        @Override
        public String getTexture() {
            return textureString;
        }

        @Override
        public void doBusiness() {
            if (awaiting) return;

            if (master == null)
                 objective = world.getAuctionHouse();
            else objective = master.objective;

            if (!reachedObjective)
                goToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Nigger(this);
        }

    }

    static class Slaver extends Nigger {
        static final String textureString = "рабовласник";

        ArrayList<Nigger> niggers = new ArrayList<>();

        protected Slaver(String _name, double scale, int x, int y, boolean isActive, World parent, Image image) {
            super(_name, scale, x, y, isActive, parent, image);
        }

        public Slaver(String _name, double scale, int x, int y, boolean isActive, World parent) {
            super(_name, scale, x, y, isActive, parent, Utilities.imageFrom(textureString));
        }

        public Slaver(Slaver slaver) {
            this(slaver.name, slaver.getScaleX(), (int) slaver.getLayoutX(), (int) slaver.getLayoutY(), slaver.isActive, slaver.world);
            niggers = new ArrayList<>();
        }

        @Override
        public String getTexture() {
            return textureString;
        }

        @Override
        public void doBusiness() {
            if  (objective == null) if (niggers.isEmpty())
                 objective = (world.getShip().objects.isEmpty()) ? world.getAuctionHouse() : world.getShip();
            else objective = world.getHut();

            if (!reachedObjective)
                goToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Slaver(this);
        }

    }

    static class Merchant extends Slaver {
        static final String textureString = "купець";

        public Merchant(String _name, double scale, int x, int y, boolean isActive, World parent) {
            super(_name, scale, x, y, isActive, parent, Utilities.imageFrom(textureString));
        }

        public Merchant(Merchant merchant) {
            this(merchant.name, merchant.getScaleX(), (int) merchant.getLayoutX(), (int) merchant.getLayoutY(), merchant.isActive, merchant.world);
        }

        @Override
        public String getTexture() {
            return textureString;
        }

        @Override
        public void doBusiness() {
            if (objective == null)
                objective = world.getShip();

            if (!reachedObjective)
                goToObjective();
        }

        @Override
        public MicroObject clone() {
            return new Merchant(this);
        }

    }
}