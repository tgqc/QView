/*
 * RemMqsc.java
 *
 * Created on 23 April 2007, 18:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.data.mqinterface;

/**
 *
 * @author T.Q.Nguyen
 */
import java.io.*;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;
import org.qview.gui.console.Console;
import org.qview.data.mqmodel.WMQQMgr;
/**
 * send PCF escape command and mqsc command to qmgr and display result
 *
 *
 */
public class RemMqsc implements Runnable
{
   final public static String copyright = "Copyright 2003-2007 (c) Trung Nguyen, all rights reserved";
   private Console theConsole= null;
   private Thread remMqscThread;
   private PCFMessageAgent agent = null;
   private String queueManagerName = "";
   private String localQMgrName = "";
   private String commandQueueName = "SYSTEM.ADMIN.COMMAND.QUEUE";
   private String hostAddress = "";
   private String input = "";
   private String channelName = "SYSTEM.ADMIN.SVRCONN";
   private int portNumber = 1414;

//   private int n ;   
//   private int MQ_Q_NAME_LENGTH = 48;
//   private int MQCA_COMMAND_INPUT_Q_NAME = 2003 ;
//   private int MQIA_PLATFORM = 32 ;
//   int MQCA_Q_MGR_NAME = 2015;
//   int MQ_Q_MGR_NAME_LENGTH = 48;

   static boolean zOSPlatform = false ;
   String		  replyQueueName = "SYSTEM.DEFAULT.MODEL.QUEUE" ;
   MQQueue		  theQueue ;
   MQQueue        theReplyQueue ;
   String		  currentMQI ;
   int            waitTime = 1000;
   int            msgLength ;
   String         msgText ;
   MQQueueManager qMgr;                     // define a queue manager object
   int            openOutOptions = MQC.MQOO_OUTPUT |
                  	               MQC.MQOO_FAIL_IF_QUIESCING ;
   int            openInOptions = MQC.MQOO_INPUT_SHARED |
		                          MQC.MQOO_FAIL_IF_QUIESCING ;
   public RemMqsc(WMQQMgr qmgr) {
        // initialize console
        theConsole = new Console();
        // Construct a PCFMessageAgent
        agent = new PCFMessageAgent();
        if ( (1178517273) < System.currentTimeMillis()/1000) {
                 theConsole.writeText(" ");
                 theConsole.writeText(" ***  QView RemMqsc program ***");
                 theConsole.writeText(" ");
        }

        queueManagerName = qmgr.getCaption();
        hostAddress = qmgr.getHostName();
        MQEnvironment.hostname = hostAddress;
        channelName = qmgr.getSvrConnChl();
        MQEnvironment.channel = channelName;
        portNumber = qmgr.getPort().intValue();
        MQEnvironment.port = portNumber;

        WMQQMgr localQMgr = qmgr.getLocalQMgr();
        if (localQMgr != null) {
            localQMgrName = localQMgr.getCaption();
            MQEnvironment.localAddressSetting = localQMgr.getHostName();
        }

        remMqscThread = new Thread(this);
        remMqscThread.setDaemon(true);
        remMqscThread.start();
   }

   public void run() {      
      try {            
          checkPlatform();
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      if (zOSPlatform) {
            try {
                connectZOS();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                startZOS();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
      } else {
            try {
                connectPCF();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                startPCF();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
      }
      remMqscThread.stop();
   }


   public void connectPCF() throws Exception {
      // Turn off MQ's internal exception logging
      MQException.log = null;
      // Was a host or IP address supplied?
      if (hostAddress.length() == 0) {
         // connect in bindings mode
         try {
            if (localQMgrName.length() > 0) {
                 agent.connect(queueManagerName,commandQueueName,localQMgrName);
            } else {
                 // Connect the agent to the Queue Manager
                 agent.connect(queueManagerName);
            }
         } catch (MQException mqe) {
            // Issue a suitable message
            theConsole.writeText("Connect to Queue Manager [" + queueManagerName +
                               "] failed, reason=" + mqe.reasonCode);
            System.exit(2);
         }
      } else {
         // Try and connect in client mode
         try {
            if (localQMgrName.length() > 0) {
                  agent.connect(hostAddress, portNumber, channelName,commandQueueName,localQMgrName);
            } else {
                  // Connect the agent to the Queue Manager
                  agent.connect(hostAddress, portNumber, channelName);
            }
         } catch (MQException mqe) {
            // Issue a suitable message
            theConsole.writeText("Connect to Queue Manager at " + hostAddress + 
                                 " port " + portNumber +
                                 " channel " + channelName +
                                 " failed, reason=" + mqe.reasonCode);
            System.exit(2);
         }
      }
   }

   public void startPCF() throws Exception {
      PCFMessage request;
      PCFMessage[] response;
      boolean moreCmd = true;
      boolean header = false;
      queueManagerName = agent.getQManagerName();
      theConsole.writeText("\nStarting MQSC for queue manager " + queueManagerName + "\nEnter \"end\" to exit" );
  
      if (localQMgrName.length() > 0){
          theConsole.writeText("\n....All commands will be directed to " + localQMgrName +"\n");
  	  }
      /*
      * the Console contains the readLine method.
      * Create a new instance for standard input System.in
      */

      // Start operation
      do {
         try {
            // Create the request message
            request = new PCFMessage(CMQCFC.MQCMD_ESCAPE);            /* escape cmd  */
            request.addParameter(CMQCFC.MQIACF_ESCAPE_TYPE, CMQCFC.MQET_MQSC); /* escape type */
            //request.addParameter(CMQCFC.,CMQC.MQPER_NOT_PERSISTENT); /* escape type */
            // get the command
            
            theConsole.writeText("\n");
            input = theConsole.readLine();           
 
            if (input.length() > 0){
               if (!input.equalsIgnoreCase("end")) {
                  request.addParameter(CMQCFC.MQCACF_ESCAPE_TEXT, input); /* the cmd     */
                  // Send the request to the Queue Manager
                  response = agent.send(request);
                  this.printResult(response) ;
               } else {
                   moreCmd = false;
               } // end of equalsIgnoreCase
            } // end of input length > 0
         } catch (PCFException pex) {
             System.err.println("Error in PCF response, reason = " + pex.reasonCode);
         }
      }  while (moreCmd) ; /* end of do while */
      theConsole.writeText("\nDisconnected from queue manager " + queueManagerName + "\n");
   }

   public void printResult(PCFMessage[] response) throws Exception {
      // Process the responses
      for (int n = 0; n < response.length; n++) {
         // Get the data for each Queue
         String cmdResult = response[n].getStringParameterValue(CMQCFC.MQCACF_ESCAPE_TEXT);
         // Print result
         theConsole.writeText(cmdResult);
      } /* end of for n */
//      theConsole.writeText("\n");
   }

   public void disconnect() throws Exception {
      // Disconnect from the Queue Manager
      try {
		  if (zOSPlatform) { qMgr.disconnect(); }
          else { agent.disconnect();qMgr.disconnect();}
      } catch (MQException mqe) {
         theConsole.writeText("Disconnect failed, reason=" + mqe.reasonCode);
      }
   }

   public void checkPlatform() throws Exception {
      int   MQPL_ZOS = 1 ;
      int[] selectors = new int[3];
      int[] intAttrs = new int[1];
      byte[] charAttrs = new byte[CMQC.MQ_Q_NAME_LENGTH + CMQC.MQ_Q_MGR_NAME_LENGTH] ;

	  selectors[0] = CMQC.MQCA_COMMAND_INPUT_Q_NAME ;
	  selectors[1] = CMQC.MQIA_PLATFORM ;
	  selectors[2] = CMQC.MQCA_Q_MGR_NAME ;
	  try {
	   qMgr = new MQQueueManager(queueManagerName);         // Create a connection to the queue manager
		  qMgr.inquire(selectors,intAttrs,charAttrs);
		  commandQueueName = new String(charAttrs,0,CMQC.MQ_Q_NAME_LENGTH) ;
		  queueManagerName = new String(charAttrs,CMQC.MQ_Q_NAME_LENGTH,CMQC.MQ_Q_MGR_NAME_LENGTH).trim() ;
		  if (intAttrs[0] == MQPL_ZOS ) { zOSPlatform = true ;}
//	   qMgr.disconnect();        // Disconnect from the queue manager
	  } catch (MQException ex) {
          theConsole.writeText("An MQSeries error occured - CC:" + ex.completionCode + " RC:" + ex.reasonCode);
          System.exit(8) ;
	  } //end of try
   }

   public void connectZOS() throws Exception {

	   try {
		  currentMQI = "MQCONN ZOS" ;
//		  qMgr = new MQQueueManager(qmgrName);         // Create a connection to the queue manager
                  theConsole.writeText("\nStarting MQSC for queue manager " + queueManagerName + "\nEenter \"end\" to exit\n" );
            
		  // Now specify the queue that we wish to open, and the open options...
		  currentMQI = "MQOPEN CMD Queue";
		  theQueue = qMgr.accessQueue(commandQueueName,
						  openOutOptions,
						  null,           // qmgr must be null for cluster queue
						  null,           // no dynamic q name
						  null);          // no alternate user id
		  currentMQI = "MQOPEN CMD ReplyQueue";
		  theReplyQueue = qMgr.accessQueue(replyQueueName,
							  openInOptions,
							  localQMgrName,       // target qmgr
							  "CSQ.*",        // no dynamic q name
							  null);          // no alternate user
		} catch (MQException mqex) {
			theConsole.writeText(currentMQI + " failed with rc " + mqex.reasonCode );
			System.exit(8) ;
		}
   }

   public void startZOS() throws Exception {
       try {
             MQMessage theMessage = new MQMessage();
             MQMessage theReplyMessage = new MQMessage();
             theMessage.replyToQueueName = theReplyQueue.name ;
             theMessage.replyToQueueManagerName = localQMgrName;
             theMessage.messageType = MQC.MQMT_REQUEST;
             MQPutMessageOptions pmo = new MQPutMessageOptions();  // accept the defaults, same
                                                                    // as MQPMO_DEFAULT constant
             pmo.options = //MQC.MQPMO_NEW_MSG_ID |
                            //MQC.MQPMO_NEW_CORREL_ID |
                            MQC.MQPMO_FAIL_IF_QUIESCING |
                            MQC.MQPMO_NO_SYNCPOINT ;
               MQGetMessageOptions gmo = new MQGetMessageOptions();
               gmo.options = MQC.MQGMO_WAIT ;
               gmo.waitInterval = waitTime ;

              boolean gotMsg = false ;
              int     replyCount = 0;
            do {
                 try {
                      theMessage.clearMessage();           // clear message buffer first
                      theReplyMessage.clearMessage();           // clear message buffer first
                      input = readStdIO() ;
                      if (input.equalsIgnoreCase("end")) {theConsole.writeText("Goodbye ...");return;}
                      gotMsg = false ;
                      if (input.length() > 0) {
                          theMessage.writeBytes(input);
                          theMessage.format = MQC.MQFMT_STRING;                 // format = MQSTR
                          theMessage.messageId = MQC.MQMI_NONE;                 // msgId = ""
                          theMessage.expiry = 3000 ;
                          theMessage.correlationId = MQC.MQCI_NONE;
                          theMessage.messageType = MQC.MQMT_REQUEST;
                          currentMQI = "MQPUT" ;
                          theQueue.put(theMessage,pmo);            // put the message
                          theReplyMessage.messageId = MQC.MQMI_NONE ;
                          theReplyMessage.correlationId = theMessage.messageId ;                 // msgId = ""
                          currentMQI = "MQGET" ;
                          theReplyMessage.messageId = MQC.MQMI_NONE ;
                          theReplyMessage.correlationId = theMessage.messageId ;                 // msgId = ""
                          theReplyQueue.get(theReplyMessage,gmo) ;
                          gotMsg = true ;
                          msgLength = theReplyMessage.getMessageLength();
                          msgText = theReplyMessage.readString(msgLength);
                          theConsole.writeText(msgText);
                          replyCount = java.lang.Integer.parseInt(msgText.substring(msgText.indexOf("COUNT=")+6,msgText.indexOf(",")).trim());
                          do {
                              theReplyMessage.messageId = MQC.MQMI_NONE ;
                              theReplyMessage.correlationId = theMessage.messageId ;                 // msgId = ""
                              theReplyQueue.get(theReplyMessage,gmo) ;
                              gotMsg = true ;
                              msgLength = theReplyMessage.getMessageLength();
                              msgText = theReplyMessage.readString(msgLength);
                              theConsole.writeText(msgText.replaceAll("\\)",")\n\t"));
                             } while (true);
                          }
                      } catch(IOException e) {
                      theConsole.writeText("error occured processing theMessage : Completion code " + e);
                      } catch (MQException mqex) {
                         if (mqex.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
                            if (!gotMsg) { theConsole.writeText("Did not receive response to  " + input); }
                         } else {
                            theConsole.writeText(currentMQI + " failed with rc " + mqex.reasonCode );
                            System.exit(8) ;
                         }
                      }
              } while (!input.equalsIgnoreCase("end")) ;              //end of big do loop

              theQueue.close();           	           // Close the queue
              theReplyQueue.close();           	           // Close the queue

       } catch (MQException mqex) {
           // do nothing
       }
    }

public String readStdIO() {
  /*
   * the Console contains the readLine method.
   * Create a new instance for standard input System.in
   */
  boolean error = false ;

      String theCommand = "";
            error = false;
            //System.out.print ("Enter a MQSC command > ");
            theConsole.writeText("\n");
            theCommand = theConsole.readLine ();
            
       return(theCommand) ;
	} // end of readStdIO

    public void sentToMqsc(String input){

    }
 
}
