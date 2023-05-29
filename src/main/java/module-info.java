module com.mini.orkut {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jfx.asynctask;
    requires java.mail;
    requires org.postgresql.jdbc;
    requires javafx.web;
    requires javafx.swing;
   // requires javafx.swingEmpty;

    opens com.mini.orkut to javafx.fxml;
    exports com.mini.orkut;

    opens com.mini.orkut.model to javafx.fxml;
    exports com.mini.orkut.model;


    opens com.mini.orkut.controller.auth to javafx.fxml;
    exports com.mini.orkut.controller.auth;

    opens com.mini.orkut.controller.feed to javafx.fxml;
    exports com.mini.orkut.controller.feed;

    opens com.mini.orkut.controller.friend to javafx.fxml;
    exports com.mini.orkut.controller.friend;

    opens com.mini.orkut.controller.user to javafx.fxml;
    exports com.mini.orkut.controller.user;
    exports com.mini.orkut.controller.post;
    opens com.mini.orkut.controller.post to javafx.fxml;


}