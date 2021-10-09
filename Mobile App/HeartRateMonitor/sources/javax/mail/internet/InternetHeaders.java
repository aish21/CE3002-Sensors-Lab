package javax.mail.internet;

import com.sun.mail.util.PropUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.Header;
import javax.mail.MessagingException;

public class InternetHeaders {
    private static final boolean ignoreWhitespaceLines = PropUtil.getBooleanSystemProperty("mail.mime.ignorewhitespacelines", false);
    protected List<InternetHeader> headers;

    protected static final class InternetHeader extends Header {
        String line;

        public InternetHeader(String l) {
            super("", "");
            int i = l.indexOf(58);
            if (i < 0) {
                this.name = l.trim();
            } else {
                this.name = l.substring(0, i).trim();
            }
            this.line = l;
        }

        public InternetHeader(String n, String v) {
            super(n, "");
            if (v != null) {
                this.line = n + ": " + v;
            } else {
                this.line = null;
            }
        }

        public String getValue() {
            int i = this.line.indexOf(58);
            if (i < 0) {
                return this.line;
            }
            int j = i + 1;
            while (j < this.line.length() && ((c = this.line.charAt(j)) == ' ' || c == 9 || c == 13 || c == 10)) {
                j++;
            }
            return this.line.substring(j);
        }
    }

    static class MatchEnum {

        /* renamed from: e */
        private Iterator<InternetHeader> f301e;
        private boolean match;
        private String[] names;
        private InternetHeader next_header = null;
        private boolean want_line;

        MatchEnum(List<InternetHeader> v, String[] n, boolean m, boolean l) {
            this.f301e = v.iterator();
            this.names = n;
            this.match = m;
            this.want_line = l;
        }

        public boolean hasMoreElements() {
            if (this.next_header == null) {
                this.next_header = nextMatch();
            }
            return this.next_header != null;
        }

        public Object nextElement() {
            if (this.next_header == null) {
                this.next_header = nextMatch();
            }
            if (this.next_header == null) {
                throw new NoSuchElementException("No more headers");
            }
            InternetHeader h = this.next_header;
            this.next_header = null;
            if (this.want_line) {
                return h.line;
            }
            return new Header(h.getName(), h.getValue());
        }

        private InternetHeader nextMatch() {
            while (this.f301e.hasNext()) {
                InternetHeader h = this.f301e.next();
                if (h.line != null) {
                    if (this.names != null) {
                        int i = 0;
                        while (true) {
                            if (i < this.names.length) {
                                if (!this.names[i].equalsIgnoreCase(h.getName())) {
                                    i++;
                                } else if (this.match) {
                                    return h;
                                }
                            } else if (!this.match) {
                                return h;
                            }
                        }
                    } else if (this.match) {
                        return null;
                    } else {
                        return h;
                    }
                }
            }
            return null;
        }
    }

    static class MatchStringEnum extends MatchEnum implements Enumeration<String> {
        MatchStringEnum(List<InternetHeader> v, String[] n, boolean m) {
            super(v, n, m, true);
        }

        public String nextElement() {
            return (String) super.nextElement();
        }
    }

    static class MatchHeaderEnum extends MatchEnum implements Enumeration<Header> {
        MatchHeaderEnum(List<InternetHeader> v, String[] n, boolean m) {
            super(v, n, m, false);
        }

        public Header nextElement() {
            return (Header) super.nextElement();
        }
    }

    public InternetHeaders() {
        this.headers = new ArrayList(40);
        this.headers.add(new InternetHeader("Return-Path", (String) null));
        this.headers.add(new InternetHeader("Received", (String) null));
        this.headers.add(new InternetHeader("Resent-Date", (String) null));
        this.headers.add(new InternetHeader("Resent-From", (String) null));
        this.headers.add(new InternetHeader("Resent-Sender", (String) null));
        this.headers.add(new InternetHeader("Resent-To", (String) null));
        this.headers.add(new InternetHeader("Resent-Cc", (String) null));
        this.headers.add(new InternetHeader("Resent-Bcc", (String) null));
        this.headers.add(new InternetHeader("Resent-Message-Id", (String) null));
        this.headers.add(new InternetHeader("Date", (String) null));
        this.headers.add(new InternetHeader("From", (String) null));
        this.headers.add(new InternetHeader("Sender", (String) null));
        this.headers.add(new InternetHeader("Reply-To", (String) null));
        this.headers.add(new InternetHeader("To", (String) null));
        this.headers.add(new InternetHeader("Cc", (String) null));
        this.headers.add(new InternetHeader("Bcc", (String) null));
        this.headers.add(new InternetHeader("Message-Id", (String) null));
        this.headers.add(new InternetHeader("In-Reply-To", (String) null));
        this.headers.add(new InternetHeader("References", (String) null));
        this.headers.add(new InternetHeader("Subject", (String) null));
        this.headers.add(new InternetHeader("Comments", (String) null));
        this.headers.add(new InternetHeader("Keywords", (String) null));
        this.headers.add(new InternetHeader("Errors-To", (String) null));
        this.headers.add(new InternetHeader("MIME-Version", (String) null));
        this.headers.add(new InternetHeader("Content-Type", (String) null));
        this.headers.add(new InternetHeader("Content-Transfer-Encoding", (String) null));
        this.headers.add(new InternetHeader("Content-MD5", (String) null));
        this.headers.add(new InternetHeader(":", (String) null));
        this.headers.add(new InternetHeader("Content-Length", (String) null));
        this.headers.add(new InternetHeader("Status", (String) null));
    }

    public InternetHeaders(InputStream is) throws MessagingException {
        this(is, false);
    }

    public InternetHeaders(InputStream is, boolean allowutf8) throws MessagingException {
        this.headers = new ArrayList(40);
        load(is, allowutf8);
    }

    public void load(InputStream is) throws MessagingException {
        load(is, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0022 A[Catch:{ IOException -> 0x0050 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void load(java.io.InputStream r10, boolean r11) throws javax.mail.MessagingException {
        /*
            r9 = this;
            com.sun.mail.util.LineInputStream r4 = new com.sun.mail.util.LineInputStream
            r4.<init>(r10, r11)
            r6 = 0
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r0 = 1
        L_0x000c:
            java.lang.String r2 = r4.readLine()     // Catch:{ IOException -> 0x0050 }
            if (r2 == 0) goto L_0x0059
            java.lang.String r7 = " "
            boolean r7 = r2.startsWith(r7)     // Catch:{ IOException -> 0x0050 }
            if (r7 != 0) goto L_0x0022
            java.lang.String r7 = "\t"
            boolean r7 = r2.startsWith(r7)     // Catch:{ IOException -> 0x0050 }
            if (r7 == 0) goto L_0x0059
        L_0x0022:
            if (r6 == 0) goto L_0x0028
            r3.append(r6)     // Catch:{ IOException -> 0x0050 }
            r6 = 0
        L_0x0028:
            if (r0 == 0) goto L_0x0041
            java.lang.String r5 = r2.trim()     // Catch:{ IOException -> 0x0050 }
            int r7 = r5.length()     // Catch:{ IOException -> 0x0050 }
            if (r7 <= 0) goto L_0x0037
            r3.append(r5)     // Catch:{ IOException -> 0x0050 }
        L_0x0037:
            r0 = 0
            if (r2 == 0) goto L_0x0040
            boolean r7 = isEmpty(r2)     // Catch:{ IOException -> 0x0050 }
            if (r7 == 0) goto L_0x000c
        L_0x0040:
            return
        L_0x0041:
            int r7 = r3.length()     // Catch:{ IOException -> 0x0050 }
            if (r7 <= 0) goto L_0x004c
            java.lang.String r7 = "\r\n"
            r3.append(r7)     // Catch:{ IOException -> 0x0050 }
        L_0x004c:
            r3.append(r2)     // Catch:{ IOException -> 0x0050 }
            goto L_0x0037
        L_0x0050:
            r1 = move-exception
            javax.mail.MessagingException r7 = new javax.mail.MessagingException
            java.lang.String r8 = "Error in input stream"
            r7.<init>(r8, r1)
            throw r7
        L_0x0059:
            if (r6 == 0) goto L_0x0060
            r9.addHeaderLine(r6)     // Catch:{ IOException -> 0x0050 }
        L_0x005e:
            r6 = r2
            goto L_0x0037
        L_0x0060:
            int r7 = r3.length()     // Catch:{ IOException -> 0x0050 }
            if (r7 <= 0) goto L_0x005e
            java.lang.String r7 = r3.toString()     // Catch:{ IOException -> 0x0050 }
            r9.addHeaderLine(r7)     // Catch:{ IOException -> 0x0050 }
            r7 = 0
            r3.setLength(r7)     // Catch:{ IOException -> 0x0050 }
            goto L_0x005e
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.InternetHeaders.load(java.io.InputStream, boolean):void");
    }

    private static final boolean isEmpty(String line) {
        return line.length() == 0 || (ignoreWhitespaceLines && line.trim().length() == 0);
    }

    public String[] getHeader(String name) {
        List<String> v = new ArrayList<>();
        for (InternetHeader h : this.headers) {
            if (name.equalsIgnoreCase(h.getName()) && h.line != null) {
                v.add(h.getValue());
            }
        }
        if (v.size() == 0) {
            return null;
        }
        return (String[]) v.toArray(new String[v.size()]);
    }

    public String getHeader(String name, String delimiter) {
        String[] s = getHeader(name);
        if (s == null) {
            return null;
        }
        if (s.length == 1 || delimiter == null) {
            return s[0];
        }
        StringBuilder r = new StringBuilder(s[0]);
        for (int i = 1; i < s.length; i++) {
            r.append(delimiter);
            r.append(s[i]);
        }
        return r.toString();
    }

    public void setHeader(String name, String value) {
        int j;
        boolean found = false;
        int i = 0;
        while (i < this.headers.size()) {
            InternetHeader h = this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                if (!found) {
                    if (h.line == null || (j = h.line.indexOf(58)) < 0) {
                        h.line = name + ": " + value;
                    } else {
                        h.line = h.line.substring(0, j + 1) + " " + value;
                    }
                    found = true;
                } else {
                    this.headers.remove(i);
                    i--;
                }
            }
            i++;
        }
        if (!found) {
            addHeader(name, value);
        }
    }

    public void addHeader(String name, String value) {
        int pos = this.headers.size();
        boolean addReverse = name.equalsIgnoreCase("Received") || name.equalsIgnoreCase("Return-Path");
        if (addReverse) {
            pos = 0;
        }
        for (int i = this.headers.size() - 1; i >= 0; i--) {
            InternetHeader h = this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                if (addReverse) {
                    pos = i;
                } else {
                    this.headers.add(i + 1, new InternetHeader(name, value));
                    return;
                }
            }
            if (!addReverse && h.getName().equals(":")) {
                pos = i;
            }
        }
        this.headers.add(pos, new InternetHeader(name, value));
    }

    public void removeHeader(String name) {
        for (int i = 0; i < this.headers.size(); i++) {
            InternetHeader h = this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                h.line = null;
            }
        }
    }

    public Enumeration<Header> getAllHeaders() {
        return new MatchHeaderEnum(this.headers, (String[]) null, false);
    }

    public Enumeration<Header> getMatchingHeaders(String[] names) {
        return new MatchHeaderEnum(this.headers, names, true);
    }

    public Enumeration<Header> getNonMatchingHeaders(String[] names) {
        return new MatchHeaderEnum(this.headers, names, false);
    }

    public void addHeaderLine(String line) {
        try {
            char c = line.charAt(0);
            if (c == ' ' || c == 9) {
                InternetHeader h = this.headers.get(this.headers.size() - 1);
                h.line += "\r\n" + line;
                return;
            }
            this.headers.add(new InternetHeader(line));
        } catch (StringIndexOutOfBoundsException e) {
        } catch (NoSuchElementException e2) {
        }
    }

    public Enumeration<String> getAllHeaderLines() {
        return getNonMatchingHeaderLines((String[]) null);
    }

    public Enumeration<String> getMatchingHeaderLines(String[] names) {
        return new MatchStringEnum(this.headers, names, true);
    }

    public Enumeration<String> getNonMatchingHeaderLines(String[] names) {
        return new MatchStringEnum(this.headers, names, false);
    }
}
