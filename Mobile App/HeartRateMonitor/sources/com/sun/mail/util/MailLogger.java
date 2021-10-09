package com.sun.mail.util;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;

public final class MailLogger {
    private final boolean debug;
    private final Logger logger;
    private final PrintStream out;
    private final String prefix;

    public MailLogger(String name, String prefix2, boolean debug2, PrintStream out2) {
        this.logger = Logger.getLogger(name);
        this.prefix = prefix2;
        this.debug = debug2;
        this.out = out2 == null ? System.out : out2;
    }

    public MailLogger(Class<?> clazz, String prefix2, boolean debug2, PrintStream out2) {
        this.logger = Logger.getLogger(packageOf(clazz));
        this.prefix = prefix2;
        this.debug = debug2;
        this.out = out2 == null ? System.out : out2;
    }

    public MailLogger(Class<?> clazz, String subname, String prefix2, boolean debug2, PrintStream out2) {
        this.logger = Logger.getLogger(packageOf(clazz) + "." + subname);
        this.prefix = prefix2;
        this.debug = debug2;
        this.out = out2 == null ? System.out : out2;
    }

    @Deprecated
    public MailLogger(String name, String prefix2, Session session) {
        this(name, prefix2, session.getDebug(), session.getDebugOut());
    }

    @Deprecated
    public MailLogger(Class<?> clazz, String prefix2, Session session) {
        this(clazz, prefix2, session.getDebug(), session.getDebugOut());
    }

    public MailLogger getLogger(String name, String prefix2) {
        return new MailLogger(name, prefix2, this.debug, this.out);
    }

    public MailLogger getLogger(Class<?> clazz, String prefix2) {
        return new MailLogger(clazz, prefix2, this.debug, this.out);
    }

    public MailLogger getSubLogger(String subname, String prefix2) {
        return new MailLogger(this.logger.getName() + "." + subname, prefix2, this.debug, this.out);
    }

    public MailLogger getSubLogger(String subname, String prefix2, boolean debug2) {
        return new MailLogger(this.logger.getName() + "." + subname, prefix2, debug2, this.out);
    }

    public void log(Level level, String msg) {
        ifDebugOut(msg);
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg);
        }
    }

    public void log(Level level, String msg, Object param1) {
        if (this.debug) {
            msg = MessageFormat.format(msg, new Object[]{param1});
            debugOut(msg);
        }
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, param1);
        }
    }

    public void log(Level level, String msg, Object... params) {
        if (this.debug) {
            msg = MessageFormat.format(msg, params);
            debugOut(msg);
        }
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, params);
        }
    }

    public void logf(Level level, String msg, Object... params) {
        String msg2 = String.format(msg, params);
        ifDebugOut(msg2);
        this.logger.log(level, msg2);
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (this.debug) {
            if (thrown != null) {
                debugOut(msg + ", THROW: ");
                thrown.printStackTrace(this.out);
            } else {
                debugOut(msg);
            }
        }
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, thrown);
        }
    }

    public void config(String msg) {
        log(Level.CONFIG, msg);
    }

    public void fine(String msg) {
        log(Level.FINE, msg);
    }

    public void finer(String msg) {
        log(Level.FINER, msg);
    }

    public void finest(String msg) {
        log(Level.FINEST, msg);
    }

    public boolean isLoggable(Level level) {
        return this.debug || this.logger.isLoggable(level);
    }

    private void ifDebugOut(String msg) {
        if (this.debug) {
            debugOut(msg);
        }
    }

    private void debugOut(String msg) {
        if (this.prefix != null) {
            this.out.println(this.prefix + ": " + msg);
        } else {
            this.out.println(msg);
        }
    }

    private String packageOf(Class<?> clazz) {
        Package p = clazz.getPackage();
        if (p != null) {
            return p.getName();
        }
        String cname = clazz.getName();
        int i = cname.lastIndexOf(46);
        if (i > 0) {
            return cname.substring(0, i);
        }
        return "";
    }

    private StackTraceElement inferCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        int ix = 0;
        while (ix < stack.length && !isLoggerImplFrame(stack[ix].getClassName())) {
            ix++;
        }
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            if (!isLoggerImplFrame(frame.getClassName())) {
                return frame;
            }
            ix++;
        }
        return new StackTraceElement(MailLogger.class.getName(), "log", MailLogger.class.getName(), -1);
    }

    private boolean isLoggerImplFrame(String cname) {
        return MailLogger.class.getName().equals(cname);
    }
}
