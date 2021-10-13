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

package com.clubobsidian.obbylang.discord.manager.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import net.dv8tion.jda.api.entities.User;

public class DiscordSenderWrapper extends SenderWrapper<User> {

    /*
     * Just a stub, this isn't used
     */

    public DiscordSenderWrapper(User sender) {
        super(sender);
    }

    @Override
    public Object asCommandBlock() {
        return null;
    }

    @Override
    public Object asConsole() {
        return null;
    }

    @Override
    public Object asPlayer() {
        return null;
    }

    @Override
    public boolean isCommandBlock() {
        return false;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public void sendMessage(String message) {
        return;
    }

    @Override
    public void sendMessage(String[] messages) {
        return;
    }
}