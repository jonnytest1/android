package com.example.jonathan.ics.services;

import android.content.ContentResolver;
import android.content.Context;

import com.example.jonathan.ics.Activities.settings.SettingsActivity;
import com.example.jonathan.ics.util.interfaceHandler;
import com.example.jonathan.ics.util.storage.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

public class MailServerService {

    private Context context;
    private MailHandler mailHandler;



    public MailServerService(Context context){
        this.context=context;
        mailHandler=new MailHandler(context);
    }

    private Store getStore() throws MessagingException {
        try {
            String host = "imap.gmail.com";// change accordingly
            String user = interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.mail);
            String password = interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.password);
            if (user == null || password == null) {
                interfaceHandler.getInstance().note("password or username is null");
                throw new RuntimeException("password or username is nul");
            }
            Properties properties = new Properties();

            properties.put("mail.pop3.host", host);
            properties.put("mail.pop3.port", "993");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");
            System.out.println("sendnig request to host");
            store.connect(host, user, password);
            //store.connect(user,password);
            interfaceHandler.getInstance().show("successfully connected to server");
            return store;
        }catch(AuthenticationFailedException e){
            interfaceHandler.getInstance().getStorage().log(e);
            interfaceHandler.getInstance().update(SettingsActivity.actions.OnError,"invalid username password",context);
            throw e;
        } catch(MessagingException e){
            e.printStackTrace();
            throw e;
        }

    }
    private Folder getFolder(Store store) throws MessagingException {
        Folder fodler = store.getFolder("INBOX");
        fodler.open(Folder.READ_WRITE);
        return fodler;
    }

    public void checkMails(){
        try {
            Store store=getStore();
            Folder emailFolder=getFolder(store);
            Message[] msg = emailFolder.getMessages();
            if (msg.length > 0 && !msg[msg.length - 1].getFlags().contains(Flags.Flag.SEEN)) {
                interfaceHandler.getInstance().note("found new");
                mailHandler.handleMessages(msg[msg.length - 1]);
                interfaceHandler.getInstance().getStorage().log("NEW emails \t"+new Date().toLocaleString());
            }else{
                interfaceHandler.getInstance().getStorage().log("no new emails \t"+new Date().toLocaleString());
            }
            setSeen(emailFolder, msg);
            refreshWidget();
        } catch (Exception e) {
            interfaceHandler.getInstance().getStorage().log(e);
        }
    }

    private void refreshWidget() {
        ContentResolver.setMasterSyncAutomatically(false);
        ContentResolver.setMasterSyncAutomatically(true);
    }
    private void setSeen(Folder emailFolder, Message[] msgs) throws MessagingException, IOException {
        emailFolder.setFlags(msgs , new Flags(Flags.Flag.SEEN), true);
        for (int i = msgs.length - 1; i > msgs.length-22; i--) {
            if(!msgs[i].getFlags().contains(Flags.Flag.SEEN)) {
                msgs[i].setFlag(Flags.Flag.SEEN, true);
                String subject = msgs[i].getSubject();
                Multipart multiPart = (Multipart) msgs[i].getContent();

                for (int x = 0; x < multiPart.getCount(); x++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(x);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        InputStream is = part.getInputStream();

                        String str = "";
                        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                        str += s.hasNext() ? s.next() : "";
                        s.findInLine(str+subject);
                    }
                }
            }
        }

    }
}
