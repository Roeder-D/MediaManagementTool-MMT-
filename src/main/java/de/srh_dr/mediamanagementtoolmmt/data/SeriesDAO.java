package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Series;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeriesDAO {
    private static final Logger LOGGER = Logger.getLogger(SeriesDAO.class.getName());
    private static final SeriesDAO INSTANCE = new SeriesDAO();

    public static SeriesDAO getInstance(){
        return INSTANCE;
    }

    private SeriesDAO(){}

    // HELPER
    public void save(Series series) {
        if (series.isNewItem()) {
            create(series);
        } else {
            update(series);
        }
    }

    // CREATE
    private void create(Series series) {
        String sql = "INSERT INTO series (series_name, number_of_titles, start_year) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Prevents incomplete data from being inserted

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, series.getName());
                stmt.setInt(2, series.getNumberOfTitles());
                stmt.setInt(3, series.getStartYear());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        series.setId(rs.getInt(1));
                    }
                }

                // Insert new AltTitles
                saveAltTitles(conn, series.getId(), series.getAltTitles());

                conn.commit();
                series.clearChangeTracking();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Failed to insert new series: " + e.getMessage(), e);
        }
    }

    private void saveAltTitles(Connection conn, int seriesId, List<AltTitle> titles) throws SQLException {
        String sql = "INSERT INTO alt_title (title, series_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (AltTitle title : titles) {
                stmt.setString(1, title.getTitle());
                stmt.setInt(2, seriesId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // UPDATE
    private void update(Series series) {
        String seriesSql = "UPDATE series SET series_name = ?, number_of_titles = ?, start_year = ? WHERE series_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); //Prevents incomplete data from being inserted

            try {
                // Refresh core data if isDirty
                if (series.isDirty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(seriesSql)) {
                        stmt.setString(1, series.getName());
                        stmt.setInt(2, series.getNumberOfTitles());
                        stmt.setInt(3, series.getStartYear());
                        stmt.setInt(4, series.getId());
                        stmt.executeUpdate();
                    }
                }

                // Synchronizing AltTitles
                if (series.listChanged()) {
                    syncAltTitles(conn, series);
                }

                conn.commit();
                series.clearChangeTracking();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Failed to update new Series: " + e.getMessage());
        }
    }

    private void syncAltTitles(Connection conn, Series series) throws SQLException {
        // Delete old data
        if (!series.getAltTitlesToRemove().isEmpty()) {
            String deleteSql = "DELETE FROM alt_title WHERE alt_title_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                for (AltTitle title : series.getAltTitlesToRemove()) {
                    stmt.setInt(1, title.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        // Insert new data
        if (!series.getAltTitlesToAdd().isEmpty()) {
            String insertSql = "INSERT INTO alt_title (title, series_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (AltTitle title : series.getAltTitlesToAdd()) {
                    stmt.setString(1, title.getTitle());
                    stmt.setInt(2, series.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM series WHERE series_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                LOGGER.log(Level.WARNING,"Cannot delete series: " + e.getMessage(), e);
            } else {
                LOGGER.log(Level.SEVERE,"Failed to delete series: " + e.getMessage(), e);
            }
            return false;
        }
    }

    // READ (findById)
    public Series findById(int id) {
        String seriesSql = "SELECT * FROM series WHERE series_id = ?";
        String titlesSql = "SELECT * FROM alt_title WHERE series_id = ?";
        Series series = null;

        try (Connection conn = DBConnection.getConnection()) {
            // Load series
            try (PreparedStatement stmt = conn.prepareStatement(seriesSql)) {
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

                    series = new Series(
                            false,
                            rs.getInt("series_id"),
                            rs.getString("series_name"),
                            rs.getInt("number_of_titles"),
                            rs.getInt("start_year"),
                            titles
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING,"Failed to find series: " + e.getMessage(), e);
        }
        return series;
    }

    // READ all
    public List<Series> findAll() {
        Map<Integer,Series> seriesMap = new LinkedHashMap<>();

        String seriesSql = "SELECT * FROM series";
        String titlesSql = "SELECT * FROM alt_title";

        try(Connection conn = DBConnection.getConnection()){
            try(PreparedStatement stmt = conn.prepareStatement(seriesSql);
                ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    int id = rs.getInt("series_id");
                    Series series = new Series(
                            false,
                            id,
                            rs.getString("series_name"),
                            rs.getInt("number_of_titles"),
                            rs.getInt("start_year"),
                            new ArrayList<>()
                    );
                    seriesMap.put(id, series);
                }
            }

            if(!seriesMap.isEmpty()){
                try(PreparedStatement stmt = conn.prepareStatement(titlesSql);
                    ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        int seriesId = rs.getInt("series_id");
                        Series series = seriesMap.get(seriesId);

                        if(series != null){
                            series.getAltTitles().add(new AltTitle(
                                    rs.getInt("alt_title_id"),
                                    rs.getString("title"),
                                    false
                            ));
                        }
                    }
                }
            }
        }catch(SQLException e){
            LOGGER.log(Level.WARNING,"Failed to find all series:  " + e.getMessage(), e);
        }
        return new ArrayList<>(seriesMap.values());
    }
}