package com.sun.mail.smtp;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public class SMTPAddressSucceededException extends MessagingException {
    private static final long serialVersionUID = -1168335848623096749L;
    protected InternetAddress addr;
    protected String cmd;

    /* renamed from: rc */
    protected int f286rc;

    public SMTPAddressSucceededException(InternetAddress addr2, String cmd2, int rc, String err) {
        super(err);
        this.addr = addr2;
        this.cmd = cmd2;
        this.f286rc = rc;
    }

    public InternetAddress getAddress() {
        return this.addr;
    }

    public String getCommand() {
        return this.cmd;
    }

    public int getReturnCode() {
        return this.f286rc;
    }
}
