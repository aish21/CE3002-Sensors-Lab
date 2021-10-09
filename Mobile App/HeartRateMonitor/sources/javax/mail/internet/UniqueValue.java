package javax.mail.internet;

import java.util.concurrent.atomic.AtomicInteger;
import javax.mail.Session;

class UniqueValue {

    /* renamed from: id */
    private static AtomicInteger f307id = new AtomicInteger();

    UniqueValue() {
    }

    public static String getUniqueBoundaryValue() {
        StringBuilder s = new StringBuilder();
        s.append("----=_Part_").append(f307id.getAndIncrement()).append("_").append((long) s.hashCode()).append('.').append(System.currentTimeMillis());
        return s.toString();
    }

    public static String getUniqueMessageIDValue(Session ssn) {
        String suffix;
        InternetAddress addr = InternetAddress.getLocalAddress(ssn);
        if (addr != null) {
            suffix = addr.getAddress();
        } else {
            suffix = "jakartamailuser@localhost";
        }
        int at = suffix.lastIndexOf(64);
        if (at >= 0) {
            suffix = suffix.substring(at);
        }
        StringBuilder s = new StringBuilder();
        s.append(s.hashCode()).append('.').append(f307id.getAndIncrement()).append('.').append(System.currentTimeMillis()).append(suffix);
        return s.toString();
    }
}
