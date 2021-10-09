package javax.mail.internet;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.BEncoderStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.QDecoderStream;
import com.sun.mail.util.QEncoderStream;
import com.sun.mail.util.QPDecoderStream;
import com.sun.mail.util.QPEncoderStream;
import com.sun.mail.util.UUDecoderStream;
import com.sun.mail.util.UUEncoderStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.mail.MessagingException;

public class MimeUtility {
    static final /* synthetic */ boolean $assertionsDisabled;
    public static final int ALL = -1;
    static final int ALL_ASCII = 1;
    static final int MOSTLY_ASCII = 2;
    static final int MOSTLY_NONASCII = 3;
    private static final boolean allowUtf8 = PropUtil.getBooleanSystemProperty("mail.mime.allowutf8", $assertionsDisabled);
    private static final boolean decodeStrict = PropUtil.getBooleanSystemProperty("mail.mime.decodetext.strict", true);
    private static String defaultJavaCharset;
    private static String defaultMIMECharset;
    private static final boolean encodeEolStrict = PropUtil.getBooleanSystemProperty("mail.mime.encodeeol.strict", $assertionsDisabled);
    private static final boolean foldEncodedWords = PropUtil.getBooleanSystemProperty("mail.mime.foldencodedwords", $assertionsDisabled);
    private static final boolean foldText = PropUtil.getBooleanSystemProperty("mail.mime.foldtext", true);
    private static final boolean ignoreUnknownEncoding = PropUtil.getBooleanSystemProperty("mail.mime.ignoreunknownencoding", $assertionsDisabled);
    private static Map<String, String> java2mime = new HashMap(40);
    private static Map<String, String> mime2java = new HashMap(14);
    private static final Map<String, Boolean> nonAsciiCharsetMap = new HashMap();

    static {
        boolean z;
        if (!MimeUtility.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = false;
        }
        $assertionsDisabled = z;
        try {
            InputStream is = MimeUtility.class.getResourceAsStream("/META-INF/javamail.charset.map");
            if (is != null) {
                try {
                    LineInputStream lineInputStream = new LineInputStream(is);
                    try {
                        loadMappings(lineInputStream, java2mime);
                        loadMappings(lineInputStream, mime2java);
                        try {
                            lineInputStream.close();
                            LineInputStream lineInputStream2 = lineInputStream;
                        } catch (Exception e) {
                            LineInputStream lineInputStream3 = lineInputStream;
                        }
                    } catch (Throwable th) {
                        th = th;
                        is = lineInputStream;
                        try {
                            is.close();
                        } catch (Exception e2) {
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    is.close();
                    throw th;
                }
            }
        } catch (Exception e3) {
        }
        if (java2mime.isEmpty()) {
            java2mime.put("8859_1", "ISO-8859-1");
            java2mime.put("iso8859_1", "ISO-8859-1");
            java2mime.put("iso8859-1", "ISO-8859-1");
            java2mime.put("8859_2", "ISO-8859-2");
            java2mime.put("iso8859_2", "ISO-8859-2");
            java2mime.put("iso8859-2", "ISO-8859-2");
            java2mime.put("8859_3", "ISO-8859-3");
            java2mime.put("iso8859_3", "ISO-8859-3");
            java2mime.put("iso8859-3", "ISO-8859-3");
            java2mime.put("8859_4", "ISO-8859-4");
            java2mime.put("iso8859_4", "ISO-8859-4");
            java2mime.put("iso8859-4", "ISO-8859-4");
            java2mime.put("8859_5", "ISO-8859-5");
            java2mime.put("iso8859_5", "ISO-8859-5");
            java2mime.put("iso8859-5", "ISO-8859-5");
            java2mime.put("8859_6", "ISO-8859-6");
            java2mime.put("iso8859_6", "ISO-8859-6");
            java2mime.put("iso8859-6", "ISO-8859-6");
            java2mime.put("8859_7", "ISO-8859-7");
            java2mime.put("iso8859_7", "ISO-8859-7");
            java2mime.put("iso8859-7", "ISO-8859-7");
            java2mime.put("8859_8", "ISO-8859-8");
            java2mime.put("iso8859_8", "ISO-8859-8");
            java2mime.put("iso8859-8", "ISO-8859-8");
            java2mime.put("8859_9", "ISO-8859-9");
            java2mime.put("iso8859_9", "ISO-8859-9");
            java2mime.put("iso8859-9", "ISO-8859-9");
            java2mime.put("sjis", "Shift_JIS");
            java2mime.put("jis", "ISO-2022-JP");
            java2mime.put("iso2022jp", "ISO-2022-JP");
            java2mime.put("euc_jp", "euc-jp");
            java2mime.put("koi8_r", "koi8-r");
            java2mime.put("euc_cn", "euc-cn");
            java2mime.put("euc_tw", "euc-tw");
            java2mime.put("euc_kr", "euc-kr");
        }
        if (mime2java.isEmpty()) {
            mime2java.put("iso-2022-cn", "ISO2022CN");
            mime2java.put("iso-2022-kr", "ISO2022KR");
            mime2java.put("utf-8", "UTF8");
            mime2java.put("utf8", "UTF8");
            mime2java.put("ja_jp.iso2022-7", "ISO2022JP");
            mime2java.put("ja_jp.eucjp", "EUCJIS");
            mime2java.put("euc-kr", "KSC5601");
            mime2java.put("euckr", "KSC5601");
            mime2java.put("us-ascii", "ISO-8859-1");
            mime2java.put("x-us-ascii", "ISO-8859-1");
            mime2java.put("gb2312", "GB18030");
            mime2java.put("cp936", "GB18030");
            mime2java.put("ms936", "GB18030");
            mime2java.put("gbk", "GB18030");
        }
    }

    private MimeUtility() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0052 A[SYNTHETIC, Splitter:B:29:0x0052] */
    /* JADX WARNING: Removed duplicated region for block: B:44:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getEncoding(javax.activation.DataSource r9) {
        /*
            r0 = 0
            r5 = 0
            r2 = 0
            boolean r7 = r9 instanceof javax.mail.EncodingAware
            if (r7 == 0) goto L_0x0012
            r7 = r9
            javax.mail.EncodingAware r7 = (javax.mail.EncodingAware) r7
            java.lang.String r2 = r7.getEncoding()
            if (r2 == 0) goto L_0x0012
            r7 = r2
        L_0x0011:
            return r7
        L_0x0012:
            javax.mail.internet.ContentType r1 = new javax.mail.internet.ContentType     // Catch:{ Exception -> 0x004d }
            java.lang.String r7 = r9.getContentType()     // Catch:{ Exception -> 0x004d }
            r1.<init>(r7)     // Catch:{ Exception -> 0x004d }
            java.io.InputStream r5 = r9.getInputStream()     // Catch:{ Exception -> 0x0066, all -> 0x0063 }
            java.lang.String r7 = "text/*"
            boolean r6 = r1.match((java.lang.String) r7)     // Catch:{ Exception -> 0x0066, all -> 0x0063 }
            r8 = -1
            if (r6 != 0) goto L_0x003a
            r7 = 1
        L_0x0029:
            int r4 = checkAscii(r5, r8, r7)     // Catch:{ Exception -> 0x0066, all -> 0x0063 }
            switch(r4) {
                case 1: goto L_0x003c;
                case 2: goto L_0x003f;
                default: goto L_0x0030;
            }     // Catch:{ Exception -> 0x0066, all -> 0x0063 }
        L_0x0030:
            java.lang.String r2 = "base64"
        L_0x0032:
            if (r5 == 0) goto L_0x0037
            r5.close()     // Catch:{ IOException -> 0x005f }
        L_0x0037:
            r0 = r1
            r7 = r2
            goto L_0x0011
        L_0x003a:
            r7 = 0
            goto L_0x0029
        L_0x003c:
            java.lang.String r2 = "7bit"
            goto L_0x0032
        L_0x003f:
            if (r6 == 0) goto L_0x004a
            boolean r7 = nonAsciiCharset(r1)     // Catch:{ Exception -> 0x0066, all -> 0x0063 }
            if (r7 == 0) goto L_0x004a
            java.lang.String r2 = "base64"
            goto L_0x0032
        L_0x004a:
            java.lang.String r2 = "quoted-printable"
            goto L_0x0032
        L_0x004d:
            r3 = move-exception
        L_0x004e:
            java.lang.String r7 = "base64"
            if (r5 == 0) goto L_0x0011
            r5.close()     // Catch:{ IOException -> 0x0056 }
            goto L_0x0011
        L_0x0056:
            r8 = move-exception
            goto L_0x0011
        L_0x0058:
            r7 = move-exception
        L_0x0059:
            if (r5 == 0) goto L_0x005e
            r5.close()     // Catch:{ IOException -> 0x0061 }
        L_0x005e:
            throw r7
        L_0x005f:
            r7 = move-exception
            goto L_0x0037
        L_0x0061:
            r8 = move-exception
            goto L_0x005e
        L_0x0063:
            r7 = move-exception
            r0 = r1
            goto L_0x0059
        L_0x0066:
            r3 = move-exception
            r0 = r1
            goto L_0x004e
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeUtility.getEncoding(javax.activation.DataSource):java.lang.String");
    }

    private static boolean nonAsciiCharset(ContentType ct) {
        Boolean bool;
        boolean z = $assertionsDisabled;
        String charset = ct.getParameter("charset");
        if (charset == null) {
            return $assertionsDisabled;
        }
        String charset2 = charset.toLowerCase(Locale.ENGLISH);
        synchronized (nonAsciiCharsetMap) {
            bool = nonAsciiCharsetMap.get(charset2);
        }
        if (bool == null) {
            try {
                byte[] b = "\r\n".getBytes(charset2);
                if (!(b.length == 2 && b[0] == 13 && b[1] == 10)) {
                    z = true;
                }
                bool = Boolean.valueOf(z);
            } catch (UnsupportedEncodingException e) {
                bool = Boolean.FALSE;
            } catch (RuntimeException e2) {
                bool = Boolean.TRUE;
            }
            synchronized (nonAsciiCharsetMap) {
                nonAsciiCharsetMap.put(charset2, bool);
            }
        }
        return bool.booleanValue();
    }

    public static String getEncoding(DataHandler dh) {
        String encoding;
        if (dh.getName() != null) {
            return getEncoding(dh.getDataSource());
        }
        try {
            ContentType cType = new ContentType(dh.getContentType());
            if (cType.match("text/*")) {
                AsciiOutputStream aos = new AsciiOutputStream($assertionsDisabled, $assertionsDisabled);
                try {
                    dh.writeTo(aos);
                } catch (IOException e) {
                }
                switch (aos.getAscii()) {
                    case 1:
                        encoding = "7bit";
                        break;
                    case 2:
                        encoding = "quoted-printable";
                        break;
                    default:
                        encoding = "base64";
                        break;
                }
            } else {
                AsciiOutputStream aos2 = new AsciiOutputStream(true, encodeEolStrict);
                try {
                    dh.writeTo(aos2);
                } catch (IOException e2) {
                }
                if (aos2.getAscii() == 1) {
                    encoding = "7bit";
                } else {
                    encoding = "base64";
                }
            }
            ContentType contentType = cType;
            return encoding;
        } catch (Exception e3) {
            return "base64";
        }
    }

    public static InputStream decode(InputStream is, String encoding) throws MessagingException {
        if (encoding.equalsIgnoreCase("base64")) {
            return new BASE64DecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPDecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUDecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit") || ignoreUnknownEncoding) {
            return is;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static OutputStream encode(OutputStream os, String encoding) throws MessagingException {
        if (encoding == null) {
            return os;
        }
        if (encoding.equalsIgnoreCase("base64")) {
            return new BASE64EncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return os;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static OutputStream encode(OutputStream os, String encoding, String filename) throws MessagingException {
        if (encoding == null) {
            return os;
        }
        if (encoding.equalsIgnoreCase("base64")) {
            return new BASE64EncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUEncoderStream(os, filename);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return os;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static String encodeText(String text) throws UnsupportedEncodingException {
        return encodeText(text, (String) null, (String) null);
    }

    public static String encodeText(String text, String charset, String encoding) throws UnsupportedEncodingException {
        return encodeWord(text, charset, encoding, $assertionsDisabled);
    }

    public static String decodeText(String etext) throws UnsupportedEncodingException {
        String word;
        if (etext.indexOf("=?") == -1) {
            return etext;
        }
        StringTokenizer st = new StringTokenizer(etext, " \t\n\r", true);
        StringBuilder sb = new StringBuilder();
        StringBuilder wsb = new StringBuilder();
        boolean prevWasEncoded = $assertionsDisabled;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            char c = s.charAt(0);
            if (c == ' ' || c == 9 || c == 13 || c == 10) {
                wsb.append(c);
            } else {
                try {
                    word = decodeWord(s);
                    if (!prevWasEncoded && wsb.length() > 0) {
                        sb.append(wsb);
                    }
                    prevWasEncoded = true;
                } catch (ParseException e) {
                    word = s;
                    if (!decodeStrict) {
                        String dword = decodeInnerWords(word);
                        if (dword != word) {
                            if ((!prevWasEncoded || !word.startsWith("=?")) && wsb.length() > 0) {
                                sb.append(wsb);
                            }
                            prevWasEncoded = word.endsWith("?=");
                            word = dword;
                        } else {
                            if (wsb.length() > 0) {
                                sb.append(wsb);
                            }
                            prevWasEncoded = $assertionsDisabled;
                        }
                    } else {
                        if (wsb.length() > 0) {
                            sb.append(wsb);
                        }
                        prevWasEncoded = $assertionsDisabled;
                    }
                }
                sb.append(word);
                wsb.setLength(0);
            }
        }
        sb.append(wsb);
        return sb.toString();
    }

    public static String encodeWord(String word) throws UnsupportedEncodingException {
        return encodeWord(word, (String) null, (String) null);
    }

    public static String encodeWord(String word, String charset, String encoding) throws UnsupportedEncodingException {
        return encodeWord(word, charset, encoding, true);
    }

    private static String encodeWord(String string, String charset, String encoding, boolean encodingWord) throws UnsupportedEncodingException {
        String jcharset;
        boolean b64;
        int ascii = checkAscii(string);
        if (ascii == 1) {
            return string;
        }
        if (charset == null) {
            jcharset = getDefaultJavaCharset();
            charset = getDefaultMIMECharset();
        } else {
            jcharset = javaCharset(charset);
        }
        if (encoding == null) {
            if (ascii != 3) {
                encoding = "Q";
            } else {
                encoding = "B";
            }
        }
        if (encoding.equalsIgnoreCase("B")) {
            b64 = true;
        } else if (encoding.equalsIgnoreCase("Q")) {
            b64 = $assertionsDisabled;
        } else {
            throw new UnsupportedEncodingException("Unknown transfer encoding: " + encoding);
        }
        StringBuilder outb = new StringBuilder();
        doEncode(string, b64, jcharset, 68 - charset.length(), "=?" + charset + "?" + encoding + "?", true, encodingWord, outb);
        return outb.toString();
    }

    private static void doEncode(String string, boolean b64, String jcharset, int avail, String prefix, boolean first, boolean encodingWord, StringBuilder buf) throws UnsupportedEncodingException {
        int len;
        OutputStream eos;
        byte[] bytes = string.getBytes(jcharset);
        if (b64) {
            len = BEncoderStream.encodedLength(bytes);
        } else {
            len = QEncoderStream.encodedLength(bytes, encodingWord);
        }
        if (len > avail) {
            int size = string.length();
            if (size > 1) {
                int split = size / 2;
                if (Character.isHighSurrogate(string.charAt(split - 1))) {
                    split--;
                }
                if (split > 0) {
                    doEncode(string.substring(0, split), b64, jcharset, avail, prefix, first, encodingWord, buf);
                }
                doEncode(string.substring(split, size), b64, jcharset, avail, prefix, $assertionsDisabled, encodingWord, buf);
                return;
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (b64) {
            eos = new BEncoderStream(os);
        } else {
            eos = new QEncoderStream(os, encodingWord);
        }
        try {
            eos.write(bytes);
            eos.close();
        } catch (IOException e) {
        }
        byte[] encodedBytes = os.toByteArray();
        if (!first) {
            if (foldEncodedWords) {
                buf.append("\r\n ");
            } else {
                buf.append(" ");
            }
        }
        buf.append(prefix);
        for (byte b : encodedBytes) {
            buf.append((char) b);
        }
        buf.append("?=");
    }

    public static String decodeWord(String eword) throws ParseException, UnsupportedEncodingException {
        String decodedWord;
        InputStream is;
        if (!eword.startsWith("=?")) {
            throw new ParseException("encoded word does not start with \"=?\": " + eword);
        }
        int pos = eword.indexOf(63, 2);
        if (pos == -1) {
            throw new ParseException("encoded word does not include charset: " + eword);
        }
        String charset = eword.substring(2, pos);
        int lpos = charset.indexOf(42);
        if (lpos >= 0) {
            charset = charset.substring(0, lpos);
        }
        String charset2 = javaCharset(charset);
        int start = pos + 1;
        int pos2 = eword.indexOf(63, start);
        if (pos2 == -1) {
            throw new ParseException("encoded word does not include encoding: " + eword);
        }
        String encoding = eword.substring(start, pos2);
        int start2 = pos2 + 1;
        int pos3 = eword.indexOf("?=", start2);
        if (pos3 == -1) {
            throw new ParseException("encoded word does not end with \"?=\": " + eword);
        }
        String word = eword.substring(start2, pos3);
        try {
            if (word.length() > 0) {
                ByteArrayInputStream bis = new ByteArrayInputStream(ASCIIUtility.getBytes(word));
                if (encoding.equalsIgnoreCase("B")) {
                    is = new BASE64DecoderStream(bis);
                } else if (encoding.equalsIgnoreCase("Q")) {
                    is = new QDecoderStream(bis);
                } else {
                    throw new UnsupportedEncodingException("unknown encoding: " + encoding);
                }
                int count = bis.available();
                byte[] bytes = new byte[count];
                int count2 = is.read(bytes, 0, count);
                if (count2 <= 0) {
                    decodedWord = "";
                } else {
                    decodedWord = new String(bytes, 0, count2, charset2);
                }
            } else {
                decodedWord = "";
            }
            if (pos3 + 2 >= eword.length()) {
                return decodedWord;
            }
            String rest = eword.substring(pos3 + 2);
            if (!decodeStrict) {
                rest = decodeInnerWords(rest);
            }
            return decodedWord + rest;
        } catch (UnsupportedEncodingException uex) {
            throw uex;
        } catch (IOException ioex) {
            throw new ParseException(ioex.toString());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedEncodingException(charset2);
        }
    }

    private static String decodeInnerWords(String word) throws UnsupportedEncodingException {
        int end;
        int end2;
        int start = 0;
        StringBuilder buf = new StringBuilder();
        while (true) {
            int i = word.indexOf("=?", start);
            if (i < 0) {
                break;
            }
            buf.append(word.substring(start, i));
            int end3 = word.indexOf(63, i + 2);
            if (end3 < 0 || (end = word.indexOf(63, end3 + 1)) < 0 || (end2 = word.indexOf("?=", end + 1)) < 0) {
                break;
            }
            String s = word.substring(i, end2 + 2);
            try {
                s = decodeWord(s);
            } catch (ParseException e) {
            }
            buf.append(s);
            start = end2 + 2;
        }
        if (start == 0) {
            return word;
        }
        if (start < word.length()) {
            buf.append(word.substring(start));
        }
        return buf.toString();
    }

    public static String quote(String word, String specials) {
        int len = word == null ? 0 : word.length();
        if (len == 0) {
            return "\"\"";
        }
        boolean needQuoting = $assertionsDisabled;
        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if (c == '\"' || c == '\\' || c == 13 || c == 10) {
                StringBuilder sb = new StringBuilder(len + 3);
                sb.append('\"');
                sb.append(word.substring(0, i));
                int lastc = 0;
                for (int j = i; j < len; j++) {
                    char cc = word.charAt(j);
                    if ((cc == '\"' || cc == '\\' || cc == 13 || cc == 10) && !(cc == 10 && lastc == 13)) {
                        sb.append('\\');
                    }
                    sb.append(cc);
                    lastc = cc;
                }
                sb.append('\"');
                return sb.toString();
            }
            if (c < ' ' || ((c >= 127 && !allowUtf8) || specials.indexOf(c) >= 0)) {
                needQuoting = true;
            }
        }
        if (!needQuoting) {
            return word;
        }
        StringBuilder sb2 = new StringBuilder(len + 2);
        sb2.append('\"').append(word).append('\"');
        return sb2.toString();
    }

    public static String fold(int used, String s) {
        if (!foldText) {
            return s;
        }
        int end = s.length() - 1;
        while (end >= 0) {
            char c = s.charAt(end);
            if (c != ' ' && c != 9 && c != 13 && c != 10) {
                break;
            }
            end--;
        }
        if (end != s.length() - 1) {
            s = s.substring(0, end + 1);
        }
        if (s.length() + used <= 76) {
            return makesafe(s);
        }
        StringBuilder sb = new StringBuilder(s.length() + 4);
        char lastc = 0;
        while (true) {
            if (s.length() + used <= 76) {
                break;
            }
            int lastspace = -1;
            int i = 0;
            while (i < s.length() && (lastspace == -1 || used + i <= 76)) {
                char c2 = s.charAt(i);
                if (!((c2 != ' ' && c2 != 9) || lastc == ' ' || lastc == 9)) {
                    lastspace = i;
                }
                lastc = c2;
                i++;
            }
            if (lastspace == -1) {
                sb.append(s);
                s = "";
                break;
            }
            sb.append(s.substring(0, lastspace));
            sb.append("\r\n");
            lastc = s.charAt(lastspace);
            sb.append(lastc);
            s = s.substring(lastspace + 1);
            used = 1;
        }
        sb.append(s);
        return makesafe(sb);
    }

    private static String makesafe(CharSequence s) {
        int i = 0;
        while (i < s.length() && (c = s.charAt(i)) != 13 && c != 10) {
            i++;
        }
        if (i == s.length()) {
            return s.toString();
        }
        StringBuilder sb = new StringBuilder(s.length() + 1);
        BufferedReader r = new BufferedReader(new StringReader(s.toString()));
        while (true) {
            try {
                String line = r.readLine();
                if (line == null) {
                    return sb.toString();
                }
                if (line.trim().length() != 0) {
                    if (sb.length() > 0) {
                        sb.append("\r\n");
                        if ($assertionsDisabled || line.length() > 0) {
                            char c = line.charAt(0);
                            if (!(c == ' ' || c == 9)) {
                                sb.append(' ');
                            }
                        } else {
                            throw new AssertionError();
                        }
                    }
                    sb.append(line);
                }
            } catch (IOException e) {
                return s.toString();
            }
        }
    }

    public static String unfold(String s) {
        char c;
        if (!foldText) {
            return s;
        }
        StringBuilder sb = null;
        while (true) {
            int i = indexOfAny(s, "\r\n");
            if (i < 0) {
                break;
            }
            int start = i;
            int slen = s.length();
            int i2 = i + 1;
            if (i2 < slen && s.charAt(i2 - 1) == 13 && s.charAt(i2) == 10) {
                i2++;
            }
            if (start > 0 && s.charAt(start - 1) == '\\') {
                if (sb == null) {
                    sb = new StringBuilder(s.length());
                }
                sb.append(s.substring(0, start - 1));
                sb.append(s.substring(start, i2));
                s = s.substring(i2);
            } else if (i2 >= slen || (c = s.charAt(i2)) == ' ' || c == 9) {
                if (sb == null) {
                    sb = new StringBuilder(s.length());
                }
                sb.append(s.substring(0, start));
                s = s.substring(i2);
            } else {
                if (sb == null) {
                    sb = new StringBuilder(s.length());
                }
                sb.append(s.substring(0, i2));
                s = s.substring(i2);
            }
        }
        if (sb == null) {
            return s;
        }
        sb.append(s);
        return sb.toString();
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

    public static String javaCharset(String charset) {
        if (mime2java == null || charset == null) {
            return charset;
        }
        String alias = mime2java.get(charset.toLowerCase(Locale.ENGLISH));
        if (alias != null) {
            try {
                Charset.forName(alias);
            } catch (Exception e) {
                alias = null;
            }
        }
        return alias != null ? alias : charset;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0007, code lost:
        r0 = java2mime.get(r3.toLowerCase(java.util.Locale.ENGLISH));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String mimeCharset(java.lang.String r3) {
        /*
            java.util.Map<java.lang.String, java.lang.String> r1 = java2mime
            if (r1 == 0) goto L_0x0006
            if (r3 != 0) goto L_0x0007
        L_0x0006:
            return r3
        L_0x0007:
            java.util.Map<java.lang.String, java.lang.String> r1 = java2mime
            java.util.Locale r2 = java.util.Locale.ENGLISH
            java.lang.String r2 = r3.toLowerCase(r2)
            java.lang.Object r0 = r1.get(r2)
            java.lang.String r0 = (java.lang.String) r0
            if (r0 == 0) goto L_0x0006
            r3 = r0
            goto L_0x0006
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeUtility.mimeCharset(java.lang.String):java.lang.String");
    }

    public static String getDefaultJavaCharset() {
        if (defaultJavaCharset == null) {
            String mimecs = null;
            try {
                mimecs = System.getProperty("mail.mime.charset");
            } catch (SecurityException e) {
            }
            if (mimecs == null || mimecs.length() <= 0) {
                try {
                    defaultJavaCharset = System.getProperty("file.encoding", "8859_1");
                } catch (SecurityException e2) {
                    defaultJavaCharset = new InputStreamReader(new InputStream() {
                        public int read() {
                            return 0;
                        }
                    }).getEncoding();
                    if (defaultJavaCharset == null) {
                        defaultJavaCharset = "8859_1";
                    }
                }
            } else {
                defaultJavaCharset = javaCharset(mimecs);
                return defaultJavaCharset;
            }
        }
        return defaultJavaCharset;
    }

    static String getDefaultMIMECharset() {
        if (defaultMIMECharset == null) {
            try {
                defaultMIMECharset = System.getProperty("mail.mime.charset");
            } catch (SecurityException e) {
            }
        }
        if (defaultMIMECharset == null) {
            defaultMIMECharset = mimeCharset(getDefaultJavaCharset());
        }
        return defaultMIMECharset;
    }

    private static void loadMappings(LineInputStream is, Map<String, String> table) {
        while (true) {
            try {
                String currLine = is.readLine();
                if (currLine != null) {
                    if (currLine.startsWith("--") && currLine.endsWith("--")) {
                        return;
                    }
                    if (currLine.trim().length() != 0 && !currLine.startsWith("#")) {
                        StringTokenizer tk = new StringTokenizer(currLine, " \t");
                        try {
                            String key = tk.nextToken();
                            table.put(key.toLowerCase(Locale.ENGLISH), tk.nextToken());
                        } catch (NoSuchElementException e) {
                        }
                    }
                } else {
                    return;
                }
            } catch (IOException e2) {
                return;
            }
        }
    }

    static int checkAscii(String s) {
        int ascii = 0;
        int non_ascii = 0;
        int l = s.length();
        for (int i = 0; i < l; i++) {
            if (nonascii(s.charAt(i))) {
                non_ascii++;
            } else {
                ascii++;
            }
        }
        if (non_ascii == 0) {
            return 1;
        }
        if (ascii > non_ascii) {
            return 2;
        }
        return 3;
    }

    static int checkAscii(byte[] b) {
        int ascii = 0;
        int non_ascii = 0;
        for (byte b2 : b) {
            if (nonascii(b2 & Ev3Constants.Opcode.TST)) {
                non_ascii++;
            } else {
                ascii++;
            }
        }
        if (non_ascii == 0) {
            return 1;
        }
        if (ascii > non_ascii) {
            return 2;
        }
        return 3;
    }

    static int checkAscii(InputStream is, int max, boolean breakOnNonAscii) {
        int ascii = 0;
        int non_ascii = 0;
        int block = 4096;
        int linelen = 0;
        boolean longLine = $assertionsDisabled;
        boolean badEOL = $assertionsDisabled;
        boolean checkEOL = (!encodeEolStrict || !breakOnNonAscii) ? $assertionsDisabled : true;
        byte[] buf = null;
        if (max != 0) {
            block = max == -1 ? 4096 : Math.min(max, 4096);
            buf = new byte[block];
        }
        while (max != 0) {
            try {
                int len = is.read(buf, 0, block);
                if (len == -1) {
                    break;
                }
                int lastb = 0;
                for (int i = 0; i < len; i++) {
                    int b = buf[i] & 255;
                    if (checkEOL && ((lastb == 13 && b != 10) || (lastb != 13 && b == 10))) {
                        badEOL = true;
                    }
                    if (b == 13 || b == 10) {
                        linelen = 0;
                    } else {
                        linelen++;
                        if (linelen > 998) {
                            longLine = true;
                        }
                    }
                    if (!nonascii(b)) {
                        ascii++;
                    } else if (breakOnNonAscii) {
                        return 3;
                    } else {
                        non_ascii++;
                    }
                    lastb = b;
                }
                if (max != -1) {
                    max -= len;
                }
            } catch (IOException e) {
            }
        }
        if (max == 0 && breakOnNonAscii) {
            return 3;
        }
        if (non_ascii == 0) {
            if (badEOL) {
                return 3;
            }
            if (longLine) {
                return 2;
            }
            return 1;
        } else if (ascii > non_ascii) {
            return 2;
        } else {
            return 3;
        }
    }

    static final boolean nonascii(int b) {
        if (b >= 127 || (b < 32 && b != 13 && b != 10 && b != 9)) {
            return true;
        }
        return $assertionsDisabled;
    }
}
