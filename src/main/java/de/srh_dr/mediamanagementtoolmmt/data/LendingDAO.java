package de.srh_dr.mediamanagementtoolmmt.data;

import de.srh_dr.mediamanagementtoolmmt.model.Lending;

import java.sql.*;


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
}
