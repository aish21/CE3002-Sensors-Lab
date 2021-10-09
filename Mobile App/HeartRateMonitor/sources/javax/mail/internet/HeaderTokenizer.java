package javax.mail.internet;

public class HeaderTokenizer {
    private static final Token EOFToken = new Token(-4, (String) null);
    public static final String MIME = "()<>@,;:\\\"\t []/?=";
    public static final String RFC822 = "()<>@,;:\\\"\t .[]";
    private int currentPos;
    private String delimiters;
    private int maxPos;
    private int nextPos;
    private int peekPos;
    private boolean skipComments;
    private String string;

    public static class Token {
        public static final int ATOM = -1;
        public static final int COMMENT = -3;
        public static final int EOF = -4;
        public static final int QUOTEDSTRING = -2;
        private int type;
        private String value;

        public Token(int type2, String value2) {
            this.type = type2;
            this.value = value2;
        }

        public int getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }
    }

    public HeaderTokenizer(String header, String delimiters2, boolean skipComments2) {
        this.string = header == null ? "" : header;
        this.skipComments = skipComments2;
        this.delimiters = delimiters2;
        this.peekPos = 0;
        this.nextPos = 0;
        this.currentPos = 0;
        this.maxPos = this.string.length();
    }

    public HeaderTokenizer(String header, String delimiters2) {
        this(header, delimiters2, true);
    }

    public HeaderTokenizer(String header) {
        this(header, RFC822);
    }

    public Token next() throws ParseException {
        return next(0, false);
    }

    public Token next(char endOfAtom) throws ParseException {
        return next(endOfAtom, false);
    }

    public Token next(char endOfAtom, boolean keepEscapes) throws ParseException {
        this.currentPos = this.nextPos;
        Token tk = getNext(endOfAtom, keepEscapes);
        int i = this.currentPos;
        this.peekPos = i;
        this.nextPos = i;
        return tk;
    }

    public Token peek() throws ParseException {
        this.currentPos = this.peekPos;
        Token tk = getNext(0, false);
        this.peekPos = this.currentPos;
        return tk;
    }

    public String getRemainder() {
        if (this.nextPos >= this.string.length()) {
            return null;
        }
        return this.string.substring(this.nextPos);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        return new javax.mail.internet.HeaderTokenizer.Token(-1, r13.string.substring(r5, r13.currentPos));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private javax.mail.internet.HeaderTokenizer.Token getNext(char r14, boolean r15) throws javax.mail.internet.ParseException {
        /*
            r13 = this;
            r12 = 127(0x7f, float:1.78E-43)
            r11 = -4
            r10 = 40
            r9 = 34
            r8 = 32
            int r6 = r13.currentPos
            int r7 = r13.maxPos
            if (r6 < r7) goto L_0x0012
            javax.mail.internet.HeaderTokenizer$Token r6 = EOFToken
        L_0x0011:
            return r6
        L_0x0012:
            int r6 = r13.skipWhiteSpace()
            if (r6 != r11) goto L_0x001b
            javax.mail.internet.HeaderTokenizer$Token r6 = EOFToken
            goto L_0x0011
        L_0x001b:
            r2 = 0
            java.lang.String r6 = r13.string
            int r7 = r13.currentPos
            char r0 = r6.charAt(r7)
        L_0x0024:
            if (r0 != r10) goto L_0x00a0
            int r6 = r13.currentPos
            int r5 = r6 + 1
            r13.currentPos = r5
            r3 = 1
        L_0x002d:
            if (r3 <= 0) goto L_0x0061
            int r6 = r13.currentPos
            int r7 = r13.maxPos
            if (r6 >= r7) goto L_0x0061
            java.lang.String r6 = r13.string
            int r7 = r13.currentPos
            char r0 = r6.charAt(r7)
            r6 = 92
            if (r0 != r6) goto L_0x004f
            int r6 = r13.currentPos
            int r6 = r6 + 1
            r13.currentPos = r6
            r2 = 1
        L_0x0048:
            int r6 = r13.currentPos
            int r6 = r6 + 1
            r13.currentPos = r6
            goto L_0x002d
        L_0x004f:
            r6 = 13
            if (r0 != r6) goto L_0x0055
            r2 = 1
            goto L_0x0048
        L_0x0055:
            if (r0 != r10) goto L_0x005a
            int r3 = r3 + 1
            goto L_0x0048
        L_0x005a:
            r6 = 41
            if (r0 != r6) goto L_0x0048
            int r3 = r3 + -1
            goto L_0x0048
        L_0x0061:
            if (r3 == 0) goto L_0x006b
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.String r7 = "Unbalanced comments"
            r6.<init>(r7)
            throw r6
        L_0x006b:
            boolean r6 = r13.skipComments
            if (r6 != 0) goto L_0x008d
            if (r2 == 0) goto L_0x0082
            java.lang.String r6 = r13.string
            int r7 = r13.currentPos
            int r7 = r7 + -1
            java.lang.String r4 = filterToken(r6, r5, r7, r15)
        L_0x007b:
            javax.mail.internet.HeaderTokenizer$Token r6 = new javax.mail.internet.HeaderTokenizer$Token
            r7 = -3
            r6.<init>(r7, r4)
            goto L_0x0011
        L_0x0082:
            java.lang.String r6 = r13.string
            int r7 = r13.currentPos
            int r7 = r7 + -1
            java.lang.String r4 = r6.substring(r5, r7)
            goto L_0x007b
        L_0x008d:
            int r6 = r13.skipWhiteSpace()
            if (r6 != r11) goto L_0x0097
            javax.mail.internet.HeaderTokenizer$Token r6 = EOFToken
            goto L_0x0011
        L_0x0097:
            java.lang.String r6 = r13.string
            int r7 = r13.currentPos
            char r0 = r6.charAt(r7)
            goto L_0x0024
        L_0x00a0:
            if (r0 != r9) goto L_0x00ae
            int r6 = r13.currentPos
            int r6 = r6 + 1
            r13.currentPos = r6
            javax.mail.internet.HeaderTokenizer$Token r6 = r13.collectString(r9, r15)
            goto L_0x0011
        L_0x00ae:
            if (r0 < r8) goto L_0x00ba
            if (r0 >= r12) goto L_0x00ba
            java.lang.String r6 = r13.delimiters
            int r6 = r6.indexOf(r0)
            if (r6 < 0) goto L_0x00dc
        L_0x00ba:
            if (r14 <= 0) goto L_0x00c4
            if (r0 == r14) goto L_0x00c4
            javax.mail.internet.HeaderTokenizer$Token r6 = r13.collectString(r14, r15)
            goto L_0x0011
        L_0x00c4:
            int r6 = r13.currentPos
            int r6 = r6 + 1
            r13.currentPos = r6
            r6 = 1
            char[] r1 = new char[r6]
            r6 = 0
            r1[r6] = r0
            javax.mail.internet.HeaderTokenizer$Token r6 = new javax.mail.internet.HeaderTokenizer$Token
            java.lang.String r7 = new java.lang.String
            r7.<init>(r1)
            r6.<init>(r0, r7)
            goto L_0x0011
        L_0x00dc:
            int r5 = r13.currentPos
        L_0x00de:
            int r6 = r13.currentPos
            int r7 = r13.maxPos
            if (r6 >= r7) goto L_0x0111
            java.lang.String r6 = r13.string
            int r7 = r13.currentPos
            char r0 = r6.charAt(r7)
            if (r0 < r8) goto L_0x00fe
            if (r0 >= r12) goto L_0x00fe
            if (r0 == r10) goto L_0x00fe
            if (r0 == r8) goto L_0x00fe
            if (r0 == r9) goto L_0x00fe
            java.lang.String r6 = r13.delimiters
            int r6 = r6.indexOf(r0)
            if (r6 < 0) goto L_0x010a
        L_0x00fe:
            if (r14 <= 0) goto L_0x0111
            if (r0 == r14) goto L_0x0111
            r13.currentPos = r5
            javax.mail.internet.HeaderTokenizer$Token r6 = r13.collectString(r14, r15)
            goto L_0x0011
        L_0x010a:
            int r6 = r13.currentPos
            int r6 = r6 + 1
            r13.currentPos = r6
            goto L_0x00de
        L_0x0111:
            javax.mail.internet.HeaderTokenizer$Token r6 = new javax.mail.internet.HeaderTokenizer$Token
            r7 = -1
            java.lang.String r8 = r13.string
            int r9 = r13.currentPos
            java.lang.String r8 = r8.substring(r5, r9)
            r6.<init>(r7, r8)
            goto L_0x0011
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.HeaderTokenizer.getNext(char, boolean):javax.mail.internet.HeaderTokenizer$Token");
    }

    private Token collectString(char eos, boolean keepEscapes) throws ParseException {
        String s;
        String s2;
        boolean filter = false;
        int start = this.currentPos;
        while (this.currentPos < this.maxPos) {
            char c = this.string.charAt(this.currentPos);
            if (c == '\\') {
                this.currentPos++;
                filter = true;
            } else if (c == 13) {
                filter = true;
            } else if (c == eos) {
                this.currentPos++;
                if (filter) {
                    s2 = filterToken(this.string, start, this.currentPos - 1, keepEscapes);
                } else {
                    s2 = this.string.substring(start, this.currentPos - 1);
                }
                if (c != '\"') {
                    s2 = trimWhiteSpace(s2);
                    this.currentPos--;
                }
                return new Token(-2, s2);
            }
            this.currentPos++;
        }
        if (eos == '\"') {
            throw new ParseException("Unbalanced quoted string");
        }
        if (filter) {
            s = filterToken(this.string, start, this.currentPos, keepEscapes);
        } else {
            s = this.string.substring(start, this.currentPos);
        }
        return new Token(-2, trimWhiteSpace(s));
    }

    private int skipWhiteSpace() {
        while (this.currentPos < this.maxPos) {
            char c = this.string.charAt(this.currentPos);
            if (c != ' ' && c != 9 && c != 13 && c != 10) {
                return this.currentPos;
            }
            this.currentPos++;
        }
        return -4;
    }

    private static String trimWhiteSpace(String s) {
        int i = s.length() - 1;
        while (i >= 0) {
            char c = s.charAt(i);
            if (c != ' ' && c != 9 && c != 13 && c != 10) {
                break;
            }
            i--;
        }
        if (i <= 0) {
            return "";
        }
        return s.substring(0, i + 1);
    }

    private static String filterToken(String s, int start, int end, boolean keepEscapes) {
        StringBuilder sb = new StringBuilder();
        boolean gotEscape = false;
        boolean gotCR = false;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c != 10 || !gotCR) {
                gotCR = false;
                if (gotEscape) {
                    if (keepEscapes) {
                        sb.append('\\');
                    }
                    sb.append(c);
                    gotEscape = false;
                } else if (c == '\\') {
                    gotEscape = true;
                } else if (c == 13) {
                    gotCR = true;
                } else {
                    sb.append(c);
                }
            } else {
                gotCR = false;
            }
        }
        return sb.toString();
    }
}
