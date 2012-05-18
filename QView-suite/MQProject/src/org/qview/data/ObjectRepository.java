/*
 * ObjectRepository.java
 *
 * Created on 24 May 2006, 18:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.qview.control.EntryPoint;
import org.qview.data.mqinterface.MQConstants;
import org.qview.data.mqmodel.WMQChannel;
import org.qview.data.mqmodel.WMQCluster;
import org.qview.data.mqmodel.WMQObject;
import org.qview.data.mqmodel.WMQProcess;
import org.qview.data.mqmodel.WMQQMgr;
import org.qview.data.mqmodel.WMQQueue;

/**
 * The purpose of the repository is to store all discovered objects, 
 * keyed with the unique MQ "longname", ie, object name prefaced by its unique MQ Queue Manager name.
 */
public class ObjectRepository implements Serializable {    
    private static HashMap namedInstance = new HashMap();
    private transient MQConstants mqconstants;
    private ConcurrentHashMap repository;
    private Integer defaultPort;
    private String defaultSvrConnChl;
    private String defaultConnId;
    private String instName;
    
    /** Creates a new instance of ObjectRepository */
    private ObjectRepository() {
         this.instName = "MQ Network";
         this.repository = new ConcurrentHashMap(120);
         this.mqconstants = MQConstants.findInstance();
    }
    private ObjectRepository(String networkName) {         
         this();
         this.instName = networkName;
         namedInstance.put(networkName, this);
    }
    
    /*
     * Variable "instance" represents this repository object.
     * Repository objects for different networks are stored in the "namedInstance" hashmap.
     */    
    public static ObjectRepository findInstance(String thisName){
        if (namedInstance.containsKey(thisName)){
            return (ObjectRepository) namedInstance.get(thisName);
        } else {
            return new ObjectRepository(thisName);
        }        
    }    
    public static ObjectRepository createInstance(String newName, Integer portNo, String channelName, String connectionId) {
          ObjectRepository instance = findInstance(newName);
          instance.defaultPort = portNo;
          instance.defaultSvrConnChl = channelName;
          instance.defaultConnId = connectionId;
          return instance;
    }
    /* TODO : To Be Removed!!!! */
    public static ObjectRepository createInstance(String newName, Integer portNo, String channelName) {
          ObjectRepository instance = findInstance(newName);
          instance.defaultPort = portNo;
          instance.defaultSvrConnChl = channelName;
          return instance;
    }
    public static boolean existsInstance(String thisName){
        return namedInstance.containsKey(thisName);        
    }  
    public static void destroyInstance(String thisName){
        namedInstance.remove(thisName);        
    }
    public static void renameRepository(String oldName, String newName){
        ObjectRepository inst = (ObjectRepository) namedInstance.remove(oldName);
        if (inst != null){
            inst.instName = newName;
            namedInstance.put(newName, inst);
        }
    }
    public static void reinstateRepository(ObjectRepository reloadedRepository){
        if (namedInstance == null){
            namedInstance = new HashMap();
        }        
        reloadedRepository.mqconstants = MQConstants.findInstance();
        reloadedRepository.resetToBaseMap();
        namedInstance.put(reloadedRepository.instName, reloadedRepository);
    }
    
    /**
     * retrieve generic WMQObject object from repository 
     */
    public WMQObject getFromRepository(String name){
        return (WMQObject) repository.get(name);
    }
    /**
     * retrieve all WMQObject objects in repository matching caption
     */
    public ArrayList findInRepository(String caption) {
        ArrayList WMQObjects = new ArrayList();
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {
            WMQObject obj = (WMQObject) e.next();
            if (obj.getCaption().equals(caption)){
                WMQObjects.add(obj);
            }
        }
        return WMQObjects;
    }
    public WMQChannel getReceiver(String channelName, String connName){
        WMQChannel channelObj = null;
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {
            Object obj = e.next();
            if (obj.getClass() == WMQChannel.class){
                WMQChannel chObj = (WMQChannel) obj;
                if ((chObj.getCaption().equals(channelName)) && (chObj.getParentQM().getConnName().equals(connName))){
                    channelObj = chObj;
                }
            }
        }
        return channelObj;
    }
    /**
     * add generic WMQObject object to repository 
     */
    public void addToRepository(WMQObject mqObj){
        addToRepository(mqObj.getUniqueName(), mqObj);
    }
    public void addToRepository(String name, WMQObject mqObj){        
        repository.put(name, mqObj);
    }
            
    /**
     * iterate through collection to retrieve objects of "WMQCluster" class 
     */
    public ArrayList getClusters(){
        ArrayList clusters = new ArrayList();
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {            
            Object obj = e.next();            
            if (obj.getClass() == WMQCluster.class){                
                clusters.add(((WMQCluster) obj).getUniqueName());
            }                
        }
        return clusters;
    }
    /**
     * iterate through collection to retrieve objects of "WMQQMgr" class to retrieve QMGR unique names
     */
    public ArrayList getQMgrUniqueNames(){
//        return (ArrayList) clusters.clone();
        ArrayList qMgrs = new ArrayList();
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {            
            Object obj = e.next();            
            if (obj.getClass() == WMQQMgr.class){                
                qMgrs.add(((WMQQMgr) obj).getUniqueName());
            }                
        }
        return qMgrs;
    }
    /**
     * iterate through collection to retrieve objects of "WMQQMgr" class to retrieve QMGR common names
     */
    public ArrayList getQMgrNames(){
//        return (ArrayList) clusters.clone();
        ArrayList qMgrs = new ArrayList();
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {            
            Object obj = e.next();            
            if (obj.getClass() == WMQQMgr.class){                
                qMgrs.add(((WMQQMgr) obj).getCaption());
            }                
        }
        return qMgrs;
    }
    /**
     * iterate through collection to retrieve WMQQMgr class objects, and test for cluster membership.
     */
    public ArrayList getUnclusteredQMgrs(){
        ArrayList qMgrs = new ArrayList();
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {            
            Object obj = e.next();
            if ((obj.getClass() == WMQQMgr.class) && (((WMQQMgr) obj).getClusterName() == null)){                
                qMgrs.add(((WMQQMgr) obj).getUniqueName());
            }                
        }
        return qMgrs;
    }
     /**
     * iterate through collection to retrieve WMQQMgr class objects, 
     * and return QMgr Name corresponding to given connName parameter.
     */
    public String getQMgrName(String connName) {
        connName = connName.trim();
        String qMgrName = null;
        Iterator e = repository.values().iterator();
        while ((qMgrName == null) && e.hasNext()) {            
            Object obj = e.next();
            if ((obj.getClass() == WMQQMgr.class) && (connName.equalsIgnoreCase(((WMQQMgr)obj).getConnName()))){ 
                // if connName (conn details) match
                qMgrName = ((WMQQMgr)obj).getUniqueName();
            }                
        }
        return qMgrName;        
    }
   /*
    * find the first QMgr to match qmgr display name
    */    
    public WMQQMgr getQMgrMatch(String displayName) {
        WMQQMgr qMgr = null;
        Iterator e = repository.values().iterator();
        while ((qMgr == null) && e.hasNext()) {
            Object obj = e.next();
            if ((obj.getClass() == WMQQMgr.class) && (displayName.equalsIgnoreCase(((WMQQMgr)obj).getCaption()))){                
                qMgr = (WMQQMgr)obj;
            }                
        }
        return qMgr;
    }
     /*
    * find the QMgr to match conname
    */
    public WMQQMgr getQMgr(String conname) {
        WMQQMgr qMgr = null;
        Iterator e = repository.values().iterator();
        while ((qMgr == null) && e.hasNext()) {
            Object obj = e.next();
            if ((obj.getClass() == WMQQMgr.class) && ((((WMQQMgr)obj).getConnName()).equalsIgnoreCase(conname))){
                qMgr = (WMQQMgr)obj;
            }
        }
        return qMgr;
    }

    /*
     * "unmappedQMgrs" are those QMgrs in the collection where "updated" attribute is false
     */    
    public WMQQMgr getUnmappedQMgr(){
        WMQQMgr qMgr = null;
        Iterator e = repository.values().iterator();
        while (e.hasNext() && (qMgr == null)) {
            Object obj = e.next();            
            //if ((obj.getClass().getName() == "org.qview.model.WMQQMgr") && (((WMQQMgr) obj).getQueues().isEmpty())){
            if ((obj.getClass() == WMQQMgr.class) && (((WMQQMgr) obj).getUpdated().equals("false")) && ((WMQQMgr) obj).isPollable()) {
                qMgr = (WMQQMgr) obj;
            }                
        }
        return qMgr;
    }


    public void setObjectsToUnmapped(){
        WMQQMgr qMgr = null;
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {
            WMQObject obj = (WMQObject) e.next();
            obj.setUpdated("false");
//            if (obj.getClass() == WMQQMgr.class){
//                ((WMQQMgr)obj).setUpdated("false");
//            }                
        }        
    }

    public void resetToBaseMap(){        
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {
            WMQObject mqobj = (WMQObject) e.next();            
            mqobj.setUpdated("false");
//            mqobj.setAttributes(new HashMap());
            mqobj.setStatus(new HashMap());
            if (mqobj.getClass() == WMQQMgr.class){
                ((WMQQMgr)mqobj).resetChildren();
            } 
        }        
    }
    
    public void orphanAll(){  
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {
            WMQObject mqobj = (WMQObject) e.next();            
            mqobj.orphan();
//            mqobj.setAttributes(new HashMap());
            mqobj.setStatus(new HashMap());            
        }     
    }    
    
    public void removeOrphaned(){        
        Iterator e = repository.values().iterator();
        while (e.hasNext()) {
            WMQObject mqobj = (WMQObject) e.next();
            if ((mqobj.getClass() != WMQQMgr.class) && (mqobj.getClass() != WMQCluster.class)) {
                if ((mqobj.getParentQM().getUpdated().equals("true")) && (mqobj.getUpdated().equals("false"))) {
                    repository.remove(mqobj.getUniqueName(), null);
                }
            } else if (mqobj.getClass() != WMQCluster.class) {
                if (mqobj.getUpdated().equals("false")) {
                    repository.remove(mqobj.getUniqueName());
                }
            } else if (mqobj.getClass() == WMQQMgr.class) {
                WMQQMgr mqqmgr = (WMQQMgr) mqobj;
                // if not mapped, and not mapped to. May be qmgrs mapped, but no longer connected to tne n/w !?!?
                if ((mqqmgr.getPeers().size() == 0) && (!mqqmgr.getUniqueName().equals(getQMgrName(EntryPoint.findInstance(instName).getConnName())))) {
                    repository.remove(mqqmgr.getUniqueName());
                }
            }
        }        
    }    
    
    // debug print statement: all objects in repository.
    public void printAll(){        
        System.out.println("***Key :  ***Value");        
        Iterator x = repository.keySet().iterator();        
        while (x.hasNext()) {
            String keyName = (String) x.next();
            System.out.println(keyName + " : " + ((WMQObject) getFromRepository(keyName)).getUniqueName());
        }//while        
    }
    
    
    //-----------------------------WMQObject "factory"--------------------------------
    
 
   /**
     * check repository for existance of object, if absent, instantiate new MQ object and
     * add Object to repository keyed with the unique MQ "uniqueName" (Class property)
     * ie, object name prefaced by its unique MQ Queue Manager name, OR Cluster name.
     */
    public void reportCluster(String clusName, WMQQMgr qMgr){
        WMQCluster cluster;
        String networkName = this.instName;
        if (!repository.containsKey(clusName)) {
           cluster = new WMQCluster(clusName, networkName);
           addToRepository(cluster);
        }else{
           cluster = (WMQCluster) getFromRepository(clusName);
        }
        System.out.println("--- qMgr --- cluster ---");
        System.out.println("--- " + qMgr.getUniqueName() + " --- " + clusName);
        cluster.addQMgr(qMgr);
        cluster.setUpdated("true");
        qMgr.addCluster(clusName);
    }
    /*     
     * Checks to see if QMGrs in repository have this connname, creates new QMgr if not.
     * Adds conname on bothe ends of the channel to eachothers 'peer' collection.
     */
    public void reportQMgr(String connName, String hostName, Integer port, String softQMgrName, WMQQMgr connectedQMgr){
        System.out.println("reportQMgr : " + connName);
//        System.out.println("getQMgrName(connName) : " + getQMgrName(connName));
        if (getQMgrName(connName) == null){ // already exists in repository
            String networkName = this.instName;
            WMQQMgr qMgr = new WMQQMgr(connName, hostName, port, defaultSvrConnChl, defaultConnId, networkName);
            if (softQMgrName != null) { qMgr.setCaption(softQMgrName); }
            qMgr.addPeer(connectedQMgr.getHostName(), connectedQMgr.getPort());
            addToRepository(qMgr);
            System.out.println("add QMgr to repository : " + qMgr.getUniqueName());
        } else {
            WMQQMgr qMgr = (WMQQMgr)getFromRepository(getQMgrName(connName));
            if ((qMgr.getCaption().equals(qMgr.getConnName())) && (softQMgrName != null)) {
                qMgr.setCaption(softQMgrName);
            }
            qMgr.addPeer(connectedQMgr.getHostName(), connectedQMgr.getPort());
            System.out.println("dont add, set as peer : " + getQMgrName(connName));
        }
        connectedQMgr.addPeer(hostName, port);
    }
    /*     
     * Creates new QMgr from 'EntryPoint' details entered by user.
     */
    public void reportQMgr(String qMgrName, String hostName, Integer port){
        System.out.println("reportQmgr : " + qMgrName);
        String networkName = this.instName;
        WMQQMgr qMgr = null;
        if (!repository.containsKey(qMgrName)){
            qMgr = new WMQQMgr(qMgrName, hostName, port, defaultSvrConnChl, defaultConnId, networkName);
            qMgr.setDiscovery("Polling Enabled", Integer.valueOf("1"));
            addToRepository(qMgr);
        }        
    }        
    
    /*     
     * Updates QMgr with QMGr name and attribute hashmap.
     */
    public void updateQMgr(String foundUniqueName, HashMap mgrAttr, WMQQMgr qMgr){
//        System.out.println("attempt updateQMgr - oldname : " + qMgr.getUniqueName() + "  newname : " + foundUniqueName);
        qMgr.setAttributes(mgrAttr);    
        qMgr.setUpdated("true");          
        if (!qMgr.getUniqueName().equalsIgnoreCase(foundUniqueName)) {
            System.out.println("updateQMgr... oldname : " + qMgr.getUniqueName() + "  newname : " + foundUniqueName);
            if (getFromRepository(foundUniqueName) != null){
                // TODO - if listening on 2 ports, the data will need to be merged!
                // quick-fix 
                WMQQMgr oldMqr = (WMQQMgr)getFromRepository(foundUniqueName);
                oldMqr.setUpdated("true");
                
                addToRepository(oldMqr.getConnName(), oldMqr);
            }
            repository.remove(qMgr.getUniqueName()); 
            qMgr.setName();            
            addToRepository(qMgr); // overwrites existing          
        }         
    }  
    
    public void reportChannel(String chName, HashMap chAttr, WMQQMgr qMgr) {
        Integer chTypeInt = (Integer) chAttr.get("Channel Type");
        String chType = mqconstants.getStatusName("Channel Type", (Integer) chTypeInt);
        
        WMQChannel thisChannel = null;
        Object thisObject = getFromRepository(qMgr.getUniqueName() + "." + chType + "." + chName);
        if (thisObject != null){
            thisChannel = (WMQChannel) thisObject;
        } else {
            thisChannel = new WMQChannel(chName, chType, qMgr, this.instName);
        }
        
        thisChannel.setAttributes(chAttr);
        thisChannel.setUpdated("true");
        qMgr.addChannel(thisChannel);
        addToRepository(thisChannel);      
    }    
    
    public void reportQueue(String qName, HashMap qAttr, WMQQMgr qMgr) {        
        WMQQueue thisQueue = null;
        Object thisObject = getFromRepository(qMgr.getUniqueName() + ".queue." + qName);
        if (thisObject != null){
            thisQueue = (WMQQueue) thisObject;
        } else {
            thisQueue = new WMQQueue(qName, qMgr, this.instName);
        }
        
        thisQueue.setAttributes(qAttr);
        thisQueue.setUpdated("true");
        qMgr.addQueue(thisQueue);
        addToRepository(thisQueue);  
    }
    
    public void reportProcess(String prName, HashMap prAttr, WMQQMgr qMgr) {
        WMQProcess thisProcess = null;
        Object thisObject = getFromRepository(qMgr.getUniqueName() + ".process." + prName);
        if (thisObject != null){
            thisProcess = (WMQProcess) thisObject;
        } else {
            thisProcess = new WMQProcess(prName, qMgr, this.instName);
        } 
        
        thisProcess.setAttributes(prAttr);
        thisProcess.setUpdated("true");
        qMgr.addProcess(thisProcess);
        addToRepository(thisProcess);        
    }

    public void reportRepository(String clusName, String qMgrName) {
        WMQCluster thisCluster;
        Object thisObject = getFromRepository(clusName);
        if (thisObject != null){
            thisCluster = (WMQCluster) thisObject;
        } else {
            thisCluster = new WMQCluster(clusName, instName);
        }
        addToRepository(thisCluster);

        WMQQMgr thisMgr = getQMgrMatch(qMgrName);
        if (thisMgr != null) {
            thisMgr.addCluster(clusName);
            thisCluster.addQMgr(thisMgr);
            thisCluster.addFullRepository(thisMgr);
            thisCluster.setUpdated("true");
        }
    }
    
    /*     
     * Updates MQ Object status hashmap.
     */
    public void updateQMgrStats(WMQQMgr qMgr, HashMap mgrStats){        
        qMgr.setStatus(mgrStats);
//        qMgr.setUpdated("true");
    }        
    public void updateChannelStats(String qmgrName, String chName, HashMap chStats) {
        Integer chTypeInt = (Integer) chStats.get("Channel Type");
        String chType = mqconstants.getStatusName("Channel Type", chTypeInt);
        WMQChannel thisChannel = (WMQChannel) getFromRepository(qmgrName + "." + chType + "." + chName);
        if (thisChannel != null){
            thisChannel.setStatus(chStats);
        } // else? new channel?
    }    
    public void updateQueueStats(String qmgrName, String qName, HashMap qStats) {
        WMQQueue thisQueue = (WMQQueue) getFromRepository(qmgrName + ".queue." + qName);
        if (thisQueue != null){
            thisQueue.setStatus(qStats);
        } // else? new queue?
    }
        
    
}
