package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Franchise;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FranchiseDAO extends AbstractDAO<Franchise>{
    private static final Logger LOGGER = Logger.getLogger(FranchiseDAO.class.getName());
    private final static FranchiseDAO INSTANCE = new FranchiseDAO();

    public static FranchiseDAO getInstance(){
        return INSTANCE;
    }

    private FranchiseDAO(){}
    @Override
    protected String getTableName() {
        return "franchise";
    }

    @Override
    protected String getIdColumnName() {
        return "franchise_id";
    }

    @Override
    protected String getValueColumnName() {
        return "franchise_name";
    }

    @Override
    protected Franchise mapResultSet(ResultSet rs) throws SQLException {
        return new Franchise(
                rs.getInt("franchise_id"),
                rs.getString("franchise_name"),
                List.of(),
                false
        );
    }


    // HELPER
    public void save(Franchise franchise) {
        if (franchise.isNewItem()) {
            create(franchise);
        } else {
            update(franchise);
        }
    }

    // CREATE
    private void create(Franchise franchise) {
        String sql = "INSERT INTO franchise (franchise_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Prevents incomplete data from being inserted

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, franchise.getName());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        franchise.setId(rs.getInt(1));
                    }
                }

                saveAltTitles(conn, franchise.getId(), franchise.getAltTitles());

                conn.commit();
                franchise.clearChangeTracking();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error saving franchise " + franchise.getName(), e);
        }
    }

    private void saveAltTitles(Connection conn, int franchiseId, List<AltTitle> titles) throws SQLException {
        String sql = "INSERT INTO alt_title (title, franchise_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (AltTitle title : titles) {
                stmt.setString(1, title.getTitle());
                stmt.setInt(2, franchiseId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // UPDATE
    private void update(Franchise franchise) {
        String franchiseSql = "UPDATE franchise SET franchise_name = ? WHERE franchise_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Prevents incomplete data from being inserted

            try {
                // Refresh core data if isDirty
                if (franchise.isDirty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(franchiseSql)) {
                        stmt.setString(1, franchise.getName());
                        stmt.setInt(2, franchise.getId());
                        stmt.executeUpdate();
                    }
                }

                // Synchronizing AltTitles
                if (franchise.listChanged()) {
                    syncAltTitles(conn, franchise);
                }

                conn.commit();
                franchise.clearChangeTracking();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error updating franchise " + franchise.getName(), e);
        }
    }

    private void syncAltTitles(Connection conn, Franchise franchise) throws SQLException {
        // Delete old data
        if (!franchise.getAltTitlesToRemove().isEmpty()) {
            String deleteSql = "DELETE FROM alt_title WHERE alt_title_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                for (AltTitle title : franchise.getAltTitlesToRemove()) {
                    stmt.setInt(1, title.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        // Insert new data
        if (!franchise.getAltTitlesToAdd().isEmpty()) {
            String insertSql = "INSERT INTO alt_title (title, franchise_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (AltTitle title : franchise.getAltTitlesToAdd()) {
                    stmt.setString(1, title.getTitle());
                    stmt.setInt(2, franchise.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
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
            if (e.getErrorCode() == 1451) {
                LOGGER.log(Level.SEVERE,"Couldn't delete Franchise: " + e.getMessage(), e);
            } else {
                LOGGER.log(Level.SEVERE,"Error deleting franchise " + id + ": " + e.getMessage(), e) ;
            }
            return false;
        }
    }

    // READ (findById)
    @Override
    public Franchise findById(int id) {
        Franchise franchise = super.findById(id);

        if(franchise != null){
            List<AltTitle> altTitles = fetchAltTitlesForFranchise(id);
            franchise.setAltTitles(altTitles);
        }
        return franchise;
    }

    // READ all
    @Override
    public List<Franchise> findAll(){
        Map<Integer, Franchise> franchiseMap = new LinkedHashMap<>();

        String titlesSql = "SELECT * FROM alt_title WHERE franchise_id IS NOT NULL";

        List<Franchise> franchises = super.findAll();

        for (Franchise franchise : franchises) {
            franchiseMap.put(franchise.getId(), franchise);
        }

        try (Connection conn = DBConnection.getConnection()){
            try (PreparedStatement stmt = conn.prepareStatement(titlesSql);
            ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    int franchiseId = rs.getInt("franchise_id");
                    Franchise franchise = franchiseMap.get(franchiseId);

                    if (franchise != null) {
                        franchise.getAltTitles().add(new AltTitle(
                                rs.getInt("alt_title_id"),
                                rs.getString("title"),
                                false
                        ));
                    }
                }

            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE,"Error fetching franchise " + franchiseMap.size(), e);
        }
        return new ArrayList<>(franchiseMap.values());
    }

    public void saveFranchisesForMedia(int mediaId, List<Franchise> franchises, Connection conn) throws SQLException {
        String sql = "INSERT INTO media_franchise (media_id, franchise_id) VALUES (?, ?)";
        saveJunctionBatch(mediaId, franchises, sql, conn);
    }

    public void deleteFranchisesForMedia(int mediaId, List<Franchise> franchisesToDelete, Connection conn) throws SQLException {
        String sql = "DELETE FROM media_franchise WHERE media_id = ? AND franchise_id = ?";
        deleteJunctionBatch(mediaId, franchisesToDelete, sql, conn);
    }

    public List<Franchise> fetchByMediaId(int mediaId){
        String sql = "SELECT f.*, at.alt_title_id, at.title AS alt_title FROM media_franchise mf " +
                "JOIN franchise f ON mf.franchise_id = f.franchise_id " +
                "LEFT JOIN alt_title at ON f.franchise_id = at.franchise_id " +
                "WHERE mf.media_id = ?";

        Map<Integer, Franchise> franchiseMap = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, mediaId);

            try(ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    int currentFranchiseId = rs.getInt("franchise_id");
                    Franchise currentFranchise;

                    if(franchiseMap.containsKey(currentFranchiseId)){
                        currentFranchise = franchiseMap.get(currentFranchiseId);
                    }else{
                        currentFranchise = new Franchise(
                                rs.getInt("franchise_id"),
                                rs.getString("franchise_name"),
                                new ArrayList<>(),
                        false
                        );
                        franchiseMap.put(currentFranchiseId, currentFranchise);
                    }

                    if(rs.getString("alt_title_id") != null) {
                        currentFranchise.addAltTitle(new AltTitle(rs.getInt("alt_title_id"), rs.getString("alt_title"), false));
                        currentFranchise.clearChangeTracking();
                    }
                }
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE,"Error fetching franchise " + franchiseMap.size(), e);
        }
        return new ArrayList<>(franchiseMap.values());
    }

    //helper
    private List<AltTitle> fetchAltTitlesForFranchise(int franchiseId){
        List<AltTitle> altTitles = new ArrayList<>();
        String sql = "SELECT * FROM alt_title WHERE franchise_id = ?";
            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, franchiseId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        AltTitle altTitle = new AltTitle(
                                rs.getInt("alt_title_id"),
                                rs.getString("title"),
                                false
                        );
                        altTitles.add(altTitle);
                    }
                }
            }
        catch (SQLException e){
            LOGGER.log(Level.SEVERE,"Error fetching alt titles " + franchiseId, e);
        }
        return altTitles;
    }
}