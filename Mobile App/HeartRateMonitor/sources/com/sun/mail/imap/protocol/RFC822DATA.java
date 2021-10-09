package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.ParsingException;
import gnu.bytecode.Access;
import java.io.ByteArrayInputStream;

public class RFC822DATA implements Item {
    static final char[] name = {'R', Access.FIELD_CONTEXT, Access.CLASS_CONTEXT, '8', '2', '2'};
    private final ByteArray data;
    private final boolean isHeader;
    private final int msgno;

    public RFC822DATA(FetchResponse r) throws ParsingException {
        this(r, false);
    }

    public RFC822DATA(FetchResponse r, boolean isHeader2) throws ParsingException {
        this.isHeader = isHeader2;
        this.msgno = r.getNumber();
        r.skipSpaces();
        this.data = r.readByteArray();
    }

    public ByteArray getByteArray() {
        return this.data;
    }

    public ByteArrayInputStream getByteArrayInputStream() {
        if (this.data != null) {
            return this.data.toByteArrayInputStream();
        }
        return null;
    }

    public boolean isHeader() {
        return this.isHeader;
    }
}
