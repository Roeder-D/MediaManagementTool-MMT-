package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Tag;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TagDAO extends AbstractDAO<Tag> {
    Logger LOGGER = Logger.getLogger(TagDAO.class.getName());

    @Override protected String getTableName() { return "tag"; }
    @Override protected String getIdColumnName() { return "tag_id"; }
    @Override protected String getValueColumnName() { return "tag"; }

    @Override
    protected Tag mapResultSet(ResultSet rs) throws SQLException {
        return new Tag(
                rs.getInt("tag_id"),
                rs.getString("tag"),
                false
        );
    }

    // HELPER
    public void save(Tag tag) throws SQLException {
        if (tag.isNewItem()) {
            create(tag);
        } else {
            update(tag);
        }
    }

    // CREATE
    private void create(Tag tag) throws SQLException {
        String sql = "INSERT INTO tag (tag) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tag.getName());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    tag.setId(rs.getInt(1));
                }
            }

            tag.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Failed to insert new tag: " + e.getMessage(), e);
        }
    }

    // UPDATE
    private void update(Tag tag){
        if (tag.isNewItem()) return;
        if (!tag.isDirty()) return;

        String sql = "UPDATE tag SET tag = ? WHERE tag_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tag.getName());
            stmt.setInt(2, tag.getId());

            stmt.executeUpdate();
            tag.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Failed to update tag: " + e.getMessage(), e);
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM tag WHERE tag_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Error code 1451 is the standard MySQL code for a Foreign Key violation
            if (e.getErrorCode() == 1451) {
                LOGGER.log(Level.WARNING,"Cannot delete tag:" + e.getMessage(), e);
            }
            return false;
        }
    }

//Media relations
    public void saveTagsForMedia(int mediaId, List<Tag> tags, Connection conn) throws SQLException {
        String sql = "INSERT INTO media_tag (media_id, tag_id) VALUES (?, ?)";
        saveJunctionBatch(mediaId, tags, sql, conn);
    }

    public void deleteTagsForMedia(int mediaId, List<Tag> tagsToDelete, Connection conn) throws SQLException {
        String sql = "DELETE FROM media_tag WHERE media_id = ? AND tag_id = ?";
        deleteJunctionBatch(mediaId, tagsToDelete, sql, conn);
    }

    public List<Tag> fetchByMediaId(int mediaId) throws SQLException {
        return fetchViaJunction(mediaId, "media_tag", "tag_id");
    }
}