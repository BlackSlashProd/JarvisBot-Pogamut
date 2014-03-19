package iajv.project.pogamut.ut2004.JarvisBot.configs;

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

/**
 * Tools class to estimate weapons profits.
 */
public class UTWeapons {
    /**
     * Given a weapon, it estimates how profitable it is from 0 to 100.
     * @param type Weapon we want estimate how good it is.
     * @return How good this weapon is from 0 to 100.
     */
    public static int estimateWeaponAdvantage (final ItemType type, final Player enemy) {
        //ItemType type = weapon.getType();
        int profit = 0;
        // SHIELD GUN
        if(type.equals(ItemType.SHIELD_GUN)) {
            profit = 10;
        }
        // ASSAULT RIFLE
        else if (type.equals(ItemType.ASSAULT_RIFLE)) {
            profit = 20;
        }
        // BIO RIFLE
        else if (type.equals(ItemType.BIO_RIFLE)) {
            profit = 30;
        }
        // LINK GUN
        else if (type.equals(ItemType.LINK_GUN)) {
            profit = 40;
        }
        // MINIGUN
        else if (type.equals(ItemType.MINIGUN)) {
            profit = 50;
        }
        // FLAK CANNON
        else if (type.equals(ItemType.FLAK_CANNON)) {
            profit = 60;
        }
        // ROCKET LAUNCHER
        else if (type.equals(ItemType.ROCKET_LAUNCHER)) {
            profit = 70;
        }
        // SHOCK RIFLE
        else if (type.equals(ItemType.SHOCK_RIFLE)) {
            profit = 80;
        }
        // SNIPER RIFLE/LIGHTNING GUN
        else if (type.equals(ItemType.SNIPER_RIFLE) || type.equals(ItemType.LIGHTNING_GUN)) {
            profit = 90;
        }
        return profit;
    }
}
