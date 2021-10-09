package com.sun.mail.iap;

import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.SSLSocket;

public class Protocol {
    private static final byte[] CRLF = {13, 10};
    static final AtomicInteger tagNum = new AtomicInteger();
    private final List<ResponseHandler> handlers;
    protected String host;
    private volatile ResponseInputStream input;
    private String localHostName;
    protected MailLogger logger;
    private volatile DataOutputStream output;
    protected String prefix;
    protected Properties props;
    protected boolean quote;
    private Socket socket;
    private int tagCounter;
    private final String tagPrefix;
    private volatile long timestamp;
    private TraceInputStream traceInput;
    protected MailLogger traceLogger;
    private TraceOutputStream traceOutput;

    public Protocol(String host2, int port, Properties props2, String prefix2, boolean isSSL, MailLogger logger2) throws IOException, ProtocolException {
        this.tagCounter = 0;
        this.handlers = new CopyOnWriteArrayList();
        boolean connected = false;
        this.tagPrefix = computePrefix(props2, prefix2);
        try {
            this.host = host2;
            this.props = props2;
            this.prefix = prefix2;
            this.logger = logger2;
            this.traceLogger = logger2.getSubLogger("protocol", (String) null);
            this.socket = SocketFetcher.getSocket(host2, port, props2, prefix2, isSSL);
            this.quote = PropUtil.getBooleanProperty(props2, "mail.debug.quote", false);
            initStreams();
            processGreeting(readResponse());
            this.timestamp = System.currentTimeMillis();
            connected = true;
        } finally {
            if (!connected) {
                disconnect();
            }
        }
    }

    private void initStreams() throws IOException {
        this.traceInput = new TraceInputStream(this.socket.getInputStream(), this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        this.traceOutput = new TraceOutputStream(this.socket.getOutputStream(), this.traceLogger);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
    }

    private String computePrefix(Properties props2, String prefix2) {
        if (PropUtil.getBooleanProperty(props2, prefix2 + ".reusetagprefix", false)) {
            return "A";
        }
        int n = tagNum.getAndIncrement() % 18278;
        if (n < 26) {
            return new String(new char[]{(char) (n + 65)});
        } else if (n < 702) {
            int n2 = n - 26;
            return new String(new char[]{(char) ((n2 / 26) + 65), (char) ((n2 % 26) + 65)});
        } else {
            int n3 = n - 702;
            return new String(new char[]{(char) ((n3 / 676) + 65), (char) (((n3 % 676) / 26) + 65), (char) ((n3 % 26) + 65)});
        }
    }

    public Protocol(InputStream in, PrintStream out, Properties props2, boolean debug) throws IOException {
        this.tagCounter = 0;
        this.handlers = new CopyOnWriteArrayList();
        this.host = "localhost";
        this.props = props2;
        this.quote = false;
        this.tagPrefix = computePrefix(props2, "mail.imap");
        this.logger = new MailLogger(getClass(), "DEBUG", debug, System.out);
        this.traceLogger = this.logger.getSubLogger("protocol", (String) null);
        this.traceInput = new TraceInputStream(in, this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        this.traceOutput = new TraceOutputStream((OutputStream) out, this.traceLogger);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void addResponseHandler(ResponseHandler h) {
        this.handlers.add(h);
    }

    public void removeResponseHandler(ResponseHandler h) {
        this.handlers.remove(h);
    }

    public void notifyResponseHandlers(Response[] responses) {
        if (!this.handlers.isEmpty()) {
            for (Response r : responses) {
                if (r != null) {
                    for (ResponseHandler rh : this.handlers) {
                        if (rh != null) {
                            rh.handleResponse(r);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processGreeting(Response r) throws ProtocolException {
        if (r.isBYE()) {
            throw new ConnectionException(this, r);
        }
    }

    /* access modifiers changed from: protected */
    public ResponseInputStream getInputStream() {
        return this.input;
    }

    /* access modifiers changed from: protected */
    public OutputStream getOutputStream() {
        return this.output;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean supportsNonSyncLiterals() {
        return false;
    }

    public Response readResponse() throws IOException, ProtocolException {
        return new Response(this);
    }

    public boolean hasResponse() {
        try {
            return this.input.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public ByteArray getResponseBuffer() {
        return null;
    }

    public String writeCommand(String command, Argument args) throws IOException, ProtocolException {
        StringBuilder append = new StringBuilder().append(this.tagPrefix);
        int i = this.tagCounter;
        this.tagCounter = i + 1;
        String tag = append.append(Integer.toString(i)).toString();
        this.output.writeBytes(tag + " " + command);
        if (args != null) {
            this.output.write(32);
            args.write(this);
        }
        this.output.write(CRLF);
        this.output.flush();
        return tag;
    }

    public synchronized Response[] command(String command, Argument args) {
        Response[] responses;
        commandStart(command);
        List<Response> v = new ArrayList<>();
        boolean done = false;
        String tag = null;
        try {
            tag = writeCommand(command, args);
        } catch (LiteralException lex) {
            v.add(lex.getResponse());
            done = true;
        } catch (Exception ex) {
            v.add(Response.byeResponse(ex));
            done = true;
        }
        Response byeResp = null;
        while (!done) {
            try {
                Response r = readResponse();
                if (r.isBYE()) {
                    byeResp = r;
                } else {
                    v.add(r);
                    if (r.isTagged() && r.getTag().equals(tag)) {
                        done = true;
                    }
                }
            } catch (IOException ioex) {
                if (byeResp == null) {
                    byeResp = Response.byeResponse(ioex);
                }
            } catch (ProtocolException pex) {
                this.logger.log(Level.FINE, "ignoring bad response", (Throwable) pex);
            }
        }
        if (byeResp != null) {
            v.add(byeResp);
        }
        responses = new Response[v.size()];
        v.toArray(responses);
        this.timestamp = System.currentTimeMillis();
        commandEnd();
        return responses;
    }

    public void handleResult(Response response) throws ProtocolException {
        if (!response.isOK()) {
            if (response.isNO()) {
                throw new CommandFailedException(response);
            } else if (response.isBAD()) {
                throw new BadCommandException(response);
            } else if (response.isBYE()) {
                disconnect();
                throw new ConnectionException(this, response);
            }
        }
    }

    public void simpleCommand(String cmd, Argument args) throws ProtocolException {
        Response[] r = command(cmd, args);
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
    }

    public synchronized void startTLS(String cmd) throws IOException, ProtocolException {
        if (!(this.socket instanceof SSLSocket)) {
            simpleCommand(cmd, (Argument) null);
            this.socket = SocketFetcher.startTLS(this.socket, this.host, this.props, this.prefix);
            initStreams();
        }
    }

    public synchronized void startCompression(String cmd) throws IOException, ProtocolException {
        simpleCommand(cmd, (Argument) null);
        this.traceInput = new TraceInputStream((InputStream) new InflaterInputStream(this.socket.getInputStream(), new Inflater(true)), this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        int level = PropUtil.getIntProperty(this.props, this.prefix + ".compress.level", -1);
        int strategy = PropUtil.getIntProperty(this.props, this.prefix + ".compress.strategy", 0);
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.log(Level.FINE, "Creating Deflater with compression level {0} and strategy {1}", Integer.valueOf(level), Integer.valueOf(strategy));
        }
        Deflater def = new Deflater(-1, true);
        try {
            def.setLevel(level);
        } catch (IllegalArgumentException ex) {
            this.logger.log(Level.FINE, "Ignoring bad compression level", (Throwable) ex);
        }
        try {
            def.setStrategy(strategy);
        } catch (IllegalArgumentException ex2) {
            this.logger.log(Level.FINE, "Ignoring bad compression strategy", (Throwable) ex2);
        }
        this.traceOutput = new TraceOutputStream((OutputStream) new DeflaterOutputStream(this.socket.getOutputStream(), def, true), this.traceLogger);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
        return;
    }

    public boolean isSSL() {
        return this.socket instanceof SSLSocket;
    }

    public InetAddress getInetAddress() {
        return this.socket.getInetAddress();
    }

    public SocketChannel getChannel() {
        SocketChannel ret = this.socket.getChannel();
        if (ret != null) {
            return ret;
        }
        if (this.socket instanceof SSLSocket) {
            ret = findSocketChannel(this.socket);
        }
        return ret;
    }

    private static SocketChannel findSocketChannel(Socket socket2) {
        Class cls = socket2.getClass();
        while (cls != Object.class) {
            try {
                Field f = cls.getDeclaredField("socket");
                f.setAccessible(true);
                SocketChannel ret = ((Socket) f.get(socket2)).getChannel();
                if (ret != null) {
                    return ret;
                }
                cls = cls.getSuperclass();
            } catch (Exception e) {
            }
        }
        for (Class cls2 = socket2.getClass(); cls2 != Object.class; cls2 = cls2.getSuperclass()) {
            try {
                for (Field f2 : cls2.getDeclaredFields()) {
                    if (Socket.class.isAssignableFrom(f2.getType())) {
                        try {
                            f2.setAccessible(true);
                            SocketChannel ret2 = ((Socket) f2.get(socket2)).getChannel();
                            if (ret2 != null) {
                                return ret2;
                            }
                        } catch (Exception e2) {
                        }
                    }
                }
                continue;
            } catch (Exception e3) {
            }
        }
        return null;
    }

    public SocketAddress getLocalSocketAddress() {
        return this.socket.getLocalSocketAddress();
    }

    public boolean supportsUtf8() {
        return false;
    }

    /* access modifiers changed from: protected */
    public synchronized void disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
            }
            this.socket = null;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized String getLocalHost() {
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.props.getProperty(this.prefix + ".localhost");
        }
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.props.getProperty(this.prefix + ".localaddress");
        }
        try {
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                InetAddress localHost = InetAddress.getLocalHost();
                this.localHostName = localHost.getCanonicalHostName();
                if (this.localHostName == null) {
                    this.localHostName = "[" + localHost.getHostAddress() + "]";
                }
            }
        } catch (UnknownHostException e) {
        }
        if ((this.localHostName == null || this.localHostName.length() <= 0) && this.socket != null && this.socket.isBound()) {
            InetAddress localHost2 = this.socket.getLocalAddress();
            this.localHostName = localHost2.getCanonicalHostName();
            if (this.localHostName == null) {
                this.localHostName = "[" + localHost2.getHostAddress() + "]";
            }
        }
        return this.localHostName;
    }

    /* access modifiers changed from: protected */
    public boolean isTracing() {
        return this.traceLogger.isLoggable(Level.FINEST);
    }

    /* access modifiers changed from: protected */
    public void suspendTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace(false);
            this.traceOutput.setTrace(false);
        }
    }

    /* access modifiers changed from: protected */
    public void resumeTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace(true);
            this.traceOutput.setTrace(true);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            disconnect();
        } finally {
            super.finalize();
        }
    }

    private void commandStart(String command) {
    }

    private void commandEnd() {
    }
}
