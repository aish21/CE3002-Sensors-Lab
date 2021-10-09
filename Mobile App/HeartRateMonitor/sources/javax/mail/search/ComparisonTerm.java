package javax.mail.search;

public abstract class ComparisonTerm extends SearchTerm {

    /* renamed from: EQ */
    public static final int f308EQ = 3;

    /* renamed from: GE */
    public static final int f309GE = 6;

    /* renamed from: GT */
    public static final int f310GT = 5;

    /* renamed from: LE */
    public static final int f311LE = 1;

    /* renamed from: LT */
    public static final int f312LT = 2;

    /* renamed from: NE */
    public static final int f313NE = 4;
    private static final long serialVersionUID = 1456646953666474308L;
    protected int comparison;

    public boolean equals(Object obj) {
        if ((obj instanceof ComparisonTerm) && ((ComparisonTerm) obj).comparison == this.comparison) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.comparison;
    }
}
