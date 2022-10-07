/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.bukkit.util;

import java.lang.reflect.Method;

public final class ReflectionUtil {

    private static final boolean MOJANG_MAPPED = checkMojangMapped();

    private static boolean checkMojangMapped() {
        try {
            Class.forName("net.minecraft.server.MinecraftServer");
            return true;
        } catch(ClassNotFoundException e) {
            return false;
        }
    }

    public static Class<?> getNmsClass(String className) throws ClassNotFoundException {
        if(MOJANG_MAPPED) {
            return Class.forName("net.minecraft." + className);
        }
        return Class.forName("net.minecraft.server." + VersionUtil.getVersion() + "." + className);
    }

    public static Class<?> getNmsClass(String packageName, String className) throws ClassNotFoundException {
        if(MOJANG_MAPPED) {
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

    public static Method getMethod(Class<?> cl, String... methods) {
        for (Method m : cl.getDeclaredMethods()) {
            for (String methodName : methods) {
                if(m.getName().equals(methodName)) {
                    return m;
                }
            }
        }
        return null;
    }

    private ReflectionUtil() {
    }
}