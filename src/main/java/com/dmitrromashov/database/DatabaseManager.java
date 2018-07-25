package com.dmitrromashov.database;

import com.dmitrromashov.WeatherSubscription;
import com.dmitrromashov.WeatherState;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DatabaseManager {

    private Connection connection;
    private String weatherSubscriptionTableName = "WeatherSubscription";
    private String knownWeatherTableName = "KnownWeather";

    public DatabaseManager() {
        try {
            String connectionURL = "jdbc:h2:./WeatherDB";
            String userName = "sa";
            String password = "";
            connection = DriverManager.getConnection(connectionURL, userName, password);
            createWeatherSubscriptionTable(connection);
            createKnownWeatherTable(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createWeatherSubscriptionTable(Connection connection) {

        try (Statement createTableStatement = connection.createStatement()) {
            String weatherSubscriptionCreateTableQuery = "CREATE TABLE IF NOT EXISTS "
                    + weatherSubscriptionTableName + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "userId INT,"
                    + "city VARCHAR(50),"
                    + "period INT,"
                    + "userName VARCHAR(50))";
            createTableStatement.execute(weatherSubscriptionCreateTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createKnownWeatherTable(Connection connection) {

        try (Statement createTableStatement = connection.createStatement()) {
            String knownWeatherCreateTableQuery = "CREATE TABLE IF NOT EXISTS "
                    + knownWeatherTableName + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "userId INT,"
                    + "city VARCHAR(50),"
                    + "condition VARCHAR(50),"
                    + "time INT);";

            createTableStatement.execute(knownWeatherCreateTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public WeatherState getKnownWeatherState(int userId, String city) {
        String query = "SELECT condition, time from " + knownWeatherTableName
                + " WHERE userId = ? and city = ?;";

        WeatherState knownWeatherState = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, city);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String condition = resultSet.getString("condition");
                    int time = resultSet.getInt("time");
                    knownWeatherState = new WeatherState(condition, time);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return knownWeatherState;
    }

    public void addKnownWeatherState(int userId, String city, String condition, int time) {
        String query = "INSERT INTO " + knownWeatherTableName + " (userId, city, condition, time)" +
                "VALUES (?,?,?,?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, city);
            preparedStatement.setString(3, condition);
            preparedStatement.setInt(4, time);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void updateKnownWeatherState(int userId, String city, String condition, int time) {
        String query = "UPDATE " + knownWeatherTableName
                + " SET condition = ?, time = ? " +
                "WHERE userId = ? and city = ?;";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, condition);
            preparedStatement.setInt(2, time);
            preparedStatement.setInt(3, userId);
            preparedStatement.setString(4, city);
            int rowUpdated = preparedStatement.executeUpdate();
            System.out.println("Updated " + rowUpdated + " rows");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addWeatherSubscription(Integer userId, String city, int period, String userName) throws Exception {
        String query = "INSERT INTO " +
                "" + weatherSubscriptionTableName + "(userId, city, period, userName)" +
                "VALUES (?,?,?, ?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, city);
            preparedStatement.setInt(3, period);
            preparedStatement.setString(4, userName);
            boolean result = preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Не получилось создать уведомление");
        }
    }

    public List<WeatherSubscription> getAllWeatherSucscriptions() {
        List<WeatherSubscription> weatherSubscriptions = new ArrayList<>();

        String query = "SELECT userId, city, period from " + weatherSubscriptionTableName + ";";


        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int userId = resultSet.getInt("userId");
                    String city = resultSet.getString("city");
                    int period = resultSet.getInt("period");

                    WeatherSubscription weatherSubscription = new WeatherSubscription(userId, city, period);
                    weatherSubscriptions.add(weatherSubscription);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return weatherSubscriptions;

    }

    public void deleteUserSubscription(int userId) {
        String query = "DELETE FROM " +
                "" + weatherSubscriptionTableName + " WHERE userId = ?;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUserWeatherState(int userId) {
        String query = "DELETE FROM " +
                "" + knownWeatherTableName + " WHERE userId = ?;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
