package javax.mail.internet;

import com.sun.mail.util.PropUtil;
import gnu.kawa.functions.GetNamedPart;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.Session;

public class InternetAddress extends Address implements Cloneable {
    private static final boolean allowUtf8 = PropUtil.getBooleanSystemProperty("mail.mime.allowutf8", false);
    private static final boolean ignoreBogusGroupName = PropUtil.getBooleanSystemProperty("mail.mime.address.ignorebogusgroupname", true);
    private static final String rfc822phrase = HeaderTokenizer.RFC822.replace(' ', 0).replace(9, 0);
    private static final long serialVersionUID = -7507595530758302903L;
    private static final String specialsNoDot = "()<>,;:\\\"[]@";
    private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";
    private static final boolean useCanonicalHostName = PropUtil.getBooleanSystemProperty("mail.mime.address.usecanonicalhostname", true);
    protected String address;
    protected String encodedPersonal;
    protected String personal;

    public InternetAddress() {
    }

    public InternetAddress(String address2) throws AddressException {
        InternetAddress[] a = parse(address2, true);
        if (a.length != 1) {
            throw new AddressException("Illegal address", address2);
        }
        this.address = a[0].address;
        this.personal = a[0].personal;
        this.encodedPersonal = a[0].encodedPersonal;
    }

    public InternetAddress(String address2, boolean strict) throws AddressException {
        this(address2);
        if (!strict) {
            return;
        }
        if (isGroup()) {
            getGroup(true);
        } else {
            checkAddress(this.address, true, true);
        }
    }

    public InternetAddress(String address2, String personal2) throws UnsupportedEncodingException {
        this(address2, personal2, (String) null);
    }

    public InternetAddress(String address2, String personal2, String charset) throws UnsupportedEncodingException {
        this.address = address2;
        setPersonal(personal2, charset);
    }

    public Object clone() {
        try {
            return (InternetAddress) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getType() {
        return "rfc822";
    }

    public void setAddress(String address2) {
        this.address = address2;
    }

    public void setPersonal(String name, String charset) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name, charset, (String) null);
        } else {
            this.encodedPersonal = null;
        }
    }

    public void setPersonal(String name) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name);
        } else {
            this.encodedPersonal = null;
        }
    }

    public String getAddress() {
        return this.address;
    }

    public String getPersonal() {
        if (this.personal != null) {
            return this.personal;
        }
        if (this.encodedPersonal == null) {
            return null;
        }
        try {
            this.personal = MimeUtility.decodeText(this.encodedPersonal);
            return this.personal;
        } catch (Exception e) {
            return this.encodedPersonal;
        }
    }

    public String toString() {
        String a = this.address == null ? "" : this.address;
        if (this.encodedPersonal == null && this.personal != null) {
            try {
                this.encodedPersonal = MimeUtility.encodeWord(this.personal);
            } catch (UnsupportedEncodingException e) {
            }
        }
        if (this.encodedPersonal != null) {
            return quotePhrase(this.encodedPersonal) + " <" + a + ">";
        }
        if (isGroup() || isSimple()) {
            return a;
        }
        return "<" + a + ">";
    }

    public String toUnicodeString() {
        String p = getPersonal();
        if (p != null) {
            return quotePhrase(p) + " <" + this.address + ">";
        }
        if (isGroup() || isSimple()) {
            return this.address;
        }
        return "<" + this.address + ">";
    }

    private static String quotePhrase(String phrase) {
        int len = phrase.length();
        boolean needQuoting = false;
        for (int i = 0; i < len; i++) {
            char c = phrase.charAt(i);
            if (c == '\"' || c == '\\') {
                StringBuilder sb = new StringBuilder(len + 3);
                sb.append('\"');
                for (int j = 0; j < len; j++) {
                    char cc = phrase.charAt(j);
                    if (cc == '\"' || cc == '\\') {
                        sb.append('\\');
                    }
                    sb.append(cc);
                }
                sb.append('\"');
                return sb.toString();
            }
            if ((c < ' ' && c != 13 && c != 10 && c != 9) || ((c >= 127 && !allowUtf8) || rfc822phrase.indexOf(c) >= 0)) {
                needQuoting = true;
            }
        }
        if (!needQuoting) {
            return phrase;
        }
        StringBuilder sb2 = new StringBuilder(len + 2);
        sb2.append('\"').append(phrase).append('\"');
        return sb2.toString();
    }

    private static String unquote(String s) {
        if (!s.startsWith("\"") || !s.endsWith("\"") || s.length() <= 1) {
            return s;
        }
        String s2 = s.substring(1, s.length() - 1);
        if (s2.indexOf(92) < 0) {
            return s2;
        }
        StringBuilder sb = new StringBuilder(s2.length());
        int i = 0;
        while (i < s2.length()) {
            char c = s2.charAt(i);
            if (c == '\\' && i < s2.length() - 1) {
                i++;
                c = s2.charAt(i);
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    public boolean equals(Object a) {
        if (!(a instanceof InternetAddress)) {
            return false;
        }
        String s = ((InternetAddress) a).getAddress();
        if (s == this.address) {
            return true;
        }
        if (this.address == null || !this.address.equalsIgnoreCase(s)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        if (this.address == null) {
            return 0;
        }
        return this.address.toLowerCase(Locale.ENGLISH).hashCode();
    }

    public static String toString(Address[] addresses) {
        return toString(addresses, 0);
    }

    public static String toUnicodeString(Address[] addresses) {
        return toUnicodeString(addresses, 0);
    }

    public static String toString(Address[] addresses, int used) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addresses.length; i++) {
            if (i != 0) {
                sb.append(", ");
                used += 2;
            }
            String s = MimeUtility.fold(0, addresses[i].toString());
            if (used + lengthOfFirstSegment(s) > 76) {
                int curlen = sb.length();
                if (curlen > 0 && sb.charAt(curlen - 1) == ' ') {
                    sb.setLength(curlen - 1);
                }
                sb.append("\r\n\t");
                used = 8;
            }
            sb.append(s);
            used = lengthOfLastSegment(s, used);
        }
        return sb.toString();
    }

    public static String toUnicodeString(Address[] addresses, int used) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean sawNonAscii = false;
        for (int i = 0; i < addresses.length; i++) {
            if (i != 0) {
                sb.append(", ");
                used += 2;
            }
            String as = addresses[i].toUnicodeString();
            if (MimeUtility.checkAscii(as) != 1) {
                sawNonAscii = true;
                as = new String(as.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
            String s = MimeUtility.fold(0, as);
            if (used + lengthOfFirstSegment(s) > 76) {
                int curlen = sb.length();
                if (curlen > 0 && sb.charAt(curlen - 1) == ' ') {
                    sb.setLength(curlen - 1);
                }
                sb.append("\r\n\t");
                used = 8;
            }
            sb.append(s);
            used = lengthOfLastSegment(s, used);
        }
        String ret = sb.toString();
        if (sawNonAscii) {
            return new String(ret.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }
        return ret;
    }

    private static int lengthOfFirstSegment(String s) {
        int pos = s.indexOf("\r\n");
        return pos != -1 ? pos : s.length();
    }

    private static int lengthOfLastSegment(String s, int used) {
        int pos = s.lastIndexOf("\r\n");
        if (pos != -1) {
            return (s.length() - pos) - 2;
        }
        return s.length() + used;
    }

    public static InternetAddress getLocalAddress(Session session) {
        try {
            return _getLocalAddress(session);
        } catch (SecurityException | UnknownHostException | AddressException e) {
            return null;
        }
    }

    static InternetAddress _getLocalAddress(Session session) throws SecurityException, AddressException, UnknownHostException {
        String user = null;
        String host = null;
        String address2 = null;
        if (session == null) {
            user = System.getProperty("user.name");
            host = getLocalHostName();
        } else {
            address2 = session.getProperty("mail.from");
            if (address2 == null) {
                user = session.getProperty("mail.user");
                if (user == null || user.length() == 0) {
                    user = session.getProperty("user.name");
                }
                if (user == null || user.length() == 0) {
                    user = System.getProperty("user.name");
                }
                host = session.getProperty("mail.host");
                if (host == null || host.length() == 0) {
                    host = getLocalHostName();
                }
            }
        }
        if (!(address2 != null || user == null || user.length() == 0 || host == null || host.length() == 0)) {
            address2 = MimeUtility.quote(user.trim(), "()<>,;:\\\"[]@\t ") + GetNamedPart.CAST_METHOD_NAME + host;
        }
        if (address2 == null) {
            return null;
        }
        return new InternetAddress(address2);
    }

    private static String getLocalHostName() throws UnknownHostException {
        String host = null;
        InetAddress me = InetAddress.getLocalHost();
        if (me == null) {
            return null;
        }
        if (useCanonicalHostName) {
            host = me.getCanonicalHostName();
        }
        if (host == null) {
            host = me.getHostName();
        }
        if (host == null) {
            host = me.getHostAddress();
        }
        if (host == null || host.length() <= 0 || !isInetAddressLiteral(host)) {
            return host;
        }
        return '[' + host + ']';
    }

    private static boolean isInetAddressLiteral(String addr) {
        boolean sawHex = false;
        boolean sawColon = false;
        for (int i = 0; i < addr.length(); i++) {
            char c = addr.charAt(i);
            if ((c < '0' || c > '9') && c != '.') {
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    sawHex = true;
                } else if (c != ':') {
                    return false;
                } else {
                    sawColon = true;
                }
            }
        }
        if (!sawHex || sawColon) {
            return true;
        }
        return false;
    }

    public static InternetAddress[] parse(String addresslist) throws AddressException {
        return parse(addresslist, true);
    }

    public static InternetAddress[] parse(String addresslist, boolean strict) throws AddressException {
        return parse(addresslist, strict, false);
    }

    public static InternetAddress[] parseHeader(String addresslist, boolean strict) throws AddressException {
        return parse(MimeUtility.unfold(addresslist), strict, true);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x01c0, code lost:
        r13 = r13 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x01f7, code lost:
        r13 = r13 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x011f, code lost:
        r13 = r13 + 1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x002f A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static javax.mail.internet.InternetAddress[] parse(java.lang.String r33, boolean r34, boolean r35) throws javax.mail.internet.AddressException {
        /*
            r28 = -1
            r8 = -1
            int r15 = r33.length()
            if (r35 == 0) goto L_0x0032
            if (r34 != 0) goto L_0x0032
            r11 = 1
        L_0x000c:
            r12 = 0
            r25 = 0
            r23 = 0
            java.util.ArrayList r30 = new java.util.ArrayList
            r30.<init>()
            r7 = -1
            r27 = r7
            r13 = 0
        L_0x001a:
            if (r13 >= r15) goto L_0x0417
            r0 = r33
            char r6 = r0.charAt(r13)
            switch(r6) {
                case 9: goto L_0x002f;
                case 10: goto L_0x002f;
                case 13: goto L_0x002f;
                case 32: goto L_0x002f;
                case 34: goto L_0x01a5;
                case 40: goto L_0x0034;
                case 41: goto L_0x0087;
                case 44: goto L_0x0280;
                case 58: goto L_0x035c;
                case 59: goto L_0x0213;
                case 60: goto L_0x00a2;
                case 62: goto L_0x0189;
                case 91: goto L_0x01dc;
                default: goto L_0x0025;
            }
        L_0x0025:
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x002f
            r27 = r13
        L_0x002f:
            int r13 = r13 + 1
            goto L_0x001a
        L_0x0032:
            r11 = 0
            goto L_0x000c
        L_0x0034:
            r23 = 1
            if (r27 < 0) goto L_0x003f
            r31 = -1
            r0 = r31
            if (r7 != r0) goto L_0x003f
            r7 = r13
        L_0x003f:
            r21 = r13
            int r13 = r13 + 1
            r19 = 1
        L_0x0045:
            if (r13 >= r15) goto L_0x005e
            if (r19 <= 0) goto L_0x005e
            r0 = r33
            char r6 = r0.charAt(r13)
            switch(r6) {
                case 40: goto L_0x0058;
                case 41: goto L_0x005b;
                case 92: goto L_0x0055;
                default: goto L_0x0052;
            }
        L_0x0052:
            int r13 = r13 + 1
            goto L_0x0045
        L_0x0055:
            int r13 = r13 + 1
            goto L_0x0052
        L_0x0058:
            int r19 = r19 + 1
            goto L_0x0052
        L_0x005b:
            int r19 = r19 + -1
            goto L_0x0052
        L_0x005e:
            if (r19 <= 0) goto L_0x0073
            if (r11 != 0) goto L_0x0070
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Missing ')'"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x0070:
            int r13 = r21 + 1
            goto L_0x002f
        L_0x0073:
            int r13 = r13 + -1
            r31 = -1
            r0 = r28
            r1 = r31
            if (r0 != r1) goto L_0x007f
            int r28 = r21 + 1
        L_0x007f:
            r31 = -1
            r0 = r31
            if (r8 != r0) goto L_0x002f
            r8 = r13
            goto L_0x002f
        L_0x0087:
            if (r11 != 0) goto L_0x0097
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Missing '('"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x0097:
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x002f
            r27 = r13
            goto L_0x002f
        L_0x00a2:
            r23 = 1
            if (r25 == 0) goto L_0x010f
            if (r11 != 0) goto L_0x00b6
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Extra route-addr"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x00b6:
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x00c7
            r25 = 0
            r23 = 0
            r7 = -1
            r27 = r7
            goto L_0x002f
        L_0x00c7:
            if (r12 != 0) goto L_0x010f
            r31 = -1
            r0 = r31
            if (r7 != r0) goto L_0x00d0
            r7 = r13
        L_0x00d0:
            r0 = r33
            r1 = r27
            java.lang.String r31 = r0.substring(r1, r7)
            java.lang.String r4 = r31.trim()
            javax.mail.internet.InternetAddress r17 = new javax.mail.internet.InternetAddress
            r17.<init>()
            r0 = r17
            r0.setAddress(r4)
            if (r28 < 0) goto L_0x00fe
            r0 = r33
            r1 = r28
            java.lang.String r31 = r0.substring(r1, r8)
            java.lang.String r31 = r31.trim()
            java.lang.String r31 = unquote(r31)
            r0 = r31
            r1 = r17
            r1.encodedPersonal = r0
        L_0x00fe:
            r0 = r30
            r1 = r17
            r0.add(r1)
            r25 = 0
            r23 = 0
            r7 = -1
            r27 = r7
            r8 = -1
            r28 = r8
        L_0x010f:
            r24 = r13
            r14 = 0
            int r13 = r13 + 1
        L_0x0114:
            if (r13 >= r15) goto L_0x012d
            r0 = r33
            char r6 = r0.charAt(r13)
            switch(r6) {
                case 34: goto L_0x0125;
                case 62: goto L_0x012b;
                case 92: goto L_0x0122;
                default: goto L_0x011f;
            }
        L_0x011f:
            int r13 = r13 + 1
            goto L_0x0114
        L_0x0122:
            int r13 = r13 + 1
            goto L_0x011f
        L_0x0125:
            if (r14 != 0) goto L_0x0129
            r14 = 1
        L_0x0128:
            goto L_0x011f
        L_0x0129:
            r14 = 0
            goto L_0x0128
        L_0x012b:
            if (r14 != 0) goto L_0x011f
        L_0x012d:
            if (r14 == 0) goto L_0x015a
            if (r11 != 0) goto L_0x013f
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Missing '\"'"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x013f:
            int r13 = r24 + 1
        L_0x0141:
            if (r13 >= r15) goto L_0x015a
            r0 = r33
            char r6 = r0.charAt(r13)
            r31 = 92
            r0 = r31
            if (r6 != r0) goto L_0x0154
            int r13 = r13 + 1
        L_0x0151:
            int r13 = r13 + 1
            goto L_0x0141
        L_0x0154:
            r31 = 62
            r0 = r31
            if (r6 != r0) goto L_0x0151
        L_0x015a:
            if (r13 < r15) goto L_0x017a
            if (r11 != 0) goto L_0x016c
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Missing '>'"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x016c:
            int r13 = r24 + 1
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x002f
            r27 = r24
            goto L_0x002f
        L_0x017a:
            if (r12 != 0) goto L_0x0184
            if (r27 < 0) goto L_0x0182
            r28 = r27
            r8 = r24
        L_0x0182:
            int r27 = r24 + 1
        L_0x0184:
            r25 = 1
            r7 = r13
            goto L_0x002f
        L_0x0189:
            if (r11 != 0) goto L_0x0199
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Missing '<'"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x0199:
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x002f
            r27 = r13
            goto L_0x002f
        L_0x01a5:
            r22 = r13
            r23 = 1
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x01b3
            r27 = r13
        L_0x01b3:
            int r13 = r13 + 1
        L_0x01b5:
            if (r13 >= r15) goto L_0x01c6
            r0 = r33
            char r6 = r0.charAt(r13)
            switch(r6) {
                case 34: goto L_0x01c6;
                case 92: goto L_0x01c3;
                default: goto L_0x01c0;
            }
        L_0x01c0:
            int r13 = r13 + 1
            goto L_0x01b5
        L_0x01c3:
            int r13 = r13 + 1
            goto L_0x01c0
        L_0x01c6:
            if (r13 < r15) goto L_0x002f
            if (r11 != 0) goto L_0x01d8
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Missing '\"'"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x01d8:
            int r13 = r22 + 1
            goto L_0x002f
        L_0x01dc:
            r16 = r13
            r23 = 1
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x01ea
            r27 = r13
        L_0x01ea:
            int r13 = r13 + 1
        L_0x01ec:
            if (r13 >= r15) goto L_0x01fd
            r0 = r33
            char r6 = r0.charAt(r13)
            switch(r6) {
                case 92: goto L_0x01fa;
                case 93: goto L_0x01fd;
                default: goto L_0x01f7;
            }
        L_0x01f7:
            int r13 = r13 + 1
            goto L_0x01ec
        L_0x01fa:
            int r13 = r13 + 1
            goto L_0x01f7
        L_0x01fd:
            if (r13 < r15) goto L_0x002f
            if (r11 != 0) goto L_0x020f
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Missing ']'"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x020f:
            int r13 = r16 + 1
            goto L_0x002f
        L_0x0213:
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x0224
            r25 = 0
            r23 = 0
            r7 = -1
            r27 = r7
            goto L_0x002f
        L_0x0224:
            if (r12 == 0) goto L_0x0270
            r12 = 0
            if (r35 == 0) goto L_0x0243
            if (r34 != 0) goto L_0x0243
            int r31 = r13 + 1
            r0 = r31
            if (r0 >= r15) goto L_0x0243
            int r31 = r13 + 1
            r0 = r33
            r1 = r31
            char r31 = r0.charAt(r1)
            r32 = 64
            r0 = r31
            r1 = r32
            if (r0 == r1) goto L_0x002f
        L_0x0243:
            javax.mail.internet.InternetAddress r17 = new javax.mail.internet.InternetAddress
            r17.<init>()
            int r7 = r13 + 1
            r0 = r33
            r1 = r27
            java.lang.String r31 = r0.substring(r1, r7)
            java.lang.String r31 = r31.trim()
            r0 = r17
            r1 = r31
            r0.setAddress(r1)
            r0 = r30
            r1 = r17
            r0.add(r1)
            r25 = 0
            r23 = 0
            r7 = -1
            r27 = r7
            r8 = -1
            r28 = r8
            goto L_0x002f
        L_0x0270:
            if (r11 != 0) goto L_0x0280
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Illegal semicolon, not in group"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x0280:
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x0291
            r25 = 0
            r23 = 0
            r7 = -1
            r27 = r7
            goto L_0x002f
        L_0x0291:
            if (r12 == 0) goto L_0x0297
            r25 = 0
            goto L_0x002f
        L_0x0297:
            r31 = -1
            r0 = r31
            if (r7 != r0) goto L_0x029e
            r7 = r13
        L_0x029e:
            r0 = r33
            r1 = r27
            java.lang.String r31 = r0.substring(r1, r7)
            java.lang.String r4 = r31.trim()
            r20 = 0
            if (r23 == 0) goto L_0x02cc
            if (r28 < 0) goto L_0x02cc
            r0 = r33
            r1 = r28
            java.lang.String r31 = r0.substring(r1, r8)
            java.lang.String r31 = r31.trim()
            java.lang.String r20 = unquote(r31)
            java.lang.String r31 = r20.trim()
            int r31 = r31.length()
            if (r31 != 0) goto L_0x02cc
            r20 = 0
        L_0x02cc:
            if (r35 == 0) goto L_0x02f8
            if (r34 != 0) goto L_0x02f8
            if (r20 == 0) goto L_0x02f8
            r31 = 64
            r0 = r20
            r1 = r31
            int r31 = r0.indexOf(r1)
            if (r31 < 0) goto L_0x02f8
            r31 = 64
            r0 = r31
            int r31 = r4.indexOf(r0)
            if (r31 >= 0) goto L_0x02f8
            r31 = 33
            r0 = r31
            int r31 = r4.indexOf(r0)
            if (r31 >= 0) goto L_0x02f8
            r29 = r4
            r4 = r20
            r20 = r29
        L_0x02f8:
            if (r23 != 0) goto L_0x02fe
            if (r34 != 0) goto L_0x02fe
            if (r35 == 0) goto L_0x032e
        L_0x02fe:
            if (r11 != 0) goto L_0x0309
            r31 = 0
            r0 = r25
            r1 = r31
            checkAddress(r4, r0, r1)
        L_0x0309:
            javax.mail.internet.InternetAddress r17 = new javax.mail.internet.InternetAddress
            r17.<init>()
            r0 = r17
            r0.setAddress(r4)
            if (r20 == 0) goto L_0x031b
            r0 = r20
            r1 = r17
            r1.encodedPersonal = r0
        L_0x031b:
            r0 = r30
            r1 = r17
            r0.add(r1)
        L_0x0322:
            r25 = 0
            r23 = 0
            r7 = -1
            r27 = r7
            r8 = -1
            r28 = r8
            goto L_0x002f
        L_0x032e:
            java.util.StringTokenizer r26 = new java.util.StringTokenizer
            r0 = r26
            r0.<init>(r4)
        L_0x0335:
            boolean r31 = r26.hasMoreTokens()
            if (r31 == 0) goto L_0x0322
            java.lang.String r3 = r26.nextToken()
            r31 = 0
            r32 = 0
            r0 = r31
            r1 = r32
            checkAddress(r3, r0, r1)
            javax.mail.internet.InternetAddress r17 = new javax.mail.internet.InternetAddress
            r17.<init>()
            r0 = r17
            r0.setAddress(r3)
            r0 = r30
            r1 = r17
            r0.add(r1)
            goto L_0x0335
        L_0x035c:
            r23 = 1
            if (r12 == 0) goto L_0x0370
            if (r11 != 0) goto L_0x0370
            javax.mail.internet.AddressException r31 = new javax.mail.internet.AddressException
            java.lang.String r32 = "Nested group"
            r0 = r31
            r1 = r32
            r2 = r33
            r0.<init>(r1, r2, r13)
            throw r31
        L_0x0370:
            r31 = -1
            r0 = r27
            r1 = r31
            if (r0 != r1) goto L_0x037a
            r27 = r13
        L_0x037a:
            if (r35 == 0) goto L_0x0414
            if (r34 != 0) goto L_0x0414
            int r31 = r13 + 1
            r0 = r31
            if (r0 >= r15) goto L_0x03ba
            java.lang.String r5 = ")>[]:@\\,."
            int r31 = r13 + 1
            r0 = r33
            r1 = r31
            char r18 = r0.charAt(r1)
            r0 = r18
            int r31 = r5.indexOf(r0)
            if (r31 < 0) goto L_0x03ba
            r31 = 64
            r0 = r18
            r1 = r31
            if (r0 != r1) goto L_0x002f
            int r10 = r13 + 2
        L_0x03a2:
            if (r10 >= r15) goto L_0x03b2
            r0 = r33
            char r18 = r0.charAt(r10)
            r31 = 59
            r0 = r18
            r1 = r31
            if (r0 != r1) goto L_0x0406
        L_0x03b2:
            r31 = 59
            r0 = r18
            r1 = r31
            if (r0 == r1) goto L_0x002f
        L_0x03ba:
            r0 = r33
            r1 = r27
            java.lang.String r9 = r0.substring(r1, r13)
            boolean r31 = ignoreBogusGroupName
            if (r31 == 0) goto L_0x0411
            java.lang.String r31 = "mailto"
            r0 = r31
            boolean r31 = r9.equalsIgnoreCase(r0)
            if (r31 != 0) goto L_0x0402
            java.lang.String r31 = "From"
            r0 = r31
            boolean r31 = r9.equalsIgnoreCase(r0)
            if (r31 != 0) goto L_0x0402
            java.lang.String r31 = "To"
            r0 = r31
            boolean r31 = r9.equalsIgnoreCase(r0)
            if (r31 != 0) goto L_0x0402
            java.lang.String r31 = "Cc"
            r0 = r31
            boolean r31 = r9.equalsIgnoreCase(r0)
            if (r31 != 0) goto L_0x0402
            java.lang.String r31 = "Subject"
            r0 = r31
            boolean r31 = r9.equalsIgnoreCase(r0)
            if (r31 != 0) goto L_0x0402
            java.lang.String r31 = "Re"
            r0 = r31
            boolean r31 = r9.equalsIgnoreCase(r0)
            if (r31 == 0) goto L_0x0411
        L_0x0402:
            r27 = -1
            goto L_0x002f
        L_0x0406:
            r0 = r18
            int r31 = r5.indexOf(r0)
            if (r31 >= 0) goto L_0x03b2
            int r10 = r10 + 1
            goto L_0x03a2
        L_0x0411:
            r12 = 1
            goto L_0x002f
        L_0x0414:
            r12 = 1
            goto L_0x002f
        L_0x0417:
            if (r27 < 0) goto L_0x04a4
            r31 = -1
            r0 = r31
            if (r7 != r0) goto L_0x0420
            r7 = r15
        L_0x0420:
            r0 = r33
            r1 = r27
            java.lang.String r31 = r0.substring(r1, r7)
            java.lang.String r4 = r31.trim()
            r20 = 0
            if (r23 == 0) goto L_0x044e
            if (r28 < 0) goto L_0x044e
            r0 = r33
            r1 = r28
            java.lang.String r31 = r0.substring(r1, r8)
            java.lang.String r31 = r31.trim()
            java.lang.String r20 = unquote(r31)
            java.lang.String r31 = r20.trim()
            int r31 = r31.length()
            if (r31 != 0) goto L_0x044e
            r20 = 0
        L_0x044e:
            if (r35 == 0) goto L_0x047a
            if (r34 != 0) goto L_0x047a
            if (r20 == 0) goto L_0x047a
            r31 = 64
            r0 = r20
            r1 = r31
            int r31 = r0.indexOf(r1)
            if (r31 < 0) goto L_0x047a
            r31 = 64
            r0 = r31
            int r31 = r4.indexOf(r0)
            if (r31 >= 0) goto L_0x047a
            r31 = 33
            r0 = r31
            int r31 = r4.indexOf(r0)
            if (r31 >= 0) goto L_0x047a
            r29 = r4
            r4 = r20
            r20 = r29
        L_0x047a:
            if (r23 != 0) goto L_0x0480
            if (r34 != 0) goto L_0x0480
            if (r35 == 0) goto L_0x04b2
        L_0x0480:
            if (r11 != 0) goto L_0x048b
            r31 = 0
            r0 = r25
            r1 = r31
            checkAddress(r4, r0, r1)
        L_0x048b:
            javax.mail.internet.InternetAddress r17 = new javax.mail.internet.InternetAddress
            r17.<init>()
            r0 = r17
            r0.setAddress(r4)
            if (r20 == 0) goto L_0x049d
            r0 = r20
            r1 = r17
            r1.encodedPersonal = r0
        L_0x049d:
            r0 = r30
            r1 = r17
            r0.add(r1)
        L_0x04a4:
            int r31 = r30.size()
            r0 = r31
            javax.mail.internet.InternetAddress[] r3 = new javax.mail.internet.InternetAddress[r0]
            r0 = r30
            r0.toArray(r3)
            return r3
        L_0x04b2:
            java.util.StringTokenizer r26 = new java.util.StringTokenizer
            r0 = r26
            r0.<init>(r4)
        L_0x04b9:
            boolean r31 = r26.hasMoreTokens()
            if (r31 == 0) goto L_0x04a4
            java.lang.String r3 = r26.nextToken()
            r31 = 0
            r32 = 0
            r0 = r31
            r1 = r32
            checkAddress(r3, r0, r1)
            javax.mail.internet.InternetAddress r17 = new javax.mail.internet.InternetAddress
            r17.<init>()
            r0 = r17
            r0.setAddress(r3)
            r0 = r30
            r1 = r17
            r0.add(r1)
            goto L_0x04b9
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.InternetAddress.parse(java.lang.String, boolean, boolean):javax.mail.internet.InternetAddress[]");
    }

    public void validate() throws AddressException {
        if (isGroup()) {
            getGroup(true);
        } else {
            checkAddress(getAddress(), true, true);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0115, code lost:
        throw new javax.mail.internet.AddressException("Local address contains control or whitespace", r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void checkAddress(java.lang.String r9, boolean r10, boolean r11) throws javax.mail.internet.AddressException {
        /*
            r6 = 0
            if (r9 != 0) goto L_0x000b
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Address is null"
            r7.<init>(r8)
            throw r7
        L_0x000b:
            int r5 = r9.length()
            if (r5 != 0) goto L_0x0019
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Empty address"
            r7.<init>(r8, r9)
            throw r7
        L_0x0019:
            if (r10 == 0) goto L_0x0047
            r7 = 0
            char r7 = r9.charAt(r7)
            r8 = 64
            if (r7 != r8) goto L_0x0047
            r6 = 0
        L_0x0025:
            java.lang.String r7 = ",:"
            int r1 = indexOfAny(r9, r7, r6)
            if (r1 < 0) goto L_0x0047
            char r7 = r9.charAt(r6)
            r8 = 64
            if (r7 == r8) goto L_0x003d
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Illegal route-addr"
            r7.<init>(r8, r9)
            throw r7
        L_0x003d:
            char r7 = r9.charAt(r1)
            r8 = 58
            if (r7 != r8) goto L_0x0061
            int r6 = r1 + 1
        L_0x0047:
            r0 = 65535(0xffff, float:9.1834E-41)
            r4 = 65535(0xffff, float:9.1834E-41)
            r3 = 0
            r1 = r6
        L_0x004f:
            if (r1 >= r5) goto L_0x0126
            r4 = r0
            char r0 = r9.charAt(r1)
            r7 = 92
            if (r0 == r7) goto L_0x005e
            r7 = 92
            if (r4 != r7) goto L_0x0064
        L_0x005e:
            int r1 = r1 + 1
            goto L_0x004f
        L_0x0061:
            int r6 = r1 + 1
            goto L_0x0025
        L_0x0064:
            r7 = 34
            if (r0 != r7) goto L_0x0092
            if (r3 == 0) goto L_0x0084
            if (r11 == 0) goto L_0x0082
            int r7 = r1 + 1
            if (r7 >= r5) goto L_0x0082
            int r7 = r1 + 1
            char r7 = r9.charAt(r7)
            r8 = 64
            if (r7 == r8) goto L_0x0082
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Quote not at end of local address"
            r7.<init>(r8, r9)
            throw r7
        L_0x0082:
            r3 = 0
            goto L_0x005e
        L_0x0084:
            if (r11 == 0) goto L_0x0090
            if (r1 == 0) goto L_0x0090
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Quote not at start of local address"
            r7.<init>(r8, r9)
            throw r7
        L_0x0090:
            r3 = 1
            goto L_0x005e
        L_0x0092:
            r7 = 13
            if (r0 != r7) goto L_0x00ac
            int r7 = r1 + 1
            if (r7 >= r5) goto L_0x00d0
            int r7 = r1 + 1
            char r7 = r9.charAt(r7)
            r8 = 10
            if (r7 == r8) goto L_0x00d0
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Quoted local address contains CR without LF"
            r7.<init>(r8, r9)
            throw r7
        L_0x00ac:
            r7 = 10
            if (r0 != r7) goto L_0x00d0
            int r7 = r1 + 1
            if (r7 >= r5) goto L_0x00d0
            int r7 = r1 + 1
            char r7 = r9.charAt(r7)
            r8 = 32
            if (r7 == r8) goto L_0x00d0
            int r7 = r1 + 1
            char r7 = r9.charAt(r7)
            r8 = 9
            if (r7 == r8) goto L_0x00d0
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Quoted local address contains newline without whitespace"
            r7.<init>(r8, r9)
            throw r7
        L_0x00d0:
            if (r3 != 0) goto L_0x005e
            r7 = 46
            if (r0 != r7) goto L_0x00ec
            if (r1 != r6) goto L_0x00e0
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Local address starts with dot"
            r7.<init>(r8, r9)
            throw r7
        L_0x00e0:
            r7 = 46
            if (r4 != r7) goto L_0x00ec
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Local address contains dot-dot"
            r7.<init>(r8, r9)
            throw r7
        L_0x00ec:
            r7 = 64
            if (r0 != r7) goto L_0x0106
            if (r1 != 0) goto L_0x00fa
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Missing local name"
            r7.<init>(r8, r9)
            throw r7
        L_0x00fa:
            r7 = 46
            if (r4 != r7) goto L_0x0126
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Local address ends with dot"
            r7.<init>(r8, r9)
            throw r7
        L_0x0106:
            r7 = 32
            if (r0 <= r7) goto L_0x010e
            r7 = 127(0x7f, float:1.78E-43)
            if (r0 != r7) goto L_0x0116
        L_0x010e:
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Local address contains control or whitespace"
            r7.<init>(r8, r9)
            throw r7
        L_0x0116:
            java.lang.String r7 = "()<>,;:\\\"[]@"
            int r7 = r7.indexOf(r0)
            if (r7 < 0) goto L_0x005e
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Local address contains illegal character"
            r7.<init>(r8, r9)
            throw r7
        L_0x0126:
            if (r3 == 0) goto L_0x0130
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Unterminated quote"
            r7.<init>(r8, r9)
            throw r7
        L_0x0130:
            r7 = 64
            if (r0 == r7) goto L_0x013e
            if (r11 == 0) goto L_0x01cb
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Missing final '@domain'"
            r7.<init>(r8, r9)
            throw r7
        L_0x013e:
            int r6 = r1 + 1
            if (r6 < r5) goto L_0x014a
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Missing domain"
            r7.<init>(r8, r9)
            throw r7
        L_0x014a:
            char r7 = r9.charAt(r6)
            r8 = 46
            if (r7 != r8) goto L_0x015a
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Domain starts with dot"
            r7.<init>(r8, r9)
            throw r7
        L_0x015a:
            r2 = 0
            r1 = r6
        L_0x015c:
            if (r1 >= r5) goto L_0x01bf
            char r0 = r9.charAt(r1)
            r7 = 91
            if (r0 != r7) goto L_0x0175
            if (r1 == r6) goto L_0x0170
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Domain literal not at start of domain"
            r7.<init>(r8, r9)
            throw r7
        L_0x0170:
            r2 = 1
        L_0x0171:
            r4 = r0
            int r1 = r1 + 1
            goto L_0x015c
        L_0x0175:
            r7 = 93
            if (r0 != r7) goto L_0x0187
            int r7 = r5 + -1
            if (r1 == r7) goto L_0x0185
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Domain literal end not at end of domain"
            r7.<init>(r8, r9)
            throw r7
        L_0x0185:
            r2 = 0
            goto L_0x0171
        L_0x0187:
            r7 = 32
            if (r0 <= r7) goto L_0x018f
            r7 = 127(0x7f, float:1.78E-43)
            if (r0 != r7) goto L_0x0197
        L_0x018f:
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Domain contains control or whitespace"
            r7.<init>(r8, r9)
            throw r7
        L_0x0197:
            if (r2 != 0) goto L_0x0171
            boolean r7 = java.lang.Character.isLetterOrDigit(r0)
            if (r7 != 0) goto L_0x01af
            r7 = 45
            if (r0 == r7) goto L_0x01af
            r7 = 46
            if (r0 == r7) goto L_0x01af
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Domain contains illegal character"
            r7.<init>(r8, r9)
            throw r7
        L_0x01af:
            r7 = 46
            if (r0 != r7) goto L_0x0171
            r7 = 46
            if (r4 != r7) goto L_0x0171
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Domain contains dot-dot"
            r7.<init>(r8, r9)
            throw r7
        L_0x01bf:
            r7 = 46
            if (r4 != r7) goto L_0x01cb
            javax.mail.internet.AddressException r7 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Domain ends with dot"
            r7.<init>(r8, r9)
            throw r7
        L_0x01cb:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.InternetAddress.checkAddress(java.lang.String, boolean, boolean):void");
    }

    private boolean isSimple() {
        return this.address == null || indexOfAny(this.address, specialsNoDotNoAt) < 0;
    }

    public boolean isGroup() {
        return this.address != null && this.address.endsWith(";") && this.address.indexOf(58) > 0;
    }

    public InternetAddress[] getGroup(boolean strict) throws AddressException {
        int ix;
        String addr = getAddress();
        if (addr != null && addr.endsWith(";") && (ix = addr.indexOf(58)) >= 0) {
            return parseHeader(addr.substring(ix + 1, addr.length() - 1), strict);
        }
        return null;
    }

    private static int indexOfAny(String s, String any) {
        return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
        try {
            int len = s.length();
            for (int i = start; i < len; i++) {
                if (any.indexOf(s.charAt(i)) >= 0) {
                    return i;
                }
            }
            return -1;
        } catch (StringIndexOutOfBoundsException e) {
            return -1;
        }
    }
}
