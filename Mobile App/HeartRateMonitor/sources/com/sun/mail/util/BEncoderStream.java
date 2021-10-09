package com.sun.mail.util;

import androidx.appcompat.widget.ActivityChooserView;
import java.io.OutputStream;

public class BEncoderStream extends BASE64EncoderStream {
    public BEncoderStream(OutputStream out) {
        super(out, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    }

    public static int encodedLength(byte[] b) {
        return ((b.length + 2) / 3) * 4;
    }
}
