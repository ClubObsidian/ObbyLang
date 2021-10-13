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

package com.clubobsidian.obbylang.util;

public enum ChatColor {

    AQUA('b'),
    BLACK('0'),
    BLUE('9'),
    DARK_AQUA('3'),
    DARK_BLUE('1'),
    DARK_GRAY('8'),
    DARK_GREEN('2'),
    DARK_PURPLE('5'),
    DARK_RED('4'),
    GOLD('6'),
    GRAY('7'),
    GREEN('a'),
    LIGHT_PURPLE('d'),
    RED('c'),
    WHITE('f'),
    YELLOW('e'),
    //Formatting
    BOLD('l', true),
    ITALIC('o', true),
    MAGIC('k', true),
    RESET('r', true),
    STRIKETHROUGH('m', true),
    UNDERLINE('n', true);

    public static final char FORMATTING_CODE = '\u00A7';

    private final char colorCode;
    private final boolean formatting;

    ChatColor(char colorCode) {
        this(colorCode, false);
    }

    ChatColor(char colorCode, boolean formatting) {
        this.colorCode = colorCode;
        this.formatting = formatting;
    }

    public char getColorCode() {
        return this.colorCode;
    }

    public boolean isColor() {
        return !this.isFormatting();
    }

    public boolean isFormatting() {
        return this.formatting;
    }

    public static String translateAlternateColorCodes(char translate, String message) {
        char[] chars = message.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            if(chars[i] == translate) {
                if(i + 1 < chars.length) {
                    for(ChatColor color : ChatColor.values()) {
                        if(chars[i + 1] == color.getColorCode()) {
                            chars[i] = ChatColor.FORMATTING_CODE;
                        }
                    }
                }
            }
        }
        return String.valueOf(chars);
    }

    public static String translateAlternateColorCodes(String message) {
        return translateAlternateColorCodes('&', message);
    }

    @Override
    public String toString() {
        return ChatColor.FORMATTING_CODE + "" + this.getColorCode();
    }
}