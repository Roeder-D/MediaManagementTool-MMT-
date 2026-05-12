package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Language;

import java.sql.ResultSet;
import java.sql.SQLException;

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
}