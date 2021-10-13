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

package com.clubobsidian.obbylang.manager.command;

public abstract class SenderWrapper<T> {

    private final T sender;

    public SenderWrapper(T sender) {
        this.sender = sender;
    }

    public abstract boolean isPlayer();

    public abstract boolean isConsole();

    public abstract boolean isCommandBlock();

    public abstract Object asPlayer();

    public abstract Object asConsole();

    public abstract Object asCommandBlock();

    public abstract void sendMessage(String message);

    public abstract void sendMessage(String[] messages);

    public T getOriginalSender() {
        return this.sender;
    }
}