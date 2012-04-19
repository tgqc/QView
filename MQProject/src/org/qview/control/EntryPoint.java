/*
 * EntryPoint.java
 *
 * Created on 30 May 2006, 13:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import org.qview.data.*;
import org.qview.control.StatusPoll;

/**
 *
 * @author T.R.Goodwill
 */
public class EntryPoint  implements Serializable {
    private static final String saveDir = "etc/";
    private static final String serializedFileName = "entryPointSerialized";

    public static final String THIS_APPLICATION_NAME = "QView";
    public static final String THIS_VERSION_NUMBER = "0.4";
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    private static final String defaultName = "MQ Network";
    private static String defEpName = defaultName;
    private static HashMap namedInstances = new HashMap();    
    private static HashMap rootAttrMap = new HashMap();
    private String name;
    private HashMap entryPointMap = new HashMap();
//    private ObjectRepository repository = null;
    private boolean isDefault = false;
    transient private javax.swing.Timer timer = null;
    transient private javax.swing.Timer updateGuiListener = null;
    transient private javax.swing.Timer updateOutputListener = null;
    private String outText;
    
    /** Creates a new instance of EntryPoint */
    public EntryPoint() {        
        this.name = defEpName;
        entryPointMap.put("Name", defEpName);
        entryPointMap.put("Host", "localhost");
        entryPointMap.put("Port", Integer.valueOf("1414"));
        entryPointMap.put("Channel", "SYSTEM.DEF.SVRCONN");
        entryPointMap.put("Q Threshold Critical", "90%");
        entryPointMap.put("Q Threshold Warning", "75%");
        entryPointMap.put("Monitoring Enabled", Integer.valueOf("1"));        
        entryPointMap.put("Monitoring Interval", Integer.valueOf("300"));
        entryPointMap.put("Polling Enabled", Integer.valueOf("1"));
        entryPointMap.put("Explore Peers", Integer.valueOf("0"));
        entryPointMap.put("Mask - HostName", ".*");
        entryPointMap.put("Mask - QMgrName", ".*");
        entryPointMap.put("Mask - Exclude", "");
        entryPointMap.put("Updated", "0");
        if (rootAttrMap.isEmpty()){
            setRootAttrs();
        }        
        if (namedInstances.size()<1){
            this.isDefault = true;
        }

    }
    public EntryPoint(String newName) {
        this();        
        this.name = newName;
        entryPointMap.put("Name", newName);
        
        namedInstances.put(newName, this);
//        repository = ObjectRepository.findInstance(this.name);
        serializeInstances();
    }
    public EntryPoint(String newName, String host, Integer port, String channel) {
        this();
        this.name = newName;
//        repository = ObjectRepository.findInstance(newName);
        entryPointMap.put("Name", newName);
        entryPointMap.put("Host", host);
        entryPointMap.put("Port", port);
        entryPointMap.put("Channel", channel);
        
        namedInstances.put(newName, this);
//        repository = ObjectRepository.findInstance(this.name);
        serializeInstances();
    }
    
    public void setName(String newName){
        String oldName = this.name;
        this.name = newName;
        this.entryPointMap.remove("Name");
        this.entryPointMap.put("Name", newName);
        ObjectRepository.renameRepository(oldName, newName);
        namedInstances.remove(oldName);
        namedInstances.put(newName, this);
        
        if (oldName.equals(defEpName)) {
            SetDefaultEP(newName);
        }        
        serializeInstances();
    }
    public String getName(){
        return this.name;
    }
    public String getConnName(){
        return (entryPointMap.get("Host") + "(" + entryPointMap.get("Port") + ")");
    }
    
    public boolean pollingEnabled(){
        return (entryPointMap.get("Polling Enabled").equals(Integer.valueOf("1")));
    }
    public void setDiscovery(String key, Object value){        
        this.entryPointMap.put(key, value);
        if (key.equalsIgnoreCase("Polling Enabled")){
            if (((Integer)value).intValue() == 1){
                setTimer();
            } else {
                removeTimer();
            }
        }
        serializeInstances();   
    }
    public Object getDiscovery(String key){
        return this.entryPointMap.get(key);
    }    
    public HashMap getDiscoveryMap(){
        return (HashMap) this.entryPointMap.clone();
    }

    public void updateTimestamp(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        entryPointMap.remove("Updated");
        entryPointMap.put("Updated", sdf.format(cal.getTime()));        
    }

    public String getTimestamp() {
        return (String) entryPointMap.get("Updated");
    }

    public static void setRootAttrs() {
        rootAttrMap.put("Application", THIS_APPLICATION_NAME);
        rootAttrMap.put("ApplVersion", THIS_VERSION_NUMBER);
        rootAttrMap.put("Enable Admin", Integer.valueOf("0"));
        rootAttrMap.put("Enable LclAddr", Integer.valueOf("0"));
        rootAttrMap.put("Enable Hop", Integer.valueOf("0"));        
    }
    public static void setRootAttr(String key, Object value){
        rootAttrMap.remove(key);
        System.out.println("refresh Props : " + value.toString());
        rootAttrMap.put(key, value);
        if (key.equalsIgnoreCase("Enable Admin")){
            System.out.println("refresh Props...............");
            DataGuiAdapter.findInstance().refreshProps();            
        }
    }
    public static Object getRootAttr(String key){               
        return rootAttrMap.get(key);
    }
    public static HashMap getRootAttrMap(){        
        return (HashMap) rootAttrMap.clone();
    }
    
    public static void SetDefaultEP(String newName){        
        EntryPoint oldEp = (EntryPoint) namedInstances.get(defEpName);
        if (oldEp != null){
            oldEp.isDefault = false;
        }
        EntryPoint newEp = (EntryPoint) namedInstances.get(newName);
        newEp.isDefault = true;
        defEpName = newName;
        serializeInstances();
    } 
    public static String GetDefaultEPName(){
        if (namedInstances.size()<1){
            reloadEntryPoints();
        }        
        return defEpName;
    }    
    public static EntryPoint existingInstance(String thisName){        
        return (EntryPoint) namedInstances.get(thisName);           
    }
    public static void destroyInstance(String epName){
        EntryPoint thisInstance = (EntryPoint) namedInstances.remove(epName);
        if (namedInstances.size()>0){
            if (epName.equals(defEpName)){
                String newName = (String) namedInstances.keySet().iterator().next();
                SetDefaultEP(newName);
            }
        } else {
            namedInstances = new HashMap();
            namedInstances.put(defaultName, new EntryPoint());
            SetDefaultEP(defaultName);
        }
        serializeInstances();

//        //ObjectRepository.destroyInstance(epName);
//        if (epName.equals(defEpName) && (namedInstances.size()>0)){
//            String newName = (String) namedInstances.keySet().iterator().next();
//            SetDefaultEP(newName);
//        } else if (namedInstances.size()<1) {
//            thisInstance.setName(defEpName);
//            //new EntryPoint();
//        }
//        serializeInstances();
                 
    }
    public static EntryPoint findInstance(String thisName){
        EntryPoint entryPoint = null;
        if ((namedInstances.size()>0) && (namedInstances.containsKey(thisName))){
            entryPoint = (EntryPoint) namedInstances.get(thisName);
        } else {
            reloadEntryPoints();
            if (namedInstances.containsKey(thisName)){
                entryPoint = (EntryPoint) namedInstances.get(thisName);
            }            
            if (entryPoint == null){
                entryPoint = new EntryPoint(thisName);
            }          
        }
        return entryPoint;
    }

    public static void reloadEntryPoints(){
        namedInstances = Serializer(null);
        if ((namedInstances != null) && (!namedInstances.isEmpty())){
            System.out.println("EntryPoint.namedInstance.toString() " + EntryPoint.namedInstances.toString());
            if (rootAttrMap.isEmpty()){
                setRootAttrs();
            }
            Iterator e = namedInstances.values().iterator();        
            while (e.hasNext()) { 
                Object nextone = e.next();
                System.out.println("e.next().getClass() " + nextone.getClass());
                System.out.println("EntryPoint.class " + EntryPoint.class);
                if (nextone.getClass() == EntryPoint.class){                    
                    EntryPoint ep = (EntryPoint) nextone;
//                    ObjectRepository.reinstateRepository(ep.repository);
                    if (ep.isDefault) {
                        SetDefaultEP(ep.getName());                        
                    }
                }                
            }
        } else {
            namedInstances = new HashMap();
            if (rootAttrMap.isEmpty()){
                setRootAttrs();
            } 
        }
//        DataGuiAdapter.findInstance().resetNode();
        DataGuiAdapter.findInstance().refreshNodes();
    }    
    
    public static ArrayList getEntryPoints(){
//        ArrayList eplist = (ArrayList) namedInstances.keySet();
        ArrayList list = new ArrayList();        
        Iterator e = namedInstances.keySet().iterator();        
        while (e.hasNext()) {            
            list.add(e.next());               
        }        
        return list;
    }
    
    public void setTimer(){
        if (((Integer)entryPointMap.get("Polling Enabled")).intValue() == 1){
            if (timer != null){
                timer.stop();
                if (timer.getActionListeners().length > 1){
                    timer.removeActionListener(timer.getActionListeners()[0]);    
                }                    
            }            
            Integer intvl;
            int interval = 0;
            intvl = (Integer)this.getDiscovery("Monitoring Interval");
            if (intvl != null){
                interval = intvl.intValue();
            }
            if (interval > 1){
                System.out.println("Made it to timer... " + interval);
                timer = new javax.swing.Timer(interval*1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                        StatusPoll nextPoll = new StatusPoll(name);
                        nextPoll.start(); 
                    }
                });
                timer.start();
            }  
        }                  
    }
    public void removeTimer(){
        if (timer != null){
            timer.stop();
            if (timer.getActionListeners().length > 1){
                timer.removeActionListener(timer.getActionListeners()[0]);      
            }
            timer = null;
        }
    }
    
    public void updateGui(){
        
        if (updateGuiListener == null){
            updateGuiListener = new javax.swing.Timer(0, new ActionListener() {
                // this is a shameless hack to transfer control to the AWT thread.
                // If you can do it more elegantly, be my guest!
                public void actionPerformed(ActionEvent e) {
                        DataGuiAdapter.findInstance().refreshNode(name);
                        try {
                            Thread.currentThread().sleep(150);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        updateGuiListener.stop();
                }
            });
        }
        if (!updateGuiListener.isRunning()) {
            updateGuiListener.start();
        }
        // infers new information so serialize...
        serializeInstances();
    }

    /** Queues output text if listener is not already busy. Does not block, will not necessarily write the output */
    public void updateOutput(String outputText){
        outText = outputText;

        if (updateOutputListener == null){
            updateOutputListener = new javax.swing.Timer(0, new ActionListener() {
                // this is a shameless hack to transfer control to the AWT thread.
                // If you can do it more elegantly, be my guest!
            public void actionPerformed(ActionEvent e) {
                    DataGuiAdapter.findInstance().addToOutputWindow(outText);
                    updateOutputListener.stop();
                }
            });
        }
        if (!updateOutputListener.isRunning()) {            
            updateOutputListener.start();
        }
    }

//    /** Queues debug text if debug option (root node property) is selected */ Depricated -> IDE log output.
//    public void debugOutput(String debugText){
//        if (((Integer)rootAttrMap.get("Debug On")).intValue() == 1) {
//            updateOutput(debugText);
//        }
//    }
    
    public static void serializeInstances(){
        Serializer(namedInstances);
    }
    
    /* gateway method to prevent concurrent modification */
    public static synchronized HashMap Serializer(HashMap instances){
        if (instances != null){
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            try {
                new File(saveDir).mkdir();
                File sFile = new File(saveDir + serializedFileName);
                fos = new FileOutputStream(sFile);
                out = new ObjectOutputStream(fos);
                try {
                    out.writeObject(namedInstances);
                } catch (ConcurrentModificationException cx) {
                    cx.printStackTrace();
                }
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            instances = null;
            FileInputStream fis = null;
            ObjectInputStream in = null;
            File sFile = new File(saveDir + serializedFileName);
            // check file exists
            if (sFile.exists()){
                try {
                    //read it
                    fis = new FileInputStream(sFile);            
                    in = new ObjectInputStream(fis);
                    instances = (HashMap)in.readObject();
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch(ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }        
        }
        return instances;
    }
    
//    public static synchronized void serializeInstances(){        
//        FileOutputStream fos = null;
//        ObjectOutputStream out = null;
//        try {
//            fos = new FileOutputStream("entryPointSerialized");
//            out = new ObjectOutputStream(fos);
//            out.writeObject(namedInstances);
//            out.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }    
//    public static synchronized HashMap reloadSerializedInstances(){ 
//        HashMap instances = null;
//        FileInputStream fis = null;
//        ObjectInputStream in = null;
//        File sFile = new File("entryPointSerialized");
//        // check file exists
//        if (sFile.exists()){
//            try {
//                //read it
//                fis = new FileInputStream(sFile);            
//                in = new ObjectInputStream(fis);
//                instances = (HashMap)in.readObject();
//                in.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            } catch(ClassNotFoundException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return instances;
//    }
    
}
