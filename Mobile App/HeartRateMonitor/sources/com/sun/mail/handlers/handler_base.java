package com.sun.mail.handlers;

import java.io.IOException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;

public abstract class handler_base implements DataContentHandler {
    /* access modifiers changed from: protected */
    public abstract ActivationDataFlavor[] getDataFlavors();

    /* access modifiers changed from: protected */
    public Object getData(ActivationDataFlavor aFlavor, DataSource ds) throws IOException {
        return getContent(ds);
    }

    public ActivationDataFlavor[] getTransferDataFlavors() {
        return (ActivationDataFlavor[]) getDataFlavors().clone();
    }

    public Object getTransferData(ActivationDataFlavor df, DataSource ds) throws IOException {
        ActivationDataFlavor[] adf = getDataFlavors();
        for (int i = 0; i < adf.length; i++) {
            if (adf[i].equals(df)) {
                return getData(adf[i], ds);
            }
        }
        return null;
    }
}
