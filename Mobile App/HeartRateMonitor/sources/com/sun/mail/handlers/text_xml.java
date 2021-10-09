package com.sun.mail.handlers;

import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.NanoHTTPD;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class text_xml extends text_plain {
    private static final ActivationDataFlavor[] flavors = {new ActivationDataFlavor(String.class, NanoHTTPD.MIME_XML, "XML String"), new ActivationDataFlavor(String.class, "application/xml", "XML String"), new ActivationDataFlavor(StreamSource.class, NanoHTTPD.MIME_XML, "XML"), new ActivationDataFlavor(StreamSource.class, "application/xml", "XML")};

    /* access modifiers changed from: protected */
    public ActivationDataFlavor[] getDataFlavors() {
        return flavors;
    }

    /* access modifiers changed from: protected */
    public Object getData(ActivationDataFlavor aFlavor, DataSource ds) throws IOException {
        if (aFlavor.getRepresentationClass() == String.class) {
            return super.getContent(ds);
        }
        if (aFlavor.getRepresentationClass() == StreamSource.class) {
            return new StreamSource(ds.getInputStream());
        }
        return null;
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (!isXmlType(mimeType)) {
            throw new IOException("Invalid content type \"" + mimeType + "\" for text/xml DCH");
        } else if (obj instanceof String) {
            super.writeTo(obj, mimeType, os);
        } else if ((obj instanceof DataSource) || (obj instanceof Source)) {
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                StreamResult result = new StreamResult(os);
                if (obj instanceof DataSource) {
                    transformer.transform(new StreamSource(((DataSource) obj).getInputStream()), result);
                } else {
                    transformer.transform((Source) obj, result);
                }
            } catch (TransformerException ex) {
                IOException ioex = new IOException("Unable to run the JAXP transformer on a stream " + ex.getMessage());
                ioex.initCause(ex);
                throw ioex;
            } catch (RuntimeException ex2) {
                IOException ioex2 = new IOException("Unable to run the JAXP transformer on a stream " + ex2.getMessage());
                ioex2.initCause(ex2);
                throw ioex2;
            }
        } else {
            throw new IOException("Invalid Object type = " + obj.getClass() + ". XmlDCH can only convert DataSource or Source to XML.");
        }
    }

    private boolean isXmlType(String type) {
        try {
            ContentType ct = new ContentType(type);
            if (!ct.getSubType().equals("xml")) {
                return false;
            }
            if (ct.getPrimaryType().equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT) || ct.getPrimaryType().equals("application")) {
                return true;
            }
            return false;
        } catch (RuntimeException | ParseException e) {
            return false;
        }
    }
}
