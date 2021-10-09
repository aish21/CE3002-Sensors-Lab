package com.sun.mail.imap.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class UIDSet {
    public long end;
    public long start;

    public UIDSet() {
    }

    public UIDSet(long start2, long end2) {
        this.start = start2;
        this.end = end2;
    }

    public long size() {
        return (this.end - this.start) + 1;
    }

    public static UIDSet[] createUIDSets(long[] uids) {
        int j;
        if (uids == null) {
            return null;
        }
        List<UIDSet> v = new ArrayList<>();
        for (int i = 0; i < uids.length; i = (j - 1) + 1) {
            UIDSet ms = new UIDSet();
            ms.start = uids[i];
            j = i + 1;
            while (j < uids.length && uids[j] == uids[j - 1] + 1) {
                j++;
            }
            ms.end = uids[j - 1];
            v.add(ms);
        }
        return (UIDSet[]) v.toArray(new UIDSet[v.size()]);
    }

    public static UIDSet[] parseUIDSets(String uids) {
        UIDSet cur;
        if (uids == null) {
            return null;
        }
        List<UIDSet> v = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(uids, ",:", true);
        UIDSet cur2 = null;
        while (st.hasMoreTokens()) {
            try {
                String s = st.nextToken();
                if (s.equals(",")) {
                    if (cur2 != null) {
                        v.add(cur2);
                    }
                    cur = null;
                } else if (s.equals(":")) {
                    cur = cur2;
                } else {
                    long n = Long.parseLong(s);
                    if (cur2 != null) {
                        cur2.end = n;
                        cur = cur2;
                    } else {
                        cur = new UIDSet(n, n);
                    }
                }
                cur2 = cur;
            } catch (NumberFormatException e) {
            }
        }
        if (cur2 != null) {
            v.add(cur2);
        }
        return (UIDSet[]) v.toArray(new UIDSet[v.size()]);
    }

    public static String toString(UIDSet[] uidset) {
        if (uidset == null) {
            return null;
        }
        if (uidset.length == 0) {
            return "";
        }
        int i = 0;
        StringBuilder s = new StringBuilder();
        int size = uidset.length;
        while (true) {
            long start2 = uidset[i].start;
            long end2 = uidset[i].end;
            if (end2 > start2) {
                s.append(start2).append(':').append(end2);
            } else {
                s.append(start2);
            }
            i++;
            if (i >= size) {
                return s.toString();
            }
            s.append(',');
        }
    }

    public static long[] toArray(UIDSet[] uidset) {
        if (uidset == null) {
            return null;
        }
        long[] uids = new long[((int) size(uidset))];
        int i = 0;
        for (UIDSet u : uidset) {
            long n = u.start;
            while (n <= u.end) {
                uids[i] = n;
                n++;
                i++;
            }
        }
        return uids;
    }

    public static long[] toArray(UIDSet[] uidset, long uidmax) {
        if (uidset == null) {
            return null;
        }
        long[] uids = new long[((int) size(uidset, uidmax))];
        int i = 0;
        for (UIDSet u : uidset) {
            long n = u.start;
            while (n <= u.end && (uidmax < 0 || n <= uidmax)) {
                uids[i] = n;
                n++;
                i++;
            }
        }
        return uids;
    }

    public static long size(UIDSet[] uidset) {
        long count = 0;
        if (uidset != null) {
            for (UIDSet u : uidset) {
                count += u.size();
            }
        }
        return count;
    }

    private static long size(UIDSet[] uidset, long uidmax) {
        long count = 0;
        if (uidset != null) {
            for (UIDSet u : uidset) {
                if (uidmax < 0) {
                    count += u.size();
                } else if (u.start <= uidmax) {
                    if (u.end < uidmax) {
                        count += (u.end - u.start) + 1;
                    } else {
                        count += (uidmax - u.start) + 1;
                    }
                }
            }
        }
        return count;
    }
}
