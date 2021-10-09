package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import gnu.bytecode.Access;

public class RFC822SIZE implements Item {
    static final char[] name = {'R', Access.FIELD_CONTEXT, Access.CLASS_CONTEXT, '8', '2', '2', '.', 'S', Access.INNERCLASS_CONTEXT, 'Z', 'E'};
    public int msgno;
    public long size;

    public RFC822SIZE(FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        this.size = r.readLong();
    }
}
