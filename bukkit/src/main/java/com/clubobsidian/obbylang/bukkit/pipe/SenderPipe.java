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

package com.clubobsidian.obbylang.bukkit.pipe;

import com.clubobsidian.obbylang.pipe.Pipe;
import org.bukkit.command.CommandSender;

public class SenderPipe implements Pipe {

    private final CommandSender sender;

    public SenderPipe(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void out(String msg) {
        this.sender.sendMessage(msg);
    }
}