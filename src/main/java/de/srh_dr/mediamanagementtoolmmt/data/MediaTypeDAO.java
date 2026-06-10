package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.MediaType;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaTypeDAO {

    // HELPER
    public void save(MediaType type) {
        if (type.isNewItem()) {
            create(type);
        } else {
            update(type);
        }
    }

    // CREATE
    private void create(MediaType type) {
        String sql = "INSERT INTO media_type (type_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, type.getTypeName());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    type.setId(rs.getInt(1));
                }
            }

            type.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    // READ (by ID)
    public MediaType findById(int id) {
        String sql = "SELECT * FROM media_type WHERE media_type_id = ?";
        MediaType type = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    type = new MediaType(
                            rs.getInt("media_type_id"),
                            rs.getString("type_name"),
                            false
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
        return type;
    }

    // READ (all)
    public List<MediaType> findAll() {
        String sql = "SELECT * FROM media_type";
        List<MediaType> types = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                types.add(new MediaType(
                        rs.getInt("media_type_id"),
                        rs.getString("type_name"),
                        false
                ));
            }

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }

        return types;
    }

    // UPDATE
    private void update(MediaType type) {
        if (type.isNewItem()) return;
        if (!type.isDirty()) return;

        String sql = "UPDATE media_type SET type_name = ? WHERE media_type_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.getTypeName());
            stmt.setInt(2, type.getId());

            stmt.executeUpdate();
            type.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM media_type WHERE media_type_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                System.err.println(LanguageManager.getString("sql.error.mediaType.cannot_delete"));
            } else {
                System.err.println("SQLException: " + e.getMessage());
            }
            return false;
        }
    }
}
