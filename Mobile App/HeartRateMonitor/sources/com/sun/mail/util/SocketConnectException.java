package com.sun.mail.util;

import java.io.IOException;

public class SocketConnectException extends IOException {
    static final /* synthetic */ boolean $assertionsDisabled = (!SocketConnectException.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final long serialVersionUID = 3997871560538755463L;
    private int cto;
    private String host;
    private int port;

    public SocketConnectException(String msg, Exception cause, String host2, int port2, int cto2) {
        super(msg);
        initCause(cause);
        this.host = host2;
        this.port = port2;
        this.cto = cto2;
    }

    public Exception getException() {
        Throwable t = getCause();
        if ($assertionsDisabled || t == null || (t instanceof Exception)) {
            return (Exception) t;
        }
        throw new AssertionError();
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public int getConnectionTimeout() {
        return this.cto;
    }
}
