package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Lendee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LendeeDAO {
    // HELPER
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
            System.err.println("Failed to create lendee " + lendee.getFirstName() + " " + lendee.getLastName());
        }
    }

    // READ (by ID)
    public Lendee findById(int id) {
        String sql = "SELECT * FROM lendee WHERE lendee_id = ?";
        Lendee lendee = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    lendee = new Lendee(
                            rs.getInt("lendee_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("alias"),
                            false
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding lendee " + id);
        }
        return lendee;
    }

    // READ (all)
    public List<Lendee> findAll() {
        String sql = "SELECT * FROM lendee";
        List<Lendee> lendees = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lendees.add(new Lendee(
                        rs.getInt("lendee_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("alias"),
                        false
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error finding lendees " + lendees.size());
        }

        return lendees;
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
            System.err.println("Error updating lendee " + lendee.getId());
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM lendee WHERE lendee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting lendee " + id);
        }
    }
}
