package de.srh_dr.mediamanagementtoolmmt.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class DBConnection {
   private static HikariDataSource dataSource;

   public static void initPool(HikariConfig hikariConfig) {
       dataSource = new HikariDataSource(hikariConfig);
   }

   public static Connection getConnection() throws SQLException {
       if(dataSource == null){
           throw new SQLException("HikariDataSource not initialized");
       }
       return dataSource.getConnection();
   }

   public static boolean isConnected() {
       if(dataSource == null || dataSource.isClosed()) {
           return false;
       }
       try (Connection conn = dataSource.getConnection()){
           return conn.isValid(2);
       }catch (SQLException e){
           return false;
       }
   }

   public static boolean isInitialized() {
       return dataSource != null;
   }

   public static void closePool() {
       if (dataSource != null) {
           dataSource.close();
       }
   }
}
