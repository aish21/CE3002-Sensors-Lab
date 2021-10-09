package com.sun.mail.util.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class DurationFilter implements Filter {
    static final /* synthetic */ boolean $assertionsDisabled = (!DurationFilter.class.desiredAssertionStatus());
    private long count;
    private final long duration;
    private long peak;
    private final long records;
    private long start;

    public DurationFilter() {
        this.records = checkRecords(initLong(".records"));
        this.duration = checkDuration(initLong(".duration"));
    }

    public DurationFilter(long records2, long duration2) {
        this.records = checkRecords(records2);
        this.duration = checkDuration(duration2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(java.lang.Object r15) {
        /*
            r14 = this;
            r3 = 1
            r8 = 0
            if (r14 != r15) goto L_0x0005
        L_0x0004:
            return r3
        L_0x0005:
            if (r15 == 0) goto L_0x0011
            java.lang.Class r9 = r14.getClass()
            java.lang.Class r10 = r15.getClass()
            if (r9 == r10) goto L_0x0013
        L_0x0011:
            r3 = r8
            goto L_0x0004
        L_0x0013:
            r2 = r15
            com.sun.mail.util.logging.DurationFilter r2 = (com.sun.mail.util.logging.DurationFilter) r2
            long r10 = r14.records
            long r12 = r2.records
            int r9 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
            if (r9 == 0) goto L_0x0020
            r3 = r8
            goto L_0x0004
        L_0x0020:
            long r10 = r14.duration
            long r12 = r2.duration
            int r9 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
            if (r9 == 0) goto L_0x002a
            r3 = r8
            goto L_0x0004
        L_0x002a:
            monitor-enter(r14)
            long r0 = r14.count     // Catch:{ all -> 0x0048 }
            long r4 = r14.peak     // Catch:{ all -> 0x0048 }
            long r6 = r14.start     // Catch:{ all -> 0x0048 }
            monitor-exit(r14)     // Catch:{ all -> 0x0048 }
            monitor-enter(r2)
            long r10 = r2.count     // Catch:{ all -> 0x004d }
            int r9 = (r0 > r10 ? 1 : (r0 == r10 ? 0 : -1))
            if (r9 != 0) goto L_0x0045
            long r10 = r2.peak     // Catch:{ all -> 0x004d }
            int r9 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1))
            if (r9 != 0) goto L_0x0045
            long r10 = r2.start     // Catch:{ all -> 0x004d }
            int r9 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1))
            if (r9 == 0) goto L_0x004b
        L_0x0045:
            monitor-exit(r2)     // Catch:{ all -> 0x004d }
            r3 = r8
            goto L_0x0004
        L_0x0048:
            r3 = move-exception
            monitor-exit(r14)     // Catch:{ all -> 0x0048 }
            throw r3
        L_0x004b:
            monitor-exit(r2)     // Catch:{ all -> 0x004d }
            goto L_0x0004
        L_0x004d:
            r3 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x004d }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.DurationFilter.equals(java.lang.Object):boolean");
    }

    public boolean isIdle() {
        return test(0, System.currentTimeMillis());
    }

    public int hashCode() {
        return ((((int) (this.records ^ (this.records >>> 32))) + 267) * 89) + ((int) (this.duration ^ (this.duration >>> 32)));
    }

    public boolean isLoggable(LogRecord record) {
        return accept(record.getMillis());
    }

    public boolean isLoggable() {
        return test(this.records, System.currentTimeMillis());
    }

    public String toString() {
        boolean idle;
        boolean loggable;
        synchronized (this) {
            long millis = System.currentTimeMillis();
            idle = test(0, millis);
            loggable = test(this.records, millis);
        }
        return getClass().getName() + "{records=" + this.records + ", duration=" + this.duration + ", idle=" + idle + ", loggable=" + loggable + '}';
    }

    /* access modifiers changed from: protected */
    public DurationFilter clone() throws CloneNotSupportedException {
        DurationFilter clone = (DurationFilter) super.clone();
        clone.count = 0;
        clone.peak = 0;
        clone.start = 0;
        return clone;
    }

    private boolean test(long limit, long millis) {
        long c;
        long s;
        if ($assertionsDisabled || limit >= 0) {
            synchronized (this) {
                c = this.count;
                s = this.start;
            }
            if (c > 0) {
                if (millis - s >= this.duration || c < limit) {
                    return true;
                }
            } else if (millis - s >= 0 || c == 0) {
                return true;
            }
            return false;
        }
        throw new AssertionError(limit);
    }

    private synchronized boolean accept(long millis) {
        boolean allow;
        if (this.count > 0) {
            if (millis - this.peak > 0) {
                this.peak = millis;
            }
            if (this.count != this.records) {
                this.count++;
                allow = true;
            } else if (this.peak - this.start >= this.duration) {
                this.count = 1;
                this.start = this.peak;
                allow = true;
            } else {
                this.count = -1;
                this.start = this.peak + this.duration;
                allow = false;
            }
        } else if (millis - this.start >= 0 || this.count == 0) {
            this.count = 1;
            this.start = millis;
            this.peak = millis;
            allow = true;
        } else {
            allow = false;
        }
        return allow;
    }

    private long initLong(String suffix) {
        long result = 0;
        String value = LogManagerProperties.fromLogManager(getClass().getName().concat(suffix));
        if (value == null || value.length() == 0) {
            return Long.MIN_VALUE;
        }
        String value2 = value.trim();
        if (isTimeEntry(suffix, value2)) {
            try {
                result = LogManagerProperties.parseDurationToMillis(value2);
            } catch (Exception | LinkageError | RuntimeException e) {
            }
        }
        if (result != 0) {
            return result;
        }
        long result2 = 1;
        try {
            for (String s : tokenizeLongs(value2)) {
                if (s.endsWith("L") || s.endsWith("l")) {
                    s = s.substring(0, s.length() - 1);
                }
                result2 = multiplyExact(result2, Long.parseLong(s));
            }
            return result2;
        } catch (RuntimeException e2) {
            return Long.MIN_VALUE;
        }
    }

    private boolean isTimeEntry(String suffix, String value) {
        if ((value.charAt(0) == 'P' || value.charAt(0) == 'p') && suffix.equals(".duration")) {
            return true;
        }
        return false;
    }

    private static String[] tokenizeLongs(String value) {
        int i = value.indexOf(42);
        if (i > -1) {
            String[] e = value.split("\\s*\\*\\s*");
            if (e.length != 0) {
                if (i == 0 || value.charAt(value.length() - 1) == '*') {
                    throw new NumberFormatException(value);
                } else if (e.length != 1) {
                    return e;
                } else {
                    throw new NumberFormatException(e[0]);
                }
            }
        }
        return new String[]{value};
    }

    private static long multiplyExact(long x, long y) {
        long r = x * y;
        if (((Math.abs(x) | Math.abs(y)) >>> 31) == 0 || ((y == 0 || r / y == x) && (x != Long.MIN_VALUE || y != -1))) {
            return r;
        }
        throw new ArithmeticException();
    }

    private static long checkRecords(long records2) {
        if (records2 > 0) {
            return records2;
        }
        return 1000;
    }

    private static long checkDuration(long duration2) {
        if (duration2 > 0) {
            return duration2;
        }
        return 900000;
    }
}
