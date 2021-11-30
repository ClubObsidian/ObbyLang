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

package com.clubobsidian.obbylang.util;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;

import java.lang.reflect.Field;

public final class ListenerUtil {

    private ListenerUtil() {
    }

    @SuppressWarnings("rawtypes")
    public static MemberValue getMemberValue(Object obj, ConstPool constPool) {
        MemberValue memberValue = null;
        Class<?> clazz = obj.getClass();
        if(obj instanceof Enum) {
            Enum objEnum = (Enum) obj;
            EnumMemberValue enumMemberValue = new EnumMemberValue(constPool);
            enumMemberValue.setType(obj.getClass().getName());
            enumMemberValue.setValue(objEnum.name());
            memberValue = enumMemberValue;
        } else if(clazz == Byte.class || clazz == byte.class) {
            ByteMemberValue byteMemberValue = new ByteMemberValue(constPool);
            byteMemberValue.setValue((byte) obj);
            memberValue = byteMemberValue;
        }

        return memberValue;
    }

    public static Object getStaticDeclaredField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}