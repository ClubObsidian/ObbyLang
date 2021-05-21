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