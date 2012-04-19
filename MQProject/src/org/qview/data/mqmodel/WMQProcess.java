/*
 * WMQProcess.java
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
public class WMQProcess extends WMQObject implements Serializable{    
    private String processID;
    /**
     * Creates a new instance of WMQProcess
     */
    public WMQProcess(String c, WMQQMgr parent, String networkName) {
      caption = c;
      uniqueName = parent.getUniqueName() + ".process."+ c;
      typeStr= "Process";
      this.parentQM = parent;
      this.network = networkName;
//      this.processID = this.getAttributes().get("ProcID"); //TO be inplimented      
    }
    
}
