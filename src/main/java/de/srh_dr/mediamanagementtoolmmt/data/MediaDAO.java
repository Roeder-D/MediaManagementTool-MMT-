package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


//Complex structure to ensure synchronized db-updates across all affected tables
public class MediaDAO {
    // HELPER
    public void save(Media media){
        Connection conn = null;
        try{
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); //Prevent partial updates

            if(media.isNewItem()){
                create(media, conn);
            }else{
                update(media, conn);
            }

            // Synchronize junction tables
            updateRelations(media, conn);

            conn.commit();
            media.clearChangeTracking();
        }catch(SQLException e){
            // Rollback in case of failure
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back due to: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }finally {
            if(conn != null){
                try{
                    conn.setAutoCommit(true); // Reset to default behavior
                    conn.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    // CREATE
    private void create(Media media, Connection conn) throws SQLException {
        String sql = "INSERT INTO media (isbn, title, original_title, cover_url, description, rating, release_date, series_order, series_id, mediatype_id, publisher_id, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            prepareMediaStatement(stmt, media);

            stmt.executeUpdate();
            try(ResultSet rs = stmt.getGeneratedKeys()){
                if(rs.next()){
                    media.setId(rs.getInt(1));
                }
            }
        }

    }

    // UPDATE
    private void update(Media media, Connection conn) throws SQLException {
        EnumSet<MediaField> dirty = media.getDirtyFields();
        if (dirty.isEmpty()) return;

        StringBuilder sql = new StringBuilder("UPDATE media SET ");
        List<Object> values = new ArrayList<>();

        // Map Enum to Column Names
        for (MediaField field : dirty) {
            switch (field) {
                case ISBN -> appendUpdate(sql, "isbn", values, media.getIsbn());
                case TITLE -> appendUpdate(sql, "title", values, media.getTitle());
                case ORIGINAL_TITLE -> appendUpdate(sql, "original_title", values, media.getOriginalTitle());
                case COVER -> appendUpdate(sql, "cover_url", values, media.getCoverFileName());
                case DESCRIPTION -> appendUpdate(sql, "description", values, media.getDescription());
                case RATING -> appendUpdate(sql, "rating", values, media.getRating());
                case RELEASE_DATE -> appendUpdate(sql, "release_date", values, media.getReleaseDate() != null ? Date.valueOf(media.getReleaseDate()) : null);
                case SERIES_ORDER -> appendUpdate(sql, "series_order", values, media.getSeriesOrder());
                case SERIES -> appendUpdate(sql, "series_id", values, media.getSeries().getId());
                case PUBLISHER -> appendUpdate(sql, "publisher_id", values, media.getPublisher().getId());
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

    private void appendUpdate(StringBuilder sql, String column, List<Object> values, Object value) {
        sql.append(column).append(" = ?, ");
        values.add(value);
    }

    // DELETE
    public void deleteById(int id) {
        String sql = "DELETE FROM media WHERE media_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper for preparedStatement
    private void prepareMediaStatement(PreparedStatement stmt, Media m) throws SQLException {
        stmt.setString(1, m.getIsbn());
        stmt.setString(2, m.getTitle());
        stmt.setString(3, m.getOriginalTitle());
        stmt.setString(4, m.getCoverFileName());
        stmt.setString(5, m.getDescription());
        stmt.setInt(6, m.getRating());
        stmt.setDate(7, m.getReleaseDate() != null ? Date.valueOf(m.getReleaseDate()) : null);
        stmt.setInt(8, m.getSeriesOrder());
        stmt.setInt(9, m.getSeries().getId());
        stmt.setInt(10, m.getMediatype().getId());
        stmt.setInt(11, m.getPublisher().getId());
        stmt.setString(12, m.getStatus().name());
    }
    // Helpers for synchronization of affected tables
    private void updateRelations(Media media, Connection conn) throws SQLException {
        // simple Many-to-Many relations
        syncJunction(conn, media.getId(), "media_tag", "tag_id", media.getTagsToAdd(), media.getTagsToRemove());
        syncJunction(conn, media.getId(), "media_genre", "genre_id", media.getGenresToAdd(), media.getGenresToRemove());
        syncJunction(conn, media.getId(), "media_language", "language_id", media.getLanguagesToAdd(), media.getLanguagesToRemove());
        syncJunction(conn, media.getId(), "media_franchise", "franchise_id", media.getFranchisesToAdd(), media.getFranchisesToRemove());

        // MediaArtist(Credits)
        String insSql = "INSERT INTO media_artist (media_id, artist_id, artist_role_id) VALUES (?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(insSql)) {
            for (MediaArtist ma : media.getCreditsToAdd()) {
                stmt.setInt(1, media.getId());
                stmt.setInt(2, ma.getArtist().getId());
                stmt.setInt(3, ma.getArtistRole().getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

        String delSql = "DELETE FROM media_artist WHERE media_id=? AND artist_id=? AND artist_role_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(delSql)) {
            for (MediaArtist ma : media.getCreditsToRemove()) {
                stmt.setInt(1, media.getId());
                stmt.setInt(2, ma.getArtist().getId());
                stmt.setInt(3, ma.getArtistRole().getId());
                stmt.addBatch(); // Batching is better for performance here too
            }
            stmt.executeBatch();
        }
    }


    private void syncJunction(Connection conn, int mediaId, String table, String column, List<?> toAdd, List<?> toRemove) throws SQLException {
        if(!toAdd.isEmpty()){
            String insSql = "INSERT INTO " + table + " (media_id,  " +  column + ") VALUES (?, ?)";

            try(PreparedStatement stmt = conn.prepareStatement(insSql)){
                for(Object o : toAdd){
                    stmt.setInt(1, mediaId);
                    stmt.setInt(2, getEntityId(o));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
        if(!toRemove.isEmpty()){
            String insSql = "DELETE FROM " + table + " WHERE media_id = ? AND " + column + " = ?";

            try(PreparedStatement stmt = conn.prepareStatement(insSql)){
                for(Object o : toRemove){
                    stmt.setInt(1, mediaId);
                    stmt.setInt(2, getEntityId(o));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }
    private int getEntityId(Object o){
        if(o instanceof Tag){return ((Tag)o).getId();}
        if(o instanceof Genre){return ((Genre)o).getId();}
        if(o instanceof Language){return ((Language)o).getId();}
        if(o instanceof Franchise){return ((Franchise)o).getId();}
        throw new IllegalArgumentException("Unknown entity type: " + o.getClass().getName());
    }

}
