/*
 * MQObjectStatus.java
 *
 * Created on 2 March 2007, 09:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.data.mqinterface;

import org.qview.data.ObjectRepository;
import java.io.*;
import java.util.*;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;
import org.qview.control.EntryPoint;
import org.qview.data.mqmodel.WMQQMgr;

/**
 * 
 * 
 */
public class MQObjectStatus extends Thread {
    private ObjectRepository repository;    
    private String networkName;
    private EntryPoint entryPoint;
    private WMQQMgr qMgr = null;
    private String qMgrName = "";
    private ArrayList clusterNL = new ArrayList();
    private ArrayList clusterChannels = new ArrayList();
    private HashMap statsMap = new HashMap();    
    private String currentMQI;
    private MQQueueManager mqqmgr = null;
    private int openOptions, messageCount = 0 ;  
    
    public MQObjectStatus(WMQQMgr theqmgr) {       
        super();
        this.qMgr = theqmgr;
        this.networkName = theqmgr.getNetworkName();
        this.entryPoint = EntryPoint.findInstance(networkName);
        this.repository = ObjectRepository.findInstance(networkName);

        System.out.println("ObjectStatusUpdate : " + qMgr.getHostName() + " : "
                                                   + qMgr.getPort()  + " : "
                                                   + qMgr.getSvrConnChl()  + " : "
                                                   + networkName);

        statsMap = MQConstants.findInstance().getConstNameMap();
    }   
    public MQObjectStatus(ThreadGroup threadgroup, String threadname, WMQQMgr theqmgr) { 
        super(threadgroup, threadname);
        this.qMgr = theqmgr;
        this.networkName = theqmgr.getNetworkName();
        this.entryPoint = EntryPoint.findInstance(networkName);
        this.repository = ObjectRepository.findInstance(networkName);

        System.out.println("ObjectStatusUpdate : " + qMgr.getHostName() + " : "
                                                   + qMgr.getPort()  + " : "
                                                   + qMgr.getSvrConnChl()  + " : "
                                                   + networkName);

        statsMap = MQConstants.findInstance().getConstNameMap();
    }   
    
    public void run() {
        MQConnect mqconnect = new MQConnect(qMgr);
        mqconnect.attemptConnection();

        mqqmgr = mqconnect.getMQQueueManager();
        qMgrName = mqconnect.getQMgrName();
        PCFMessageAgent agent = mqconnect.getPCFMessageAgent();
                
        if (agent != null) {
            this.qMgr.setUpdated("true");
            QMgrStatus(agent);
            QueueStatus(agent);
            ChannelStatus(agent);
            try {
                agent.disconnect();
            } catch (MQException ex) {
                ex.printStackTrace();
            }
            try {
                mqqmgr.disconnect();
            } catch (MQException ex) {
                ex.printStackTrace();
            }
        }        
    }        
        
    private void QMgrStatus(PCFMessageAgent agent) {
                        
        HashMap MgrStats = new HashMap(); 
            // this hashmap contains attribute names and their corresponding values. 
            // The whole hashmap then becomes a propery of the WMQMgr object.
        
        try {
            PCFMessage request;
            PCFMessage[] responses;
            
            // Build the PCF request                           
            request = new PCFMessage(161);
                // MQCMD_INQUIRE_Q_MGR_STATUS 161    
            request.addParameter(1229, new int [] { CMQCFC.MQIACF_ALL });
                // MQIACF_Q_MGR_STATUS_ATTRS 1229 
            // Use the agent to send the request
            System.out.println("Sending PCF request... " + agent.getQManagerName());
            responses = agent.send(request);
            System.out.println("Received reply.");
            
            Enumeration e = responses [0].getParameters ();            
            
            while (e.hasMoreElements ()) {
                PCFParameter p = (PCFParameter) e.nextElement ();                
                if (statsMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                    MgrStats.put(statsMap.get(Integer.valueOf(String.valueOf(p.getParameter()))), p.getValue());
                } else {
                    MgrStats.put(Integer.valueOf(String.valueOf(p.getParameter())), p.getValue());  
                }    
            }
            
            // report the qmgr and transfer attributes hashmap
            System.out.println("updateQMgrStats(MgrStats)");
            updateQMgrStats(MgrStats);
            
        } catch (MQException ex) {
            System.out.println("MQCONN Failed: " + ex.getMessage() + "\n");
            entryPoint.updateOutput("MQCONN Failed : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
//            entryPoint.updateOutput(ex.toString());
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("MQCONN Failed: " + ex.getMessage() + "\n");
            entryPoint.updateOutput("MQCONN Failed: " + ex.getMessage() + "\n");
//            entryPoint.updateOutput(ex.toString());
        } catch (NumberFormatException ex) {
            System.out.println("MQCONN Failed: " + ex.getMessage() + "\n");
            entryPoint.updateOutput("MQCONN Failed: " + ex.getMessage() + "\n");
//            entryPoint.updateOutput(ex.toString());
        } catch (IOException ex) {
            System.out.println("MQCONN Failed: " + ex.getMessage() + "\n");
            entryPoint.updateOutput("MQCONN Failed: " + ex.getMessage() + "\n");
//            entryPoint.updateOutput(ex.toString());
        }
    }   
    
    private void QueueStatus(PCFMessageAgent agent) {
        int[] attrs = { CMQCFC.MQIACF_ALL };        
        PCFParameter[] parameters = {
                new MQCFST (CMQC.MQCA_Q_NAME, "*"),
                new MQCFIN (CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL),
                new MQCFIL (CMQCFC.MQIACF_Q_ATTRS, attrs)
            };
        MQMessage[] responses;
        MQCFH cfh;
        PCFParameter p;
        String qName = "";
        String depth = "";
        
//        System.out.println("QueueStats........");
        
        try {
           responses = agent.send (CMQCFC.MQCMD_INQUIRE_Q_STATUS, parameters); // send PCF command
           System.out.println("QueueStats: " + responses.length);
           for (int i = 0; i < responses.length; i++) {
                cfh = new MQCFH (responses [i]);                
                if (cfh.reason == 0) {
                    // iterate through all attributes
                    HashMap qStats = new HashMap(); 
                    
                    for (int j = 0; j < cfh.parameterCount; j++) {
                        p = PCFParameter.nextParameter (responses [i]);
                        // if qname, then create new queue object
                        if (p.getParameter() == CMQC.MQCA_Q_NAME){
                            qName = (String)p.getValue();
                            qName = qName.trim();
                        }
                        // add attribute to attribute hashmap
                        if (statsMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                            qStats.put(statsMap.get(Integer.valueOf(String.valueOf(p.getParameter()))), p.getValue());                            
                        } else {
                            qStats.put(Integer.valueOf(String.valueOf(p.getParameter())), p.getValue());  
                        }                        
		    }
                    // report the q and transfer attributes hashmap                    
                    updateQStats(qName, (HashMap) qStats);
                }                    
                else {
                    // Walk through the returned parameters describing the error
                    entryPoint.updateOutput("Error while getting queue names, PCF reason code :" + cfh.reason + "\n");
                }
           }           
        } catch (MQException ex) {
//           System.out.println("MQ error occured while getting queue names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
	   entryPoint.updateOutput("MQ error occured while getting queue names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
        } catch (IOException ioe) {
//	   System.out.println("IO error occured while getting queue names : " + ioe + "\n");
           entryPoint.updateOutput("IO error occured while getting queue names : " + ioe + "\n");
	}        
    }
    
    private void ChannelStatus(PCFMessageAgent agent) {
        PCFMessage request;
        PCFMessage[] responses;
        
        PCFParameter p;
        String chName = "";        
        
        request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
        request.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*");
        
//        System.out.println("ChannelStats........");
        
        try {
           responses = agent.send(request);
           System.out.println("ChannelStats: " + responses.length);
           for (int i = 0; i < responses.length; i++) {                
                HashMap chStats = new HashMap();
                chName = responses[i].getStringParameterValue(CMQCFC.MQCACH_CHANNEL_NAME);
                
                Enumeration f = responses [i].getParameters ();
                while (f.hasMoreElements ()) {
                    p = (PCFParameter) f.nextElement ();
                    Integer param = Integer.valueOf(String.valueOf(p.getParameter()));
                    if (statsMap.get(param) != null){
                         chStats.put(statsMap.get(param), p.getValue());
                    } else {
                        chStats.put(Integer.valueOf(String.valueOf(p.getParameter())), p.getValue());  
                    }                        
                }
//                System.out.println("ChannelStats HashMap: " + chName + "  " + chStats.size());                
                updateChStats(chName, (HashMap) chStats);
           }           
        } catch (MQException ex) {
//           System.out.println("MQ error occured while getting channel names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
	   entryPoint.updateOutput("MQ error occured while getting channel names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
        }
	catch (IOException ioe) {
//           System.out.println("IO error occured while getting channel names : " + ioe + "\n");
	   entryPoint.updateOutput("IO error occured while getting channel names : " + ioe + "\n");
	}          
    }
    
    
    //---------------------------------------Interface to the Model------------------------------------------
    
    
    private void updateQMgrStats(HashMap qmgrStats) {
        String qmgrName = qMgr.getUniqueName();
//        System.out.println("UpdateQMgrStats : " + qmgrName);
        qmgrName = qmgrName.trim();
        repository.updateQMgrStats(qMgr, qmgrStats);     
    } 
    
    private void updateQStats(String qName, HashMap qStats) {
//        System.out.println("updateQStats: " + qName);
        String qmgrName = qMgr.getUniqueName();
        qName = qName.trim();
        repository.updateQueueStats(qmgrName, qName, qStats);
    }
    private void updateChStats(String chName, HashMap chStats) {
//        System.out.println("UpdateChStats: " + chName);
        String qmgrName = qMgr.getUniqueName();
        chName = chName.trim();
        repository.updateChannelStats(qmgrName, chName, chStats);
    }

     
}

