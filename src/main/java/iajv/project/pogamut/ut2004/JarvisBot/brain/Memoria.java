package iajv.project.pogamut.ut2004.JarvisBot.brain;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import iajv.project.pogamut.ut2004.JarvisBot.gameinfo.PlayerInfo;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Memoria {
    private List<PlayerInfo> playerInfo;
    private PlayerInfo me;
    private int currentWeaponProfit = 0;
    
    public Memoria(PlayerInfo bot) {
        playerInfo = new ArrayList<PlayerInfo>();
        me = bot;
    }
    
    public void resetPlayerInfo() {
        currentWeaponProfit = 0;
        for(PlayerInfo pl : playerInfo) {
             pl.reset();
        }       
    }
    
    public void resetPlayerInfo(UnrealId playerID) {
        PlayerInfo pl = findPlayerInfo(playerID);
        if(pl!=null) pl.reset();
    }
    
    public PlayerInfo getMe() {
        return me;
    }
    
    public void updateCurrentWeapon(int profit) {
        this.currentWeaponProfit = profit;
    }
    
    public int getCurrentWeaponProfit() {
        return this.currentWeaponProfit;
    }
    
    public void updatePlayerInfo(final Player targetPlayer, final double clockTime) {
            if(targetPlayer==null) return;
            PlayerInfo pl = findPlayerInfo(targetPlayer.getId());
            if(pl==null) {
                pl = new PlayerInfo(targetPlayer.getId(),targetPlayer.getName());
                playerInfo.add(pl);
            }
            Location enemyLocation = targetPlayer.getLocation();
            if (enemyLocation == null) {
                pl.updateEnemyLocation(null, -1);
            }
            else {
                pl.updateEnemyLocation(enemyLocation, clockTime);
            }
    }
    
    public PlayerInfo getLastKnowPlayerLocation(final Player targetPlayer) {
        return findPlayerInfo(targetPlayer.getId());
    }
    
    private PlayerInfo findPlayerInfo(UnrealId playerId) {
        for(PlayerInfo pl : playerInfo) {
            if(pl.getID().equals(playerId))
                return pl;
        }
        return null;
    }
}
