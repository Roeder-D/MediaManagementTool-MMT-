package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Genre;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenreDAO extends AbstractDAO<Genre> {
    private static final Logger LOGGER = Logger.getLogger(GenreDAO.class.getName());
    private static final GenreDAO INSTANCE = new GenreDAO();

    public static GenreDAO getInstance(){
        return INSTANCE;
    }

    private GenreDAO(){}

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
    private void create(Genre genre){
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
            LOGGER.log(Level.SEVERE,"Error saving genre " + genre.getGenreName(), e);
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
            LOGGER.log(Level.SEVERE,"Error updating genre " + genre.getGenreName(), e);
        }
    }

    //Media relations

    public void saveGenresForMedia(int mediaId, List<Genre> genres, Connection conn) throws SQLException {
        String sql = "INSERT INTO media_genre (media_id, genre_id) VALUES (?, ?)";
        saveJunctionBatch(mediaId, genres, sql, conn);
    }

    public void deleteGenresForMedia(int mediaId, List<Genre> genresToDelete, Connection conn) throws SQLException {
        String sql = "DELETE FROM media_genre WHERE media_id = ? AND genre_id = ?";
        deleteJunctionBatch(mediaId, genresToDelete, sql, conn);
    }

    public List<Genre> fetchByMediaId(int mediaId) throws SQLException {
        return fetchViaJunction(mediaId, "media_genre", "genre_id");
    }
}