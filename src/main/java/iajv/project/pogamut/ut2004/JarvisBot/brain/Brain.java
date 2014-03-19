package iajv.project.pogamut.ut2004.JarvisBot.brain;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.ItemDescriptors;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.WeaponDescriptor;
import iajv.project.pogamut.ut2004.JarvisBot.configs.Rules;
import iajv.project.pogamut.ut2004.JarvisBot.configs.UTWeapons;
import iajv.project.pogamut.ut2004.JarvisBot.gameinfo.PlayerInfo;

/**
 * The brain of the bot.
 */
public class Brain {
    
    private Memoria memoria;
    private Goals goals;
    
    private AgentInfo info;
    private CompleteBotCommandsWrapper body;
    
    public Brain(AgentInfo info, CompleteBotCommandsWrapper body) {
        PlayerInfo me;
        me = new PlayerInfo(info.getId(), info.getName());
        this.memoria = new Memoria(me);
        this.goals = new Goals();
        this.info = info;
        this.body = body;
    }
    
    public void reset() {
        memoria.resetPlayerInfo();
        goals.resetPrimaryGoals();
    }
    
    public boolean shouldEngage(Players players) {
        if(this.info.getCurrentAmmo()<10)
            return false;
        Player nearest = players.getNearestVisibleEnemy();
        if(nearest!=null) {
            double dist = info.getLocation().getDistance(nearest.getLocation());
            if(dist<Rules.AVERAGE) return true;
        }
        return !goals.needQuicklyHealth;
    }
    
    public boolean needHealth() {
        return (goals.needHealth||goals.needQuicklyHealth);
    }
    
    public boolean needChangeWeapon() {
        return goals.needChangeWeapon;
    }
    
    private void mindNeedHealth() {
        if(info.getHealth()>Rules.HEALTH_WARNING) {
            goals.needHealth = false;
            goals.needQuicklyHealth = false;
        }        
        else if(info.getHealth()>Rules.HEALTH_CRITIC) {
            goals.needHealth = true;
            goals.needQuicklyHealth = false;               
        }
        else {
            goals.needHealth = true;
            goals.needQuicklyHealth = true;                  
        }
    }
    
    private void mindNeedChangeWeapon() {
        goals.needChangeWeapon = true;
    }
    
    public void updatePlayerLocation(final Player targetPlayer, final double clockTime) {
        memoria.updatePlayerInfo(targetPlayer, clockTime);
    }
    
    public Location getLastKnowPlayerLocation(final Player targetPlayer, final double clockTime) {
        PlayerInfo pl = memoria.getLastKnowPlayerLocation(targetPlayer);
        if(pl!=null && clockTime-pl.getLastTimeMet()<30) {
            return pl.getLastKnownLocation();
        }
        return null;
    }
    
    public void updateWeapon(Weapon weapon) {
        body.getCommunication().sendGlobalTextMessage("[SUB-STATE] - SwitchToBestWeapon");
        goals.needChangeWeapon = false;
        memoria.updateCurrentWeapon(UTWeapons.estimateWeaponAdvantage(weapon.getType(), null));
    }
    
    public void playerKilled (UnrealId killerId, UnrealId deadId) {
        memoria.resetPlayerInfo(deadId);
        if(killerId.equals(memoria.getMe().getID())){
            memoria.getMe().setNBFrags(memoria.getMe().getNBFrags()+1);
        }
    }
    
    public void takeDamage(int takenDamage) {
        mindNeedHealth();
    }
    
    public void itemPickedUp(ItemDescriptors descriptors, ItemType itemType) {
    	ItemType.Category itemCategory = itemType.getCategory();
    	switch (itemCategory) {
            case ADRENALINE: 
    		//AdrenalineDescriptor descAdrenaline = (AdrenalineDescriptor) descriptors.getDescriptor(itemType);
    		break;
            case AMMO:
    		//AmmoDescriptor descAmmo = (AmmoDescriptor) descriptors.getDescriptor(itemType);
    		break;
            case ARMOR:
    		//ArmorDescriptor descArmor = (ArmorDescriptor) descriptors.getDescriptor(itemType);   		    		
    		break;
            case HEALTH:
    		//HealthDescriptor descHealth = (HealthDescriptor) descriptors.getDescriptor(itemType);
                mindNeedHealth();
    		break;
            case SHIELD:
    		//ShieldDescriptor descShield = (ShieldDescriptor) descriptors.getDescriptor(itemType);
    		break;
            case WEAPON:
    		WeaponDescriptor descWeapon = (WeaponDescriptor) descriptors.getDescriptor(itemType);
                if(memoria.getCurrentWeaponProfit()<UTWeapons.estimateWeaponAdvantage(itemType, null))
                    mindNeedChangeWeapon();
    		break;
            case OTHER:
    		//OtherDescriptor descOther = (OtherDescriptor) descriptors.getDescriptor(itemType);
    		break;
    	}
    }
}
