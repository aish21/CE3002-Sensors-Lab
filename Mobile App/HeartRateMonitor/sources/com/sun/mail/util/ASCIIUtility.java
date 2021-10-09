package com.sun.mail.util;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ASCIIUtility {
    private ASCIIUtility() {
    }

    public static int parseInt(byte[] b, int start, int end, int radix) throws NumberFormatException {
        int limit;
        int i;
        if (b == null) {
            throw new NumberFormatException("null");
        }
        int result = 0;
        boolean negative = false;
        int i2 = start;
        if (end > start) {
            if (b[i2] == 45) {
                negative = true;
                limit = Integer.MIN_VALUE;
                i2++;
            } else {
                limit = -2147483647;
            }
            int multmin = limit / radix;
            if (i2 < end) {
                i = i2 + 1;
                int digit = Character.digit((char) b[i2], radix);
                if (digit < 0) {
                    throw new NumberFormatException("illegal number: " + toString(b, start, end));
                }
                result = -digit;
            } else {
                i = i2;
            }
            while (i < end) {
                int i3 = i + 1;
                int digit2 = Character.digit((char) b[i], radix);
                if (digit2 < 0) {
                    throw new NumberFormatException("illegal number");
                } else if (result < multmin) {
                    throw new NumberFormatException("illegal number");
                } else {
                    int result2 = result * radix;
                    if (result2 < limit + digit2) {
                        throw new NumberFormatException("illegal number");
                    }
                    result = result2 - digit2;
                    i = i3;
                }
            }
            if (!negative) {
                return -result;
            }
            if (i > start + 1) {
                return result;
            }
            throw new NumberFormatException("illegal number");
        }
        throw new NumberFormatException("illegal number");
    }

    public static int parseInt(byte[] b, int start, int end) throws NumberFormatException {
        return parseInt(b, start, end, 10);
    }

    public static long parseLong(byte[] b, int start, int end, int radix) throws NumberFormatException {
        long limit;
        int i;
        if (b == null) {
            throw new NumberFormatException("null");
        }
        long result = 0;
        boolean negative = false;
        int i2 = start;
        if (end > start) {
            if (b[i2] == 45) {
                negative = true;
                limit = Long.MIN_VALUE;
                i2++;
            } else {
                limit = -9223372036854775807L;
            }
            long multmin = limit / ((long) radix);
            if (i2 < end) {
                i = i2 + 1;
                int digit = Character.digit((char) b[i2], radix);
                if (digit < 0) {
                    throw new NumberFormatException("illegal number: " + toString(b, start, end));
                }
                result = (long) (-digit);
            } else {
                i = i2;
            }
            while (i < end) {
                int i3 = i + 1;
                int digit2 = Character.digit((char) b[i], radix);
                if (digit2 < 0) {
                    throw new NumberFormatException("illegal number");
                } else if (result < multmin) {
                    throw new NumberFormatException("illegal number");
                } else {
                    long result2 = result * ((long) radix);
                    if (result2 < ((long) digit2) + limit) {
                        throw new NumberFormatException("illegal number");
                    }
                    result = result2 - ((long) digit2);
                    i = i3;
                }
            }
            if (!negative) {
                return -result;
            }
            if (i > start + 1) {
                return result;
            }
            throw new NumberFormatException("illegal number");
        }
        throw new NumberFormatException("illegal number");
    }

    public static long parseLong(byte[] b, int start, int end) throws NumberFormatException {
        return parseLong(b, start, end, 10);
    }

    public static String toString(byte[] b, int start, int end) {
        int size = end - start;
        char[] theChars = new char[size];
        int j = start;
        for (int i = 0; i < size; i++) {
            theChars[i] = (char) (b[j] & Ev3Constants.Opcode.TST);
            j++;
        }
        return new String(theChars);
    }

    public static String toString(byte[] b) {
        return toString(b, 0, b.length);
    }

    public static String toString(ByteArrayInputStream is) {
        int size = is.available();
        char[] theChars = new char[size];
        byte[] bytes = new byte[size];
        is.read(bytes, 0, size);
        for (int i = 0; i < size; i++) {
            theChars[i] = (char) (bytes[i] & Ev3Constants.Opcode.TST);
        }
        return new String(theChars);
    }

    public static byte[] getBytes(String s) {
        char[] chars = s.toCharArray();
        int size = chars.length;
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        if (is instanceof ByteArrayInputStream) {
            int size = is.available();
            byte[] buf = new byte[size];
            int read = is.read(buf, 0, size);
            return buf;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf2 = new byte[1024];
        while (true) {
            int len = is.read(buf2, 0, 1024);
            if (len == -1) {
                return bos.toByteArray();
            }
            bos.write(buf2, 0, len);
        }
    }
}
