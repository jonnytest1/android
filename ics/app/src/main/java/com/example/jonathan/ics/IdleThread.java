package com.example.jonathan.ics;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

public class IdleThread extends Thread {
    private final Folder folder;
    private volatile boolean running = true;
    private String username;
    private  String password;
    public IdleThread(Folder folder,String username,String password) {
        super();
        this.folder = folder;
        this.password=password;
        this.username=username;
    }

    public synchronized void kill() {

        if (!running)
            return;
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                ensureOpen(folder);
                System.out.println("enter idle");
                ((IMAPFolder) folder).idle();
            } catch (Exception e) {
                // something went wrong
                // wait and try again
                e.printStackTrace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    // ignore
                }
            }

        }
    }


    public static void close(final Folder folder) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
        } catch (final Exception e) {
            // ignore
        }

    }

    public static void close(final Store store) {
        try {
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (final Exception e) {
            // ignore
        }

    }

    public  void ensureOpen(final Folder folder) throws MessagingException {

        if (folder != null) {
            Store store = folder.getStore();
            if (store != null && !store.isConnected()) {
                store.connect(username, password);
            }
        } else {
            throw new MessagingException("Unable to open a null folder");
        }

        if (folder.exists() && !folder.isOpen() && (folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
            System.out.println("open folder " + folder.getFullName());
            folder.open(Folder.READ_WRITE);
            if (!folder.isOpen())
                throw new MessagingException("Unable to open folder " + folder.getFullName());
        }
    }
}
