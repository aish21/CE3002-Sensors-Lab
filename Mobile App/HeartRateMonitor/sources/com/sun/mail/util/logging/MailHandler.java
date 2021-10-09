package com.sun.mail.util.logging;

import androidx.core.app.NotificationCompat;
import com.google.appinventor.components.runtime.util.NanoHTTPD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.activation.DataHandler;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

public class MailHandler extends Handler {
    static final /* synthetic */ boolean $assertionsDisabled = (!MailHandler.class.desiredAssertionStatus());
    private static final Filter[] EMPTY_FILTERS = new Filter[0];
    private static final Formatter[] EMPTY_FORMATTERS = new Formatter[0];
    private static final PrivilegedAction<Object> MAILHANDLER_LOADER = new GetAndSetContext(MailHandler.class);
    private static final int MIN_HEADER_SIZE = 1024;
    private static final ThreadLocal<Integer> MUTEX = new ThreadLocal<>();
    private static final Integer MUTEX_LINKAGE = -8;
    private static final Integer MUTEX_PUBLISH = -2;
    private static final Integer MUTEX_REPORT = -4;
    private static final int offValue = Level.OFF.intValue();
    private volatile Filter[] attachmentFilters;
    private Formatter[] attachmentFormatters;
    private Formatter[] attachmentNames;
    private Authenticator auth;
    private int capacity;
    private Comparator<? super LogRecord> comparator;
    private FileTypeMap contentTypes;
    private LogRecord[] data;
    private String encoding;
    private volatile ErrorManager errorManager = defaultErrorManager();
    private volatile Filter filter;
    private Formatter formatter;
    private boolean isWriting;
    private volatile Level logLevel = Level.ALL;
    private Properties mailProps;
    private int[] matched;
    private Filter pushFilter;
    private Level pushLevel;
    private volatile boolean sealed;
    private Session session;
    private int size;
    private Formatter subjectFormatter;

    public MailHandler() {
        init((Properties) null);
        this.sealed = true;
        checkAccess();
    }

    public MailHandler(int capacity2) {
        init((Properties) null);
        this.sealed = true;
        setCapacity0(capacity2);
    }

    public MailHandler(Properties props) {
        if (props == null) {
            throw new NullPointerException();
        }
        init(props);
        this.sealed = true;
        setMailProperties0(props);
    }

    public boolean isLoggable(LogRecord record) {
        int levelValue;
        if (record == null || record.getLevel().intValue() < (levelValue = getLevel().intValue()) || levelValue == offValue) {
            return $assertionsDisabled;
        }
        Filter body = getFilter();
        if (body != null && !body.isLoggable(record)) {
            return isAttachmentLoggable(record);
        }
        setMatchedPart(-1);
        return true;
    }

    public void publish(LogRecord record) {
        if (tryMutex()) {
            try {
                if (isLoggable(record)) {
                    if (record != null) {
                        record.getSourceMethodName();
                        publish0(record);
                    } else {
                        reportNullError(1);
                    }
                }
            } catch (LinkageError JDK8152515) {
                reportLinkageError(JDK8152515, 1);
            } finally {
                releaseMutex();
            }
        } else {
            reportUnPublishedError(record);
        }
    }

    private void publish0(LogRecord record) {
        boolean priority;
        Message msg;
        synchronized (this) {
            if (this.size == this.data.length && this.size < this.capacity) {
                grow();
            }
            if (this.size < this.data.length) {
                this.matched[this.size] = getMatchedPart();
                this.data[this.size] = record;
                this.size++;
                priority = isPushable(record);
                if (priority || this.size >= this.capacity) {
                    msg = writeLogRecords(1);
                } else {
                    msg = null;
                }
            } else {
                priority = $assertionsDisabled;
                msg = null;
            }
        }
        if (msg != null) {
            send(msg, priority, 1);
        }
    }

    private void reportUnPublishedError(LogRecord record) {
        String msg;
        Integer idx = MUTEX.get();
        if (idx == null || idx.intValue() > MUTEX_REPORT.intValue()) {
            MUTEX.set(MUTEX_REPORT);
            if (record != null) {
                try {
                    Formatter f = createSimpleFormatter();
                    msg = "Log record " + record.getSequenceNumber() + " was not published. " + head(f) + format(f, record) + tail(f, "");
                } catch (Throwable th) {
                    if (idx != null) {
                        MUTEX.set(idx);
                    } else {
                        MUTEX.remove();
                    }
                    throw th;
                }
            } else {
                msg = null;
            }
            reportError(msg, new IllegalStateException("Recursive publish detected by thread " + Thread.currentThread()), 1);
            if (idx != null) {
                MUTEX.set(idx);
            } else {
                MUTEX.remove();
            }
        }
    }

    private boolean tryMutex() {
        if (MUTEX.get() != null) {
            return $assertionsDisabled;
        }
        MUTEX.set(MUTEX_PUBLISH);
        return true;
    }

    private void releaseMutex() {
        MUTEX.remove();
    }

    private int getMatchedPart() {
        Integer idx = MUTEX.get();
        if (idx == null || idx.intValue() >= readOnlyAttachmentFilters().length) {
            idx = MUTEX_PUBLISH;
        }
        return idx.intValue();
    }

    private void setMatchedPart(int index) {
        if (MUTEX_PUBLISH.equals(MUTEX.get())) {
            MUTEX.set(Integer.valueOf(index));
        }
    }

    private void clearMatches(int index) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            for (int r = 0; r < this.size; r++) {
                if (this.matched[r] >= index) {
                    this.matched[r] = MUTEX_PUBLISH.intValue();
                }
            }
            return;
        }
        throw new AssertionError();
    }

    public void postConstruct() {
    }

    public void preDestroy() {
        push($assertionsDisabled, 3);
    }

    public void push() {
        push(true, 2);
    }

    public void flush() {
        push($assertionsDisabled, 2);
    }

    public void close() {
        Message msg;
        try {
            checkAccess();
            synchronized (this) {
                try {
                    msg = writeLogRecords(3);
                    this.logLevel = Level.OFF;
                    if (this.capacity > 0) {
                        this.capacity = -this.capacity;
                    }
                    if (this.size == 0 && this.data.length != 1) {
                        this.data = new LogRecord[1];
                        this.matched = new int[this.data.length];
                    }
                } catch (Throwable th) {
                    this.logLevel = Level.OFF;
                    if (this.capacity > 0) {
                        this.capacity = -this.capacity;
                    }
                    if (this.size == 0 && this.data.length != 1) {
                        this.data = new LogRecord[1];
                        this.matched = new int[this.data.length];
                    }
                    throw th;
                }
            }
            if (msg != null) {
                send(msg, $assertionsDisabled, 3);
            }
        } catch (LinkageError JDK8152515) {
            reportLinkageError(JDK8152515, 3);
        }
    }

    public void setLevel(Level newLevel) {
        if (newLevel == null) {
            throw new NullPointerException();
        }
        checkAccess();
        synchronized (this) {
            if (this.capacity > 0) {
                this.logLevel = newLevel;
            }
        }
    }

    public Level getLevel() {
        return this.logLevel;
    }

    public ErrorManager getErrorManager() {
        checkAccess();
        return this.errorManager;
    }

    public void setErrorManager(ErrorManager em) {
        checkAccess();
        setErrorManager0(em);
    }

    private void setErrorManager0(ErrorManager em) {
        if (em == null) {
            throw new NullPointerException();
        }
        try {
            synchronized (this) {
                this.errorManager = em;
                super.setErrorManager(em);
            }
        } catch (LinkageError | RuntimeException e) {
        }
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setFilter(Filter newFilter) {
        checkAccess();
        synchronized (this) {
            if (newFilter != this.filter) {
                clearMatches(-1);
            }
            this.filter = newFilter;
        }
    }

    public synchronized String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding2) throws UnsupportedEncodingException {
        checkAccess();
        setEncoding0(encoding2);
    }

    private void setEncoding0(String e) throws UnsupportedEncodingException {
        if (e != null) {
            try {
                if (!Charset.isSupported(e)) {
                    throw new UnsupportedEncodingException(e);
                }
            } catch (IllegalCharsetNameException e2) {
                throw new UnsupportedEncodingException(e);
            }
        }
        synchronized (this) {
            this.encoding = e;
        }
    }

    public synchronized Formatter getFormatter() {
        return this.formatter;
    }

    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        checkAccess();
        if (newFormatter == null) {
            throw new NullPointerException();
        }
        this.formatter = newFormatter;
    }

    public final synchronized Level getPushLevel() {
        return this.pushLevel;
    }

    public final synchronized void setPushLevel(Level level) {
        checkAccess();
        if (level == null) {
            throw new NullPointerException();
        } else if (this.isWriting) {
            throw new IllegalStateException();
        } else {
            this.pushLevel = level;
        }
    }

    public final synchronized Filter getPushFilter() {
        return this.pushFilter;
    }

    public final synchronized void setPushFilter(Filter filter2) {
        checkAccess();
        if (this.isWriting) {
            throw new IllegalStateException();
        }
        this.pushFilter = filter2;
    }

    public final synchronized Comparator<? super LogRecord> getComparator() {
        return this.comparator;
    }

    public final synchronized void setComparator(Comparator<? super LogRecord> c) {
        checkAccess();
        if (this.isWriting) {
            throw new IllegalStateException();
        }
        this.comparator = c;
    }

    public final synchronized int getCapacity() {
        if ($assertionsDisabled || !(this.capacity == Integer.MIN_VALUE || this.capacity == 0)) {
        } else {
            throw new AssertionError(this.capacity);
        }
        return Math.abs(this.capacity);
    }

    public final synchronized Authenticator getAuthenticator() {
        checkAccess();
        return this.auth;
    }

    public final void setAuthenticator(Authenticator auth2) {
        setAuthenticator0(auth2);
    }

    public final void setAuthenticator(char... password) {
        if (password == null) {
            setAuthenticator0((Authenticator) null);
        } else {
            setAuthenticator0(DefaultAuthenticator.m42of(new String(password)));
        }
    }

    private void setAuthenticator0(Authenticator auth2) {
        Session settings;
        checkAccess();
        synchronized (this) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.auth = auth2;
            settings = updateSession();
        }
        verifySettings(settings);
    }

    public final void setMailProperties(Properties props) {
        setMailProperties0(props);
    }

    private void setMailProperties0(Properties props) {
        Session settings;
        checkAccess();
        Properties props2 = (Properties) props.clone();
        synchronized (this) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.mailProps = props2;
            settings = updateSession();
        }
        verifySettings(settings);
    }

    public final Properties getMailProperties() {
        Properties props;
        checkAccess();
        synchronized (this) {
            props = this.mailProps;
        }
        return (Properties) props.clone();
    }

    public final Filter[] getAttachmentFilters() {
        return (Filter[]) readOnlyAttachmentFilters().clone();
    }

    public final void setAttachmentFilters(Filter... filters) {
        Filter[] filters2;
        checkAccess();
        if (filters.length == 0) {
            filters2 = emptyFilterArray();
        } else {
            filters2 = (Filter[]) Arrays.copyOf(filters, filters.length, Filter[].class);
        }
        synchronized (this) {
            if (this.attachmentFormatters.length != filters2.length) {
                throw attachmentMismatch(this.attachmentFormatters.length, filters2.length);
            } else if (this.isWriting) {
                throw new IllegalStateException();
            } else {
                if (this.size != 0) {
                    int i = 0;
                    while (true) {
                        if (i >= filters2.length) {
                            break;
                        } else if (filters2[i] != this.attachmentFilters[i]) {
                            clearMatches(i);
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                this.attachmentFilters = filters2;
            }
        }
    }

    public final Formatter[] getAttachmentFormatters() {
        Formatter[] formatters;
        synchronized (this) {
            formatters = this.attachmentFormatters;
        }
        return (Formatter[]) formatters.clone();
    }

    public final void setAttachmentFormatters(Formatter... formatters) {
        Formatter[] formatters2;
        checkAccess();
        if (formatters.length == 0) {
            formatters2 = emptyFormatterArray();
        } else {
            formatters2 = (Formatter[]) Arrays.copyOf(formatters, formatters.length, Formatter[].class);
            for (int i = 0; i < formatters2.length; i++) {
                if (formatters2[i] == null) {
                    throw new NullPointerException(atIndexMsg(i));
                }
            }
        }
        synchronized (this) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.attachmentFormatters = formatters2;
            alignAttachmentFilters();
            alignAttachmentNames();
        }
    }

    public final Formatter[] getAttachmentNames() {
        Formatter[] formatters;
        synchronized (this) {
            formatters = this.attachmentNames;
        }
        return (Formatter[]) formatters.clone();
    }

    public final void setAttachmentNames(String... names) {
        Formatter[] formatters;
        checkAccess();
        if (names.length == 0) {
            formatters = emptyFormatterArray();
        } else {
            formatters = new Formatter[names.length];
        }
        int i = 0;
        while (i < names.length) {
            String name = names[i];
            if (name == null) {
                throw new NullPointerException(atIndexMsg(i));
            } else if (name.length() > 0) {
                formatters[i] = TailNameFormatter.m43of(name);
                i++;
            } else {
                throw new IllegalArgumentException(atIndexMsg(i));
            }
        }
        synchronized (this) {
            if (this.attachmentFormatters.length != names.length) {
                throw attachmentMismatch(this.attachmentFormatters.length, names.length);
            } else if (this.isWriting) {
                throw new IllegalStateException();
            } else {
                this.attachmentNames = formatters;
            }
        }
    }

    public final void setAttachmentNames(Formatter... formatters) {
        Formatter[] formatters2;
        checkAccess();
        if (formatters.length == 0) {
            formatters2 = emptyFormatterArray();
        } else {
            formatters2 = (Formatter[]) Arrays.copyOf(formatters, formatters.length, Formatter[].class);
        }
        for (int i = 0; i < formatters2.length; i++) {
            if (formatters2[i] == null) {
                throw new NullPointerException(atIndexMsg(i));
            }
        }
        synchronized (this) {
            if (this.attachmentFormatters.length != formatters2.length) {
                throw attachmentMismatch(this.attachmentFormatters.length, formatters2.length);
            } else if (this.isWriting) {
                throw new IllegalStateException();
            } else {
                this.attachmentNames = formatters2;
            }
        }
    }

    public final synchronized Formatter getSubject() {
        return this.subjectFormatter;
    }

    public final void setSubject(String subject) {
        if (subject != null) {
            setSubject(TailNameFormatter.m43of(subject));
        } else {
            checkAccess();
            throw new NullPointerException();
        }
    }

    public final void setSubject(Formatter format) {
        checkAccess();
        if (format == null) {
            throw new NullPointerException();
        }
        synchronized (this) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.subjectFormatter = format;
        }
    }

    /* access modifiers changed from: protected */
    public void reportError(String msg, Exception ex, int code) {
        if (msg != null) {
            try {
                this.errorManager.error(Level.SEVERE.getName().concat(": ").concat(msg), ex, code);
            } catch (LinkageError | RuntimeException GLASSFISH_21258) {
                reportLinkageError(GLASSFISH_21258, code);
            }
        } else {
            this.errorManager.error((String) null, ex, code);
        }
    }

    private void checkAccess() {
        if (this.sealed) {
            LogManagerProperties.checkLogManagerAccess();
        }
    }

    /* access modifiers changed from: package-private */
    public final String contentTypeOf(CharSequence chunk) {
        if (!isEmpty(chunk)) {
            if (chunk.length() > 25) {
                chunk = chunk.subSequence(0, 25);
            }
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(chunk.toString().getBytes(getEncodingName()));
                if ($assertionsDisabled || in.markSupported()) {
                    return URLConnection.guessContentTypeFromStream(in);
                }
                throw new AssertionError(in.getClass().getName());
            } catch (IOException IOE) {
                reportError(IOE.getMessage(), (Exception) IOE, 5);
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0076, code lost:
        r2 = r2.getSuperclass();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.lang.String contentTypeOf(java.util.logging.Formatter r8) {
        /*
            r7 = this;
            boolean r5 = $assertionsDisabled
            if (r5 != 0) goto L_0x0010
            boolean r5 = java.lang.Thread.holdsLock(r7)
            if (r5 != 0) goto L_0x0010
            java.lang.AssertionError r5 = new java.lang.AssertionError
            r5.<init>()
            throw r5
        L_0x0010:
            if (r8 == 0) goto L_0x007b
            java.lang.Class r5 = r8.getClass()
            java.lang.String r5 = r5.getName()
            java.lang.String r4 = r7.getContentType(r5)
            if (r4 == 0) goto L_0x0021
        L_0x0020:
            return r4
        L_0x0021:
            java.lang.Class r2 = r8.getClass()
        L_0x0025:
            java.lang.Class<java.util.logging.Formatter> r5 = java.util.logging.Formatter.class
            if (r2 == r5) goto L_0x007b
            java.lang.String r3 = r2.getSimpleName()     // Catch:{ InternalError -> 0x0053 }
        L_0x002d:
            java.util.Locale r5 = java.util.Locale.ENGLISH
            java.lang.String r3 = r3.toLowerCase(r5)
            r5 = 36
            int r5 = r3.indexOf(r5)
            int r1 = r5 + 1
        L_0x003b:
            java.lang.String r5 = "ml"
            int r1 = r3.indexOf(r5, r1)
            r5 = -1
            if (r1 <= r5) goto L_0x0076
            if (r1 <= 0) goto L_0x0073
            int r5 = r1 + -1
            char r5 = r3.charAt(r5)
            r6 = 120(0x78, float:1.68E-43)
            if (r5 != r6) goto L_0x0059
            java.lang.String r4 = "application/xml"
            goto L_0x0020
        L_0x0053:
            r0 = move-exception
            java.lang.String r3 = r2.getName()
            goto L_0x002d
        L_0x0059:
            r5 = 1
            if (r1 <= r5) goto L_0x0073
            int r5 = r1 + -2
            char r5 = r3.charAt(r5)
            r6 = 104(0x68, float:1.46E-43)
            if (r5 != r6) goto L_0x0073
            int r5 = r1 + -1
            char r5 = r3.charAt(r5)
            r6 = 116(0x74, float:1.63E-43)
            if (r5 != r6) goto L_0x0073
            java.lang.String r4 = "text/html"
            goto L_0x0020
        L_0x0073:
            int r1 = r1 + 2
            goto L_0x003b
        L_0x0076:
            java.lang.Class r2 = r2.getSuperclass()
            goto L_0x0025
        L_0x007b:
            r4 = 0
            goto L_0x0020
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.MailHandler.contentTypeOf(java.util.logging.Formatter):java.lang.String");
    }

    /* access modifiers changed from: package-private */
    public final boolean isMissingContent(Message msg, Throwable t) {
        Object ccl = getAndSetContextClassLoader(MAILHANDLER_LOADER);
        try {
            msg.writeTo(new ByteArrayOutputStream(1024));
            getAndSetContextClassLoader(ccl);
        } catch (RuntimeException RE) {
            throw RE;
        } catch (Exception noContent) {
            String txt = noContent.getMessage();
            if (!isEmpty(txt)) {
                int limit = 0;
                while (t != null) {
                    if (noContent.getClass() != t.getClass() || !txt.equals(t.getMessage())) {
                        Throwable cause = t.getCause();
                        if (cause != null || !(t instanceof MessagingException)) {
                            t = cause;
                        } else {
                            t = ((MessagingException) t).getNextException();
                        }
                        limit++;
                        if (limit == 65536) {
                            break;
                        }
                    } else {
                        getAndSetContextClassLoader(ccl);
                        return true;
                    }
                }
            }
            getAndSetContextClassLoader(ccl);
        } catch (Throwable th) {
            getAndSetContextClassLoader(ccl);
            throw th;
        }
        return $assertionsDisabled;
    }

    private void reportError(Message msg, Exception ex, int code) {
        try {
            this.errorManager.error(toRawString(msg), ex, code);
        } catch (RuntimeException re) {
            try {
                reportError(toMsgString(re), ex, code);
            } catch (LinkageError GLASSFISH_21258) {
                reportLinkageError(GLASSFISH_21258, code);
            }
        } catch (Exception e) {
            reportError(toMsgString(e), ex, code);
        }
    }

    private void reportLinkageError(Throwable le, int code) {
        if (le == null) {
            throw new NullPointerException(String.valueOf(code));
        }
        Integer idx = MUTEX.get();
        if (idx == null || idx.intValue() > MUTEX_LINKAGE.intValue()) {
            MUTEX.set(MUTEX_LINKAGE);
            try {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), le);
                if (idx != null) {
                    MUTEX.set(idx);
                } else {
                    MUTEX.remove();
                }
            } catch (LinkageError | RuntimeException e) {
                if (idx != null) {
                    MUTEX.set(idx);
                } else {
                    MUTEX.remove();
                }
            } catch (Throwable th) {
                if (idx != null) {
                    MUTEX.set(idx);
                } else {
                    MUTEX.remove();
                }
                throw th;
            }
        }
    }

    private String getContentType(String name) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            String type = this.contentTypes.getContentType(name);
            if (NanoHTTPD.MIME_DEFAULT_BINARY.equalsIgnoreCase(type)) {
                return null;
            }
            return type;
        }
        throw new AssertionError();
    }

    private String getEncodingName() {
        String charset = getEncoding();
        if (charset == null) {
            return MimeUtility.getDefaultJavaCharset();
        }
        return charset;
    }

    private void setContent(MimePart part, CharSequence buf, String type) throws MessagingException {
        String charset = getEncodingName();
        if (type == null || NanoHTTPD.MIME_PLAINTEXT.equalsIgnoreCase(type)) {
            part.setText(buf.toString(), MimeUtility.mimeCharset(charset));
            return;
        }
        try {
            part.setDataHandler(new DataHandler(new ByteArrayDataSource(buf.toString(), contentWithEncoding(type, charset))));
        } catch (IOException IOE) {
            reportError(IOE.getMessage(), (Exception) IOE, 5);
            part.setText(buf.toString(), charset);
        }
    }

    private String contentWithEncoding(String type, String encoding2) {
        if ($assertionsDisabled || encoding2 != null) {
            try {
                ContentType ct = new ContentType(type);
                ct.setParameter("charset", MimeUtility.mimeCharset(encoding2));
                String encoding3 = ct.toString();
                if (!isEmpty(encoding3)) {
                    return encoding3;
                }
                return type;
            } catch (MessagingException ME) {
                reportError(type, (Exception) ME, 5);
                return type;
            }
        } else {
            throw new AssertionError();
        }
    }

    private synchronized void setCapacity0(int newCapacity) {
        checkAccess();
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        } else if (this.isWriting) {
            throw new IllegalStateException();
        } else if (this.capacity < 0) {
            this.capacity = -newCapacity;
        } else {
            this.capacity = newCapacity;
        }
    }

    private Filter[] readOnlyAttachmentFilters() {
        return this.attachmentFilters;
    }

    private static Formatter[] emptyFormatterArray() {
        return EMPTY_FORMATTERS;
    }

    private static Filter[] emptyFilterArray() {
        return EMPTY_FILTERS;
    }

    private boolean alignAttachmentNames() {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            boolean fixed = $assertionsDisabled;
            int expect = this.attachmentFormatters.length;
            int current = this.attachmentNames.length;
            if (current != expect) {
                this.attachmentNames = (Formatter[]) Arrays.copyOf(this.attachmentNames, expect, Formatter[].class);
                fixed = current != 0 ? true : $assertionsDisabled;
            }
            if (expect == 0) {
                this.attachmentNames = emptyFormatterArray();
                if (!$assertionsDisabled && this.attachmentNames.length != 0) {
                    throw new AssertionError();
                }
            } else {
                for (int i = 0; i < expect; i++) {
                    if (this.attachmentNames[i] == null) {
                        this.attachmentNames[i] = TailNameFormatter.m43of(toString(this.attachmentFormatters[i]));
                    }
                }
            }
            return fixed;
        }
        throw new AssertionError();
    }

    private boolean alignAttachmentFilters() {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            boolean fixed = $assertionsDisabled;
            int expect = this.attachmentFormatters.length;
            int current = this.attachmentFilters.length;
            if (current != expect) {
                this.attachmentFilters = (Filter[]) Arrays.copyOf(this.attachmentFilters, expect, Filter[].class);
                clearMatches(current);
                fixed = current != 0 ? true : $assertionsDisabled;
                Filter body = this.filter;
                if (body != null) {
                    for (int i = current; i < expect; i++) {
                        this.attachmentFilters[i] = body;
                    }
                }
            }
            if (expect == 0) {
                this.attachmentFilters = emptyFilterArray();
                if (!$assertionsDisabled && this.attachmentFilters.length != 0) {
                    throw new AssertionError();
                }
            }
            return fixed;
        }
        throw new AssertionError();
    }

    private void reset() {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            if (this.size < this.data.length) {
                Arrays.fill(this.data, 0, this.size, (Object) null);
            } else {
                Arrays.fill(this.data, (Object) null);
            }
            this.size = 0;
            return;
        }
        throw new AssertionError();
    }

    private void grow() {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            int len = this.data.length;
            int newCapacity = (len >> 1) + len + 1;
            if (newCapacity > this.capacity || newCapacity < len) {
                newCapacity = this.capacity;
            }
            if ($assertionsDisabled || len != this.capacity) {
                this.data = (LogRecord[]) Arrays.copyOf(this.data, newCapacity, LogRecord[].class);
                this.matched = Arrays.copyOf(this.matched, newCapacity);
                return;
            }
            throw new AssertionError(len);
        }
        throw new AssertionError();
    }

    /* JADX INFO: finally extract failed */
    private synchronized void init(Properties props) {
        if ($assertionsDisabled || this.errorManager != null) {
            String p = getClass().getName();
            this.mailProps = new Properties();
            Object ccl = getAndSetContextClassLoader(MAILHANDLER_LOADER);
            try {
                this.contentTypes = FileTypeMap.getDefaultFileTypeMap();
                getAndSetContextClassLoader(ccl);
                initErrorManager(p);
                initLevel(p);
                initFilter(p);
                initCapacity(p);
                initAuthenticator(p);
                initEncoding(p);
                initFormatter(p);
                initComparator(p);
                initPushLevel(p);
                initPushFilter(p);
                initSubject(p);
                initAttachmentFormaters(p);
                initAttachmentFilters(p);
                initAttachmentNames(p);
                if (props == null && LogManagerProperties.fromLogManager(p.concat(".verify")) != null) {
                    verifySettings(initSession());
                }
                intern();
            } catch (Throwable th) {
                getAndSetContextClassLoader(ccl);
                throw th;
            }
        } else {
            throw new AssertionError();
        }
    }

    private void intern() {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                Map<Object, Object> seen = new HashMap<>();
                try {
                    intern(seen, this.errorManager);
                } catch (SecurityException se) {
                    reportError(se.getMessage(), (Exception) se, 4);
                }
                try {
                    Filter canidate = this.filter;
                    Object result = intern(seen, canidate);
                    if (result != canidate && (result instanceof Filter)) {
                        this.filter = (Filter) result;
                    }
                    Formatter canidate2 = this.formatter;
                    Object result2 = intern(seen, canidate2);
                    if (result2 != canidate2 && (result2 instanceof Formatter)) {
                        this.formatter = (Formatter) result2;
                    }
                } catch (SecurityException se2) {
                    reportError(se2.getMessage(), (Exception) se2, 4);
                }
                Formatter canidate3 = this.subjectFormatter;
                Object result3 = intern(seen, canidate3);
                if (result3 != canidate3 && (result3 instanceof Formatter)) {
                    this.subjectFormatter = (Formatter) result3;
                }
                Filter canidate4 = this.pushFilter;
                Object result4 = intern(seen, canidate4);
                if (result4 != canidate4 && (result4 instanceof Filter)) {
                    this.pushFilter = (Filter) result4;
                }
                for (int i = 0; i < this.attachmentFormatters.length; i++) {
                    Formatter canidate5 = this.attachmentFormatters[i];
                    Object result5 = intern(seen, canidate5);
                    if (result5 != canidate5 && (result5 instanceof Formatter)) {
                        this.attachmentFormatters[i] = (Formatter) result5;
                    }
                    Filter canidate6 = this.attachmentFilters[i];
                    Object result6 = intern(seen, canidate6);
                    if (result6 != canidate6 && (result6 instanceof Filter)) {
                        this.attachmentFilters[i] = (Filter) result6;
                    }
                    Formatter canidate7 = this.attachmentNames[i];
                    Object result7 = intern(seen, canidate7);
                    if (result7 != canidate7 && (result7 instanceof Formatter)) {
                        this.attachmentNames[i] = (Formatter) result7;
                    }
                }
            } catch (Exception skip) {
                reportError(skip.getMessage(), skip, 4);
            } catch (LinkageError skip2) {
                reportError(skip2.getMessage(), (Exception) new InvocationTargetException(skip2), 4);
            }
        } else {
            throw new AssertionError();
        }
    }

    private Object intern(Map<Object, Object> m, Object o) throws Exception {
        Object key;
        if (o == null) {
            return null;
        }
        if (o.getClass().getName().equals(TailNameFormatter.class.getName())) {
            key = o;
        } else {
            key = o.getClass().getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        if (key.getClass() != o.getClass()) {
            return o;
        }
        Object found = m.get(key);
        if (found == null) {
            boolean right = key.equals(o);
            boolean left = o.equals(key);
            if (right && left) {
                Object found2 = m.put(o, o);
                if (found2 != null) {
                    reportNonDiscriminating(key, found2);
                    Object found3 = m.remove(key);
                    if (found3 != o) {
                        reportNonDiscriminating(key, found3);
                        m.clear();
                    }
                }
            } else if (right != left) {
                reportNonSymmetric(o, key);
            }
            return o;
        } else if (o.getClass() == found.getClass()) {
            return found;
        } else {
            reportNonDiscriminating(o, found);
            return o;
        }
    }

    private static Formatter createSimpleFormatter() {
        return Formatter.class.cast(new SimpleFormatter());
    }

    private static boolean isEmpty(CharSequence s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    private static boolean hasValue(String name) {
        if (isEmpty(name) || "null".equalsIgnoreCase(name)) {
            return $assertionsDisabled;
        }
        return true;
    }

    private void initAttachmentFilters(String p) {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if ($assertionsDisabled || this.attachmentFormatters != null) {
            String list = LogManagerProperties.fromLogManager(p.concat(".attachment.filters"));
            if (!isEmpty(list)) {
                String[] names = list.split(",");
                Filter[] a = new Filter[names.length];
                for (int i = 0; i < a.length; i++) {
                    names[i] = names[i].trim();
                    if (!"null".equalsIgnoreCase(names[i])) {
                        try {
                            a[i] = LogManagerProperties.newFilter(names[i]);
                        } catch (SecurityException SE) {
                            throw SE;
                        } catch (Exception E) {
                            reportError(E.getMessage(), E, 4);
                        }
                    }
                }
                this.attachmentFilters = a;
                if (alignAttachmentFilters()) {
                    reportError("Attachment filters.", (Exception) attachmentMismatch("Length mismatch."), 4);
                    return;
                }
                return;
            }
            this.attachmentFilters = emptyFilterArray();
            alignAttachmentFilters();
        } else {
            throw new AssertionError();
        }
    }

    private void initAttachmentFormaters(String p) {
        Formatter[] a;
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            String list = LogManagerProperties.fromLogManager(p.concat(".attachment.formatters"));
            if (!isEmpty(list)) {
                String[] names = list.split(",");
                if (names.length == 0) {
                    a = emptyFormatterArray();
                } else {
                    a = new Formatter[names.length];
                }
                for (int i = 0; i < a.length; i++) {
                    names[i] = names[i].trim();
                    if (!"null".equalsIgnoreCase(names[i])) {
                        try {
                            a[i] = LogManagerProperties.newFormatter(names[i]);
                            if (a[i] instanceof TailNameFormatter) {
                                reportError("Attachment formatter.", new ClassNotFoundException(a[i].toString()), 4);
                                a[i] = createSimpleFormatter();
                            }
                        } catch (SecurityException SE) {
                            throw SE;
                        } catch (Exception E) {
                            reportError(E.getMessage(), E, 4);
                            a[i] = createSimpleFormatter();
                        }
                    } else {
                        reportError("Attachment formatter.", new NullPointerException(atIndexMsg(i)), 4);
                        a[i] = createSimpleFormatter();
                    }
                }
                this.attachmentFormatters = a;
                return;
            }
            this.attachmentFormatters = emptyFormatterArray();
            return;
        }
        throw new AssertionError();
    }

    private void initAttachmentNames(String p) {
        String[] names;
        Formatter[] a;
        int i;
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if ($assertionsDisabled || this.attachmentFormatters != null) {
            String list = LogManagerProperties.fromLogManager(p.concat(".attachment.names"));
            if (!isEmpty(list)) {
                names = list.split(",");
                a = new Formatter[names.length];
                i = 0;
                while (i < a.length) {
                    names[i] = names[i].trim();
                    if (!"null".equalsIgnoreCase(names[i])) {
                        try {
                            a[i] = LogManagerProperties.newFormatter(names[i]);
                        } catch (ClassNotFoundException e) {
                            ClassNotFoundException classNotFoundException = e;
                        } catch (ClassCastException e2) {
                            ClassCastException classCastException = e2;
                        }
                    } else {
                        reportError("Attachment names.", new NullPointerException(atIndexMsg(i)), 4);
                    }
                    i++;
                }
                this.attachmentNames = a;
                if (alignAttachmentNames()) {
                    reportError("Attachment names.", (Exception) attachmentMismatch("Length mismatch."), 4);
                    return;
                }
                return;
            }
            this.attachmentNames = emptyFormatterArray();
            alignAttachmentNames();
            return;
        } else {
            throw new AssertionError();
        }
        try {
            a[i] = TailNameFormatter.m43of(names[i]);
        } catch (SecurityException SE) {
            throw SE;
        } catch (Exception E) {
            reportError(E.getMessage(), E, 4);
        }
        i++;
    }

    private void initAuthenticator(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            String name = LogManagerProperties.fromLogManager(p.concat(".authenticator"));
            if (name != null && !"null".equalsIgnoreCase(name)) {
                if (name.length() != 0) {
                    try {
                        this.auth = (Authenticator) LogManagerProperties.newObjectFrom(name, Authenticator.class);
                    } catch (SecurityException SE) {
                        throw SE;
                    } catch (ClassCastException | ClassNotFoundException e) {
                        this.auth = DefaultAuthenticator.m42of(name);
                    } catch (Exception E) {
                        reportError(E.getMessage(), E, 4);
                    }
                } else {
                    this.auth = DefaultAuthenticator.m42of(name);
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    private void initLevel(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String val = LogManagerProperties.fromLogManager(p.concat(".level"));
                if (val != null) {
                    this.logLevel = Level.parse(val);
                } else {
                    this.logLevel = Level.WARNING;
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (RuntimeException RE) {
                reportError(RE.getMessage(), (Exception) RE, 4);
                this.logLevel = Level.WARNING;
            }
        } else {
            throw new AssertionError();
        }
    }

    private void initFilter(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String name = LogManagerProperties.fromLogManager(p.concat(".filter"));
                if (hasValue(name)) {
                    this.filter = LogManagerProperties.newFilter(name);
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (Exception E) {
                reportError(E.getMessage(), E, 4);
            }
        } else {
            throw new AssertionError();
        }
    }

    private void initCapacity(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String value = LogManagerProperties.fromLogManager(p.concat(".capacity"));
                if (value != null) {
                    setCapacity0(Integer.parseInt(value));
                } else {
                    setCapacity0(1000);
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (RuntimeException RE) {
                reportError(RE.getMessage(), (Exception) RE, 4);
            }
            if (this.capacity <= 0) {
                this.capacity = 1000;
            }
            this.data = new LogRecord[1];
            this.matched = new int[this.data.length];
            return;
        }
        throw new AssertionError();
    }

    private void initEncoding(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String e = LogManagerProperties.fromLogManager(p.concat(".encoding"));
                if (e != null) {
                    setEncoding0(e);
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (UnsupportedEncodingException | RuntimeException UEE) {
                reportError(UEE.getMessage(), UEE, 4);
            }
        } else {
            throw new AssertionError();
        }
    }

    private ErrorManager defaultErrorManager() {
        ErrorManager em;
        try {
            em = super.getErrorManager();
        } catch (LinkageError | RuntimeException e) {
            em = null;
        }
        if (em == null) {
            return new ErrorManager();
        }
        return em;
    }

    private void initErrorManager(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String name = LogManagerProperties.fromLogManager(p.concat(".errorManager"));
                if (name != null) {
                    setErrorManager0(LogManagerProperties.newErrorManager(name));
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (Exception E) {
                reportError(E.getMessage(), E, 4);
            }
        } else {
            throw new AssertionError();
        }
    }

    private void initFormatter(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String name = LogManagerProperties.fromLogManager(p.concat(".formatter"));
                if (hasValue(name)) {
                    Formatter f = LogManagerProperties.newFormatter(name);
                    if (!$assertionsDisabled && f == null) {
                        throw new AssertionError();
                    } else if (!(f instanceof TailNameFormatter)) {
                        this.formatter = f;
                    } else {
                        this.formatter = createSimpleFormatter();
                    }
                } else {
                    this.formatter = createSimpleFormatter();
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (Exception E) {
                reportError(E.getMessage(), E, 4);
                this.formatter = createSimpleFormatter();
            }
        } else {
            throw new AssertionError();
        }
    }

    private void initComparator(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String name = LogManagerProperties.fromLogManager(p.concat(".comparator"));
                String reverse = LogManagerProperties.fromLogManager(p.concat(".comparator.reverse"));
                if (hasValue(name)) {
                    this.comparator = LogManagerProperties.newComparator(name);
                    if (!Boolean.parseBoolean(reverse)) {
                        return;
                    }
                    if ($assertionsDisabled || this.comparator != null) {
                        this.comparator = LogManagerProperties.reverseOrder(this.comparator);
                        return;
                    }
                    throw new AssertionError("null");
                } else if (!isEmpty(reverse)) {
                    throw new IllegalArgumentException("No comparator to reverse.");
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (Exception E) {
                reportError(E.getMessage(), E, 4);
            }
        } else {
            throw new AssertionError();
        }
    }

    private void initPushLevel(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String val = LogManagerProperties.fromLogManager(p.concat(".pushLevel"));
                if (val != null) {
                    this.pushLevel = Level.parse(val);
                }
            } catch (RuntimeException RE) {
                reportError(RE.getMessage(), (Exception) RE, 4);
            }
            if (this.pushLevel == null) {
                this.pushLevel = Level.OFF;
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    private void initPushFilter(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                String name = LogManagerProperties.fromLogManager(p.concat(".pushFilter"));
                if (hasValue(name)) {
                    this.pushFilter = LogManagerProperties.newFilter(name);
                }
            } catch (SecurityException SE) {
                throw SE;
            } catch (Exception E) {
                reportError(E.getMessage(), E, 4);
            }
        } else {
            throw new AssertionError();
        }
    }

    private void initSubject(String p) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            String name = LogManagerProperties.fromLogManager(p.concat(".subject"));
            if (name == null) {
                name = "com.sun.mail.util.logging.CollectorFormatter";
            }
            if (hasValue(name)) {
                try {
                    this.subjectFormatter = LogManagerProperties.newFormatter(name);
                } catch (SecurityException SE) {
                    throw SE;
                } catch (ClassCastException | ClassNotFoundException e) {
                    this.subjectFormatter = TailNameFormatter.m43of(name);
                } catch (Exception E) {
                    this.subjectFormatter = TailNameFormatter.m43of(name);
                    reportError(E.getMessage(), E, 4);
                }
            } else {
                this.subjectFormatter = TailNameFormatter.m43of(name);
            }
        } else {
            throw new AssertionError();
        }
    }

    private boolean isAttachmentLoggable(LogRecord record) {
        Filter[] filters = readOnlyAttachmentFilters();
        for (int i = 0; i < filters.length; i++) {
            Filter f = filters[i];
            if (f == null || f.isLoggable(record)) {
                setMatchedPart(i);
                return true;
            }
        }
        return $assertionsDisabled;
    }

    private boolean isPushable(LogRecord record) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            int value = getPushLevel().intValue();
            if (value == offValue || record.getLevel().intValue() < value) {
                return $assertionsDisabled;
            }
            Filter push = getPushFilter();
            if (push == null) {
                return true;
            }
            int match = getMatchedPart();
            if (match == -1 && getFilter() == push) {
                return true;
            }
            if (match < 0 || this.attachmentFilters[match] != push) {
                return push.isLoggable(record);
            }
            return true;
        }
        throw new AssertionError();
    }

    private void push(boolean priority, int code) {
        if (tryMutex()) {
            try {
                Message msg = writeLogRecords(code);
                if (msg != null) {
                    send(msg, priority, code);
                }
            } catch (LinkageError JDK8152515) {
                reportLinkageError(JDK8152515, code);
            } finally {
                releaseMutex();
            }
        } else {
            reportUnPublishedError((LogRecord) null);
        }
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void send(javax.mail.Message r5, boolean r6, int r7) {
        /*
            r4 = this;
            r4.envelopeFor(r5, r6)     // Catch:{ RuntimeException -> 0x0015, Exception -> 0x001a }
            java.security.PrivilegedAction<java.lang.Object> r3 = MAILHANDLER_LOADER     // Catch:{ RuntimeException -> 0x0015, Exception -> 0x001a }
            java.lang.Object r0 = r4.getAndSetContextClassLoader(r3)     // Catch:{ RuntimeException -> 0x0015, Exception -> 0x001a }
            javax.mail.Transport.send(r5)     // Catch:{ all -> 0x0010 }
            r4.getAndSetContextClassLoader(r0)     // Catch:{ RuntimeException -> 0x0015, Exception -> 0x001a }
        L_0x000f:
            return
        L_0x0010:
            r3 = move-exception
            r4.getAndSetContextClassLoader(r0)     // Catch:{ RuntimeException -> 0x0015, Exception -> 0x001a }
            throw r3     // Catch:{ RuntimeException -> 0x0015, Exception -> 0x001a }
        L_0x0015:
            r2 = move-exception
            r4.reportError((javax.mail.Message) r5, (java.lang.Exception) r2, (int) r7)
            goto L_0x000f
        L_0x001a:
            r1 = move-exception
            r4.reportError((javax.mail.Message) r5, (java.lang.Exception) r1, (int) r7)
            goto L_0x000f
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.MailHandler.send(javax.mail.Message, boolean, int):void");
    }

    private void sort() {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.comparator != null) {
            try {
                if (this.size != 1) {
                    Arrays.sort(this.data, 0, this.size, this.comparator);
                } else if (this.comparator.compare(this.data[0], this.data[0]) != 0) {
                    throw new IllegalArgumentException(this.comparator.getClass().getName());
                }
            } catch (RuntimeException RE) {
                reportError(RE.getMessage(), (Exception) RE, 5);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private javax.mail.Message writeLogRecords(int r5) {
        /*
            r4 = this;
            monitor-enter(r4)     // Catch:{ RuntimeException -> 0x002b, Exception -> 0x0037 }
            int r2 = r4.size     // Catch:{ all -> 0x0028 }
            if (r2 <= 0) goto L_0x0035
            boolean r2 = r4.isWriting     // Catch:{ all -> 0x0028 }
            if (r2 != 0) goto L_0x0035
            r2 = 1
            r4.isWriting = r2     // Catch:{ all -> 0x0028 }
            javax.mail.Message r2 = r4.writeLogRecords0()     // Catch:{ all -> 0x001c }
            r3 = 0
            r4.isWriting = r3     // Catch:{ all -> 0x0028 }
            int r3 = r4.size     // Catch:{ all -> 0x0028 }
            if (r3 <= 0) goto L_0x001a
            r4.reset()     // Catch:{ all -> 0x0028 }
        L_0x001a:
            monitor-exit(r4)     // Catch:{ all -> 0x0028 }
        L_0x001b:
            return r2
        L_0x001c:
            r2 = move-exception
            r3 = 0
            r4.isWriting = r3     // Catch:{ all -> 0x0028 }
            int r3 = r4.size     // Catch:{ all -> 0x0028 }
            if (r3 <= 0) goto L_0x0027
            r4.reset()     // Catch:{ all -> 0x0028 }
        L_0x0027:
            throw r2     // Catch:{ all -> 0x0028 }
        L_0x0028:
            r2 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x0028 }
            throw r2     // Catch:{ RuntimeException -> 0x002b, Exception -> 0x0037 }
        L_0x002b:
            r1 = move-exception
            java.lang.String r2 = r1.getMessage()
            r4.reportError((java.lang.String) r2, (java.lang.Exception) r1, (int) r5)
        L_0x0033:
            r2 = 0
            goto L_0x001b
        L_0x0035:
            monitor-exit(r4)     // Catch:{ all -> 0x0028 }
            goto L_0x0033
        L_0x0037:
            r0 = move-exception
            java.lang.String r2 = r0.getMessage()
            r4.reportError((java.lang.String) r2, (java.lang.Exception) r0, (int) r5)
            goto L_0x0033
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.MailHandler.writeLogRecords(int):javax.mail.Message");
    }

    private Message writeLogRecords0() throws Exception {
        MimePart body;
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            sort();
            if (this.session == null) {
                initSession();
            }
            MimeMessage mimeMessage = new MimeMessage(this.session);
            MimeBodyPart[] parts = new MimeBodyPart[this.attachmentFormatters.length];
            StringBuilder[] buffers = new StringBuilder[parts.length];
            StringBuilder buf = null;
            if (parts.length == 0) {
                mimeMessage.setDescription(descriptionFrom(getFormatter(), getFilter(), this.subjectFormatter));
                body = mimeMessage;
            } else {
                mimeMessage.setDescription(descriptionFrom((Comparator<?>) this.comparator, this.pushLevel, this.pushFilter));
                body = createBodyPart();
            }
            appendSubject(mimeMessage, head(this.subjectFormatter));
            Formatter bodyFormat = getFormatter();
            Filter bodyFilter = getFilter();
            Locale lastLocale = null;
            for (int ix = 0; ix < this.size; ix++) {
                boolean formatted = $assertionsDisabled;
                int match = this.matched[ix];
                LogRecord r = this.data[ix];
                this.data[ix] = null;
                Locale locale = localeFor(r);
                appendSubject(mimeMessage, format(this.subjectFormatter, r));
                Filter lmf = null;
                if (bodyFilter == null || match == -1 || parts.length == 0 || (match < -1 && bodyFilter.isLoggable(r))) {
                    lmf = bodyFilter;
                    if (buf == null) {
                        buf = new StringBuilder();
                        buf.append(head(bodyFormat));
                    }
                    formatted = true;
                    buf.append(format(bodyFormat, r));
                    if (locale != null && !locale.equals(lastLocale)) {
                        appendContentLang(body, locale);
                    }
                }
                for (int i = 0; i < parts.length; i++) {
                    Filter af = this.attachmentFilters[i];
                    if (af == null || lmf == af || match == i || (match < i && af.isLoggable(r))) {
                        if (lmf == null && af != null) {
                            lmf = af;
                        }
                        if (parts[i] == null) {
                            parts[i] = createBodyPart(i);
                            buffers[i] = new StringBuilder();
                            buffers[i].append(head(this.attachmentFormatters[i]));
                            appendFileName(parts[i], head(this.attachmentNames[i]));
                        }
                        formatted = true;
                        appendFileName(parts[i], format(this.attachmentNames[i], r));
                        buffers[i].append(format(this.attachmentFormatters[i], r));
                        if (locale != null && !locale.equals(lastLocale)) {
                            appendContentLang(parts[i], locale);
                        }
                    }
                }
                if (!formatted) {
                    reportFilterError(r);
                } else if (!(body == mimeMessage || locale == null || locale.equals(lastLocale))) {
                    appendContentLang(mimeMessage, locale);
                }
                lastLocale = locale;
            }
            this.size = 0;
            for (int i2 = parts.length - 1; i2 >= 0; i2--) {
                if (parts[i2] != null) {
                    appendFileName(parts[i2], tail(this.attachmentNames[i2], NotificationCompat.CATEGORY_ERROR));
                    buffers[i2].append(tail(this.attachmentFormatters[i2], ""));
                    if (buffers[i2].length() > 0) {
                        String name = parts[i2].getFileName();
                        if (isEmpty(name)) {
                            name = toString(this.attachmentFormatters[i2]);
                            parts[i2].setFileName(name);
                        }
                        setContent(parts[i2], buffers[i2], getContentType(name));
                    } else {
                        setIncompleteCopy(mimeMessage);
                        parts[i2] = null;
                    }
                    buffers[i2] = null;
                }
            }
            if (buf != null) {
                buf.append(tail(bodyFormat, ""));
            } else {
                buf = new StringBuilder(0);
            }
            appendSubject(mimeMessage, tail(this.subjectFormatter, ""));
            String contentType = contentTypeOf((CharSequence) buf);
            String altType = contentTypeOf(bodyFormat);
            if (altType != null) {
                contentType = altType;
            }
            setContent(body, buf, contentType);
            if (body != mimeMessage) {
                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart((BodyPart) body);
                for (int i3 = 0; i3 < parts.length; i3++) {
                    if (parts[i3] != null) {
                        multipart.addBodyPart(parts[i3]);
                    }
                }
                mimeMessage.setContent(multipart);
            }
            return mimeMessage;
        }
        throw new AssertionError();
    }

    private void verifySettings(Session session2) {
        if (session2 != null) {
            try {
                Object check = session2.getProperties().put("verify", "");
                if (check instanceof String) {
                    String value = (String) check;
                    if (hasValue(value)) {
                        verifySettings0(session2, value);
                    }
                } else if (check != null) {
                    verifySettings0(session2, check.getClass().toString());
                }
            } catch (LinkageError JDK8152515) {
                reportLinkageError(JDK8152515, 4);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processExcHandler(RegionMaker.java:1043)
        	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:975)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
        */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x02ff A[ExcHandler: SendFailedException (r28v0 'sfe' javax.mail.SendFailedException A[CUSTOM_DECLARE]), PHI: r16 r20 
      PHI: (r16v2 'closed' javax.mail.MessagingException) = (r16v0 'closed' javax.mail.MessagingException), (r16v4 'closed' javax.mail.MessagingException), (r16v4 'closed' javax.mail.MessagingException), (r16v0 'closed' javax.mail.MessagingException), (r16v0 'closed' javax.mail.MessagingException), (r16v7 'closed' javax.mail.MessagingException), (r16v7 'closed' javax.mail.MessagingException), (r16v7 'closed' javax.mail.MessagingException), (r16v0 'closed' javax.mail.MessagingException) binds: [B:127:0x02fb, B:129:0x02fe, B:130:?, B:128:?, B:66:0x01d8, B:69:0x01e1, B:145:0x033d, B:146:?, B:67:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r20v5 'local' java.lang.String) = (r20v0 'local' java.lang.String), (r20v0 'local' java.lang.String), (r20v0 'local' java.lang.String), (r20v0 'local' java.lang.String), (r20v7 'local' java.lang.String), (r20v7 'local' java.lang.String), (r20v7 'local' java.lang.String), (r20v7 'local' java.lang.String), (r20v7 'local' java.lang.String) binds: [B:127:0x02fb, B:129:0x02fe, B:130:?, B:128:?, B:66:0x01d8, B:69:0x01e1, B:145:0x033d, B:146:?, B:67:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:66:0x01d8] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:66:0x01d8=Splitter:B:66:0x01d8, B:129:0x02fe=Splitter:B:129:0x02fe} */
    private void verifySettings0(javax.mail.Session r35, java.lang.String r36) {
        /*
            r34 = this;
            boolean r31 = $assertionsDisabled
            if (r31 != 0) goto L_0x0014
            if (r36 != 0) goto L_0x0014
            java.lang.AssertionError r32 = new java.lang.AssertionError
            r31 = 0
            java.lang.String r31 = (java.lang.String) r31
            r0 = r32
            r1 = r31
            r0.<init>(r1)
            throw r32
        L_0x0014:
            java.lang.String r31 = "local"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)
            if (r31 != 0) goto L_0x0069
            java.lang.String r31 = "remote"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)
            if (r31 != 0) goto L_0x0069
            java.lang.String r31 = "limited"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)
            if (r31 != 0) goto L_0x0069
            java.lang.String r31 = "resolve"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)
            if (r31 != 0) goto L_0x0069
            java.lang.String r31 = "login"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)
            if (r31 != 0) goto L_0x0069
            java.lang.String r31 = "Verify must be 'limited', local', 'resolve', 'login', or 'remote'."
            java.lang.IllegalArgumentException r32 = new java.lang.IllegalArgumentException
            r0 = r32
            r1 = r36
            r0.<init>(r1)
            r33 = 4
            r0 = r34
            r1 = r31
            r2 = r32
            r3 = r33
            r0.reportError((java.lang.String) r1, (java.lang.Exception) r2, (int) r3)
        L_0x0068:
            return
        L_0x0069:
            javax.mail.internet.MimeMessage r8 = new javax.mail.internet.MimeMessage
            r0 = r35
            r8.<init>((javax.mail.Session) r0)
            java.lang.String r31 = "limited"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)
            if (r31 != 0) goto L_0x0138
            java.lang.StringBuilder r31 = new java.lang.StringBuilder
            r31.<init>()
            java.lang.String r32 = "Local address is "
            java.lang.StringBuilder r31 = r31.append(r32)
            javax.mail.internet.InternetAddress r32 = javax.mail.internet.InternetAddress.getLocalAddress(r35)
            java.lang.StringBuilder r31 = r31.append(r32)
            r32 = 46
            java.lang.StringBuilder r31 = r31.append(r32)
            java.lang.String r23 = r31.toString()
            java.lang.String r31 = r34.getEncodingName()     // Catch:{ RuntimeException -> 0x011c }
            java.nio.charset.Charset.forName(r31)     // Catch:{ RuntimeException -> 0x011c }
        L_0x00a0:
            monitor-enter(r34)
            r0 = r34
            java.util.logging.Formatter r0 = r0.subjectFormatter     // Catch:{ all -> 0x0159 }
            r31 = r0
            r0 = r34
            r1 = r31
            java.lang.String r31 = r0.head(r1)     // Catch:{ all -> 0x0159 }
            r0 = r34
            r1 = r31
            r0.appendSubject(r8, r1)     // Catch:{ all -> 0x0159 }
            r0 = r34
            java.util.logging.Formatter r0 = r0.subjectFormatter     // Catch:{ all -> 0x0159 }
            r31 = r0
            java.lang.String r32 = ""
            r0 = r34
            r1 = r31
            r2 = r32
            java.lang.String r31 = r0.tail(r1, r2)     // Catch:{ all -> 0x0159 }
            r0 = r34
            r1 = r31
            r0.appendSubject(r8, r1)     // Catch:{ all -> 0x0159 }
            r0 = r34
            java.util.logging.Formatter[] r0 = r0.attachmentNames     // Catch:{ all -> 0x0159 }
            r31 = r0
            r0 = r31
            int r0 = r0.length     // Catch:{ all -> 0x0159 }
            r31 = r0
            r0 = r31
            java.lang.String[] r12 = new java.lang.String[r0]     // Catch:{ all -> 0x0159 }
            r19 = 0
        L_0x00e0:
            int r0 = r12.length     // Catch:{ all -> 0x0159 }
            r31 = r0
            r0 = r19
            r1 = r31
            if (r0 >= r1) goto L_0x015c
            r0 = r34
            java.util.logging.Formatter[] r0 = r0.attachmentNames     // Catch:{ all -> 0x0159 }
            r31 = r0
            r31 = r31[r19]     // Catch:{ all -> 0x0159 }
            r0 = r34
            r1 = r31
            java.lang.String r31 = r0.head(r1)     // Catch:{ all -> 0x0159 }
            r12[r19] = r31     // Catch:{ all -> 0x0159 }
            r31 = r12[r19]     // Catch:{ all -> 0x0159 }
            int r31 = r31.length()     // Catch:{ all -> 0x0159 }
            if (r31 != 0) goto L_0x013c
            r0 = r34
            java.util.logging.Formatter[] r0 = r0.attachmentNames     // Catch:{ all -> 0x0159 }
            r31 = r0
            r31 = r31[r19]     // Catch:{ all -> 0x0159 }
            java.lang.String r32 = ""
            r0 = r34
            r1 = r31
            r2 = r32
            java.lang.String r31 = r0.tail(r1, r2)     // Catch:{ all -> 0x0159 }
            r12[r19] = r31     // Catch:{ all -> 0x0159 }
        L_0x0119:
            int r19 = r19 + 1
            goto L_0x00e0
        L_0x011c:
            r6 = move-exception
            java.io.UnsupportedEncodingException r7 = new java.io.UnsupportedEncodingException
            java.lang.String r31 = r6.toString()
            r0 = r31
            r7.<init>(r0)
            r7.initCause(r6)
            r31 = 5
            r0 = r34
            r1 = r23
            r2 = r31
            r0.reportError((java.lang.String) r1, (java.lang.Exception) r7, (int) r2)
            goto L_0x00a0
        L_0x0138:
            java.lang.String r23 = "Skipping local address check."
            goto L_0x00a0
        L_0x013c:
            r31 = r12[r19]     // Catch:{ all -> 0x0159 }
            r0 = r34
            java.util.logging.Formatter[] r0 = r0.attachmentNames     // Catch:{ all -> 0x0159 }
            r32 = r0
            r32 = r32[r19]     // Catch:{ all -> 0x0159 }
            java.lang.String r33 = ""
            r0 = r34
            r1 = r32
            r2 = r33
            java.lang.String r32 = r0.tail(r1, r2)     // Catch:{ all -> 0x0159 }
            java.lang.String r31 = r31.concat(r32)     // Catch:{ all -> 0x0159 }
            r12[r19] = r31     // Catch:{ all -> 0x0159 }
            goto L_0x0119
        L_0x0159:
            r31 = move-exception
            monitor-exit(r34)     // Catch:{ all -> 0x0159 }
            throw r31
        L_0x015c:
            monitor-exit(r34)     // Catch:{ all -> 0x0159 }
            r0 = r34
            r0.setIncompleteCopy(r8)
            r31 = 1
            r0 = r34
            r1 = r31
            r0.envelopeFor(r8, r1)
            r0 = r34
            r1 = r23
            r0.saveChangesNoContent(r8, r1)
            javax.mail.Address[] r9 = r8.getAllRecipients()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r9 != 0) goto L_0x017e
            r31 = 0
            r0 = r31
            javax.mail.internet.InternetAddress[] r9 = new javax.mail.internet.InternetAddress[r0]     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x017e:
            int r0 = r9.length     // Catch:{ MessagingException -> 0x02a8 }
            r31 = r0
            if (r31 == 0) goto L_0x0289
            r11 = r9
        L_0x0184:
            if (r11 == 0) goto L_0x028f
            int r0 = r11.length     // Catch:{ MessagingException -> 0x02a8 }
            r31 = r0
            if (r31 == 0) goto L_0x028f
            r31 = 0
            r31 = r11[r31]     // Catch:{ MessagingException -> 0x02a8 }
            r0 = r35
            r1 = r31
            javax.mail.Transport r29 = r0.getTransport((javax.mail.Address) r1)     // Catch:{ MessagingException -> 0x02a8 }
            java.lang.String r31 = "mail.transport.protocol"
            r0 = r35
            r1 = r31
            r0.getProperty(r1)     // Catch:{ MessagingException -> 0x02a8 }
        L_0x01a0:
            r20 = 0
            java.lang.String r31 = "remote"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 != 0) goto L_0x01ba
            java.lang.String r31 = "login"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 == 0) goto L_0x0369
        L_0x01ba:
            r16 = 0
            r29.connect()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r34
            r1 = r29
            java.lang.String r20 = r0.getLocalHost(r1)     // Catch:{ all -> 0x02fa }
            java.lang.String r31 = "remote"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ all -> 0x02fa }
            if (r31 == 0) goto L_0x01d8
            r0 = r29
            r0.sendMessage(r8, r9)     // Catch:{ all -> 0x02fa }
        L_0x01d8:
            r29.close()     // Catch:{ MessagingException -> 0x02f5, SendFailedException -> 0x02ff }
        L_0x01db:
            java.lang.String r31 = "remote"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ SendFailedException -> 0x02ff, MessagingException -> 0x034e }
            if (r31 == 0) goto L_0x033d
            r31 = 0
            r0 = r34
            r1 = r36
            r2 = r31
            r0.reportUnexpectedSend(r8, r1, r2)     // Catch:{ SendFailedException -> 0x02ff, MessagingException -> 0x034e }
        L_0x01f2:
            if (r16 == 0) goto L_0x0208
            r0 = r34
            r1 = r36
            r2 = r16
            r0.setErrorContent(r8, r1, r2)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = 3
            r0 = r34
            r1 = r16
            r2 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r1, (int) r2)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x0208:
            java.lang.String r31 = "limited"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 != 0) goto L_0x04f8
            java.lang.String r31 = "remote"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ RuntimeException -> 0x047e, IOException -> 0x05bc, Exception -> 0x02e2 }
            if (r31 != 0) goto L_0x0234
            java.lang.String r31 = "login"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ RuntimeException -> 0x047e, IOException -> 0x05bc, Exception -> 0x02e2 }
            if (r31 != 0) goto L_0x0234
            r0 = r34
            r1 = r29
            java.lang.String r20 = r0.getLocalHost(r1)     // Catch:{ RuntimeException -> 0x047e, IOException -> 0x05bc, Exception -> 0x02e2 }
        L_0x0234:
            verifyHost(r20)     // Catch:{ RuntimeException -> 0x047e, IOException -> 0x05bc, Exception -> 0x02e2 }
        L_0x0237:
            java.security.PrivilegedAction<java.lang.Object> r31 = MAILHANDLER_LOADER     // Catch:{ IOException -> 0x04e0 }
            r0 = r34
            r1 = r31
            java.lang.Object r15 = r0.getAndSetContextClassLoader(r1)     // Catch:{ IOException -> 0x04e0 }
            javax.mail.internet.MimeMultipart r24 = new javax.mail.internet.MimeMultipart     // Catch:{ all -> 0x04d9 }
            r24.<init>()     // Catch:{ all -> 0x04d9 }
            int r0 = r12.length     // Catch:{ all -> 0x04d9 }
            r31 = r0
            r0 = r31
            javax.mail.internet.MimeBodyPart[] r10 = new javax.mail.internet.MimeBodyPart[r0]     // Catch:{ all -> 0x04d9 }
            monitor-enter(r34)     // Catch:{ all -> 0x04d9 }
            java.util.logging.Formatter r31 = r34.getFormatter()     // Catch:{ all -> 0x04d6 }
            r0 = r34
            r1 = r31
            java.lang.String r14 = r0.contentTypeOf((java.util.logging.Formatter) r1)     // Catch:{ all -> 0x04d6 }
            javax.mail.internet.MimeBodyPart r13 = r34.createBodyPart()     // Catch:{ all -> 0x04d6 }
            r19 = 0
        L_0x0260:
            int r0 = r12.length     // Catch:{ all -> 0x04d6 }
            r31 = r0
            r0 = r19
            r1 = r31
            if (r0 >= r1) goto L_0x049a
            r0 = r34
            r1 = r19
            javax.mail.internet.MimeBodyPart r31 = r0.createBodyPart(r1)     // Catch:{ all -> 0x04d6 }
            r10[r19] = r31     // Catch:{ all -> 0x04d6 }
            r31 = r10[r19]     // Catch:{ all -> 0x04d6 }
            r32 = r12[r19]     // Catch:{ all -> 0x04d6 }
            r31.setFileName(r32)     // Catch:{ all -> 0x04d6 }
            r31 = r12[r19]     // Catch:{ all -> 0x04d6 }
            r0 = r34
            r1 = r31
            java.lang.String r31 = r0.getContentType(r1)     // Catch:{ all -> 0x04d6 }
            r12[r19] = r31     // Catch:{ all -> 0x04d6 }
            int r19 = r19 + 1
            goto L_0x0260
        L_0x0289:
            javax.mail.Address[] r11 = r8.getFrom()     // Catch:{ MessagingException -> 0x02a8 }
            goto L_0x0184
        L_0x028f:
            javax.mail.MessagingException r22 = new javax.mail.MessagingException     // Catch:{ MessagingException -> 0x02a8 }
            java.lang.String r31 = "No recipient or from address."
            r0 = r22
            r1 = r31
            r0.<init>(r1)     // Catch:{ MessagingException -> 0x02a8 }
            r31 = 4
            r0 = r34
            r1 = r23
            r2 = r22
            r3 = r31
            r0.reportError((java.lang.String) r1, (java.lang.Exception) r2, (int) r3)     // Catch:{ MessagingException -> 0x02a8 }
            throw r22     // Catch:{ MessagingException -> 0x02a8 }
        L_0x02a8:
            r25 = move-exception
            java.security.PrivilegedAction<java.lang.Object> r31 = MAILHANDLER_LOADER     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r34
            r1 = r31
            java.lang.Object r15 = r0.getAndSetContextClassLoader(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            javax.mail.Transport r29 = r35.getTransport()     // Catch:{ MessagingException -> 0x02d1 }
            r0 = r34
            r0.getAndSetContextClassLoader(r15)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x01a0
        L_0x02be:
            r6 = move-exception
            r0 = r34
            r1 = r36
            r0.setErrorContent(r8, r1, r6)
            r31 = 4
            r0 = r34
            r1 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r6, (int) r1)
            goto L_0x0068
        L_0x02d1:
            r17 = move-exception
            r0 = r25
            r1 = r17
            javax.mail.MessagingException r31 = attach(r0, r1)     // Catch:{ all -> 0x02db }
            throw r31     // Catch:{ all -> 0x02db }
        L_0x02db:
            r31 = move-exception
            r0 = r34
            r0.getAndSetContextClassLoader(r15)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            throw r31     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x02e2:
            r5 = move-exception
            r0 = r34
            r1 = r36
            r0.setErrorContent(r8, r1, r5)
            r31 = 4
            r0 = r34
            r1 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r5, (int) r1)
            goto L_0x0068
        L_0x02f5:
            r5 = move-exception
            r16 = r5
            goto L_0x01db
        L_0x02fa:
            r31 = move-exception
            r29.close()     // Catch:{ MessagingException -> 0x0339, SendFailedException -> 0x02ff }
        L_0x02fe:
            throw r31     // Catch:{ SendFailedException -> 0x02ff, MessagingException -> 0x034e }
        L_0x02ff:
            r28 = move-exception
            javax.mail.Address[] r26 = r28.getInvalidAddresses()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r26 == 0) goto L_0x0321
            r0 = r26
            int r0 = r0.length     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = r0
            if (r31 == 0) goto L_0x0321
            r0 = r34
            r1 = r36
            r2 = r28
            r0.setErrorContent(r8, r1, r2)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = 4
            r0 = r34
            r1 = r28
            r2 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r1, (int) r2)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x0321:
            javax.mail.Address[] r26 = r28.getValidSentAddresses()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r26 == 0) goto L_0x01f2
            r0 = r26
            int r0 = r0.length     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = r0
            if (r31 == 0) goto L_0x01f2
            r0 = r34
            r1 = r36
            r2 = r28
            r0.reportUnexpectedSend(r8, r1, r2)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x01f2
        L_0x0339:
            r5 = move-exception
            r16 = r5
            goto L_0x02fe
        L_0x033d:
            javax.mail.URLName r31 = r29.getURLName()     // Catch:{ SendFailedException -> 0x02ff, MessagingException -> 0x034e }
            java.lang.String r25 = r31.getProtocol()     // Catch:{ SendFailedException -> 0x02ff, MessagingException -> 0x034e }
            r0 = r35
            r1 = r25
            verifyProperties(r0, r1)     // Catch:{ SendFailedException -> 0x02ff, MessagingException -> 0x034e }
            goto L_0x01f2
        L_0x034e:
            r5 = move-exception
            r0 = r34
            boolean r31 = r0.isMissingContent(r8, r5)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 != 0) goto L_0x01f2
            r0 = r34
            r1 = r36
            r0.setErrorContent(r8, r1, r5)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = 4
            r0 = r34
            r1 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r5, (int) r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x01f2
        L_0x0369:
            javax.mail.URLName r31 = r29.getURLName()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r25 = r31.getProtocol()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r35
            r1 = r25
            verifyProperties(r0, r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.StringBuilder r31 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31.<init>()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = "mail."
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r25
            java.lang.StringBuilder r31 = r0.append(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = ".host"
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r31 = r31.toString()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r35
            r1 = r31
            java.lang.String r21 = r0.getProperty(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            boolean r31 = isEmpty(r21)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 == 0) goto L_0x0445
            java.lang.String r31 = "mail.host"
            r0 = r35
            r1 = r31
            java.lang.String r21 = r0.getProperty(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x03ad:
            java.lang.StringBuilder r31 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31.<init>()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = "mail."
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r25
            java.lang.StringBuilder r31 = r0.append(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = ".localhost"
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r31 = r31.toString()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r35
            r1 = r31
            java.lang.String r20 = r0.getProperty(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            boolean r31 = isEmpty(r20)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 == 0) goto L_0x0450
            java.lang.StringBuilder r31 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31.<init>()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = "mail."
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r25
            java.lang.StringBuilder r31 = r0.append(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = ".localaddress"
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r31 = r31.toString()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r35
            r1 = r31
            java.lang.String r20 = r0.getProperty(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x03fd:
            java.lang.String r31 = "resolve"
            r0 = r31
            r1 = r36
            boolean r31 = r0.equals(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 == 0) goto L_0x0208
            javax.mail.URLName r31 = r29.getURLName()     // Catch:{ RuntimeException -> 0x0429, IOException -> 0x047a, Exception -> 0x02e2 }
            java.lang.String r30 = r31.getHost()     // Catch:{ RuntimeException -> 0x0429, IOException -> 0x047a, Exception -> 0x02e2 }
            boolean r31 = isEmpty(r30)     // Catch:{ RuntimeException -> 0x0429, IOException -> 0x047a, Exception -> 0x02e2 }
            if (r31 != 0) goto L_0x0475
            verifyHost(r30)     // Catch:{ RuntimeException -> 0x0429, IOException -> 0x047a, Exception -> 0x02e2 }
            r0 = r30
            r1 = r21
            boolean r31 = r0.equalsIgnoreCase(r1)     // Catch:{ RuntimeException -> 0x0429, IOException -> 0x047a, Exception -> 0x02e2 }
            if (r31 != 0) goto L_0x0208
            verifyHost(r21)     // Catch:{ RuntimeException -> 0x0429, IOException -> 0x047a, Exception -> 0x02e2 }
            goto L_0x0208
        L_0x0429:
            r31 = move-exception
            r4 = r31
        L_0x042c:
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r23
            r5.<init>(r0, r4)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r34
            r1 = r36
            r0.setErrorContent(r8, r1, r5)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = 4
            r0 = r34
            r1 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r5, (int) r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x0208
        L_0x0445:
            java.lang.String r31 = "mail.host"
            r0 = r35
            r1 = r31
            r0.getProperty(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x03ad
        L_0x0450:
            java.lang.StringBuilder r31 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31.<init>()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = "mail."
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r25
            java.lang.StringBuilder r31 = r0.append(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = ".localaddress"
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r31 = r31.toString()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r35
            r1 = r31
            r0.getProperty(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x03fd
        L_0x0475:
            verifyHost(r21)     // Catch:{ RuntimeException -> 0x0429, IOException -> 0x047a, Exception -> 0x02e2 }
            goto L_0x0208
        L_0x047a:
            r31 = move-exception
            r4 = r31
            goto L_0x042c
        L_0x047e:
            r31 = move-exception
            r4 = r31
        L_0x0481:
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r23
            r5.<init>(r0, r4)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r34
            r1 = r36
            r0.setErrorContent(r8, r1, r5)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = 4
            r0 = r34
            r1 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r5, (int) r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x0237
        L_0x049a:
            monitor-exit(r34)     // Catch:{ all -> 0x04d6 }
            r0 = r36
            r13.setDescription(r0)     // Catch:{ all -> 0x04d9 }
            java.lang.String r31 = ""
            r0 = r34
            r1 = r31
            r0.setContent(r13, r1, r14)     // Catch:{ all -> 0x04d9 }
            r0 = r24
            r0.addBodyPart(r13)     // Catch:{ all -> 0x04d9 }
            r19 = 0
        L_0x04b0:
            int r0 = r10.length     // Catch:{ all -> 0x04d9 }
            r31 = r0
            r0 = r19
            r1 = r31
            if (r0 >= r1) goto L_0x0578
            r31 = r10[r19]     // Catch:{ all -> 0x04d9 }
            r0 = r31
            r1 = r36
            r0.setDescription(r1)     // Catch:{ all -> 0x04d9 }
            r31 = r10[r19]     // Catch:{ all -> 0x04d9 }
            java.lang.String r32 = ""
            r33 = r12[r19]     // Catch:{ all -> 0x04d9 }
            r0 = r34
            r1 = r31
            r2 = r32
            r3 = r33
            r0.setContent(r1, r2, r3)     // Catch:{ all -> 0x04d9 }
            int r19 = r19 + 1
            goto L_0x04b0
        L_0x04d6:
            r31 = move-exception
            monitor-exit(r34)     // Catch:{ all -> 0x04d6 }
            throw r31     // Catch:{ all -> 0x04d9 }
        L_0x04d9:
            r31 = move-exception
            r0 = r34
            r0.getAndSetContextClassLoader(r15)     // Catch:{ IOException -> 0x04e0 }
            throw r31     // Catch:{ IOException -> 0x04e0 }
        L_0x04e0:
            r4 = move-exception
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r23
            r5.<init>(r0, r4)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r34
            r1 = r36
            r0.setErrorContent(r8, r1, r5)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = 5
            r0 = r34
            r1 = r31
            r0.reportError((javax.mail.Message) r8, (java.lang.Exception) r5, (int) r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x04f8:
            int r0 = r9.length     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = r0
            if (r31 == 0) goto L_0x0593
            verifyAddresses(r9)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            javax.mail.Address[] r18 = r8.getFrom()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            javax.mail.Address r27 = r8.getSender()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r27
            boolean r0 = r0 instanceof javax.mail.internet.InternetAddress     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = r0
            if (r31 == 0) goto L_0x0519
            r0 = r27
            javax.mail.internet.InternetAddress r0 = (javax.mail.internet.InternetAddress) r0     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = r0
            r31.validate()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x0519:
            java.lang.String r31 = "From"
            java.lang.String r32 = ","
            r0 = r31
            r1 = r32
            java.lang.String r31 = r8.getHeader(r0, r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 == 0) goto L_0x059e
            r0 = r18
            int r0 = r0.length     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = r0
            if (r31 == 0) goto L_0x059e
            verifyAddresses(r18)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r19 = 0
        L_0x0533:
            r0 = r18
            int r0 = r0.length     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31 = r0
            r0 = r19
            r1 = r31
            if (r0 >= r1) goto L_0x05b3
            r31 = r18[r19]     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r27
            boolean r31 = r0.equals(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            if (r31 == 0) goto L_0x059b
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.StringBuilder r31 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r31.<init>()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = "Sender address '"
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r27
            java.lang.StringBuilder r31 = r0.append(r1)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = "' equals from address."
            java.lang.StringBuilder r31 = r31.append(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r31 = r31.toString()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r5.<init>(r0)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            javax.mail.MessagingException r31 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r23
            r0.<init>(r1, r5)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            throw r31     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x0578:
            r0 = r24
            r8.setContent(r0)     // Catch:{ all -> 0x04d9 }
            r8.saveChanges()     // Catch:{ all -> 0x04d9 }
            java.io.ByteArrayOutputStream r31 = new java.io.ByteArrayOutputStream     // Catch:{ all -> 0x04d9 }
            r32 = 1024(0x400, float:1.435E-42)
            r31.<init>(r32)     // Catch:{ all -> 0x04d9 }
            r0 = r31
            r8.writeTo(r0)     // Catch:{ all -> 0x04d9 }
            r0 = r34
            r0.getAndSetContextClassLoader(r15)     // Catch:{ IOException -> 0x04e0 }
            goto L_0x04f8
        L_0x0593:
            javax.mail.MessagingException r31 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r32 = "No recipient addresses."
            r31.<init>(r32)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            throw r31     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x059b:
            int r19 = r19 + 1
            goto L_0x0533
        L_0x059e:
            if (r27 != 0) goto L_0x05b3
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            java.lang.String r31 = "No from or sender address."
            r0 = r31
            r5.<init>(r0)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            javax.mail.MessagingException r31 = new javax.mail.MessagingException     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            r0 = r31
            r1 = r23
            r0.<init>(r1, r5)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            throw r31     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
        L_0x05b3:
            javax.mail.Address[] r31 = r8.getReplyTo()     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            verifyAddresses(r31)     // Catch:{ RuntimeException -> 0x02be, Exception -> 0x02e2 }
            goto L_0x0068
        L_0x05bc:
            r31 = move-exception
            r4 = r31
            goto L_0x0481
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.MailHandler.verifySettings0(javax.mail.Session, java.lang.String):void");
    }

    private void saveChangesNoContent(Message abort, String msg) {
        Exception e;
        if (abort != null) {
            try {
                abort.saveChanges();
                return;
            } catch (NullPointerException xferEncoding) {
                if (abort.getHeader("Content-Transfer-Encoding") == null) {
                    abort.setHeader("Content-Transfer-Encoding", "base64");
                    abort.saveChanges();
                    return;
                }
                throw xferEncoding;
            } catch (RuntimeException e2) {
                e = e2;
            } catch (MessagingException e3) {
                e = e3;
            }
        } else {
            return;
        }
        if (e != xferEncoding) {
            try {
                e.addSuppressed(xferEncoding);
            } catch (RuntimeException | MessagingException ME) {
                reportError(msg, ME, 5);
                return;
            }
        }
        throw e;
    }

    private static void verifyProperties(Session session2, String protocol) {
        session2.getProperty("mail.from");
        session2.getProperty("mail." + protocol + ".from");
        session2.getProperty("mail.dsn.ret");
        session2.getProperty("mail." + protocol + ".dsn.ret");
        session2.getProperty("mail.dsn.notify");
        session2.getProperty("mail." + protocol + ".dsn.notify");
        session2.getProperty("mail." + protocol + ".port");
        session2.getProperty("mail.user");
        session2.getProperty("mail." + protocol + ".user");
        session2.getProperty("mail." + protocol + ".localport");
    }

    private static InetAddress verifyHost(String host) throws IOException {
        InetAddress a;
        if (isEmpty(host)) {
            a = InetAddress.getLocalHost();
        } else {
            a = InetAddress.getByName(host);
        }
        if (a.getCanonicalHostName().length() != 0) {
            return a;
        }
        throw new UnknownHostException();
    }

    private static void verifyAddresses(Address[] all) throws AddressException {
        if (all != null) {
            for (Address a : all) {
                if (a instanceof InternetAddress) {
                    ((InternetAddress) a).validate();
                }
            }
        }
    }

    private void reportUnexpectedSend(MimeMessage msg, String verify, Exception cause) {
        MessagingException write = new MessagingException("An empty message was sent.", cause);
        setErrorContent(msg, verify, write);
        reportError((Message) msg, (Exception) write, 4);
    }

    private void setErrorContent(MimeMessage msg, String verify, Throwable t) {
        MimeBodyPart body;
        String msgDesc;
        String subjectType;
        String name;
        try {
            synchronized (this) {
                body = createBodyPart();
                msgDesc = descriptionFrom((Comparator<?>) this.comparator, this.pushLevel, this.pushFilter);
                subjectType = getClassId(this.subjectFormatter);
            }
            StringBuilder append = new StringBuilder().append("Formatted using ");
            if (t == null) {
                name = Throwable.class.getName();
            } else {
                name = t.getClass().getName();
            }
            body.setDescription(append.append(name).append(", filtered with ").append(verify).append(", and named by ").append(subjectType).append('.').toString());
            setContent(body, toMsgString(t), NanoHTTPD.MIME_PLAINTEXT);
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(body);
            msg.setContent(multipart);
            msg.setDescription(msgDesc);
            setAcceptLang(msg);
            msg.saveChanges();
        } catch (RuntimeException | MessagingException ME) {
            reportError("Unable to create body.", ME, 4);
        }
    }

    private Session updateSession() {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.mailProps.getProperty("verify") != null) {
            Session settings = initSession();
            if ($assertionsDisabled || settings == this.session) {
                return settings;
            }
            throw new AssertionError(this.session);
        } else {
            this.session = null;
            return null;
        }
    }

    private Session initSession() {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            this.session = Session.getInstance(new LogManagerProperties(this.mailProps, getClass().getName()), this.auth);
            return this.session;
        }
        throw new AssertionError();
    }

    private void envelopeFor(Message msg, boolean priority) {
        setAcceptLang(msg);
        setFrom(msg);
        if (!setRecipient(msg, "mail.to", Message.RecipientType.f298TO)) {
            setDefaultRecipient(msg, Message.RecipientType.f298TO);
        }
        setRecipient(msg, "mail.cc", Message.RecipientType.f297CC);
        setRecipient(msg, "mail.bcc", Message.RecipientType.BCC);
        setReplyTo(msg);
        setSender(msg);
        setMailer(msg);
        setAutoSubmitted(msg);
        if (priority) {
            setPriority(msg);
        }
        try {
            msg.setSentDate(new Date());
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private MimeBodyPart createBodyPart() throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            MimeBodyPart part = new MimeBodyPart();
            part.setDisposition(Part.INLINE);
            part.setDescription(descriptionFrom(getFormatter(), getFilter(), this.subjectFormatter));
            setAcceptLang(part);
            return part;
        }
        throw new AssertionError();
    }

    private MimeBodyPart createBodyPart(int index) throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            MimeBodyPart part = new MimeBodyPart();
            part.setDisposition(Part.ATTACHMENT);
            part.setDescription(descriptionFrom(this.attachmentFormatters[index], this.attachmentFilters[index], this.attachmentNames[index]));
            setAcceptLang(part);
            return part;
        }
        throw new AssertionError();
    }

    private String descriptionFrom(Comparator<?> c, Level l, Filter f) {
        String name;
        String name2;
        StringBuilder append = new StringBuilder().append("Sorted using ");
        if (c == null) {
            name = "no comparator";
        } else {
            name = c.getClass().getName();
        }
        StringBuilder append2 = append.append(name).append(", pushed when ").append(l.getName()).append(", and ");
        if (f == null) {
            name2 = "no push filter";
        } else {
            name2 = f.getClass().getName();
        }
        return append2.append(name2).append('.').toString();
    }

    private String descriptionFrom(Formatter f, Filter filter2, Formatter name) {
        String name2;
        StringBuilder append = new StringBuilder().append("Formatted using ").append(getClassId(f)).append(", filtered with ");
        if (filter2 == null) {
            name2 = "no filter";
        } else {
            name2 = filter2.getClass().getName();
        }
        return append.append(name2).append(", and named by ").append(getClassId(name)).append('.').toString();
    }

    private String getClassId(Formatter f) {
        if (f instanceof TailNameFormatter) {
            return String.class.getName();
        }
        return f.getClass().getName();
    }

    private String toString(Formatter f) {
        String name = f.toString();
        return !isEmpty(name) ? name : getClassId(f);
    }

    private void appendFileName(Part part, String chunk) {
        if (chunk == null) {
            reportNullError(5);
        } else if (chunk.length() > 0) {
            appendFileName0(part, chunk);
        }
    }

    private void appendFileName0(Part part, String chunk) {
        String str;
        try {
            String chunk2 = chunk.replaceAll("[\\x00-\\x1F\\x7F]+", "");
            String old = part.getFileName();
            if (old != null) {
                str = old.concat(chunk2);
            } else {
                str = chunk2;
            }
            part.setFileName(str);
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private void appendSubject(Message msg, String chunk) {
        if (chunk == null) {
            reportNullError(5);
        } else if (chunk.length() > 0) {
            appendSubject0(msg, chunk);
        }
    }

    private void appendSubject0(Message msg, String chunk) {
        try {
            String chunk2 = chunk.replaceAll("[\\x00-\\x1F\\x7F]+", "");
            String charset = getEncodingName();
            String old = msg.getSubject();
            if ($assertionsDisabled || (msg instanceof MimeMessage)) {
                ((MimeMessage) msg).setSubject(old != null ? old.concat(chunk2) : chunk2, MimeUtility.mimeCharset(charset));
                return;
            }
            throw new AssertionError(msg);
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private Locale localeFor(LogRecord r) {
        ResourceBundle rb = r.getResourceBundle();
        if (rb == null) {
            return null;
        }
        Locale l = rb.getLocale();
        if (l == null || isEmpty(l.getLanguage())) {
            return Locale.getDefault();
        }
        return l;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0046 A[Catch:{ MessagingException -> 0x006d }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:? A[Catch:{ MessagingException -> 0x006d }, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void appendContentLang(javax.mail.internet.MimePart r8, java.util.Locale r9) {
        /*
            r7 = this;
            java.lang.String r3 = com.sun.mail.util.logging.LogManagerProperties.toLanguageTag(r9)     // Catch:{ MessagingException -> 0x006d }
            int r5 = r3.length()     // Catch:{ MessagingException -> 0x006d }
            if (r5 == 0) goto L_0x001c
            java.lang.String r5 = "Content-Language"
            r6 = 0
            java.lang.String r1 = r8.getHeader(r5, r6)     // Catch:{ MessagingException -> 0x006d }
            boolean r5 = isEmpty(r1)     // Catch:{ MessagingException -> 0x006d }
            if (r5 == 0) goto L_0x001d
            java.lang.String r5 = "Content-Language"
            r8.setHeader(r5, r3)     // Catch:{ MessagingException -> 0x006d }
        L_0x001c:
            return
        L_0x001d:
            boolean r5 = r1.equalsIgnoreCase(r3)     // Catch:{ MessagingException -> 0x006d }
            if (r5 != 0) goto L_0x001c
            java.lang.String r5 = ","
            java.lang.String r3 = r5.concat(r3)     // Catch:{ MessagingException -> 0x006d }
            r2 = 0
        L_0x002a:
            int r2 = r1.indexOf(r3, r2)     // Catch:{ MessagingException -> 0x006d }
            r5 = -1
            if (r2 <= r5) goto L_0x0044
            int r5 = r3.length()     // Catch:{ MessagingException -> 0x006d }
            int r2 = r2 + r5
            int r5 = r1.length()     // Catch:{ MessagingException -> 0x006d }
            if (r2 == r5) goto L_0x0044
            char r5 = r1.charAt(r2)     // Catch:{ MessagingException -> 0x006d }
            r6 = 44
            if (r5 != r6) goto L_0x002a
        L_0x0044:
            if (r2 >= 0) goto L_0x001c
            java.lang.String r5 = "\r\n\t"
            int r4 = r1.lastIndexOf(r5)     // Catch:{ MessagingException -> 0x006d }
            if (r4 >= 0) goto L_0x0077
            int r5 = r1.length()     // Catch:{ MessagingException -> 0x006d }
            int r4 = r5 + 20
        L_0x0054:
            int r5 = r3.length()     // Catch:{ MessagingException -> 0x006d }
            int r5 = r5 + r4
            r6 = 76
            if (r5 <= r6) goto L_0x007f
            java.lang.String r5 = "\r\n\t"
            java.lang.String r5 = r5.concat(r3)     // Catch:{ MessagingException -> 0x006d }
            java.lang.String r1 = r1.concat(r5)     // Catch:{ MessagingException -> 0x006d }
        L_0x0067:
            java.lang.String r5 = "Content-Language"
            r8.setHeader(r5, r1)     // Catch:{ MessagingException -> 0x006d }
            goto L_0x001c
        L_0x006d:
            r0 = move-exception
            java.lang.String r5 = r0.getMessage()
            r6 = 5
            r7.reportError((java.lang.String) r5, (java.lang.Exception) r0, (int) r6)
            goto L_0x001c
        L_0x0077:
            int r5 = r1.length()     // Catch:{ MessagingException -> 0x006d }
            int r5 = r5 - r4
            int r4 = r5 + 8
            goto L_0x0054
        L_0x007f:
            java.lang.String r1 = r1.concat(r3)     // Catch:{ MessagingException -> 0x006d }
            goto L_0x0067
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.logging.MailHandler.appendContentLang(javax.mail.internet.MimePart, java.util.Locale):void");
    }

    private void setAcceptLang(Part p) {
        try {
            String lang = LogManagerProperties.toLanguageTag(Locale.getDefault());
            if (lang.length() != 0) {
                p.setHeader("Accept-Language", lang);
            }
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private void reportFilterError(LogRecord record) {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            Formatter f = createSimpleFormatter();
            reportError("Log record " + record.getSequenceNumber() + " was filtered from all message parts.  " + head(f) + format(f, record) + tail(f, ""), (Exception) new IllegalArgumentException(getFilter() + ", " + Arrays.asList(readOnlyAttachmentFilters())), 5);
            return;
        }
        throw new AssertionError();
    }

    private void reportNonSymmetric(Object o, Object found) {
        reportError("Non symmetric equals implementation.", (Exception) new IllegalArgumentException(o.getClass().getName() + " is not equal to " + found.getClass().getName()), 4);
    }

    private void reportNonDiscriminating(Object o, Object found) {
        reportError("Non discriminating equals implementation.", (Exception) new IllegalArgumentException(o.getClass().getName() + " should not be equal to " + found.getClass().getName()), 4);
    }

    private void reportNullError(int code) {
        reportError("null", (Exception) new NullPointerException(), code);
    }

    private String head(Formatter f) {
        try {
            return f.getHead(this);
        } catch (RuntimeException RE) {
            reportError(RE.getMessage(), (Exception) RE, 5);
            return "";
        }
    }

    private String format(Formatter f, LogRecord r) {
        try {
            return f.format(r);
        } catch (RuntimeException RE) {
            reportError(RE.getMessage(), (Exception) RE, 5);
            return "";
        }
    }

    private String tail(Formatter f, String def) {
        try {
            return f.getTail(this);
        } catch (RuntimeException RE) {
            reportError(RE.getMessage(), (Exception) RE, 5);
            return def;
        }
    }

    private void setMailer(Message msg) {
        String value;
        String value2;
        Class<MailHandler> cls = MailHandler.class;
        try {
            Class<?> k = getClass();
            if (k == cls) {
                value2 = cls.getName();
            } else {
                try {
                    value = MimeUtility.encodeText(k.getName());
                } catch (UnsupportedEncodingException E) {
                    reportError(E.getMessage(), (Exception) E, 5);
                    value = k.getName().replaceAll("[^\\x00-\\x7F]", "\u001a");
                }
                value2 = MimeUtility.fold(10, cls.getName() + " using the " + value + " extension.");
            }
            msg.setHeader("X-Mailer", value2);
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private void setPriority(Message msg) {
        try {
            msg.setHeader("Importance", "High");
            msg.setHeader("Priority", "urgent");
            msg.setHeader("X-Priority", "2");
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private void setIncompleteCopy(Message msg) {
        try {
            msg.setHeader("Incomplete-Copy", "");
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private void setAutoSubmitted(Message msg) {
        if (allowRestrictedHeaders()) {
            try {
                msg.setHeader("auto-submitted", "auto-generated");
            } catch (MessagingException ME) {
                reportError(ME.getMessage(), (Exception) ME, 5);
            }
        }
    }

    private void setFrom(Message msg) {
        String from = getSession(msg).getProperty("mail.from");
        if (from != null) {
            try {
                Address[] address = InternetAddress.parse(from, $assertionsDisabled);
                if (address.length <= 0) {
                    return;
                }
                if (address.length == 1) {
                    msg.setFrom(address[0]);
                } else {
                    msg.addFrom(address);
                }
            } catch (MessagingException ME) {
                reportError(ME.getMessage(), (Exception) ME, 5);
                setDefaultFrom(msg);
            }
        } else {
            setDefaultFrom(msg);
        }
    }

    private void setDefaultFrom(Message msg) {
        try {
            msg.setFrom();
        } catch (MessagingException ME) {
            reportError(ME.getMessage(), (Exception) ME, 5);
        }
    }

    private void setDefaultRecipient(Message msg, Message.RecipientType type) {
        try {
            Address a = InternetAddress.getLocalAddress(getSession(msg));
            if (a != null) {
                msg.setRecipient(type, a);
                return;
            }
            MimeMessage m = new MimeMessage(getSession(msg));
            m.setFrom();
            Address[] from = m.getFrom();
            if (from.length > 0) {
                msg.setRecipients(type, from);
                return;
            }
            throw new MessagingException("No local address.");
        } catch (RuntimeException | MessagingException ME) {
            reportError("Unable to compute a default recipient.", ME, 5);
        }
    }

    private void setReplyTo(Message msg) {
        String reply = getSession(msg).getProperty("mail.reply.to");
        if (!isEmpty(reply)) {
            try {
                Address[] address = InternetAddress.parse(reply, $assertionsDisabled);
                if (address.length > 0) {
                    msg.setReplyTo(address);
                }
            } catch (MessagingException ME) {
                reportError(ME.getMessage(), (Exception) ME, 5);
            }
        }
    }

    private void setSender(Message msg) {
        if ($assertionsDisabled || (msg instanceof MimeMessage)) {
            String sender = getSession(msg).getProperty("mail.sender");
            if (!isEmpty(sender)) {
                try {
                    InternetAddress[] address = InternetAddress.parse(sender, $assertionsDisabled);
                    if (address.length > 0) {
                        ((MimeMessage) msg).setSender(address[0]);
                        if (address.length > 1) {
                            reportError("Ignoring other senders.", (Exception) tooManyAddresses(address, 1), 5);
                        }
                    }
                } catch (MessagingException ME) {
                    reportError(ME.getMessage(), (Exception) ME, 5);
                }
            }
        } else {
            throw new AssertionError(msg);
        }
    }

    private AddressException tooManyAddresses(Address[] address, int offset) {
        return new AddressException(Arrays.asList(address).subList(offset, address.length).toString());
    }

    private boolean setRecipient(Message msg, String key, Message.RecipientType type) {
        boolean containsKey = $assertionsDisabled;
        String value = getSession(msg).getProperty(key);
        if (value != null) {
            containsKey = true;
        }
        if (!isEmpty(value)) {
            try {
                Address[] address = InternetAddress.parse(value, $assertionsDisabled);
                if (address.length > 0) {
                    msg.setRecipients(type, address);
                }
            } catch (MessagingException ME) {
                reportError(ME.getMessage(), (Exception) ME, 5);
            }
        }
        return containsKey;
    }

    private String toRawString(Message msg) throws MessagingException, IOException {
        if (msg == null) {
            return null;
        }
        Object ccl = getAndSetContextClassLoader(MAILHANDLER_LOADER);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(msg.getSize() + 1024, 1024));
            msg.writeTo(out);
            return out.toString("UTF-8");
        } finally {
            getAndSetContextClassLoader(ccl);
        }
    }

    private String toMsgString(Throwable t) {
        PrintWriter pw;
        if (t == null) {
            return "null";
        }
        String charset = getEncodingName();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            OutputStreamWriter ows = new OutputStreamWriter(out, charset);
            try {
                pw = new PrintWriter(ows);
                pw.println(t.getMessage());
                t.printStackTrace(pw);
                pw.flush();
                pw.close();
                ows.close();
                return out.toString(charset);
            } catch (Throwable th) {
                ows.close();
                throw th;
            }
        } catch (RuntimeException unexpected) {
            return t.toString() + ' ' + unexpected.toString();
        } catch (Exception badMimeCharset) {
            return t.toString() + ' ' + badMimeCharset.toString();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
        throw th;
    }

    private Object getAndSetContextClassLoader(Object ccl) {
        PrivilegedAction<?> pa;
        if (ccl != GetAndSetContext.NOT_MODIFIED) {
            try {
                if (ccl instanceof PrivilegedAction) {
                    pa = (PrivilegedAction) ccl;
                } else {
                    pa = new GetAndSetContext(ccl);
                }
                return AccessController.doPrivileged(pa);
            } catch (SecurityException e) {
            }
        }
        return GetAndSetContext.NOT_MODIFIED;
    }

    private static RuntimeException attachmentMismatch(String msg) {
        return new IndexOutOfBoundsException(msg);
    }

    private static RuntimeException attachmentMismatch(int expected, int found) {
        return attachmentMismatch("Attachments mismatched, expected " + expected + " but given " + found + '.');
    }

    private static MessagingException attach(MessagingException required, Exception optional) {
        if (optional != null && !required.setNextException(optional)) {
            if (optional instanceof MessagingException) {
                MessagingException head = (MessagingException) optional;
                if (head.setNextException(required)) {
                    return head;
                }
            }
            if (optional != required) {
                required.addSuppressed(optional);
            }
        }
        return required;
    }

    private String getLocalHost(Service s) {
        try {
            return LogManagerProperties.getLocalHost(s);
        } catch (LinkageError | NoSuchMethodException | SecurityException e) {
        } catch (Exception ex) {
            reportError(s.toString(), ex, 4);
        }
        return null;
    }

    private Session getSession(Message msg) {
        if (msg != null) {
            return new MessageContext(msg).getSession();
        }
        throw new NullPointerException();
    }

    private boolean allowRestrictedHeaders() {
        return LogManagerProperties.hasLogManager();
    }

    private static String atIndexMsg(int i) {
        return "At index: " + i + '.';
    }

    private static final class DefaultAuthenticator extends Authenticator {
        static final /* synthetic */ boolean $assertionsDisabled = (!MailHandler.class.desiredAssertionStatus() ? true : MailHandler.$assertionsDisabled);
        private final String pass;

        /* renamed from: of */
        static Authenticator m42of(String pass2) {
            return new DefaultAuthenticator(pass2);
        }

        private DefaultAuthenticator(String pass2) {
            if ($assertionsDisabled || pass2 != null) {
                this.pass = pass2;
                return;
            }
            throw new AssertionError();
        }

        /* access modifiers changed from: protected */
        public final PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(getDefaultUserName(), this.pass);
        }
    }

    private static final class GetAndSetContext implements PrivilegedAction<Object> {
        static final /* synthetic */ boolean $assertionsDisabled = (!MailHandler.class.desiredAssertionStatus() ? true : MailHandler.$assertionsDisabled);
        public static final Object NOT_MODIFIED = GetAndSetContext.class;
        private final Object source;

        GetAndSetContext(Object source2) {
            this.source = source2;
        }

        public final Object run() {
            ClassLoader loader;
            Thread current = Thread.currentThread();
            ClassLoader ccl = current.getContextClassLoader();
            if (this.source == null) {
                loader = null;
            } else if (this.source instanceof ClassLoader) {
                loader = (ClassLoader) this.source;
            } else if (this.source instanceof Class) {
                loader = ((Class) this.source).getClassLoader();
            } else if (this.source instanceof Thread) {
                loader = ((Thread) this.source).getContextClassLoader();
            } else if ($assertionsDisabled || !(this.source instanceof Class)) {
                loader = this.source.getClass().getClassLoader();
            } else {
                throw new AssertionError(this.source);
            }
            if (ccl == loader) {
                return NOT_MODIFIED;
            }
            current.setContextClassLoader(loader);
            return ccl;
        }
    }

    private static final class TailNameFormatter extends Formatter {
        static final /* synthetic */ boolean $assertionsDisabled = (!MailHandler.class.desiredAssertionStatus() ? true : MailHandler.$assertionsDisabled);
        private final String name;

        /* renamed from: of */
        static Formatter m43of(String name2) {
            return new TailNameFormatter(name2);
        }

        private TailNameFormatter(String name2) {
            if ($assertionsDisabled || name2 != null) {
                this.name = name2;
                return;
            }
            throw new AssertionError();
        }

        public final String format(LogRecord record) {
            return "";
        }

        public final String getTail(Handler h) {
            return this.name;
        }

        public final boolean equals(Object o) {
            if (o instanceof TailNameFormatter) {
                return this.name.equals(((TailNameFormatter) o).name);
            }
            return MailHandler.$assertionsDisabled;
        }

        public final int hashCode() {
            return getClass().hashCode() + this.name.hashCode();
        }

        public final String toString() {
            return this.name;
        }
    }
}
