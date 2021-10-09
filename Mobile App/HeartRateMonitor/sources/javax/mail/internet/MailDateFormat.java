package javax.mail.internet;

import com.sun.mail.util.MailLogger;
import gnu.bytecode.Access;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;

public class MailDateFormat extends SimpleDateFormat {
    private static final int LEAP_SECOND = 60;
    /* access modifiers changed from: private */
    public static final MailLogger LOGGER = new MailLogger((Class<?>) MailDateFormat.class, "DEBUG", false, System.out);
    private static final String PATTERN = "EEE, d MMM yyyy HH:mm:ss Z (z)";
    private static final int UNKNOWN_DAY_NAME = -1;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final long serialVersionUID = -8148227605210628779L;

    public MailDateFormat() {
        super(PATTERN, Locale.US);
    }

    private Object writeReplace() throws ObjectStreamException {
        MailDateFormat fmt = new MailDateFormat();
        fmt.superApplyPattern("EEE, d MMM yyyy HH:mm:ss 'XXXXX' (z)");
        fmt.setTimeZone(getTimeZone());
        return fmt;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        super.applyPattern(PATTERN);
    }

    public MailDateFormat clone() {
        return (MailDateFormat) super.clone();
    }

    public StringBuffer format(Date date, StringBuffer dateStrBuf, FieldPosition fieldPosition) {
        return super.format(date, dateStrBuf, fieldPosition);
    }

    public Date parse(String text, ParsePosition pos) {
        if (text == null || pos == null) {
            throw new NullPointerException();
        } else if (pos.getIndex() < 0 || pos.getIndex() >= text.length()) {
            return null;
        } else {
            if (isLenient()) {
                return new Rfc2822LenientParser(text, pos).parse();
            }
            return new Rfc2822StrictParser(text, pos).parse();
        }
    }

    public void setCalendar(Calendar newCalendar) {
        throw new UnsupportedOperationException("Method setCalendar() shouldn't be called");
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        throw new UnsupportedOperationException("Method setNumberFormat() shouldn't be called");
    }

    public void applyLocalizedPattern(String pattern) {
        throw new UnsupportedOperationException("Method applyLocalizedPattern() shouldn't be called");
    }

    public void applyPattern(String pattern) {
        throw new UnsupportedOperationException("Method applyPattern() shouldn't be called");
    }

    private void superApplyPattern(String pattern) {
        super.applyPattern(pattern);
    }

    public Date get2DigitYearStart() {
        throw new UnsupportedOperationException("Method get2DigitYearStart() shouldn't be called");
    }

    public void set2DigitYearStart(Date startDate) {
        throw new UnsupportedOperationException("Method set2DigitYearStart() shouldn't be called");
    }

    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        throw new UnsupportedOperationException("Method setDateFormatSymbols() shouldn't be called");
    }

    /* access modifiers changed from: private */
    public Date toDate(int dayName, int day, int month, int year, int hour, int minute, int second, int zone) {
        if (second == 60) {
            second = 59;
        }
        TimeZone tz = this.calendar.getTimeZone();
        try {
            this.calendar.setTimeZone(UTC);
            this.calendar.clear();
            this.calendar.set(year, month, day, hour, minute, second);
            if (dayName == -1 || dayName == this.calendar.get(7)) {
                this.calendar.add(12, zone);
                return this.calendar.getTime();
            }
            throw new IllegalArgumentException("Inconsistent day-name");
        } finally {
            this.calendar.setTimeZone(tz);
        }
    }

    private static abstract class AbstractDateParser {
        static final int INVALID_CHAR = -1;
        static final int MAX_YEAR_DIGITS = 8;
        final ParsePosition pos;
        final String text;

        /* access modifiers changed from: package-private */
        public abstract Date tryParse() throws ParseException;

        AbstractDateParser(String text2, ParsePosition pos2) {
            this.text = text2;
            this.pos = pos2;
        }

        /* access modifiers changed from: package-private */
        public final Date parse() {
            int startPosition = this.pos.getIndex();
            try {
                return tryParse();
            } catch (Exception e) {
                if (MailDateFormat.LOGGER.isLoggable(Level.FINE)) {
                    MailDateFormat.LOGGER.log(Level.FINE, "Bad date: '" + this.text + "'", (Throwable) e);
                }
                this.pos.setErrorIndex(this.pos.getIndex());
                this.pos.setIndex(startPosition);
                return null;
            }
        }

        /* access modifiers changed from: package-private */
        public final int parseDayName() throws ParseException {
            switch (getChar()) {
                case -1:
                    throw new ParseException("Invalid day-name", this.pos.getIndex());
                case 70:
                    if (skipPair('r', 'i')) {
                        return 6;
                    }
                    break;
                case 77:
                    if (skipPair('o', 'n')) {
                        return 2;
                    }
                    break;
                case 83:
                    if (skipPair('u', 'n')) {
                        return 1;
                    }
                    if (skipPair('a', 't')) {
                        return 7;
                    }
                    break;
                case 84:
                    if (skipPair('u', 'e')) {
                        return 3;
                    }
                    if (skipPair('h', 'u')) {
                        return 5;
                    }
                    break;
                case 87:
                    if (skipPair('e', 'd')) {
                        return 4;
                    }
                    break;
            }
            this.pos.setIndex(this.pos.getIndex() - 1);
            throw new ParseException("Invalid day-name", this.pos.getIndex());
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x008e, code lost:
            if (r8 == false) goto L_0x0090;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d7, code lost:
            if (r8 == false) goto L_0x00d9;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:4:0x002c, code lost:
            if (r8 == false) goto L_0x002e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x010f, code lost:
            if (r8 == false) goto L_0x0111;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x0125, code lost:
            if (r8 == false) goto L_0x0127;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:75:0x0141, code lost:
            if (r8 == false) goto L_0x0143;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:82:0x0157, code lost:
            if (r8 == false) goto L_0x0159;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:89:0x0177, code lost:
            if (r8 == false) goto L_0x0179;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public final int parseMonthName(boolean r8) throws java.text.ParseException {
            /*
                r7 = this;
                r6 = 97
                r5 = 69
                r4 = 112(0x70, float:1.57E-43)
                r3 = 99
                r2 = 101(0x65, float:1.42E-43)
                int r0 = r7.getChar()
                switch(r0) {
                    case -1: goto L_0x0190;
                    case 65: goto L_0x00d9;
                    case 68: goto L_0x0111;
                    case 70: goto L_0x0179;
                    case 74: goto L_0x002e;
                    case 77: goto L_0x0090;
                    case 78: goto L_0x0159;
                    case 79: goto L_0x0127;
                    case 83: goto L_0x0143;
                    case 97: goto L_0x00d7;
                    case 100: goto L_0x010f;
                    case 102: goto L_0x0177;
                    case 106: goto L_0x002c;
                    case 109: goto L_0x008e;
                    case 110: goto L_0x0157;
                    case 111: goto L_0x0125;
                    case 115: goto L_0x0141;
                    default: goto L_0x0011;
                }
            L_0x0011:
                java.text.ParsePosition r0 = r7.pos
                java.text.ParsePosition r1 = r7.pos
                int r1 = r1.getIndex()
                int r1 = r1 + -1
                r0.setIndex(r1)
                java.text.ParseException r0 = new java.text.ParseException
                java.lang.String r1 = "Invalid month"
                java.text.ParsePosition r2 = r7.pos
                int r2 = r2.getIndex()
                r0.<init>(r1, r2)
                throw r0
            L_0x002c:
                if (r8 != 0) goto L_0x0011
            L_0x002e:
                r0 = 117(0x75, float:1.64E-43)
                boolean r0 = r7.skipChar(r0)
                if (r0 != 0) goto L_0x0040
                if (r8 != 0) goto L_0x0076
                r0 = 85
                boolean r0 = r7.skipChar(r0)
                if (r0 == 0) goto L_0x0076
            L_0x0040:
                r0 = 108(0x6c, float:1.51E-43)
                boolean r0 = r7.skipChar(r0)
                if (r0 != 0) goto L_0x0052
                if (r8 != 0) goto L_0x0054
                r0 = 76
                boolean r0 = r7.skipChar(r0)
                if (r0 == 0) goto L_0x0054
            L_0x0052:
                r0 = 6
            L_0x0053:
                return r0
            L_0x0054:
                r0 = 110(0x6e, float:1.54E-43)
                boolean r0 = r7.skipChar(r0)
                if (r0 != 0) goto L_0x0066
                if (r8 != 0) goto L_0x0068
                r0 = 78
                boolean r0 = r7.skipChar(r0)
                if (r0 == 0) goto L_0x0068
            L_0x0066:
                r0 = 5
                goto L_0x0053
            L_0x0068:
                java.text.ParsePosition r0 = r7.pos
                java.text.ParsePosition r1 = r7.pos
                int r1 = r1.getIndex()
                int r1 = r1 + -1
                r0.setIndex(r1)
                goto L_0x0011
            L_0x0076:
                r0 = 110(0x6e, float:1.54E-43)
                boolean r0 = r7.skipPair(r6, r0)
                if (r0 != 0) goto L_0x008c
                if (r8 != 0) goto L_0x0011
                r0 = 65
                r1 = 110(0x6e, float:1.54E-43)
                r2 = 78
                boolean r0 = r7.skipAlternativePair(r6, r0, r1, r2)
                if (r0 == 0) goto L_0x0011
            L_0x008c:
                r0 = 0
                goto L_0x0053
            L_0x008e:
                if (r8 != 0) goto L_0x0011
            L_0x0090:
                boolean r0 = r7.skipChar(r6)
                if (r0 != 0) goto L_0x00a0
                if (r8 != 0) goto L_0x0011
                r0 = 65
                boolean r0 = r7.skipChar(r0)
                if (r0 == 0) goto L_0x0011
            L_0x00a0:
                r0 = 114(0x72, float:1.6E-43)
                boolean r0 = r7.skipChar(r0)
                if (r0 != 0) goto L_0x00b2
                if (r8 != 0) goto L_0x00b4
                r0 = 82
                boolean r0 = r7.skipChar(r0)
                if (r0 == 0) goto L_0x00b4
            L_0x00b2:
                r0 = 2
                goto L_0x0053
            L_0x00b4:
                r0 = 121(0x79, float:1.7E-43)
                boolean r0 = r7.skipChar(r0)
                if (r0 != 0) goto L_0x00c6
                if (r8 != 0) goto L_0x00c8
                r0 = 89
                boolean r0 = r7.skipChar(r0)
                if (r0 == 0) goto L_0x00c8
            L_0x00c6:
                r0 = 4
                goto L_0x0053
            L_0x00c8:
                java.text.ParsePosition r0 = r7.pos
                java.text.ParsePosition r1 = r7.pos
                int r1 = r1.getIndex()
                int r1 = r1 + -1
                r0.setIndex(r1)
                goto L_0x0011
            L_0x00d7:
                if (r8 != 0) goto L_0x0011
            L_0x00d9:
                r0 = 117(0x75, float:1.64E-43)
                r1 = 103(0x67, float:1.44E-43)
                boolean r0 = r7.skipPair(r0, r1)
                if (r0 != 0) goto L_0x00f3
                if (r8 != 0) goto L_0x00f6
                r0 = 117(0x75, float:1.64E-43)
                r1 = 85
                r2 = 103(0x67, float:1.44E-43)
                r3 = 71
                boolean r0 = r7.skipAlternativePair(r0, r1, r2, r3)
                if (r0 == 0) goto L_0x00f6
            L_0x00f3:
                r0 = 7
                goto L_0x0053
            L_0x00f6:
                r0 = 114(0x72, float:1.6E-43)
                boolean r0 = r7.skipPair(r4, r0)
                if (r0 != 0) goto L_0x010c
                if (r8 != 0) goto L_0x0011
                r0 = 80
                r1 = 114(0x72, float:1.6E-43)
                r2 = 82
                boolean r0 = r7.skipAlternativePair(r4, r0, r1, r2)
                if (r0 == 0) goto L_0x0011
            L_0x010c:
                r0 = 3
                goto L_0x0053
            L_0x010f:
                if (r8 != 0) goto L_0x0011
            L_0x0111:
                boolean r0 = r7.skipPair(r2, r3)
                if (r0 != 0) goto L_0x0121
                if (r8 != 0) goto L_0x0011
                r0 = 67
                boolean r0 = r7.skipAlternativePair(r2, r5, r3, r0)
                if (r0 == 0) goto L_0x0011
            L_0x0121:
                r0 = 11
                goto L_0x0053
            L_0x0125:
                if (r8 != 0) goto L_0x0011
            L_0x0127:
                r0 = 116(0x74, float:1.63E-43)
                boolean r0 = r7.skipPair(r3, r0)
                if (r0 != 0) goto L_0x013d
                if (r8 != 0) goto L_0x0011
                r0 = 67
                r1 = 116(0x74, float:1.63E-43)
                r2 = 84
                boolean r0 = r7.skipAlternativePair(r3, r0, r1, r2)
                if (r0 == 0) goto L_0x0011
            L_0x013d:
                r0 = 9
                goto L_0x0053
            L_0x0141:
                if (r8 != 0) goto L_0x0011
            L_0x0143:
                boolean r0 = r7.skipPair(r2, r4)
                if (r0 != 0) goto L_0x0153
                if (r8 != 0) goto L_0x0011
                r0 = 80
                boolean r0 = r7.skipAlternativePair(r2, r5, r4, r0)
                if (r0 == 0) goto L_0x0011
            L_0x0153:
                r0 = 8
                goto L_0x0053
            L_0x0157:
                if (r8 != 0) goto L_0x0011
            L_0x0159:
                r0 = 111(0x6f, float:1.56E-43)
                r1 = 118(0x76, float:1.65E-43)
                boolean r0 = r7.skipPair(r0, r1)
                if (r0 != 0) goto L_0x0173
                if (r8 != 0) goto L_0x0011
                r0 = 111(0x6f, float:1.56E-43)
                r1 = 79
                r2 = 118(0x76, float:1.65E-43)
                r3 = 86
                boolean r0 = r7.skipAlternativePair(r0, r1, r2, r3)
                if (r0 == 0) goto L_0x0011
            L_0x0173:
                r0 = 10
                goto L_0x0053
            L_0x0177:
                if (r8 != 0) goto L_0x0011
            L_0x0179:
                r0 = 98
                boolean r0 = r7.skipPair(r2, r0)
                if (r0 != 0) goto L_0x018d
                if (r8 != 0) goto L_0x0011
                r0 = 98
                r1 = 66
                boolean r0 = r7.skipAlternativePair(r2, r5, r0, r1)
                if (r0 == 0) goto L_0x0011
            L_0x018d:
                r0 = 1
                goto L_0x0053
            L_0x0190:
                java.text.ParseException r0 = new java.text.ParseException
                java.lang.String r1 = "Invalid month"
                java.text.ParsePosition r2 = r7.pos
                int r2 = r2.getIndex()
                r0.<init>(r1, r2)
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MailDateFormat.AbstractDateParser.parseMonthName(boolean):int");
        }

        /* access modifiers changed from: package-private */
        public final int parseZoneOffset() throws ParseException {
            int i = -1;
            int sign = getChar();
            if (sign == 43 || sign == 45) {
                int offset = parseAsciiDigits(4, 4, true);
                if (!isValidZoneOffset(offset)) {
                    this.pos.setIndex(this.pos.getIndex() - 5);
                    throw new ParseException("Invalid zone", this.pos.getIndex());
                }
                if (sign != 43) {
                    i = 1;
                }
                return i * (((offset / 100) * 60) + (offset % 100));
            }
            if (sign != -1) {
                this.pos.setIndex(this.pos.getIndex() - 1);
            }
            throw new ParseException("Invalid zone", this.pos.getIndex());
        }

        /* access modifiers changed from: package-private */
        public boolean isValidZoneOffset(int offset) {
            return offset % 100 < 60;
        }

        /* access modifiers changed from: package-private */
        public final int parseAsciiDigits(int count) throws ParseException {
            return parseAsciiDigits(count, count);
        }

        /* access modifiers changed from: package-private */
        public final int parseAsciiDigits(int min, int max) throws ParseException {
            return parseAsciiDigits(min, max, false);
        }

        /* access modifiers changed from: package-private */
        public final int parseAsciiDigits(int min, int max, boolean isEOF) throws ParseException {
            String range;
            int result = 0;
            int nbDigitsParsed = 0;
            while (nbDigitsParsed < max && peekAsciiDigit()) {
                result = (result * 10) + getAsciiDigit();
                nbDigitsParsed++;
            }
            if (nbDigitsParsed >= min && (nbDigitsParsed != max || isEOF || !peekAsciiDigit())) {
                return result;
            }
            this.pos.setIndex(this.pos.getIndex() - nbDigitsParsed);
            if (min == max) {
                range = Integer.toString(min);
            } else {
                range = "between " + min + " and " + max;
            }
            throw new ParseException("Invalid input: expected " + range + " ASCII digits", this.pos.getIndex());
        }

        /* access modifiers changed from: package-private */
        public final void parseFoldingWhiteSpace() throws ParseException {
            if (!skipFoldingWhiteSpace()) {
                throw new ParseException("Invalid input: expected FWS", this.pos.getIndex());
            }
        }

        /* access modifiers changed from: package-private */
        public final void parseChar(char ch) throws ParseException {
            if (!skipChar(ch)) {
                throw new ParseException("Invalid input: expected '" + ch + "'", this.pos.getIndex());
            }
        }

        /* access modifiers changed from: package-private */
        public final int getAsciiDigit() {
            int ch = getChar();
            if (48 <= ch && ch <= 57) {
                return Character.digit((char) ch, 10);
            }
            if (ch == -1) {
                return -1;
            }
            this.pos.setIndex(this.pos.getIndex() - 1);
            return -1;
        }

        /* access modifiers changed from: package-private */
        public final int getChar() {
            if (this.pos.getIndex() >= this.text.length()) {
                return -1;
            }
            char ch = this.text.charAt(this.pos.getIndex());
            this.pos.setIndex(this.pos.getIndex() + 1);
            return ch;
        }

        /* access modifiers changed from: package-private */
        public boolean skipFoldingWhiteSpace() {
            if (skipChar(' ')) {
                if (!peekFoldingWhiteSpace()) {
                    return true;
                }
                this.pos.setIndex(this.pos.getIndex() - 1);
            } else if (!peekFoldingWhiteSpace()) {
                return false;
            }
            int startIndex = this.pos.getIndex();
            if (skipWhiteSpace()) {
                while (skipNewline()) {
                    if (!skipWhiteSpace()) {
                        this.pos.setIndex(startIndex);
                        return false;
                    }
                }
                return true;
            } else if (skipNewline() && skipWhiteSpace()) {
                return true;
            } else {
                this.pos.setIndex(startIndex);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public final boolean skipWhiteSpace() {
            int startIndex = this.pos.getIndex();
            do {
            } while (skipAlternative(' ', 9));
            return this.pos.getIndex() > startIndex;
        }

        /* access modifiers changed from: package-private */
        public final boolean skipNewline() {
            return skipPair(13, 10);
        }

        /* access modifiers changed from: package-private */
        public final boolean skipAlternativeTriple(char firstStandard, char firstAlternative, char secondStandard, char secondAlternative, char thirdStandard, char thirdAlternative) {
            if (skipAlternativePair(firstStandard, firstAlternative, secondStandard, secondAlternative)) {
                if (skipAlternative(thirdStandard, thirdAlternative)) {
                    return true;
                }
                this.pos.setIndex(this.pos.getIndex() - 2);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public final boolean skipAlternativePair(char firstStandard, char firstAlternative, char secondStandard, char secondAlternative) {
            if (skipAlternative(firstStandard, firstAlternative)) {
                if (skipAlternative(secondStandard, secondAlternative)) {
                    return true;
                }
                this.pos.setIndex(this.pos.getIndex() - 1);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public final boolean skipAlternative(char standard, char alternative) {
            return skipChar(standard) || skipChar(alternative);
        }

        /* access modifiers changed from: package-private */
        public final boolean skipPair(char first, char second) {
            if (skipChar(first)) {
                if (skipChar(second)) {
                    return true;
                }
                this.pos.setIndex(this.pos.getIndex() - 1);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public final boolean skipChar(char ch) {
            if (this.pos.getIndex() >= this.text.length() || this.text.charAt(this.pos.getIndex()) != ch) {
                return false;
            }
            this.pos.setIndex(this.pos.getIndex() + 1);
            return true;
        }

        /* access modifiers changed from: package-private */
        public final boolean peekAsciiDigit() {
            return this.pos.getIndex() < this.text.length() && '0' <= this.text.charAt(this.pos.getIndex()) && this.text.charAt(this.pos.getIndex()) <= '9';
        }

        /* access modifiers changed from: package-private */
        public boolean peekFoldingWhiteSpace() {
            return this.pos.getIndex() < this.text.length() && (this.text.charAt(this.pos.getIndex()) == ' ' || this.text.charAt(this.pos.getIndex()) == 9 || this.text.charAt(this.pos.getIndex()) == 13);
        }

        /* access modifiers changed from: package-private */
        public final boolean peekChar(char ch) {
            return this.pos.getIndex() < this.text.length() && this.text.charAt(this.pos.getIndex()) == ch;
        }
    }

    private class Rfc2822StrictParser extends AbstractDateParser {
        Rfc2822StrictParser(String text, ParsePosition pos) {
            super(text, pos);
        }

        /* access modifiers changed from: package-private */
        public Date tryParse() throws ParseException {
            int dayName = parseOptionalBegin();
            int day = parseDay();
            int month = parseMonth();
            int year = parseYear();
            parseFoldingWhiteSpace();
            int hour = parseHour();
            parseChar(':');
            int minute = parseMinute();
            int second = skipChar(':') ? parseSecond() : 0;
            parseFwsBetweenTimeOfDayAndZone();
            try {
                return MailDateFormat.this.toDate(dayName, day, month, year, hour, minute, second, parseZone());
            } catch (IllegalArgumentException e) {
                throw new ParseException("Invalid input: some of the calendar fields have invalid values, or day-name is inconsistent with date", this.pos.getIndex());
            }
        }

        /* access modifiers changed from: package-private */
        public int parseOptionalBegin() throws ParseException {
            if (peekAsciiDigit()) {
                return -1;
            }
            skipFoldingWhiteSpace();
            int dayName = parseDayName();
            parseChar(',');
            return dayName;
        }

        /* access modifiers changed from: package-private */
        public int parseDay() throws ParseException {
            skipFoldingWhiteSpace();
            return parseAsciiDigits(1, 2);
        }

        /* access modifiers changed from: package-private */
        public int parseMonth() throws ParseException {
            parseFwsInMonth();
            int month = parseMonthName(isMonthNameCaseSensitive());
            parseFwsInMonth();
            return month;
        }

        /* access modifiers changed from: package-private */
        public void parseFwsInMonth() throws ParseException {
            parseFoldingWhiteSpace();
        }

        /* access modifiers changed from: package-private */
        public boolean isMonthNameCaseSensitive() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public int parseYear() throws ParseException {
            int year = parseAsciiDigits(4, 8);
            if (year >= 1900) {
                return year;
            }
            this.pos.setIndex(this.pos.getIndex() - 4);
            while (this.text.charAt(this.pos.getIndex() - 1) == '0') {
                this.pos.setIndex(this.pos.getIndex() - 1);
            }
            throw new ParseException("Invalid year", this.pos.getIndex());
        }

        /* access modifiers changed from: package-private */
        public int parseHour() throws ParseException {
            return parseAsciiDigits(2);
        }

        /* access modifiers changed from: package-private */
        public int parseMinute() throws ParseException {
            return parseAsciiDigits(2);
        }

        /* access modifiers changed from: package-private */
        public int parseSecond() throws ParseException {
            return parseAsciiDigits(2);
        }

        /* access modifiers changed from: package-private */
        public void parseFwsBetweenTimeOfDayAndZone() throws ParseException {
            parseFoldingWhiteSpace();
        }

        /* access modifiers changed from: package-private */
        public int parseZone() throws ParseException {
            return parseZoneOffset();
        }
    }

    private class Rfc2822LenientParser extends Rfc2822StrictParser {
        private Boolean hasDefaultFws;

        Rfc2822LenientParser(String text, ParsePosition pos) {
            super(text, pos);
        }

        /* access modifiers changed from: package-private */
        public int parseOptionalBegin() {
            while (this.pos.getIndex() < this.text.length() && !peekAsciiDigit()) {
                this.pos.setIndex(this.pos.getIndex() + 1);
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public int parseDay() throws ParseException {
            skipFoldingWhiteSpace();
            return parseAsciiDigits(1, 3);
        }

        /* access modifiers changed from: package-private */
        public void parseFwsInMonth() throws ParseException {
            if (this.hasDefaultFws == null) {
                this.hasDefaultFws = Boolean.valueOf(!skipChar('-'));
                skipFoldingWhiteSpace();
            } else if (this.hasDefaultFws.booleanValue()) {
                skipFoldingWhiteSpace();
            } else {
                parseChar('-');
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isMonthNameCaseSensitive() {
            return false;
        }

        /* access modifiers changed from: package-private */
        public int parseYear() throws ParseException {
            int year = parseAsciiDigits(1, 8);
            if (year >= 1000) {
                return year;
            }
            if (year >= 50) {
                return year + 1900;
            }
            return year + 2000;
        }

        /* access modifiers changed from: package-private */
        public int parseHour() throws ParseException {
            return parseAsciiDigits(1, 2);
        }

        /* access modifiers changed from: package-private */
        public int parseMinute() throws ParseException {
            return parseAsciiDigits(1, 2);
        }

        /* access modifiers changed from: package-private */
        public int parseSecond() throws ParseException {
            return parseAsciiDigits(1, 2);
        }

        /* access modifiers changed from: package-private */
        public void parseFwsBetweenTimeOfDayAndZone() throws ParseException {
            skipFoldingWhiteSpace();
        }

        /* access modifiers changed from: package-private */
        public int parseZone() throws ParseException {
            int hoursOffset;
            try {
                if (this.pos.getIndex() >= this.text.length()) {
                    throw new ParseException("Missing zone", this.pos.getIndex());
                } else if (peekChar('+') || peekChar('-')) {
                    return parseZoneOffset();
                } else {
                    if (skipAlternativePair('U', 'u', 'T', 't')) {
                        return 0;
                    }
                    if (skipAlternativeTriple('G', 'g', Access.METHOD_CONTEXT, 'm', 'T', 't')) {
                        return 0;
                    }
                    if (skipAlternative('E', 'e')) {
                        hoursOffset = 4;
                    } else if (skipAlternative(Access.CLASS_CONTEXT, 'c')) {
                        hoursOffset = 5;
                    } else if (skipAlternative(Access.METHOD_CONTEXT, 'm')) {
                        hoursOffset = 6;
                    } else if (skipAlternative('P', 'p')) {
                        hoursOffset = 7;
                    } else {
                        throw new ParseException("Invalid zone", this.pos.getIndex());
                    }
                    if (skipAlternativePair('S', 's', 'T', 't')) {
                        hoursOffset++;
                    } else if (!skipAlternativePair('D', 'd', 'T', 't')) {
                        this.pos.setIndex(this.pos.getIndex() - 1);
                        throw new ParseException("Invalid zone", this.pos.getIndex());
                    }
                    return hoursOffset * 60;
                }
            } catch (ParseException e) {
                if (MailDateFormat.LOGGER.isLoggable(Level.FINE)) {
                    MailDateFormat.LOGGER.log(Level.FINE, "No timezone? : '" + this.text + "'", (Throwable) e);
                }
                return 0;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isValidZoneOffset(int offset) {
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean skipFoldingWhiteSpace() {
            boolean result = peekFoldingWhiteSpace();
            while (this.pos.getIndex() < this.text.length()) {
                switch (this.text.charAt(this.pos.getIndex())) {
                    case 9:
                    case 10:
                    case 13:
                    case ' ':
                        this.pos.setIndex(this.pos.getIndex() + 1);
                }
                return result;
            }
            return result;
        }

        /* access modifiers changed from: package-private */
        public boolean peekFoldingWhiteSpace() {
            return super.peekFoldingWhiteSpace() || (this.pos.getIndex() < this.text.length() && this.text.charAt(this.pos.getIndex()) == 10);
        }
    }
}
