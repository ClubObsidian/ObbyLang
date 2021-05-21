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