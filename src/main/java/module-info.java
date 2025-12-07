module lt.ignassenkus.metmap {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires univocity.parsers;
    //requires lt.ignassenkus.metmap;

    opens lt.ignassenkus.metmap to javafx.fxml;
    exports lt.ignassenkus.metmap;
    exports lt.ignassenkus.metmap.controller;
    opens lt.ignassenkus.metmap.controller to javafx.fxml;
}