package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import gnu.bytecode.Access;

public class UID implements Item {
    static final char[] name = {'U', Access.INNERCLASS_CONTEXT, 'D'};
    public int seqnum;
    public long uid;

    public UID(FetchResponse r) throws ParsingException {
        this.seqnum = r.getNumber();
        r.skipSpaces();
        this.uid = r.readLong();
    }
}
