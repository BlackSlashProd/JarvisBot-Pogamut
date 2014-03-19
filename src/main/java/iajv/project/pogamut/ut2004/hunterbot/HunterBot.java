package iajv.project.pogamut.ut2004.hunterbot;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Move;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Stop;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;

@AgentScoped
public class HunterBot extends UT2004BotModuleController<UT2004Bot> {

    @JProp
    public boolean shouldEngage = true;
    @JProp
    public boolean shouldPursue = true;
    @JProp
    public boolean shouldRearm = true;
    @JProp
    public boolean shouldCollectItems = true;
    @JProp
    public boolean shouldCollectHealth = true;
    @JProp
    public int healthLevel = 90;
    @JProp
    public int frags = 0;
    @JProp
    public int deaths = 0;

    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        if (event.getKiller().equals(info.getId())) {
            ++frags;
        }
        if (enemy == null) {
            return;
        }
        if (enemy.getId().equals(event.getId())) {
            previousState = State.OTHER;
            enemy = null;
        }
    }
    protected Player enemy = null;
    protected TabooSet<Item> tabooItems = null;
    private UT2004PathAutoFixer autoFixer;
    @Override
    public void prepareBot(UT2004Bot bot) {
        tabooItems = new TabooSet<Item>(bot);
        pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(bot, 3000, 10000)); // if the bot does not move for 3 seconds, considered that it is stuck
        pathExecutor.addStuckDetector(new UT2004PositionStuckDetector(bot)); // watch over the position history of the bot, if the bot does not move sufficiently enough, consider that it is stuck
        pathExecutor.addStuckDetector(new UT2004DistanceStuckDetector(bot)); // watch over distances to target
        autoFixer = new UT2004PathAutoFixer(bot, pathExecutor, fwMap, navBuilder); // auto-removes wrong navigation links between navpoints
        pathExecutor.getState().addListener(new FlagListener<IPathExecutorState>() {
            @Override
            public void flagChanged(IPathExecutorState changedValue) {
                switch (changedValue.getState()) {
                    case PATH_COMPUTATION_FAILED:
                    case STUCK:
                        if (item != null) {
                            tabooItems.add(item, 10);
                        }
                        reset();
                        break;

                    case TARGET_REACHED:
                        reset();
                        break;
                }
            }
        });
        weaponPrefs.addGeneralPref(ItemType.MINIGUN, false);
        weaponPrefs.addGeneralPref(ItemType.LIGHTNING_GUN, true);                
        weaponPrefs.addGeneralPref(ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(ItemType.FLAK_CANNON, true);        
        weaponPrefs.addGeneralPref(ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(ItemType.LINK_GUN, true);
        weaponPrefs.addGeneralPref(ItemType.ASSAULT_RIFLE, true);        
        weaponPrefs.addGeneralPref(ItemType.BIO_RIFLE, true);
    }
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("Hunter").setDesiredSkill(5);
    }
    protected static enum State {

        OTHER,
        ENGAGE,
        PURSUE,
        MEDKIT,
        GRAB,
        ITEMS
    }
    protected void reset() {
        previousState = State.OTHER;
        notMoving = 0;
        enemy = null;
        navigation.stopNavigation();
        itemsToRunAround = null;
        item = null;
    }
    protected State previousState = State.OTHER;
    protected int notMoving = 0;
    @Override
    public void logic() {
        // global anti-stuck?
        if (!info.isMoving()) {
            ++notMoving;
            if (notMoving > 4) {
                // we're stuck - reset the bot's mind
                reset();
                return;
            }
        }

        // 1) do you see enemy? 	-> go to PURSUE (start shooting / hunt the enemy)
        if (shouldEngage && players.canSeeEnemies() && weaponry.hasLoadedWeapon()) {
            stateEngage();
            return;
        }

        // 2) are you shooting? 	-> stop shooting, you've lost your target
        if (info.isShooting() || info.isSecondaryShooting()) {
            getAct().act(new StopShooting());
        }

        // 3) are you being shot? 	-> go to HIT (turn around - try to find your enemy)
        if (senses.isBeingDamaged()) {
            this.stateHit();
            return;
        }

        // 4) have you got enemy to pursue? -> go to the last position of enemy
        if (enemy != null && shouldPursue && weaponry.hasLoadedWeapon()) {  // !enemy.isVisible() because of 2)
            this.statePursue();
            return;
        }

        // 5) are you hurt?			-> get yourself some medKit
        if (info.getHealth() < healthLevel && canRunAlongMedKit()) {
            this.stateMedKit();
            return;
        }

        // 6) do you see item? 		-> go to GRAB_ITEM	  (pick the most suitable item and run for)        
        if (shouldCollectItems && !items.getVisibleItems().isEmpty()) {
        	item = getNearestVisibleItem();
        	if (item != null && fwMap.getDistance(info.getNearestNavPoint(), item.getNavPoint()) < 500) {
            	stateSeeItem();
        		previousState = State.GRAB;
        		return;
        	}
        }

        // 7) if nothing ... run around items
        stateRunAroundItems();
    }

    //////////////////
    // STATE ENGAGE //
    //////////////////
    protected boolean runningToPlayer = false;

    /**
     * Fired when bot see any enemy. <ol> <li> if enemy that was attacked last
     * time is not visible than choose new enemy <li> if enemy is reachable and the bot is far - run to him
     * <li> otherwise - stand still (kind a silly, right? :-)
     * </ol>
     */
    protected void stateEngage() {
        log.info("Decision is: ENGAGE");
        //config.setName("Hunter [ENGAGE]");

        boolean shooting = false;
        double distance = Double.MAX_VALUE;

        // 1) pick new enemy if the old one has been lost
        if (previousState != State.ENGAGE || enemy == null || !enemy.isVisible()) {
            // pick new enemy
            enemy = players.getNearestVisiblePlayer(players.getVisibleEnemies().values());
            if (enemy == null) {
                log.info("Can't see any enemies... ???");
                return;
            }
            if (info.isShooting()) {
                // stop shooting
                getAct().act(new StopShooting());
            }
            runningToPlayer = false;
        }

        if (enemy != null) {
            // 2) if not shooting at enemyID - start shooting
            distance = info.getLocation().getDistance(enemy.getLocation());

            // 3) should shoot?
            if (shoot.shoot(weaponPrefs, enemy) != null) {
                log.info("Shooting at enemy!!!");
                shooting = true;
            }
        }

        // 4) if enemy is far - run to him
        int decentDistance = Math.round(random.nextFloat() * 800) + 200;
        if (!enemy.isVisible() || !shooting || decentDistance < distance) {
            if (!runningToPlayer) {
                navigation.navigate(enemy);
                runningToPlayer = true;
            }
        } else {
            runningToPlayer = false;
            navigation.stopNavigation();
            getAct().act(new Stop());
        }

        previousState = State.ENGAGE;
    }

    ///////////////
    // STATE HIT //
    ///////////////
    protected void stateHit() {
        log.info("Decision is: HIT");
        getAct().act(new Rotate().setAmount(32000));
        previousState = State.OTHER;
    }

    //////////////////
    // STATE PURSUE //
    //////////////////
    /**
     * State pursue is for pursuing enemy who was for example lost behind a
     * corner. How it works?: <ol> <li> initialize properties <li> obtain path
     * to the enemy <li> follow the path - if it reaches the end - set lastEnemy
     * to null - bot would have seen him before or lost him once for all </ol>
     */
    protected void statePursue() {
        log.info("Decision is: PURSUE");
        //config.setName("Hunter [PURSUE]");
        if (previousState != State.PURSUE) {
            pursueCount = 0;
            navigation.navigate(enemy);
        }
        ++pursueCount;
        if (pursueCount > 30) {
            reset();
        } else {
            previousState = State.PURSUE;
        }
    }
    protected int pursueCount = 0;

    //////////////////
    // STATE MEDKIT //
    //////////////////
    protected void stateMedKit() {
        log.info("Decision is: MEDKIT");
        //config.setName("Hunter [MEDKIT]");
        if (previousState != State.MEDKIT) {
            List<Item> healths = new LinkedList();
            healths.addAll(items.getSpawnedItems(ItemType.HEALTH_PACK).values());
            if (healths.size() == 0) {
                healths.addAll(items.getSpawnedItems(ItemType.MINI_HEALTH_PACK).values());
            }
            Set<Item> okHealths = tabooItems.filter(healths);
            if (okHealths.size() == 0) {
                log.log(Level.WARNING, "No suitable health to run for.");
                stateRunAroundItems();
                return;
            }
            item = fwMap.getNearestItem(okHealths, info.getNearestNavPoint());
            navigation.navigate(item);
        }
        previousState = State.MEDKIT;
    }

    ////////////////////
    // STATE SEE ITEM //
    ////////////////////
    protected Item item = null;

    protected void stateSeeItem() {
        log.info("Decision is: SEE ITEM");
        //config.setName("Hunter [SEE ITEM]");

        if (item != null && item.getLocation().getDistance(info.getLocation()) < 80) {
            reset();
        }

        if (item != null && previousState != State.GRAB) {
            if (item.getLocation().getDistance(info.getLocation()) < 300) {
            	getAct().act(new Move().setFirstLocation(item.getLocation()));
            } else {
            	navigation.navigate(item);
            }             
        }
    }
    
    protected Item getNearestPossiblySpawnedItemOfType(ItemType type) {
    	final NavPoint nearestNavPoint = info.getNearestNavPoint();
    	List<Item> itemsDistanceSortedAscending = 
    			DistanceUtils.getDistanceSorted(
    					items.getSpawnedItems(type).values(), 
    					info.getLocation(), 
    					new DistanceUtils.IGetDistance<Item>() {
							@Override
							public double getDistance(Item object, ILocated target) {
								return fwMap.getDistance(nearestNavPoint, object.getNavPoint());
							}
						}
    			);
    	if (itemsDistanceSortedAscending.size() == 0) return null;
    	return itemsDistanceSortedAscending.get(0);
    }
    
    protected Item getNearestVisibleItem() {
    	final NavPoint nearestNavPoint = info.getNearestNavPoint();
    	List<Item> itemsDistanceSortedAscending = 
    			DistanceUtils.getDistanceSorted(
    					items.getVisibleItems().values(), 
    					info.getLocation(), 
    					new DistanceUtils.IGetDistance<Item>() {
							@Override
							public double getDistance(Item object, ILocated target) {
								return fwMap.getDistance(nearestNavPoint, object.getNavPoint());
							}
						}
    			);
    	if (itemsDistanceSortedAscending.size() == 0) return null;
    	return itemsDistanceSortedAscending.get(0);
    }

    protected boolean canRunAlongMedKit() {
        boolean result = !items.getSpawnedItems(ItemType.HEALTH_PACK).isEmpty()
                || !items.getSpawnedItems(ItemType.MINI_HEALTH_PACK).isEmpty();
        return result;
    }

    ////////////////////////////
    // STATE RUN AROUND ITEMS //
    ////////////////////////////
    protected List<Item> itemsToRunAround = null;

    protected void stateRunAroundItems() {
        log.info("Decision is: ITEMS");
        //config.setName("Hunter [ITEMS]");
        if (previousState != State.ITEMS) {
            itemsToRunAround = new LinkedList<Item>(items.getSpawnedItems().values());
            Set<Item> items = tabooItems.filter(itemsToRunAround);
            if (items.size() == 0) {
                log.log(Level.WARNING, "No item to run for...");
                reset();
                return;
            }
            item = items.iterator().next();
            navigation.navigate(item);
        }
        previousState = State.ITEMS;
    }

    ////////////////
    // BOT KILLED //
    ////////////////
    @Override
    public void botKilled(BotKilled event) {
        itemsToRunAround = null;
        enemy = null;
    }

    ///////////////////////////////////
    public static void main(String args[]) throws PogamutException {
        // starts 2 Hunters at once
        // note that this is the most easy way to get a bunch of bots running at the same time        
    	new UT2004BotRunner(HunterBot.class, "Hunter").setMain(true).setLogLevel(Level.INFO).startAgents(2);
    }
}
