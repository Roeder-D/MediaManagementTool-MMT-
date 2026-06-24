package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//simple counts are provided by AbstractDAO instead
public class StatisticsDAO {
    private static final  StatisticsDAO INSTANCE = new StatisticsDAO();

    public static StatisticsDAO getInstance(){
        return INSTANCE;
    }

    private StatisticsDAO(){}

    // READ
    public Map<String, Integer> getCollectionStatistics() {
        Map<String, Integer> statistics = new HashMap<>();
        String sql = "Select total_titles, available_titles, lent_titles, lost_titles FROM v_collection_statistics LIMIT 1";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){

            if(rs.next()){
                statistics.put("total", rs.getInt("total_titles"));
                statistics.put("available", rs.getInt("available_titles"));
                statistics.put("lent", rs.getInt("lent_titles"));
                statistics.put("lost", rs.getInt("lost_titles"));
            }
        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
        return statistics;
    }

    public Map<String, Integer> getMediaTypeDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT media_type, total_count FROM v_media_type_distribution";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){

            while (rs.next()){
                String mediaType = rs.getString("media_type");
                int totalCount = rs.getInt("total_count");

                if(mediaType != null && totalCount > 0){
                    distribution.put(mediaType, totalCount);
                }
            }

        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
        return distribution;
    }
}
