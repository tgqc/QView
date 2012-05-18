/*
 * WMQObject.java
 *
 * Created on 29 March 2006, 15:48
 *
 */

package org.qview.data.mqmodel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.qview.data.ObjectRepository;

/**
 *
 */
public class WMQObject implements Serializable {    
    protected String network = null;
    protected String typeStr = null;
    protected String caption = null;
    protected String uniqueName = null;
    protected String updated = "false";
//    protected String clusterName = null; // cluster to map to.
    protected ArrayList clusterList = new ArrayList(); // all clusters
    protected HashMap thresholds = new HashMap();   
    protected WMQQMgr parentQM = null;
    protected HashMap attributes = new HashMap();
    protected HashMap status = new HashMap();
    protected HashMap discoveryMap = new HashMap();
    
//    private ArrayList children= new ArrayList();
    
    /**
     * Creates a new instance of WMQObject
     */
    public WMQObject() {
        this.typeStr= "MQObject";
        this.caption= "Caption";
        this.uniqueName= "LongName";              
    } //WMQObject
    
    public void setAttribute(String key, Object obj){
        if (key.getClass() == String.class ) {
            this.attributes.put(key, obj);
        } else {
            this.attributes.put(key.toString(), obj);
        }        
//        if (key == "CurrentQDepth"){
//            this.status.put(key, obj);
//        } else if (key == "InhibitPut"){
//            this.status.put(key, obj);
//        } else if (key == "InhibitGet"){
//            this.status.put(key, obj);
//        }
    }
    public void setAttributes(HashMap map){        
        this.attributes = map;
    }    
    public Object getAttribute(String key){
         return (Object) attributes.get(key);
    }
    public HashMap getAttributesMap(){
        return (HashMap) attributes.clone();
    }
    
    public void setStatus(HashMap map){
//        if ((this.typeStr == "QMgr") || (this.typeStr == "Queue")){
//            map.put("Q Thresholds Enabled", this.status.get("Monitoring Enabled"));
//            map.put("Q Threshold Warning", this.status.get("Q Threshold Warning"));
//            map.put("Q Threshold Critical", this.status.get("Q Threshold Critical"));
//        }        
        this.status = map;        
        Iterator e = this.status.keySet().iterator();        
        while (e.hasNext()) {
            String statName = null;
            Object next = e.next();
            if (next.getClass() == String.class ) {
                statName = (String) next;
            } else {
                statName = next.toString();
            }               
            if (this.attributes.containsKey(statName)){                
                this.attributes.put(statName, this.status.get(statName));
            }                
        }
    }    
    public void setStatus(String key, Object obj){
        this.status.put(key, obj);
    }
    public Object getStatus(String key){
        return (Object) status.get(key);
    }
    public HashMap getStatiiMap(){
        return (HashMap) status.clone();
    }
    
    public Object getDiscovery(String key){
        return discoveryMap.get(key);
    }
    public void setDiscovery(String key, Object value){
        discoveryMap.put(key, value);
    }    
    public HashMap getDiscoveryMap(){
        return (HashMap) discoveryMap.clone();
    }
            
    public void setCaption(String caption){
        this.caption= caption;        
    } //setCaption
    
    public void setClusterName(String clusName){
//        this.clusterName = (String) clusName;
        if (!clusterList.contains(clusName)){
            clusterList.add(clusName);            
        }
    }
    
    public void setUpdated(String updateString){
        this.updated = updateString;
    }
    
    public String getCaption(){
        return this.caption;
    } // getCaption
    public String getUniqueName(){
        return this.uniqueName;
    }
    public String getNetworkName() {
          return this.network; 
    }
    public String getTypeString(){
        return this.typeStr;
    } //getTypeString
    public String getParentQMName() {
          return this.parentQM.getUniqueName(); 
    }
    public WMQQMgr getParentQM() {
        return this.parentQM;         
    }
//    public String getClusterName() {
//          return this.clusterName; 
//    }
 
//    public void addParentCl(String parent){
//        clusterList.add(parent);        
//        this.clusterName = (String) parent;
//    } //getParentClusters
//    public WMQCluster getPrimaryCluster () {
//        if (!clusterList.isEmpty()){
//            return (WMQCluster) clusterList.get(0);
//        } else {
//            return null;
//        }        
//    }
    public ArrayList getClusterList() {        
        return (ArrayList) clusterList.clone();            
    }
    public String getClusterName () {
        if (!clusterList.isEmpty()){
            return (String) clusterList.get(0);
        } else {
            return (String) this.attributes.get("Cluster Name");
        }
//        return this.clusterName;        
    }    
    public String getUpdated(){
        return this.updated;
    }
    
    public String getThreshold(String key){
        String threshold = (String) thresholds.get(key);
        return threshold;
    }
    
    public void orphan(){
        setUpdated("false");
    }
   
    public String toString(){
        return this.typeStr + ": " + caption;
    } //toString
    
} //WMQObject




