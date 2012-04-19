/*
 * WMQQMgr.java
 *
 * Created on March 30, 2006, 5:32 PM
 *
 */

package org.qview.data.mqmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import org.qview.control.EntryPoint;
import org.qview.data.ObjectRepository;

/**
 *
 */
public class WMQQMgr extends WMQObject implements Serializable {
    private final String LOCAL_ADDR = "Local-Addr";
    private final String LOCAL_MGR = "Local-Mgr";
    private String hostName;    
    private Integer port;
    private String svrConnChannel = "SYSTEM.DEF.SVRCONN";
    private WMQQMgr localQMgr = null; // QMgr via which PCF messages will hop.
    private String localAddress = null; // QMgr address for firewall purposes.
    private ArrayList clusters = new ArrayList();
    private ArrayList channels = new ArrayList();
    private ArrayList processes = new ArrayList();
    private ArrayList queues = new ArrayList();
    private ArrayList peers = new ArrayList();
    
    /**
     * Creates a new instance of WMQQMgr
     */
    public WMQQMgr(String c, String networkName) {        
        caption = c;
        uniqueName = c;
        typeStr= "QMgr";
        parentQM = this;
        
        this.setStatus("STATUS", "Unknown");
        this.setDiscovery("QMNAME", this.getCaption());
        this.setDiscovery("Host", "localhost");
        this.setDiscovery(LOCAL_ADDR, "");
        this.setDiscovery(LOCAL_MGR, "");
        this.setDiscovery("Port", Integer.valueOf("1414"));
        this.setDiscovery("SvrConnChl", EntryPoint.findInstance(networkName).getDiscovery("Channel"));        
        this.setDiscovery("Q Threshold Critical", "");
        this.setDiscovery("Q Threshold Warning", "");        
        this.setDiscovery("Monitoring Enabled", Integer.valueOf("1"));
        
        Integer explore;
        if (EntryPoint.findInstance(networkName).getDiscovery("Explore Peers").getClass() == Integer.class){
            explore = (Integer) EntryPoint.findInstance(networkName).getDiscovery("Explore Peers");
        } else {
            explore = Integer.valueOf("1");
        }        
        this.setDiscovery("Polling Enabled", explore);
        this.network = networkName;   
    }
    public WMQQMgr(String c, String host, Integer prt, String channel, String networkName){           
        this(c, networkName);
        System.out.println("WMQQMgr : hostName " + c); 
        this.hostName = host;
        this.port = prt;
        this.svrConnChannel = channel;
        this.setDiscovery("Host", host);
        this.setDiscovery("Port", prt);
        this.setDiscovery("SvrConnChl", channel);        
    }
    // this overriding method is to set the STATUS to Running
    public void setUpdated(String updateString){        
        this.updated = updateString;
        if (updateString.equalsIgnoreCase("true")){
            this.setStatus("STATUS", "Running");
        } else {
            this.setStatus("STATUS", "Unknown");
        }        
    }    
    public boolean isPollable(){
        return (((((Integer)this.discoveryMap.get("Polling Enabled")).intValue() == 1)) && (((Integer)this.discoveryMap.get("Polling Enabled")).intValue() == 1));
    }

    public synchronized void addQueue(WMQQueue newQueue){
        queues.add(newQueue.uniqueName);
    } //addQueue    
    public ArrayList getQueues(){
        ArrayList queueList = (ArrayList) queues.clone();
        if ((queueList != null) && !(queueList.isEmpty())) {
            Collections.sort(queueList);
        }        
        return queueList;
    } //getQueues 
    
    public synchronized void addChannel(WMQChannel newChannel){
        channels.add(newChannel.uniqueName);  
    } //addChannel
    public ArrayList getChannels(){
        ArrayList channelList = (ArrayList) channels.clone();
        if ((channelList != null) && !(channelList.isEmpty())) {
            Collections.sort(channelList);
        }        
        return channelList;
    } //getChannels
    
    public synchronized void addProcess(WMQProcess newProcess){
        processes.add(newProcess.uniqueName);
    } //addProcess
    public ArrayList getProcess(){
        ArrayList processList = (ArrayList) processes.clone();
        if ((processList != null) && !(processList.isEmpty())) {
            Collections.sort(processList);
        }        
        return processList;
    } //getChannels
         
    public synchronized void addCluster(String clusName){
        this.setClusterName(clusName);
        this.clusters.add(clusName);
    }    
    public void addPeer(String hostName, Integer port){
        if (!peers.contains(hostName + "(" + port + ")")){
            peers.add(hostName + "(" + port + ")");            
        }        
    } //addProcess
    
    public ArrayList getPeers(){
        ArrayList peerList = null;
        if (this.attributes.get("Q Mgr Identifier") != null){ //if this qmgr is not mapped, 
                // then map other more useful relationships to these peers from elsewhere.
            peerList = (ArrayList) peers.clone();
        }
        if ((peerList != null) && !(peerList.isEmpty())) {
            Collections.sort(peerList);
        }        
        return peerList;
    } //addProcess
    public ArrayList getAllPeers(){
        ArrayList peerList = null;
        peerList = (ArrayList) peers.clone();
        if ((peerList != null) && !(peerList.isEmpty())) {
            Collections.sort(peerList);
        }
        return peerList;
    } //addProcess
        
    public String getHostName(){
        return this.hostName;
    }
    public Integer getPort(){
        return this.port;
    }
    public String getConnName(){
        return (this.hostName + "(" + this.port + ")");
    }
    public String getSvrConnChl(){
        String svrconn = (String) this.getDiscovery("svrConnCh");
        if (svrconn == null){
            svrconn = this.svrConnChannel;
        }
        return svrconn;
    }
    public void setSvrConnChl(String svrconn){
        this.setDiscovery("svrConnCh", svrconn);        
        this.svrConnChannel = svrconn;
    }
    public void setName(){
        if (this.attributes.get("Q Mgr Name") != null){
             this.caption = ((String) this.attributes.get("Q Mgr Name")).trim();        
        }
        if (this.attributes.get("Q Mgr Identifier") != null){
             this.uniqueName = ((String) this.attributes.get("Q Mgr Identifier")).trim();        
        }
        System.out.println("this.uniqueName.........." + this.uniqueName);
        this.setStatus("QMNAME", this.getCaption());        
    }
    public void resetChildren(){
        queues = new ArrayList();
        channels = new ArrayList();
        processes = new ArrayList();
    }
    public void orphan(){
        this.setUpdated("false");
        resetChildren();
        peers = new ArrayList();
        clusterList = new ArrayList();
    }

    // over-rides WMQObject setDiscovery to set localQMgr.
    public void setDiscovery(String key, Object value){
        discoveryMap.put(key, value);
        if (key.equals(LOCAL_ADDR)) {
            this.localAddress = (String) value;
        } else if (key.equals(LOCAL_MGR)) {
            this.localQMgr = ObjectRepository.findInstance(network).getQMgrMatch((String)value);
        }
    }

    // the optional 'localQMgr' variable holds a reference to the QMgr via which PCF comand will hop.
    public void setLocalQMgr(WMQQMgr localMgr){
        this.localQMgr = localMgr;
        discoveryMap.put(LOCAL_MGR, localMgr.getCaption());
    }
    public WMQQMgr getLocalQMgr(){
        return localQMgr;
    }
    // the optional 'localAddress' variable is the source address presented to the firewall.
    public void setLocalAddress(String localAddr){
        this.localAddress = localAddr;
        discoveryMap.put(LOCAL_ADDR, localAddr);
    }
    public String getLocalAddress(){
        if ((localAddress != null) && (localAddress.equals(""))) {
            localAddress = null;
        }
        return localAddress;
    }
}
