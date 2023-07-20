package com.thgplugins.regions.repository;

import com.thgplugins.regions.util.RegionsConstants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AsyncSQLAPI {

    private JavaPlugin plugin;

    private final HikariDataSource dataSource;

    public AsyncSQLAPI(JavaPlugin plugin) throws SQLException {
        this.plugin = plugin;
        dataSource = createSQLPool(RegionsConstants.database, 10);
        if (dataSource.isRunning()) {
            Stream.of("Successfully established database connection with the following details:",
                    "JDBC URL: " + dataSource.getJdbcUrl(),
                    "Maximum Pool Size: " + dataSource.getMaximumPoolSize(),
                    "Username: " + dataSource.getUsername()).forEach(plugin.getLogger()::info);
        }
    }


    public CompletableFuture<Void> executeUpdate(String sql, Object... parameters) {
        return CompletableFuture.runAsync(() -> {
            try (var connection = dataSource.getConnection()) {
                if (connection.isValid(2)) {
                    try (var statement = connection.prepareStatement(sql)) {
                        for (int i = 0; i < parameters.length; i++) {
                            statement.setObject(i + 1, parameters[i]);
                        }
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> executeQuery(String sql, Consumer<ResultSet> resultSetProcessor, Object... parameters) {
        return CompletableFuture.runAsync(() -> {
            try (var connection = dataSource.getConnection()) {
                if (connection.isValid(2)) {
                    try (var statement = connection.prepareStatement(sql)) {
                        for (int i = 0; i < parameters.length; i++) {
                            statement.setObject(i + 1, parameters[i]);
                        }
                        try (var resultSet = statement.executeQuery()) {
                            resultSetProcessor.accept(resultSet);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @NotNull
    @ApiStatus.Internal
    private HikariDataSource createSQLPool(@NotNull String database, int maximumPoolSize) {
        var config = plugin.getConfig();

        var mysqlHost = config.getString("database.host", "localhost");
        int mysqlPort = config.getInt("database.port", 3306);
        var mysqlUser = config.getString("database.user", "root");
        var mysqlPassword = config.getString("database.password", "");

        long leakDetectionThreshold = config.getLong("database.leak-detection-threshold-ms", 0L);

        var hikariConfig = this.createDefaultConfig(mysqlHost, mysqlPort, mysqlUser, mysqlPassword,
                database, leakDetectionThreshold);
        hikariConfig.setMaximumPoolSize(maximumPoolSize);

        return new HikariDataSource(hikariConfig);
    }

    @NotNull
    private HikariConfig createDefaultConfig(@NotNull String host, int port, @NotNull String user, @NotNull String password, @NotNull String database, long leakDetectionThreshold) {
        var config = new HikariConfig();

        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, database));
        config.setUsername(user);
        config.setPassword(password);

        config.setConnectionTimeout(10000);

        if (leakDetectionThreshold > 0) {
            config.setLeakDetectionThreshold(leakDetectionThreshold);
        }

        return config;
    }

    public CompletableFuture<Integer> insertDatabase(String sql, Object... parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try (var connection = dataSource.getConnection();
                 var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
                statement.executeUpdate();

                try (var generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating item failed, no ID obtained.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
