package com.clubobsidian.obbylang.manager.bukkit.world;

import com.clubobsidian.obbylang.plugin.BukkitObbyLangPlugin;
import com.clubobsidian.obbylang.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class WorldManager {

    private static WorldManager instance;

    private static Class<?> craftServerClass;
    private static Class<?> minecraftServerClass;
    private static Class<?> dataConverterManagerClass;
    private static Class<?> iDataManagerClass;
    private static Class<?> serverNBTManagerClass;
    private static Class<?> worldNBTStorageClass;
    private static Class<?> worldDataClass;
    private static Class<?> enumGamemodeClass;
    private static Class<?> worldSettingsClass;
    private static Class<?> worldTypeClass;
    private static Class<?> methodProfilerClass;
    private static Class<?> worldServerClass;
    private static Class<?> worldNMSClass;
    private static Class<?> craftScoreboardManagerClass;
    private static Class<?> craftScoreboardClass;
    private static Class<?> entityTrackerClass;
    private static Class<?> worldManagerClass;
    private static Class<?> enumDifficultyClass;
    private static Class<?> iWorldAccessClass;
    private static Class<?> baseBlockPositionClass;
    private static Class<?> chunkProviderServerClass;

    private static Constructor<?> serverNBTManagerConstructor;
    private static Constructor<?> worldSettingsConstructor;
    private static Constructor<?> worldDataConstructor;
    private static Constructor<?> worldServerConstructor;
    private static Constructor<?> entityTrackerConstructor;
    private static Constructor<?> worldManagerConstructor;

    private static Object craftServer;
    private static Object minecraftServer;
    private static Object survivalGamemode;
    private static Object customizedWorld;
    private static Object methodProfiler;
    private static Map<String, World> craftServerWorlds;
    private static Object scoreboard;
    private static Object hardDifficulty;

    @SuppressWarnings("rawtypes")
    private static List nmsWorlds;

    private static Method setGeneratorSettings;
    private static Method sdmGetWorldData;
    private static Method getWorldData;
    private static Method checkName;
    private static Method worldServerGetWorld;
    private static Method worldServerAddIWorldAccess;
    private static Method worldDataSetDifficulty;
    private static Method worldServerSetSpawnFlags;
    private static Method nmsWorldGetWorld;
    private static Method nmsWorldGetSpawn;
    private static Method worldServerGetChunkProviderServer;
    private static Method chunkProviderServerGetChunkAt;
    private static Method blockPositionGetX;
    private static Method blockPositionGetZ;

    private static Field craftServerConsole;

    private static Field dataConverterManager;
    private static Field worldServerScoreboard;
    private static Field worldServerTracker;
    private static Field worldServerWorldData;
    private static Field worldServerDimension;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        if(instance == null) {
            instance = new WorldManager();

            try {
                craftServerClass = ReflectionUtil.getCraftClass("CraftServer");
                minecraftServerClass = ReflectionUtil.getMinecraftClass("MinecraftServer");
                iDataManagerClass = ReflectionUtil.getMinecraftClass("IDataManager");
                dataConverterManagerClass = ReflectionUtil.getMinecraftClass("DataConverterManager");
                serverNBTManagerClass = ReflectionUtil.getMinecraftClass("ServerNBTManager");
                worldNBTStorageClass = ReflectionUtil.getMinecraftClass("WorldNBTStorage");
                worldDataClass = ReflectionUtil.getMinecraftClass("WorldData");
                enumGamemodeClass = ReflectionUtil.getMinecraftClass("EnumGamemode");
                worldSettingsClass = ReflectionUtil.getMinecraftClass("WorldSettings");
                worldTypeClass = ReflectionUtil.getMinecraftClass("WorldType");
                methodProfilerClass = ReflectionUtil.getMinecraftClass("MethodProfiler");
                worldServerClass = ReflectionUtil.getMinecraftClass("WorldServer");
                worldNMSClass = ReflectionUtil.getMinecraftClass("World");
                craftScoreboardManagerClass = ReflectionUtil.getCraftClass("scoreboard", "CraftScoreboardManager");
                craftScoreboardClass = ReflectionUtil.getCraftClass("scoreboard", "CraftScoreboard");
                entityTrackerClass = ReflectionUtil.getMinecraftClass("EntityTracker");
                worldManagerClass = ReflectionUtil.getMinecraftClass("WorldManager");
                enumDifficultyClass = ReflectionUtil.getMinecraftClass("EnumDifficulty");
                iWorldAccessClass = ReflectionUtil.getMinecraftClass("IWorldAccess");
                baseBlockPositionClass = ReflectionUtil.getMinecraftClass("BaseBlockPosition");
                chunkProviderServerClass = ReflectionUtil.getMinecraftClass("ChunkProviderServer");

                serverNBTManagerConstructor = serverNBTManagerClass.getDeclaredConstructor(File.class, String.class, boolean.class, dataConverterManagerClass);
                worldSettingsConstructor = worldSettingsClass.getDeclaredConstructor(long.class, enumGamemodeClass, boolean.class, boolean.class, worldTypeClass);
                worldDataConstructor = worldDataClass.getDeclaredConstructor(worldSettingsClass, String.class);
                worldServerConstructor = worldServerClass.getDeclaredConstructor(minecraftServerClass, iDataManagerClass, worldDataClass, int.class, methodProfilerClass, Environment.class, ChunkGenerator.class);
                entityTrackerConstructor = entityTrackerClass.getDeclaredConstructor(worldServerClass);
                worldManagerConstructor = worldManagerClass.getDeclaredConstructor(minecraftServerClass, worldServerClass);

                serverNBTManagerConstructor.setAccessible(true);
                worldSettingsConstructor.setAccessible(true);
                worldDataConstructor.setAccessible(true);
                worldServerConstructor.setAccessible(true);
                entityTrackerConstructor.setAccessible(true);
                worldManagerConstructor.setAccessible(true);

                setGeneratorSettings = worldSettingsClass.getDeclaredMethod("setGeneratorSettings", String.class);
                setGeneratorSettings.setAccessible(true);

                sdmGetWorldData = worldNBTStorageClass.getDeclaredMethod("getWorldData");
                sdmGetWorldData.setAccessible(true);

                craftServer = craftServerClass.cast(Bukkit.getServer());

                Method getServer = craftServerClass.getDeclaredMethod("getServer");
                minecraftServer = getServer.invoke(craftServer);

                Method getById = enumGamemodeClass.getDeclaredMethod("getById", int.class);
                survivalGamemode = getById.invoke(null, 0);

                getWorldData = iDataManagerClass.getDeclaredMethod("getWorldData");
                getWorldData.setAccessible(true);

                checkName = worldDataClass.getDeclaredMethod("checkName", String.class);
                checkName.setAccessible(true);

                worldServerSetSpawnFlags = worldNMSClass.getDeclaredMethod("setSpawnFlags", boolean.class, boolean.class);
                worldServerSetSpawnFlags.setAccessible(true);

                worldServerGetWorld = worldServerClass.getDeclaredMethod("b");
                worldServerGetWorld.setAccessible(true);

                worldServerAddIWorldAccess = worldNMSClass.getDeclaredMethod("addIWorldAccess", iWorldAccessClass);
                worldServerAddIWorldAccess.setAccessible(true);

                worldDataSetDifficulty = worldDataClass.getDeclaredMethod("setDifficulty", enumDifficultyClass);
                worldDataSetDifficulty.setAccessible(true);

                nmsWorldGetWorld = worldNMSClass.getDeclaredMethod("getWorld");
                nmsWorldGetWorld.setAccessible(true);

                nmsWorldGetSpawn = worldNMSClass.getDeclaredMethod("getSpawn");
                nmsWorldGetSpawn.setAccessible(true);

                chunkProviderServerGetChunkAt = chunkProviderServerClass.getDeclaredMethod("getChunkAt", int.class, int.class);
                chunkProviderServerGetChunkAt.setAccessible(true);

                blockPositionGetX = baseBlockPositionClass.getDeclaredMethod("getX");
                blockPositionGetX.setAccessible(true);

                blockPositionGetZ = baseBlockPositionClass.getDeclaredMethod("getZ");
                blockPositionGetZ.setAccessible(true);

                //worldServerGetChunkProviderServer = worldServerClass.getDeclaredMethod("getChunkProviderServer");
                //worldServerGetChunkProviderServer.setAccessible(true);
                //end methods

                customizedWorld = worldTypeClass.getDeclaredField("CUSTOMIZED").get(null);
                methodProfiler = minecraftServerClass.getDeclaredField("methodProfiler").get(minecraftServer);

                Field worldsField = craftServerClass.getDeclaredField("worlds");
                worldsField.setAccessible(true);

                craftServerWorlds = (Map<String, World>) worldsField.get(craftServer);

                hardDifficulty = enumDifficultyClass.getDeclaredMethod("getById", int.class).invoke(null, 3);

                System.out.println("Craft server  is null: " + (craftServer == null));
                Method craftGetScoreboardManagerMethod = craftServerClass.getDeclaredMethod("getScoreboardManager");
                craftGetScoreboardManagerMethod.setAccessible(true);

                Object scoreboardManager = craftGetScoreboardManagerMethod.invoke(craftServer);
                System.out.println("scoreboard manager: " + (scoreboardManager == null));
                Object mainScoreboard = craftScoreboardManagerClass.getDeclaredMethod("getMainScoreboard").invoke(scoreboardManager);
                System.out.println("main scoreboard: " + (mainScoreboard == null));
                scoreboard = craftScoreboardClass.getDeclaredMethod("getHandle").invoke(mainScoreboard);

                Field nmsWorldsField = minecraftServerClass.getDeclaredField("worlds");
                nmsWorldsField.setAccessible(true);
                nmsWorlds = (List) nmsWorldsField.get(minecraftServer);

                craftServerConsole = craftServerClass.getDeclaredField("console");

                dataConverterManager = minecraftServerClass.getDeclaredField("dataConverterManager");
                worldServerScoreboard = worldNMSClass.getDeclaredField("scoreboard");
                worldServerTracker = worldServerClass.getDeclaredField("tracker");
                worldServerWorldData = worldNMSClass.getDeclaredField("worldData");

                worldServerDimension = worldServerClass.getDeclaredField("dimension");

                craftServerConsole.setAccessible(true);

                dataConverterManager.setAccessible(true);
                worldServerScoreboard.setAccessible(true);
                worldServerTracker.setAccessible(true);
                worldServerWorldData.setAccessible(true);
                worldServerDimension.setAccessible(true);
            } catch(ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

    }

    public static WorldManager get() {
        if(instance == null) {
            init();
        }
        return instance;
    }

    private final Map<String, World> loadedWorlds;
    private final Map<String, World> failedUnloadingWorlds;

    private WorldManager() {
        this.loadedWorlds = new ConcurrentHashMap<>();
        this.failedUnloadingWorlds = new ConcurrentHashMap<>();
    }

    public World createWorld(String worldName, boolean temporary) {
        //Temporary patch
        World failedUnloadingWorld = this.failedUnloadingWorlds.get(worldName);
        if(failedUnloadingWorld != null) {
            this.failedUnloadingWorlds.remove(worldName);
            return failedUnloadingWorld;
        }

        World created = this.createWorld(WorldCreator
                .name(worldName)
                .generator(new EmptyChunkGenerator())
                .type(WorldType.CUSTOMIZED)
                .environment(Environment.NORMAL)
                .generateStructures(false), temporary);

        created.getWorldBorder().setCenter(0, 0);
        created.getWorldBorder().setSize(10000);
        created.getWorldBorder().setWarningDistance(0);
        this.loadedWorlds.put(created.getName(), created);

        return created;
    }

    @SuppressWarnings("unchecked")
    private World createWorld(final WorldCreator creator, boolean temporary) {
        try {
            //Validate.notNull(creator, "Creator may not be null");
            final String name = creator.name();
            ChunkGenerator generator = creator.generator();
            File folder = InstanceManager.get().getPermanentInstanceFolder();
            if(temporary) {
                folder = InstanceManager.get().getTemporaryInstanceFolder();
            }
            final World world = Bukkit.getServer().getWorld(name);

            if(world != null) {
                return world;
            }
            if(folder.exists() && !folder.isDirectory()) {
                throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
            }
            if(generator == null) {
                throw new Exception("Generator must be specified");
            }

            int dimension = 10 + Bukkit.getServer().getWorlds().size();
            boolean used = false;
            do {
                for(final Object server : nmsWorlds) {
                    used = (worldServerDimension.getInt(server) == dimension);
                    if(used) {
                        ++dimension;
                        break;
                    }
                }
            } while(used);

            Object console = craftServerConsole.get(Bukkit.getServer());
            Object dataConverter = dataConverterManager.get(console);
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "data converter is null: " + (dataConverter == null));

            final Object sdm = serverNBTManagerConstructor.newInstance(folder, name, true, dataConverter);
            final Object worldSettings = worldSettingsConstructor.newInstance(creator.seed(), survivalGamemode, false, false, customizedWorld);//new WorldSettings(creator.seed(), WorldSettings.EnumGamemode.getById(this.getDefaultGameMode().getValue()), generateStructures, hardcore, type);
            setGeneratorSettings.invoke(worldSettings, creator.generatorSettings());
            Object worlddata = worldDataConstructor.newInstance(worldSettings, name);
            //checkName.invoke(worlddata, name);

            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "minecraft server is null: " + (minecraftServer == null));
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "sdm is null: " + (sdm == null));
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "worlddata is null: " + (worlddata == null));
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "dimension is: " + dimension);
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "method profiler is null: " + (methodProfiler == null));
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "creator environment is null: " + (creator.environment() == null));
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "Generator is null: " + (generator == null));
            Object internal = worldServerConstructor.newInstance(minecraftServer, sdm, worlddata, dimension, methodProfiler, creator.environment(), generator);
            internal = worldServerClass.cast(worldServerGetWorld.invoke(internal));

            if(!craftServerWorlds.containsKey(name.toLowerCase())) {
                return null;
            }

            worldServerScoreboard.set(internal, scoreboard);
            worldServerTracker.set(internal, entityTrackerConstructor.newInstance(internal));
            worldServerAddIWorldAccess.invoke(internal, worldManagerConstructor.newInstance(minecraftServer, internal));
            Object worldData = worldServerWorldData.get(internal);

            worldDataSetDifficulty.invoke(worldData, hardDifficulty);
            worldServerSetSpawnFlags.invoke(internal, false, false);
            nmsWorlds.add(internal);

            World internalWorld = (World) nmsWorldGetWorld.invoke(internal);

            if(generator != null) {
                internalWorld.getPopulators().addAll(internalWorld.getGenerator().getDefaultPopulators(internalWorld));
            }

            Bukkit.getServer().getPluginManager().callEvent(new WorldInitEvent(internalWorld));
            Bukkit.getServer().getPluginManager().callEvent(new WorldLoadEvent(internalWorld));
            return internalWorld;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFailedUnloadedWorld(String worldName) {
        return this.failedUnloadingWorlds.get(worldName) != null;
    }

    public Collection<World> getFailedUnloadingWorlds() {
        return this.failedUnloadingWorlds.values();
    }

    public boolean isWorldLoaded(String worldName) {
        return this.loadedWorlds.containsKey(worldName);
    }

    public boolean unloadWorld(World world) {
        boolean unloaded = Bukkit.getServer().unloadWorld(world, true);
        if(!unloaded) {
            BukkitObbyLangPlugin.get().getLogger().log(Level.INFO, "Was unable to unload world: " + world.getName());
            this.failedUnloadingWorlds.put(world.getName(), world);
            return false;
        }

        World unloadedWorld = this.loadedWorlds.remove(world.getName());
        if(unloadedWorld == null)
            return false;

        return unloaded;
    }

    public boolean worldExists(String name, boolean temporary) {
        File folder = InstanceManager.get().getPermanentInstanceFolder();
        if(temporary) {
            folder = InstanceManager.get().getTemporaryInstanceFolder();
        }
        return folder.exists();
    }
}