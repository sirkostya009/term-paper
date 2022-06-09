module com.junkstudios.coursejob {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;
    requires kotlin.stdlib;
    requires java.desktop;

    opens com.junkstudios.coursejob to javafx.fxml;
    exports com.junkstudios.coursejob;
}