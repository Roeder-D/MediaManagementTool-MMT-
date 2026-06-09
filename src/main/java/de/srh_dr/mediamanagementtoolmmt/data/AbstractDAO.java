package de.srh_dr.mediamanagementtoolmmt.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDAO<T> {

    protected abstract String getTableName();
    protected abstract String getIdColumnName();
    protected abstract String getValueColumnName();
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;

    public List<T> findAll() {
        String sql = String.format("SELECT * FROM %s ORDER BY %s ASC",
                getTableName(), getValueColumnName());
        List<T> items = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching from " + getTableName());
        }
        return items;
    }

    public T findById(int id) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?",
                getTableName(), getIdColumnName());
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching from " + getTableName());
        }
        return null;
    }
}