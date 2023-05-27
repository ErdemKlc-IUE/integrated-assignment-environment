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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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

        root = new TreeItem<Submission>(new Submission("Submissions","-1","-1"));
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
            if (event.getButton().equals(MouseButton.PRIMARY)) {

                Dialog<Configuration> d = new Dialog<>();
                DialogPane pane = new DialogPane();
                pane.setMaxHeight(700);
                pane.setMaxWidth(400);
                //   pane.getStylesheets().add("style.css");
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
            //pane.getStylesheets().add("style.css");

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
            try {

                // Extract zip files in the selected directory
                ZipExtractor zipExtractor = new ZipExtractor();
                zipExtractor.extractZipFilesInDirectory(selectedDirectory.toString());

                // Compile and get results for each extracted file
                File[] files = selectedDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {

//********************JAVA*********************
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
                            if (conf.compilerPath.equals("javac")) {
                                // Compile the Java file
                                System.out.println("selected directory"+selectedDirectory);


                                JavaCompiler javaCompiler = new JavaCompiler(selectedDirectory);
                                Output output = javaCompiler.compile(file.getPath(), conf.args);

                                System.out.println(file.getPath());
                                System.out.println(output.getOutput());
                                System.out.println(output.getError());
                                System.out.println(output.getExitCode());

                                Submission sub = null;
                                // Process the compilation result
                                if (output.getExitCode() == 0) {
                                    // Compilation successful
                                    System.out.println("File: " + file.getName() + " compiled successfully");

                                    // Add a TreeItem to the TreeView
                                    File[] filess = directory.listFiles();
                                    if (filess == null) {
                                        return;
                                    }

                                    for (File c1 : files) {
                                        if (c1.isFile() && c1.getName().toLowerCase().endsWith(".zip")) {

                                            String name1 = c1.getName().substring(0,c1.getName().length()-4);
                                            sub = new Submission(name1, output.getOutput(), conf.expectedOutput);
                                            System.out.println(sub.getId());
                                            System.out.println(sub.getExpectedOutput());
                                            System.out.println(sub.getOutput());


                                            if (sub.getOutput().equals(sub.getExpectedOutput())) {
                                                System.out.println("Output matches expected output for submission: " + sub.getId());
                                            } else {
                                                System.out.println("Output does not match expected output for submission: " + sub.getId());
                                            }


                                            TreeItem<Submission> newItem = new TreeItem<>(sub);
                                            treeView.getRoot().getChildren().add(newItem);

                                        }
                                    }

                                } else {
                                    Submission submission= null;

                                    File[] files3 = directory.listFiles();
                                    if (files3 == null) {
                                        return;
                                    }

                                    for (File c3 : files3) {
                                        if (c3.isFile() && c3.getName().toLowerCase().endsWith(".zip")) {

                                            String name3 = c3.getName().substring(0,c3.getName().length()-4);
                                            submission = new Submission(name3, "Incorrect", conf.expectedOutput);
                                        }
                                    }

                                    TreeItem<Submission> newSubmission = new TreeItem<>(submission);
                                    treeView.getRoot().getChildren().add(newSubmission);
                                    // Compilation failed
                                    System.out.println("File: " + file.getName() + " compilation failed");
                                    System.out.println("Output: " + output.getOutput());
                                    System.out.println("Error: " + output.getError());
                                }
                            }
                        }
                        //********************Python*********************
                        else if (file.isFile() && file.getName().toLowerCase().endsWith(".py")) {
                            if (conf.compilerPath.equals("python")) {
                                // Compile the Python file
                                PythonInterpreter pythonCompiler = new PythonInterpreter(selectedDirectory);
                                Output output = pythonCompiler.compile(file.getAbsolutePath(),  conf.args);

                                // Process the compilation result
                                if (output.getExitCode() == 0) {
                                    // Compilation successful
                                    System.out.println("File: " + file.getName() + " compiled successfully");

                                    // Add a TreeItem to the TreeView
                                    File[] filess = directory.listFiles();
                                    if (filess == null) {
                                        return;
                                    }
                                    Submission sub2 = null;
                                    for (File c1 : files) {
                                        if (c1.isFile() && c1.getName().toLowerCase().endsWith(".zip")) {

                                            String name1 = c1.getName().substring(0,c1.getName().length()-4);
                                            sub2 = new Submission(name1, output.getOutput(), conf.expectedOutput);
                                            System.out.println(sub2.getId());
                                            System.out.println(sub2.getExpectedOutput());
                                            System.out.println(sub2.getOutput());



                                            if (sub2.getOutput().equals(sub2.getExpectedOutput())) {
                                                System.out.println("Output matches expected output for submission: " + sub2.getId());
                                            } else {
                                                System.out.println("Output does not match expected output for submission: " + sub2.getId());
                                            }

                                            TreeItem<Submission> newItem = new TreeItem<>(sub2);
                                            treeView.getRoot().getChildren().add(newItem);


                                        }
                                    }



                                } else {
                                    // Compilation failed
                                    Submission submission = new Submission(file.getName(), "Incorrect", conf.expectedOutput);
                                    TreeItem<Submission> newSubmission = new TreeItem<>(submission);
                                    treeView.getRoot().getChildren().add(newSubmission);
                                    System.out.println("File: " + file.getName() + " compilation failed");
                                    System.out.println("Output: " + output.getOutput());
                                    System.out.println("Error: " + output.getError());
                                }
                            }
                        }
                        //********************CLanguage*********************
                        else if (file.isFile() && file.getName().toLowerCase().endsWith(".c")) {
                            if (conf.compilerPath.equals("gcc")) {
                                // Compile the C file
                                CCompiler cCompiler = new CCompiler(selectedDirectory);
                                Output output = cCompiler.compile(file.getAbsolutePath(),  conf.args);



                                // Process the compilation result
                                if (output.getExitCode() == 0) {
                                    // Compilation successful
                                    System.out.println("File: " + file.getName() + " compiled successfully");

                                    // Add a TreeItem to the TreeView
                                    File[] filess = directory.listFiles();
                                    if (filess == null) {
                                        return;
                                    }
                                    Submission sub3 = null;
                                    for (File c1 : files) {
                                        if (c1.isFile() && c1.getName().toLowerCase().endsWith(".zip")) {

                                            String name1 = c1.getName().substring(0,c1.getName().length()-4);
                                            sub3 = new Submission(name1, output.getOutput(), conf.expectedOutput);
                                            System.out.println(sub3.getId());
                                            System.out.println(sub3.getExpectedOutput());
                                            System.out.println(sub3.getOutput());


                                            if (sub3.getOutput().equals(sub3.getExpectedOutput())) {
                                                System.out.println("Output matches expected output for submission: " + sub3.getId());
                                            } else {
                                                System.out.println("Output does not match expected output for submission: " + sub3.getId());
                                            }

                                            TreeItem<Submission> newItem = new TreeItem<>(sub3);
                                            treeView.getRoot().getChildren().add(newItem);


                                        }
                                    }



                                } else {
                                    // Compilation failed
                                    System.out.println("File: " + file.getName() + " compilation failed");
                                    System.out.println("Output: " + output.getOutput());
                                    System.out.println("Error: " + output.getError());
                                }
                            }
                        }
                        //********************CPP*********************
                        else if(file.isFile() && file.getName().toLowerCase().endsWith(".cpp")) {
                            if (conf.name.equals("CPP")) {
                                // Compile the C file
                                CPPCompiler cppCompiler = new CPPCompiler(selectedDirectory);
                                Output output = cppCompiler.compile(file.getAbsolutePath(),  conf.args);

                                // Process the compilation result
                                if (output.getExitCode() == 0) {
                                    // Compilation successful
                                    System.out.println("File: " + file.getName() + " compiled successfully");

                                    Submission sub = new Submission(file.getName(), output.getOutput(), conf.expectedOutput);


                                    if (sub.getOutput().equals(sub.getExpectedOutput())) {
                                        System.out.println("Output matches expected output for submission: " + sub.getId());
                                    } else {
                                        System.out.println("Output does not match expected output for submission: " + sub.getId());
                                    }

                                    TreeItem<Submission> newItem = new TreeItem<>(sub);
                                    treeView.getRoot().getChildren().add(newItem);


                                } else {
                                    Submission submission = new Submission(file.getName(), "Incorrect", conf.expectedOutput);
                                    TreeItem<Submission> newSubmission = new TreeItem<>(submission);
                                    treeView.getRoot().getChildren().add(newSubmission);
                                    // Compilation failed
                                    System.out.println("File: " + file.getName() + " compilation failed");
                                    System.out.println("Output: " + output.getOutput());
                                    System.out.println("Error: " + output.getError());
                                }

                            }
                        }
                    }
                }

                // Show a success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Extraction and Compilation");
                alert.setHeaderText("Extraction and Compilation Complete");
                alert.setContentText("Extraction and compilation of zip files completed successfully.");
                alert.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                // Show an error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error occurred during extraction and compilation");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                // Show an error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error occurred during compilation");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });

//
        configButton.setOnAction((value) -> {
            DialogPane pane = new DialogPane();
            pane.getStylesheets().add("style.css");


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