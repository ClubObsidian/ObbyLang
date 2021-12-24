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

package com.clubobsidian.obbylang.test.project;

import com.clubobsidian.obbylang.project.PackageResolver;
import com.clubobsidian.obbylang.project.ProjectPackage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PackageResolverTest {

    @Test
    public void testResolve() {
        File rootDir = new File("").getAbsoluteFile();
        Optional<ProjectPackage> projectPackageOptional = PackageResolver.resolve(rootDir);
        assertTrue(projectPackageOptional.isPresent());
        assertNotNull(projectPackageOptional.get());
    }

    @Test
    public void testResolveDoesNotExist() {
        File rootDir = new File("").getAbsoluteFile().getParentFile();
        Optional<ProjectPackage> projectPackageOptional = PackageResolver.resolve(rootDir);
        assertFalse(projectPackageOptional.isPresent());
    }
}