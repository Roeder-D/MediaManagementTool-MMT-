package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Lendee;
import de.srh_dr.mediamanagementtoolmmt.model.Lending;
import de.srh_dr.mediamanagementtoolmmt.model.LendingDashboardItem;
import de.srh_dr.mediamanagementtoolmmt.model.Media;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class LendingDAO {

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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
                    Media media = new MediaDAO().read(rs.getInt("media_id"));
                    Lendee lendee = new LendeeDAO().findById(rs.getInt("lendee_id"));

                    Date sqlReturnDate = rs.getDate("return_date");

                    return new Lending(
                            rs.getInt("lending_id"),
                            media,
                            lendee,
                            rs.getDate("borrow_date").toLocalDate(),
                            sqlReturnDate != null ? sqlReturnDate.toLocalDate() : null,
                            false
                    );
                }
            }
        }catch(SQLException e){
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    LanguageManager.getString("error.failedToLoad") + " :" + e.getMessage());
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
            System.err.println("SQLException: " + e.getMessage());
            AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), LanguageManager.getString("error.failedToLoad") + " :" + e.getMessage());
        }
        return 0;
    }

    //DASHBOARD
    public List<LendingDashboardItem> getLendingDashboard(){
        String sql = "SELECT * FROM v_lending_dashboard";
        List<LendingDashboardItem> lendingDashboardItems = new ArrayList<LendingDashboardItem>();
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while(rs.next()){
                int lendingId = rs.getInt("lending_id");
                int mediaId = rs.getInt("media_id");
                String mediaTitle = rs.getString("media_title");
                String lendeeInfo = rs.getString("lendee_info");
                String borrowedOn = rs.getString("lent_on");
                String returnedOn = rs.getString("returned_on");
                String status = rs.getString("lending_status");

                LendingDashboardItem li = new LendingDashboardItem(lendingId, mediaId,mediaTitle, lendeeInfo, borrowedOn, returnedOn, status);
                lendingDashboardItems.add(li);

            }
        }catch(SQLException e){
            AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), LanguageManager.getString("error.failedToLoad") + " " + e.getMessage());
        }
        return lendingDashboardItems;
    }
}
