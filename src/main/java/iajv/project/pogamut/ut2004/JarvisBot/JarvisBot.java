package iajv.project.pogamut.ut2004.JarvisBot;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectDisappearedEvent;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Stop;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.HearNoise;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.HearPickup;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.MapFinished;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerJoinsGame;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerLeft;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Spawn;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import iajv.project.pogamut.ut2004.JarvisBot.brain.Brain;
import iajv.project.pogamut.ut2004.JarvisBot.configs.Rules;
import iajv.project.pogamut.ut2004.JarvisBot.configs.UTItems;
import iajv.project.pogamut.ut2004.JarvisBot.configs.UTWeapons;
import iajv.project.pogamut.ut2004.JarvisBot.utils.Computing;
import iajv.project.pogamut.ut2004.JarvisBot.utils.MapNavInfo;
import java.util.Map;

/**
 * 
 */
@AgentScoped
public class JarvisBot extends UT2004BotModuleController<UT2004Bot> {

    /**
     * boolean switch to activate engage behavior
     */
    @JProp
    public boolean shouldEngage = true;
    
    /**
     * boolean switch to activate pursue behavior
     */
    @JProp
    public boolean shouldPursue = true;
    /**
     * boolean switch to activate rearm behavior
     */
    @JProp
    public boolean shouldRearm = true;
    /**
     * boolean switch to activate collect items behavior
     */
    @JProp
    public boolean shouldCollectItems = true;
    /**
     * boolean switch to activate collect health behavior
     */
    @JProp
    public boolean shouldCollectHealth = true;
    /**
     * how many times the bot killed other bots.
     */
    @JProp
    public int frags = 0;
    /**
     * how many times the bot died
     */
    @JProp
    public int deaths = 0;
    
    // *************************************************************************
    //                              STATIC FIELDS
    // *************************************************************************
    
    /** Bot's name **/
    private static final String botName = "JarvisBot";
    /** Bot's number **/
    private static int botNum = 0;
    /** All reachables pathnodes **/
    public static NavPoint[] pathNodes;
    /** All importants areas (items, damage area, ...) **/
    public static NavPoint[] areas;
    
    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************
    
    private int botIndex = botNum;
    /** Bot's brain **/
    private Brain brain;
    /** Last see player **/
    protected Player focusPlayer = null;
    /** True if we have just killed an enemy */
    private boolean recentKiller = false;
    /** **/
    protected TabooSet<Item> tabooItems = null;
    /** **/
    protected UT2004PathAutoFixer autoFixer;
    /** It represents the destination where we want to make the bot go (no matter what) */
    private Location destination = null;
    
    // *************************************************************************
    //                                METHODS
    // *************************************************************************
    
    /**
     * To modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName(botName+"#"+(botNum++)).setDesiredSkill(5).setSkin("Dominator");
    }

    /**
     * Handshake with GameBots2004 is over - bot has information about the map
     * in its world view.
     *
     * @param gameInfo informaton about the game type
     * @param config information about configuration
     * @param init information about configuration
     */
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange config, InitedMessage init) {
        pathNodes = MapNavInfo.initializePathNodes (world);
        areas = MapNavInfo.initializeAreas (items);        
    }

    /**
     * The bot is initilized in the environment - a physical representation of
     * the bot is present in the game.
     *
     * @param gameInfo informaton about the game type
     * @param config information about configuration
     * @param init information about configuration
     * @param self information about the agent
     */
    @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
        if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[INIT] - Hello world! I am alive!");
    }
    
    /**
     * 
     * 
     * @param event 
     */
    @EventListener (eventClass = Spawn.class)
    public void newSpawn (Spawn event) {
        if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[EVENT] - New Spawn.");
    }
    
    /**
     * This method is called only once, right before actual logic() method is
     * called for the first time.
     */
    @Override
    public void beforeFirstLogic() {
        
    }
    
    // *************************************************************************
    //                                LISTENERS
    // *************************************************************************  
    
    /**
    * Whenever an item gets in or out of bot's line of vision
    * 
    * @param event Item appeared..
    */
    @ObjectClassEventListener (eventClass = WorldObjectAppearedEvent.class, objectClass = Item.class)
    protected void objectAppeared (WorldObjectAppearedEvent <Item> event) {
        //Item item = event.getObject ();
    }    
 
    /**
    * Whenever a player gets in or out of bot's line of vision
    * 
    * @param event Item appeared..
    */
    @ObjectClassEventListener (eventClass = WorldObjectAppearedEvent.class, objectClass = Player.class)
    protected void playerAppeared (WorldObjectAppearedEvent<Player> event) {
        Player targetPlayer = event.getObject ();
        brain.updatePlayerLocation(targetPlayer, game.getTime());
    }    
    
    /**
    * Whenever an object is updated, it gets triggered. In this particular case,
    * it gets triggered when the object is of type Player.
    * @param event Player updated..
    */
    @ObjectClassEventListener (eventClass = WorldObjectUpdatedEvent.class, objectClass = Player.class)
    protected void playerUpdated (WorldObjectUpdatedEvent<Player> event) {
        Player targetPlayer = event.getObject();
        brain.updatePlayerLocation(targetPlayer, game.getTime());
    }

    /**
     * Whenever a player gets out of sight, it gets triggered.
     * @param event Player disappeared.
     */
    @ObjectClassEventListener (eventClass = WorldObjectDisappearedEvent.class, objectClass = Player.class)
    protected void playerDisappeared (WorldObjectDisappearedEvent<Player> event) {
        if (!players.canSeeEnemies()) {
            shoot.stopShooting();
        }
    }

    @EventListener(eventClass = ItemPickedUp.class)
    public void itemPickedUp(ItemPickedUp event) {
    	ItemType itemType = event.getType();
        brain.itemPickedUp(descriptors, itemType);
        tabooItems.add(items.getItem(event.getId()),60);
    }

    /**
     * Whenever a projectile is in the bots field of vision, it gets triggered.
     * Note that projectiles can also be the ones we shoot.
     * @param event Projectile coming.
     */
    @ObjectClassEventListener (eventClass = WorldObjectUpdatedEvent.class, objectClass = IncomingProjectile.class)
    protected void incomingProjectile (WorldObjectUpdatedEvent<IncomingProjectile> event) {
        IncomingProjectile projectile = event.getObject ();
    }

    /**
     * Whenever the bot hears a noise, it gets triggered.
     * (usually when he hears another bot/user)
     * @param event HearNoise event.
     */
    @EventListener (eventClass = HearNoise.class)
    protected void hearNoise (HearNoise event) {
    	//double noiseDistance = event.getDistance();
        if(focusPlayer==null) {
            Location focusSpot = Computing.rotationToLocation (info, event.getRotation());

            if (pathExecutor.isExecuting()) {
                pathExecutor.setFocus (focusSpot);
            }
            else {
                move.stopMovement();
                move.turnTo (focusSpot);
            }
        }
    }

    /**
     * Whenever an item is picked up, it gets triggered.
     * It also gets triggered when the bot picks up things (he hears it).
     * @param event HearPickup event.
     */
    @EventListener (eventClass = HearPickup.class)
    protected void hearPickup (HearPickup event) {
        if(item==null) {
            Item item = items.getItem (event.getSource ());
            if (focusPlayer == null && item != null && item.getLocation () != null && info.getDistance (item.getLocation()) > 200) {
                Location focusSpot = Computing.rotationToLocation (info, event.getRotation());
                if (pathExecutor.isExecuting()) {
                    pathExecutor.setFocus(focusSpot);
                }
                else {
                    move.stopMovement();
                    move.turnTo(focusSpot);
                }
            }   
        }
    }
    
    /**
     * Whenever a player dies, it gets triggered.
     * @param event PlayerKilled event.
     */
    @EventListener (eventClass = PlayerKilled.class)
    protected void playerKilled (PlayerKilled event) {
        UnrealId killerId = event.getKiller ();
        UnrealId deadId = event.getId();
        if (killerId!= null && killerId.equals(info.getId())) {
            ++frags;
            Player dead = (Player) world.get(deadId);     
            body.getCommunication().sendGlobalTextMessage ("Reste à terre "+dead.getName()+" !");
            shoot.stopShooting();
        }
        if (focusPlayer!=null && focusPlayer.getId().equals(deadId)) {
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage ("Stop shooting !");
            shoot.stopShooting();
            previousState = State.OTHER;
            focusPlayer = null;
        }
    }

    /**
     * Whenever a user/bot joins the match it gets triggered.
     * @param event PlayerJoinsGame event.
     */
    @EventListener (eventClass = PlayerJoinsGame.class)
    protected void playerJoinedGame (PlayerJoinsGame event) {
        String playerName = event.getName ();
    }

    /**
     * Whenever a user/bot leaves the match it gets triggered.
     * @param event PlayerLeft event.
     */
    @EventListener (eventClass = PlayerLeft.class)
    protected void playerLeft (PlayerLeft event) {
        String playerName = event.getName();
    }

    /**
     * When the match ends it gets triggered.
     * @param event MapFinished event.
     */
    @EventListener (eventClass = MapFinished.class)
    protected void mapFinished (MapFinished event) {
        if(botIndex==0) body.getCommunication ().sendGlobalTextMessage ("[FINISH] - Map finished.");
        
    }

    /**
     * Whenever the bot hit an other player.
     * @param event PlayerDamaged event.
     */
    @EventListener (eventClass = PlayerDamaged.class)
    protected void playerDamaged (PlayerDamaged event) {
        int givenDamage = event.getDamage();
    }
    
    /**
    * Whenever the bot is hit.
    * @param event BotDamaged event.
    */
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
        int takenDamage = event.getDamage();
        brain.takeDamage(takenDamage);
    }
    
    /**
     * Called each time the bot dies. Good for reseting all bot's state
     * dependent variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
        itemsToRunAround = null;
        focusPlayer = null;
        tabooItems.clear();
        shoot.stopShooting();
        brain.reset();
        pathExecutor.stop();
        if (event.getKiller() != null) {
            UnrealId killerId = event.getKiller();
            Player killer = (Player) world.get(killerId);
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[EVENT] - I was KILLED by "+killer.getName()+" !");
        } 
    }

    /**
     * Bot's preparation - called before the bot is connected to GB2004 and
     * launched into UT2004.
     * @param bot
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
        
        brain = new Brain(info, body);
        focusPlayer = null;
        tabooItems = new TabooSet<Item>(bot);
        
        pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(bot, 3000, 10000));
        pathExecutor.addStuckDetector(new UT2004PositionStuckDetector(bot));
        pathExecutor.addStuckDetector(new UT2004DistanceStuckDetector(bot)); 

        autoFixer = new UT2004PathAutoFixer(bot, pathExecutor, fwMap, navBuilder); // auto-removes wrong navigation links between navpoints

        // listeners        
        pathExecutor.getState().addListener(new FlagListener<IPathExecutorState>() {
            @Override
            public void flagChanged(IPathExecutorState changedValue) {
                switch (changedValue.getState()) {
                    case STUCK:
                        if (item != null) {
                            tabooItems.add(item, 10);
                        }
                        reset();
                        break;

                    case TARGET_REACHED:
                        if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[EVENT] - Reached");
                        reset();
                        break;
                }
            }
        });

        // DEFINE WEAPON PREFERENCES
        weaponPrefs.addGeneralPref(ItemType.MINIGUN, false);
        weaponPrefs.addGeneralPref(ItemType.LIGHTNING_GUN, true);                
        weaponPrefs.addGeneralPref(ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(ItemType.FLAK_CANNON, true);        
        weaponPrefs.addGeneralPref(ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(ItemType.LINK_GUN, true);
        weaponPrefs.addGeneralPref(ItemType.ASSAULT_RIFLE, true);        
        weaponPrefs.addGeneralPref(ItemType.BIO_RIFLE, true);
    }

    private Item getBestVisibleItem(Map<UnrealId, Item> visibleItems) {
        Item retitem = getNearestPickableItem(info.getNearestNavPoint());
        if(retitem==null)
            return retitem;
        int maximumPriority = UTItems.estimateItemPriority (retitem, info, weaponry);;
        int itemPriority;

        double maximumPriorityDistance = Computing.INFINITY;
        double itemPriorityDistance = 0;
        for (Item it : visibleItems.values()) {
            itemPriority = UTItems.estimateItemPriority (it, info, weaponry);
            Location itemLocation = it.getLocation();
            if (itemPriority > maximumPriority) {
                maximumPriorityDistance = info.getDistance (itemLocation);
                maximumPriority = itemPriority;
                retitem = it;
            }
            else if (itemPriority == maximumPriority) {
                itemPriorityDistance = info.getDistance (itemLocation);
                if (itemPriorityDistance < maximumPriorityDistance) {
                    maximumPriorityDistance = itemPriorityDistance;
                    maximumPriority = itemPriority;
                    retitem = it;
                }
            }
        }
        return retitem;
    }

    private Item getNearestPickableItem(NavPoint whereFrom) {
        Map<UnrealId, Item> allItems = items.getAllItems();
        Set<Item> spawnedItems = tabooItems.filter(allItems.values());
        if (spawnedItems.isEmpty()) {
            return null;
        }
        float closestDist = Float.MAX_VALUE;
        Item closestItem = null;
        for (Item it: spawnedItems) {
            if (fwMap.getDistance(whereFrom, it.getNavPoint()) < closestDist) {
                closestDist = fwMap.getDistance(whereFrom, it.getNavPoint());
                closestItem = it;
            }
        }
        return closestItem;
    }
    
    /**
     * The bot maintains the information of the state it was in the previous
     * logic-cycle.
     */
    protected static enum State {
        OTHER,
        ENGAGE,
        PURSUE,
        RETREAT,
        MEDKIT,
        GRAB,
        WANDERING
    }

    /**
     * Reset
     */
    protected void reset() {
        navigation.stopNavigation();
        shoot.stopShooting();
        previousState = State.OTHER;
        notMoving = 0;
        itemsToRunAround = null;
        item = null;
        focusPlayer = null;
        destination = null;
    }
    /**
     * 
     */
    protected State previousState = State.OTHER;
    /**
     * Global anti-stuck mechanism.
     */
    protected int notMoving = 0;

    /**
     * Main method that controls the bot - makes decisions what to do next.
     *
     */
    @Override
    public void logic() {
        if (!info.isMoving()) {
            ++notMoving;
            if (notMoving > 4) {
                reset();
                return;
            }
        }
        boolean canSeePlayers = players.canSeeEnemies();
        boolean brainShouldEngage = brain.shouldEngage(players);
        
        // ENGAGE : If you see close enemies.
        if (shouldEngage && brainShouldEngage  && canSeePlayers && weaponry.hasLoadedWeapon()) {
            this.stateEngage();
            return;
        }
        
        boolean isShooting = (info.isShooting()||info.isSecondaryShooting());
        
        // Stop Shooting
        if(isShooting) shoot.stopShooting();
        // Change weapon
        if(brain.needChangeWeapon())switchToBestWeapon(focusPlayer);
        
        boolean damaged = senses.isBeingDamaged();
        
        // HIT : If you are hurt.
        if (damaged) {
            this.stateHit();
            return;
        }
        // HEALTH : If you need health ?
        if (brain.needHealth() && canRunAlongMedKit()) {
            this.stateMedKit();
            return;
        }
        
        // PURSUE : If you have a target.
        if (focusPlayer != null && shouldPursue && weaponry.hasLoadedWeapon()) {  
            this.statePursue();
            return;
        }
        // GRAB : If you see item(s). 
        if (shouldCollectItems && !items.getVisibleItems().isEmpty()) {
             this.stateSeeItem();
             return;
        }
        // WANDERING : If nothing to do.
        if(destination==null) stateWandering();
    }

    //////////////////
    // STATE ENGAGE //
    //////////////////
    protected boolean runningToPlayer = false;

    /**
     * Fired when bot see any enemy.
     */
    protected void stateEngage() {
        boolean shooting = false;
        double distance = Double.MAX_VALUE;
        // 1) pick new enemy if the old one has been lost
        if (previousState != State.ENGAGE || focusPlayer == null || !focusPlayer.isVisible()) {
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[STATE] - Engage");
            focusPlayer = players.getNearestVisiblePlayer(players.getVisibleEnemies().values());
            if (focusPlayer == null) {
                shoot.stopShooting();
                return;
            }
            runningToPlayer = false;
        }
        if (focusPlayer != null) {
            distance = info.getLocation().getDistance(focusPlayer.getLocation());
            Weapon currentWeapon = weaponry.getCurrentWeapon();
            ItemType weaponType = currentWeapon.getType();
            if(currentWeapon.getPrimaryAmmo()==0 && currentWeapon.getSecondaryAmmo()==0) {
                if(botIndex==0) body.getCommunication().sendGlobalTextMessage ("[INFO] - NO AMMO !");
                switchToBestWeapon(focusPlayer);
            }
            Location enemyLocation = focusPlayer.getLocation();
            double enemyDistance = info.getDistance(enemyLocation);
            int enemyHeight = Computing.estimateHeight (info.getLocation(), enemyLocation);
            pathExecutor.setFocus(focusPlayer);
            move.turnTo (focusPlayer);

            // *****************************************************************
            //                     TRADITIONAL SHOOTING
            // *****************************************************************
            // SHIELD GUN
            if (weaponType.equals(ItemType.SHIELD_GUN)) {
                shoot.shootPrimary(focusPlayer);
                shooting = true;
            }
            // ASSAULT RIFLE
            else if (weaponType.equals(ItemType.ASSAULT_RIFLE)) {
                if (enemyDistance < Rules.CLOSE) { 
                    if (currentWeapon.getSecondaryAmmo() > 0) {
                        shoot.shootPrimary(focusPlayer);
                    }
                    else {
                        shoot.shootSecondaryCharged(focusPlayer, 2);
                    }
                }
                else { 
                    if (currentWeapon.getSecondaryAmmo() > 0) {
                        shoot.shootPrimary(focusPlayer);
                    }
                    else {
                       shoot.shootSecondaryCharged(focusPlayer, 3);
                    }
                }
                shooting = true;
            }
            // BIO RIFLE
            else if (weaponType.equals(ItemType.BIO_RIFLE)) {
                if (enemyDistance < Rules.CLOSE) { 
                    shoot.shootPrimary(focusPlayer);
                }
                else {
                    shoot.shootSecondaryCharged(focusPlayer, 3);
                }
                shooting = true;
            }
            // LINK GUN
            else if (weaponType.equals(ItemType.LINK_GUN)) {
                if (enemyDistance < Rules.CLOSE) { 
                    shoot.shootSecondary(focusPlayer);
                }
                else if (enemyDistance < Rules.AVERAGE) { 
                    shoot.shootSecondary(focusPlayer);
                }
                else {
                    shoot.shootPrimary(focusPlayer);
                }
                shooting = true;
            }
            // MINIGUN
            else if (weaponType.equals(ItemType.MINIGUN)) {
                if (enemyDistance >= Rules.FAR) {
                    shoot.shootSecondary(focusPlayer);
                }
                else {
                    shoot.shootPrimary(focusPlayer);
                }
                shooting = true;
            }
            // FLAK CANNON
            else if (weaponType.equals(ItemType.FLAK_CANNON)) {
                if (enemyDistance < Rules.CLOSE) { 
                    shoot.shootPrimary(focusPlayer);
                }
                else if (enemyDistance < Rules.AVERAGE) { 
                    if (enemyHeight == 0) {
                        if (enemyDistance < Rules.AVERAGE/2) {
                            shoot.shootPrimary(focusPlayer);
                        }
                        else {
                            shoot.shootSecondary(focusPlayer);
                        }
                    }
                    else if (enemyHeight < 0) {
                        shoot.shootPrimary(focusPlayer);
                    }
                    else {
                        shoot.shootSecondary(focusPlayer);
                    }
                }
                else if (enemyDistance < Rules.FAR) {
                    if (enemyHeight == 0 || enemyHeight < 0) { 
                        shoot.shootPrimary(focusPlayer);
                    }
                    else {
                        shoot.shootSecondary(focusPlayer);
                    }
                }
                else if (enemyDistance >= Rules.FAR) { 
                    shoot.shootPrimary(focusPlayer);
                }
                shooting = true;
            }
            // ROCKET LAUNCHER
            else if (weaponType.equals(ItemType.ROCKET_LAUNCHER)) {
                shoot.shootPrimary(focusPlayer);
                shooting = true;
            }
            //SHOCK RIFLE
            else if (weaponType.equals(ItemType.SHOCK_RIFLE)) {
                shoot.shootPrimary(focusPlayer);
                shooting = true;
            }
            // SNIPER RIFLE
            else if (weaponType.equals(ItemType.SNIPER_RIFLE) || weaponType.equals(ItemType.LIGHTNING_GUN)) {
                shoot.shootPrimary(focusPlayer);
                shooting = true;
            }
        }

        // 
        int decentDistance = (int) (Math.round(random.nextFloat() * Rules.AVERAGE) + 200);
        if (!focusPlayer.isVisible() || !shooting || decentDistance < distance) {
            if (!runningToPlayer && !navigation.isNavigating()) {
                navigation.navigate(focusPlayer);
                runningToPlayer = true;
            }
        } else {
            runningToPlayer = false;
            navigation.stopNavigation();
            getAct().act(new Stop());
            Item target = getBestVisibleItem(items.getVisibleItems());
            if(destination!=null&&info.getLocation().getDistance(destination)<80) {
                destination = null;
            }
            if(target!=null && destination==null) {
                if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[SUBSTATE] - Engage_Grab");
                destination = target.getLocation();
                getAct().act(new Move().setFirstLocation(destination));
                //navigation.navigate(destination);
            }
        }
        previousState = State.ENGAGE;
    }

    ///////////////
    // STATE HIT //
    ///////////////
    protected void stateHit() {
        if (focusPlayer == null) {
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[STATE] - Hit");
            move.stopMovement ();
            Player pl = players.getNearestEnemy(30);
            if(pl!=null)
                move.turnTo(pl);
        }
        previousState = State.OTHER;
    }

    //////////////////
    // STATE PURSUE //
    //////////////////
    
    protected int pursueCount = 0;
    
    /**
     * State pursue is for pursuing enemy who was lost of view.
     */
    protected void statePursue() {
        if(focusPlayer==null) {
            reset();
        }
        if (previousState != State.PURSUE) {
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[STATE] - Pursue");
            pursueCount = 0;
            destination = focusPlayer.getLocation();
            Location lastKnow = brain.getLastKnowPlayerLocation(focusPlayer, game.getTime());
            if(lastKnow!=null) {
                destination = lastKnow;
            }
            navigation.navigate(destination);
        }
        ++pursueCount;
        if (pursueCount > 30) {
            reset();
        } else {
            previousState = State.PURSUE;
        }
    }
    
    //////////////////
    // STATE MEDKIT //
    //////////////////
    protected void stateMedKit() {
        if (previousState != State.MEDKIT) {
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[STATE] - MedKit");
            List<Item> healths = new LinkedList();
            healths.addAll(items.getSpawnedItems(ItemType.HEALTH_PACK).values());
            if (healths.isEmpty()) {
                healths.addAll(items.getSpawnedItems(ItemType.MINI_HEALTH_PACK).values());
            }
            Set<Item> okHealths = tabooItems.filter(healths);
            if (okHealths.isEmpty()) {
                stateWandering();
                return;
            }
            item = fwMap.getNearestItem(okHealths, info.getNearestNavPoint());
            destination = item.getLocation();
            navigation.navigate(item);
        }
        previousState = State.MEDKIT;
    }

    ////////////////////
    // STATE SEE ITEM //
    ////////////////////
    protected Item item = null;

    protected void stateSeeItem() {
        if (item != null && item.getLocation().getDistance(info.getLocation()) < 80) {
            reset();
        }
        
        if(item==null) {
            item = getNearestPickableItem(info.getNearestNavPoint());
        }

        if (item != null && previousState != State.GRAB) {
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[STATE] - Grab");
            move.turnTo(item);
            if (item.getLocation().getDistance(info.getLocation()) < 300) {
            	getAct().act(new Move().setFirstLocation(item.getLocation()));
            } else {
            	navigation.navigate(item);
            }         
            /*if(item!=null) { 
                destination = item.getLocation();
            }*/
        }
    }

    protected boolean canRunAlongMedKit() {
        boolean result = !items.getSpawnedItems(ItemType.HEALTH_PACK).isEmpty()
                || !items.getSpawnedItems(ItemType.MINI_HEALTH_PACK).isEmpty();
        return result;
    }

    ////////////////////////////
    // STATE RUN AROUND ITEMS //
    ////////////////////////////
    
    /** **/
    protected List<Item> itemsToRunAround = null;
    
    protected void stateWandering() {
        
        if (previousState != State.WANDERING) {     
            if(botIndex==0) body.getCommunication().sendGlobalTextMessage("[STATE] - Wandering");
            itemsToRunAround = new LinkedList<Item>(items.getSpawnedItems().values());
            Set<Item> itemsAround = tabooItems.filter(itemsToRunAround);
            if (itemsAround.isEmpty()) {
                item = getBestVisibleItem(items.getAllItems());
            }
            else {
                item = itemsAround.iterator().next();
            }
            if(item==null) {
                reset();
                return;
            }
            destination = item.getLocation();
            navigation.navigate(item);
        }
        previousState = State.WANDERING;
    }

   /**
     * Switches to best weapon.
     * @param enemy Enemy.
     */
    public void switchToBestWeapon(final Player enemy) {
        Map <ItemType, Weapon> arsenal;
        int distanceAdvantage = 0, maximum = 0;
        Weapon selectedWeapon = null;

        // Get all weapons
        arsenal = weaponry.getWeapons ();
        if (weaponry.hasPrimaryWeaponAmmo(ItemType.SHOCK_RIFLE)) {
            weaponry.changeWeapon (ItemType.SHOCK_RIFLE);
            brain.updateWeapon(weaponry.getCurrentWeapon());
        }
        else if (weaponry.hasWeapon(ItemType.SHOCK_RIFLE) && weaponry.getAmmo(ItemType.SHOCK_RIFLE) >= 5) {
            weaponry.changeWeapon (ItemType.SHOCK_RIFLE);
            brain.updateWeapon(weaponry.getCurrentWeapon());
        }
        else {
            for (Weapon currentWeapon : arsenal.values ()) {
                if (currentWeapon.getAmmo() > 0) {
                    // Estimate the advantage this weapon has based on the distance to the enemy
                    distanceAdvantage = UTWeapons.estimateWeaponAdvantage (currentWeapon.getType(), enemy);
                    if (distanceAdvantage > maximum) {
                        maximum = distanceAdvantage;
                        selectedWeapon = currentWeapon;
                    }
                }
            }

            if (selectedWeapon != null) {
                Weapon currentWeapon = weaponry.getCurrentWeapon();
                if (!currentWeapon.equals(selectedWeapon)) {
                    brain.updateWeapon(selectedWeapon);
                    weaponry.changeWeapon(selectedWeapon);
                  
                }
            }
        }
    }
    
    ///////////////////////////////////
    public static void main(String args[]) throws PogamutException {        
        try {
            new UT2004BotRunner(JarvisBot.class, "Jarvis").setMain(true).setLogLevel(Level.INFO).startAgents(2);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
