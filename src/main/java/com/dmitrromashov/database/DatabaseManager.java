package com.dmitrromashov.database;

import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class DatabaseManager {

    private Connection connection;
    private String weatherSubscriptionTableName = "WeatherSubscription";
    private String weatherTableName = "Weather";

    public DatabaseManager() {
        try {
            String connectionURL = "jdbc:h2:./WeatherDB";
            String userName = "sa";
            String password = "";
            connection = DriverManager.getConnection(connectionURL, userName, password);
            createWeatherSubscriptionTable(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createWeatherSubscriptionTable(Connection connection){
        try {
            Statement createTableStatement = connection.createStatement();
            String weatherSubscriptionCreateTableQuery = "CREATE TABLE IF NOT EXISTS "
                    + weatherSubscriptionTableName + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "userId INT,"
                    + "city VARCHAR(50),"
                    + "period INT)";

            createTableStatement.execute(weatherSubscriptionCreateTableQuery);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addWeatherNotification(Integer userId, String city, int period) throws Exception {
        String query = "INSERT INTO " +
                "" + weatherSubscriptionTableName + "(userId, city, period)" +
                "VALUES (?,?,?);";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, city);
            preparedStatement.setInt(3, period);
            boolean result = preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Не получилось создать уведомление");
        }
    }
}
