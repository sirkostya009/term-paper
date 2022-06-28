package com.sirkostya009.termpaper;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

import static com.sirkostya009.termpaper.MacroObject.*;
import static com.sirkostya009.termpaper.MicroObject.MicroConfig;

public final class World extends Scene { // UNIVERSAL OBJECT
    public final static World INSTANCE = new World(640, 480);

    public final ArrayList<MicroObject> microObjects = new ArrayList<>();
    public final AuctionHouse auctionHouse = new AuctionHouse(174,1130);
    public final NiggerHut hut1 = new NiggerHut(1,138,417);
    public final NiggerHut hut2 = new NiggerHut(.8,530,13);
    public final TradeShip tradeShip = new TradeShip(1351,1240);
    public final MacroObject[] macroObjects = new MacroObject[]{auctionHouse, hut1, hut2, tradeShip};

    public final ImageView  view = new ImageView(Utilities.imageFrom("універсалич"));
    public final MiniMap miniMap = new MiniMap(this);

    private World(double width, double height) {
        super(new Group(), width, height);
        addChildren(view, miniMap.view, miniMap.camera);

        view.setOnMouseClicked(mouseEvent -> {
            var x = mouseEvent.getSceneX();
            var y = mouseEvent.getSceneY();

            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                for (var macro : macroObjects)
                    if (macro.contains(x, y)) {
                        macro.getOnMouseClicked().handle(mouseEvent);
                        return;
                    }

                menu(x, y).show(getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
            else if (mouseEvent.getButton() == MouseButton.PRIMARY)
                for (var micro : microObjects)
                    if (micro.contains(x, y))
                        micro.clickAction();
        });

        setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case DELETE -> deleteAll();
                case INSERT -> callCreator(getWidth() / 2, getHeight() / 2);
                case ESCAPE -> microObjects.forEach(MicroObject::deselect);
                case W,A,S,D-> microObjects.forEach(microObject -> microObject.move(keyEvent.getCode()));
                case DOWN, LEFT, RIGHT, UP -> moveCamera(keyEvent.getCode());
                case F -> runner.toggle();
                case P -> microObjects.forEach(MicroObject::nullifyObjective);
                case B -> System.out.println("breakpoint triggered");
                case N -> automator.toggle();
                case CAPS -> callPicker();
            }
        });
    }

    private final Runner runner = new Runner();

    private class Runner extends AnimationTimer {
        private long prev = 0;

        @Override
        public void handle(long l) {
            if (l - prev < 40000000) return;

            prev = l;
            microObjects.forEach(MicroObject::doBusiness);
        }

        private boolean running = false;

        public void toggle() {
            running = !running;

            if (running) start();
            else         stop();
        }
    }

    private final Automator automator = new Automator();

    private class Automator extends AnimationTimer {
        private final int[] totals = new int[MicroObject.LEVEL.TOTAL.ordinal()];
        private final boolean[] alignments = new boolean[MicroObject.LEVEL.TOTAL.ordinal()];

        private boolean iSeeItAsAnAbsoluteWin = false;

        private void tellUser(MicroObject.LEVEL level) {
            if (iSeeItAsAnAbsoluteWin) return;

            iSeeItAsAnAbsoluteWin = true;

            var stage = new Stage();

            var root = new VBox(new Label(level + " team wins!"));
            root.setAlignment(Pos.CENTER);

            stage.setScene(new Scene(root, 200, 200));
            stage.show();
        }

        @Override
        public void handle(long l) {
            microObjects.forEach(MicroObject::moveToCaptain);

            microObjects.forEach(microObject ->  {
                if (!microObject.isCaptain) return;

                final int[] counter = {0};
                totals[microObject.level.ordinal()] = microObject.privateObjects.size();

                microObject.privateObjects.forEach(microObject1 -> {
                    if (microObject1.isLinedUp) counter[0]++;
                });

                if (counter[0] == microObject.privateObjects.size())
                    alignments[microObject.level.ordinal()] = true;
            });

            var max = -1;
            var maxI = MicroObject.LEVEL.TOTAL;

            for (var i : MicroObject.LEVEL.values())
                if (i == MicroObject.LEVEL.TOTAL) break;
                else if  (totals[i.ordinal()] > max) {
                    max = totals[i.ordinal()];
                    maxI= i;
                }

            if (max != -1 && alignments[maxI.ordinal()]) tellUser(maxI);
        }

        private boolean isRunning = false;

        public void toggle() {
            isRunning = !isRunning;

            if (isRunning) start();
            else           stop();
        }
    }

    private boolean firstHut = true;

    public MacroObject getHut() {
        firstHut = !firstHut;
        return (firstHut) ? hut1 : hut2;
    }

    private void moveCamera(KeyCode code) {
        double x = 0, y = 0;

        switch (code) {
            case DOWN -> y = -MicroObject.SPEED;
            case RIGHT-> x = -MicroObject.SPEED;
            case UP   -> y =  MicroObject.SPEED;
            case LEFT -> x =  MicroObject.SPEED;
        }

        if (view.getY() + y > 0 || view.getY() + y < -(view.getImage().getHeight() - getHeight())) return;
        if (view.getX() + x > 0 || view.getX() + x < -(view.getImage().getWidth()  - getWidth()))  return;

        actualCameraMove(x, y);
        miniMap.updateCamera(x, y);
    }

    private void actualCameraMove(double x, double y) {
        view.setX(view.getX() + x);
        view.setY(view.getY() + y);

        for (var micro : microObjects) micro.move(x, y);

        for (var macro : macroObjects) macro.move(x, y);
    }

    public void setPos(double x, double y) {
        actualCameraMove(-(x + view.getX()), -(y + view.getY()));
    }

    public void addChildren(Node... nodes) {
        ((Group) getRoot()).getChildren().addAll(nodes);
    }

    public void removeChildren(Node... nodes) {
        ((Group) getRoot()).getChildren().removeAll(nodes);
    }

    public void callCreator(double x, double y) {
        MicroObjectCreator.call(x, y, microObject -> {
            miniMap.push(microObject);
            addChildren(microObject, microObject.text, microObject.miniMapVersion);
            microObjects.add(microObject);
            for (var macro : macroObjects) macro.toFront();
            miniMap.toFront();
        });
    }

    private void callPicker() {
        var vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        microObjects.forEach(microObject -> vbox.getChildren().add(new MicroObjectOption(microObject)));

        var stage = new Stage();
        stage.setScene(new Scene(new ScrollPane(vbox)));
        stage.show();
    }

    public void deleteAll() {
        microObjects.removeIf(microObject -> {
            if (microObject.isActive)
                removeMicro(microObject);

            return microObject.isActive;
        });
    }

    public void removeMicro(MicroObject object) {
        removeChildren(object, object.text, object.miniMapVersion);
    }

    private ContextMenu menu(double x, double y) {
        return new ContextMenu(
                newMicroObject(x, y),
                interactObjects(),
                saveState(),
                loadState(),
                deleteSelected()
        );
    }

    private MenuItem newMicroObject(double x, double y) {
        var res = new MenuItem("New MicroObject here...");

        res.setOnAction(actionEvent -> callCreator(x, y));

        return res;
    }

    private MenuItem interactObjects() {
        var item = new MenuItem("Interact selected");

        item.setOnAction(actionEvent -> {
            var list = new ArrayList<MicroObject>();

            microObjects.forEach(microObject -> {
                if (microObject.isActive)
                    list.add(microObject);
            });

            list.forEach(microObject -> microObject.objective.interactWith(microObject));
        });

        return item;
    }

    private MenuItem saveState() {
        var result = new MenuItem("Save state");

        result.setOnAction(event -> {
            var chooser = Utilities.genericChooser("Save state...");

            var configs = new ArrayList<MicroObject.MicroConfig>(microObjects.size());

            microObjects.forEach(microObject -> configs.add(microObject.convertToConfig()));

            try {
                Utilities.saveState(chooser.showSaveDialog(getWindow()), configs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return result;
    }

    private MenuItem loadState() {
        var result = new MenuItem("Load state");

        result.setOnAction(event -> {
            var chooser = Utilities.genericChooser("Load state...");

            try {
                var read = Utilities.<ArrayList<MicroConfig>>loadState(chooser.showOpenDialog(getWindow()));

                microObjects.forEach(this::removeMicro);
                microObjects.clear();

                read.forEach(microConfig -> {
                    var object = microConfig.convert();
                    if (object == null) return;
                    miniMap.push(object);
                    microObjects.add(object);
                });

                microObjects.forEach(microObject -> addChildren(microObject, microObject.text, microObject.miniMapVersion));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return result;
    }

    private MenuItem deleteSelected() {
        var res = new MenuItem("Delete selected");

        res.setOnAction(actionEvent -> deleteAll());

        return res;
    }
}
