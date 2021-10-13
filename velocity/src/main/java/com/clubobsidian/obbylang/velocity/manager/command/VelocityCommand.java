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

package com.clubobsidian.obbylang.velocity.manager.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.HashMap;
import java.util.Map;

public class VelocityCommand implements SimpleCommand {

    private final Object owner;
    private final String command;
    private final ScriptObjectMirror base;

    public VelocityCommand(Object owner, String command, ScriptObjectMirror base) {
        this.owner = owner;
        this.command = command;
        this.base = base;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        SenderWrapper<?> wrapper = new VelocitySenderWrapper(invocation.source());
        Map<String, Object> properties = new HashMap<>();
        properties.put("args", args);
        properties.put("sender", wrapper);

        this.base.call(this.owner, wrapper, this, this.command, args);
    }
}