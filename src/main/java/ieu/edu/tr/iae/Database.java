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
                "compilerPath varchar(255)," +
                " args varchar(255)," +
                " name varchar(255)," +
                "expectedOutput varchar(255));";
        statement.executeUpdate(query);
        statement.close();
    }
    public void addConfig(String compilerPath, String args,String name, String expectedOutput) throws SQLException {
        String query = "INSERT INTO config_table (compilerPath, args,name, expectedOutput) VALUES (?, ?, ?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, compilerPath);
        preparedStatement.setString(2, args);
        preparedStatement.setString(3, name);
        preparedStatement.setString(4, expectedOutput);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public Configuration getConfig() throws SQLException {

        Configuration configuration = Configuration.getInstance();

        String query = "SELECT compilerPath, args,name, expectedOutput FROM config_table WHERE ID = 1;";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        if (resultSet.next()) {
            configuration.compilerPath = resultSet.getString("compilerPath");
            configuration.args = resultSet.getString("args");
            configuration.name = resultSet.getString("name");
            configuration.expectedOutput = resultSet.getString("expectedOutput");
        }

        resultSet.close();
        statement.close();

        return configuration;
    }
        public HashMap<String, Configuration> getAllConfigs() throws SQLException {
        HashMap<String,Configuration> allConfigs = new HashMap<String,Configuration>();

        Configuration configuration = Configuration.getInstance();

        String query = "SELECT compilerPath, args,name, expectedOutput FROM config_table;";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        if (resultSet.next()) {
            configuration.compilerPath = resultSet.getString("compilerPath");
            configuration.args = resultSet.getString("args");
            configuration.name = resultSet.getString("name");
            configuration.expectedOutput = resultSet.getString("expectedOutput");
            allConfigs.put(configuration.name,new Configuration(configuration.name,"",configuration.compilerPath,configuration.args,configuration.expectedOutput));
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
