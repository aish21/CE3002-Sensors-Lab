package com.sun.mail.imap;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MessageRemovedIOException;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;

public class IMAPInputStream extends InputStream {
    private static final int slop = 64;
    private int blksize;
    private byte[] buf;
    private int bufcount;
    private int bufpos;
    private boolean lastBuffer;
    private int max;
    private IMAPMessage msg;
    private boolean peek;
    private int pos = 0;
    private ByteArray readbuf;
    private String section;

    public IMAPInputStream(IMAPMessage msg2, String section2, int max2, boolean peek2) {
        this.msg = msg2;
        this.section = section2;
        this.max = max2;
        this.peek = peek2;
        this.blksize = msg2.getFetchBlockSize();
    }

    private void forceCheckExpunged() throws MessageRemovedIOException, FolderClosedIOException {
        synchronized (this.msg.getMessageCacheLock()) {
            try {
                this.msg.getProtocol().noop();
            } catch (ConnectionException cex) {
                throw new FolderClosedIOException(this.msg.getFolder(), cex.getMessage());
            } catch (FolderClosedException fex) {
                throw new FolderClosedIOException(fex.getFolder(), fex.getMessage());
            } catch (ProtocolException e) {
            }
        }
        if (this.msg.isExpunged()) {
            throw new MessageRemovedIOException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0089, code lost:
        if (r7 == null) goto L_0x008b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void fill() throws java.io.IOException {
        /*
            r15 = this;
            r5 = -1
            r13 = 0
            r12 = 1
            boolean r2 = r15.lastBuffer
            if (r2 != 0) goto L_0x0011
            int r2 = r15.max
            if (r2 == r5) goto L_0x001c
            int r2 = r15.pos
            int r3 = r15.max
            if (r2 < r3) goto L_0x001c
        L_0x0011:
            int r2 = r15.pos
            if (r2 != 0) goto L_0x0018
            r15.checkSeen()
        L_0x0018:
            r2 = 0
            r15.readbuf = r2
        L_0x001b:
            return
        L_0x001c:
            r6 = 0
            com.sun.mail.iap.ByteArray r2 = r15.readbuf
            if (r2 != 0) goto L_0x002c
            com.sun.mail.iap.ByteArray r2 = new com.sun.mail.iap.ByteArray
            int r3 = r15.blksize
            int r3 = r3 + 64
            r2.<init>(r3)
            r15.readbuf = r2
        L_0x002c:
            com.sun.mail.imap.IMAPMessage r2 = r15.msg
            java.lang.Object r14 = r2.getMessageCacheLock()
            monitor-enter(r14)
            com.sun.mail.imap.IMAPMessage r2 = r15.msg     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r2.getProtocol()     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            com.sun.mail.imap.IMAPMessage r2 = r15.msg     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            boolean r2 = r2.isExpunged()     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            if (r2 == 0) goto L_0x005a
            com.sun.mail.util.MessageRemovedIOException r2 = new com.sun.mail.util.MessageRemovedIOException     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            java.lang.String r3 = "No content for expunged message"
            r2.<init>(r3)     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            throw r2     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
        L_0x0049:
            r11 = move-exception
            r15.forceCheckExpunged()     // Catch:{ all -> 0x0057 }
            java.io.IOException r2 = new java.io.IOException     // Catch:{ all -> 0x0057 }
            java.lang.String r3 = r11.getMessage()     // Catch:{ all -> 0x0057 }
            r2.<init>(r3)     // Catch:{ all -> 0x0057 }
            throw r2     // Catch:{ all -> 0x0057 }
        L_0x0057:
            r2 = move-exception
            monitor-exit(r14)     // Catch:{ all -> 0x0057 }
            throw r2
        L_0x005a:
            com.sun.mail.imap.IMAPMessage r2 = r15.msg     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r1 = r2.getSequenceNumber()     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r4 = r15.blksize     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r2 = r15.max     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            if (r2 == r5) goto L_0x0075
            int r2 = r15.pos     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r3 = r15.blksize     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r2 = r2 + r3
            int r3 = r15.max     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            if (r2 <= r3) goto L_0x0075
            int r2 = r15.max     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r3 = r15.pos     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r4 = r2 - r3
        L_0x0075:
            boolean r2 = r15.peek     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            if (r2 == 0) goto L_0x00c9
            java.lang.String r2 = r15.section     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r3 = r15.pos     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            com.sun.mail.iap.ByteArray r5 = r15.readbuf     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            com.sun.mail.imap.protocol.BODY r6 = r0.peekBody(r1, r2, r3, r4, r5)     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
        L_0x0083:
            if (r6 == 0) goto L_0x008b
            com.sun.mail.iap.ByteArray r7 = r6.getByteArray()     // Catch:{ all -> 0x0057 }
            if (r7 != 0) goto L_0x0094
        L_0x008b:
            r15.forceCheckExpunged()     // Catch:{ all -> 0x0057 }
            com.sun.mail.iap.ByteArray r7 = new com.sun.mail.iap.ByteArray     // Catch:{ all -> 0x0057 }
            r2 = 0
            r7.<init>(r2)     // Catch:{ all -> 0x0057 }
        L_0x0094:
            monitor-exit(r14)     // Catch:{ all -> 0x0057 }
            int r2 = r15.pos
            if (r2 != 0) goto L_0x009c
            r15.checkSeen()
        L_0x009c:
            byte[] r2 = r7.getBytes()
            r15.buf = r2
            int r2 = r7.getStart()
            r15.bufpos = r2
            int r9 = r7.getCount()
            if (r6 == 0) goto L_0x00e3
            int r10 = r6.getOrigin()
        L_0x00b2:
            if (r10 >= 0) goto L_0x00ec
            int r2 = r15.pos
            if (r2 != 0) goto L_0x00e8
            if (r9 == r4) goto L_0x00e6
            r2 = r12
        L_0x00bb:
            r15.lastBuffer = r2
        L_0x00bd:
            int r2 = r15.bufpos
            int r2 = r2 + r9
            r15.bufcount = r2
            int r2 = r15.pos
            int r2 = r2 + r9
            r15.pos = r2
            goto L_0x001b
        L_0x00c9:
            java.lang.String r2 = r15.section     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            int r3 = r15.pos     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            com.sun.mail.iap.ByteArray r5 = r15.readbuf     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            com.sun.mail.imap.protocol.BODY r6 = r0.fetchBody(r1, r2, r3, r4, r5)     // Catch:{ ProtocolException -> 0x0049, FolderClosedException -> 0x00d4 }
            goto L_0x0083
        L_0x00d4:
            r8 = move-exception
            com.sun.mail.util.FolderClosedIOException r2 = new com.sun.mail.util.FolderClosedIOException     // Catch:{ all -> 0x0057 }
            javax.mail.Folder r3 = r8.getFolder()     // Catch:{ all -> 0x0057 }
            java.lang.String r5 = r8.getMessage()     // Catch:{ all -> 0x0057 }
            r2.<init>(r3, r5)     // Catch:{ all -> 0x0057 }
            throw r2     // Catch:{ all -> 0x0057 }
        L_0x00e3:
            int r10 = r15.pos
            goto L_0x00b2
        L_0x00e6:
            r2 = r13
            goto L_0x00bb
        L_0x00e8:
            r9 = 0
            r15.lastBuffer = r12
            goto L_0x00bd
        L_0x00ec:
            int r2 = r15.pos
            if (r10 != r2) goto L_0x00f7
            if (r9 >= r4) goto L_0x00f5
        L_0x00f2:
            r15.lastBuffer = r12
            goto L_0x00bd
        L_0x00f5:
            r12 = r13
            goto L_0x00f2
        L_0x00f7:
            r9 = 0
            r15.lastBuffer = r12
            goto L_0x00bd
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPInputStream.fill():void");
    }

    public synchronized int read() throws IOException {
        byte b;
        if (this.bufpos >= this.bufcount) {
            fill();
            if (this.bufpos >= this.bufcount) {
                b = -1;
            }
        }
        byte[] bArr = this.buf;
        int i = this.bufpos;
        this.bufpos = i + 1;
        b = bArr[i] & Ev3Constants.Opcode.TST;
        return b;
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int cnt;
        int avail = this.bufcount - this.bufpos;
        if (avail <= 0) {
            fill();
            avail = this.bufcount - this.bufpos;
            if (avail <= 0) {
                cnt = -1;
            }
        }
        if (avail < len) {
            cnt = avail;
        } else {
            cnt = len;
        }
        System.arraycopy(this.buf, this.bufpos, b, off, cnt);
        this.bufpos += cnt;
        return cnt;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public synchronized int available() throws IOException {
        return this.bufcount - this.bufpos;
    }

    private void checkSeen() {
        if (!this.peek) {
            try {
                Folder f = this.msg.getFolder();
                if (f != null && f.getMode() != 1 && !this.msg.isSet(Flags.Flag.SEEN)) {
                    this.msg.setFlag(Flags.Flag.SEEN, true);
                }
            } catch (MessagingException e) {
            }
        }
    }
}
