package javax.mail.internet;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.PropUtil;
import gnu.bytecode.Access;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ParameterList {
    private static final boolean applehack = PropUtil.getBooleanSystemProperty("mail.mime.applefilenames", false);
    private static final boolean decodeParameters = PropUtil.getBooleanSystemProperty("mail.mime.decodeparameters", true);
    private static final boolean decodeParametersStrict = PropUtil.getBooleanSystemProperty("mail.mime.decodeparameters.strict", false);
    private static final boolean encodeParameters = PropUtil.getBooleanSystemProperty("mail.mime.encodeparameters", true);
    private static final char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', Access.CLASS_CONTEXT, 'D', 'E', Access.FIELD_CONTEXT};
    private static final boolean parametersStrict = PropUtil.getBooleanSystemProperty("mail.mime.parameters.strict", true);
    private static final boolean splitLongParameters = PropUtil.getBooleanSystemProperty("mail.mime.splitlongparameters", true);
    private static final boolean windowshack = PropUtil.getBooleanSystemProperty("mail.mime.windowsfilenames", false);
    private String lastName;
    private Map<String, Object> list;
    private Set<String> multisegmentNames;
    private Map<String, Object> slist;

    private static class Value {
        String charset;
        String encodedValue;
        String value;

        private Value() {
        }
    }

    private static class LiteralValue {
        String value;

        private LiteralValue() {
        }
    }

    private static class MultiValue extends ArrayList<Object> {
        private static final long serialVersionUID = 699561094618751023L;
        String value;

        private MultiValue() {
        }
    }

    private static class ParamEnum implements Enumeration<String> {

        /* renamed from: it */
        private Iterator<String> f305it;

        ParamEnum(Iterator<String> it) {
            this.f305it = it;
        }

        public boolean hasMoreElements() {
            return this.f305it.hasNext();
        }

        public String nextElement() {
            return this.f305it.next();
        }
    }

    public ParameterList() {
        this.list = new LinkedHashMap();
        this.lastName = null;
        if (decodeParameters) {
            this.multisegmentNames = new HashSet();
            this.slist = new HashMap();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x001a, code lost:
        if (decodeParameters == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x001c, code lost:
        combineMultisegmentNames(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ParameterList(java.lang.String r12) throws javax.mail.internet.ParseException {
        /*
            r11 = this;
            r10 = -4
            r9 = 59
            r8 = -1
            r11.<init>()
            javax.mail.internet.HeaderTokenizer r0 = new javax.mail.internet.HeaderTokenizer
            java.lang.String r6 = "()<>@,;:\\\"\t []/?="
            r0.<init>(r12, r6)
        L_0x000e:
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            int r4 = r3.getType()
            if (r4 != r10) goto L_0x0021
        L_0x0018:
            boolean r6 = decodeParameters
            if (r6 == 0) goto L_0x0020
            r6 = 0
            r11.combineMultisegmentNames(r6)
        L_0x0020:
            return
        L_0x0021:
            char r6 = (char) r4
            if (r6 != r9) goto L_0x0118
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            int r6 = r3.getType()
            if (r6 == r10) goto L_0x0018
            int r6 = r3.getType()
            if (r6 == r8) goto L_0x0061
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "In parameter list <"
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.StringBuilder r7 = r7.append(r12)
            java.lang.String r8 = ">, expected parameter name, got \""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x0061:
            java.lang.String r6 = r3.getValue()
            java.util.Locale r7 = java.util.Locale.ENGLISH
            java.lang.String r2 = r6.toLowerCase(r7)
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            int r6 = r3.getType()
            char r6 = (char) r6
            r7 = 61
            if (r6 == r7) goto L_0x00a5
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "In parameter list <"
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.StringBuilder r7 = r7.append(r12)
            java.lang.String r8 = ">, expected '=', got \""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x00a5:
            boolean r6 = windowshack
            if (r6 == 0) goto L_0x00f4
            java.lang.String r6 = "name"
            boolean r6 = r2.equals(r6)
            if (r6 != 0) goto L_0x00b9
            java.lang.String r6 = "filename"
            boolean r6 = r2.equals(r6)
            if (r6 == 0) goto L_0x00f4
        L_0x00b9:
            r6 = 1
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next(r9, r6)
        L_0x00be:
            int r4 = r3.getType()
            if (r4 == r8) goto L_0x0102
            r6 = -2
            if (r4 == r6) goto L_0x0102
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "In parameter list <"
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.StringBuilder r7 = r7.append(r12)
            java.lang.String r8 = ">, expected parameter value, got \""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x00f4:
            boolean r6 = parametersStrict
            if (r6 == 0) goto L_0x00fd
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            goto L_0x00be
        L_0x00fd:
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next(r9)
            goto L_0x00be
        L_0x0102:
            java.lang.String r5 = r3.getValue()
            r11.lastName = r2
            boolean r6 = decodeParameters
            if (r6 == 0) goto L_0x0111
            r11.putEncodedName(r2, r5)
            goto L_0x000e
        L_0x0111:
            java.util.Map<java.lang.String, java.lang.Object> r6 = r11.list
            r6.put(r2, r5)
            goto L_0x000e
        L_0x0118:
            if (r4 != r8) goto L_0x0168
            java.lang.String r6 = r11.lastName
            if (r6 == 0) goto L_0x0168
            boolean r6 = applehack
            if (r6 == 0) goto L_0x0136
            java.lang.String r6 = r11.lastName
            java.lang.String r7 = "name"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x013a
            java.lang.String r6 = r11.lastName
            java.lang.String r7 = "filename"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x013a
        L_0x0136:
            boolean r6 = parametersStrict
            if (r6 != 0) goto L_0x0168
        L_0x013a:
            java.util.Map<java.lang.String, java.lang.Object> r6 = r11.list
            java.lang.String r7 = r11.lastName
            java.lang.Object r1 = r6.get(r7)
            java.lang.String r1 = (java.lang.String) r1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.StringBuilder r6 = r6.append(r1)
            java.lang.String r7 = " "
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r7 = r3.getValue()
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r5 = r6.toString()
            java.util.Map<java.lang.String, java.lang.Object> r6 = r11.list
            java.lang.String r7 = r11.lastName
            r6.put(r7, r5)
            goto L_0x000e
        L_0x0168:
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "In parameter list <"
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.StringBuilder r7 = r7.append(r12)
            java.lang.String r8 = ">, expected ';', got \""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.<init>(java.lang.String):void");
    }

    public void combineSegments() {
        if (decodeParameters && this.multisegmentNames.size() > 0) {
            try {
                combineMultisegmentNames(true);
            } catch (ParseException e) {
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: javax.mail.internet.ParameterList$Value} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v6, resolved type: javax.mail.internet.ParameterList$Value} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void putEncodedName(java.lang.String r8, java.lang.String r9) throws javax.mail.internet.ParseException {
        /*
            r7 = this;
            r6 = 0
            r4 = 42
            int r2 = r8.indexOf(r4)
            if (r2 >= 0) goto L_0x000f
            java.util.Map<java.lang.String, java.lang.Object> r4 = r7.list
            r4.put(r8, r9)
        L_0x000e:
            return
        L_0x000f:
            int r4 = r8.length()
            int r4 = r4 + -1
            if (r2 != r4) goto L_0x003e
            java.lang.String r8 = r8.substring(r6, r2)
            javax.mail.internet.ParameterList$Value r3 = extractCharset(r9)
            java.lang.String r4 = r3.value     // Catch:{ UnsupportedEncodingException -> 0x002f }
            java.lang.String r5 = r3.charset     // Catch:{ UnsupportedEncodingException -> 0x002f }
            java.lang.String r4 = decodeBytes((java.lang.String) r4, (java.lang.String) r5)     // Catch:{ UnsupportedEncodingException -> 0x002f }
            r3.value = r4     // Catch:{ UnsupportedEncodingException -> 0x002f }
        L_0x0029:
            java.util.Map<java.lang.String, java.lang.Object> r4 = r7.list
            r4.put(r8, r3)
            goto L_0x000e
        L_0x002f:
            r0 = move-exception
            boolean r4 = decodeParametersStrict
            if (r4 == 0) goto L_0x0029
            javax.mail.internet.ParseException r4 = new javax.mail.internet.ParseException
            java.lang.String r5 = r0.toString()
            r4.<init>(r5)
            throw r4
        L_0x003e:
            java.lang.String r1 = r8.substring(r6, r2)
            java.util.Set<java.lang.String> r4 = r7.multisegmentNames
            r4.add(r1)
            java.util.Map<java.lang.String, java.lang.Object> r4 = r7.list
            java.lang.String r5 = ""
            r4.put(r1, r5)
            java.lang.String r4 = "*"
            boolean r4 = r8.endsWith(r4)
            if (r4 == 0) goto L_0x0083
            java.lang.String r4 = "*0*"
            boolean r4 = r8.endsWith(r4)
            if (r4 == 0) goto L_0x0072
            javax.mail.internet.ParameterList$Value r3 = extractCharset(r9)
        L_0x0062:
            int r4 = r8.length()
            int r4 = r4 + -1
            java.lang.String r8 = r8.substring(r6, r4)
        L_0x006c:
            java.util.Map<java.lang.String, java.lang.Object> r4 = r7.slist
            r4.put(r8, r3)
            goto L_0x000e
        L_0x0072:
            javax.mail.internet.ParameterList$Value r3 = new javax.mail.internet.ParameterList$Value
            r4 = 0
            r3.<init>()
            r4 = r3
            javax.mail.internet.ParameterList$Value r4 = (javax.mail.internet.ParameterList.Value) r4
            r4.encodedValue = r9
            r4 = r3
            javax.mail.internet.ParameterList$Value r4 = (javax.mail.internet.ParameterList.Value) r4
            r4.value = r9
            goto L_0x0062
        L_0x0083:
            r3 = r9
            goto L_0x006c
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.putEncodedName(java.lang.String, java.lang.String):void");
    }

    private void combineMultisegmentNames(boolean keepConsistentOnFailure) throws ParseException {
        MultiValue mv;
        ByteArrayOutputStream bos;
        try {
            for (String name : this.multisegmentNames) {
                mv = new MultiValue();
                String charset = null;
                bos = new ByteArrayOutputStream();
                int segment = 0;
                while (true) {
                    String sname = name + "*" + segment;
                    Object v = this.slist.get(sname);
                    if (v == null) {
                        break;
                    }
                    mv.add(v);
                    try {
                        if (v instanceof Value) {
                            Value vv = (Value) v;
                            if (segment != 0) {
                                if (charset == null) {
                                    this.multisegmentNames.remove(name);
                                    break;
                                }
                            } else {
                                charset = vv.charset;
                            }
                            decodeBytes(vv.value, (OutputStream) bos);
                        } else {
                            bos.write(ASCIIUtility.getBytes((String) v));
                        }
                    } catch (IOException e) {
                    }
                    this.slist.remove(sname);
                    segment++;
                }
                if (segment == 0) {
                    this.list.remove(name);
                } else {
                    if (charset != null) {
                        charset = MimeUtility.javaCharset(charset);
                    }
                    if (charset == null || charset.length() == 0) {
                        charset = MimeUtility.getDefaultJavaCharset();
                    }
                    if (charset != null) {
                        mv.value = bos.toString(charset);
                    } else {
                        mv.value = bos.toString();
                    }
                    this.list.put(name, mv);
                }
            }
            if (keepConsistentOnFailure || 1 != 0) {
                if (this.slist.size() > 0) {
                    for (Object v2 : this.slist.values()) {
                        if (v2 instanceof Value) {
                            Value vv2 = (Value) v2;
                            try {
                                vv2.value = decodeBytes(vv2.value, vv2.charset);
                            } catch (UnsupportedEncodingException ex) {
                                if (decodeParametersStrict) {
                                    throw new ParseException(ex.toString());
                                }
                            }
                        }
                    }
                    this.list.putAll(this.slist);
                }
                this.multisegmentNames.clear();
                this.slist.clear();
            }
        } catch (UnsupportedEncodingException uex) {
            if (decodeParametersStrict) {
                throw new ParseException(uex.toString());
            }
            try {
                mv.value = bos.toString("iso-8859-1");
            } catch (UnsupportedEncodingException e2) {
            }
        } catch (Throwable th) {
            if (keepConsistentOnFailure || 0 != 0) {
                if (this.slist.size() > 0) {
                    for (Object v3 : this.slist.values()) {
                        if (v3 instanceof Value) {
                            Value vv3 = (Value) v3;
                            try {
                                vv3.value = decodeBytes(vv3.value, vv3.charset);
                            } catch (UnsupportedEncodingException ex2) {
                                if (decodeParametersStrict) {
                                    throw new ParseException(ex2.toString());
                                }
                            }
                        }
                    }
                    this.list.putAll(this.slist);
                }
                this.multisegmentNames.clear();
                this.slist.clear();
            }
            throw th;
        }
    }

    public int size() {
        return this.list.size();
    }

    public String get(String name) {
        Object v = this.list.get(name.trim().toLowerCase(Locale.ENGLISH));
        if (v instanceof MultiValue) {
            return ((MultiValue) v).value;
        }
        if (v instanceof LiteralValue) {
            return ((LiteralValue) v).value;
        }
        if (v instanceof Value) {
            return ((Value) v).value;
        }
        return (String) v;
    }

    public void set(String name, String value) {
        String name2 = name.trim().toLowerCase(Locale.ENGLISH);
        if (decodeParameters) {
            try {
                putEncodedName(name2, value);
            } catch (ParseException e) {
                this.list.put(name2, value);
            }
        } else {
            this.list.put(name2, value);
        }
    }

    public void set(String name, String value, String charset) {
        if (encodeParameters) {
            Value ev = encodeValue(value, charset);
            if (ev != null) {
                this.list.put(name.trim().toLowerCase(Locale.ENGLISH), ev);
            } else {
                set(name, value);
            }
        } else {
            set(name, value);
        }
    }

    /* access modifiers changed from: package-private */
    public void setLiteral(String name, String value) {
        LiteralValue lv = new LiteralValue();
        lv.value = value;
        this.list.put(name, lv);
    }

    public void remove(String name) {
        this.list.remove(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public Enumeration<String> getNames() {
        return new ParamEnum(this.list.keySet().iterator());
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int used) {
        String ns;
        String value;
        ToStringBuffer sb = new ToStringBuffer(used);
        for (Map.Entry<String, Object> ent : this.list.entrySet()) {
            String name = ent.getKey();
            Object v = ent.getValue();
            if (v instanceof MultiValue) {
                MultiValue vv = (MultiValue) v;
                String name2 = name + "*";
                for (int i = 0; i < vv.size(); i++) {
                    Object va = vv.get(i);
                    if (va instanceof Value) {
                        ns = name2 + i + "*";
                        value = ((Value) va).encodedValue;
                    } else {
                        ns = name2 + i;
                        value = (String) va;
                    }
                    sb.addNV(ns, quote(value));
                }
            } else if (v instanceof LiteralValue) {
                sb.addNV(name, quote(((LiteralValue) v).value));
            } else if (v instanceof Value) {
                sb.addNV(name + "*", quote(((Value) v).encodedValue));
            } else {
                String value2 = (String) v;
                if (value2.length() <= 60 || !splitLongParameters || !encodeParameters) {
                    sb.addNV(name, quote(value2));
                } else {
                    int seg = 0;
                    String name3 = name + "*";
                    while (value2.length() > 60) {
                        sb.addNV(name3 + seg, quote(value2.substring(0, 60)));
                        value2 = value2.substring(60);
                        seg++;
                    }
                    if (value2.length() > 0) {
                        sb.addNV(name3 + seg, quote(value2));
                    }
                }
            }
        }
        return sb.toString();
    }

    private static class ToStringBuffer {

        /* renamed from: sb */
        private StringBuilder f306sb = new StringBuilder();
        private int used;

        public ToStringBuffer(int used2) {
            this.used = used2;
        }

        public void addNV(String name, String value) {
            this.f306sb.append("; ");
            this.used += 2;
            if (this.used + name.length() + value.length() + 1 > 76) {
                this.f306sb.append("\r\n\t");
                this.used = 8;
            }
            this.f306sb.append(name).append('=');
            this.used += name.length() + 1;
            if (this.used + value.length() > 76) {
                String s = MimeUtility.fold(this.used, value);
                this.f306sb.append(s);
                int lastlf = s.lastIndexOf(10);
                if (lastlf >= 0) {
                    this.used += (s.length() - lastlf) - 1;
                } else {
                    this.used += s.length();
                }
            } else {
                this.f306sb.append(value);
                this.used += value.length();
            }
        }

        public String toString() {
            return this.f306sb.toString();
        }
    }

    private static String quote(String value) {
        return MimeUtility.quote(value, HeaderTokenizer.MIME);
    }

    private static Value encodeValue(String value, String charset) {
        if (MimeUtility.checkAscii(value) == 1) {
            return null;
        }
        try {
            byte[] b = value.getBytes(MimeUtility.javaCharset(charset));
            StringBuffer sb = new StringBuffer(b.length + charset.length() + 2);
            sb.append(charset).append("''");
            for (byte b2 : b) {
                char c = (char) (b2 & Ev3Constants.Opcode.TST);
                if (c <= ' ' || c >= 127 || c == '*' || c == '\'' || c == '%' || HeaderTokenizer.MIME.indexOf(c) >= 0) {
                    sb.append('%').append(hex[c >> 4]).append(hex[c & 15]);
                } else {
                    sb.append(c);
                }
            }
            Value v = new Value();
            v.charset = charset;
            v.value = value;
            v.encodedValue = sb.toString();
            return v;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static Value extractCharset(String value) throws ParseException {
        Value v = new Value();
        v.encodedValue = value;
        v.value = value;
        try {
            int i = value.indexOf(39);
            if (i >= 0) {
                String charset = value.substring(0, i);
                int li = value.indexOf(39, i + 1);
                if (li >= 0) {
                    v.value = value.substring(li + 1);
                    v.charset = charset;
                } else if (decodeParametersStrict) {
                    throw new ParseException("Missing language in encoded value: " + value);
                }
            } else if (decodeParametersStrict) {
                throw new ParseException("Missing charset in encoded value: " + value);
            }
        } catch (NumberFormatException nex) {
            if (decodeParametersStrict) {
                throw new ParseException(nex.toString());
            }
        } catch (StringIndexOutOfBoundsException ex) {
            if (decodeParametersStrict) {
                throw new ParseException(ex.toString());
            }
        }
        return v;
    }

    private static String decodeBytes(String value, String charset) throws ParseException, UnsupportedEncodingException {
        byte[] b = new byte[value.length()];
        int i = 0;
        int bi = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '%') {
                try {
                    c = (char) Integer.parseInt(value.substring(i + 1, i + 3), 16);
                    i += 2;
                } catch (NumberFormatException ex) {
                    if (decodeParametersStrict) {
                        throw new ParseException(ex.toString());
                    }
                } catch (StringIndexOutOfBoundsException ex2) {
                    if (decodeParametersStrict) {
                        throw new ParseException(ex2.toString());
                    }
                }
            }
            b[bi] = (byte) c;
            i++;
            bi++;
        }
        if (charset != null) {
            charset = MimeUtility.javaCharset(charset);
        }
        if (charset == null || charset.length() == 0) {
            charset = MimeUtility.getDefaultJavaCharset();
        }
        return new String(b, 0, bi, charset);
    }

    private static void decodeBytes(String value, OutputStream os) throws ParseException, IOException {
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '%') {
                try {
                    c = (char) Integer.parseInt(value.substring(i + 1, i + 3), 16);
                    i += 2;
                } catch (NumberFormatException ex) {
                    if (decodeParametersStrict) {
                        throw new ParseException(ex.toString());
                    }
                } catch (StringIndexOutOfBoundsException ex2) {
                    if (decodeParametersStrict) {
                        throw new ParseException(ex2.toString());
                    }
                }
            }
            os.write((byte) c);
            i++;
        }
    }
}
