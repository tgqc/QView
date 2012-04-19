/*
 * QueueUtils.java
 *
 * Created on 4 June 2006, 22:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.data.mqinterface;

import org.qview.control.DataGuiAdapter;
import org.qview.data.mqmodel.WMQQueue;
import java.io.*;
import java.util.*;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;
//import org.openide.util.Exceptions;
import javax.swing.JOptionPane;
import org.qview.data.mqmodel.WMQQMgr;
/**
 *
 * 
 */
public class MessageUtils extends Thread {
    private String outputText;
    private Object browseWindow = null;
    private WMQQMgr qmgrObject = null;
    private WMQQueue queueObject = null;
    private ArrayList messageList = new ArrayList();
    private ArrayList putMessageList = null;
    private boolean destructive = false;
    private String currentMQI ;
    private boolean convert = false;
    int openOptions = 0 ;    
    
    private com.ibm.mq.MQQueue theQueue ;
    private PCFMessageAgent agent = null;
    private MQQueueManager mqqmgr = null;

    private String msgId = null;
      
    public MessageUtils(WMQQueue queue, Object window) {
        browseWindow = window;        
        qmgrObject = queue.getParentQM();
        queueObject = queue; 
    }
    public MessageUtils(WMQQueue queue, Object window, ArrayList newList){
        this(queue, window);
        putMessageList = newList;        
    }
    public MessageUtils(WMQQueue queue, Object window, boolean convertChecked){
        this(queue, window);
        this.msgId = null;
        this.convert = convertChecked;        
    }
    public MessageUtils(WMQQueue queue, Object window, boolean convertChecked, boolean destructive, String thisMsgId){
        this(queue, window);
        this.msgId = thisMsgId;
        this.destructive = destructive;
        this.convert = convertChecked;       
    }
    public MessageUtils(WMQQueue queue, Object window, ArrayList msgList, boolean destructive){
        this(queue, window);
        this.destructive = destructive;
    }
    
    public void run() {
        DataGuiAdapter.findInstance().displayText(browseWindow, "Connecting...");

        MQConnect mqconnect = new MQConnect(qmgrObject);
        mqconnect.attemptConnection();

        mqqmgr = mqconnect.getMQQueueManager();
        agent = mqconnect.getPCFMessageAgent();

        initOpenOptions();

        if (agent != null) {             
            currentMQI = "MQCONN" ;
            try {                
                currentMQI = "MQOPEN" ;
                String selectedQueueName = queueObject.getCaption();
                WMQQMgr localQMgr = qmgrObject.getLocalQMgr();
                if (localQMgr == null) {
                    theQueue = mqqmgr.accessQueue(selectedQueueName,
                           openOptions,
                           "",             // qmgr
                           null,           // no dynamic q name
                           null);          // no alternate user id
                } else {
                    theQueue = mqqmgr.accessQueue(selectedQueueName,
                           openOptions,
                           qmgrObject.getCaption(),       // target qmgr
                           "AMQ.*",        // no dynamic q name
                           null);          // no alternate user
                }
                if (putMessageList != null) {  // messages to put...
                    processMQPut();
                } else if (!destructive) {  // browse
                    processMQGet() ;
                } else if (destructive) {   // get
                    String popupMessage;
                    if (msgId == null){
                        popupMessage = "Are you sure you want to clear this queue?";
                    } else {
                        popupMessage = "Are you sure you want to destructively read this message?";
                    }
                    int option = JOptionPane.showConfirmDialog(null, popupMessage, "Get Message", JOptionPane.CANCEL_OPTION);
                    if (option == 1) {
                        processMQGet() ;
                    }
                }
                mqqmgr.disconnect() ;
            } catch (MQException getex) {
                DataGuiAdapter.findInstance().sendToOutputWindow(currentMQI + " Failed : Completion code " + getex.completionCode + " Reason code " + getex.reasonCode);
                System.out.println(currentMQI + " Failed processFunction()");
            } 
        } else {
            System.out.println("agent == null");
        }
        // disconnect()
    }
    
    private void initOpenOptions() {

          if (putMessageList != null) { // msgs to put
            openOptions = MQC.MQOO_OUTPUT +
                          MQC.MQOO_FAIL_IF_QUIESCING ;
          } else if (!destructive) {    // browse
            openOptions = MQC.MQOO_INPUT_SHARED |
                          MQC.MQOO_BROWSE |
                          MQC.MQOO_FAIL_IF_QUIESCING ;
          } else if (!destructive) {    // get
            openOptions = MQC.MQOO_BROWSE |
                          MQC.MQOO_FAIL_IF_QUIESCING ;
          }
    }
        
    private boolean disconnect() {
        // Close queue and disconnect from the queue manager.
        boolean success = true ;
        try {
            theQueue.close();        // Close the queue
        } catch (MQException ex) {
//            ex.printStackTrace();
            success = false ;
        }
        try {
            mqqmgr.disconnect();       // Disconnect from the queue manager 
        } catch (MQException ex) {
//            ex.printStackTrace();
            success = false ;
        }       // Disconnect from the queue manager 
        
        return true;
    }

    private void processMQPut() { // needs complete rework to extract MQMD data.
        DataGuiAdapter.findInstance().displayText(browseWindow, "Put messages...");
        boolean error;
        boolean moremsg = true;
        int i=0, flag = 0, msgsToWrite = 1, msgTot = 1, msgNum = 0 ;
        //msg variables
        HashMap tempMsgMap = null;
        String msgText;                             // the message
        msgsToWrite = putMessageList.size();
        msgTot = msgsToWrite;

        MQMessage theMessage = new MQMessage();                 // message object
        theMessage.format = MQC.MQFMT_STRING;
        MQPutMessageOptions pmo = new MQPutMessageOptions();    // accept the defaults, same

        //  pmo.options = MQC.MQPMO_FAIL_IF_QUIESCING;
        pmo.options = MQC.MQPMO_FAIL_IF_QUIESCING +
                      MQC.MQPMO_DEFAULT_CONTEXT +
                      MQC.MQPMO_NEW_CORREL_ID +
                      MQC.MQPMO_NEW_MSG_ID ;
        MQException.log = null;
        /*
        * This loop is used to put messages to the queue
        */
        
        do {
            try {
                theMessage.clearMessage();						// clear the message
                msgNum = msgTot - msgsToWrite;
                tempMsgMap = (HashMap) putMessageList.get(msgNum);
                msgText = (String) tempMsgMap.get("Message");
                System.out.println("Message " + msgText);
                theMessage.writeString(msgText);			  	// write the message
                theQueue.put(theMessage,pmo);             	 	// put the message

             } catch (MQException putex) {
                moremsg = false;
                DataGuiAdapter.findInstance().sendToOutputWindow("An error occured during mqput: Completion code "
                                                + putex.completionCode
                                                + " Reason code " + putex.reasonCode);
             } catch (IOException e) {
                    DataGuiAdapter.findInstance().sendToOutputWindow("An error occured writing the message object");
             }
             msgsToWrite = msgsToWrite - 1;
             if (msgsToWrite == 0) { moremsg = false; }
        } while (moremsg) ;                         //end of put loop
  }
    
    private void deleteMessages(){
        DataGuiAdapter.findInstance().displayText(browseWindow, "Remove messages...");
        //TODO iterate through "messageList", extract MQMD data and delete messages.

    }
        
    private void processMQGet() {  
        if (this.destructive) {
            DataGuiAdapter.findInstance().displayText(browseWindow, "Get messages...");
        } else {
            DataGuiAdapter.findInstance().displayText(browseWindow, "Browse messages...");
        }
        MQMessage theMessage = new MQMessage();                 // message object
        HashMap tempMsgMap = null;       
        int messageCount = 0 ;
        boolean moremsg = true ;
        //This loop is used to get messages from the queue
        MQGetMessageOptions gmo = new MQGetMessageOptions();    // accept the defaults
                gmo.options = MQC.MQGMO_BROWSE_NEXT +
                              MQC.MQGMO_WAIT +
//                              MQC.MQGMO_CONVERT +                              
                              MQC.MQGMO_FAIL_IF_QUIESCING ;
       
       if (convert) { gmo.options = gmo.options + MQC.MQGMO_CONVERT ;}
        
       do {
          if ((this.msgId != null) && (this.msgId.equals("")))  {
              theMessage.messageId = msgId.getBytes();
          } else {
              theMessage.messageId = MQC.MQMI_NONE;
          }                             // msgId = "" get any message
          theMessage.correlationId = MQC.MQCI_NONE;
          // correlationId = "" get any message
          try {
               currentMQI = "MQGET" ;
               theQueue.get(theMessage,gmo);                        // get the message
               int proceed = 1;
               
               messageCount++ ;
               tempMsgMap = new HashMap();   // tempMsgMap is a hash of labled msg data, incl MQMD header info, 1 per message.

               try {
                  if (messageCount <= 20){
                      tempMsgMap.put("Message", theMessage.readStringOfCharLength(theMessage.getMessageLength())); // add message to hashmap
                      tempMsgMap.put("HexMessage", getHexMessage(theMessage, theMessage.getDataLength()));  // add hex message to hashmap
                  } else {
                      // just 100 bytes where number of msgs is over 20 - or could be a prob for thou's msgs
                      int shortMsgLength = Math.min(100, theMessage.getMessageLength());
                      tempMsgMap.put("Message", theMessage.readStringOfCharLength(shortMsgLength)); // add message to hashmap
                      tempMsgMap.put("HexMessage", getHexMessage(theMessage, shortMsgLength));  // add hex message to hashmap
                  }
               } catch (IOException e) {
                  tempMsgMap.put("Message", "There was an error processing current msg: io exception: " + e);
               }
               
               getMQMD(theMessage, tempMsgMap);  // populate hashmap with MQMD data.
               messageList.add((HashMap) tempMsgMap); //  messageList is an array of hash tables, 1 hash per message.              
          } catch (MQException getex) {
             moremsg = false;
             if (getex.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE ) {
                DataGuiAdapter.findInstance().displayMessages(browseWindow, (ArrayList) messageList.clone()); // send to gui.
             } else {
                DataGuiAdapter.findInstance().sendToOutputWindow(currentMQI + " Failed : Completion code " + getex.completionCode + " Reason code " + getex.reasonCode);
             }
             System.out.println(currentMQI + " Failed");
          }
        } while (moremsg) ;        
        DataGuiAdapter.findInstance().displayMessages(browseWindow, messageList);
    }    
        
    private void getMQMD(MQMessage myMessage, HashMap theMsgMap) {
      int i, msgLength;
      String oneByte, valueString, outputText;      
      
      theMsgMap.put("StrucID", "MD");
      theMsgMap.put("Version", String.valueOf(myMessage.getVersion()));
      theMsgMap.put("Report", String.valueOf(myMessage.report));
      theMsgMap.put("MsgType", String.valueOf(myMessage.messageType));
      theMsgMap.put("Expiry", String.valueOf(myMessage.expiry));
      theMsgMap.put("Feedback", String.valueOf(myMessage.feedback));
      theMsgMap.put("Encoding", String.valueOf(myMessage.encoding));
      theMsgMap.put("CodedCharSetId", String.valueOf(myMessage.characterSet));
      theMsgMap.put("Format", String.valueOf(myMessage.format));
      theMsgMap.put("Priority", String.valueOf(myMessage.priority));
      theMsgMap.put("Persistence", String.valueOf(myMessage.persistence));
      theMsgMap.put("BackoutCount", String.valueOf(myMessage.backoutCount));
      theMsgMap.put("ReplyToQ", String.valueOf(myMessage.replyToQueueName));
      theMsgMap.put("ReplyToQMgr", String.valueOf(myMessage.replyToQueueManagerName));      
      theMsgMap.put("UserIdentifier", String.valueOf(myMessage.userId));
      theMsgMap.put("ApplIdentityData", String.valueOf(myMessage.applicationIdData));
      theMsgMap.put("PutApplType", String.valueOf(myMessage.putApplicationType));
      theMsgMap.put("PutApplName", String.valueOf(myMessage.putApplicationName));
      theMsgMap.put("MsgSeqNumber", String.valueOf(myMessage.messageSequenceNumber));
      theMsgMap.put("Offset", String.valueOf(myMessage.offset));
      theMsgMap.put("MsgFlags", String.valueOf(myMessage.messageFlags));
      theMsgMap.put("OriginalLength", String.valueOf(myMessage.originalLength));
      
      myMessage.putDateTime.add(Calendar.HOUR,11) ; // convert UTC to AEDT      
      valueString = (myMessage.putDateTime.get(Calendar.MONTH)+1) + "/" + myMessage.putDateTime.get(Calendar.DATE) + "/" + myMessage.putDateTime.get(Calendar.YEAR);
      theMsgMap.put("PutDate", valueString);              
      valueString = myMessage.putDateTime.get(Calendar.HOUR_OF_DAY) + ":" + myMessage.putDateTime.get(Calendar.MINUTE) + ":" + myMessage.putDateTime.get(Calendar.SECOND);
      theMsgMap.put("PutTime", valueString);      
      theMsgMap.put("ApplOriginData", String.valueOf(myMessage.applicationOriginData));
      
      valueString = "";
      for (i=0; i < MQC.MQ_MSG_ID_LENGTH; i += 1) {
         oneByte = hexToString(myMessage.messageId[i]);
             valueString = valueString + String.valueOf(oneByte);
         }      
      theMsgMap.put("MsgId", String.valueOf(valueString));      
      valueString = "";      
      for (i=0; i < MQC.MQ_CORREL_ID_LENGTH; i += 1) {
         oneByte = hexToString(myMessage.correlationId[i]);
             valueString = valueString + String.valueOf(oneByte);
          }
      theMsgMap.put("CorrelId", String.valueOf(valueString));
      valueString = "";    
      for (i=0; i < MQC.MQ_ACCOUNTING_TOKEN_LENGTH; i += 1) {
         oneByte = hexToString(myMessage.accountingToken[i]);
             valueString = valueString + String.valueOf(oneByte);
          }
      theMsgMap.put("AccountingToken", String.valueOf(valueString));
      valueString = "";   
      for (i=0; i < MQC.MQ_GROUP_ID_LENGTH; i += 1) {
         oneByte = hexToString(myMessage.groupId[i]);
             valueString = valueString + String.valueOf(oneByte);
          }
      theMsgMap.put("GroupId", valueString); 
 
  } // end of getMQMD


  private String getHexMessage(MQMessage myMessage, int retainedDataLength) {
        int            i, msgLength, currentLine, remainingLine, msgTextIndex, msgTextIndex2;
        int            overlayPos;
        String         textDumpOffset, textDump;
        String         outputText;
        boolean        moreMsg = true;

        try {
            //msgLength = myMessage.getMessageLength();
            msgLength = myMessage.getDataLength();
            remainingLine = msgLength;
            byte[] msgText = new byte[msgLength];
            myMessage.readFully(msgText);
            msgTextIndex = 0;
            msgTextIndex2 = 0;
            do {
//                if ((remainingLine > msgLength - retainedDataLength) && (remainingLine > 16)) {
                if ((remainingLine > msgLength) && (remainingLine > 16)) {
                    currentLine = 16;
                    remainingLine -= 16;
                } else {
                    currentLine = remainingLine;
                    moreMsg = false;
                }
                textDump = "00000000:                                          ";
                textDumpOffset = Integer.toHexString(((msgTextIndex / 16) * 16));
                textDump = overlay(textDump,9 - textDumpOffset.length(),textDumpOffset);
                overlayPos = 10;
                for (i = 0; i < currentLine; i += 1) {
                     if (i % 2 == 0) overlayPos +=1;
                     textDump = overlay(textDump,overlayPos,hexToString(msgText[msgTextIndex]));
                     overlayPos +=2;
                     msgTextIndex += 1;
                }//for
                outputText = textDump + "\t '";

                for (i = 0; i < currentLine; i += 1) {
                    if ((int)(msgText[msgTextIndex2]) > 31) {
                        char myChar = (char)msgText[msgTextIndex2] ;
                        outputText = outputText + myChar +"";
                            }
                    else { outputText = outputText + "."; }
                    msgTextIndex2 += 1;
                }//for
                outputText = outputText + "'\n";
           } while (moreMsg);
           outputText = outputText + "\n\n";
      } catch (IOException e) {
            //Exceptions.printStackTrace(e);
            outputText = "An error occured accessing the message object - " + e + "\n";
      }
      return outputText;
  } // end of getHexMessage

  private static String hexToString(byte myByte){
      String[] byteArray  = {"00","01","02","03","04","05","06","07","08","09","0A","0B","0C","0D","0E","0F",
                             "10","11","12","13","14","15","16","17","18","19","1A","1B","1C","1D","1E","1F",
                             "20","21","22","23","24","25","26","27","28","29","2A","2B","2C","2D","2E","2F",
                             "30","31","32","33","34","35","36","37","38","39","3A","3B","3C","3D","3E","3F",
                             "40","41","42","43","44","45","46","47","48","49","4A","4B","4C","4D","4E","4F",
                             "50","51","52","53","54","55","56","57","58","59","5A","5B","5C","5D","5E","5F",
                             "60","61","62","63","64","65","66","67","68","69","6A","6B","6C","6D","6E","6F",
                             "70","71","72","73","74","75","76","77","78","79","7A","7B","7C","7D","7E","7F",
                             "80","81","82","83","84","85","86","87","88","89","8A","8B","8C","8D","8E","8F",
                             "90","91","92","93","94","95","96","97","98","99","9A","9B","9C","9D","9E","9F",
                             "A0","A1","A2","A3","A4","A5","A6","A7","A8","A9","AA","AB","AC","AD","AE","AF",
                             "B0","B1","B2","B3","B4","B5","B6","B7","B8","B9","BA","BB","BC","BD","BE","BF",
                             "C0","C1","C2","C3","C4","C5","C6","C7","C8","C9","CA","CB","CC","CD","CE","CF",
                             "D0","D1","D2","D3","D4","D5","D6","D7","D8","D9","DA","DB","DC","DD","DE","DF",
                             "E0","F1","E2","E3","E4","E5","E6","E7","E8","E9","EA","EB","EC","ED","EE","EF",
                             "F0","F1","F2","F3","F4","F5","F6","F7","F8","F9","FA","FB","FC","FD","FE","FF"};
      if ((int)myByte >= 0) {
        return(byteArray[(int)myByte]);
      } else {
        return(byteArray[(int)myByte+256]);
      }
  } // end of hexToString

  private static String overlay(String myString, int pos, String newValue){
       String part1, part2;
       part1 = myString.substring(0,pos - 1);
       part2 = myString.substring(pos -1 + newValue.length());
       return(part1 + newValue + part2);
  } // end of overlay
   
}
