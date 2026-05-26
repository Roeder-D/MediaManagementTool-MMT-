package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.model.ArtistRole;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;

public class ArtistRoleDAO extends AbstractDAO<ArtistRole> {

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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM artist WHERE artist_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Error code 1451 is the standard MySQL code for a Foreign Key violation
            if (e.getErrorCode() == 1451) {
                System.err.println(LanguageManager.getString(("sql.error.artist.cannot_delete")));
            }
            return false;
        }
    }
}