package com.sun.mail.imap;

import com.sun.mail.util.DefaultProvider;
import javax.mail.Provider;

@DefaultProvider
public class IMAPProvider extends Provider {
    public IMAPProvider() {
        super(Provider.Type.STORE, "imap", IMAPStore.class.getName(), "Oracle", (String) null);
    }
}
