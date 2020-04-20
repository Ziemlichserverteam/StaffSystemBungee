package de.staticfx.staffsystem.db;


import de.staticfx.staffsystem.filemanagment.ConfigManagment;

import java.sql.*;

public class DataBaseConnection {



    private Connection connection;
    String user, password,url;

    public static DataBaseConnection INSTANCE = new DataBaseConnection();

    public DataBaseConnection() {
        user = ConfigManagment.INSTANCE.getUser();
        password = ConfigManagment.INSTANCE.getPassword();
        url = "jdbc:mysql://localhost/" + ConfigManagment.INSTANCE.getDataBase();
    }

    public void executeUpdate(String string, Object... obj) throws SQLException {

        PreparedStatement ps = getConnection().prepareStatement(string);
        for(int i = 0; i < obj.length; i++) {
            ps.setObject(i + 1, obj[i]);
        }
        ps.executeUpdate();
        ps.close();
    }


    public void openConnection() throws SQLException {
        try {

            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("driver not found");
        }
        connection = DriverManager.getConnection(url,user,password);
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectionOpened() {
        return (connection != null);
    }

    public Connection getConnection() {
        return connection;
    }



}
