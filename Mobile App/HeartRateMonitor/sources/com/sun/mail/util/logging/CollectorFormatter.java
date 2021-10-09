package com.sun.mail.util.logging;

import java.lang.reflect.UndeclaredThrowableException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CollectorFormatter extends Formatter {
    static final /* synthetic */ boolean $assertionsDisabled = (!CollectorFormatter.class.desiredAssertionStatus());
    private static final long INIT_TIME = System.currentTimeMillis();
    private final Comparator<? super LogRecord> comparator;
    private long count;
    private final String fmt;
    private final Formatter formatter;
    private long generation = 1;
    private LogRecord last;
    private long maxMillis = Long.MIN_VALUE;
    private long minMillis = INIT_TIME;
    private long thrown;

    public CollectorFormatter() {
        String p = getClass().getName();
        this.fmt = initFormat(p);
        this.formatter = initFormatter(p);
        this.comparator = initComparator(p);
    }

    public CollectorFormatter(String format) {
        String p = getClass().getName();
        this.fmt = format == null ? initFormat(p) : format;
        this.formatter = initFormatter(p);
        this.comparator = initComparator(p);
    }

    public CollectorFormatter(String format, Formatter f, Comparator<? super LogRecord> c) {
        this.fmt = format == null ? initFormat(getClass().getName()) : format;
        this.formatter = f;
        this.comparator = c;
    }

    public String format(LogRecord record) {
        LogRecord logRecord;
        boolean accepted;
        if (record == null) {
            throw new NullPointerException();
        }
        do {
            LogRecord peek = peek();
            if (peek != null) {
                logRecord = peek;
            } else {
                logRecord = record;
            }
            LogRecord update = apply(logRecord, record);
            if (peek != update) {
                update.getSourceMethodName();
                accepted = acceptAndUpdate(peek, update);
                continue;
            } else {
                accepted = accept(peek, record);
                continue;
            }
        } while (!accepted);
        return "";
    }

    public String getTail(Handler h) {
        super.getTail(h);
        return formatRecord(h, true);
    }

    public String toString() {
        try {
            return formatRecord((Handler) null, false);
        } catch (RuntimeException e) {
            return super.toString();
        }
    }

    /* access modifiers changed from: protected */
    public LogRecord apply(LogRecord t, LogRecord u) {
        if (t == null || u == null) {
            throw new NullPointerException();
        } else if (this.comparator != null) {
            return this.comparator.compare(t, u) >= 0 ? t : u;
        } else {
            return u;
        }
    }

    private synchronized boolean accept(LogRecord e, LogRecord u) {
        boolean z;
        long millis = u.getMillis();
        Throwable ex = u.getThrown();
        if (this.last == e) {
            long j = this.count + 1;
            this.count = j;
            if (j != 1) {
                this.minMillis = Math.min(this.minMillis, millis);
            } else {
                this.minMillis = millis;
            }
            this.maxMillis = Math.max(this.maxMillis, millis);
            if (ex != null) {
                this.thrown++;
            }
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private synchronized void reset(long min) {
        if (this.last != null) {
            this.last = null;
            this.generation++;
        }
        this.count = 0;
        this.thrown = 0;
        this.minMillis = min;
        this.maxMillis = Long.MIN_VALUE;
    }

    private String formatRecord(Handler h, boolean reset) {
        LogRecord record;
        long c;
        long g;
        long t;
        long msl;
        long msh;
        long now;
        String head;
        String msg;
        String tail;
        MessageFormat mf;
        synchronized (this) {
            record = this.last;
            c = this.count;
            g = this.generation;
            t = this.thrown;
            msl = this.minMillis;
            msh = this.maxMillis;
            now = System.currentTimeMillis();
            if (c == 0) {
                msh = now;
            }
            if (reset) {
                reset(msh);
            }
        }
        Formatter f = this.formatter;
        if (f != null) {
            synchronized (f) {
                head = f.getHead(h);
                msg = record != null ? f.format(record) : "";
                tail = f.getTail(h);
            }
        } else {
            head = "";
            msg = record != null ? formatMessage(record) : "";
            tail = "";
        }
        Locale l = null;
        if (record != null) {
            ResourceBundle rb = record.getResourceBundle();
            l = rb == null ? null : rb.getLocale();
        }
        if (l == null) {
            mf = new MessageFormat(this.fmt);
        } else {
            mf = new MessageFormat(this.fmt, l);
        }
        return mf.format(new Object[]{finish(head), finish(msg), finish(tail), Long.valueOf(c), Long.valueOf(c - 1), Long.valueOf(t), Long.valueOf(c - t), Long.valueOf(msl), Long.valueOf(msh), Long.valueOf(msh - msl), Long.valueOf(INIT_TIME), Long.valueOf(now), Long.valueOf(now - INIT_TIME), Long.valueOf(g)});
    }

    /* access modifiers changed from: protected */
    public String finish(String s) {
        return s.trim();
    }

    private synchronized LogRecord peek() {
        return this.last;
    }

    private synchronized boolean acceptAndUpdate(LogRecord e, LogRecord u) {
        boolean z;
        if (accept(e, u)) {
            this.last = u;
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private String initFormat(String p) {
        String v = LogManagerProperties.fromLogManager(p.concat(".format"));
        if (v == null || v.length() == 0) {
            return "{0}{1}{2}{4,choice,-1#|0#|0<... {4,number,integer} more}\n";
        }
        return v;
    }

    private Formatter initFormatter(String p) {
        String v = LogManagerProperties.fromLogManager(p.concat(".formatter"));
        if (v == null || v.length() == 0) {
            return Formatter.class.cast(new CompactFormatter());
        }
        if ("null".equalsIgnoreCase(v)) {
            return null;
        }
        try {
            return LogManagerProperties.newFormatter(v);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private Comparator<? super LogRecord> initComparator(String p) {
        String name = LogManagerProperties.fromLogManager(p.concat(".comparator"));
        String reverse = LogManagerProperties.fromLogManager(p.concat(".comparator.reverse"));
        if (name != null) {
            try {
                if (name.length() != 0) {
                    if (!"null".equalsIgnoreCase(name)) {
                        Comparator<? super LogRecord> c = LogManagerProperties.newComparator(name);
                        if (!Boolean.parseBoolean(reverse)) {
                            return c;
                        }
                        if ($assertionsDisabled || c != null) {
                            return LogManagerProperties.reverseOrder(c);
                        }
                        throw new AssertionError();
                    } else if (reverse == null) {
                        return null;
                    } else {
                        throw new IllegalArgumentException("No comparator to reverse.");
                    }
                }
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
            }
        }
        if (reverse == null) {
            return Comparator.class.cast(SeverityComparator.getInstance());
        }
        throw new IllegalArgumentException("No comparator to reverse.");
    }
}
