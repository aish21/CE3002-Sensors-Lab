package com.sun.mail.handlers;

import com.google.appinventor.components.runtime.util.NanoHTTPD;
import javax.activation.ActivationDataFlavor;

public class text_html extends text_plain {
    private static ActivationDataFlavor[] myDF = {new ActivationDataFlavor(String.class, NanoHTTPD.MIME_HTML, "HTML String")};

    /* access modifiers changed from: protected */
    public ActivationDataFlavor[] getDataFlavors() {
        return myDF;
    }
}
