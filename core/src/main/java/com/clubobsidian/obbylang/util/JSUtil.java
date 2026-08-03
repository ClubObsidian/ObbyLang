package com.clubobsidian.obbylang.util;

import com.caoccao.qjs4j.core.*;

public final class JSUtil {

    private JSUtil() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    public static JSValue call(JSFunction function) {
        return call(function, new JSValue[]{});
    }

    public static JSValue call(JSFunction function, JSValue[] args) {
        JSContext context = function.getContext();
        JSValue currentThis = context.getCurrentThis();
        return function.call(context, currentThis, args);
    }

    public static JSArray convertStringArray(JSContext context, String[] stringAr) {
        JSArray jsArray = new JSArray(context, stringAr.length);
        for (int i = 0; i < stringAr.length; i++) {
            jsArray.set(i, new JSString(stringAr[i]));
        }
        return jsArray;
    }
}
