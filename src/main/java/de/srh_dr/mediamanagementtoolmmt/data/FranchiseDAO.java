package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Franchise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FranchiseDAO extends AbstractDAO<Franchise> {
    @Override
    protected String getTableName() { return "franchise"; }
    @Override
    protected String getIdColumnName() { return "franchise_id"; }
    @Override
    protected String getValueColumnName() { return "franchise_name"; }

    @Override
    protected Franchise mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("franchise_id");

        Connection conn = rs.getStatement().getConnection();
        return new Franchise(
                id,
                rs.getString("franchise_name"),
                fetchAltTitles(conn, id),
                false
        );
    }

    private List<AltTitle> fetchAltTitles(Connection conn, int franchiseId) {
        String sql = "SELECT alt_title_id, title FROM alt_title WHERE franchise_id = ?";
        List<AltTitle> titles = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    // HELPER
    public void save(Franchise franchise) throws SQLException {
        if (franchise.isNewItem()) {
            create(franchise);
        } else {
            update(franchise);
        }
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

    // UPDATE
    public void update(Franchise franchise) {
        if (franchise.isNewItem()) return;
        if (!franchise.isDirty()) return;

        String sql = "UPDATE franchise SET franchise_name = ? WHERE franchise_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, franchise.getName());
            stmt.setInt(2, franchise.getId());

            stmt.executeUpdate();
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