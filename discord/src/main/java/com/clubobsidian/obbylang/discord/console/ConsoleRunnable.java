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

package com.clubobsidian.obbylang.discord.console;

import com.clubobsidian.obbylang.discord.plugin.DiscordObbyLangPlugin;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

public class ConsoleRunnable implements Runnable {

    @Override
    public void run() {
        LineReader reader = LineReaderBuilder.builder().build();
        String prompt = "> ";
        DiscordObbyLangPlugin plugin = DiscordObbyLangPlugin.get();

        while(plugin.isRunning().get()) {
            String line = null;
            try {
                line = reader.readLine(prompt);
                plugin.getConsoleCommandManager().dispatchCommand(line);
            } catch(UserInterruptException e) {
            } catch(EndOfFileException e) {
                return;
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}