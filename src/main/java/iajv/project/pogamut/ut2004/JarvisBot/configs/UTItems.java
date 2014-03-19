package iajv.project.pogamut.ut2004.JarvisBot.configs;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;

/**
 * Tools class to estimate items profits.
 */
public class UTItems {
    
    /**
     * Estimate the priority of an item based on our status.
     * @param item Item we want to evaluate.
     * @param info Agent information.
     * @param weaponry Weaponry that we are carrying.
     * @return -1 if the item isn't in the list of items considered or else, a value between
     * 0 and 100 indicating how good the item is.
     */
    public static int estimateItemPriority (final Item item, final AgentInfo info, final Weaponry weaponry) {
        
        ItemType type = item.getType();
        int health = info.getHealth();
        int priority;

        if (type.equals(ItemType.MINI_HEALTH_PACK)) {
            if (health < Rules.HEALTH_WARNING) {
                priority = 90;
            }
            else if (health < Rules.HEALTH_FULL) {
                priority = 65;
            }
            else if (health < Rules.HEALTH_SAFE) {
                priority = 75;
            }
            else {
                priority = 65;
            }
        }
        else if (type.equals(ItemType.HEALTH_PACK)) {
            if (health < Rules.HEALTH_WARNING) {
                priority = 95;
            }
            else if (health < Rules.HEALTH_FULL) {
                priority = 75;
            }
            else {
                priority = 0;
            }
        }
        else if (type.equals(ItemType.SUPER_SHIELD_PACK)) {
            if (health < Rules.HEALTH_WARNING) {
                priority = 99;
            }
            else if (health < Rules.HEALTH_FULL) {
                priority = 99;
            }
            else if (health < Rules.HEALTH_SAFE) {
                priority = 99;
            }
            else {
                priority = 99;
            }
        }
        else if (type.equals(ItemType.SHIELD_PACK)) {
            if (health < Rules.HEALTH_WARNING) {
                priority = 85;
            }
            else if (health < Rules.HEALTH_FULL) {
                priority = 98;
            }
            else if (health < Rules.HEALTH_SAFE) {
                priority = 98;
            }
            else {
                priority = 98;
            }
        }
        else if (type.equals (ItemType.U_DAMAGE_PACK)) {
            if (health < Rules.HEALTH_WARNING) {
                priority = 100;
            }
            else if (health < Rules.HEALTH_FULL) {
                priority = 100;
            }
            else if (health < Rules.HEALTH_SAFE) {
                priority = 100;
            }
            else {
                priority = 100;
            }
        }
        else if (type.equals (ItemType.ADRENALINE_PACK)) {
            if (health < Rules.HEALTH_WARNING) {
                priority = 40;
            }
            else if (health < Rules.HEALTH_FULL) {
                priority = 40;
            }
            else if (health < Rules.HEALTH_SAFE) {
                priority = 40;
            }
            else {
                priority = 40;
            }
        }
        else if (type.getCategory().equals(ItemType.Category.AMMO)) {
            if (health < Rules.HEALTH_WARNING) {
                priority = 20;
            }
            else if (health < Rules.HEALTH_FULL) {
                priority = 20;
            }
            else if (health < Rules.HEALTH_SAFE) {
                priority = 20;
            }
            else {
                priority = 20;
            }
        }
        else if (type.equals (ItemType.BIO_RIFLE)) {
            if (health < Rules.HEALTH_WARNING) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 50;
                }
                else {
                    priority = 25;
                }
            }
            else if (health < Rules.HEALTH_FULL) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 55;
                }
                else {
                    priority = 25;
                }
            }
            else if (health < Rules.HEALTH_SAFE) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
        }
        else if (type.equals (ItemType.LINK_GUN)) {
            if (health < Rules.HEALTH_WARNING) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 55;
                }
                else {
                    priority = 30;
                }
            }
            else if (health < Rules.HEALTH_FULL) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
            else if (health < Rules.HEALTH_SAFE) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 65;
                }
                else {
                    priority = 40;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
        }
        else if (type.equals (ItemType.MINIGUN)) {
            if (health < Rules.HEALTH_WARNING) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 65;
                }
                else {
                    priority = 40;
                }
            }
            else if (health < Rules.HEALTH_FULL) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
            else if (health < Rules.HEALTH_SAFE) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
        }
        else if (type.equals (ItemType.FLAK_CANNON)) {
            if (health < Rules.HEALTH_WARNING) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
            else if (health < Rules.HEALTH_FULL) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 85;
                }
                else {
                    priority = 60;
                }
            }
            else if (health < Rules.HEALTH_SAFE) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 85;
                }
                else {
                    priority = 60;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 85;
                }
                else {
                    priority = 60;
                }
            }
        }
        else if (type.equals (ItemType.ROCKET_LAUNCHER)) {
            if (health < Rules.HEALTH_WARNING) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
            else if (health < Rules.HEALTH_FULL) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
            else if (health < Rules.HEALTH_SAFE) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 75;
                }
                else {
                    priority = 50;
                }
            }
        }
        else if (type.equals (ItemType.SHOCK_RIFLE)) {
            if (health < Rules.HEALTH_WARNING) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
            else if (health < Rules.HEALTH_FULL) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 95;
                }
                else {
                    priority = 80;
                }
            }
            else if (health < Rules.HEALTH_SAFE) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 95;
                }
                else {
                    priority = 80;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 95;
                }
                else {
                    priority = 80;
                }
            }
        }
        else if (type.equals (ItemType.SNIPER_RIFLE) || type.equals(ItemType.LIGHTNING_GUN)) {
            if (health < Rules.HEALTH_WARNING) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 75;
                }
                else {
                    priority = 50;
                }
            }
            else if (health < Rules.HEALTH_FULL) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 90;
                }
                else {
                    priority = 65;
                }
            }
            else if (health < Rules.HEALTH_SAFE) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 90;
                }
                else {
                    priority = 65;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 90;
                }
                else {
                    priority = 65;
                }
            }
        }
        else {
            priority = -1;
        }
        return priority;
    }     
}
