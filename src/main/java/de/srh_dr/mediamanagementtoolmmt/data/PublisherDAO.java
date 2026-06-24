package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Publisher;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublisherDAO extends AbstractDAO<Publisher> {
    private static final Logger LOGGER = Logger.getLogger(PublisherDAO.class.getName());
    private static final PublisherDAO INSTANCE = new PublisherDAO();

    public static PublisherDAO getInstance(){
        return INSTANCE;
    }

    private PublisherDAO(){}

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

    // Helper for switching between create and update
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
            LOGGER.log(Level.SEVERE,"Error creating publisher " + publisher.getPublisherName(), e);
        }
    }

    // UPDATE
    private void update(Publisher publisher) {
        if (publisher.isNewItem()) return;
        if (!publisher.isDirty()) return;

        String sql = "UPDATE publisher SET publisher_name = ? WHERE publisher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publisher.getPublisherName());
            stmt.setInt(2, publisher.getId());

            stmt.executeUpdate();
            publisher.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error updating publisher " + publisher.getPublisherName(), e);
        }
    }

    // READ
    public Publisher findByName(String name) {
        String sql = "SELECT * FROM publisher WHERE publisher_name=?";
        Publisher publisher = null;

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1,name);
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    publisher = new Publisher(
                            rs.getInt("publisher_id"),
                            rs.getString("publisher_name"),
                            false
                    );
                }
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE,LanguageManager.getString(("sql.error.artist.cannot_delete")), e);
        }
        return publisher;
    }
}
