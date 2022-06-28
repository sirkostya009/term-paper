package com.sirkostya009.termpaper;

public class CaptainButton extends javafx.scene.control.Button {
    private final static boolean[] captainsPicked = new boolean[MicroObject.LEVEL.TOTAL.ordinal()];
    private int captainCounter = 0;
    private boolean isClicked = false;

    public CaptainButton(MicroObject reference) {
        decorate(reference.isCaptain);

        setOnAction(actionEvent -> {
            if (captainCounter == 3 && captainsPicked[reference.level.ordinal()] && !isClicked) return;

            toggle();

            reference.isCaptain = isClicked;

            captainsPicked[reference.level.ordinal()] = isClicked;
        });
    }

    private void toggle() {
        isClicked = !isClicked;
        if (isClicked) captainCounter++;
        else           captainCounter--;
        decorate(isClicked);
    }

    private void decorate(boolean status) {
        isClicked = status;
        setText(status ? "selected" : "select");
        setStyle("-fx-background-color: " + (status ? "beige" : "white"));
    }
}
