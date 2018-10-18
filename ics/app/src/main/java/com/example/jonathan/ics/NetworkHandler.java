package com.example.jonathan.ics;

import android.content.ContentResolver;
import android.content.Context;

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

public class NetworkHandler {

    Context context;
    private MailHandler mailHandler;



    NetworkHandler(Context context){
        this.context=context;
        mailHandler=new MailHandler(context);
    }

    private Store getStore() throws MessagingException {
        try {
            String host = "imap.gmail.com";// change accordingly
            String user = interfaceHandler.get(MainActivity.vars.mail, context);
            String password = interfaceHandler.get(MainActivity.vars.password, context);
            if (user == null || password == null) {
                interfaceHandler.note("password or username is null", context);
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
            return store;
        }catch(AuthenticationFailedException e){
            interfaceHandler.push(MainActivity.vars.log,"Authentication Failure"+e.getMessage(),context);
            interfaceHandler.update(MainActivity.actions.OnError,"invalid username password",context);
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



    void connect(){
        try {
            Store store=getStore();
            Folder emailFolder=getFolder(store);
            Message[] msg = emailFolder.getMessages();
            if (msg.length > 0 && !msg[msg.length - 1].getFlags().contains(Flags.Flag.SEEN)) {
                interfaceHandler.note("found new",context);
                mailHandler.checkMails(new Message[]{msg[msg.length - 1]});
                interfaceHandler.push(MainActivity.vars.log,"NEW emails \t"+new Date().toLocaleString(),context);
            }else{
                interfaceHandler.push(MainActivity.vars.log,"no new emails \t"+new Date().toLocaleString(),context);
            }
            setSeen(emailFolder, msg);
            refreshWidget();
        } catch (MessagingException e) {
            interfaceHandler.push(e,context);
        }catch (Exception e){
            interfaceHandler.push(e,context);
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
                    }
                }
            }
        }

    }
}
