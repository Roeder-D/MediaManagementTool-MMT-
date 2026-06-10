package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Franchise;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;
import java.util.*;

public class FranchiseDAO {
    // HELPER
    public void save(Franchise franchise) {
        if (franchise.isNewItem()) {
            create(franchise);
        } else {
            update(franchise);
        }
    }

    // CREATE
    private void create(Franchise franchise) {
        String sql = "INSERT INTO franchise (franchise_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Prevents incomplete data from being inserted

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, franchise.getName());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        franchise.setId(rs.getInt(1));
                    }
                }

                saveAltTitles(conn, franchise.getId(), franchise.getAltTitles());

                conn.commit();
                franchise.clearChangeTracking();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving franchise " + franchise.getName());
        }
    }

    private void saveAltTitles(Connection conn, int franchiseId, List<AltTitle> titles) throws SQLException {
        String sql = "INSERT INTO alt_title (title, franchise_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (AltTitle title : titles) {
                stmt.setString(1, title.getTitle());
                stmt.setInt(2, franchiseId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // UPDATE
    private void update(Franchise franchise) {
        String franchiseSql = "UPDATE franchise SET franchise_name = ? WHERE franchise_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Prevents incomplete data from being inserted

            try {
                // Refresh core data if isDirty
                if (franchise.isDirty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(franchiseSql)) {
                        stmt.setString(1, franchise.getName());
                        stmt.setInt(2, franchise.getId());
                        stmt.executeUpdate();
                    }
                }

                // Synchronizing AltTitles
                if (franchise.listChanged()) {
                    syncAltTitles(conn, franchise);
                }

                conn.commit();
                franchise.clearChangeTracking();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error updating franchise " + franchise.getName());
        }
    }

    private void syncAltTitles(Connection conn, Franchise franchise) throws SQLException {
        // Delete old data
        if (!franchise.getAltTitlesToRemove().isEmpty()) {
            String deleteSql = "DELETE FROM alt_title WHERE alt_title_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                for (AltTitle title : franchise.getAltTitlesToRemove()) {
                    stmt.setInt(1, title.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        // Insert new data
        if (!franchise.getAltTitlesToAdd().isEmpty()) {
            String insertSql = "INSERT INTO alt_title (title, franchise_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (AltTitle title : franchise.getAltTitlesToAdd()) {
                    stmt.setString(1, title.getTitle());
                    stmt.setInt(2, franchise.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
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
            if (e.getErrorCode() == 1451) {
                System.err.println(LanguageManager.getString("sql.error.franchise.cannot_delete"));
            } else {
                System.err.println("Error deleting franchise " + id);
            }
            return false;
        }
    }

    // READ (findById)
    public Franchise findById(int id) throws SQLException {
        String franchiseSql = "SELECT * FROM franchise WHERE franchise_id = ?";
        String titlesSql = "SELECT * FROM alt_title WHERE franchise_id = ?";
        Franchise franchise = null;

        try (Connection conn = DBConnection.getConnection()) {
            // Load franchise
            try (PreparedStatement stmt = conn.prepareStatement(franchiseSql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    List<AltTitle> titles = new ArrayList<>();
                    // Load altTitles
                    try (PreparedStatement titleStmt = conn.prepareStatement(titlesSql)) {
                        titleStmt.setInt(1, id);
                        ResultSet rsTitles = titleStmt.executeQuery();
                        while (rsTitles.next()) {
                            titles.add(new AltTitle(
                                    rsTitles.getInt("alt_title_id"),
                                    rsTitles.getString("title"),
                                    false
                            ));
                        }
                    }

                    franchise = new Franchise(
                            rs.getInt("franchise_id"),
                            rs.getString("franchise_name"),
                            titles,
                            false
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching franchise " + id);
        }
        return franchise;
    }

    // READ all
    public List<Franchise> findAll() {
        Map<Integer, Franchise> franchiseMap = new LinkedHashMap<>();

        String franchiseSql = "SELECT * FROM franchise";
        // Optimized: Only grab alt_titles associated with a franchise (ignoring series titles)
        String titlesSql = "SELECT * FROM alt_title WHERE franchise_id IS NOT NULL";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(franchiseSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("franchise_id");
                    Franchise franchise = new Franchise(
                            id,
                            rs.getString("franchise_name"),
                            new ArrayList<>(),
                            false
                    );
                    franchiseMap.put(id, franchise);
                }
            }

            if (!franchiseMap.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(titlesSql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int franchiseId = rs.getInt("franchise_id");
                        Franchise franchise = franchiseMap.get(franchiseId);

                        if (franchise != null) {
                            franchise.getAltTitles().add(new AltTitle(
                                    rs.getInt("alt_title_id"),
                                    rs.getString("title"),
                                    false
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching franchise " + franchiseMap.size());
        }
        return new ArrayList<>(franchiseMap.values());
    }
}