package com.sun.mail.util;

import java.util.Properties;
import javax.mail.Session;

public class PropUtil {
    private PropUtil() {
    }

    public static int getIntProperty(Properties props, String name, int def) {
        return getInt(getProp(props, name), def);
    }

    public static boolean getBooleanProperty(Properties props, String name, boolean def) {
        return getBoolean(getProp(props, name), def);
    }

    @Deprecated
    public static int getIntSessionProperty(Session session, String name, int def) {
        return getInt(getProp(session.getProperties(), name), def);
    }

    @Deprecated
    public static boolean getBooleanSessionProperty(Session session, String name, boolean def) {
        return getBoolean(getProp(session.getProperties(), name), def);
    }

    public static boolean getBooleanSystemProperty(String name, boolean def) {
        try {
            return getBoolean(getProp(System.getProperties(), name), def);
        } catch (SecurityException e) {
            try {
                String value = System.getProperty(name);
                if (value == null) {
                    return def;
                }
                if (!def) {
                    return value.equalsIgnoreCase("true");
                }
                return !value.equalsIgnoreCase("false");
            } catch (SecurityException e2) {
                return def;
            }
        }
    }

    private static Object getProp(Properties props, String name) {
        Object val = props.get(name);
        return val != null ? val : props.getProperty(name);
    }

    private static int getInt(Object value, int def) {
        if (value == null) {
            return def;
        }
        if (value instanceof String) {
            try {
                String s = (String) value;
                if (s.startsWith("0x")) {
                    return Integer.parseInt(s.substring(2), 16);
                }
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
            }
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        return def;
    }

    private static boolean getBoolean(Object value, boolean def) {
        if (value == null) {
            return def;
        }
        if (value instanceof String) {
            if (!def) {
                return ((String) value).equalsIgnoreCase("true");
            }
            return !((String) value).equalsIgnoreCase("false");
        } else if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else {
            return def;
        }
    }
}
