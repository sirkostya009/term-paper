package com.sirkostya009.termpaper;

import javafx.scene.control.Button;

import java.util.Arrays;

public class CaptainButton extends Button {
    private final static boolean[] captainsPicked = new boolean[MicroObject.LEVEL.TOTAL.ordinal()];
    private int captainCounter = 0;
    private boolean isClicked = false;

    public CaptainButton(MicroObject reference) {
        super(reference.isCaptain() ? "selected" : "select");
        setStyle("-fx-background-color: " + (reference.isCaptain() ? "beige" : "white"));

        setOnAction(actionEvent -> {
            if (captainCounter == 3 && captainsPicked[reference.level.ordinal()] && !isClicked) return;

            isClicked = !isClicked;
            if (isClicked) captainCounter++;
            else           captainCounter--;

            setText(isClicked ? "selected" : "select");
            setStyle("-fx-background-color: " + (isClicked ? "beige" : "white"));

            reference.setCaptain(isClicked);

            captainsPicked[reference.level.ordinal()] = isClicked;
        });
    }
}
