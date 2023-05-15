package ieu.edu.tr.iae.controllers;

import ieu.edu.tr.iae.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import org.sqlite.SQLiteException;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;


public class AppController {
    @FXML
    private Button configButton;

    @FXML
    private Button runButton;

    @FXML
    private MenuItem menuItemHelp;

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

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        System.out.println("-************************************");
        Database database = Database.getInstance();
        database.open();

        ObservableList<String> configList = FXCollections.observableArrayList();
        configList.addAll("JavaConfig","PythonConfig","CConfig","CPPConfig","OptionalConfig");

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
            alert.setHeaderText("Help");
            alert.setContentText("Welcome to IAE");
            TextArea area = new TextArea(menuItemHelp.getText());
            area.setText("Welcome to IAE");
            area.setWrapText(true);
            area.setEditable(false);

            alert.getDialogPane().setExpandableContent(area);
            alert.showAndWait();
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
                                System.out.println("selected driectory"+selectedDirectory);


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

                                                output.setOutput("Correct");
                                                System.out.println("---");

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



                                            output.setOutput("Correct");
                                            System.out.println("---");

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

                                            output.setOutput("Correct");
                                            System.out.println("---");

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

                                        output.setOutput("Correct");
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


                    } else if (selectedComboBoxValue.equals("PythonConfig")) {
                        Configuration.getInstance().name = "Python";
                        compilerPath.setText("python3");
                        args.setText("main.py");


                    } else if (selectedComboBoxValue.equals("JavaConfig")) {
                        Configuration.getInstance().name = "Java";
                        compilerPath.setText("javac");
                        args.setText("main.java");


                    } else if (selectedComboBoxValue.equals("CPPConfig")) {
                        Configuration.getInstance().name = "CPP";
                        compilerPath.setText("g++");
                        args.setText("main.cpp");


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
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
            Dialog<Configuration> dialog = new Dialog<>();
            dialog.setTitle("add");
            dialog.setDialogPane(pane);

            dialog.setResultConverter(type -> {
                if (type == ButtonType.OK) {
                    saveConfig(String.valueOf(directory));
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
                Database.getInstance().addConfig(compilerPath.getText(), args.getText(),name.getText(), expectedOutput.getText());

                Database.getInstance().disconnect();

                conf = Configuration.getInstance();
                conf.name = name.getText();
                conf.compilerPath = compilerPath.getText();
                conf.expectedOutput = expectedOutput.getText();
                conf.args = args.getText();
                conf.assignmentPath = assignmentPath.getText();


                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Condig save");
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
}