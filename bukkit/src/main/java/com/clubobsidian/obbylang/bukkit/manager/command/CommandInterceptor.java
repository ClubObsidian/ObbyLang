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

package com.clubobsidian.obbylang.bukkit.manager.command;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandInterceptor {

    private static Class<?>[] argsToClasses(Object[] args) {
        Class<?>[] clazzes = new Class[args.length];
        for(int i = 0; i < args.length; i++) {
            clazzes[i] = args[i].getClass();
        }
        return clazzes;
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>[] types) {
        try {
            return clazz.getDeclaredMethod(methodName, types);
        } catch(NoSuchMethodException e) {
            try {
                return clazz.getMethod(methodName, types);
            } catch(NoSuchMethodException ex) {
                return null;
            }
        }
    }

    @RuntimeType
    public Object intercept(@AllArguments Object[] args,
                            @Origin Method method,
                            @This BukkitSenderWrapper wrapper) {
        try {
            //Maybe we can cache this lookup later
            Object sender = wrapper.getOriginalSender();
            Method original = getMethod(sender.getClass(), method.getName(), argsToClasses(args));
            return original.invoke(sender, args);
        } catch(IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
