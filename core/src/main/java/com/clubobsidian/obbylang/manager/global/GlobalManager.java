package com.clubobsidian.obbylang.manager.global;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyInstantiable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.HashMap;
import java.util.Map;

public class GlobalManager {

    private static GlobalManager instance;

    public static GlobalManager get() {
        if(instance == null) {
            instance = new GlobalManager();
        }
        return instance;
    }

    private final Map<String, Object> globals;

    private GlobalManager() {
        this.globals = new HashMap<>();
    }

    public void set(String name, Value passed) {
        Object wrapped = this.createSharable(passed);
        this.globals.put(name, wrapped);
    }

    public Object get(String name) {
        return this.globals.get(name);
    }

    public void remove(String name) {
        this.globals.remove(name);
    }

    public Map<String, Object> getGlobals() {
        return this.globals;
    }

    private Object createSharable(Value v) {
        if (v.isNull()) {
            return null;
        } else if (v.isBoolean()) {
            return v.asBoolean();
        } else if (v.isNumber()) {
            return v.as(Number.class);
        } else if (v.isString()) {
            return v.asString();
        } else if (v.isHostObject()) {
            return v.asHostObject();
        }  else if (v.isProxyObject()) {
            return v.asProxyObject();
        } else if (v.hasArrayElements()) {
            return new ProxyArray() {
                @Override
                public void set(long index, Value value) {
                    v.setArrayElement(index, createSharable(value));
                }

                @Override
                public long getSize() {
                    return v.getArraySize();
                }

                @Override
                public Object get(long index) {
                    return createSharable(v.getArrayElement(index));
                }

                @Override
                public boolean remove(long index) {
                    return v.removeArrayElement(index);
                }
            };
        } else if (v.hasMembers()) {
            if (v.canExecute()) {
                if (v.canInstantiate()) {
                    // js functions have members, can be executed and are instantiable
                    return new SharableMembersAndInstantiable(v);
                } else {
                    return new SharableMembersAndExecutable(v);
                }
            } else {
                return new SharableMembers(v);
            }
        } else {
            throw new AssertionError("Uncovered shared value. ");
        }
    }

    class SharableMembers implements ProxyObject {
        final Value v;
        SharableMembers(Value v) {
            this.v = v;
        }

        @Override
        public void putMember(String key, Value value) {
            v.putMember(key, createSharable(value));
        }

        @Override
        public boolean hasMember(String key) {
            return v.hasMember(key);
        }

        @Override
        public Object getMemberKeys() {
            return v.getMemberKeys().toArray(new String[0]);
        }

        @Override
        public Object getMember(String key) {
            return createSharable(v.getMember(key));
        }

        @Override
        public boolean removeMember(String key) {
            return v.removeMember(key);
        }
    }

    class SharableMembersAndExecutable extends SharableMembers implements ProxyExecutable {

        SharableMembersAndExecutable(Value v) {
            super(v);
        }

        @Override
        public Object execute(Value... arguments) {
            Object[] newArgs = new Value[arguments.length];
            for (int i = 0; i < newArgs.length; i++) {
                newArgs[i] = createSharable(arguments[i]);
            }
            return createSharable(v.execute(newArgs));
        }

    }

    class SharableMembersAndInstantiable extends SharableMembersAndExecutable implements ProxyInstantiable {

        SharableMembersAndInstantiable(Value v) {
            super(v);
        }

        @Override
        public Object newInstance(Value... arguments) {
            Object[] newArgs = new Value[arguments.length];
            for (int i = 0; i < newArgs.length; i++) {
                newArgs[i] = createSharable(arguments[i]);
            }
            return createSharable(v.execute(newArgs));
        }

    }
}