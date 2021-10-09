package com.sun.mail.imap;

public final class SortTerm {
    public static final SortTerm ARRIVAL = new SortTerm("ARRIVAL");

    /* renamed from: CC */
    public static final SortTerm f274CC = new SortTerm("CC");
    public static final SortTerm DATE = new SortTerm("DATE");
    public static final SortTerm FROM = new SortTerm("FROM");
    public static final SortTerm REVERSE = new SortTerm("REVERSE");
    public static final SortTerm SIZE = new SortTerm("SIZE");
    public static final SortTerm SUBJECT = new SortTerm("SUBJECT");

    /* renamed from: TO */
    public static final SortTerm f275TO = new SortTerm("TO");
    private String term;

    private SortTerm(String term2) {
        this.term = term2;
    }

    public String toString() {
        return this.term;
    }
}
