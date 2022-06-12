package com.sirkostya009.term_paper;

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

    double x, y;

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
        return switch (microObjectLevel.getValue()) {
            case slaver -> new MicroObject.Slaver(nameField.getText(), Double.parseDouble(scaleField.getText()), x, y, isActive.isSelected(), parent);
            case merchant -> new MicroObject.Merchant(nameField.getText(), Double.parseDouble(scaleField.getText()), x, y, isActive.isSelected(), parent);
            case nigga -> new MicroObject.Nigger(nameField.getText(), Double.parseDouble(scaleField.getText()), x, y, isActive.isSelected(), parent);
            default -> null;
        };
    }
}
