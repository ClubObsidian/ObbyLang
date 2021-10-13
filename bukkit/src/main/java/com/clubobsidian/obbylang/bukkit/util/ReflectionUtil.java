/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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