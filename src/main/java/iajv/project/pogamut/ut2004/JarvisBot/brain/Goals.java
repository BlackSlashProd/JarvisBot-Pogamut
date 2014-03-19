package iajv.project.pogamut.ut2004.JarvisBot.brain;

/**
 * What the bot should or need to do.
 */
public class Goals {
    
    public boolean needChangeWeapon = false;
    
    public boolean needHealth = false;
    
    public boolean needQuicklyHealth = false;
    
    public boolean needArmor = true;
    
    public Goals() {}

    void resetPrimaryGoals() {
        needChangeWeapon = false;
        needHealth = false;
        needQuicklyHealth = false;
        needArmor = true;
    }
    
}
