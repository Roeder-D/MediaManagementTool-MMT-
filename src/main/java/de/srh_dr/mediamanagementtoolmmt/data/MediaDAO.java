package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.dto.MediaEntity;
import de.srh_dr.mediamanagementtoolmmt.model.*;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MediaDAO {
    private static final Logger LOGGER = Logger.getLogger(MediaDAO.class.getName());
    private static final MediaDAO INSTANCE = new MediaDAO();

    public static MediaDAO getInstance(){
        return INSTANCE;
    }

    private MediaDAO(){}

    // READ root entity
    public MediaEntity readMediaEntity(int mediaId){
        String sql = "SELECT * FROM media WHERE media_id = ?";

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, mediaId);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return new MediaEntity(
                            rs.getInt("media_id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("original_title"),
                            rs.getString("cover_url"),
                            rs.getString("description"),
                            rs.getInt("rating"),
                            rs.getDate("release_date") != null ? rs.getDate("release_date").toLocalDate() : null,
                            rs.getInt("series_order"),
                            rs.getInt("series_id"),
                            rs.getInt("media_type_id"),
                            rs.getInt("publisher_id"),
                            Media.MediaStatus.valueOf(rs.getString("status"))
                    );
                }
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to read media entity: " +e.getMessage(),e);
        }
        return null;
    }

    // CREATE
    public int create(Media media, Connection conn) throws SQLException {
        String sql = "INSERT INTO media (isbn, title, original_title, cover_url, description, rating, release_date, series_order," +
                " series_id, media_type_id, publisher_id, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            prepareMediaStatement(stmt, media);

            stmt.executeUpdate();
            try(ResultSet rs = stmt.getGeneratedKeys()){
                if(rs.next()) {
                    int mediaId = rs.getInt(1);
                    media.setId(mediaId);
                    return mediaId;
                }else{
                    throw new SQLException("Creating media failed, no ID obtained.");
                }
            }
        }
    }

    // UPDATE
    public void update(Media media, Connection conn) throws SQLException {
        EnumSet<Media.MediaField> dirty = media.getDirtyFields();
        if (dirty.isEmpty()) return;

        StringBuilder sql = new StringBuilder("UPDATE media SET ");
        List<Object> values = new ArrayList<>();

        // Map Enum to Column Names
        for (Media.MediaField field : dirty) {
            switch (field) {
                case ISBN -> appendUpdate(sql, "isbn", values, media.getIsbn());
                case TITLE -> appendUpdate(sql, "title", values, media.getTitle());
                case ORIGINAL_TITLE -> appendUpdate(sql, "original_title", values, media.getOriginalTitle());
                case COVER -> appendUpdate(sql, "cover_url", values, media.getCoverFileName());
                case DESCRIPTION -> appendUpdate(sql, "description", values, media.getDescription());
                case RATING -> appendUpdate(sql, "rating", values, media.getRating());
                case RELEASE_DATE -> appendUpdate(sql, "release_date", values, media.getReleaseDate() != null ? Date.valueOf(media.getReleaseDate()) : null);
                case SERIES_ORDER -> appendUpdate(sql, "series_order", values, media.getSeriesOrder());
                case SERIES -> appendUpdate(sql, "series_id", values, media.getSeries() != null ? media.getSeries().getId() : null);
                case PUBLISHER -> appendUpdate(sql, "publisher_id", values, media.getPublisher() != null ? media.getPublisher().getId() : null);
                case STATUS -> appendUpdate(sql, "status", values, media.getStatus().name());
            }
        }
        sql.setLength(sql.length() - 2); // Removing the last comma
        sql.append(" WHERE media_id = ?");
        values.add(media.getId());

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        }
    }

    // DELETE
    public void deleteById(int id) {
        String sql = "DELETE FROM media WHERE media_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    // Helper
    private void prepareMediaStatement(PreparedStatement stmt, Media m) throws SQLException {
        stmt.setString(1, m.getIsbn());
        stmt.setString(2, m.getTitle());
        stmt.setString(3, m.getOriginalTitle());
        stmt.setString(4, m.getCoverFileName());
        stmt.setString(5, m.getDescription());
        stmt.setInt(6, m.getRating());
        stmt.setDate(7, m.getReleaseDate() != null ? Date.valueOf(m.getReleaseDate()) : null);
        stmt.setInt(8, m.getSeriesOrder());
        if(m.getSeries() != null){
            stmt.setInt(9, m.getSeries().getId());
        }else{
            stmt.setNull(9, Types.INTEGER);
        }
        stmt.setInt(10, m.getMediatype().getId());
        if(m.getPublisher() != null){
            stmt.setInt(11, m.getPublisher().getId());
        }else{
            stmt.setNull(11, Types.INTEGER);
        }
        stmt.setString(12, m.getStatus().name());
    }

    // dynamic sql builder
    private void appendUpdate(StringBuilder sql, String column, List<Object> values, Object value) {
        sql.append(column).append(" = ?, ");
        values.add(value);
    }
}
