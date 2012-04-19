/*
 * 
 *
 * Created on 1 June 2006, 12:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.data.mqinterface;

import java.io.IOException;
import org.qview.control.DataGuiAdapter;
import org.qview.data.mqmodel.WMQChannel;
import org.qview.data.mqmodel.WMQObject;
import java.util.*;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;
import org.qview.data.mqmodel.WMQQMgr;

/**
 * 
 * 
 */
public class QMgrUtils {        
    private WMQQMgr qmgrObject = null;
    private ArrayList clusterNL = new ArrayList();
    private ArrayList clusterChannels = new ArrayList();
    private String hostName = "localhost";
    private Integer port = Integer.valueOf("1414");
    private String channel = "SYSTEM.ADMIN.SVRCONN";
    
    private String currentMQI ;
    private int openOptions, messageCount = 0 ;
    private MQQueueManager mqqmgr;
    private PCFMessageAgent agent = null;
    
    private QMgrUtils(WMQObject obj) {        
        WMQQMgr mgr = null;
        if (obj.getClass() == WMQQMgr.class){
           mgr = (WMQQMgr)obj;            
        } else {
           mgr = obj.getParentQM();
        }
        this.qmgrObject = mgr;
        MQConnect mqconnect = new MQConnect(qmgrObject);
        mqconnect.attemptConnection();

        mqqmgr = mqconnect.getMQQueueManager();
        agent = mqconnect.getPCFMessageAgent();
    }
    
     /*
     *  given the attribute name and value, either evaluate MQObject class or overload constructor ,
     *  then call appropriate methods (eg. "initMQConnParameters", "writeQueueAttributes")
     */
    public static boolean writeAttribute(WMQObject obj, String key, String Attribute){
        QMgrUtils thisInstance = new QMgrUtils(obj);
//        TODO...  
//            writeQueueAttributes(Data)
        
        return true;
    }
    public static boolean readAttributes(WMQObject obj, String key, String Attribute){
        QMgrUtils thisInstance = new QMgrUtils(obj);
        //TODO
        return true;
    }    
    public static boolean setChannelStatus(WMQChannel obj, String newStatus){
        QMgrUtils thisInstance = new QMgrUtils(obj);
        //TODO
        return true;
    }
    
     /*     
     *  Require routine to map attribute name to attribute number, change object attribute, handle errors.
     *  Routines required for each of Manager, (incl. separate Cluster Mgr?) Queue, Channel and Process objects.)
     */
    private void writeQueueAttributes(com.ibm.mq.MQQueue theQueue, String key, String Attribute) {
	int [] attrs = {
            CMQC.MQCA_Q_NAME, 
            CMQC.MQIA_CURRENT_Q_DEPTH
        };        
	PCFParameter[] parameters = {
            new MQCFST (CMQC.MQCA_Q_NAME, "*"), 
            new MQCFIN (CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL), 
            new MQCFIL (CMQCFC.MQIACF_Q_ATTRS, attrs) };
	MQMessage[] responses;
	MQCFH cfh;
	PCFParameter p;
        String qName = "";
        String depth = "";
        try {
           responses = agent.send (CMQCFC.MQCMD_CHANGE_Q, parameters); // send PCF command         
        } catch (MQException ex) {
	   DataGuiAdapter.findInstance().sendToOutputWindow("MQ error occured while getting queue names : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode);
        } catch (IOException ioe){
	   DataGuiAdapter.findInstance().sendToOutputWindow("IO error occured while getting queue names : " + ioe);
	}
        DataGuiAdapter.findInstance().sendToOutputWindow("");
    } 
    //TODO ...

}
