package ieu.edu.tr.iae;

import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;

public class FileHandler {

    public static void exportConf(Configuration configuration) throws IOException {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Directory");

        // Set initial directory (optional)
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Show the directory selection dialog
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            // Perform the file export
            exportTextFile(selectedDirectory,configuration);
        }


        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Configuration");
        alert.setHeaderText("Export Configuration Complete");
        alert.setContentText("Configuration is exported successfully.");
        alert.showAndWait();
    }

    public static void exportTextFile(File directory,Configuration conf) {
        try {
            File file = new File(directory, conf.name +".txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            String text = conf.name + "\n" + conf.compilerPath + "\n" + conf.args;
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration importConf() throws IOException, SQLException {

        // Create a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Configuration Txt File");

        // Set extension filter if needed (optional)
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().add(txtFilter);

        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Read the JSON file and map it to the Configuration object
            Configuration newConf = new Configuration();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                String line;
                ArrayList<String> features = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    features.add(line);
                }
                reader.close();
                newConf.name = features.get(0);
                newConf.compilerPath = features.get(1);
                newConf.args = features.get(2);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Configuration imported from txt file successfully!");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Configuration");
            alert.setHeaderText("Import Configuration Complete");
            alert.setContentText("Configuration is imported successfully.");
            alert.showAndWait();

            return newConf;
        } else {
            System.out.println("Import canceled by the user.");
        }

        return null;
    }
}

