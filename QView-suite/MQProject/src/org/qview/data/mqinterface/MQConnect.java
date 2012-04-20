/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qview.data.mqinterface;

import java.util.*;
import com.ibm.mq.*;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.pcf.*;
import org.qview.control.EntryPoint;
import org.qview.control.GuiDataAdapter;
import org.qview.data.ObjectRepository;
import org.qview.data.mqmodel.WMQQMgr;

/**
 *
 * @author T.Goodwill
 */
public class MQConnect {
    
    private ObjectRepository repository;    
//    private String networkName;
    private EntryPoint entryPoint;
    
    private WMQQMgr qMgr = null;
    private String qMgrName = null;
    private WMQQMgr localQMgr = null;
    private String localAddress = null;
    private String commandQueueName = "SYSTEM.ADMIN.COMMAND.QUEUE";

    private String hostName = "localhost";
    private Integer port = Integer.valueOf("1414");
    private String connId = "";
    private String svrConnChannel = "SYSTEM.DEF.SVRCONN";
    private String[] svrConnFallback = new String[]{"SYSTEM.DEF.SVRCONN", "SYSTEM.AUTO.SVRCONN", "SYSTEM.ADMIN.SVRCONN"};
    private int lastMQrc;

    private boolean zOSPlatform = false ;
    private boolean localAddrEnabled = false ;
    private boolean hopEnabled = false ;
   
    private MQEnvironment mqenv = null;
    private MQQueueManager mqqmgr = null;
    private PCFMessageAgent agent = null;
    private MQQueue theQueue ;
    private MQQueue theReplyQueue ;
    String replyQueueName = "SYSTEM.DEFAULT.MODEL.QUEUE" ;
    
    public MQConnect(WMQQMgr mgr) {
        this.qMgr = mgr;
        
   }
    
    public void attemptConnection() {
        
        mqenv = new MQEnvironment();
        String networkName = qMgr.getNetworkName();
        localAddrEnabled  = GuiDataAdapter.findInstance().localAddrEnabled();
        hopEnabled = GuiDataAdapter.findInstance().hopEnabled();                
        this.qMgrName = qMgr.getCaption();
        this.entryPoint = EntryPoint.findInstance(networkName);
        this.repository = ObjectRepository.findInstance(networkName);
        this.hostName = qMgr.getHostName();
        this.port = qMgr.getPort();
        this.connId = qMgr.getConnId();
        this.svrConnChannel = qMgr.getSvrConnChl();
        if (hopEnabled) {
             this.localQMgr = qMgr.getLocalQMgr();
        }
        this.localAddress = qMgr.getLocalAddress();

        // connect and establish platform and qmgrname
        if (connect() && inquireQMgr()) {
            agent = initPCFAgent();
        }

        if ((agent == null) && (lastMQrc != 2059)) {
            for (int i=0;i<svrConnFallback.length;i++){
                if (!qMgr.getSvrConnChl().equals(svrConnFallback[i])){
                    // iterate through fallback serverconn channels
                    svrConnChannel = svrConnFallback[i];
                    // connect and establish platform and qmgrname
                    if (connect() && inquireQMgr()) {
                        agent = initPCFAgent();
                    }
                }
                if (agent != null) {
                    // set server conn channel for the qmgr
                    qMgr.setSvrConnChl(svrConnChannel);                    
                    i=svrConnFallback.length;                    
                } 
            }
        }
        
        // TODO - The following localAddress section slows mapping dramatically,
        // that prevents a timely attempt at a hop option in case of connect failure.

        // iterate through again with localAddress set to address of network peer.
        if ((agent == null) && (localAddress == null) && localAddrEnabled) {
            ArrayList peerMgrs = qMgr.getAllPeers();
            if (peerMgrs != null && peerMgrs.size()>0){
                String localConname = (String) peerMgrs.get(0);
                String[] stringList = localConname.split("\\(");
                localAddress = stringList[0];

                if (connect() && inquireQMgr()) {
                    agent = initPCFAgent();
                }

                if ((agent == null) && (lastMQrc != 2059)) {
                    for (int i=0;i<svrConnFallback.length;i++){
                        if (!qMgr.getSvrConnChl().equals(svrConnFallback[i])){
                            svrConnChannel = svrConnFallback[i];
                            if (connect() && inquireQMgr()) {
                                agent = initPCFAgent();
                            }
                        }
                        if (agent != null) {
                            // set server conn channel for the qmgr
                            qMgr.setSvrConnChl(svrConnChannel);
                            // set localAddress for next time.
                            qMgr.setLocalAddress(localAddress);
                            i=svrConnFallback.length;
                        }
                    }
                }
            }
        }
        
        // iterate through known peers to attempt hop
        if ((agent == null) && (localQMgr == null) && hopEnabled) {
            System.out.println("#########################################");
            System.out.println("got here");
            ArrayList peerMgrs = qMgr.getAllPeers();
            Iterator i = peerMgrs.iterator();
            while (i.hasNext()) {
                localQMgr = repository.getQMgr((String)i.next());
                if (localQMgr != null) {
                    entryPoint.updateOutput("Could not connect to " + qMgrName
                                          + ".\nAttempting connect via QMgr " + localQMgr.getCaption() + "\n");
                    if (connect() && inquireQMgr()) {
                        agent = initPCFAgent();
                    }
                    if (agent != null) {
                        // set the localqmgr (local address) for the qmgr
                        qMgr.setLocalQMgr(localQMgr);
                        entryPoint.updateOutput("Successfully conncted via QMgr " + localQMgr.getCaption() + "\n");
                   }
                }
            }
        }
    }

    public boolean connect() {
        boolean success = false;
        mqenv.localAddressSetting = null;
        Hashtable props = new Hashtable();

        if (localQMgr != null) {
            this.hostName = localQMgr.getHostName();
            this.port = localQMgr.getPort();
            this.connId = qMgr.getConnId();
            this.svrConnChannel = localQMgr.getSvrConnChl();
            if (localAddrEnabled) {
                localAddress = localQMgr.getLocalAddress(); // ordinarily null
            }
        }
        if (localAddress != null) {
            mqenv.localAddressSetting = localAddress;
            props.put(MQC.LOCAL_ADDRESS_PROPERTY, localAddress);
        }

        mqenv.hostname = this.hostName;
        mqenv.port = this.port.intValue();
        mqenv.channel = this.svrConnChannel;

        props.put(MQC.CHANNEL_PROPERTY, this.svrConnChannel);
        props.put(MQC.HOST_NAME_PROPERTY, this.hostName);
        props.put(MQC.PORT_PROPERTY, this.port);
        props.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES_CLIENT);
        if ((connId != null) && (!connId.equals(""))) {
            props.put(MQC.USER_ID_PROPERTY, this.connId);
        }
        
        try {
            mqqmgr = new MQQueueManager("", props);
            success = true;
        } catch (MQException ex) {
            //Exceptions.printStackTrace(ex);
            entryPoint.updateOutput("MQCONN Failed : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
            success = false;
            lastMQrc = ex.completionCode;
        }

        return success;
    }

    public boolean inquireQMgr() {
        boolean success = false;

        int   MQPL_ZOS = 1 ;
        int[] selectors = new int[3];
        int[] intAttrs = new int[1];
        byte[] charAttrs = new byte[CMQC.MQ_Q_NAME_LENGTH + CMQC.MQ_Q_MGR_NAME_LENGTH] ;

        selectors[0] = CMQC.MQCA_COMMAND_INPUT_Q_NAME ;
        selectors[1] = CMQC.MQIA_PLATFORM ;
        selectors[2] = CMQC.MQCA_Q_MGR_NAME ;

        try {
            mqqmgr.inquire(selectors,intAttrs,charAttrs);
            commandQueueName = new String(charAttrs,0,CMQC.MQ_Q_NAME_LENGTH) ;
            if (localQMgr == null) { // Unless hopping to another QMgr
                qMgrName = new String(charAttrs,CMQC.MQ_Q_NAME_LENGTH,CMQC.MQ_Q_MGR_NAME_LENGTH).trim() ;
            }
            if (intAttrs[0] == MQPL_ZOS ) { zOSPlatform = true ;}
            success = true;
            //	   qMgr.disconnect();        // Disconnect from the queue manager
        } catch (MQException ex) {
            entryPoint.updateOutput("MQCONN Failed : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
            lastMQrc = ex.completionCode;
            try { mqqmgr.disconnect(); } catch (MQException ex1) { }
            success = false;
        } //end of try

        return success;
    }

    private PCFMessageAgent initPCFAgent() {
//         Construct a PCFMessageAgent
        PCFMessageAgent agent = new PCFMessageAgent();

        try {
            if (zOSPlatform) {
                mqqmgr = new MQQueueManager(qMgrName);
                agent.setUsePlatformSettings(zOSPlatform);
                connectZOS();
            }
            if (localQMgr != null) {
                agent.connect(mqqmgr,commandQueueName,qMgrName);
            } else {
                agent = new PCFMessageAgent(mqqmgr);
            }

            System.out.println("this.agent.getQManagerName() " + agent.getQManagerName() + " " + this.hostName + " " + this.port.intValue() + " " + this.svrConnChannel);

        } catch (MQException ex) {
           entryPoint.updateOutput("MQCONN Failed : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");        
           System.out.println("initPCFAgent failed... " + this.hostName + " " + this.port.intValue() + " " + this.svrConnChannel);
            lastMQrc = ex.completionCode;
            agent = null;
        }
        return(agent);
    }

    public void connectZOS() {
       int openOutOptions = MQC.MQOO_OUTPUT |
                  	        MQC.MQOO_FAIL_IF_QUIESCING ;
       int openInOptions = MQC.MQOO_INPUT_SHARED |
		                   MQC.MQOO_FAIL_IF_QUIESCING ;
       String currentMQI = "MQCONN ZOS";
       try {
            // Specify the queue that we wish to open, and the open options...
            currentMQI = "MQOPEN CMD Queue";
            theQueue = mqqmgr.accessQueue(commandQueueName,
                                          openOutOptions,
                                          null,           // qmgr must be null for cluster queue
                                          null,           // no dynamic q name
                                          null);          // no alternate user id
            currentMQI = "MQOPEN CMD ReplyQueue";
            theReplyQueue = mqqmgr.accessQueue(replyQueueName,
                                               openInOptions,
                                               localQMgr.getCaption(),       // target qmgr
                                               "CSQ.*",        // no dynamic q name
                                               null);          // no alternate user
        } catch (MQException ex) {
            entryPoint.updateOutput(currentMQI + " failed with rc " + ex.reasonCode);
            lastMQrc = ex.completionCode;
        }
    }

    public MQQueueManager getMQQueueManager() {
        return mqqmgr;
    }

    public String getQMgrName() {
        return qMgrName;
    }

    public PCFMessageAgent getPCFMessageAgent() {
        return agent;
    }
}