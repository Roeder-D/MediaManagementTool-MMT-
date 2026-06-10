package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Genre;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;

public class GenreDAO extends AbstractDAO<Genre> {
    @Override protected String getTableName() { return "genre"; }
    @Override protected String getIdColumnName() { return "genre_id"; }
    @Override protected String getValueColumnName() { return "genre_name"; }

    @Override
    protected Genre mapResultSet(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre_name"),
                false
        );
    }

    // HELPER
    public void save(Genre genre) throws SQLException {
        if (genre.isNewItem()) {
            create(genre);
        } else {
            update(genre);
        }
    }

    // CREATE
    private void create(Genre genre) throws SQLException {
        String sql = "INSERT INTO genre (genre_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, genre.getGenreName());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    genre.setId(rs.getInt(1));
                }
            }

            genre.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("Error saving genre " + genre.getGenreName());
        }
    }

    // UPDATE
    private void update(Genre genre) {
        if (genre.isNewItem()) return;
        if (!genre.isDirty()) return;

        String sql = "UPDATE genre SET genre_name = ? WHERE genre_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, genre.getGenreName());
            stmt.setInt(2, genre.getId());

            stmt.executeUpdate();
            genre.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("Error updating genre " + genre.getGenreName());
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM genre WHERE genre_id = ?";
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