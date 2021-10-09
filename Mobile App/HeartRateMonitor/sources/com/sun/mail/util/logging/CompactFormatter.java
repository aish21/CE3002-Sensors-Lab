package com.sun.mail.util.logging;

import java.util.Date;
import java.util.Formattable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import javax.mail.UIDFolder;

public class CompactFormatter extends Formatter {
    private final String fmt;

    static {
        loadDeclaredClasses();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: java.lang.Class<?>[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.Class<?>[] loadDeclaredClasses() {
        /*
            r0 = 1
            java.lang.Class[] r0 = new java.lang.Class[r0]
            r1 = 0
            java.lang.Class<com.sun.mail.util.logging.CompactFormatter$Alternate> r2 = com.sun.mail.util.logging.CompactFormatter.Alternate.class
            r0[r1] = r2
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.CompactFormatter.loadDeclaredClasses():java.lang.Class[]");
    }

    public CompactFormatter() {
        this.fmt = initFormat(getClass().getName());
    }

    public CompactFormatter(String format) {
        this.fmt = format == null ? initFormat(getClass().getName()) : format;
    }

    public String format(LogRecord record) {
        ResourceBundle rb = record.getResourceBundle();
        Locale l = rb == null ? null : rb.getLocale();
        String msg = formatMessage(record);
        String thrown = formatThrown(record);
        String err = formatError(record);
        Object[] params = {formatZonedDateTime(record), formatSource(record), formatLoggerName(record), formatLevel(record), msg, thrown, new Alternate(msg, thrown), new Alternate(thrown, msg), Long.valueOf(record.getSequenceNumber()), formatThreadID(record), err, new Alternate(msg, err), new Alternate(err, msg), formatBackTrace(record), record.getResourceBundleName(), record.getMessage()};
        if (l == null) {
            return String.format(this.fmt, params);
        }
        return String.format(l, this.fmt, params);
    }

    public String formatMessage(LogRecord record) {
        return replaceClassName(replaceClassName(super.formatMessage(record), record.getThrown()), record.getParameters());
    }

    public String formatMessage(Throwable t) {
        String r;
        if (t == null) {
            return "";
        }
        Throwable apply = apply(t);
        String m = apply.getLocalizedMessage();
        String s = apply.toString();
        String sn = simpleClassName(apply.getClass());
        if (isNullOrSpaces(m)) {
            r = replaceClassName(simpleClassName(s), t);
        } else if (!s.contains(m)) {
            r = replaceClassName(simpleClassName(s) + ": " + m, t);
        } else if (s.startsWith(apply.getClass().getName()) || s.startsWith(sn)) {
            r = replaceClassName(m, t);
        } else {
            r = replaceClassName(simpleClassName(s), t);
        }
        if (!r.contains(sn)) {
            return sn + ": " + r;
        }
        return r;
    }

    public String formatLevel(LogRecord record) {
        return record.getLevel().getLocalizedName();
    }

    public String formatSource(LogRecord record) {
        String source = record.getSourceClassName();
        if (source == null) {
            return simpleClassName(record.getLoggerName());
        }
        if (record.getSourceMethodName() != null) {
            return simpleClassName(source) + " " + record.getSourceMethodName();
        }
        return simpleClassName(source);
    }

    public String formatLoggerName(LogRecord record) {
        return simpleClassName(record.getLoggerName());
    }

    public Number formatThreadID(LogRecord record) {
        Long id = LogManagerProperties.getLongThreadID(record);
        if (id == null) {
            return Long.valueOf(((long) record.getThreadID()) & UIDFolder.MAXUID);
        }
        return id;
    }

    public String formatThrown(LogRecord record) {
        Throwable t = record.getThrown();
        if (t == null) {
            return "";
        }
        String site = formatBackTrace(record);
        return formatMessage(t) + (isNullOrSpaces(site) ? "" : ' ' + site);
    }

    public String formatError(LogRecord record) {
        return formatMessage(record.getThrown());
    }

    public String formatBackTrace(LogRecord record) {
        Throwable t = record.getThrown();
        if (t == null) {
            return "";
        }
        StackTraceElement[] trace = apply(t).getStackTrace();
        String site = findAndFormat(trace);
        if (!isNullOrSpaces(site)) {
            return site;
        }
        int limit = 0;
        for (Throwable c = t; c != null; c = c.getCause()) {
            StackTraceElement[] ste = c.getStackTrace();
            site = findAndFormat(ste);
            if (!isNullOrSpaces(site)) {
                break;
            }
            if (trace.length == 0) {
                trace = ste;
            }
            limit++;
            if (limit == 65536) {
                break;
            }
        }
        if (!isNullOrSpaces(site) || trace.length == 0) {
            return site;
        }
        return formatStackTraceElement(trace[0]);
    }

    private String findAndFormat(StackTraceElement[] trace) {
        String site = "";
        int length = trace.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            StackTraceElement s = trace[i];
            if (!ignore(s)) {
                site = formatStackTraceElement(s);
                break;
            }
            i++;
        }
        if (!isNullOrSpaces(site)) {
            return site;
        }
        for (StackTraceElement s2 : trace) {
            if (!defaultIgnore(s2)) {
                return formatStackTraceElement(s2);
            }
        }
        return site;
    }

    private String formatStackTraceElement(StackTraceElement s) {
        String result = s.toString().replace(s.getClassName(), simpleClassName(s.getClassName()));
        String v = simpleFileName(s.getFileName());
        if (v == null || !result.startsWith(v)) {
            return result;
        }
        return result.replace(s.getFileName(), "");
    }

    /* access modifiers changed from: protected */
    public Throwable apply(Throwable t) {
        return SeverityComparator.getInstance().apply(t);
    }

    /* access modifiers changed from: protected */
    public boolean ignore(StackTraceElement s) {
        return isUnknown(s) || defaultIgnore(s);
    }

    /* access modifiers changed from: protected */
    public String toAlternate(String s) {
        if (s != null) {
            return s.replaceAll("[\\x00-\\x1F\\x7F]+", "");
        }
        return null;
    }

    private Comparable<?> formatZonedDateTime(LogRecord record) {
        Comparable<?> zdt = LogManagerProperties.getZonedDateTime(record);
        if (zdt == null) {
            return new Date<>(record.getMillis());
        }
        return zdt;
    }

    private boolean defaultIgnore(StackTraceElement s) {
        return isSynthetic(s) || isStaticUtility(s) || isReflection(s);
    }

    private boolean isStaticUtility(StackTraceElement s) {
        try {
            return LogManagerProperties.isStaticUtilityClass(s.getClassName());
        } catch (Exception | LinkageError | RuntimeException e) {
            String cn = s.getClassName();
            return (cn.endsWith("s") && !cn.endsWith("es")) || cn.contains("Util") || cn.endsWith("Throwables");
        }
    }

    private boolean isSynthetic(StackTraceElement s) {
        return s.getMethodName().indexOf(36) > -1;
    }

    private boolean isUnknown(StackTraceElement s) {
        return s.getLineNumber() < 0;
    }

    private boolean isReflection(StackTraceElement s) {
        try {
            return LogManagerProperties.isReflectionClass(s.getClassName());
        } catch (Exception | LinkageError | RuntimeException e) {
            return s.getClassName().startsWith("java.lang.reflect.") || s.getClassName().startsWith("sun.reflect.");
        }
    }

    private String initFormat(String p) {
        String v = LogManagerProperties.fromLogManager(p.concat(".format"));
        if (isNullOrSpaces(v)) {
            return "%7$#.160s%n";
        }
        return v;
    }

    private static String replaceClassName(String msg, Throwable t) {
        if (!isNullOrSpaces(msg)) {
            int limit = 0;
            for (Throwable c = t; c != null; c = c.getCause()) {
                Class<?> k = c.getClass();
                msg = msg.replace(k.getName(), simpleClassName(k));
                limit++;
                if (limit == 65536) {
                    break;
                }
            }
        }
        return msg;
    }

    private static String replaceClassName(String msg, Object[] p) {
        if (!isNullOrSpaces(msg) && p != null) {
            for (Object o : p) {
                if (o != null) {
                    Class<?> k = o.getClass();
                    msg = msg.replace(k.getName(), simpleClassName(k));
                }
            }
        }
        return msg;
    }

    private static String simpleClassName(Class<?> k) {
        try {
            return k.getSimpleName();
        } catch (InternalError e) {
            return simpleClassName(k.getName());
        }
    }

    private static String simpleClassName(String name) {
        int dot;
        int sign;
        if (name == null) {
            return name;
        }
        int cursor = 0;
        int sign2 = -1;
        int dot2 = -1;
        int prev = -1;
        while (true) {
            if (cursor >= name.length()) {
                break;
            }
            int c = name.codePointAt(cursor);
            if (!Character.isJavaIdentifierPart(c)) {
                if (c == 46) {
                    if (dot2 + 1 == cursor || dot2 + 1 == sign2) {
                        return name;
                    }
                    prev = dot2;
                    dot2 = cursor;
                } else if (dot2 + 1 == cursor) {
                    dot2 = prev;
                }
            } else if (c == 36) {
                sign2 = cursor;
            }
            cursor += Character.charCount(c);
        }
        if (dot2 <= -1 || (dot = dot2 + 1) >= cursor || (sign = sign2 + 1) >= cursor) {
            return name;
        }
        if (sign <= dot) {
            sign = dot;
        }
        return name.substring(sign);
    }

    private static String simpleFileName(String name) {
        int index;
        if (name == null || (index = name.lastIndexOf(46)) <= -1) {
            return name;
        }
        return name.substring(0, index);
    }

    private static boolean isNullOrSpaces(String s) {
        return s == null || s.trim().isEmpty();
    }

    private class Alternate implements Formattable {
        private final String left;
        private final String right;

        Alternate(String left2, String right2) {
            this.left = String.valueOf(left2);
            this.right = String.valueOf(right2);
        }

        public void formatTo(java.util.Formatter formatter, int flags, int width, int precision) {
            String l = this.left;
            String r = this.right;
            if ((flags & 2) == 2) {
                l = l.toUpperCase(formatter.locale());
                r = r.toUpperCase(formatter.locale());
            }
            if ((flags & 4) == 4) {
                l = CompactFormatter.this.toAlternate(l);
                r = CompactFormatter.this.toAlternate(r);
            }
            int lc = 0;
            int rc = 0;
            if (precision >= 0) {
                lc = minCodePointCount(l, precision);
                int rc2 = minCodePointCount(r, precision);
                if (lc > (precision >> 1)) {
                    lc = Math.max(lc - rc2, lc >> 1);
                }
                rc = Math.min(precision - lc, rc2);
                l = l.substring(0, l.offsetByCodePoints(0, lc));
                r = r.substring(0, r.offsetByCodePoints(0, rc));
            }
            if (width > 0) {
                if (precision < 0) {
                    lc = minCodePointCount(l, width);
                    rc = minCodePointCount(r, width);
                }
                int half = width >> 1;
                if (lc < half) {
                    l = pad(flags, l, half - lc);
                }
                if (rc < half) {
                    r = pad(flags, r, half - rc);
                }
            }
            formatter.format(l, new Object[0]);
            if (!l.isEmpty() && !r.isEmpty()) {
                formatter.format("|", new Object[0]);
            }
            formatter.format(r, new Object[0]);
        }

        private int minCodePointCount(String s, int limit) {
            int len = s.length();
            return len - limit >= limit ? limit : Math.min(s.codePointCount(0, len), limit);
        }

        private String pad(int flags, String s, int padding) {
            StringBuilder b = new StringBuilder(Math.max(s.length() + padding, padding));
            if ((flags & 1) == 1) {
                for (int i = 0; i < padding; i++) {
                    b.append(' ');
                }
                b.append(s);
            } else {
                b.append(s);
                for (int i2 = 0; i2 < padding; i2++) {
                    b.append(' ');
                }
            }
            return b.toString();
        }
    }
}
