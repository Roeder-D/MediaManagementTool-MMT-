package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Language;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.sql.*;

public class LanguageDAO extends AbstractDAO<Language> {
    @Override protected String getTableName() { return "language"; }
    @Override protected String getIdColumnName() { return "language_id"; }
    @Override protected String getValueColumnName() { return "language"; }

    @Override
    protected Language mapResultSet(ResultSet rs) throws SQLException {
        return new Language(
                rs.getInt("language_id"),
                rs.getString("language"),
                false
        );
    }

    // HELPER
    public void save(Language language) throws SQLException {
        if (language.isNewItem()) {
            create(language);
        } else {
            update(language);
        }
    }

    // CREATE
    private void create(Language language) throws SQLException {
        String sql = "INSERT INTO language (language) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, language.getLanguage());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    language.setId(rs.getInt(1));
                }
            }

            language.clearChangeTracking();

        }catch(SQLException e){
            System.err.println("Error saving language " + language.getLanguage());
        }
    }

    // UPDATE
    private void update(Language language) throws SQLException {
        if (language.isNewItem()) return;
        if (!language.isDirty()) return;

        String sql = "UPDATE tag SET language = ? WHERE language_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, language.getLanguage());
            stmt.setInt(2, language.getId());

            stmt.executeUpdate();
            language.clearChangeTracking();

        } catch (SQLException e) {
            System.err.println("Error updating language " + language.getLanguage());
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM language WHERE language_id = ?";
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