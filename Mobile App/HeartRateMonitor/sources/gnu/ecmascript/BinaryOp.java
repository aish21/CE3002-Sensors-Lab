package gnu.ecmascript;

import gnu.mapping.Procedure2;

public class BinaryOp extends Procedure2 {

    /* renamed from: op */
    int f44op;

    public BinaryOp(String name, int op) {
        setName(name);
        this.f44op = op;
    }

    public Object apply2(Object arg1, Object arg2) {
        if (this.f44op == 5) {
            return Convert.toNumber(arg1) < Convert.toNumber(arg2) ? Boolean.TRUE : Boolean.FALSE;
        }
        return new Double(apply(Convert.toNumber(arg1), Convert.toNumber(arg2)));
    }

    public double apply(double arg1, double arg2) {
        switch (this.f44op) {
            case 1:
                return arg1 + arg2;
            case 2:
                return arg1 - arg2;
            case 3:
                return arg1 * arg2;
            case 4:
                return (double) (((int) arg1) << (((int) arg2) & 31));
            default:
                return Double.NaN;
        }
    }
}
