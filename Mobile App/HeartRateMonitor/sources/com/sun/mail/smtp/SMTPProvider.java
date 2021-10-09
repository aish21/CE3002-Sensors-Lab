package com.sun.mail.smtp;

import com.sun.mail.util.DefaultProvider;
import javax.mail.Provider;

@DefaultProvider
public class SMTPProvider extends Provider {
    public SMTPProvider() {
        super(Provider.Type.TRANSPORT, "smtp", SMTPTransport.class.getName(), "Oracle", (String) null);
    }
}
