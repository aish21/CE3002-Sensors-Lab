package com.sun.mail.imap.protocol;

import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.util.ASCIIUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IMAPResponse extends Response {
    private String key;
    private int number;

    public IMAPResponse(Protocol c) throws IOException, ProtocolException {
        super(c);
        init();
    }

    private void init() throws IOException, ProtocolException {
        if (isUnTagged() && !isOK() && !isNO() && !isBAD() && !isBYE()) {
            this.key = readAtom();
            try {
                this.number = Integer.parseInt(this.key);
                this.key = readAtom();
            } catch (NumberFormatException e) {
            }
        }
    }

    public IMAPResponse(IMAPResponse r) {
        super((Response) r);
        this.key = r.key;
        this.number = r.number;
    }

    public IMAPResponse(String r) throws IOException, ProtocolException {
        this(r, true);
    }

    public IMAPResponse(String r, boolean utf8) throws IOException, ProtocolException {
        super(r, utf8);
        init();
    }

    public String[] readSimpleList() {
        skipSpaces();
        if (this.buffer[this.index] != 40) {
            return null;
        }
        this.index++;
        List<String> v = new ArrayList<>();
        int start = this.index;
        while (this.buffer[this.index] != 41) {
            if (this.buffer[this.index] == 32) {
                v.add(ASCIIUtility.toString(this.buffer, start, this.index));
                start = this.index + 1;
            }
            this.index++;
        }
        if (this.index > start) {
            v.add(ASCIIUtility.toString(this.buffer, start, this.index));
        }
        this.index++;
        int size = v.size();
        if (size > 0) {
            return (String[]) v.toArray(new String[size]);
        }
        return null;
    }

    public String getKey() {
        return this.key;
    }

    public boolean keyEquals(String k) {
        if (this.key == null || !this.key.equalsIgnoreCase(k)) {
            return false;
        }
        return true;
    }

    public int getNumber() {
        return this.number;
    }
}
