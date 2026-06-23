package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.ArtistRole;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtistRoleDAO extends AbstractDAO<ArtistRole> {
    private static final Logger LOGGER = Logger.getLogger(ArtistRoleDAO.class.getName());
    private final static ArtistRoleDAO INSTANCE = new ArtistRoleDAO();

    public static ArtistRoleDAO getInstance() {
        return INSTANCE;
    }

    private ArtistRoleDAO(){}

    @Override
    protected String getTableName() { return "artist_role"; }

    @Override
    protected String getIdColumnName() { return "artist_role_id"; }

    @Override
    protected String getValueColumnName() { return "role"; }

    @Override
    protected ArtistRole mapResultSet(ResultSet rs) throws SQLException {
        return new ArtistRole(
                rs.getInt("artist_role_id"),
                rs.getString("role"),
                false
        );
    }

    // HELPER
    public void save(ArtistRole artistRole) {
        if (artistRole.isNewItem()) {
            create(artistRole);
        } else {
            update(artistRole);
        }
    }

    // CREATE
    private void create(ArtistRole artistRole) {
        String sql = "INSERT INTO artist_role (role) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, artistRole.getRole());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    artistRole.setId(rs.getInt(1));
                }
            }

            artistRole.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error saving artist_role: " + e.getMessage(), e);
        }
    }

    // UPDATE
    private void update(ArtistRole artistRole) {
        if (artistRole.isNewItem()) return;
        if (!artistRole.isDirty()) return;

        String sql = "UPDATE artist_role SET role = ? WHERE artist_role_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artistRole.getRole());
            stmt.setInt(2, artistRole.getId());

            stmt.executeUpdate();
            artistRole.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error updating artist_role: " + e.getMessage(), e);
        }
    }
}