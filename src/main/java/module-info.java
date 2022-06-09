module com.junkstudios.coursejob {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;
    requires kotlin.stdlib;
    requires java.desktop;

    opens com.sirkostya009.term_paper to javafx.fxml;
    exports com.sirkostya009.term_paper;
}