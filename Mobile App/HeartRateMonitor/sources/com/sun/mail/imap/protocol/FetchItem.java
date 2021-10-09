package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import javax.mail.FetchProfile;

public abstract class FetchItem {
    private FetchProfile.Item fetchProfileItem;
    private String name;

    public abstract Object parseItem(FetchResponse fetchResponse) throws ParsingException;

    public FetchItem(String name2, FetchProfile.Item fetchProfileItem2) {
        this.name = name2;
        this.fetchProfileItem = fetchProfileItem2;
    }

    public String getName() {
        return this.name;
    }

    public FetchProfile.Item getFetchProfileItem() {
        return this.fetchProfileItem;
    }
}
