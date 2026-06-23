package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.MediaType;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaTypeDAO extends AbstractDAO<MediaType> {
    private static final Logger LOGGER = Logger.getLogger(MediaTypeDAO.class.getName());
    private static final MediaTypeDAO INSTANCE = new MediaTypeDAO();

    public static MediaTypeDAO getInstance(){
        return INSTANCE;
    }

    private MediaTypeDAO(){}

    @Override
    protected String getTableName() {
        return "media_type";
    }

    @Override
    protected String getIdColumnName() {
        return "media_type_id";
    }

    @Override
    protected String getValueColumnName() {
        return "type_name";
    }

    @Override
    protected MediaType mapResultSet(ResultSet rs) throws SQLException {
        return new MediaType(
                rs.getInt("media_type_id"),
                rs.getString("type_name"),
                false
        );
    }

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
            LOGGER.log(Level.SEVERE,"Failed to insert new media " + e.getMessage(), e);
        }
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
            LOGGER.log(Level.SEVERE,"Failed to update media: " + e.getMessage(), e);
        }
    }

}
