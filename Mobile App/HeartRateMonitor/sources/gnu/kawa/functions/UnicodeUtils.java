package gnu.kawa.functions;

import gnu.mapping.Namespace;
import gnu.mapping.Symbol;
import java.text.BreakIterator;

public class UnicodeUtils {

    /* renamed from: Cc */
    static final Symbol f54Cc;

    /* renamed from: Cf */
    static final Symbol f55Cf;

    /* renamed from: Cn */
    static final Symbol f56Cn;

    /* renamed from: Co */
    static final Symbol f57Co;

    /* renamed from: Cs */
    static final Symbol f58Cs;

    /* renamed from: Ll */
    static final Symbol f59Ll;

    /* renamed from: Lm */
    static final Symbol f60Lm;

    /* renamed from: Lo */
    static final Symbol f61Lo;

    /* renamed from: Lt */
    static final Symbol f62Lt;

    /* renamed from: Lu */
    static final Symbol f63Lu;

    /* renamed from: Mc */
    static final Symbol f64Mc;

    /* renamed from: Me */
    static final Symbol f65Me;

    /* renamed from: Mn */
    static final Symbol f66Mn;

    /* renamed from: Nd */
    static final Symbol f67Nd;

    /* renamed from: Nl */
    static final Symbol f68Nl;

    /* renamed from: No */
    static final Symbol f69No;

    /* renamed from: Pc */
    static final Symbol f70Pc;

    /* renamed from: Pd */
    static final Symbol f71Pd;

    /* renamed from: Pe */
    static final Symbol f72Pe;

    /* renamed from: Pf */
    static final Symbol f73Pf;

    /* renamed from: Pi */
    static final Symbol f74Pi;

    /* renamed from: Po */
    static final Symbol f75Po;

    /* renamed from: Ps */
    static final Symbol f76Ps;

    /* renamed from: Sc */
    static final Symbol f77Sc;

    /* renamed from: Sk */
    static final Symbol f78Sk;

    /* renamed from: Sm */
    static final Symbol f79Sm;

    /* renamed from: So */
    static final Symbol f80So;

    /* renamed from: Zl */
    static final Symbol f81Zl;

    /* renamed from: Zp */
    static final Symbol f82Zp;

    /* renamed from: Zs */
    static final Symbol f83Zs;

    public static boolean isWhitespace(int ch) {
        if (ch == 32 || (ch >= 9 && ch <= 13)) {
            return true;
        }
        if (ch < 133) {
            return false;
        }
        if (ch == 133 || ch == 160 || ch == 5760 || ch == 6158) {
            return true;
        }
        if (ch < 8192 || ch > 12288) {
            return false;
        }
        if (ch <= 8202 || ch == 8232 || ch == 8233 || ch == 8239 || ch == 8287 || ch == 12288) {
            return true;
        }
        return false;
    }

    public static String capitalize(String str) {
        StringBuilder sbuf = new StringBuilder();
        BreakIterator wb = BreakIterator.getWordInstance();
        wb.setText(str);
        int start = wb.first();
        for (int end = wb.next(); end != -1; end = wb.next()) {
            boolean isWord = false;
            int p = start;
            while (true) {
                if (p >= end) {
                    break;
                } else if (Character.isLetter(str.codePointAt(p))) {
                    isWord = true;
                    break;
                } else {
                    p++;
                }
            }
            if (!isWord) {
                sbuf.append(str, start, end);
            } else {
                sbuf.append(Character.toTitleCase(str.charAt(start)));
                sbuf.append(str.substring(start + 1, end).toLowerCase());
            }
            start = end;
        }
        return sbuf.toString();
    }

    public static String foldCase(CharSequence str) {
        int len = str.length();
        if (len == 0) {
            return "";
        }
        StringBuilder sbuf = null;
        int start = 0;
        int i = 0;
        while (true) {
            int ch = i == len ? -1 : str.charAt(i);
            boolean sigma = ch == 931 || ch == 963 || ch == 962;
            if (ch < 0 || ch == 304 || ch == 305 || sigma) {
                if (sbuf == null && ch >= 0) {
                    sbuf = new StringBuilder();
                }
                if (i > start) {
                    String converted = str.subSequence(start, i).toString().toUpperCase().toLowerCase();
                    if (sbuf == null) {
                        return converted;
                    }
                    sbuf.append(converted);
                }
                if (ch < 0) {
                    return sbuf.toString();
                }
                if (sigma) {
                    ch = 963;
                }
                sbuf.append((char) ch);
                start = i + 1;
            }
            i++;
        }
    }

    public static Symbol generalCategory(int ch) {
        switch (Character.getType(ch)) {
            case 1:
                return f63Lu;
            case 2:
                return f59Ll;
            case 3:
                return f62Lt;
            case 4:
                return f60Lm;
            case 5:
                return f61Lo;
            case 6:
                return f66Mn;
            case 7:
                return f65Me;
            case 8:
                return f64Mc;
            case 9:
                return f67Nd;
            case 10:
                return f68Nl;
            case 11:
                return f69No;
            case 12:
                return f83Zs;
            case 13:
                return f81Zl;
            case 14:
                return f82Zp;
            case 15:
                return f54Cc;
            case 16:
                return f55Cf;
            case 18:
                return f57Co;
            case 19:
                return f58Cs;
            case 20:
                return f71Pd;
            case 21:
                return f76Ps;
            case 22:
                return f72Pe;
            case 23:
                return f70Pc;
            case 24:
                return f75Po;
            case 25:
                return f79Sm;
            case 26:
                return f77Sc;
            case 27:
                return f78Sk;
            case 28:
                return f80So;
            case 29:
                return f74Pi;
            case 30:
                return f73Pf;
            default:
                return f56Cn;
        }
    }

    static {
        Namespace empty = Namespace.EmptyNamespace;
        f64Mc = empty.getSymbol("Mc");
        f70Pc = empty.getSymbol("Pc");
        f54Cc = empty.getSymbol("Cc");
        f77Sc = empty.getSymbol("Sc");
        f71Pd = empty.getSymbol("Pd");
        f67Nd = empty.getSymbol("Nd");
        f65Me = empty.getSymbol("Me");
        f72Pe = empty.getSymbol("Pe");
        f73Pf = empty.getSymbol("Pf");
        f55Cf = empty.getSymbol("Cf");
        f74Pi = empty.getSymbol("Pi");
        f68Nl = empty.getSymbol("Nl");
        f81Zl = empty.getSymbol("Zl");
        f59Ll = empty.getSymbol("Ll");
        f79Sm = empty.getSymbol("Sm");
        f60Lm = empty.getSymbol("Lm");
        f78Sk = empty.getSymbol("Sk");
        f66Mn = empty.getSymbol("Mn");
        f61Lo = empty.getSymbol("Lo");
        f69No = empty.getSymbol("No");
        f75Po = empty.getSymbol("Po");
        f80So = empty.getSymbol("So");
        f82Zp = empty.getSymbol("Zp");
        f57Co = empty.getSymbol("Co");
        f83Zs = empty.getSymbol("Zs");
        f76Ps = empty.getSymbol("Ps");
        f58Cs = empty.getSymbol("Cs");
        f62Lt = empty.getSymbol("Lt");
        f56Cn = empty.getSymbol("Cn");
        f63Lu = empty.getSymbol("Lu");
    }
}
