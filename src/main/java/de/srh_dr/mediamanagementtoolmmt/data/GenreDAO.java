package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GenreDAO extends AbstractDAO<Genre> {
    @Override protected String getTableName() { return "genre"; }
    @Override protected String getIdColumnName() { return "genre_id"; }
    @Override protected String getValueColumnName() { return "genre_name"; }

    @Override
    protected Genre mapResultSet(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre_name"),
                false
        );
    }
}