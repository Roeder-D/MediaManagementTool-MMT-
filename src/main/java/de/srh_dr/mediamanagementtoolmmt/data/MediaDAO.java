package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


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
                    System.err.println("SQLException: " + ex.getMessage());
                }
            }
            System.err.println("SQLException: " + e.getMessage());
        }finally{
            if(conn != null){
                try{
                    conn.setAutoCommit(true); // Reset to default behavior
                    conn.close();
                }catch(SQLException e){
                    System.err.println("SQLException: " + e.getMessage());
                }
            }
        }
    }

    // READ by ID
    public Media read(int mediaId) throws SQLException{
        Media media = null;

        String media_sql = "SELECT * FROM media WHERE media_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(media_sql)){
                stmt.setInt(1, mediaId);

                try(ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        Media.Builder builder = new Media.Builder()
                                .isNewItem(false)
                                .id(rs.getInt("media_id"))
                                .isbn(rs.getString("isbn"))
                                .title(rs.getString("title"))
                                .originalTitle(rs.getString("original_title"))
                                .coverFileName(rs.getString("cover_url"))
                                .description(rs.getString("description"))
                                .rating(rs.getInt("rating"))
                                .releaseDate(rs.getDate("release_date") != null ? rs.getDate("release_date").toLocalDate() : null)
                                .seriesOrder(rs.getInt("series_order"))
                                .status(MediaStatus.valueOf(rs.getString("status")));

                        // Fetch linked entities
                        int publisherId = rs.getInt("publisher_id");
                        if(!rs.wasNull()){
                            builder.publisher(new PublisherDAO().findById(publisherId));
                        }
                        int seriesId = rs.getInt("series_id");
                        if(!rs.wasNull()){
                            builder.series(new SeriesDAO().findById(seriesId));
                        }
                        builder.mediaType(new MediaTypeDAO().findById(rs.getInt("media_type_id")));

                        // Fetch lists
                        builder.tags(fetchList(conn, mediaId, "media_tag", "tag_id", new TagDAO()));
                        builder.genres(fetchList(conn, mediaId, "media_genre", "genre_id", new GenreDAO()));
                        builder.languages(fetchList(conn, mediaId, "media_language", "language_id", new LanguageDAO()));

                        builder.franchises(fetchFranchises(conn, mediaId));
                        builder.credits(fetchCredits(conn, mediaId));

                        media = builder.build();
                        media.clearChangeTracking();
                    }
                }
        }
        return media;
    }

    private <T> List<T> fetchList(Connection conn, int mediaId, String table, String column, AbstractDAO<T> dao) throws SQLException {
        List<T> items = new ArrayList<>();
        String sql = "SELECT " + column + " FROM " + table + " WHERE media_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mediaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(dao.findById(rs.getInt(column)));
                }
            }
        }
        return items;
    }

    private List<Franchise> fetchFranchises(Connection conn, int mediaId) throws SQLException {
        List<Franchise> franchises = new ArrayList<>();
        String franchiseSql = "SELECT f.franchise_id, f.franchise_name " +
                "FROM media_franchise mf " +
                "JOIN franchise f ON mf.franchise_id = f.franchise_id " +
                "WHERE mf.media_id = ?";

        String titlesSql = "SELECT alt_title_id, title FROM alt_title WHERE franchise_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(franchiseSql)) {
            stmt.setInt(1, mediaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int franchiseId = rs.getInt("franchise_id");
                    String franchiseName = rs.getString("franchise_name");
                    List<AltTitle> altTitles = new ArrayList<>();

                    try(PreparedStatement titleStmt = conn.prepareStatement(titlesSql)){
                        titleStmt.setInt(1, franchiseId);
                        try(ResultSet rsTitles =  titleStmt.executeQuery()){
                            while(rsTitles.next()){
                                altTitles.add(new AltTitle(
                                   rsTitles.getInt("alt_title_id"),
                                   rsTitles.getString("title"),
                                   false
                                ));
                            }
                        }
                    }
                    franchises.add(new Franchise(franchiseId, franchiseName, altTitles, false));
                }
            }
        }
        return franchises;
    }

    private List<MediaArtist> fetchCredits(Connection conn, int mediaId) throws SQLException {
        List<MediaArtist> credits = new ArrayList<>();
        String sql = "SELECT a.artist_id, a.first_name, a.last_name, a.alias, a.nationality, ar.artist_role_id, ar.role " +
                "FROM media_artist ma JOIN artist a ON ma.artist_id = a.artist_id JOIN artist_role ar ON ma.artist_role_id = ar.artist_role_id " +
                "WHERE ma.media_id = ? ";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mediaId);
            try(ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Artist artist = new Artist(
                            rs.getInt("artist_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("alias"),
                            rs.getString("nationality"),
                            false);
                    ArtistRole artistRole = new ArtistRole(
                            rs.getInt("artist_role_id"),
                            rs.getString("role"),
                            false);

                    credits.add(new MediaArtist(artist, artistRole, false));
                }
            }
        }
        return credits;
    }

    // CREATE
    private void create(Media media, Connection conn) throws SQLException {
        String sql = "INSERT INTO media (isbn, title, original_title, cover_url, description, rating, release_date, series_order," +
                " series_id, media_type_id, publisher_id, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

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
            System.err.println("SQLException: " + e.getMessage());
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
