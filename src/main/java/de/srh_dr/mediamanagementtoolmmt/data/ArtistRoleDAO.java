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
    public void save(Artist artist) {
        if (artist.isNewItem()) {
            create(artist);
        } else {
            update(artist);
        }
    }

    // CREATE
    private void create(Artist artist) {
        String sql = "INSERT INTO artist (first_name, last_name, nationality) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, artist.getFirstName());
            stmt.setString(2, artist.getLastName());
            stmt.setString(3, artist.getNationality());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    artist.setId(rs.getInt(1));
                }
            }

            artist.clearChangeTracking();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // UPDATE
    private void update(Artist artist) {
        if (artist.isNewItem()) return;
        if (!artist.isDirty()) return;

        String sql = "UPDATE artist SET first_name = ?, last_name = ?, nationality = ? WHERE artist_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artist.getFirstName());
            stmt.setString(2, artist.getLastName());
            stmt.setString(3, artist.getNationality());
            stmt.setInt(4, artist.getId());

            stmt.executeUpdate();
            artist.clearChangeTracking();

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