/*
 * Connect.java
 *
 * Created on 1 June 2006, 12:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.data.mqinterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class MQObjectDiscover extends Thread {
    private ObjectRepository repository;    
    private String networkName;
    private EntryPoint entryPoint;
    private String hostNameMask = ".*";
    private String qMgrNameMask = ".*";
    private String excludeMask = "";
    private Pattern hnPattern;
    private Pattern qmPattern;    
    private Pattern exPattern;
    private WMQQMgr qMgr = null;
    private String qMgrName = null;
    private String hostName = "localhost";
    private String commandQueueName = "SYSTEM.ADMIN.COMMAND.QUEUE";
    private ArrayList clusterNL = new ArrayList();
    private ArrayList clusterChannels = new ArrayList();
    private String[] svrConnFallback = new String[]{"SYSTEM.DEF.SVRCONN", "SYSTEM.SVRCONN", "SYSTEM.ADMIN.SVRCONN", "SYSTEM.AUTO.SVRCONN"};
    private HashMap attrMap = new HashMap();

    private int[] selectors = new int[3];
    private int[] intAttrs = new int[1];
    private int MQCA_COMMAND_INPUT_Q_NAME = 2003 ;
    private int MQIA_PLATFORM = 32 ;
    private int MQCA_Q_MGR_NAME = 2015;
    private int MQ_Q_MGR_NAME_LENGTH = 48;
    private int MQ_Q_NAME_LENGTH = 48;
    private int MQPL_ZOS = 1 ;
    static boolean zOSPlatform = false ;
    private byte[] charAttrs = new byte[MQ_Q_NAME_LENGTH + MQ_Q_MGR_NAME_LENGTH] ;
    
    private boolean suppressAMQ = true;
    
    private MQQueueManager mqqmgr = null;
    private MQQueue theQueue ;
    private MQQueue theReplyQueue ;
    String replyQueueName = "SYSTEM.DEFAULT.MODEL.QUEUE" ;
    private int openOptions, messageCount = 0 ;
    
    public MQObjectDiscover(WMQQMgr mgr) {        
        super();
        this.qMgr = mgr;
        this.qMgrName = mgr.getCaption();
        this.networkName = mgr.getNetworkName();
        this.hostName = mgr.getHostName();
//        System.out.println("QMgrDiscover : " + hostName + " : " + port  + " : " + svrConnChannel  + " : "+ networkName);        
    }
    public MQObjectDiscover(ThreadGroup threadgroup, String threadname, WMQQMgr mgr) {        
        super(threadgroup, threadname);
        this.qMgr = mgr;
        this.qMgrName = mgr.getCaption();
        this.networkName = mgr.getNetworkName();
        this.hostName = mgr.getHostName();
//        System.out.println("QMgrDiscover : " + hostName + " : " + port  + " : " + svrConnChannel  + " : "+ networkName);        
    }    
        
    public void run(){
        this.entryPoint = EntryPoint.findInstance(networkName);
        this.hostNameMask = (String)entryPoint.getDiscovery("Mask - HostName");
        this.qMgrNameMask = (String)entryPoint.getDiscovery("Mask - QMgrName");
        this.excludeMask = (String)entryPoint.getDiscovery("Mask - Exclude");
        this.hnPattern = Pattern.compile(hostNameMask);
        this.qmPattern = Pattern.compile(qMgrNameMask);
        this.exPattern = Pattern.compile(excludeMask);
        this.repository = ObjectRepository.findInstance(this.networkName);
        this.attrMap = MQConstants.findInstance().getConstNameMap();

        try {
            MQConnect mqconnect = new MQConnect(qMgr);
            mqconnect.attemptConnection();

            mqqmgr = mqconnect.getMQQueueManager();
            qMgrName = mqconnect.getQMgrName();
            PCFMessageAgent agent = mqconnect.getPCFMessageAgent();

            if (agent != null) {  // agent constructed AND qmgr discovery succeded (in that order) ...

                // map each of the following object types. TODO - add listener type?
                MapQMgr(agent);
                MapQueues(agent);
                MapChannels(agent);
                //MapProcesses(agent);
                MapClusqmgrs(agent);

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

            } else {
                qMgr.setDiscovery("Polling Enabled", Integer.valueOf("0"));
            }
        } catch (RuntimeException e) {
            entryPoint.updateOutput("RuntimeException: " + e.toString() + "\n");
            e.printStackTrace();
        }
        
//        entryPoint.updateGui();
    }

    private boolean MapQMgr(PCFMessageAgent agent) {
        boolean success = false;
        HashMap MgrAttributes = new HashMap();
         // this hashmap contains attribute names and their corresponding values. 
         // The whole hashmap then becomes a propery of the WMQMgr object.
        try {
            PCFMessage request;
            PCFMessage[] responses;
            
 //           entryPoint.updateOutput("Discovering Queue Manager \"" + agent.getQManagerName() + "\" : " + this.hostName + "(" + this.port.intValue() + "), " + this.svrConnChannel + "\n");
            
            // Build the PCF request
            request = new PCFMessage (CMQCFC.MQCMD_INQUIRE_Q_MGR);
            request.addParameter (CMQCFC.MQIACF_Q_MGR_ATTRS, new int [] { CMQCFC.MQIACF_ALL });
            
            // Use the agent to send the request
            System.out.print ("Sending PCF request... ");
            responses = agent.send (request);
//            System.out.println ("Received reply.");
//            qMgrName = responses[0].getStringParameterValue(2015);
                        
            Matcher qmm = qmPattern.matcher(qMgrName);
            Matcher exm = exPattern.matcher(qMgrName);
            boolean qmMatches = qmm.matches();
            boolean exMatches = exm.matches();
            
            if (qmMatches && !exMatches){  // should qMgr be mapped?
            
                System.out.println("QMgr \"" + qMgrName + "\" responses........");
                Enumeration e = responses [0].getParameters ();

                while (e.hasMoreElements ()) {
                    PCFParameter p = (PCFParameter) e.nextElement ();                
                    if (attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                        MgrAttributes.put(attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))), p.getValue());
                    } else {
                        MgrAttributes.put(Integer.valueOf(String.valueOf(p.getParameter())), p.getValue());  
                    }    
                }

                //  call ReportCluster for each entry in turn.
                String clusNL = (String)MgrAttributes.get("Repository Namelist");
                if (clusNL != null) { 
                    clusNL = clusNL.trim();
                    if (clusNL.length() > 0){
                        MapNamelist(agent, clusNL);        
                    } 
                }

                // report the qmgr and transfer attributes hashmap
                updateQMgr(MgrAttributes);
                success = true;
                
            } else {  // qMgr should not be mapped.
                System.out.println("QMgr \"" + qMgrName + "\" does not satisfy Include/Exclude Masks... dropping.");
                success = false;
            }
            
        } catch (MQException ex) {
            entryPoint.updateOutput("MQCONN Failed : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");            
//            entryPoint.updateOutput(ex.toString());
            success = false;
        } catch (ArrayIndexOutOfBoundsException ex) {
            entryPoint.updateOutput("MQCONN Failed: " + ex.getMessage() + "\n");
//            entryPoint.updateOutput(ex.toString());
            success = false;
        } catch (NumberFormatException ex) {
            entryPoint.updateOutput("MQCONN Failed: " + ex.getMessage() + "\n");
//            entryPoint.updateOutput(ex.toString());
            success = false;
        } catch (IOException ex) {
            entryPoint.updateOutput("MQCONN Failed: " + ex.getMessage() + "\n");
//            entryPoint.updateOutput(ex.toString());
            success = false;
        }
        
        return success;
    }   
    
    private void MapQueues(PCFMessageAgent agent) {
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
        
//        System.out.println("MapQueues........");
        
        try {
           responses = agent.send (CMQCFC.MQCMD_INQUIRE_Q, parameters); // send PCF command
//           System.out.println("MapQueues: " + responses.length);
           for (int i = 0; i < responses.length; i++) {
                cfh = new MQCFH (responses [i]);                
                if (cfh.reason == 0) {
                    // iterate through all attributes
                    HashMap qAttributes = new HashMap();                   
                    
                    for (int j = 0; j < cfh.parameterCount; j++) {
                        p = PCFParameter.nextParameter (responses [i]);
                        // if qname, then create new queue object
                        if (p.getParameter() == CMQC.MQCA_Q_NAME){
                            qName = (String)p.getValue();
                            qName = qName.trim();
                        }
                        // add attribute to attribute hashmap
                        if (attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                            String attrName = (String) attrMap.get(Integer.valueOf(String.valueOf(p.getParameter())));
                            qAttributes.put(attrName, p.getValue());            
                                                                    
                        } else {
                            qAttributes.put(Integer.valueOf(String.valueOf(p.getParameter())), p.getValue());  
                        }                        
                    }
                    // report the q and transfer attributes hashmap
                    if ((qName.length() < 4) || (!((qName.substring(0, 4).equalsIgnoreCase("AMQ.")) && (suppressAMQ)))){
                        reportQueue(qName, (HashMap) qAttributes);
                    }                    
                } else {
                    // Walk through the returned parameters describing the error
                    entryPoint.updateOutput("Error while getting queue names, PCF reason code :" + cfh.reason + "\n");
                }
           }           
        } catch (MQException ex) {
	   entryPoint.updateOutput("MQ error occured while getting queue names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
        } catch (IOException ioe) {
	   entryPoint.updateOutput("IO error occured while getting queue names : " + ioe + "\n");
	}        
    }
    
    private void MapChannels(PCFMessageAgent agent) {
        int [] 	attrs = { CMQCFC.MQIACF_ALL };        
        PCFParameter [] parameters = {
            new MQCFST (CMQCFC.MQCACH_CHANNEL_NAME, "*"),
            new MQCFIL (CMQCFC.MQIACF_CHANNEL_ATTRS, attrs) 
        };
	
        MQMessage[] responses;
        MQCFH cfh;
        PCFParameter p;
        String chName = "";        

//        System.out.println("MapChannels........");
        
        try {           
           responses = agent.send (CMQCFC.MQCMD_INQUIRE_CHANNEL, parameters); // send PCF command           
           System.out.println("MapChannels: " + responses.length);
           for (int i = 0; i < responses.length; i++) {
                cfh = new MQCFH (responses [i]);                
                if (cfh.reason == 0){
                    // iterate through all attributes
                    HashMap chAttributes = new HashMap();   
                    
                    for (int j = 0; j < cfh.parameterCount; j++) {
                        p = PCFParameter.nextParameter (responses [i]);
                        // if channelname, then create new channel object
                        if (p.getParameter() == CMQCFC.MQCACH_CHANNEL_NAME){
                            chName = (String)p.getValue();
                            chName = chName.trim();                            
                        }
                        // add attribute to attribute hashmap
                        if (attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                            chAttributes.put((String)attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))), p.getValue());
                        } else {
                            chAttributes.put(Integer.valueOf(String.valueOf(p.getParameter())), p.getValue());
                        }                                              
                    }
                    String conName = (String)chAttributes.get("Connection Name"); //quick fix to ignore connection-name " "
                    String xmitqName = (String)chAttributes.get("Xmit Q Name"); //quick fix to ignore connection-name " "
                    // If conName has value, report the connection to "reportQMgr". If new - is added to array of undiscoverdQMgrs
                    if ((conName != null) && (conName.charAt(0) > 48)){                        
                        ReportQMgr(conName, chName, xmitqName);
                    } 
                    
                    String clusName = (String)chAttributes.get("Cluster Name");                               
                    // If clusName has value, report the connection to "reportCluster"                    
                    if ((clusName != null) && (clusName.charAt(0) > 48)){ //quick fix to ignore cluster-name " "
                        ReportCluster(clusName);
                    }
                    
                    //  call ReportCluster for each entry in turn.
                    String clusNL = (String)chAttributes.get("Cluster Namelist");
                    if (clusNL != null) { 
                        clusNL = clusNL.trim();
                        if (clusNL.length() > 0){
                            MapNamelist(agent, clusNL);
                        }                         
                    }                    
                    
                    // report channel and transfer attributes hashmap.
                    reportChannel(chName, (HashMap) chAttributes);
                } else {
                    // Walk through the returned parameters describing the error
                    entryPoint.updateOutput("Error while getting channel names, PCF reason code :" + cfh.reason + "\n");
                }
           }           
        } catch (MQException ex) {
	   entryPoint.updateOutput("MQ error occured while getting channel names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
        } catch (IOException ioe) {
	   entryPoint.updateOutput("IO error occured while getting channel names : " + ioe + "\n");
	}                
    }
    
    private void MapProcesses(PCFMessageAgent agent) {      
        int [] 	attrs = { CMQCFC.MQIACF_ALL };
        PCFParameter [] parameters = {
            new MQCFST (CMQC.MQCA_PROCESS_NAME, "*"),
            new MQCFIL (CMQCFC.MQIACF_PROCESS_ATTRS, attrs)
        };

        MQMessage[] responses;
        MQCFH cfh;
        PCFParameter p;
        String prName = "";      

        try {           
           responses = agent.send (CMQCFC.MQCMD_INQUIRE_PROCESS, parameters); // send PCF command           
           for (int i = 0; i < responses.length; i++) {
                cfh = new MQCFH (responses [i]);
                if (cfh.reason == 0){
                    // iterate through all attributes
                    HashMap prAttributes = new HashMap(); 
                    
                    for (int j = 0; j < cfh.parameterCount; j++) {
                        p = PCFParameter.nextParameter (responses [i]);
                        // if process name, then name new process object
                        if (p.getParameter() == CMQC.MQCA_PROCESS_NAME){
                            prName = (String)p.getValue();
                            prName = prName.trim();                            
                        }
                        // add attribute to attribute hashmap
                        if (attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                            prAttributes.put((String)attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))), p.getValue());
                        }                                              
                    }   
                    // report the q and transfer attributes hashmap                    
                    reportProcess(prName, (HashMap) prAttributes);
                    } else {
                    // Walk through the returned parameters describing the error
                        entryPoint.updateOutput("Error while getting process names, PCF reason code :" + cfh.reason + "\n");
                    }
           }
        } catch (MQException ex) {
	   entryPoint.updateOutput("MQ error occured while getting process names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
        } catch (IOException ioe) {
	   entryPoint.updateOutput("IO error occured while getting process names : " + ioe + "\n");
	}        
    }

    private void MapClusqmgrs(PCFMessageAgent agent) {
        int [] 	attrs = { CMQCFC.MQIACF_ALL };
        PCFParameter [] parameters = {
            new MQCFST (CMQC.MQCA_CLUSTER_Q_MGR_NAME, "*"),
            new MQCFIL (CMQCFC.MQIACF_CLUSTER_Q_MGR_ATTRS, attrs)
        };

	    MQMessage[] responses;
	    MQCFH cfh;
	    PCFParameter p;
        String qmName = "";

        try {
           responses = agent.send (CMQCFC.MQCMD_INQUIRE_CLUSTER_Q_MGR, parameters); // send PCF command
           for (int i = 0; i < responses.length; i++) {
                cfh = new MQCFH (responses [i]);
                if (cfh.reason == 0){ // is good
                    // iterate through all attributes
                    HashMap qmAttributes = new HashMap();

                    for (int j = 0; j < cfh.parameterCount; j++) {
                        p = PCFParameter.nextParameter (responses [i]);
                        // if QMgr name, then name new object
                        if (p.getParameter() == CMQC.MQCA_CLUSTER_Q_MGR_NAME){
                            qmName = (String)p.getValue();
                            qmName = qmName.trim();
                        } 
                        // add attribute to attribute hashmap
                        if (attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                            qmAttributes.put((String)attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))), p.getValue());
                        }
                    }
                    // report the qmgr and transfer attributes hashmap
                    String conName = ((String)qmAttributes.get("Connection Name")).trim(); //quick fix to ignore connection-name " "
                    // If conName has value, report the connection to "reportQMgr". If new - is added to array of undiscoverdQMgrs
                    // TODO - report with qmgrname?
                    if ((conName != null) && (conName.charAt(0) > 48)){
                        ReportQMgr(conName, null, qmName);
                    }

                    // If clusName has value, report the qmgr to "reportRepository"
                    Integer qmgrType = (Integer)qmAttributes.get("Q Mgr Type");
                    String clusterName = ((String)qmAttributes.get("Cluster Name")).trim();
                    if ((qmgrType != null) && (qmgrType.intValue() == 1) ) {
                        reportRepository(clusterName, qmName);
                    }

                } else {
                // Walk through the returned parameters describing the error
                    entryPoint.updateOutput("Error while getting ClusQmgr names, PCF reason code :" + cfh.reason + "\n");
                }
           }
        } catch (MQException ex) {
            entryPoint.updateOutput("MQ error occured while getting ClusQmgr names. May be incompatible version : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
        } catch (IOException ioe) {
            entryPoint.updateOutput("IO error occured while getting ClusQmgr names. May be incompatible version : " + ioe + "\n");
        }
    }
    
    private void MapNamelist(PCFMessageAgent agent, String clusNL) {
//        System.out.println("@@@@@@@@@@@clusNL :" + clusNL + ":");
//        int [] 	attrs = { CMQCFC.MQIACF_ALL };
        int [] 	attrs = { 2020 }; // MQCA_NAMES 
        PCFParameter [] parameters = {
            new MQCFST (CMQC.MQCA_NAMELIST_NAME, clusNL),
            new MQCFIL (CMQCFC.MQIACF_NAMELIST_ATTRS, attrs)
        };

        MQMessage[] responses;
        MQCFH cfh;
        PCFParameter p;
        String nlName = "";

        try {           
           responses = agent.send (CMQCFC.MQCMD_INQUIRE_NAMELIST, parameters); // send PCF command    
           System.out.println("MapNamelist responses.length " + responses.length);
           for (int i = 0; i < responses.length; i++) {
                cfh = new MQCFH (responses [i]);
                if (cfh.reason == 0){
                    // iterate through all attributes
                    HashMap nlAttributes = new HashMap(); 
                    System.out.println("MapNamelist cfh.parameterCount " + cfh.parameterCount);
                    for (int j = 0; j < cfh.parameterCount; j++) {
                        p = PCFParameter.nextParameter (responses [i]);
			// if process name, then name new process object
                        if (p.getParameter() == CMQC.MQCA_NAMELIST_NAME){
                            nlName = (String)p.getValue();
                            nlName = nlName.trim();                            
                        }
                        // add attribute to attribute hashmap
                        if (attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))) != null){
                            nlAttributes.put((String)attrMap.get(Integer.valueOf(String.valueOf(p.getParameter()))), p.getValue());
                        }                                              
                    }   
                    
                    String clusName = null;
                    String[] clusNames = null;
                    if (nlAttributes.get("Names") != null){
                        if (nlAttributes.get("Names").getClass() == String[].class){
                            clusNames = (String[]) nlAttributes.get("Names");
                        } else {
                            clusNames = ((String)nlAttributes.get("Names")).split(",");
                        }
                    }
                    for (i=0;i==clusNames.length;i++){
                        clusName = clusNames[i].trim();
                        // If clusName has value, report the connection to "reportCluster"     
                        if (clusName != null) { 
                            ReportCluster(clusName);
                        }
                    }      
               } else {
                    // Walk through the returned parameters describing the error
                        entryPoint.updateOutput("Error while getting Namelist, PCF reason code :" + cfh.reason + "\n");
               }
           }
        } catch (MQException ex) {
            entryPoint.updateOutput("MQ error occured while getting Namelist : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode + "\n");
        } catch (IOException ioe) {
            entryPoint.updateOutput("IO error occured while getting Namelist : " + ioe + "\n");
        }
    }

    
    //---------------------------------------Interface to the Model------------------------------------------
    
    // If clusName has value, report the cluster to the repository.
    private void ReportCluster(String clusName) {  //, String chName
        clusName = clusName.trim();
        repository.reportCluster(clusName, qMgr);
    }

    // If conName has value, report the connection to the repository. If new - is added to array of undiscoverdQMgrs
    private void ReportQMgr(String conName, String chlName, String qmName) {
//        System.out.println("ReportQueueManager : " + conName);        
        conName = conName.trim();                    
        String[] connameArray = conName.split("\\(");
        String foundHostName = connameArray[0].trim();        
        if (foundHostName.equals("localhost")){
            // replace 'localhost' with valid IP address
            foundHostName = this.hostName;
        }
        
        //  check the include and exclude masks
        Matcher hnm = hnPattern.matcher(foundHostName);
        Matcher exm = exPattern.matcher(foundHostName);
        boolean hnMatches = hnm.matches();
        boolean exMatches = exm.matches();
            
        if (hnMatches && !exMatches){
            Integer foundPort;
            if (connameArray.length > 1){
                foundPort = Integer.valueOf(String.valueOf(connameArray[1].split("\\)")[0]));
            } else {
                foundPort = Integer.valueOf("1414");
            }
            String softQMgrName = null;
            if (qmName != null) {
                // this will be the case with clusterMgr discovered channels 
                softQMgrName = qmName.trim();
            } else if ((chlName != null) && (chlName.contains("."))) {
                // convention only, hence the qmgr name is 'soft' - caption only
                String[] channelStrings = chlName.split("\\.");                
                softQMgrName = channelStrings[channelStrings.length-1].trim();
            }

            conName = foundHostName + "(" + foundPort + ")";
            entryPoint.updateOutput("Found connected Queue Manager : " + conName + "\n");
            repository.reportQMgr(conName, foundHostName, foundPort, softQMgrName, qMgr);
        }
    }
    
    private void updateQMgr(HashMap qmgrAttr) {
        String qmgrUniqueName = (String) qmgrAttr.get("Q Mgr Identifier");
//        String qmgrName = qMgrName;
        System.out.println("UpdateQueueManager : " + qmgrUniqueName);
        qmgrUniqueName = qmgrUniqueName.trim();     
        repository.updateQMgr(qmgrUniqueName, qmgrAttr, qMgr);     
    } 
    
    private void reportQueue(String qName, HashMap qAttr) {
//        System.out.println("ReportQueue: " + qName);
        qName = qName.trim();
        repository.reportQueue(qName, qAttr, qMgr);
    }
    private void reportChannel(String chName, HashMap chAttr) {
//        System.out.println("ReportChannel: " + chName);
        chName = chName.trim();
        repository.reportChannel(chName, chAttr, qMgr);
    }
    private void reportProcess(String prName, HashMap prAttr) {
        prName = prName.trim();
        repository.reportProcess(prName, prAttr, qMgr);
    }
    private void reportRepository(String clusterName , String qMgrName) {
        clusterName = clusterName.trim();
        repository.reportRepository(clusterName, qMgrName);
    }

    
    
}
