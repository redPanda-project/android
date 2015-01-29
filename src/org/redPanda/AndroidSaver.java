/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPandaLib.Main;
import org.redPandaLib.core.*;
import org.redPandaLib.core.messages.RawMsg;

/**
 *
 * @author robin
 */
public class AndroidSaver implements SaverInterface {

    private Context context;

    public AndroidSaver(Context context) {
        this.context = context;
    }
    public static final String SAVE_DIR = "data";

    public static String getPrefix() {
        return "";
    }

    public void saveMsgs(ArrayList<RawMsg> msgs) {
        try {
            File mkdirs = new File(context.getFilesDir(), SAVE_DIR);
            mkdirs.mkdir();

            File file = new File(context.getFilesDir(), SAVE_DIR + "/msgs" + getPrefix() + ".dat");



            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(msgs.clone());
            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException ex) {
            Logger.getLogger(AndroidSaver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ArrayList<RawMsg> loadMsgs() {

        System.out.println("[DRSM] filesdir: " + context.getFilesDir());

        try {
            File file = new File(context.getFilesDir(), SAVE_DIR + "/msgs" + getPrefix() + ".dat");

            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object readObject = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            ArrayList<RawMsg> msgs = (ArrayList<RawMsg>) readObject;

            System.out.println("[DRSM] msgs: " + msgs.size());

            return msgs;

        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {

            try {
                Main.sendBroadCastMsg("[DRSM] + IOException loading msgs... ");
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }

        System.out.println("[DRSM] could not load msgs.dat");

        return new ArrayList<RawMsg>();
    }

    public void savePeerss(ArrayList<Peer> peers) {
        ArrayList<PeerSaveable> arrayList = new ArrayList<PeerSaveable>();

        for (Peer peer : peers) {
            arrayList.add(peer.toSaveable());
        }

        try {
            File file = new File(context.getFilesDir(), SAVE_DIR + "/prepeers" + getPrefix() + ".dat");

            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(arrayList);
            objectOutputStream.close();
            fileOutputStream.close();

            //if write fails at writeObject, file will not be damaged!
            File newFile = new File(context.getFilesDir(), SAVE_DIR + "/peers" + getPrefix() + ".dat");
            file.renameTo(newFile);
        } catch (Exception ex) {
            Logger.getLogger(AndroidSaver.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    public ArrayList<Peer> loadPeers() {
        try {
            File file = new File(context.getFilesDir(), SAVE_DIR + "/peers" + getPrefix() + ".dat");

            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object readObject = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            ArrayList<PeerSaveable> pp = (ArrayList<PeerSaveable>) readObject;
            ArrayList<Peer> arrayList = new ArrayList<Peer>();


            for (PeerSaveable p : pp) {
                arrayList.add(p.toPeer());
            }


            return arrayList;


        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        }

        System.out.println("could not load peers.dat");

        return new ArrayList<Peer>();
    }

    public void saveIdentities(ArrayList<Channel> identities) {
        try {

            File mkdirs = new File(context.getFilesDir(), SAVE_DIR);
            mkdirs.mkdir();

            File file = new File(context.getFilesDir(), SAVE_DIR + "/identities.dat");
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(identities);
            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException ex) {
            Logger.getLogger(AndroidSaver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ArrayList<Channel> loadIdentities() {
        try {
            File file = new File(context.getFilesDir(), SAVE_DIR + "/identities.dat");

            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object readObject = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            return (ArrayList<Channel>) readObject;


        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        }

        System.out.println("could not load identities.dat");

        return new ArrayList<Channel>();
    }

    public void saveLocalSettings(LocalSettings ls) {
        try {

            File mkdirs = new File(context.getFilesDir(), SAVE_DIR);
            mkdirs.mkdir();

            File file = new File(context.getFilesDir(), SAVE_DIR + "/localSettings" + getPrefix() + ".dat");

            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(ls);
            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException ex) {
            Logger.getLogger(Saver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public LocalSettings loadLocalSettings() {
        try {
            File file = new File(context.getFilesDir(), SAVE_DIR + "/localSettings" + getPrefix() + ".dat");

            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object readObject = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            return (LocalSettings) readObject;


        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        }

        System.out.println("could not load objects.dat");

        return new LocalSettings();
    }

    public void saveTrustedPeers(ArrayList<PeerTrustData> peertrusts) {

        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            File mkdirs = new File(context.getFilesDir(), SAVE_DIR);
            mkdirs.mkdir();

            File fileTmp = new File(context.getFilesDir(), SAVE_DIR + "/trustData-tmp" + getPrefix() + ".dat");

            fileTmp.createNewFile();
            fileOutputStream = new FileOutputStream(fileTmp);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(peertrusts.clone());
            objectOutputStream.close();
            fileOutputStream.close();

            File originFile = new File(context.getFilesDir(), SAVE_DIR + "/trustData" + getPrefix() + ".dat");
            originFile.delete();
            fileTmp.renameTo(originFile);

        } catch (final Exception ex) {

            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException ex1) {
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ex1) {
                }
            }
//            new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        sleep(20000);
//                    } catch (InterruptedException ex1) {
//                        Logger.getLogger(AndroidSaver.class.getName()).log(Level.SEVERE, null, ex1);
//                    }
//                    Test.sendStacktrace(ex);
//                }
//            }.start();
        }
    }

    public ArrayList<PeerTrustData> loadTrustedPeers() {

        try {
            File file = new File(context.getFilesDir(), SAVE_DIR + "/trustData" + getPrefix() + ".dat");

            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object readObject = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            ArrayList<PeerTrustData> msgs = (ArrayList<PeerTrustData>) readObject;
            return msgs;

        } catch (final ClassNotFoundException ex) {
            new Thread() {

                @Override
                public void run() {
                    try {
                        sleep(20000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(AndroidSaver.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    Test.sendStacktrace(ex);
                }
            }.start();

        } catch (final IOException ex) {

            new Thread() {

                @Override
                public void run() {
                    try {
                        sleep(20000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(AndroidSaver.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    Test.sendStacktrace(ex);
                }
            }.start();

            try {
                Main.sendBroadCastMsg("[DRSM] + IOException loading msgs... ");
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
        System.out.println("[DRSM] could not load trustData.dat");
        return new ArrayList<PeerTrustData>();
    }
}
