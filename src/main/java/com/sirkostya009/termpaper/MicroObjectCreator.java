package com.sirkostya009.termpaper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MicroObjectCreator implements Initializable {
    private static final String nigga = "Негр", slaver = "Рабовласник", merchant = "Работоргівець";

    @FXML
    private TextField nameField, scaleField;

    @FXML
    private CheckBox isActive;

    @FXML
    private ChoiceBox<String> microObjectLevel;

    @FXML
    private void create() {
        var name = nameField.getText();
        var scale = Double.parseDouble(scaleField.getText());
        var active = isActive.isSelected();

        handler.handle(switch (microObjectLevel.getValue()) {
            case nigga -> new MicroObject.Nigger(name, scale, x, y, active);
            case slaver -> new MicroObject.Slaver(name, scale, x, y, active);
            case merchant -> new MicroObject.Merchant(name, scale, x, y, active);
            default -> null;
        });

        close();
    }

    @FXML
    private void close() {
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        microObjectLevel.getItems().addAll(nigga, slaver, merchant);
        microObjectLevel.setValue(nigga);
    }

    private double x, y;
    private Handler handler;
    private Stage stage;

    public static void call(double x, double y, Handler handler) {
        var loader = new FXMLLoader(MicroObjectCreator.class.getResource("object-creator.fxml"));
        VBox root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (root == null) return;

        var controller = (MicroObjectCreator) loader.getController();
        controller.x = x;
        controller.y = y;
        controller.handler = handler;

        controller.stage = new Stage();
        controller.stage.setScene(new Scene(root));
        controller.stage.setTitle("New MicroObject...");
        controller.stage.setResizable(false);
        controller.stage.show();
    }

    public interface Handler {
        void handle(MicroObject microObject);
    }
}
