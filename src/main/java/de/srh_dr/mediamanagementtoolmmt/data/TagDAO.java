package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Tag;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TagDAO extends AbstractDAO<Tag> {
    @Override protected String getTableName() { return "tag"; }
    @Override protected String getIdColumnName() { return "tag_id"; }
    @Override protected String getValueColumnName() { return "tag"; }

    @Override
    protected Tag mapResultSet(ResultSet rs) throws SQLException {
        return new Tag(
                rs.getInt("tag_id"),
                rs.getString("tag"),
                false
        );
    }
}