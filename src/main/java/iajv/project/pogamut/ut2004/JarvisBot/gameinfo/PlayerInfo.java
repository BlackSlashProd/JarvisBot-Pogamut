package iajv.project.pogamut.ut2004.JarvisBot.gameinfo;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import iajv.project.pogamut.ut2004.JarvisBot.utils.Computing;

/**
 * Needed information to know about players.
 */
public class PlayerInfo {

    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************
    
    /** Player's name */
    private String name;
    /** Unreal Id **/
    private UnrealId id;
    /** Number of frags **/
    private int frags;
    /** Location where we know/guess the enemy is */
    private Location currentPosition;
    /** NavPoint where we know/guess the enemy is */
    private NavPoint navPointPosition;
    /** Game time where we saw for the last time the enemy */
    private double lastEncounter;
    /** **/
    
    // *************************************************************************
    //                               METHODS
    // *************************************************************************

     /**
     *
     */
    public PlayerInfo () {
        this.name = "";
        this.id = null;
        navPointPosition = null;
        currentPosition = null;
        lastEncounter = -1;
        frags = 0;
    }   
    
    /**
     *
     * @param plID
     * @param plName
     */
    public PlayerInfo (UnrealId plID, String plName) {
        this.name = plName;
        this.id = plID;
        navPointPosition = null;
        currentPosition = null;
        lastEncounter = -1;
    }
    
    /**
     * It resets all the information that we are guessing about the bot, to its
     * default values (except for the player's name).
     * Use it when the enemy dies.
     */
    public void reset () {
        navPointPosition = null;
        currentPosition = null;
        lastEncounter = -1;
    }
    
    /**
     * Updates the location where we think/know the enemy is.
     * @param enemyLocation Location where we think the enemy is.
     * @param clockTime Game time when we guessed/saw where the enemy was.
     */
    public void updateEnemyLocation (final Location enemyLocation, final double clockTime) {
        lastEncounter = clockTime;
        currentPosition = enemyLocation;
    }

    /**
     * Updates the NavPoint where we think/know the enemy is.
     * @param enemyNavPoint NavPoint where we think the enemy is.
     * @param clockTime Game time when we guessed/saw where the enemy was.
     */
    public void updateEnemyNavPoint (final NavPoint enemyNavPoint, final double clockTime) {
        lastEncounter = clockTime;
        navPointPosition = enemyNavPoint;
        currentPosition = enemyNavPoint.getLocation();
    }

    /**
     * It returns the time when we saw for the last time the enemy.
     * @return Game time when we saw for the last time the enemy. -1 if we haven't
     * still seen the enemy.
     */
    public double getLastTimeMet () {
        return lastEncounter;
    }

    /**
     * It returns the location where we saw for the last time the enemy.
     * @return Location where we saw for the last time the enemy. Null if we haven't
     * still seen the enemy.
     */
    public Location getLastKnownLocation () {
        return currentPosition;
    }

    /**
     * It returns the NavPoint where we saw for the last time the enemy.
     * @return NavPoint where we saw for the last time the enemy. Null if we haven't
     * still seen the enemy.
     */
    public NavPoint getLastKnownNavPoint () {
        if (navPointPosition == null && currentPosition != null) {
            return Computing.getClosestPathNodeToLocation (currentPosition);
        }
        return navPointPosition;
    }

    /**
     * Get the name of the player.
     * @return The player's name.
     */
    public String getName () {
        return name;
    }
    
    /**
     * Get the id of the player.
     * @return The player's id.
     */
    public UnrealId getID () {
        return id;
    }
    
    /**
     * Get the number of frags of the player.
     * @return The player's frags number.
     */
    public int getNBFrags () {
        return frags;
    }
    
    /**
     * Set the number of frags of the player.
     */
    public void setNBFrags (int nb) {
        this.frags = nb;
    }
}
