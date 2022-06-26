package com.sirkostya009.termpaper;

public class CaptainButton extends javafx.scene.control.Button {
    private final static boolean[] captainsPicked = new boolean[MicroObject.LEVEL.TOTAL.ordinal()];
    private int captainCounter = 0;
    private boolean isClicked = false;

    public CaptainButton(MicroObject reference) {
        decorate();

        setOnAction(actionEvent -> {
            if (captainCounter == 3 && captainsPicked[reference.level.ordinal()] && !isClicked) return;

            toggle();

            reference.setCaptain(isClicked);

            captainsPicked[reference.level.ordinal()] = isClicked;
        });
    }

    private void toggle() {
        isClicked = !isClicked;
        if (isClicked) captainCounter++;
        else           captainCounter--;
        decorate();
    }

    private void decorate() {
        setText(isClicked ? "selected" : "select");
        setStyle("-fx-background-color: " + (isClicked ? "beige" : "white"));
    }
}
