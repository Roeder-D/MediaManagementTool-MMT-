package de.srh_dr.mediamanagementtoolmmt.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

class DBConnectionTest {

    @Test
    void testConnectionIsSuccessful() {
        // try-with-resources block so the connection automatically closes itself after the test finishes
        try (Connection connection = DBConnection.getConnection()) {
            assertNotNull(connection, "The database connection should not be null.");

            assertFalse(connection.isClosed(), "The database connection should be open.");

            System.out.println("Success: Database connection established!");

        } catch (SQLException e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }
}