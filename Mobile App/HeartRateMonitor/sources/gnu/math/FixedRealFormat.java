package gnu.math;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class FixedRealFormat extends Format {

    /* renamed from: d */
    private int f228d;

    /* renamed from: i */
    private int f229i;
    public boolean internalPad;
    public char overflowChar;
    public char padChar;
    public int scale;
    public boolean showPlus;
    public int width;

    public int getMaximumFractionDigits() {
        return this.f228d;
    }

    public int getMinimumIntegerDigits() {
        return this.f229i;
    }

    public void setMaximumFractionDigits(int d) {
        this.f228d = d;
    }

    public void setMinimumIntegerDigits(int i) {
        this.f229i = i;
    }

    public void format(RealNum number, StringBuffer sbuf, FieldPosition fpos) {
        int decimals;
        if (!(number instanceof RatNum) || (decimals = getMaximumFractionDigits()) < 0) {
            format(number.doubleValue(), sbuf, fpos);
            return;
        }
        RatNum ratnum = (RatNum) number;
        boolean negative = ratnum.isNegative();
        if (negative) {
            ratnum = ratnum.rneg();
        }
        int oldSize = sbuf.length();
        int signLen = 1;
        if (negative) {
            sbuf.append('-');
        } else if (this.showPlus) {
            sbuf.append('+');
        } else {
            signLen = 0;
        }
        String string = RealNum.toScaledInt(ratnum, this.scale + decimals).toString();
        sbuf.append(string);
        int length = string.length();
        format(sbuf, fpos, length, length - decimals, decimals, signLen, oldSize);
    }

    public StringBuffer format(long num, StringBuffer sbuf, FieldPosition fpos) {
        format((RealNum) IntNum.make(num), sbuf, fpos);
        return sbuf;
    }

    public StringBuffer format(double num, StringBuffer sbuf, FieldPosition fpos) {
        boolean negative;
        int decimals;
        char nextDigit;
        if (Double.isNaN(num) || Double.isInfinite(num)) {
            return sbuf.append(num);
        }
        if (getMaximumFractionDigits() >= 0) {
            format((RealNum) DFloNum.toExact(num), sbuf, fpos);
            return sbuf;
        }
        if (num < 0.0d) {
            negative = true;
            num = -num;
        } else {
            negative = false;
        }
        int oldSize = sbuf.length();
        int signLen = 1;
        if (negative) {
            sbuf.append('-');
        } else if (this.showPlus) {
            sbuf.append('+');
        } else {
            signLen = 0;
        }
        String string = Double.toString(num);
        int cur_scale = this.scale;
        int seenE = string.indexOf(69);
        if (seenE >= 0) {
            int expStart = seenE + 1;
            if (string.charAt(expStart) == '+') {
                expStart++;
            }
            cur_scale += Integer.parseInt(string.substring(expStart));
            string = string.substring(0, seenE);
        }
        int seenDot = string.indexOf(46);
        int length = string.length();
        if (seenDot >= 0) {
            cur_scale -= (length - seenDot) - 1;
            int length2 = length - 1;
            string = string.substring(0, seenDot) + string.substring(seenDot + 1);
        }
        int i = string.length();
        int initial_zeros = 0;
        while (initial_zeros < i - 1 && string.charAt(initial_zeros) == '0') {
            initial_zeros++;
        }
        if (initial_zeros > 0) {
            string = string.substring(initial_zeros);
            i -= initial_zeros;
        }
        int digits = i + cur_scale;
        if (this.width > 0) {
            while (digits < 0) {
                sbuf.append('0');
                digits++;
                i++;
            }
            decimals = ((this.width - signLen) - 1) - digits;
        } else {
            decimals = (i > 16 ? 16 : i) - digits;
        }
        if (decimals < 0) {
            decimals = 0;
        }
        sbuf.append(string);
        while (cur_scale > 0) {
            sbuf.append('0');
            cur_scale--;
            i++;
        }
        int digStart = oldSize + signLen;
        int digEnd = digStart + digits + decimals;
        int i2 = sbuf.length();
        if (digEnd >= i2) {
            digEnd = i2;
            nextDigit = '0';
        } else {
            nextDigit = sbuf.charAt(digEnd);
        }
        boolean addOne = nextDigit >= '5';
        char skip = addOne ? '9' : '0';
        while (digEnd > digStart + digits) {
            if (sbuf.charAt(digEnd - 1) != skip) {
                break;
            }
            digEnd--;
        }
        int length3 = digEnd - digStart;
        int decimals2 = length3 - digits;
        if (addOne && ExponentialFormat.addOne(sbuf, digStart, digEnd)) {
            digits++;
            decimals2 = 0;
            length3 = digits;
        }
        if (decimals2 == 0 && (this.width <= 0 || signLen + digits + 1 < this.width)) {
            decimals2 = 1;
            length3++;
            sbuf.insert(digStart + digits, '0');
        }
        sbuf.setLength(digStart + length3);
        format(sbuf, fpos, length3, digits, decimals2, negative ? 1 : 0, oldSize);
        return sbuf;
    }

    public StringBuffer format(Object num, StringBuffer sbuf, FieldPosition fpos) {
        RealNum rnum = RealNum.asRealNumOrNull(num);
        if (rnum == null) {
            if (num instanceof Complex) {
                String str = num.toString();
                int padding = this.width - str.length();
                while (true) {
                    padding--;
                    if (padding >= 0) {
                        sbuf.append(' ');
                    } else {
                        sbuf.append(str);
                        return sbuf;
                    }
                }
            } else {
                rnum = (RealNum) num;
            }
        }
        return format(rnum.doubleValue(), sbuf, fpos);
    }

    private void format(StringBuffer sbuf, FieldPosition fpos, int length, int digits, int decimals, int signLen, int oldSize) {
        int zero_digits;
        int i = digits + decimals;
        int zero_digits2 = getMinimumIntegerDigits();
        if (digits < 0 || digits <= zero_digits2) {
            zero_digits = zero_digits2 - digits;
        } else {
            zero_digits = 0;
        }
        if (digits + zero_digits <= 0 && (this.width <= 0 || this.width > decimals + 1 + signLen)) {
            zero_digits++;
        }
        int padding = this.width - (((signLen + length) + zero_digits) + 1);
        int i2 = zero_digits;
        while (true) {
            i2--;
            if (i2 < 0) {
                break;
            }
            sbuf.insert(oldSize + signLen, '0');
        }
        if (padding >= 0) {
            int i3 = oldSize;
            if (this.internalPad && signLen > 0) {
                i3++;
            }
            while (true) {
                padding--;
                if (padding < 0) {
                    break;
                }
                sbuf.insert(i3, this.padChar);
            }
        } else if (this.overflowChar != 0) {
            sbuf.setLength(oldSize);
            this.f229i = this.width;
            while (true) {
                int i4 = this.f229i - 1;
                this.f229i = i4;
                if (i4 >= 0) {
                    sbuf.append(this.overflowChar);
                } else {
                    return;
                }
            }
        }
        sbuf.insert(sbuf.length() - decimals, '.');
    }

    public Number parse(String text, ParsePosition status) {
        throw new Error("RealFixedFormat.parse - not implemented");
    }

    public Object parseObject(String text, ParsePosition status) {
        throw new Error("RealFixedFormat.parseObject - not implemented");
    }
}
