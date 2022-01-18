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

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class BukkitCommand extends Command implements CommandExecutor {

    private static Class<?> subwrapperClazz;

    private static void createWrapperClazz() {
        if(subwrapperClazz == null) {
            subwrapperClazz = new ByteBuddy()
                    .subclass(BukkitSenderWrapper.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS.withInheritedAnnotations())
                    .method(ElementMatchers.isAbstract())
                    .intercept(MethodDelegation.to(new CommandInterceptor()))
                    .make()
                    .load(BukkitCommand.class.getClassLoader())
                    .getLoaded();
        }
    }

    public static SenderWrapper<?> createWrapper(CommandSender sender) {
        createWrapperClazz();
        try {
            return (SenderWrapper<?>) subwrapperClazz.getDeclaredConstructor(CommandSender.class).newInstance(sender);
        } catch(InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final Object owner;
    private final String command;
    private final ScriptObjectMirror base;
    private TabCompleter tabCompleter;

    public BukkitCommand(Object owner, String command, ScriptObjectMirror base) {
        super(command);
        this.owner = owner;
        this.command = command;
        this.base = base;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase(this.command)) {
            SenderWrapper<?> wrapper = createWrapper(sender);
            Object ret = this.base.call(this.owner, wrapper, command, label, args);
            if(ret != null && ret instanceof Boolean) {
                return (boolean) ret;
            }
        }
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return this.onCommand(sender, this, commandLabel, args);
    }

    public void setTabCompleter(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if(this.tabCompleter == null) {
            return super.tabComplete(sender, alias, args);
        }
        return this.tabCompleter.onTabComplete(sender, this, alias, args);
    }
}