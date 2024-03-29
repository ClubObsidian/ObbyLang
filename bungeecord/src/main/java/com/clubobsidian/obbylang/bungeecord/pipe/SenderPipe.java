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

package com.clubobsidian.obbylang.bungeecord.pipe;

import com.clubobsidian.obbylang.pipe.Pipe;
import net.md_5.bungee.api.CommandSender;

public class SenderPipe implements Pipe {

    private final CommandSender sender;

    public SenderPipe(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void out(String message) {
        this.sender.sendMessage(message);
    }
}