package de.srh_dr.mediamanagementtoolmmt.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    //using "io.github.cdimascio.dotenv.java" to load .env-files
    private static final Dotenv dotenv = Dotenv.load();
    private static final HikariDataSource dataSource;

    //creating a connection pool using HikariDataSouce
    static {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = "jdbc:mysql://" + dotenv.get("DB_HOST") + ":" + dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME");

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dotenv.get("DB_USER"));
        config.setPassword(dotenv.get("DB_PASSWORD"));

        config.setMaximumPoolSize(10); // MediaService requires up to 9 connections simultaneously
        config.setMinimumIdle(2);

        dataSource = new HikariDataSource(config);
    }


    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool(){
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
    }
}
