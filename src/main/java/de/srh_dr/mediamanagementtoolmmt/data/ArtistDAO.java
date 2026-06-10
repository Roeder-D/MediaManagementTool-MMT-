package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtistDAO extends AbstractDAO<Artist> {
    @Override
    protected String getTableName() {return "artist";}
    @Override
    protected String getIdColumnName() {return "artist_id";}
    @Override
    protected  String getValueColumnName() {return "last_name";}
    @Override
    protected Artist mapResultSet(ResultSet rs) throws SQLException {
        return new Artist(
                rs.getInt("artist_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("alias"),
                rs.getString("nationality"),
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
        String sql = "INSERT INTO artist (first_name, last_name, alias, nationality) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, artist.getFirstName());
            stmt.setString(2, artist.getLastName());
            stmt.setString(3, artist.getAlias());
            stmt.setString(4, artist.getNationality());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    artist.setId(rs.getInt(1));
                }
            }

            artist.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("Error creating insert into " + getTableName());
        }
    }

    // READ
    public List<String> getAllNationalities() {
        String sql = "SELECT DISTINCT nationality FROM artist WHERE nationality IS NOT NULL AND nationality != '' ORDER BY nationality ASC";
        List<String> nationalities = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                nationalities.add(rs.getString("nationality"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all nationalities from " + getTableName());
        }
        return nationalities;
    }

    public Artist findByFullName(String firstName, String lastName) {
        String sql = "SELECT * FROM artist WHERE first_name = ? AND last_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error looking up artist: " + firstName + " " + lastName);
        }
        return null;
    }

    // UPDATE
    private void update(Artist artist) {
        if (artist.isNewItem()) return;
        if (!artist.isDirty()) return;

        String sql = "UPDATE artist SET first_name = ?, last_name = ?, alias = ?, nationality = ? WHERE artist_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artist.getFirstName());
            stmt.setString(2, artist.getLastName());
            stmt.setString(3, artist.getAlias());
            stmt.setString(4, artist.getNationality());
            stmt.setInt(5, artist.getId());

            stmt.executeUpdate();
            artist.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("Error updating " + getTableName());
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