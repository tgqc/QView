/*
 * WMQChannel.java
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
public class WMQChannel extends WMQObject implements Serializable{
    
    /**
     * Creates a new instance of WMQChannel
     */
    public WMQChannel(String c, String chType, WMQQMgr parent, String networkName) {      
       caption = c;
       uniqueName = parent.getUniqueName() + "." + chType + "." + c;
       typeStr= "Channel";
       this.parentQM = parent;
       this.network = networkName;       
       this.setStatus("Channel Status", Integer.valueOf("0"));
       if (!caption.startsWith("SYSTEM.")){
           setDiscovery("Monitoring Enabled", Integer.valueOf("1"));
       } else {
           setDiscovery("Monitoring Enabled", Integer.valueOf("0"));
       }     
//       System.out.println("WMQChannel : name " + c);     
    } 

}
