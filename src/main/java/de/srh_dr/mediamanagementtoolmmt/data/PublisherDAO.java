package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Publisher;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PublisherDAO extends AbstractDAO<Publisher> {
    @Override protected String getTableName() { return "publisher"; }
    @Override protected String getIdColumnName() { return "publisher_id"; }
    @Override protected String getValueColumnName() { return "publisher_name"; }

    @Override
    protected Publisher mapResultSet(ResultSet rs) throws SQLException {
        return new Publisher(
                rs.getInt("publisher_id"),
                rs.getString("publisher_name"),
                false
        );
    }
}
