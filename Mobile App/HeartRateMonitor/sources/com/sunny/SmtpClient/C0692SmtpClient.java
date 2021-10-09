package com.sunny.SmtpClient;

import android.app.Activity;
import android.text.TextUtils;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.NanoHTTPD;
import com.google.appinventor.components.runtime.util.YailList;
import java.util.Properties;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@SimpleObject(external = true)
@DesignerComponent(category = ComponentCategory.EXTENSION, description = "An extension to send emails using SMTP server <br> Developed by Sunny Gupta", iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png", nonVisible = true, version = 1, versionName = "1.4")
@UsesLibraries(libraries = "android-mail.jar,android-activation.jar")
/* renamed from: com.sunny.SmtpClient.SmtpClient */
public class C0692SmtpClient extends AndroidNonvisibleComponent {
    public Activity activity;

    public C0692SmtpClient(ComponentContainer componentContainer) {
        super(componentContainer.$form());
        this.activity = componentContainer.$context();
    }

    @SimpleEvent(description = "Event triggered when any error occurs and provides the error message")
    public void GotError(String str) {
        EventDispatcher.dispatchEvent(this, "GotError", str);
    }

    @SimpleEvent(description = "Event triggered after getting 'Send' method's result")
    public void GotResult(boolean z) {
        EventDispatcher.dispatchEvent(this, "GotResult", Boolean.valueOf(z));
    }

    @SimpleFunction(description = "Sends the email")
    public void Send(String str, String str2, int i, String str3, String str4, String str5, YailList yailList, YailList yailList2, YailList yailList3, String str6, String str7, boolean z, YailList yailList4) {
        final String str8 = str2;
        final int i2 = i;
        final String str9 = str;
        final String str10 = str3;
        final String str11 = str4;
        final String str12 = str5;
        final YailList yailList5 = yailList;
        final YailList yailList6 = yailList2;
        final YailList yailList7 = yailList3;
        final String str13 = str6;
        final boolean z2 = z;
        final String str14 = str7;
        final YailList yailList8 = yailList4;
        AsynchUtil.runAsynchronously(new Runnable() {
            public void run() {
                boolean z = false;
                try {
                    Properties properties = new Properties();
                    properties.put("mail.debug", "true");
                    properties.put("mail.smtp.host", str8);
                    properties.put("mail.smtp.port", "" + i2);
                    properties.put("mail.smtp.auth", "true");
                    properties.put("mail.smtp.timeout", "10000");
                    properties.put("mail.smtp.connectiontimeout", "10000");
                    properties.put("mail.smtp.ssl.trust", "*");
                    if (!str9.isEmpty()) {
                        String upperCase = str9.toUpperCase();
                        char c = 65535;
                        switch (upperCase.hashCode()) {
                            case 82412:
                                if (upperCase.equals("SSL")) {
                                    c = 1;
                                    break;
                                }
                                break;
                            case 83163:
                                if (upperCase.equals("TLS")) {
                                    c = 0;
                                    break;
                                }
                                break;
                        }
                        switch (c) {
                            case 0:
                                properties.put("mail.smtp.starttls.enable", "true");
                                break;
                            case 1:
                                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                                properties.put("mail.smtp.socketFactory.port", "" + i2);
                                break;
                        }
                    }
                    Session instance = Session.getInstance(properties, new Authenticator() {
                        /* access modifiers changed from: protected */
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(str10, str11);
                        }
                    });
                    instance.setDebug(true);
                    MimeMessage mimeMessage = new MimeMessage(instance);
                    mimeMessage.setFrom(new InternetAddress(str10, str12));
                    if (!yailList5.isEmpty()) {
                        mimeMessage.addRecipients(Message.RecipientType.f298TO, InternetAddress.parse(TextUtils.join(",", yailList5.toStringArray())));
                    }
                    if (!yailList6.isEmpty()) {
                        mimeMessage.addRecipients(Message.RecipientType.f297CC, InternetAddress.parse(TextUtils.join(",", yailList6.toStringArray())));
                    }
                    if (!yailList7.isEmpty()) {
                        mimeMessage.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(TextUtils.join(",", yailList7.toStringArray())));
                    }
                    mimeMessage.setSubject(str13);
                    MimeMultipart mimeMultipart = new MimeMultipart();
                    MailcapCommandMap mailcapCommandMap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                    mailcapCommandMap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                    mailcapCommandMap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                    mailcapCommandMap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                    mailcapCommandMap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                    mailcapCommandMap.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
                    CommandMap.setDefaultCommandMap(mailcapCommandMap);
                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    if (z2) {
                        mimeBodyPart.setContent(str14, NanoHTTPD.MIME_HTML);
                    } else {
                        mimeBodyPart.setText(str14);
                    }
                    mimeMultipart.addBodyPart(mimeBodyPart);
                    if (!yailList8.isEmpty()) {
                        for (String str : yailList8.toStringArray()) {
                            MimeBodyPart mimeBodyPart2 = new MimeBodyPart();
                            mimeBodyPart2.setDataHandler(new DataHandler((DataSource) new FileDataSource(str)));
                            mimeBodyPart2.setFileName(str.substring(str.lastIndexOf("/") + 1));
                            mimeMultipart.addBodyPart(mimeBodyPart2);
                        }
                    }
                    mimeMessage.setContent(mimeMultipart);
                    try {
                        Transport.send(mimeMessage);
                        C0692SmtpClient.this.activity.runOnUiThread(new Runnable() {
                            public void run() {
                                C0692SmtpClient.this.GotResult(true);
                            }
                        });
                    } catch (Exception e) {
                        e = e;
                        z = true;
                    }
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    C0692SmtpClient.this.postError(e.getMessage() != null ? e.getMessage() : e.toString());
                    if (z) {
                        C0692SmtpClient.this.activity.runOnUiThread(new Runnable() {
                            public void run() {
                                C0692SmtpClient.this.GotResult(false);
                            }
                        });
                    }
                }
            }
        });
    }

    public void postError(final String str) {
        this.activity.runOnUiThread(new Runnable() {
            public void run() {
                C0692SmtpClient.this.GotError(str);
            }
        });
    }
}
