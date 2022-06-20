package com.sirkostya009.termpaper;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MicroObjectCreator extends Stage {
    private static final String nigga = "Негр", slaver = "Рабовласник", merchant = "Работоргівець";

    private final TextField nameField = new TextField("Name"), scaleField = new TextField("Scale");

    private final CheckBox isActive = new CheckBox("Is active");

    private final Button create = Utilities.genericCreateButton();

    private final ChoiceBox<String> microObjectLevel = new ChoiceBox<>();

    final double x, y;

    public MicroObjectCreator(double x, double y) {
        this.x = x;
        this.y = y;

        microObjectLevel.getItems().addAll(nigga, slaver, merchant);
        microObjectLevel.setValue(nigga);

        var vBox = Utilities.genericVBox(
                nameField,
                scaleField,
                isActive,
                microObjectLevel,
                Utilities.genericHBox(
                        create,
                        Utilities.genericCancelButton(this)
                )
        );

        setScene(new Scene(vBox, 200, 300));
        setTitle("Create new MicroObject");
        setResizable(false);
        show();
    }

    public void setCreateAction(EventHandler<ActionEvent> handler) {
        create.setOnAction(handler);
    }

    public MicroObject makeMicroObject(World parent) {
        var name = nameField.getText();
        var scale = Double.parseDouble(scaleField.getText());
        var active = isActive.isSelected();

        return switch (microObjectLevel.getValue()) {
            case slaver -> new MicroObject.Slaver(name, scale, x, y, active, parent);
            case merchant -> new MicroObject.Merchant(name, scale, x, y, active, parent);
            case nigga -> new MicroObject.Nigger(name, scale, x, y, active, parent);
            default -> null;
        };
    }
}
