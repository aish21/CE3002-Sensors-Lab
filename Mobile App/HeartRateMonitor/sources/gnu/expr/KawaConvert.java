package gnu.expr;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import gnu.lists.Convert;
import gnu.math.DFloNum;
import gnu.math.IntNum;
import gnu.text.Char;
import javax.mail.UIDFolder;

public class KawaConvert extends Convert {
    public static Convert instance = new KawaConvert();

    public static Convert getInstance() {
        return instance;
    }

    public static void setInstance(Convert value) {
        instance = value;
    }

    public Object charToObject(char ch) {
        return Char.make(ch);
    }

    public char objectToChar(Object obj) {
        return ((Char) obj).charValue();
    }

    public Object byteToObject(byte value) {
        return IntNum.make((int) value);
    }

    public Object shortToObject(short value) {
        return IntNum.make((int) value);
    }

    public Object intToObject(int value) {
        return IntNum.make(value);
    }

    public Object longToObject(long value) {
        return IntNum.make(value);
    }

    public Object byteToObjectUnsigned(byte value) {
        return IntNum.make((int) value & Ev3Constants.Opcode.TST);
    }

    public Object shortToObjectUnsigned(short value) {
        return IntNum.make((int) 65535 & value);
    }

    public Object intToObjectUnsigned(int value) {
        return IntNum.make(((long) value) & UIDFolder.MAXUID);
    }

    public Object longToObjectUnsigned(long value) {
        return IntNum.makeU(value);
    }

    public Object floatToObject(float value) {
        return DFloNum.make((double) value);
    }

    public Object doubleToObject(double value) {
        return DFloNum.make(value);
    }
}
