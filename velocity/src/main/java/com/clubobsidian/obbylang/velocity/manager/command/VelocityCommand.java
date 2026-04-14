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

package com.clubobsidian.obbylang.velocity.manager.command;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSFunction;
import com.caoccao.qjs4j.core.JSString;
import com.caoccao.qjs4j.core.JSValue;
import com.clubobsidian.obbylang.compat.NashornJavaCompat;
import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.clubobsidian.obbylang.util.JSUtil;
import com.velocitypowered.api.command.SimpleCommand;

public class VelocityCommand implements SimpleCommand {

    private final String declaringClass;
    private final String command;
    private final JSFunction script;

    public VelocityCommand(String declaringClass, String command, JSFunction script) {
        this.declaringClass = declaringClass;
        this.command = command;
        this.script = script;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        SenderWrapper<?> wrapper = new VelocitySenderWrapper(invocation.source());
        JSContext context = this.script.getContext();
        JSUtil.call(this.script, new JSValue[]{
                NashornJavaCompat.wrapJavaObject(this.declaringClass, wrapper),
                NashornJavaCompat.wrapJavaObject(this.declaringClass, this),
                new JSString(this.command),
                JSUtil.convertStringArray(context, args)
        });
    }
}