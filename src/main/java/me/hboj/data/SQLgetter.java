package me.hboj.data;

import me.hboj.CoinWallet;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class SQLgetter {
    CoinWallet plugin;

    public SQLgetter(CoinWallet plugin){
        this.plugin=plugin;
        createTable();
    }

    private void createTable() {

        PreparedStatement statement;
        try{
            statement = plugin.sql.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Diamonds" +
                    "(NAME VARCHAR(255), UUID VARCHAR(255),DIAMONDS INT ,PRIMARY KEY (UUID))");
            statement.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void setDiamond(OfflinePlayer player, int value) {
        try{
            PreparedStatement ps = plugin.sql.getConnection().prepareStatement(
                    "INSERT INTO Diamonds (UUID, NAME, DIAMONDS) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE NAME = VALUES(NAME), DIAMONDS = VALUES(DIAMONDS)"
            );
            ps.setString(1 ,  player.getUniqueId().toString());
            ps.setString(2 ,  player.getName());
            ps.setInt(3 , value);
            ps.executeUpdate();

        }catch(SQLException e ){
            e.printStackTrace();
        }
    }

    public int getDiamond(UUID uuid) {
        try{
            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT DIAMONDS FROM Diamonds WHERE UUID=?");
            ps.setString(1 , uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int result = rs.getInt("DIAMONDS");
                rs.close();
                return result;
            }
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return -1;
    }


    public int getTotalDiamonds(){
        try{
            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT SUM(DIAMONDS) as totaldiamonds FROM Diamonds");
            ResultSet rs = ps.executeQuery();
            int result = 0;
            if(rs.next()){
                result = rs.getInt("totaldiamonds");
                return result;
            }
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public HashMap<String, Integer> getDiamondMap(int page, int playersPerPage) {
        HashMap<String, Integer> diamondsMap = new HashMap<>();

        try {
            int offset = (page - 1) * playersPerPage;

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement(
                    "SELECT * FROM Diamonds ORDER BY Diamonds.DIAMONDS DESC LIMIT ? OFFSET ?"
            );

            ps.setInt(1, playersPerPage);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();

            int diamonds;
            while (rs.next()) {
                diamonds = rs.getInt("DIAMONDS");
                diamondsMap.put(rs.getString("NAME"), diamonds);
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return diamondsMap;
    }
}


