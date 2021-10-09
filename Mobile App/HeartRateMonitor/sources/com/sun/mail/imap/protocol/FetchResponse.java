package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.util.ASCIIUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FetchResponse extends IMAPResponse {
    private static final char[] HEADER = {'.', 'H', 'E', 'A', 'D', 'E', 'R'};
    private static final char[] TEXT = {'.', 'T', 'E', 'X', 'T'};
    private Map<String, Object> extensionItems;
    private final FetchItem[] fitems;
    private Item[] items;

    public FetchResponse(Protocol p) throws IOException, ProtocolException {
        super(p);
        this.fitems = null;
        parse();
    }

    public FetchResponse(IMAPResponse r) throws IOException, ProtocolException {
        this(r, (FetchItem[]) null);
    }

    public FetchResponse(IMAPResponse r, FetchItem[] fitems2) throws IOException, ProtocolException {
        super(r);
        this.fitems = fitems2;
        parse();
    }

    public int getItemCount() {
        return this.items.length;
    }

    public Item getItem(int index) {
        return this.items[index];
    }

    public <T extends Item> T getItem(Class<T> c) {
        for (int i = 0; i < this.items.length; i++) {
            if (c.isInstance(this.items[i])) {
                return (Item) c.cast(this.items[i]);
            }
        }
        return null;
    }

    public static <T extends Item> T getItem(Response[] r, int msgno, Class<T> c) {
        if (r == null) {
            return null;
        }
        for (int i = 0; i < r.length; i++) {
            if (r[i] != null && (r[i] instanceof FetchResponse) && r[i].getNumber() == msgno) {
                FetchResponse f = r[i];
                for (int j = 0; j < f.items.length; j++) {
                    if (c.isInstance(f.items[j])) {
                        return (Item) c.cast(f.items[j]);
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static <T extends Item> List<T> getItems(Response[] r, int msgno, Class<T> c) {
        List<T> items2 = new ArrayList<>();
        if (r != null) {
            for (int i = 0; i < r.length; i++) {
                if (r[i] != null && (r[i] instanceof FetchResponse) && r[i].getNumber() == msgno) {
                    FetchResponse f = r[i];
                    for (int j = 0; j < f.items.length; j++) {
                        if (c.isInstance(f.items[j])) {
                            items2.add(c.cast(f.items[j]));
                        }
                    }
                }
            }
        }
        return items2;
    }

    public Map<String, Object> getExtensionItems() {
        return this.extensionItems;
    }

    private void parse() throws ParsingException {
        if (!isNextNonSpace('(')) {
            throw new ParsingException("error in FETCH parsing, missing '(' at index " + this.index);
        }
        List<Item> v = new ArrayList<>();
        skipSpaces();
        while (this.index < this.size) {
            Item i = parseItem();
            if (i != null) {
                v.add(i);
            } else if (!parseExtensionItem()) {
                throw new ParsingException("error in FETCH parsing, unrecognized item at index " + this.index + ", starts with \"" + next20() + "\"");
            }
            if (isNextNonSpace(')')) {
                this.items = (Item[]) v.toArray(new Item[v.size()]);
                return;
            }
        }
        throw new ParsingException("error in FETCH parsing, ran off end of buffer, size " + this.size);
    }

    private String next20() {
        if (this.index + 20 > this.size) {
            return ASCIIUtility.toString(this.buffer, this.index, this.size);
        }
        return ASCIIUtility.toString(this.buffer, this.index, this.index + 20) + "...";
    }

    private Item parseItem() throws ParsingException {
        switch (this.buffer[this.index]) {
            case 66:
            case 98:
                if (match(BODYSTRUCTURE.name)) {
                    return new BODYSTRUCTURE(this);
                }
                if (match(BODY.name)) {
                    if (this.buffer[this.index] == 91) {
                        return new BODY(this);
                    }
                    return new BODYSTRUCTURE(this);
                }
                break;
            case 69:
            case 101:
                if (match(ENVELOPE.name)) {
                    return new ENVELOPE(this);
                }
                break;
            case 70:
            case 102:
                if (match(FLAGS.name)) {
                    return new FLAGS(this);
                }
                break;
            case 73:
            case 105:
                if (match(INTERNALDATE.name)) {
                    return new INTERNALDATE(this);
                }
                break;
            case 77:
            case 109:
                if (match(MODSEQ.name)) {
                    return new MODSEQ(this);
                }
                break;
            case 82:
            case 114:
                if (match(RFC822SIZE.name)) {
                    return new RFC822SIZE(this);
                }
                if (match(RFC822DATA.name)) {
                    boolean isHeader = false;
                    if (match(HEADER)) {
                        isHeader = true;
                    } else if (match(TEXT)) {
                        isHeader = false;
                    }
                    return new RFC822DATA(this, isHeader);
                }
                break;
            case 85:
            case 117:
                if (match(UID.name)) {
                    return new UID(this);
                }
                break;
        }
        return null;
    }

    private boolean parseExtensionItem() throws ParsingException {
        if (this.fitems == null) {
            return false;
        }
        for (int i = 0; i < this.fitems.length; i++) {
            if (match(this.fitems[i].getName())) {
                if (this.extensionItems == null) {
                    this.extensionItems = new HashMap();
                }
                this.extensionItems.put(this.fitems[i].getName(), this.fitems[i].parseItem(this));
                return true;
            }
        }
        return false;
    }

    private boolean match(char[] itemName) {
        int len = itemName.length;
        int j = this.index;
        int i = 0;
        while (i < len) {
            int j2 = j + 1;
            int i2 = i + 1;
            if (Character.toUpperCase((char) this.buffer[j]) != itemName[i]) {
                return false;
            }
            j = j2;
            i = i2;
        }
        this.index += len;
        int i3 = j;
        int i4 = i;
        return true;
    }

    private boolean match(String itemName) {
        int len = itemName.length();
        int j = this.index;
        int i = 0;
        while (i < len) {
            int j2 = j + 1;
            int i2 = i + 1;
            if (Character.toUpperCase((char) this.buffer[j]) != itemName.charAt(i)) {
                return false;
            }
            j = j2;
            i = i2;
        }
        this.index += len;
        int i3 = j;
        int i4 = i;
        return true;
    }
}
