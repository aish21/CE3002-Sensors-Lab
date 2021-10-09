package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import gnu.bytecode.Access;

public class MODSEQ implements Item {
    static final char[] name = {Access.METHOD_CONTEXT, 'O', 'D', 'S', 'E', 'Q'};
    public long modseq;
    public int seqnum;

    public MODSEQ(FetchResponse r) throws ParsingException {
        this.seqnum = r.getNumber();
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("MODSEQ parse error");
        }
        this.modseq = r.readLong();
        if (!r.isNextNonSpace(')')) {
            throw new ParsingException("MODSEQ parse error");
        }
    }
}
