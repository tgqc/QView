/*
 * MQObjectAlter.java
 *
 * Created on 19 March 2007, 17:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.data.mqinterface;

import javax.swing.JOptionPane;
//import org.openide.util.Exceptions;
import org.qview.data.ObjectRepository;
import org.qview.control.DataGuiAdapter;
import org.qview.data.mqmodel.WMQChannel;
import org.qview.data.mqmodel.WMQObject;
import org.qview.data.mqmodel.WMQProcess;
import org.qview.data.mqmodel.WMQQueue;
import java.io.*;
import java.util.*;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;
import org.qview.data.mqmodel.WMQQMgr;
/**
 *
 * @author T.R.Goodwill
 */
public class MQObjectAlter {

    private static ObjectRepository repository;
    private MQConstants mqconstants;
    private String networkName;
        
    private WMQQMgr qMgr = null;
    private String qMgrName = "";
    private HashMap statsMap = new HashMap();
    public boolean success = true;
    
    private String currentMQI ;
    private int openOptions, messageCount = 0 ;
    private MQQueueManager mqqmgr;
    private PCFMessageAgent agent = null;
    
    /**
     * Creates a new instance of MQObjectAlter
     */
    public MQObjectAlter(WMQObject mqObj, String attribute, Object value) {
        this.qMgr = mqObj.getParentQM();
        this.networkName = qMgr.getNetworkName();
        this.repository = ObjectRepository.findInstance(networkName);
        this.mqconstants = MQConstants.findInstance();

        statsMap = mqconstants.getConstNameMap();

        MQConnect mqconnect = new MQConnect(qMgr);
        mqconnect.attemptConnection();

        mqqmgr = mqconnect.getMQQueueManager();
        qMgrName = mqconnect.getQMgrName();
        agent = mqconnect.getPCFMessageAgent();

        if (agent != null) {
            AlterObject(mqObj, attribute, value);
            try {
                agent.disconnect();
            } catch (MQException ex) {
                ex.printStackTrace();
            }
        } 
    }
    
    public static boolean Alteration(WMQObject mqObj, String attribute, Object value){
        MQObjectAlter instance = new MQObjectAlter(mqObj, attribute, value);
        return instance.success;
    }
   
     private void AlterObject(WMQObject mqObj, String attribute, Object value) {             
        PCFMessage request;
        PCFMessage[] responses;        
        PCFParameter p;        
        
        String objName = mqObj.getCaption();   
        int mqConst;
        
        mqConst = mqconstants.getConst(attribute);
                        
        if (attribute.equalsIgnoreCase("Channel Status")){ // right-click channel start/stop context menu
            if (((String)value).equalsIgnoreCase("Stop")){
                request = new PCFMessage(CMQCFC.MQCMD_STOP_CHANNEL);
            } else {
                request = new PCFMessage(CMQCFC.MQCMD_START_CHANNEL);
            }        
            request.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, objName);
        } else {
            if (mqObj.getClass() == WMQChannel.class){ // Channel            
                int chType = 0;
                Object attrObj = mqObj.getAttribute("Channel Type");
                if (attrObj != null){
                    chType = ((Integer)attrObj).intValue();
                }            
                request = new PCFMessage(CMQCFC.MQCMD_CHANGE_CHANNEL);            
                request.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, objName);
                request.addParameter(CMQCFC.MQIACH_CHANNEL_TYPE, chType);
            } else if (mqObj.getClass() == WMQQueue.class){ // Queue
                int qType = 0;
                Object attrObj = mqObj.getAttribute("Q Type");
                if (attrObj != null){
                    qType = ((Integer)attrObj).intValue();
                }            
                request = new PCFMessage(CMQCFC.MQCMD_CHANGE_Q);            
                request.addParameter(CMQC.MQCA_Q_NAME, objName);
                request.addParameter(CMQC.MQIA_Q_TYPE, qType);            
            } else if (mqObj.getClass() == WMQProcess.class){  // Process (No "type")
                request = new PCFMessage(CMQCFC.MQCMD_CHANGE_PROCESS);            
                request.addParameter(CMQC.MQCA_PROCESS_NAME, objName);            
            } else if (mqObj.getClass() == WMQQMgr.class){  // Q Manager (No required attributes)
                request = new PCFMessage(CMQCFC.MQCMD_CHANGE_Q_MGR);
            } else {
                request = new PCFMessage(0);
            }
            // value is either integer or string - convert "Integer" object.        
            if (value.getClass() == Integer.class){ 
                request.addParameter(mqConst, ((Integer)value).intValue());  
            } else {
                request.addParameter(mqConst, (String)value);  
            }
        }
        
        System.out.println("----------------------------------------------------------");
        System.out.println("objName: " + objName + " attribute : " + attribute + " value : " + value.toString());

        // throw up dialog box to confirm change:
        int option = JOptionPane.showConfirmDialog(null, "Changing object \"" + objName + "\" attribute \""  +  attribute + "\" to " + value.toString(), "Alter Object", JOptionPane.CANCEL_OPTION);

        if (option == 1){
            try {
               responses = agent.send(request);

               System.out.println("@@@@@@@@@@@@ responses : " + responses.length);
               System.out.println(responses[0].toString());
               DataGuiAdapter.findInstance().sendToOutputWindow(responses[0].toString());

               //JOptionPane.showMessageDialog(null, "Changing object \"" + objName + "\" attribute \""  +  attribute + "\" to " + value.toString() + "  \n\n" + responses[0].toString(), "Object Changed", JOptionPane.PLAIN_MESSAGE);

            } catch (MQException ex) {
               DataGuiAdapter.findInstance().sendToOutputWindow("MQ error occured : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode);
               System.out.println("MQ error occured : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode);
               success = false;
            } catch (IOException ex) {
               DataGuiAdapter.findInstance().sendToOutputWindow("IO Error occured : " + ex.getMessage());
               System.out.println("IO Error occured : " + ex.getMessage());
               success = false;
            }
        }
                
    }
    
}
