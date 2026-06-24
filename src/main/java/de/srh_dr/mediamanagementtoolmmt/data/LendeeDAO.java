package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Lendee;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LendeeDAO extends AbstractDAO<Lendee> {
    private static final Logger LOGGER = Logger.getLogger(LendeeDAO.class.getName());
    private static final LendeeDAO INSTANCE = new LendeeDAO();

    public static LendeeDAO getInstance(){
        return INSTANCE;
    }

    private LendeeDAO(){}

    @Override
    protected String getTableName() {
        return "lendee";
    }

    @Override
    protected String getIdColumnName() {
        return "lendee_id";
    }

    @Override
    protected String getValueColumnName() {
        return "first_name, last_name, alias";
    }

    @Override
    protected Lendee mapResultSet(ResultSet rs) throws SQLException {
        return new Lendee(
                rs.getInt("lendee_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("alias"),
                false
        );
    }

    // Helper for switching between create and update
    public void save(Lendee lendee) {
        if (lendee.isNewItem()) {
            create(lendee);
        } else {
            update(lendee);
        }
    }

    // CREATE
    private void create(Lendee lendee) {
        String sql = "INSERT INTO lendee (first_name, last_name, alias) VALUES (?, ?, ?)";

        // Use try-with-resources to ensure connection and statement are closed
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, lendee.getFirstName());
            stmt.setString(2, lendee.getLastName());
            stmt.setString(3, lendee.getAlias());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // Now possible because you removed 'final'
                    int generatedId = rs.getInt(1);
                    lendee.setId(generatedId);
                }
            }

            lendee.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Failed to create lendee " + lendee.getFirstName() + " " + lendee.getLastName(), e);
        }
    }

    // UPDATE
    private void update(Lendee lendee) {
        if (lendee.isNewItem()) return;
        if (!lendee.isDirty()) return;

        String sql = "UPDATE lendee SET first_name = ?, last_name = ?, alias = ? WHERE lendee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lendee.getFirstName());
            stmt.setString(2, lendee.getLastName());
            stmt.setString(3, lendee.getAlias());
            stmt.setInt(4, lendee.getId());

            stmt.executeUpdate();
            lendee.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error updating lendee " + lendee.getId(), e);
        }
    }
}
