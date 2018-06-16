package com.example.jonathan.ics;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeBodyPart;

public class NetworkHandler {

    static boolean islistening=false;
    public Folder emailFolder;
    boolean isWorking=false;
    Store store=null;
    Context context;
    Message[] mostRecentMessages =null;
    MailHandler mailHandler;
    NetworkHandler(Context context){
        this.context=context;
        mailHandler=new MailHandler(context);
    }

    public MessageCountListener mCL=new MessageCountListener() {
        @Override
        public void messagesAdded(MessageCountEvent e) {
            mostRecentMessages=e.getMessages();

            if(isWorking==false) {
                isWorking=true;
                interfaceHandler.note("new mails");
                interfaceHandler.update(MainActivity.actions.started, "", context);
                interfaceHandler.update(MainActivity.actions.OnError, "new Mails", context);
                Message[] messages = e.getMessages();
                System.out.println("messages.length---" + messages.length);
                try {
                    mailHandler.checkMails(messages);
                    emailFolder.setFlags(emailFolder.getMessages(), new Flags(Flags.Flag.SEEN), true);
                }catch (Exception e1){
                    interfaceHandler.note("Exception",e1.getMessage());
                    e1.printStackTrace();
                }
                //close the store and folder objects
                isWorking=false;
                mailHandler.update();
            }
        }

        @Override
        public void messagesRemoved(MessageCountEvent e) {

        }
    };

    void removeListener(){
        if(emailFolder!=null) {
            emailFolder.removeMessageCountListener(mCL);
        }
    }

    void registerMailListener(int feedback) {
        System.out.println("started");
        System.out.println();
        String host = "imap.gmail.com";// change accordingly
        String user =  interfaceHandler.get(MainActivity.vars.mail,context);
        String password = interfaceHandler.get(MainActivity.vars.password,context);
        if(user==null||password==null){
            interfaceHandler.update(MainActivity.actions.OnError,"password or user is null",context);
            return;
        }
        try {
            //create properties field
            Properties properties = new Properties();

            properties.put("mail.pop3.host", host);
            properties.put("mail.pop3.port", "993");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            store = emailSession.getStore("imaps");
            System.out.println("sendnig request to host");
            store.connect(host, user, password);
            //create the folder object and open it
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);
            Message[] msg=emailFolder.getMessages();
            if(msg.length>0&&!msg[msg.length-1].getFlags().contains(Flags.Flag.SEEN)){
                mailHandler.checkMails(new Message[]{msg[msg.length-1]});
                emailFolder.setFlags(emailFolder.getMessages(), new Flags(Flags.Flag.SEEN), true);
                mailHandler.update();
            }
            try {
                for (int i = msg.length - 2; i > -1; i--) {
                    msg[i].setFlag(Flags.Flag.SEEN, true);
                    String subject=msg[i].getSubject();
                    Multipart multiPart = (Multipart) msg[i].getContent();

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
                emailFolder.setFlags(msg, new Flags(Flags.Flag.SEEN), true);
                ContentResolver.setMasterSyncAutomatically(false);
                //interfaceHandler.note("cleared mail folder");
                ContentResolver.setMasterSyncAutomatically(true);
            }catch(Exception r){
                interfaceHandler.note("ugh",r.getMessage());
            }
            emailFolder.addMessageCountListener(mCL);
            interfaceHandler.note("registered Listener");
            islistening=true;
            interfaceHandler.update(MainActivity.actions.registered,"",context);

            IdleThread idleThread = new IdleThread(emailFolder,user,password);
            idleThread.setDaemon(false);
            idleThread.start();
            idleThread.join();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            Log.i("MainActivity1", e.getMessage());
            interfaceHandler.note(e.getMessage());
        } catch (MessagingException e) {
            if(e.getMessage().contains("[AUTHENTICATIONFAILED]")){
                if(feedback==1){
                    interfaceHandler.note("wrong user credentials");
                    interfaceHandler.update(MainActivity.actions.OnError,"wrong user Credentials",context);
                }
            }else{
                interfaceHandler.note(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("MainActivity1", "error" + e.getMessage());
            interfaceHandler.note(e.getMessage());
        }
    }
}
