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

package com.clubobsidian.obbylang.project;

import com.clubobsidian.wrappy.Configuration;

import java.io.File;
import java.util.Optional;

public final class PackageResolver {

    public static Optional<ProjectPackage> resolve(File directory) {
        for(File file : directory.listFiles()) {
            if(file.getName().startsWith("package.") && hasExtension(file)) {
                ProjectPackage projectPackage = new ProjectPackage();
                Configuration.load(file).inject(projectPackage);
                return Optional.of(projectPackage);
            }
        }
        return Optional.empty();
    }

    private static boolean hasExtension(File file) {
        String name = file.getName();
        return name.endsWith("yml") || name.endsWith("conf") || name.endsWith("json");
    }

    private PackageResolver() {
    }
}