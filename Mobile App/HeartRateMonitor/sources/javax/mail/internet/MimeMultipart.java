package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.PropUtil;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessageAware;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;

public class MimeMultipart extends Multipart {
    protected boolean allowEmpty;
    protected boolean complete;

    /* renamed from: ds */
    protected DataSource f304ds;
    protected boolean ignoreExistingBoundaryParameter;
    protected boolean ignoreMissingBoundaryParameter;
    protected boolean ignoreMissingEndBoundary;
    protected boolean parsed;
    protected String preamble;

    public MimeMultipart() {
        this("mixed");
    }

    public MimeMultipart(String subtype) {
        this.f304ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        this.ignoreMissingEndBoundary = true;
        this.ignoreMissingBoundaryParameter = true;
        this.ignoreExistingBoundaryParameter = false;
        this.allowEmpty = false;
        String boundary = UniqueValue.getUniqueBoundaryValue();
        ContentType cType = new ContentType("multipart", subtype, (ParameterList) null);
        cType.setParameter("boundary", boundary);
        this.contentType = cType.toString();
        initializeProperties();
    }

    public MimeMultipart(BodyPart... parts) throws MessagingException {
        this();
        for (BodyPart bp : parts) {
            super.addBodyPart(bp);
        }
    }

    public MimeMultipart(String subtype, BodyPart... parts) throws MessagingException {
        this(subtype);
        for (BodyPart bp : parts) {
            super.addBodyPart(bp);
        }
    }

    public MimeMultipart(DataSource ds) throws MessagingException {
        this.f304ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        this.ignoreMissingEndBoundary = true;
        this.ignoreMissingBoundaryParameter = true;
        this.ignoreExistingBoundaryParameter = false;
        this.allowEmpty = false;
        if (ds instanceof MessageAware) {
            setParent(((MessageAware) ds).getMessageContext().getPart());
        }
        if (ds instanceof MultipartDataSource) {
            setMultipartDataSource((MultipartDataSource) ds);
            return;
        }
        this.parsed = false;
        this.f304ds = ds;
        this.contentType = ds.getContentType();
    }

    /* access modifiers changed from: protected */
    public void initializeProperties() {
        this.ignoreMissingEndBoundary = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingendboundary", true);
        this.ignoreMissingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingboundaryparameter", true);
        this.ignoreExistingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoreexistingboundaryparameter", false);
        this.allowEmpty = PropUtil.getBooleanSystemProperty("mail.mime.multipart.allowempty", false);
    }

    public synchronized void setSubType(String subtype) throws MessagingException {
        ContentType cType = new ContentType(this.contentType);
        cType.setSubType(subtype);
        this.contentType = cType.toString();
    }

    public synchronized int getCount() throws MessagingException {
        parse();
        return super.getCount();
    }

    public synchronized BodyPart getBodyPart(int index) throws MessagingException {
        parse();
        return super.getBodyPart(index);
    }

    public synchronized BodyPart getBodyPart(String CID) throws MessagingException {
        MimeBodyPart part;
        parse();
        int count = getCount();
        int i = 0;
        while (true) {
            if (i < count) {
                part = (MimeBodyPart) getBodyPart(i);
                String s = part.getContentID();
                if (s != null && s.equals(CID)) {
                    break;
                }
                i++;
            } else {
                part = null;
                break;
            }
        }
        return part;
    }

    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        parse();
        return super.removeBodyPart(part);
    }

    public void removeBodyPart(int index) throws MessagingException {
        parse();
        super.removeBodyPart(index);
    }

    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        parse();
        super.addBodyPart(part);
    }

    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        parse();
        super.addBodyPart(part, index);
    }

    public synchronized boolean isComplete() throws MessagingException {
        parse();
        return this.complete;
    }

    public synchronized String getPreamble() throws MessagingException {
        parse();
        return this.preamble;
    }

    public synchronized void setPreamble(String preamble2) throws MessagingException {
        this.preamble = preamble2;
    }

    /* access modifiers changed from: protected */
    public synchronized void updateHeaders() throws MessagingException {
        parse();
        for (int i = 0; i < this.parts.size(); i++) {
            ((MimeBodyPart) this.parts.elementAt(i)).updateHeaders();
        }
    }

    public synchronized void writeTo(OutputStream os) throws IOException, MessagingException {
        parse();
        String boundary = "--" + new ContentType(this.contentType).getParameter("boundary");
        LineOutputStream los = new LineOutputStream(os);
        if (this.preamble != null) {
            byte[] pb = ASCIIUtility.getBytes(this.preamble);
            los.write(pb);
            if (!(pb.length <= 0 || pb[pb.length - 1] == 13 || pb[pb.length - 1] == 10)) {
                los.writeln();
            }
        }
        if (this.parts.size() != 0) {
            for (int i = 0; i < this.parts.size(); i++) {
                los.writeln(boundary);
                ((MimeBodyPart) this.parts.elementAt(i)).writeTo(os);
                los.writeln();
            }
        } else if (this.allowEmpty) {
            los.writeln(boundary);
            los.writeln();
        } else {
            throw new MessagingException("Empty multipart: " + this.contentType);
        }
        los.writeln(boundary + "--");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x021e A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void parse() throws javax.mail.MessagingException {
        /*
            r46 = this;
            monitor-enter(r46)
            r0 = r46
            boolean r0 = r0.parsed     // Catch:{ all -> 0x00a0 }
            r42 = r0
            if (r42 == 0) goto L_0x000b
        L_0x0009:
            monitor-exit(r46)
            return
        L_0x000b:
            r46.initializeProperties()     // Catch:{ all -> 0x00a0 }
            r25 = 0
            r37 = 0
            r40 = 0
            r18 = 0
            r0 = r46
            javax.activation.DataSource r0 = r0.f304ds     // Catch:{ Exception -> 0x00a3 }
            r42 = r0
            java.io.InputStream r25 = r42.getInputStream()     // Catch:{ Exception -> 0x00a3 }
            r0 = r25
            boolean r0 = r0 instanceof java.io.ByteArrayInputStream     // Catch:{ Exception -> 0x00a3 }
            r42 = r0
            if (r42 != 0) goto L_0x0043
            r0 = r25
            boolean r0 = r0 instanceof java.io.BufferedInputStream     // Catch:{ Exception -> 0x00a3 }
            r42 = r0
            if (r42 != 0) goto L_0x0043
            r0 = r25
            boolean r0 = r0 instanceof javax.mail.internet.SharedInputStream     // Catch:{ Exception -> 0x00a3 }
            r42 = r0
            if (r42 != 0) goto L_0x0043
            java.io.BufferedInputStream r26 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x00a3 }
            r0 = r26
            r1 = r25
            r0.<init>(r1)     // Catch:{ Exception -> 0x00a3 }
            r25 = r26
        L_0x0043:
            r0 = r25
            boolean r0 = r0 instanceof javax.mail.internet.SharedInputStream     // Catch:{ all -> 0x00a0 }
            r42 = r0
            if (r42 == 0) goto L_0x0051
            r0 = r25
            javax.mail.internet.SharedInputStream r0 = (javax.mail.internet.SharedInputStream) r0     // Catch:{ all -> 0x00a0 }
            r37 = r0
        L_0x0051:
            javax.mail.internet.ContentType r15 = new javax.mail.internet.ContentType     // Catch:{ all -> 0x00a0 }
            r0 = r46
            java.lang.String r0 = r0.contentType     // Catch:{ all -> 0x00a0 }
            r42 = r0
            r0 = r42
            r15.<init>(r0)     // Catch:{ all -> 0x00a0 }
            r11 = 0
            r0 = r46
            boolean r0 = r0.ignoreExistingBoundaryParameter     // Catch:{ all -> 0x00a0 }
            r42 = r0
            if (r42 != 0) goto L_0x0086
            java.lang.String r42 = "boundary"
            r0 = r42
            java.lang.String r12 = r15.getParameter(r0)     // Catch:{ all -> 0x00a0 }
            if (r12 == 0) goto L_0x0086
            java.lang.StringBuilder r42 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a0 }
            r42.<init>()     // Catch:{ all -> 0x00a0 }
            java.lang.String r43 = "--"
            java.lang.StringBuilder r42 = r42.append(r43)     // Catch:{ all -> 0x00a0 }
            r0 = r42
            java.lang.StringBuilder r42 = r0.append(r12)     // Catch:{ all -> 0x00a0 }
            java.lang.String r11 = r42.toString()     // Catch:{ all -> 0x00a0 }
        L_0x0086:
            if (r11 != 0) goto L_0x00b2
            r0 = r46
            boolean r0 = r0.ignoreMissingBoundaryParameter     // Catch:{ all -> 0x00a0 }
            r42 = r0
            if (r42 != 0) goto L_0x00b2
            r0 = r46
            boolean r0 = r0.ignoreExistingBoundaryParameter     // Catch:{ all -> 0x00a0 }
            r42 = r0
            if (r42 != 0) goto L_0x00b2
            javax.mail.internet.ParseException r42 = new javax.mail.internet.ParseException     // Catch:{ all -> 0x00a0 }
            java.lang.String r43 = "Missing boundary parameter"
            r42.<init>(r43)     // Catch:{ all -> 0x00a0 }
            throw r42     // Catch:{ all -> 0x00a0 }
        L_0x00a0:
            r42 = move-exception
            monitor-exit(r46)
            throw r42
        L_0x00a3:
            r20 = move-exception
            javax.mail.MessagingException r42 = new javax.mail.MessagingException     // Catch:{ all -> 0x00a0 }
            java.lang.String r43 = "No inputstream from datasource"
            r0 = r42
            r1 = r43
            r2 = r20
            r0.<init>(r1, r2)     // Catch:{ all -> 0x00a0 }
            throw r42     // Catch:{ all -> 0x00a0 }
        L_0x00b2:
            com.sun.mail.util.LineInputStream r31 = new com.sun.mail.util.LineInputStream     // Catch:{ IOException -> 0x0191 }
            r0 = r31
            r1 = r25
            r0.<init>(r1)     // Catch:{ IOException -> 0x0191 }
            r34 = 0
        L_0x00bd:
            java.lang.String r32 = r31.readLine()     // Catch:{ IOException -> 0x0191 }
            if (r32 == 0) goto L_0x00f7
            int r42 = r32.length()     // Catch:{ IOException -> 0x0191 }
            int r24 = r42 + -1
        L_0x00c9:
            if (r24 < 0) goto L_0x00df
            r0 = r32
            r1 = r24
            char r14 = r0.charAt(r1)     // Catch:{ IOException -> 0x0191 }
            r42 = 32
            r0 = r42
            if (r14 == r0) goto L_0x0115
            r42 = 9
            r0 = r42
            if (r14 == r0) goto L_0x0115
        L_0x00df:
            r42 = 0
            int r43 = r24 + 1
            r0 = r32
            r1 = r42
            r2 = r43
            java.lang.String r32 = r0.substring(r1, r2)     // Catch:{ IOException -> 0x0191 }
            if (r11 == 0) goto L_0x013f
            r0 = r32
            boolean r42 = r0.equals(r11)     // Catch:{ IOException -> 0x0191 }
            if (r42 == 0) goto L_0x0118
        L_0x00f7:
            if (r34 == 0) goto L_0x0103
            java.lang.String r42 = r34.toString()     // Catch:{ IOException -> 0x0191 }
            r0 = r42
            r1 = r46
            r1.preamble = r0     // Catch:{ IOException -> 0x0191 }
        L_0x0103:
            if (r32 != 0) goto L_0x01b1
            r0 = r46
            boolean r0 = r0.allowEmpty     // Catch:{ IOException -> 0x0191 }
            r42 = r0
            if (r42 == 0) goto L_0x01a9
            r25.close()     // Catch:{ IOException -> 0x0112 }
            goto L_0x0009
        L_0x0112:
            r42 = move-exception
            goto L_0x0009
        L_0x0115:
            int r24 = r24 + -1
            goto L_0x00c9
        L_0x0118:
            int r42 = r32.length()     // Catch:{ IOException -> 0x0191 }
            int r43 = r11.length()     // Catch:{ IOException -> 0x0191 }
            int r43 = r43 + 2
            r0 = r42
            r1 = r43
            if (r0 != r1) goto L_0x0169
            r0 = r32
            boolean r42 = r0.startsWith(r11)     // Catch:{ IOException -> 0x0191 }
            if (r42 == 0) goto L_0x0169
            java.lang.String r42 = "--"
            r0 = r32
            r1 = r42
            boolean r42 = r0.endsWith(r1)     // Catch:{ IOException -> 0x0191 }
            if (r42 == 0) goto L_0x0169
            r32 = 0
            goto L_0x00f7
        L_0x013f:
            int r42 = r32.length()     // Catch:{ IOException -> 0x0191 }
            r43 = 2
            r0 = r42
            r1 = r43
            if (r0 <= r1) goto L_0x0169
            java.lang.String r42 = "--"
            r0 = r32
            r1 = r42
            boolean r42 = r0.startsWith(r1)     // Catch:{ IOException -> 0x0191 }
            if (r42 == 0) goto L_0x0169
            int r42 = r32.length()     // Catch:{ IOException -> 0x0191 }
            r43 = 4
            r0 = r42
            r1 = r43
            if (r0 <= r1) goto L_0x01a5
            boolean r42 = allDashes(r32)     // Catch:{ IOException -> 0x0191 }
            if (r42 == 0) goto L_0x01a5
        L_0x0169:
            int r42 = r32.length()     // Catch:{ IOException -> 0x0191 }
            if (r42 <= 0) goto L_0x00bd
            if (r34 != 0) goto L_0x0180
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0191 }
            int r42 = r32.length()     // Catch:{ IOException -> 0x0191 }
            int r42 = r42 + 2
            r0 = r34
            r1 = r42
            r0.<init>(r1)     // Catch:{ IOException -> 0x0191 }
        L_0x0180:
            r0 = r34
            r1 = r32
            java.lang.StringBuilder r42 = r0.append(r1)     // Catch:{ IOException -> 0x0191 }
            java.lang.String r43 = java.lang.System.lineSeparator()     // Catch:{ IOException -> 0x0191 }
            r42.append(r43)     // Catch:{ IOException -> 0x0191 }
            goto L_0x00bd
        L_0x0191:
            r29 = move-exception
            javax.mail.MessagingException r42 = new javax.mail.MessagingException     // Catch:{ all -> 0x01a0 }
            java.lang.String r43 = "IO Error"
            r0 = r42
            r1 = r43
            r2 = r29
            r0.<init>(r1, r2)     // Catch:{ all -> 0x01a0 }
            throw r42     // Catch:{ all -> 0x01a0 }
        L_0x01a0:
            r42 = move-exception
            r25.close()     // Catch:{ IOException -> 0x044e }
        L_0x01a4:
            throw r42     // Catch:{ all -> 0x00a0 }
        L_0x01a5:
            r11 = r32
            goto L_0x00f7
        L_0x01a9:
            javax.mail.internet.ParseException r42 = new javax.mail.internet.ParseException     // Catch:{ IOException -> 0x0191 }
            java.lang.String r43 = "Missing start boundary"
            r42.<init>(r43)     // Catch:{ IOException -> 0x0191 }
            throw r42     // Catch:{ IOException -> 0x0191 }
        L_0x01b1:
            byte[] r10 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r11)     // Catch:{ IOException -> 0x0191 }
            int r9 = r10.length     // Catch:{ IOException -> 0x0191 }
            r42 = 256(0x100, float:3.59E-43)
            r0 = r42
            int[] r8 = new int[r0]     // Catch:{ IOException -> 0x0191 }
            r24 = 0
        L_0x01be:
            r0 = r24
            if (r0 >= r9) goto L_0x01d1
            byte r42 = r10[r24]     // Catch:{ IOException -> 0x0191 }
            r0 = r42
            r0 = r0 & 255(0xff, float:3.57E-43)
            r42 = r0
            int r43 = r24 + 1
            r8[r42] = r43     // Catch:{ IOException -> 0x0191 }
            int r24 = r24 + 1
            goto L_0x01be
        L_0x01d1:
            int[] r0 = new int[r9]     // Catch:{ IOException -> 0x0191 }
            r22 = r0
            r24 = r9
        L_0x01d7:
            if (r24 <= 0) goto L_0x01fe
            int r30 = r9 + -1
        L_0x01db:
            r0 = r30
            r1 = r24
            if (r0 < r1) goto L_0x01f4
            byte r42 = r10[r30]     // Catch:{ IOException -> 0x0191 }
            int r43 = r30 - r24
            byte r43 = r10[r43]     // Catch:{ IOException -> 0x0191 }
            r0 = r42
            r1 = r43
            if (r0 != r1) goto L_0x01fb
            int r42 = r30 + -1
            r22[r42] = r24     // Catch:{ IOException -> 0x0191 }
            int r30 = r30 + -1
            goto L_0x01db
        L_0x01f4:
            if (r30 <= 0) goto L_0x01fb
            int r30 = r30 + -1
            r22[r30] = r24     // Catch:{ IOException -> 0x0191 }
            goto L_0x01f4
        L_0x01fb:
            int r24 = r24 + -1
            goto L_0x01d7
        L_0x01fe:
            int r42 = r9 + -1
            r43 = 1
            r22[r42] = r43     // Catch:{ IOException -> 0x0191 }
            r16 = 0
        L_0x0206:
            if (r16 != 0) goto L_0x0236
            r23 = 0
            if (r37 == 0) goto L_0x0243
            long r40 = r37.getPosition()     // Catch:{ IOException -> 0x0191 }
        L_0x0210:
            java.lang.String r32 = r31.readLine()     // Catch:{ IOException -> 0x0191 }
            if (r32 == 0) goto L_0x021c
            int r42 = r32.length()     // Catch:{ IOException -> 0x0191 }
            if (r42 > 0) goto L_0x0210
        L_0x021c:
            if (r32 != 0) goto L_0x024b
            r0 = r46
            boolean r0 = r0.ignoreMissingEndBoundary     // Catch:{ IOException -> 0x0191 }
            r42 = r0
            if (r42 != 0) goto L_0x022e
            javax.mail.internet.ParseException r42 = new javax.mail.internet.ParseException     // Catch:{ IOException -> 0x0191 }
            java.lang.String r43 = "missing multipart end boundary"
            r42.<init>(r43)     // Catch:{ IOException -> 0x0191 }
            throw r42     // Catch:{ IOException -> 0x0191 }
        L_0x022e:
            r42 = 0
            r0 = r42
            r1 = r46
            r1.complete = r0     // Catch:{ IOException -> 0x0191 }
        L_0x0236:
            r25.close()     // Catch:{ IOException -> 0x044b }
        L_0x0239:
            r42 = 1
            r0 = r42
            r1 = r46
            r1.parsed = r0     // Catch:{ all -> 0x00a0 }
            goto L_0x0009
        L_0x0243:
            r0 = r46
            r1 = r25
            javax.mail.internet.InternetHeaders r23 = r0.createInternetHeaders(r1)     // Catch:{ IOException -> 0x0191 }
        L_0x024b:
            boolean r42 = r25.markSupported()     // Catch:{ IOException -> 0x0191 }
            if (r42 != 0) goto L_0x0259
            javax.mail.MessagingException r42 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0191 }
            java.lang.String r43 = "Stream doesn't support mark"
            r42.<init>(r43)     // Catch:{ IOException -> 0x0191 }
            throw r42     // Catch:{ IOException -> 0x0191 }
        L_0x0259:
            r13 = 0
            if (r37 != 0) goto L_0x02a0
            java.io.ByteArrayOutputStream r13 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x0191 }
            r13.<init>()     // Catch:{ IOException -> 0x0191 }
        L_0x0261:
            byte[] r0 = new byte[r9]     // Catch:{ IOException -> 0x0191 }
            r28 = r0
            byte[] r0 = new byte[r9]     // Catch:{ IOException -> 0x0191 }
            r36 = r0
            r27 = 0
            r35 = 0
            r21 = 1
        L_0x026f:
            int r42 = r9 + 4
            r0 = r42
            int r0 = r0 + 1000
            r42 = r0
            r0 = r25
            r1 = r42
            r0.mark(r1)     // Catch:{ IOException -> 0x0191 }
            r17 = 0
            r42 = 0
            r0 = r25
            r1 = r28
            r2 = r42
            int r27 = readFully(r0, r1, r2, r9)     // Catch:{ IOException -> 0x0191 }
            r0 = r27
            if (r0 >= r9) goto L_0x02d2
            r0 = r46
            boolean r0 = r0.ignoreMissingEndBoundary     // Catch:{ IOException -> 0x0191 }
            r42 = r0
            if (r42 != 0) goto L_0x02a5
            javax.mail.internet.ParseException r42 = new javax.mail.internet.ParseException     // Catch:{ IOException -> 0x0191 }
            java.lang.String r43 = "missing multipart end boundary"
            r42.<init>(r43)     // Catch:{ IOException -> 0x0191 }
            throw r42     // Catch:{ IOException -> 0x0191 }
        L_0x02a0:
            long r18 = r37.getPosition()     // Catch:{ IOException -> 0x0191 }
            goto L_0x0261
        L_0x02a5:
            if (r37 == 0) goto L_0x02ab
            long r18 = r37.getPosition()     // Catch:{ IOException -> 0x0191 }
        L_0x02ab:
            r42 = 0
            r0 = r42
            r1 = r46
            r1.complete = r0     // Catch:{ IOException -> 0x0191 }
            r16 = 1
        L_0x02b5:
            if (r37 == 0) goto L_0x0415
            r0 = r37
            r1 = r40
            r3 = r18
            java.io.InputStream r42 = r0.newStream(r1, r3)     // Catch:{ IOException -> 0x0191 }
            r0 = r46
            r1 = r42
            javax.mail.internet.MimeBodyPart r33 = r0.createMimeBodyPartIs(r1)     // Catch:{ IOException -> 0x0191 }
        L_0x02c9:
            r0 = r46
            r1 = r33
            super.addBodyPart(r1)     // Catch:{ IOException -> 0x0191 }
            goto L_0x0206
        L_0x02d2:
            int r24 = r9 + -1
        L_0x02d4:
            if (r24 < 0) goto L_0x02e0
            byte r42 = r28[r24]     // Catch:{ IOException -> 0x0191 }
            byte r43 = r10[r24]     // Catch:{ IOException -> 0x0191 }
            r0 = r42
            r1 = r43
            if (r0 == r1) goto L_0x034a
        L_0x02e0:
            if (r24 >= 0) goto L_0x0386
            r17 = 0
            if (r21 != 0) goto L_0x0312
            int r42 = r35 + -1
            byte r6 = r36[r42]     // Catch:{ IOException -> 0x0191 }
            r42 = 13
            r0 = r42
            if (r6 == r0) goto L_0x02f6
            r42 = 10
            r0 = r42
            if (r6 != r0) goto L_0x0312
        L_0x02f6:
            r17 = 1
            r42 = 10
            r0 = r42
            if (r6 != r0) goto L_0x0312
            r42 = 2
            r0 = r35
            r1 = r42
            if (r0 < r1) goto L_0x0312
            int r42 = r35 + -2
            byte r6 = r36[r42]     // Catch:{ IOException -> 0x0191 }
            r42 = 13
            r0 = r42
            if (r6 != r0) goto L_0x0312
            r17 = 2
        L_0x0312:
            if (r21 != 0) goto L_0x0316
            if (r17 <= 0) goto L_0x0384
        L_0x0316:
            if (r37 == 0) goto L_0x0328
            long r42 = r37.getPosition()     // Catch:{ IOException -> 0x0191 }
            long r0 = (long) r9     // Catch:{ IOException -> 0x0191 }
            r44 = r0
            long r42 = r42 - r44
            r0 = r17
            long r0 = (long) r0     // Catch:{ IOException -> 0x0191 }
            r44 = r0
            long r18 = r42 - r44
        L_0x0328:
            int r7 = r25.read()     // Catch:{ IOException -> 0x0191 }
            r42 = 45
            r0 = r42
            if (r7 != r0) goto L_0x034d
            int r42 = r25.read()     // Catch:{ IOException -> 0x0191 }
            r43 = 45
            r0 = r42
            r1 = r43
            if (r0 != r1) goto L_0x034d
            r42 = 1
            r0 = r42
            r1 = r46
            r1.complete = r0     // Catch:{ IOException -> 0x0191 }
            r16 = 1
            goto L_0x02b5
        L_0x034a:
            int r24 = r24 + -1
            goto L_0x02d4
        L_0x034d:
            r42 = 32
            r0 = r42
            if (r7 == r0) goto L_0x0359
            r42 = 9
            r0 = r42
            if (r7 != r0) goto L_0x035e
        L_0x0359:
            int r7 = r25.read()     // Catch:{ IOException -> 0x0191 }
            goto L_0x034d
        L_0x035e:
            r42 = 10
            r0 = r42
            if (r7 == r0) goto L_0x02b5
            r42 = 13
            r0 = r42
            if (r7 != r0) goto L_0x0384
            r42 = 1
            r0 = r25
            r1 = r42
            r0.mark(r1)     // Catch:{ IOException -> 0x0191 }
            int r42 = r25.read()     // Catch:{ IOException -> 0x0191 }
            r43 = 10
            r0 = r42
            r1 = r43
            if (r0 == r1) goto L_0x02b5
            r25.reset()     // Catch:{ IOException -> 0x0191 }
            goto L_0x02b5
        L_0x0384:
            r24 = 0
        L_0x0386:
            int r42 = r24 + 1
            byte r43 = r28[r24]     // Catch:{ IOException -> 0x0191 }
            r43 = r43 & 127(0x7f, float:1.78E-43)
            r43 = r8[r43]     // Catch:{ IOException -> 0x0191 }
            int r42 = r42 - r43
            r43 = r22[r24]     // Catch:{ IOException -> 0x0191 }
            int r38 = java.lang.Math.max(r42, r43)     // Catch:{ IOException -> 0x0191 }
            r42 = 2
            r0 = r38
            r1 = r42
            if (r0 >= r1) goto L_0x03ec
            if (r37 != 0) goto L_0x03b5
            r42 = 1
            r0 = r35
            r1 = r42
            if (r0 <= r1) goto L_0x03b5
            r42 = 0
            int r43 = r35 + -1
            r0 = r36
            r1 = r42
            r2 = r43
            r13.write(r0, r1, r2)     // Catch:{ IOException -> 0x0191 }
        L_0x03b5:
            r25.reset()     // Catch:{ IOException -> 0x0191 }
            r42 = 1
            r0 = r46
            r1 = r25
            r2 = r42
            r0.skipFully(r1, r2)     // Catch:{ IOException -> 0x0191 }
            r42 = 1
            r0 = r35
            r1 = r42
            if (r0 < r1) goto L_0x03e1
            r42 = 0
            int r43 = r35 + -1
            byte r43 = r36[r43]     // Catch:{ IOException -> 0x0191 }
            r36[r42] = r43     // Catch:{ IOException -> 0x0191 }
            r42 = 1
            r43 = 0
            byte r43 = r28[r43]     // Catch:{ IOException -> 0x0191 }
            r36[r42] = r43     // Catch:{ IOException -> 0x0191 }
            r35 = 2
        L_0x03dd:
            r21 = 0
            goto L_0x026f
        L_0x03e1:
            r42 = 0
            r43 = 0
            byte r43 = r28[r43]     // Catch:{ IOException -> 0x0191 }
            r36[r42] = r43     // Catch:{ IOException -> 0x0191 }
            r35 = 1
            goto L_0x03dd
        L_0x03ec:
            if (r35 <= 0) goto L_0x03fb
            if (r37 != 0) goto L_0x03fb
            r42 = 0
            r0 = r36
            r1 = r42
            r2 = r35
            r13.write(r0, r1, r2)     // Catch:{ IOException -> 0x0191 }
        L_0x03fb:
            r35 = r38
            r25.reset()     // Catch:{ IOException -> 0x0191 }
            r0 = r35
            long r0 = (long) r0     // Catch:{ IOException -> 0x0191 }
            r42 = r0
            r0 = r46
            r1 = r25
            r2 = r42
            r0.skipFully(r1, r2)     // Catch:{ IOException -> 0x0191 }
            r39 = r28
            r28 = r36
            r36 = r39
            goto L_0x03dd
        L_0x0415:
            int r42 = r35 - r17
            if (r42 <= 0) goto L_0x0426
            r42 = 0
            int r43 = r35 - r17
            r0 = r36
            r1 = r42
            r2 = r43
            r13.write(r0, r1, r2)     // Catch:{ IOException -> 0x0191 }
        L_0x0426:
            r0 = r46
            boolean r0 = r0.complete     // Catch:{ IOException -> 0x0191 }
            r42 = r0
            if (r42 != 0) goto L_0x043b
            if (r27 <= 0) goto L_0x043b
            r42 = 0
            r0 = r28
            r1 = r42
            r2 = r27
            r13.write(r0, r1, r2)     // Catch:{ IOException -> 0x0191 }
        L_0x043b:
            byte[] r42 = r13.toByteArray()     // Catch:{ IOException -> 0x0191 }
            r0 = r46
            r1 = r23
            r2 = r42
            javax.mail.internet.MimeBodyPart r33 = r0.createMimeBodyPart(r1, r2)     // Catch:{ IOException -> 0x0191 }
            goto L_0x02c9
        L_0x044b:
            r42 = move-exception
            goto L_0x0239
        L_0x044e:
            r43 = move-exception
            goto L_0x01a4
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeMultipart.parse():void");
    }

    private static boolean allDashes(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '-') {
                return false;
            }
        }
        return true;
    }

    private static int readFully(InputStream in, byte[] buf, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int total = 0;
        while (len > 0) {
            int bsize = in.read(buf, off, len);
            if (bsize <= 0) {
                break;
            }
            off += bsize;
            total += bsize;
            len -= bsize;
        }
        if (total <= 0) {
            return -1;
        }
        return total;
    }

    private void skipFully(InputStream in, long offset) throws IOException {
        while (offset > 0) {
            long cur = in.skip(offset);
            if (cur <= 0) {
                throw new EOFException("can't skip");
            }
            offset -= cur;
        }
    }

    /* access modifiers changed from: protected */
    public InternetHeaders createInternetHeaders(InputStream is) throws MessagingException {
        return new InternetHeaders(is);
    }

    /* access modifiers changed from: protected */
    public MimeBodyPart createMimeBodyPart(InternetHeaders headers, byte[] content) throws MessagingException {
        return new MimeBodyPart(headers, content);
    }

    /* access modifiers changed from: protected */
    public MimeBodyPart createMimeBodyPart(InputStream is) throws MessagingException {
        return new MimeBodyPart(is);
    }

    private MimeBodyPart createMimeBodyPartIs(InputStream is) throws MessagingException {
        try {
            return createMimeBodyPart(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }
}
