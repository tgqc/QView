/*
 * AdHocUpdate.java
 *
 * Created on 15 October 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.control;

import org.qview.data.mqinterface.MQObjectDiscover;
import org.qview.data.mqinterface.MQObjectStatus;
import org.qview.data.mqmodel.WMQQMgr;
import org.qview.gui.nbexplorer.RootNode;

/**
 *
 * @author T.Goodwill
 */
public class AdHocUpdate {
     private WMQQMgr qMgr = null;
     
    /** Creates a new instance of AdHocUpdate */
    public AdHocUpdate(WMQQMgr qmgrObject) {
        this.qMgr = qmgrObject;
    }    
  
    public void AdHocExplore() {         
       if (!qMgr.getUpdated().equalsIgnoreCase("updating")) {
            MQObjectDiscover discover = new MQObjectDiscover(qMgr);
            qMgr.setUpdated("updating");
            qMgr.setDiscovery("Polling Enabled", Integer.valueOf("1"));
            discover.start();
        }       
    }
    
    public void AdHocStatusUpdate() {       
        if (!qMgr.getUpdated().equalsIgnoreCase("updating")) {           
            MQObjectStatus getStatus = new MQObjectStatus(qMgr);
            qMgr.setUpdated("updating");
            qMgr.setDiscovery("Polling Enabled", Integer.valueOf("1"));
            getStatus.start();
            //RootNode.refreshNode();                
            EntryPoint.findInstance((String)(qMgr.getNetworkName())).updateGui();
        }             
    }
}       
    

