package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.model.ArtistRole;
import de.srh_dr.mediamanagementtoolmmt.model.MediaArtist;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtistDAO extends AbstractDAO<Artist> {
    private static final Logger LOGGER = Logger.getLogger(ArtistDAO.class.getName());
    private static final ArtistDAO INSTANCE = new ArtistDAO();

    public static ArtistDAO getInstance() {
        return INSTANCE;
    }

    private ArtistDAO() {}

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

    // Helper for switching between create and update
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
            LOGGER.log(Level.SEVERE,"Error creating insert into " + getTableName(), e);
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
            LOGGER.log(Level.SEVERE,"Error getting all nationalities from " + getTableName(), e);
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
            LOGGER.log(Level.SEVERE,"Error looking up artist: " + firstName + " " + lastName, e);
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
            LOGGER.log(Level.SEVERE,"Error updating " + getTableName(), e);
        }
    }

    // Media relations
    public void saveCreditsForMedia(int mediaId, List<MediaArtist> credits, Connection conn) throws SQLException {
        if(credits == null || credits.isEmpty()) return;

        String sql = "INSERT INTO media_artist (media_id, artist_id, artist_role_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (MediaArtist credit : credits) {

                stmt.setInt(1, mediaId);
                stmt.setInt(2, credit.getArtist().getId());

                if(credit.getArtistRole() == null || credit.getArtistRole().getId() == 0){
                    stmt.setNull(3, Types.INTEGER);
                }else {
                    stmt.setInt(3, credit.getArtistRole().getId());
                }

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void deleteCreditsForMedia(int mediaId, List<MediaArtist> credits, Connection conn) throws SQLException {
        if(credits == null || credits.isEmpty()) return;

        String sql = "DELETE FROM media_artist WHERE media_id = ? AND artist_id = ? AND artist_role_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (MediaArtist credit : credits) {
                stmt.setInt(1, mediaId);
                stmt.setInt(2, credit.getArtist().getId());
                stmt.setInt(3, credit.getArtistRole().getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<MediaArtist> fetchByMediaId(int mediaId) {
        String sql = "SELECT a.*, ar.* FROM media_artist ma " +
                "JOIN artist a ON ma.artist_id = a.artist_id " +
                "LEFT JOIN artist_role ar ON ma.artist_role_id = ar.artist_role_id " +
                "WHERE ma.media_id = ?";

        List<MediaArtist> mediaArtists = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, mediaId);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    Artist artist = mapResultSet(rs);

                    int roleId = rs.getInt("artist_role_id");
                    ArtistRole role = null;
                    if(!rs.wasNull()){
                        role = new ArtistRole(roleId, rs.getString("role"), false);
                    }
                    mediaArtists.add(new MediaArtist(artist, role, false));
                }
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE,"Error looking up artist: " + mediaId, e);
        }
        return mediaArtists;
    }
}