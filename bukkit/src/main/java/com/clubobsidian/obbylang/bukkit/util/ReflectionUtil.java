package com.clubobsidian.obbylang.bukkit.util;

public final class ReflectionUtil {

    private ReflectionUtil() {
    }

    private static boolean mojangMapped = false;

    static {
        try {
            Class.forName("net.minecraft.server.MinecraftServer");
            mojangMapped = true;
        } catch(ClassNotFoundException e) {
            //Ignore
        }
    }

    public static Class<?> getMinecraftClass(String className) throws ClassNotFoundException {
        if(mojangMapped) {
            return Class.forName("net.minecraft." + className);
        }
        return Class.forName("net.minecraft.server." + VersionUtil.getVersion() + "." + className);
    }

    public static Class<?> getMinecraftClass(String packageName, String className) throws ClassNotFoundException {
        if(mojangMapped) {
            return Class.forName("net.minecraft." + packageName + "." + className);
        }
        return Class.forName("net.minecraft.server." + VersionUtil.getVersion() + "." + className);
    }

    public static Class<?> getCraftClass(String packageName, String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + VersionUtil.getVersion() + "." + packageName + "." + className);
    }

    public static Class<?> getCraftClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + VersionUtil.getVersion() + "." + className);
    }
}