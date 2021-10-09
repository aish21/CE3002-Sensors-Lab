package com.sun.mail.imap;

import com.sun.mail.imap.protocol.UIDSet;

public class ResyncData {
    public static final ResyncData CONDSTORE = new ResyncData(-1, -1);
    private long modseq = -1;
    private UIDSet[] uids = null;
    private long uidvalidity = -1;

    public ResyncData(long uidvalidity2, long modseq2) {
        this.uidvalidity = uidvalidity2;
        this.modseq = modseq2;
        this.uids = null;
    }

    public ResyncData(long uidvalidity2, long modseq2, long uidFirst, long uidLast) {
        this.uidvalidity = uidvalidity2;
        this.modseq = modseq2;
        this.uids = new UIDSet[]{new UIDSet(uidFirst, uidLast)};
    }

    public ResyncData(long uidvalidity2, long modseq2, long[] uids2) {
        this.uidvalidity = uidvalidity2;
        this.modseq = modseq2;
        this.uids = UIDSet.createUIDSets(uids2);
    }

    public long getUIDValidity() {
        return this.uidvalidity;
    }

    public long getModSeq() {
        return this.modseq;
    }

    /* access modifiers changed from: package-private */
    public UIDSet[] getUIDSet() {
        return this.uids;
    }
}
