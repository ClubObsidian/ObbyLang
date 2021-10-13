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

package com.clubobsidian.obbylang.discord.command;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;

import java.util.List;

public class ObbyLangCommand extends Command {

    public ObbyLangCommand(String name) {
        super(name);
    }

    @Override
    public boolean onCommand(Pipe pipe, String[] args) {
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("load")) {
                boolean loaded = ScriptManager.get().loadScript(args[1], pipe);
                if(loaded) {
                    pipe.out("Script has been loaded");
                } else {
                    pipe.out("Script could not be loaded");
                }
            } else if(args[0].equalsIgnoreCase("unload")) {
                boolean unloaded = ScriptManager.get().unloadScript(args[1], pipe);
                if(unloaded) {
                    pipe.out("Script has been unloaded");
                } else {
                    pipe.out("Script could not be unloaded");
                }
            } else if(args[0].equalsIgnoreCase("reload")) {
                boolean reload = ScriptManager.get().reloadScript(args[1], pipe);
                if(reload) {
                    pipe.out("Script has been reloaded");
                } else {
                    pipe.out("Script could not be reloaded");
                    pipe.out("Attemping to load the script");
                    boolean load = ScriptManager.get().loadScript(args[1], pipe);
                    if(load) {
                        pipe.out("Script has been loaded");
                    } else {
                        pipe.out("Script could not be loaded");
                    }
                }
            } else if(args[0].equalsIgnoreCase("enable")) {
                boolean enable = ScriptManager.get().enableScript(args[1], pipe);
                if(enable) {
                    pipe.out("Script has been enabled");
                } else {
                    pipe.out("Script can not be enabled");
                }
            } else if(args[0].equalsIgnoreCase("disable")) {
                boolean disable = ScriptManager.get().disableScript(args[1], pipe);
                if(disable) {
                    pipe.out("Script has been disabled");
                } else {
                    pipe.out("Script can not be disabled");
                }
            } else {
                this.sendCommandList(this.getName(), pipe);
            }
            return true;
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("list")) {
                StringBuilder sb = new StringBuilder();
                List<String> scriptNames = ScriptManager.get().getScriptNamesRaw();
                for(int i = 0; i < scriptNames.size(); i++) {
                    sb.append(scriptNames.get(i).replace(".js", ""));
                    if(i != scriptNames.size() - 1) {
                        sb.append(",");
                    }
                }

                String scriptStr = sb.toString();
                pipe.out(scriptStr);
                return true;
            }
        }

        this.sendCommandList(this.getName(), pipe);
        return true;


    }

    private void sendCommandList(String label, Pipe pipe) {
        pipe.out(label + " load <script>");
        pipe.out(label + " unload <script>");
        pipe.out(label + " reload <script>");
        pipe.out(label + " enable <script>");
        pipe.out(label + " disable <script>");
        pipe.out(label + " list");
    }
}