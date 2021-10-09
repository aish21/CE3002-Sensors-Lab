package com.sun.mail.smtp;

import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

public class SMTPAddressFailedException extends SendFailedException {
    private static final long serialVersionUID = 804831199768630097L;
    protected InternetAddress addr;
    protected String cmd;

    /* renamed from: rc */
    protected int f285rc;

    public SMTPAddressFailedException(InternetAddress addr2, String cmd2, int rc, String err) {
        super(err);
        this.addr = addr2;
        this.cmd = cmd2;
        this.f285rc = rc;
    }

    public InternetAddress getAddress() {
        return this.addr;
    }

    public String getCommand() {
        return this.cmd;
    }

    public int getReturnCode() {
        return this.f285rc;
    }
}
