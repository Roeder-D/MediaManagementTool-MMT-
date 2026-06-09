package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Publisher;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;

public class PublisherDAO extends AbstractDAO<Publisher> {
    @Override protected String getTableName() { return "publisher"; }
    @Override protected String getIdColumnName() { return "publisher_id"; }
    @Override protected String getValueColumnName() { return "publisher_name"; }

    @Override
    protected Publisher mapResultSet(ResultSet rs) throws SQLException {
        return new Publisher(
                rs.getInt("publisher_id"),
                rs.getString("publisher_name"),
                false
        );
    }

    // HELPER
    public void save(Publisher publisher) throws SQLException {
        if (publisher.isNewItem()) {
            create(publisher);
        } else {
            update(publisher);
        }
    }

    // CREATE
    private void create(Publisher publisher) {
        String sql = "INSERT INTO publisher (publisher_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, publisher.getPublisherName());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    publisher.setId(rs.getInt(1));
                }
            }

            publisher.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("Error creating publisher " + publisher.getPublisherName());
        }
    }

    // UPDATE
    private void update(Publisher publisher) {
        if (publisher.isNewItem()) return;
        if (!publisher.isDirty()) return;

        String sql = "UPDATE tag SET publisher_name = ? WHERE publisher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publisher.getPublisherName());
            stmt.setInt(2, publisher.getId());

            stmt.executeUpdate();
            publisher.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("Error updating publisher " + publisher.getPublisherName());
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM publisher WHERE publisher_id = ?";
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
