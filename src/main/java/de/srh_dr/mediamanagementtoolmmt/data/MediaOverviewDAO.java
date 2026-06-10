package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.MediaOverview;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaOverviewDAO {
    public List<MediaOverview> getMediaOverview() throws SQLException {
        List<MediaOverview> overviewList = new ArrayList<>();
        String sql = "SELECT media_id, title, release_date, publisher_name, status, type_name, " + // <-- Added comma here
                " (SELECT GROUP_CONCAT(t.tag SEPARATOR ',') " + // <-- Fixed SEPARATOR and added closing )
                " FROM media_tag mt " +
                " JOIN tag t on mt.tag_id = t.tag_id " +
                " WHERE mt.media_id = v.media_id) AS tags " +
                "FROM v_media_overview v";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while(rs.next()) {
                int mediaId = rs.getInt("media_id");
                String title = rs.getString("title");
                String type = rs.getString("type_name");
                String tagsRaw = rs.getString("tags");
                String tags = (tagsRaw != null) ? tagsRaw : "";

                java.sql.Date sqlDate = rs.getDate("release_date");
                String releaseDate = (sqlDate != null) ? sqlDate.toString() : "Unknown";

                String publisher = rs.getString("publisher_name");
                if(publisher == null) publisher = "N/A";

                String status = rs.getString("status");

                overviewList.add(new MediaOverview(mediaId, type, title, releaseDate, publisher, status, tags));
            }
        }
        return overviewList;
    }
}
