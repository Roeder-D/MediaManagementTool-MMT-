package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Identifiable;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDAO<T> {
    Logger LOGGER = Logger.getLogger(AbstractDAO.class.getName());

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
            LOGGER.log(Level.WARNING,"Error fetching from " + getTableName(), e);
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
            LOGGER.log(Level.WARNING, "Error fetching from " + getTableName(), e);
        }
        return null;
    }

    protected void saveJunctionBatch(int mediaId, List<? extends Identifiable> items, String sql, Connection conn) throws SQLException{
        if (items == null || items.isEmpty()) return;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Identifiable item : items) {
                stmt.setInt(1, mediaId);
                stmt.setInt(2, item.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    protected void deleteJunctionBatch(int mediaId, List<? extends Identifiable> items, String sql, Connection conn) throws SQLException{
        if (items == null || items.isEmpty()) return;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Identifiable item : items) {
                stmt.setInt(1, mediaId);
                stmt.setInt(2, item.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    protected List<T> fetchViaJunction(int mediaId, String junctionTable, String junctionIdColumn){
        String sql = String.format("SELECT t.* FROM %s t JOIN  %s ON t.%s = j.%s WHERE j.media_id = ?",
                getTableName(), junctionTable, getIdColumnName(), junctionIdColumn);

        List<T> items = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, mediaId);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    items.add(mapResultSet(rs));
                }
            }
        }catch (SQLException e){
            LOGGER.log(Level.WARNING,"Error fetching from " + getTableName(), e);
        }
        return items;
    }

    public boolean delete(int id) {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", getTableName(), getIdColumnName());
        boolean success = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, id);
            success = stmt.executeUpdate() > 0;
        }catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error deleting from " + getTableName(), e);
        }
        return success;
    }
}