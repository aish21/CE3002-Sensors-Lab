package javax.mail;

import java.net.InetAddress;

public abstract class Authenticator {
    private int requestingPort;
    private String requestingPrompt;
    private String requestingProtocol;
    private InetAddress requestingSite;
    private String requestingUserName;

    /* access modifiers changed from: package-private */
    public final synchronized PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String defaultUserName) {
        this.requestingSite = addr;
        this.requestingPort = port;
        this.requestingProtocol = protocol;
        this.requestingPrompt = prompt;
        this.requestingUserName = defaultUserName;
        return getPasswordAuthentication();
    }

    /* access modifiers changed from: protected */
    public final InetAddress getRequestingSite() {
        return this.requestingSite;
    }

    /* access modifiers changed from: protected */
    public final int getRequestingPort() {
        return this.requestingPort;
    }

    /* access modifiers changed from: protected */
    public final String getRequestingProtocol() {
        return this.requestingProtocol;
    }

    /* access modifiers changed from: protected */
    public final String getRequestingPrompt() {
        return this.requestingPrompt;
    }

    /* access modifiers changed from: protected */
    public final String getDefaultUserName() {
        return this.requestingUserName;
    }

    /* access modifiers changed from: protected */
    public PasswordAuthentication getPasswordAuthentication() {
        return null;
    }
}
