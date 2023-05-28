package ieu.edu.tr.iae.controllers;

import ieu.edu.tr.iae.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


public class AppController {
    @FXML
    private Button configButton;

    @FXML
    private Button runButton;

    @FXML
    private MenuItem menuItemHelp;

    @FXML
    private MenuItem menuItemImport;

    @FXML
    private MenuItem menuItemExport;

    @FXML
    private TreeView<Submission> treeView;

    private TreeItem<Submission> root;

    Configuration conf = Configuration.getInstance();

    ComboBox<String> config = new ComboBox<>();
    TextField compilerPath=  new TextField();
    TextField args=  new TextField();
    TextField expectedOutput=  new TextField();
    TextField assignmentPath=  new TextField();
    TextField name=  new TextField();
    File selectedDirectory = null;

    File directory = null;
    ObservableList<String> configList = FXCollections.observableArrayList();
    ArrayList<Configuration> configurations = new ArrayList<>();

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        System.out.println("-************************************");
        Database database = Database.getInstance();
        database.open();

/*
        Configuration javaConf = new Configuration("JavaConfig",null,"javac","main.java",null);
        Configuration pythonConf = new Configuration("PythonConfig",null,"python3","main.py",null);
        Configuration cConf = new Configuration("CConfig",null,"gcc","main.c",null);
        Configuration cppConf = new Configuration("CPPConfig",null,"g++","main.cpp",null);
        configurations.addAll(javaConf,pythonConf,cConf,cppConf);

 */

        configList.addAll("JavaConfig","PythonConfig","CConfig","CPPConfig","OptionalConfig");
        HashMap<String, Configuration> configsInDatabase =  database.getAllConfigs();
        configList.addAll(configsInDatabase.keySet());
        configurations.addAll(configsInDatabase.values());

        System.out.println(configsInDatabase.keySet());

        root = new TreeItem<Submission>(new Submission("Submissions","-1","-1","-1"));
        root.setExpanded(true);

        treeView.setRoot(new TreeItem<>());
        treeView.getRoot().setExpanded(true);
        treeView.getRoot().getChildren().add(root);


        treeView.setCellFactory(value -> new TreeCell<>() {
            @Override
            protected void updateItem(final Submission submission, final boolean empty) {
                super.updateItem(submission, empty);
                if (submission == null || empty) {
                    setText(null);
                } else {
                    setText(submission.getId());
                }
            }
        });




        treeView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                TreeItem<Submission> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem != treeView.getRoot()) {
                    Submission submission = selectedItem.getValue();

                    Dialog<Submission> dialog = new Dialog<>();

                    dialog.setTitle("Submission Details");
                    dialog.setHeaderText("Submission ID: " + submission.getId());

                    DialogPane dialogPane = dialog.getDialogPane();
                    dialogPane.getButtonTypes().addAll(ButtonType.OK);

                    VBox vbox = new VBox();
                    vbox.setSpacing(10);

                    Label idLabel = new Label("ID: " + submission.getId());
                    Label outputLabel = new Label("Output: " + submission.getOutput());
                    Label expectedOutputLabel = new Label("Expected Output: " + submission.getExpectedOutput());
                    Label corectnessLabel = new Label("Corectness: " + submission.getCorectness());

                    vbox.getChildren().addAll(idLabel, outputLabel, expectedOutputLabel,corectnessLabel);
                    dialogPane.setContent(vbox);

                    dialog.showAndWait();
                }
            }
           /* if (event.getButton().equals(MouseButton.PRIMARY)) {

                Dialog<Configuration> d = new Dialog<>();
                DialogPane pane = new DialogPane();
                pane.setMaxHeight(700);
                pane.setMaxWidth(400);
                //   pane.getStylesheets().add("styles.css");
                pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                d.setDialogPane(pane);
                VBox box = new VBox();
                box.setSpacing(3);
                if (treeView.getSelectionModel().getSelectedItem() == null) {
                    return;
                }
                Submission submission = treeView.getSelectionModel().getSelectedItem().getValue();
                if (root.getValue().equals(submission) || treeView.getSelectionModel().getSelectedItem().getValue() == null) {
                    return;
                }


            }

*/
        });

        treeView.setCellFactory(tree -> new TreeCell<Submission>() {
            @Override
            protected void updateItem(Submission item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getId());

                    if (item.getCorectness().equals("Correct")) {
                        setStyle("-fx-background-color: green;");
                        setTextFill(Color.WHITE);
                    }else if(item.getCorectness().equals("False")){
                        setStyle("-fx-background-color: red;");
                        setTextFill(Color.WHITE);
                    }else {
                        setStyle("");
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });


        menuItemHelp.setOnAction((value) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Help");
            alert.setHeaderText("Click "+ "'Show Details'" +" to see the help information");
            alert.setContentText("Welcome to IAE");
            TextArea area = new TextArea();
            area.setText("1) Choose or create a configuration\n2) Fill the input fields then press 'OK' and for the assignment path please choose the folder that include project zip files." +
                    "\n3) Press Run to populate treeview" +
                    "\n4)For the configuration add part, please fill all the input fields." +
                    " \n5)For the configuration edit part, the name that is entered into the input field must be the same with the one chosen in the ComboBox." +
                    "\n6)For the configuration delete part, the selected ComboBox is deleted.");
            area.setWrapText(true);
            area.setEditable(false);

            alert.getDialogPane().setExpandableContent(area);
            alert.showAndWait();
        });

        menuItemImport.setOnAction((value) -> {
            Configuration conf = null;
            try {
                conf = FileHandler.importConf();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            if (conf == null) {
                return;
            }

            try {
                database.addConfig(conf.assignmentPath,conf.compilerPath,conf.args,conf.name,conf.expectedOutput);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            configList.add(conf.name);
            configurations.add(conf);

        });

        menuItemExport.setOnAction((value) -> {
            DialogPane pane = new DialogPane();
            TextField confName = new TextField();
            Button export = new Button("Export");
            ListView <String> confList = new ListView<>();
            confList.getItems().addAll(configList);
            //pane.getStylesheets().add("styles.css");

            HBox exportBox = new HBox();
            exportBox.getChildren().addAll(confName,export);

            config.setPromptText("Export Configurations");

            export.setOnAction(actionEvent -> {
                Configuration expConf = findConfiguration(confName.getText());
                if(expConf == null) {
                    return;
                }

                try {
                    FileHandler.exportConf(expConf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Button cancel = new Button("Cancel");

            cancel.setOnAction(event -> {
                // Get the current stage and close it
                Stage currentStage = (Stage) cancel.getScene().getWindow();
                currentStage.close();
            });

            pane.setMaxHeight(600);
            pane.setMaxWidth(400);
            VBox box = new VBox();
            box.setSpacing(3);
            Dialog<Configuration> dialog = new Dialog<>();
            dialog.setTitle("Export Configurations");
            dialog.setDialogPane(pane);

            box.getChildren().addAll(exportBox,confList,cancel);

            pane.setContent(box);

            Optional<Configuration> optional = dialog.showAndWait();
            if (optional.isPresent()) {
                Configuration config = optional.get();
                // Process the configuration
            }

        });





        runButton.setOnAction(actionEvent -> {

            String directoryPath = conf.assignmentPath;
            ZipExtractor zipExtractor = new ZipExtractor();
            try {
                zipExtractor.extractAndCompileZipFilesInDirectory(directoryPath, treeView);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        });

//
        configButton.setOnAction((value) -> {
            DialogPane pane = new DialogPane();
         //   pane.getStylesheets().add("styles.css");


            config.setPromptText("Config");


            config.setItems(configList);
            config.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    String selectedComboBoxValue = config.getValue();
                    String directoryString = selectedComboBoxValue.toString();
                    if (selectedComboBoxValue == null) {
                    } else if (selectedComboBoxValue.equals("CConfig")) {
                        Configuration.getInstance().name = "C";
                        compilerPath.setText("gcc");
                        args.setText("main.c");
                        name.setText(conf.name);
                        expectedOutput.setText(conf.expectedOutput);
                        assignmentPath.setText(conf.assignmentPath);


                    } else if (selectedComboBoxValue.equals("PythonConfig")) {
                        Configuration.getInstance().name = "Python";
                        compilerPath.setText("python3");
                        args.setText("main.py");
                        name.setText(conf.name);
                        expectedOutput.setText(conf.expectedOutput);
                        assignmentPath.setText(conf.assignmentPath);


                    } else if (selectedComboBoxValue.equals("JavaConfig")) {
                        Configuration.getInstance().name = "Java";
                        compilerPath.setText("javac");
                        args.setText("main.java");
                        name.setText(conf.name);
                        expectedOutput.setText(conf.expectedOutput);
                        assignmentPath.setText(conf.assignmentPath);


                    } else if (selectedComboBoxValue.equals("CPPConfig")) {
                        Configuration.getInstance().name = "CPP";
                        compilerPath.setText("g++");
                        args.setText("main.cpp");
                        name.setText(conf.name);
                        expectedOutput.setText(conf.expectedOutput);
                        assignmentPath.setText(conf.assignmentPath);
                    }else{
                        try {
                            System.out.println("---");
                            database.open();
                            Configuration conf = database.getConfig(selectedComboBoxValue);
                            System.out.println(conf.name);
                            compilerPath.setText(conf.compilerPath);
                            args.setText(conf.args);
                            name.setText(conf.name);
                            expectedOutput.setText(conf.expectedOutput);
                            assignmentPath.setText(conf.assignmentPath);
                            System.out.println(conf.expectedOutput);

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });


            name.setPromptText("name");
            compilerPath.setPromptText("compiler path");
            args.setPromptText("args");
            expectedOutput.setPromptText("expected output");



            Button browseButton = new Button("Browse");
            browseButton.setOnAction(event -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                selectedDirectory = directoryChooser.showDialog(pane.getScene().getWindow());
                if (selectedDirectory != null) {
                    assignmentPath.setText(selectedDirectory.getPath());
                    conf.assignmentPath = assignmentPath.getText();
                    directory = new File(conf.assignmentPath);
                }
            });


            assignmentPath.setPromptText("assignment path");
            assignmentPath.setEditable(false);

            HBox assignmentPathBox = new HBox();
            assignmentPathBox.getChildren().addAll(assignmentPath, browseButton);

            pane.setMaxHeight(700);
            pane.setMaxWidth(400);
            VBox box = new VBox();
            box.setSpacing(3);
            box.getChildren().addAll(
                    config,
                    name,
                    assignmentPathBox,
                    compilerPath,
                    args,
                    expectedOutput
            );
            pane.setContent(box);
            ButtonType add = new ButtonType("Add");
            ButtonType edit = new ButtonType("Edit");
            ButtonType delete = new ButtonType("Delete");
            pane.getButtonTypes().addAll(add, edit, delete, ButtonType.CANCEL);
            Dialog<Configuration> dialog = new Dialog<>();
            dialog.setTitle("Configuration");
            dialog.setDialogPane(pane);

            dialog.setResultConverter(type -> {
                if (type == add) {
                    saveConfig(String.valueOf(directory));
                } else if (type == delete) {
                    deleteConfig();
                } else if (type == edit) {
                    updateConfig();
                }
                return null;
            });

            Optional<Configuration> optional = dialog.showAndWait();
            if (optional.isPresent()) {
                Configuration config = optional.get();
                // Process the configuration
            }

        });



    }
    public void saveConfig(String selectedDirectory){
        try {
            if (!selectedDirectory.isEmpty()) {
                Database.getInstance().open();
                Database.getInstance().addConfig(assignmentPath.getText(), compilerPath.getText(), args.getText(),name.getText(), expectedOutput.getText());

                Database.getInstance().disconnect();

                conf = Configuration.getInstance();
                conf.name = name.getText();
                conf.compilerPath = compilerPath.getText();
                conf.expectedOutput = expectedOutput.getText();
                conf.args = args.getText();
                conf.assignmentPath = assignmentPath.getText();
                configList.add(conf.name);
                configurations.add(conf);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Config save");
                alert.setHeaderText("Config save");
                alert.setContentText("Configuration saved.");
                alert.showAndWait();

                name.setText("");
                args.setText("");
                expectedOutput.setText("");
                assignmentPath.setText("");
                compilerPath.setText("");


                System.out.println("Added");
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteConfig(){
        try {
            Database.getInstance().open();
            Database.getInstance().deleteConfig(config.getValue());
            Database.getInstance().disconnect();
            configList.removeAll(config.getValue());

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Config delete");
            alert.setHeaderText("Config delete");
            alert.setContentText("Configuration deleted.");
            alert.showAndWait();
            System.out.println("Deleted");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateConfig(){
        try {
            Database.getInstance().open();
            Database.getInstance().editConfig(assignmentPath.getText() ,compilerPath.getText(), args.getText(),name.getText(), expectedOutput.getText());
            Database.getInstance().disconnect();



            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Config update");
            alert.setHeaderText("Config update");
            alert.setContentText("Configuration updated.");
            alert.showAndWait();
            System.out.println("Updated");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Configuration findConfiguration(String name){
        for(Configuration c : configurations){
            if(c.name.equals(name)){
                return c;
            }
        }
        return null;
    }

}