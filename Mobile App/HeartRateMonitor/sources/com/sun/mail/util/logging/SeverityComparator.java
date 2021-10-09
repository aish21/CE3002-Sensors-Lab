package com.sun.mail.util.logging;

import java.io.Serializable;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class SeverityComparator implements Comparator<LogRecord>, Serializable {
    private static final Comparator<LogRecord> INSTANCE = new SeverityComparator();
    private static final long serialVersionUID = -2620442245251791965L;

    static SeverityComparator getInstance() {
        return (SeverityComparator) INSTANCE;
    }

    public Throwable apply(Throwable chain) {
        int limit = 0;
        Throwable root = chain;
        Throwable high = null;
        Throwable normal = null;
        for (Throwable cause = chain; cause != null; cause = cause.getCause()) {
            root = cause;
            if (isNormal(cause)) {
                normal = cause;
            }
            if (normal == null && (cause instanceof Error)) {
                high = cause;
            }
            limit++;
            if (limit == 65536) {
                break;
            }
        }
        if (high != null) {
            return high;
        }
        return normal != null ? normal : root;
    }

    public final int applyThenCompare(Throwable tc1, Throwable tc2) {
        if (tc1 == tc2) {
            return 0;
        }
        return compareThrowable(apply(tc1), apply(tc2));
    }

    public int compareThrowable(Throwable t1, Throwable t2) {
        int i = 1;
        int i2 = -1;
        if (t1 == t2) {
            return 0;
        }
        if (t1 == null) {
            if (!isNormal(t2)) {
                i = -1;
            }
            return i;
        } else if (t2 == null) {
            if (!isNormal(t1)) {
                i2 = 1;
            }
            return i2;
        } else if (t1.getClass() == t2.getClass()) {
            return 0;
        } else {
            if (isNormal(t1)) {
                if (isNormal(t2)) {
                    i2 = 0;
                }
                return i2;
            } else if (isNormal(t2)) {
                return 1;
            } else {
                if (t1 instanceof Error) {
                    if (!(t2 instanceof Error)) {
                        return 1;
                    }
                    return 0;
                } else if (t1 instanceof RuntimeException) {
                    if (!(t2 instanceof Error)) {
                        i2 = t2 instanceof RuntimeException ? 0 : 1;
                    }
                    return i2;
                } else if ((t2 instanceof Error) || (t2 instanceof RuntimeException)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    public int compare(LogRecord o1, LogRecord o2) {
        if (o1 == null || o2 == null) {
            throw new NullPointerException(toString(o1, o2));
        } else if (o1 == o2) {
            return 0;
        } else {
            int cmp = compare(o1.getLevel(), o2.getLevel());
            if (cmp != 0) {
                return cmp;
            }
            int cmp2 = applyThenCompare(o1.getThrown(), o2.getThrown());
            if (cmp2 != 0) {
                return cmp2;
            }
            int cmp3 = compare(o1.getSequenceNumber(), o2.getSequenceNumber());
            if (cmp3 == 0) {
                return compare(o1.getMillis(), o2.getMillis());
            }
            return cmp3;
        }
    }

    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass();
    }

    public int hashCode() {
        return getClass().hashCode() * 31;
    }

    public boolean isNormal(Throwable t) {
        if (t == null) {
            return false;
        }
        Class<Throwable> cls = Throwable.class;
        Class<Error> cls2 = Error.class;
        for (Class cls3 = t.getClass(); cls3 != cls; cls3 = cls3.getSuperclass()) {
            if (cls2.isAssignableFrom(cls3)) {
                if (cls3.getName().equals("java.lang.ThreadDeath")) {
                    return true;
                }
            } else if (cls3.getName().contains("Interrupt")) {
                return true;
            }
        }
        return false;
    }

    private int compare(Level a, Level b) {
        if (a == b) {
            return 0;
        }
        return compare((long) a.intValue(), (long) b.intValue());
    }

    private static String toString(Object o1, Object o2) {
        return o1 + ", " + o2;
    }

    private int compare(long x, long y) {
        if (x < y) {
            return -1;
        }
        return x > y ? 1 : 0;
    }
}
