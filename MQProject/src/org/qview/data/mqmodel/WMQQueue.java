/*
 * WMQQueue.java
 *
 * Created on March 30, 2006, 6:08 PM
 *
 */

package org.qview.data.mqmodel;

import java.io.Serializable;
import org.qview.data.ObjectRepository;

/**
 *
 */
public class WMQQueue extends WMQObject implements Serializable{  
        
    /**
     * Creates a new instance of WMQQueue
     */    
    public WMQQueue(String c, WMQQMgr parent, String networkName) {
        this.caption = c;
        this.uniqueName = parent.getUniqueName() + ".queue." + c;
        this.typeStr = "Queue";
        this.parentQM = parent;            

        this.network = networkName;           
        setDiscovery("Q Threshold Critical", "");
        setDiscovery("Q Threshold Warning", "");
        if (!caption.startsWith("SYSTEM.") || (caption.equals("SYSTEM.CLUSTER.TRANSMIT.QUEUE"))){
           setDiscovery("Monitoring Enabled", Integer.valueOf("1"));
       } else {
           setDiscovery("Monitoring Enabled", Integer.valueOf("0"));
       }  
//      System.out.println("WMQQueue : name " + c);     
    } 

}
