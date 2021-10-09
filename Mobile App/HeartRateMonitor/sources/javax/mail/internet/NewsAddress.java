package javax.mail.internet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.mail.Address;

public class NewsAddress extends Address {
    private static final long serialVersionUID = -4203797299824684143L;
    protected String host;
    protected String newsgroup;

    public NewsAddress() {
    }

    public NewsAddress(String newsgroup2) {
        this(newsgroup2, (String) null);
    }

    public NewsAddress(String newsgroup2, String host2) {
        this.newsgroup = newsgroup2.replaceAll("\\s+", "");
        this.host = host2;
    }

    public String getType() {
        return "news";
    }

    public void setNewsgroup(String newsgroup2) {
        this.newsgroup = newsgroup2;
    }

    public String getNewsgroup() {
        return this.newsgroup;
    }

    public void setHost(String host2) {
        this.host = host2;
    }

    public String getHost() {
        return this.host;
    }

    public String toString() {
        return this.newsgroup;
    }

    public boolean equals(Object a) {
        if (!(a instanceof NewsAddress)) {
            return false;
        }
        NewsAddress s = (NewsAddress) a;
        if ((this.newsgroup != null || s.newsgroup != null) && (this.newsgroup == null || !this.newsgroup.equals(s.newsgroup))) {
            return false;
        }
        if ((this.host != null || s.host != null) && (this.host == null || s.host == null || !this.host.equalsIgnoreCase(s.host))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 0;
        if (this.newsgroup != null) {
            hash = 0 + this.newsgroup.hashCode();
        }
        if (this.host != null) {
            return hash + this.host.toLowerCase(Locale.ENGLISH).hashCode();
        }
        return hash;
    }

    public static String toString(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        StringBuilder s = new StringBuilder(addresses[0].toString());
        int used = s.length();
        for (int i = 1; i < addresses.length; i++) {
            s.append(",");
            int used2 = used + 1;
            String ng = addresses[i].toString();
            if (ng.length() + used2 > 76) {
                s.append("\r\n\t");
                used2 = 8;
            }
            s.append(ng);
            used = used2 + ng.length();
        }
        return s.toString();
    }

    public static NewsAddress[] parse(String newsgroups) throws AddressException {
        StringTokenizer st = new StringTokenizer(newsgroups, ",");
        List<NewsAddress> nglist = new ArrayList<>();
        while (st.hasMoreTokens()) {
            nglist.add(new NewsAddress(st.nextToken()));
        }
        return (NewsAddress[]) nglist.toArray(new NewsAddress[nglist.size()]);
    }
}
