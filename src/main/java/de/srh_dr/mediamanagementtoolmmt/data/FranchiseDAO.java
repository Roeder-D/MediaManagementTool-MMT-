package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Franchise;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FranchiseDAO {
    // READ
    public List<Franchise> findAll() {
        String sql = "SELECT * FROM franchise ORDER BY franchise_name ASC";
        List<Franchise> franchises = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("franchise_id");
                franchises.add(new Franchise(
                        id,
                        rs.getString("franchise_name"),
                        fetchAltTitles(id), // Lade Titel direkt mit
                        false
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return franchises;
    }

    private List<AltTitle> fetchAltTitles(int franchiseId) {
        String sql = "SELECT alt_title_id, title FROM alt_title WHERE franchise_id = ?";
        List<AltTitle> titles = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, franchiseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    titles.add(new AltTitle(
                            rs.getInt("alt_title_id"),
                            rs.getString("title"),
                            false
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return titles;
    }

    // CREATE
    public void create(Franchise franchise) {
        String sql = "INSERT INTO franchise (franchise_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, franchise.getName());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    franchise.setId(rs.getInt(1));
                }
            }
            franchise.clearChangeTracking();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM franchise WHERE franchise_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}