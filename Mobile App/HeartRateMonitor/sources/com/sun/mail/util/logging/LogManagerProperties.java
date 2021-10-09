package com.sun.mail.util.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.LoggingPermission;

final class LogManagerProperties extends Properties {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final Object LOG_MANAGER = loadLogManager();
    private static final Method LR_GET_INSTANT;
    private static final Method LR_GET_LONG_TID;
    private static volatile String[] REFLECT_NAMES = null;
    private static final Method ZDT_OF_INSTANT;
    private static final Method ZI_SYSTEM_DEFAULT;
    private static final long serialVersionUID = -2239983349056806252L;
    private final String prefix;

    static {
        boolean z = true;
        if (LogManagerProperties.class.desiredAssertionStatus()) {
            z = false;
        }
        $assertionsDisabled = z;
        Method lrtid = null;
        try {
            lrtid = LogRecord.class.getMethod("getLongThreadID", new Class[0]);
        } catch (Exception | LinkageError | RuntimeException e) {
        }
        LR_GET_LONG_TID = lrtid;
        Method lrgi = null;
        Method zisd = null;
        Method zdtoi = null;
        try {
            lrgi = LogRecord.class.getMethod("getInstant", new Class[0]);
            if ($assertionsDisabled || Comparable.class.isAssignableFrom(lrgi.getReturnType())) {
                zisd = findClass("java.time.ZoneId").getMethod("systemDefault", new Class[0]);
                if (!Modifier.isStatic(zisd.getModifiers())) {
                    Method zisd2 = null;
                    throw new NoSuchMethodException(zisd2.toString());
                }
                zdtoi = findClass("java.time.ZonedDateTime").getMethod("ofInstant", new Class[]{findClass("java.time.Instant"), findClass("java.time.ZoneId")});
                if (!Modifier.isStatic(zdtoi.getModifiers()) || !Comparable.class.isAssignableFrom(zdtoi.getReturnType())) {
                    Method zdtoi2 = null;
                    throw new NoSuchMethodException(zdtoi2.toString());
                }
                if (lrgi == null || zisd == null || zdtoi == null) {
                    lrgi = null;
                    zisd = null;
                    zdtoi = null;
                }
                LR_GET_INSTANT = lrgi;
                ZI_SYSTEM_DEFAULT = zisd;
                ZDT_OF_INSTANT = zdtoi;
                return;
            }
            throw new AssertionError(lrgi);
        } catch (RuntimeException e2) {
            if (lrgi == null || 0 == 0 || 0 == 0) {
                lrgi = null;
                zisd = null;
                zdtoi = null;
            }
        } catch (Exception e3) {
            if (lrgi == null || 0 == 0 || 0 == 0) {
                lrgi = null;
                zisd = null;
                zdtoi = null;
            }
        } catch (LinkageError e4) {
            if (lrgi == null || 0 == 0 || 0 == 0) {
                lrgi = null;
                zisd = null;
                zdtoi = null;
            }
        } catch (Throwable th) {
            if (lrgi == null || 0 == 0 || 0 == 0) {
            }
            throw th;
        }
    }

    private static Object loadLogManager() {
        try {
            return LogManager.getLogManager();
        } catch (LinkageError e) {
            return readConfiguration();
        } catch (RuntimeException e2) {
            return readConfiguration();
        }
    }

    private static Properties readConfiguration() {
        InputStream in;
        Properties props = new Properties();
        try {
            String n = System.getProperty("java.util.logging.config.file");
            if (n != null) {
                in = new FileInputStream(new File(n).getCanonicalFile());
                props.load(in);
                in.close();
            }
        } catch (Exception | LinkageError | RuntimeException e) {
        } catch (Throwable th) {
            in.close();
            throw th;
        }
        return props;
    }

    static String fromLogManager(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        Object m = LOG_MANAGER;
        try {
            if (m instanceof Properties) {
                return ((Properties) m).getProperty(name);
            }
        } catch (RuntimeException e) {
        }
        if (m != null) {
            try {
                if (m instanceof LogManager) {
                    return ((LogManager) m).getProperty(name);
                }
            } catch (LinkageError | RuntimeException e2) {
            }
        }
        return null;
    }

    static void checkLogManagerAccess() {
        boolean checked = $assertionsDisabled;
        Object m = LOG_MANAGER;
        if (m != null) {
            try {
                if (m instanceof LogManager) {
                    checked = true;
                    ((LogManager) m).checkAccess();
                }
            } catch (SecurityException notAllowed) {
                if (0 != 0) {
                    throw notAllowed;
                }
            } catch (LinkageError | RuntimeException e) {
            }
        }
        if (!checked) {
            checkLoggingAccess();
        }
    }

    private static void checkLoggingAccess() {
        SecurityManager sm;
        boolean checked = $assertionsDisabled;
        Logger global = Logger.getLogger("global");
        try {
            if (Logger.class == global.getClass()) {
                global.removeHandler((Handler) null);
                checked = true;
            }
        } catch (NullPointerException e) {
        }
        if (!checked && (sm = System.getSecurityManager()) != null) {
            sm.checkPermission(new LoggingPermission("control", (String) null));
        }
    }

    static boolean hasLogManager() {
        Object m = LOG_MANAGER;
        if (m == null || (m instanceof Properties)) {
            return $assertionsDisabled;
        }
        return true;
    }

    static Comparable<?> getZonedDateTime(LogRecord record) {
        if (record == null) {
            throw new NullPointerException();
        }
        Method m = ZDT_OF_INSTANT;
        if (m != null) {
            try {
                return (Comparable) m.invoke((Object) null, new Object[]{LR_GET_INSTANT.invoke(record, new Object[0]), ZI_SYSTEM_DEFAULT.invoke((Object) null, new Object[0])});
            } catch (RuntimeException ignore) {
                if (!$assertionsDisabled && (LR_GET_INSTANT == null || ZI_SYSTEM_DEFAULT == null)) {
                    throw new AssertionError(ignore);
                }
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof Error) {
                    throw ((Error) cause);
                } else if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else {
                    throw new UndeclaredThrowableException(ite);
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    static Long getLongThreadID(LogRecord record) {
        if (record == null) {
            throw new NullPointerException();
        }
        Method m = LR_GET_LONG_TID;
        if (m != null) {
            try {
                return (Long) m.invoke(record, new Object[0]);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof Error) {
                    throw ((Error) cause);
                } else if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else {
                    throw new UndeclaredThrowableException(ite);
                }
            } catch (Exception | RuntimeException e) {
            }
        }
        return null;
    }

    static String getLocalHost(Object s) throws Exception {
        try {
            Method m = s.getClass().getMethod("getLocalHost", new Class[0]);
            if (!Modifier.isStatic(m.getModifiers()) && m.getReturnType() == String.class) {
                return (String) m.invoke(s, new Object[0]);
            }
            throw new NoSuchMethodException(m.toString());
        } catch (ExceptionInInitializerError EIIE) {
            throw wrapOrThrow(EIIE);
        } catch (InvocationTargetException ite) {
            throw paramOrError(ite);
        }
    }

    static long parseDurationToMillis(CharSequence value) throws Exception {
        if (value == null) {
            throw new NullPointerException();
        }
        try {
            Class<?> k = findClass("java.time.Duration");
            Method parse = k.getMethod("parse", new Class[]{CharSequence.class});
            if (!k.isAssignableFrom(parse.getReturnType()) || !Modifier.isStatic(parse.getModifiers())) {
                throw new NoSuchMethodException(parse.toString());
            }
            Method toMillis = k.getMethod("toMillis", new Class[0]);
            if (!Long.TYPE.isAssignableFrom(toMillis.getReturnType()) || Modifier.isStatic(toMillis.getModifiers())) {
                throw new NoSuchMethodException(toMillis.toString());
            }
            return ((Long) toMillis.invoke(parse.invoke((Object) null, new Object[]{value}), new Object[0])).longValue();
        } catch (ExceptionInInitializerError EIIE) {
            throw wrapOrThrow(EIIE);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof ArithmeticException) {
                throw ((ArithmeticException) cause);
            }
            throw paramOrError(ite);
        }
    }

    static String toLanguageTag(Locale locale) {
        String l = locale.getLanguage();
        String c = locale.getCountry();
        String v = locale.getVariant();
        char[] b = new char[(l.length() + c.length() + v.length() + 2)];
        int count = l.length();
        l.getChars(0, count, b, 0);
        if (!(c.length() == 0 && (l.length() == 0 || v.length() == 0))) {
            b[count] = '-';
            int count2 = count + 1;
            c.getChars(0, c.length(), b, count2);
            count = count2 + c.length();
        }
        if (!(v.length() == 0 || (l.length() == 0 && c.length() == 0))) {
            b[count] = '-';
            int count3 = count + 1;
            v.getChars(0, v.length(), b, count3);
            count = count3 + v.length();
        }
        return String.valueOf(b, 0, count);
    }

    static Filter newFilter(String name) throws Exception {
        return (Filter) newObjectFrom(name, Filter.class);
    }

    static Formatter newFormatter(String name) throws Exception {
        return (Formatter) newObjectFrom(name, Formatter.class);
    }

    static Comparator<? super LogRecord> newComparator(String name) throws Exception {
        return (Comparator) newObjectFrom(name, Comparator.class);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v9, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.util.Comparator<T>} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static <T> java.util.Comparator<T> reverseOrder(java.util.Comparator<T> r8) {
        /*
            if (r8 != 0) goto L_0x0008
            java.lang.NullPointerException r5 = new java.lang.NullPointerException
            r5.<init>()
            throw r5
        L_0x0008:
            r4 = 0
            java.lang.Class r5 = r8.getClass()     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            java.lang.String r6 = "reversed"
            r7 = 0
            java.lang.Class[] r7 = new java.lang.Class[r7]     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            java.lang.reflect.Method r3 = r5.getMethod(r6, r7)     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            int r5 = r3.getModifiers()     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            boolean r5 = java.lang.reflect.Modifier.isStatic(r5)     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            if (r5 != 0) goto L_0x0037
            java.lang.Class<java.util.Comparator> r5 = java.util.Comparator.class
            java.lang.Class r6 = r3.getReturnType()     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            boolean r5 = r5.isAssignableFrom(r6)     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            if (r5 == 0) goto L_0x0037
            r5 = 0
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ ExceptionInInitializerError -> 0x003e }
            java.lang.Object r5 = r3.invoke(r8, r5)     // Catch:{ ExceptionInInitializerError -> 0x003e }
            r0 = r5
            java.util.Comparator r0 = (java.util.Comparator) r0     // Catch:{ ExceptionInInitializerError -> 0x003e }
            r4 = r0
        L_0x0037:
            if (r4 != 0) goto L_0x003d
            java.util.Comparator r4 = java.util.Collections.reverseOrder(r8)
        L_0x003d:
            return r4
        L_0x003e:
            r1 = move-exception
            java.lang.reflect.InvocationTargetException r5 = wrapOrThrow(r1)     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
            throw r5     // Catch:{ InvocationTargetException -> 0x0044, ReflectiveOperationException -> 0x004b, RuntimeException -> 0x0049 }
        L_0x0044:
            r2 = move-exception
            paramOrError(r2)
            goto L_0x0037
        L_0x0049:
            r5 = move-exception
            goto L_0x0037
        L_0x004b:
            r5 = move-exception
            goto L_0x0037
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.LogManagerProperties.reverseOrder(java.util.Comparator):java.util.Comparator");
    }

    static ErrorManager newErrorManager(String name) throws Exception {
        return (ErrorManager) newObjectFrom(name, ErrorManager.class);
    }

    static boolean isStaticUtilityClass(String name) throws Exception {
        Class<?> c = findClass(name);
        Class<Object> cls = Object.class;
        if (c != cls) {
            Method[] methods = c.getMethods();
            if (methods.length != 0) {
                for (Method m : methods) {
                    if (m.getDeclaringClass() != cls && !Modifier.isStatic(m.getModifiers())) {
                        return $assertionsDisabled;
                    }
                }
                return true;
            }
        }
        return $assertionsDisabled;
    }

    static boolean isReflectionClass(String name) throws Exception {
        String[] names = REFLECT_NAMES;
        if (names == null) {
            names = reflectionClassNames();
            REFLECT_NAMES = names;
        }
        for (String rf : names) {
            if (name.equals(rf)) {
                return true;
            }
        }
        findClass(name);
        return $assertionsDisabled;
    }

    private static String[] reflectionClassNames() throws Exception {
        Class<LogManagerProperties> cls = LogManagerProperties.class;
        if ($assertionsDisabled || Modifier.isFinal(cls.getModifiers())) {
            try {
                HashSet<String> traces = new HashSet<>();
                Throwable t = Throwable.class.getConstructor(new Class[0]).newInstance(new Object[0]);
                for (StackTraceElement ste : t.getStackTrace()) {
                    if (cls.getName().equals(ste.getClassName())) {
                        break;
                    }
                    traces.add(ste.getClassName());
                }
                Throwable.class.getMethod("fillInStackTrace", new Class[0]).invoke(t, new Object[0]);
                for (StackTraceElement ste2 : t.getStackTrace()) {
                    if (cls.getName().equals(ste2.getClassName())) {
                        break;
                    }
                    traces.add(ste2.getClassName());
                }
                return (String[]) traces.toArray(new String[traces.size()]);
            } catch (InvocationTargetException ITE) {
                throw paramOrError(ITE);
            }
        } else {
            throw new AssertionError(cls);
        }
    }

    static <T> T newObjectFrom(String name, Class<T> type) throws Exception {
        try {
            Class<?> clazz = findClass(name);
            if (type.isAssignableFrom(clazz)) {
                return type.cast(clazz.getConstructor(new Class[0]).newInstance(new Object[0]));
            }
            throw new ClassCastException(clazz.getName() + " cannot be cast to " + type.getName());
        } catch (InvocationTargetException ITE) {
            throw paramOrError(ITE);
        } catch (NoClassDefFoundError NCDFE) {
            throw new ClassNotFoundException(NCDFE.toString(), NCDFE);
        } catch (ExceptionInInitializerError EIIE) {
            throw wrapOrThrow(EIIE);
        }
    }

    private static Exception paramOrError(InvocationTargetException ite) {
        Throwable cause = ite.getCause();
        if (cause == null || (!(cause instanceof VirtualMachineError) && !(cause instanceof ThreadDeath))) {
            return ite;
        }
        throw ((Error) cause);
    }

    private static InvocationTargetException wrapOrThrow(ExceptionInInitializerError eiie) {
        if (!(eiie.getCause() instanceof Error)) {
            return new InvocationTargetException(eiie);
        }
        throw eiie;
    }

    private static Class<?> findClass(String name) throws ClassNotFoundException {
        ClassLoader[] loaders = getClassLoaders();
        if (!$assertionsDisabled && loaders.length != 2) {
            throw new AssertionError(loaders.length);
        } else if (loaders[0] == null) {
            return tryLoad(name, loaders[1]);
        } else {
            try {
                return Class.forName(name, $assertionsDisabled, loaders[0]);
            } catch (ClassNotFoundException e) {
                return tryLoad(name, loaders[1]);
            }
        }
    }

    private static Class<?> tryLoad(String name, ClassLoader l) throws ClassNotFoundException {
        if (l != null) {
            return Class.forName(name, $assertionsDisabled, l);
        }
        return Class.forName(name);
    }

    private static ClassLoader[] getClassLoaders() {
        return (ClassLoader[]) AccessController.doPrivileged(new PrivilegedAction<ClassLoader[]>() {
            public ClassLoader[] run() {
                ClassLoader[] loaders = new ClassLoader[2];
                try {
                    loaders[0] = ClassLoader.getSystemClassLoader();
                } catch (SecurityException e) {
                    loaders[0] = null;
                }
                try {
                    loaders[1] = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e2) {
                    loaders[1] = null;
                }
                return loaders;
            }
        });
    }

    LogManagerProperties(Properties parent, String prefix2) {
        super(parent);
        if (parent == null || prefix2 == null) {
            throw new NullPointerException();
        }
        this.prefix = prefix2;
    }

    public synchronized Object clone() {
        return exportCopy(this.defaults);
    }

    public synchronized String getProperty(String key) {
        String value;
        value = this.defaults.getProperty(key);
        if (value == null) {
            if (key.length() > 0) {
                value = fromLogManager(this.prefix + '.' + key);
            }
            if (value == null) {
                value = fromLogManager(key);
            }
            if (value != null) {
                super.put(key, value);
            } else {
                Object v = super.get(key);
                value = v instanceof String ? (String) v : null;
            }
        }
        return value;
    }

    public String getProperty(String key, String def) {
        String value = getProperty(key);
        return value == null ? def : value;
    }

    public synchronized Object get(Object key) {
        Object value;
        if (key instanceof String) {
            value = getProperty((String) key);
        } else {
            value = null;
        }
        if (value == null && (value = this.defaults.get(key)) == null && !this.defaults.containsKey(key)) {
            value = super.get(key);
        }
        return value;
    }

    public synchronized Object put(Object key, Object value) {
        Object def;
        if (!(key instanceof String) || !(value instanceof String)) {
            def = super.put(key, value);
        } else {
            def = preWrite(key);
            Object man = super.put(key, value);
            if (man != null) {
                def = man;
            }
        }
        return def;
    }

    public Object setProperty(String key, String value) {
        return put(key, value);
    }

    public synchronized boolean containsKey(Object key) {
        boolean found;
        if (!(key instanceof String) || getProperty((String) key) == null) {
            found = false;
        } else {
            found = true;
        }
        if (!found) {
            if (this.defaults.containsKey(key) || super.containsKey(key)) {
                found = true;
            } else {
                found = false;
            }
        }
        return found;
    }

    public synchronized Object remove(Object key) {
        Object def;
        def = preWrite(key);
        Object man = super.remove(key);
        if (man != null) {
            def = man;
        }
        return def;
    }

    public Enumeration<?> propertyNames() {
        if ($assertionsDisabled) {
            return super.propertyNames();
        }
        throw new AssertionError();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return $assertionsDisabled;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof Properties)) {
            return $assertionsDisabled;
        }
        if ($assertionsDisabled) {
            return super.equals(o);
        }
        throw new AssertionError(this.prefix);
    }

    public int hashCode() {
        if ($assertionsDisabled) {
            return super.hashCode();
        }
        throw new AssertionError(this.prefix.hashCode());
    }

    private Object preWrite(Object key) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            return get(key);
        }
        throw new AssertionError();
    }

    private Properties exportCopy(Properties parent) {
        Thread.holdsLock(this);
        Properties child = new Properties(parent);
        child.putAll(this);
        return child;
    }

    private synchronized Object writeReplace() throws ObjectStreamException {
        if (!$assertionsDisabled) {
            throw new AssertionError();
        }
        return exportCopy((Properties) this.defaults.clone());
    }
}
