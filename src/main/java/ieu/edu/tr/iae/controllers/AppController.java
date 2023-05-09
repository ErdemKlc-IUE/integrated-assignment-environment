package ieu.edu.tr.iae.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class AppController {
    @FXML
    private Button configButton;

    @FXML
    private Button runButton;

    @FXML
    private MenuItem menuItemHelp;

    @FXML
    private TreeView<?> treeView;
    @FXML
    private void initialize(){
        System.out.println("-************************************");



        menuItemHelp.setOnAction((value) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Help");
            alert.setHeaderText("Help");
            alert.setContentText("Welcome to IAE");
            TextArea area = new TextArea(menuItemHelp.getText());
            area.setText("Welcome to IAE");
            area.setWrapText(true);
            area.setEditable(false);

            alert.getDialogPane().setExpandableContent(area);
            alert.showAndWait();
        });
    }

}