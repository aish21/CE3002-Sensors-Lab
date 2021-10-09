package javax.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EventListener;
import java.util.Vector;
import java.util.concurrent.Executor;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MailEvent;

public abstract class Service implements AutoCloseable {
    private boolean connected = false;
    private final Vector<ConnectionListener> connectionListeners = new Vector<>();
    protected boolean debug = false;

    /* renamed from: q */
    private final EventQueue f299q;
    /* access modifiers changed from: protected */
    public Session session;
    protected volatile URLName url = null;

    protected Service(Session session2, URLName urlname) {
        this.session = session2;
        this.debug = session2.getDebug();
        this.url = urlname;
        String protocol = null;
        String host = null;
        int port = -1;
        String user = null;
        String password = null;
        String file = null;
        if (this.url != null) {
            protocol = this.url.getProtocol();
            host = this.url.getHost();
            port = this.url.getPort();
            user = this.url.getUsername();
            password = this.url.getPassword();
            file = this.url.getFile();
        }
        if (protocol != null) {
            host = host == null ? session2.getProperty("mail." + protocol + ".host") : host;
            if (user == null) {
                user = session2.getProperty("mail." + protocol + ".user");
            }
        }
        host = host == null ? session2.getProperty("mail.host") : host;
        user = user == null ? session2.getProperty("mail.user") : user;
        if (user == null) {
            try {
                user = System.getProperty("user.name");
            } catch (SecurityException e) {
            }
        }
        this.url = new URLName(protocol, host, port, file, user, password);
        String scope = session2.getProperties().getProperty("mail.event.scope", "folder");
        Executor executor = (Executor) session2.getProperties().get("mail.event.executor");
        if (scope.equalsIgnoreCase("application")) {
            this.f299q = EventQueue.getApplicationEventQueue(executor);
        } else if (scope.equalsIgnoreCase("session")) {
            this.f299q = session2.getEventQueue();
        } else {
            this.f299q = new EventQueue(executor);
        }
    }

    public void connect() throws MessagingException {
        connect((String) null, (String) null, (String) null);
    }

    public void connect(String host, String user, String password) throws MessagingException {
        connect(host, -1, user, password);
    }

    public void connect(String user, String password) throws MessagingException {
        connect((String) null, user, password);
    }

    public synchronized void connect(String host, int port, String user, String password) throws MessagingException {
        InetAddress addr;
        if (isConnected()) {
            throw new IllegalStateException("already connected");
        }
        boolean connected2 = false;
        boolean save = false;
        String protocol = null;
        String file = null;
        if (this.url != null) {
            protocol = this.url.getProtocol();
            if (host == null) {
                host = this.url.getHost();
            }
            if (port == -1) {
                port = this.url.getPort();
            }
            if (user == null) {
                user = this.url.getUsername();
                if (password == null) {
                    password = this.url.getPassword();
                }
            } else if (password == null) {
                if (user.equals(this.url.getUsername())) {
                    password = this.url.getPassword();
                }
            }
            file = this.url.getFile();
        }
        if (protocol != null) {
            if (host == null) {
                host = this.session.getProperty("mail." + protocol + ".host");
            }
            if (user == null) {
                user = this.session.getProperty("mail." + protocol + ".user");
            }
        }
        if (host == null) {
            host = this.session.getProperty("mail.host");
        }
        if (user == null) {
            user = this.session.getProperty("mail.user");
        }
        if (user == null) {
            try {
                user = System.getProperty("user.name");
            } catch (SecurityException e) {
            }
        }
        if (password == null) {
            if (this.url != null) {
                setURLName(new URLName(protocol, host, port, file, user, (String) null));
                PasswordAuthentication pw = this.session.getPasswordAuthentication(getURLName());
                if (pw == null) {
                    save = true;
                } else if (user == null) {
                    user = pw.getUserName();
                    password = pw.getPassword();
                } else {
                    if (user.equals(pw.getUserName())) {
                        password = pw.getPassword();
                    }
                }
            }
        }
        AuthenticationFailedException authEx = null;
        try {
            connected2 = protocolConnect(host, port, user, password);
        } catch (AuthenticationFailedException ex) {
            authEx = ex;
        }
        if (!connected2) {
            try {
                addr = InetAddress.getByName(host);
            } catch (UnknownHostException e2) {
                addr = null;
            }
            PasswordAuthentication pw2 = this.session.requestPasswordAuthentication(addr, port, protocol, (String) null, user);
            if (pw2 != null) {
                user = pw2.getUserName();
                password = pw2.getPassword();
                connected2 = protocolConnect(host, port, user, password);
            }
        }
        if (connected2) {
            setURLName(new URLName(protocol, host, port, file, user, password));
            if (save) {
                this.session.setPasswordAuthentication(getURLName(), new PasswordAuthentication(user, password));
            }
            setConnected(true);
            notifyConnectionListeners(1);
        } else if (authEx != null) {
            throw authEx;
        } else if (user == null) {
            throw new AuthenticationFailedException("failed to connect, no user name specified?");
        } else if (password == null) {
            throw new AuthenticationFailedException("failed to connect, no password specified?");
        } else {
            throw new AuthenticationFailedException("failed to connect");
        }
    }

    /* access modifiers changed from: protected */
    public boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
        return false;
    }

    public synchronized boolean isConnected() {
        return this.connected;
    }

    /* access modifiers changed from: protected */
    public synchronized void setConnected(boolean connected2) {
        this.connected = connected2;
    }

    public synchronized void close() throws MessagingException {
        setConnected(false);
        notifyConnectionListeners(3);
    }

    public URLName getURLName() {
        URLName url2 = this.url;
        if (url2 == null || (url2.getPassword() == null && url2.getFile() == null)) {
            return url2;
        }
        return new URLName(url2.getProtocol(), url2.getHost(), url2.getPort(), (String) null, url2.getUsername(), (String) null);
    }

    /* access modifiers changed from: protected */
    public void setURLName(URLName url2) {
        this.url = url2;
    }

    public void addConnectionListener(ConnectionListener l) {
        this.connectionListeners.addElement(l);
    }

    public void removeConnectionListener(ConnectionListener l) {
        this.connectionListeners.removeElement(l);
    }

    /* access modifiers changed from: protected */
    public void notifyConnectionListeners(int type) {
        if (this.connectionListeners.size() > 0) {
            queueEvent(new ConnectionEvent(this, type), this.connectionListeners);
        }
        if (type == 3) {
            this.f299q.terminateQueue();
        }
    }

    public String toString() {
        URLName url2 = getURLName();
        if (url2 != null) {
            return url2.toString();
        }
        return super.toString();
    }

    /* access modifiers changed from: protected */
    public void queueEvent(MailEvent event, Vector<? extends EventListener> vector) {
        this.f299q.enqueue(event, (Vector) vector.clone());
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            this.f299q.terminateQueue();
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: package-private */
    public Session getSession() {
        return this.session;
    }

    /* access modifiers changed from: package-private */
    public EventQueue getEventQueue() {
        return this.f299q;
    }
}
