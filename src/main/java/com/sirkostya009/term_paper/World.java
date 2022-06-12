package com.sirkostya009.term_paper;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class World extends Stage { // UNIVERSAL OBJECT
    private final Group allObjects = new Group();
    public ArrayList<MicroObject> microObjects = new ArrayList<>();
    public final ArrayList<MacroObject> macroObjects = makeMacroObjects();
    public final double width, height;

    public final ImageView view;
    public final MiniMap miniMap;

    private boolean firstHut = true;
    private boolean isRunning = true;

    private record Runner(World world) implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                world.microObjects.forEach(MicroObject::doBusiness);
            }
        }
    }

    private Thread runner = new Thread(new Runner(this));

    World(String title, double width, double height) {
        view = new ImageView(Utilities.imageFrom("універсалич"));
        addChildren(view);
        addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            var x = mouseEvent.getSceneX();
            var y = mouseEvent.getSceneY();

            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                for (var macro : macroObjects)
                    if (macro.contains(x, y)) {
                        macro.menu((int)x, (int)y).show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                        return;
                    }

                menu((int)x, (int)y).show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
            else if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                microObjects.forEach(microObject -> {
                    if (microObject.contains(x, y))
                        microObject.clickAction();
                });
            }
        });

        addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case DELETE -> delete();
                case INSERT -> makeNewMicroObject(width / 2,height / 2);
                case ESCAPE -> microObjects.forEach(MicroObject::deselect);
                case W,A,S,D-> microObjects.forEach(microObject -> microObject.move(keyEvent.getCode()));
                case DOWN, LEFT, RIGHT, UP -> moveCamera(keyEvent.getCode());
                case F -> {
                    if (isRunning) {
                        System.out.println("Running loop...");
                        runner.setDaemon(true);
                        runner.start();
                    } else {
                        System.out.println("Stopping loop...");
                        runner.stop();
                        runner = new Thread(new Runner(this));
                    }
                    isRunning = !isRunning;
                }
                case P -> System.out.println(microObjects);
            }
        });

        this.width = width;
        this.height = height;
        setScene(new Scene(allObjects, width, height));
        setResizable(false);
        setTitle(title);
        miniMap = new MiniMap(this);
        show();
    }

    private ArrayList<MacroObject> makeMacroObjects() {
        ArrayList<MacroObject> macroObjects = new ArrayList<>(4);

        var auctionHouse = new MacroObject.AuctionHouse(1,174,1130,this);
        var hut1 = new MacroObject.NiggerHut(1,138,417,this);
        var hut2 = new MacroObject.NiggerHut(.8,530,13,this);
        var ship = new MacroObject.TradeShip(1,1351,1240,this);

        macroObjects.add(auctionHouse);
        macroObjects.add(hut1);
        macroObjects.add(hut2);
        macroObjects.add(ship);
        addChildren(auctionHouse, hut1, hut2, ship);

        return macroObjects;
    }

    public MacroObject getAuctionHouse() {
        return macroObjects.get(0);
    }

    public MacroObject getHut() {
        firstHut = !firstHut;
        return (firstHut) ? macroObjects.get(1) : macroObjects.get(2);
    }

    public MacroObject getShip() {
        return macroObjects.get(3);
    }

    public void moveCamera(KeyCode code) {
        int x = 0, y = 0;

        switch (code) {
            case DOWN -> y = -5;
            case RIGHT-> x = -5;
            case UP   -> y =  5;
            case LEFT -> x =  5;
        }

        if (view.getY() + y > 0 || view.getY() + y < -(view.getImage().getHeight() - height)) return;
        if (view.getX() + x > 0 || view.getX() + x < -(view.getImage().getWidth()  - width))  return;

        actualCameraMove(x, y);
        miniMap.updateCamera(x, y);
    }

    public void actualCameraMove(double x, double y) {
        view.setX(view.getX() + x);
        view.setY(view.getY() + y);

        for (var micro : microObjects)
            micro.move(x, y);

        for (var macro : macroObjects)
            macro.move(x, y);
    }

    public void setPos(double x, double y) {
        actualCameraMove(-(x + view.getX()), -(y + view.getY()));
    }

    public void addChildren(Node... nodes) {
        allObjects.getChildren().addAll(nodes);
    }

    public void removeChildren(Node... nodes) {
        allObjects.getChildren().removeAll(nodes);
    }

    ContextMenu menu(double x, double y) {
        return new ContextMenu(
                newMicroObject(x, y),
                interactObjects(),
                saveState(),
                loadState(),
                deleteSelected()
        );
    }

    private MenuItem loadState() {
        var result = new MenuItem("Load state");

        result.setOnAction(event -> {
            var chooser = Utilities.genericChooser();
            chooser.setTitle("Load state...");

            try {
                loadFromFile(chooser.showOpenDialog(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return result;
    }

    private void loadFromFile(File file) throws Exception {
        if (file == null) return;

        var input = new ObjectInputStream(new FileInputStream(file));

        microObjects.forEach(this::removeMicro);
        microObjects.clear();

        var configs = (ArrayList<MicroObject.MicroConfig>) input.readObject();

        configs.forEach(microConfig -> {
            var micro = microConfig.convert(this);
            miniMap.push(micro);
            microObjects.add(micro);
        });
        microObjects.forEach(microObject -> addChildren(microObject, microObject.text, microObject.miniMapVersion));
    }

    private MenuItem saveState() {
        var result = new MenuItem("Save state");

        result.setOnAction(event -> {
            var chooser = Utilities.genericChooser();
            chooser.setTitle("Save state...");

            try {
                saveToFile(chooser.showSaveDialog(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return result;
    }

    private void saveToFile(File file) throws Exception {
        if (file == null) return;

        var output = new ObjectOutputStream(new FileOutputStream(file));

        var configs = new ArrayList<MicroObject.MicroConfig>(microObjects.size());

        microObjects.forEach(microObject -> configs.add(microObject.convertToConfig()));

        output.writeObject(configs);
    }

    public void makeNewMicroObject(double x, double y) {
        var creator = new MicroObjectCreator(x, y);
        creator.setCreateAction(actionEvent -> {
            var object = creator.makeMicroObject(this);
            miniMap.push(object);
            addChildren(object, object.text, object.miniMapVersion);
            microObjects.add(object);
            macroObjects.forEach(Node::toFront);
            miniMap.toFront();
            creator.close();
        });
    }

    private void delete() {
        var toDelete = new ArrayList<MicroObject>();

        for (var micro : microObjects)
            if (micro.isActive)
                toDelete.add(micro);

        toDelete.forEach(this::removeMicro);
    }

    public void removeMicro(MicroObject object) {
        removeChildren(object, object.text, object.miniMapVersion);
        microObjects.remove(object);
    }

    MenuItem deleteSelected() {
        var res = new MenuItem("Delete selected");

        res.setOnAction(actionEvent -> delete());

        return res;
    }

    private MenuItem interactObjects() {
        var item = new MenuItem("Interact selected");

        item.setOnAction(actionEvent -> microObjects.forEach(microObject -> {
            if (microObject.isWithinRange(microObject.objective))
                microObject.objective.interactWith(microObject);
        }));

        return item;
    }

    MenuItem newMicroObject(double x, double y) {
        var res = new MenuItem("New MicroObject here...");

        res.setOnAction(actionEvent -> makeNewMicroObject(x, y));

        return res;
    }

}