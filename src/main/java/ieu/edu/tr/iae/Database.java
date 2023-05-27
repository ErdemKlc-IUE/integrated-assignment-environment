package ieu.edu.tr.iae;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    private static Database instance;
    Connection connection;

    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public void open() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:iae.db");
        final Statement statement = connection.createStatement();
        final String query = "create table if not exists config_table " +
                "(ID INTEGER primary key AUTOINCREMENT," +
                " assignmentPath varchar(255),"+
                " compilerPath varchar(255)," +
                " args varchar(255)," +
                " name varchar(255)," +
                " expectedOutput varchar(255)); ";
        statement.executeUpdate(query);
        statement.close();
    }
    public void addConfig(String assignmentPath, String compilerPath, String args,String name, String expectedOutput) throws SQLException {
        String query = "INSERT INTO config_table (assignmentPath, compilerPath, args, name, expectedOutput) VALUES (?, ?, ?, ?, ?); ";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, assignmentPath);
        preparedStatement.setString(2, compilerPath);
        preparedStatement.setString(3, args);
        preparedStatement.setString(4, name);
        preparedStatement.setString(5, expectedOutput);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    public void deleteConfig(String name) throws SQLException {
        String query = "DELETE FROM config_table WHERE name = ?; ";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, name);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void editConfig(String assignmentPath,String compilerPath, String args,String name, String expectedOutput) throws SQLException {
        String query = "UPDATE config_table SET assignmentPath = ?, compilerPath = ?, args = ?, expectedOutput = ? WHERE name = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, assignmentPath);
        preparedStatement.setString(2, compilerPath);
        preparedStatement.setString(3, args);
        preparedStatement.setString(4, expectedOutput);
        preparedStatement.setString(5, name);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    public Configuration getConfig(String name) throws SQLException {
        Configuration configuration = Configuration.getInstance();

        String query = "SELECT assignmentPath, compilerPath, args, name, expectedOutput FROM config_table WHERE name = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, name);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            configuration.assignmentPath = resultSet.getString("assignmentPath");
            configuration.compilerPath = resultSet.getString("compilerPath");
            configuration.args = resultSet.getString("args");
            configuration.name = resultSet.getString("name");
            configuration.expectedOutput = resultSet.getString("expectedOutput");
        }

        resultSet.close();
        preparedStatement.close();

        return configuration;
    }

    public HashMap<String, Configuration> getAllConfigs() throws SQLException {
        HashMap<String, Configuration> allConfigs = new HashMap<>();

        String query = "SELECT compilerPath, args, name, expectedOutput FROM config_table;";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            String compilerPath = resultSet.getString("compilerPath");
            String args = resultSet.getString("args");
            String name = resultSet.getString("name");
            String expectedOutput = resultSet.getString("expectedOutput");

            Configuration configuration = new Configuration(name, "", compilerPath, args, expectedOutput);
            allConfigs.put(name, configuration);
        }

        resultSet.close();
        statement.close();

        return allConfigs;
    }

    public void editConfiguration(Configuration configuration) throws SQLException{


    }

    public void disconnect() throws SQLException {
        connection.close();
    }

}
