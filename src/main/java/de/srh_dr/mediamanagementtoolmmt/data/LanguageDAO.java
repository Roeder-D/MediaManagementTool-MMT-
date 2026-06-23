package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Language;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LanguageDAO extends AbstractDAO<Language> {
    private static final Logger LOGGER = Logger.getLogger(LanguageDAO.class.getName());
    private static final LanguageDAO INSTANCE = new LanguageDAO();

    public static LanguageDAO getInstance(){
        return INSTANCE;
    }

    private LanguageDAO(){}

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

    // READ
    public Language findByName(String name) {
        String sql = "SELECT * FROM language WHERE language = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING,"Error looking up language by name: " + name, e);
        }
        return null;
    }

    // CREATE
    private void create(Language language){
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
            LOGGER.log(Level.SEVERE,"Error saving language " + language.getLanguage(), e);
        }
    }

    // UPDATE
    private void update(Language language){
        if (language.isNewItem()) return;
        if (!language.isDirty()) return;

        String sql = "UPDATE language SET language = ? WHERE language_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, language.getLanguage());
            stmt.setInt(2, language.getId());

            stmt.executeUpdate();
            language.clearChangeTracking();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error updating language " + language.getLanguage(), e);
        }
    }

    //Media relations
    public void saveLanguagesForMedia(int mediaId, List<Language> languages, Connection conn) throws SQLException {
        String sql = "INSERT INTO media_language (media_id, language_id) VALUES (?, ?)";
        saveJunctionBatch(mediaId, languages, sql, conn);
    }

    public void deleteLanguagesForMedia(int mediaId, List<Language> languagesToDelete, Connection conn) throws SQLException {
        String sql = "DELETE FROM media_language WHERE media_id = ? AND language_id = ?";
        deleteJunctionBatch(mediaId, languagesToDelete, sql, conn);
    }

    public List<Language> fetchByMediaId(int mediaId) {
        return fetchViaJunction(mediaId, "media_language", "language_id");
    }
}