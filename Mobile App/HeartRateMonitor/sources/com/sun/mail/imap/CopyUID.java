package com.sun.mail.imap;

import com.sun.mail.imap.protocol.UIDSet;

public class CopyUID {
    public UIDSet[] dst;
    public UIDSet[] src;
    public long uidvalidity = -1;

    public CopyUID(long uidvalidity2, UIDSet[] src2, UIDSet[] dst2) {
        this.uidvalidity = uidvalidity2;
        this.src = src2;
        this.dst = dst2;
    }
}
