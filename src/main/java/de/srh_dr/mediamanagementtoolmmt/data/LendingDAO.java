package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Lendee;
import de.srh_dr.mediamanagementtoolmmt.model.Lending;
import de.srh_dr.mediamanagementtoolmmt.services.MediaService;
import de.srh_dr.mediamanagementtoolmmt.viewmodel.LendingDashboardItem;
import de.srh_dr.mediamanagementtoolmmt.model.Media;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LendingDAO {
    private static final Logger LOGGER =  Logger.getLogger(LendingDAO.class.getName());
    private static final LendingDAO INSTANCE = new LendingDAO();

    public static LendingDAO getInstance(){
        return INSTANCE;
    }

    private LendingDAO(){}

    // HELPER
    public void save(Lending lending){
        if(lending.isNewItem()){
            create(lending);
        }else{
            update(lending);
        }
    }

    // CREATE
    private void create(Lending lending){
        String sql = "INSERT INTO lending (media_id, lendee_id, note, borrow_date, return_date) VALUES (?, ?, ?, ?, ?)";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, lending.getMedia().getId());
            stmt.setInt(2, lending.getLendee().getId());
            stmt.setString(3, lending.getNote());
            stmt.setDate(4, Date.valueOf(lending.getBorrowDate()));
            stmt.setDate(5, lending.getReturnDate() != null ? Date.valueOf(lending.getReturnDate()) : null);

            stmt.executeUpdate();

            try(ResultSet rs = stmt.getGeneratedKeys()){
                if(rs.next()){
                    lending.setId(rs.getInt(1));
                }
            }
            lending.clearChangeTracking();
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error creating lending " + lending.getLendee().getId(), e);
        }
    }

    // UPDATE
    private void update(Lending lending){
        if(lending.isNewItem()){return;}
        if(!lending.isDirty()){return;}

        String sql = "UPDATE lending SET note = ?, return_date = ? WHERE lending_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, lending.getNote());
            stmt.setDate(2, lending.getReturnDate() != null ? Date.valueOf(lending.getReturnDate()) : null);
            stmt.setInt(3, lending.getId());

            stmt.executeUpdate();
            lending.clearChangeTracking();
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error updating lending " + lending.getLendee().getId(), e);
        }
    }

    //DELETE
    public void deleteById(int id){
        String sql = "DELETE FROM lending WHERE lending_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);

            stmt.executeUpdate();
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error deleting lending " + id, e);
        }
    }

    //READ
    public Lending findById(int id){
        String sql = "SELECT * FROM lending WHERE lending_id = ?";
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, id);
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    Media media = MediaService.getInstance().getMediaById(rs.getInt("media_id"));
                    Lendee lendee = LendeeDAO.getInstance().findById(rs.getInt("lendee_id"));

                    Date sqlReturnDate = rs.getDate("return_date");

                    Lending lending =  new Lending(
                            rs.getInt("lending_id"),
                            media,
                            lendee,
                            rs.getDate("borrow_date").toLocalDate(),
                            sqlReturnDate != null ? sqlReturnDate.toLocalDate() : null,
                            false
                    );
                    lending.setNote(rs.getString("note"));
                    return lending;
                }
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error fetching lending " + id, e);
        }
        return null;
    }

    public int findActiveIdByMediaId(int mediaId){
        String sql = "SELECT lending_id FROM lending WHERE media_id = ? AND return_date IS NULL LIMIT 1";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, mediaId);
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getInt("lending_id");
                }
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"SQLException: " + e.getMessage(), e);
        }
        return 0;
    }

    //DASHBOARD
    public List<LendingDashboardItem> getLendingDashboard(){
        String sql = "SELECT * FROM v_lending_dashboard";
        List<LendingDashboardItem> lendingDashboardItems = new ArrayList<>();
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while(rs.next()){
                int lendingId = rs.getInt("lending_id");
                String mediaTitle = rs.getString("media_title");
                String lendeeInfo = rs.getString("lendee_info");
                String borrowedOn = rs.getString("lent_on");
                String returnedOn = rs.getString("returned_on");
                String status = rs.getString("lending_status");

                lendeeInfo = lendeeInfo.replace("()", "");
                LendingDashboardItem li = new LendingDashboardItem(lendingId,mediaTitle, lendeeInfo, borrowedOn, returnedOn, status);
                lendingDashboardItems.add(li);

            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"SQLException: " + e.getMessage(), e);
        }
        return lendingDashboardItems;
    }
}
