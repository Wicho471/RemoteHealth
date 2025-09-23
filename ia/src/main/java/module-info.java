module org.axolotlj.ia {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.axolotlj.ia to javafx.fxml;
    exports org.axolotlj.ia;
}
