/*
 * StatusPoll.java
 *
 * Created on 21 March 2007, 18:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.control;

import org.qview.data.ObjectRepository;
import java.util.*;
import org.qview.data.mqinterface.*;
import org.qview.data.mqmodel.WMQQMgr;

/**
 *
 * @author T.R.Goodwill
 */
public class StatusPoll extends Thread {
    private static ObjectRepository repository;    
    private String networkName;
    private EntryPoint entryPoint;
    private WMQQMgr qMgr = null;
    ArrayList qMgrs = new ArrayList();
    private String hostName = "localhost";
    private Integer port = Integer.valueOf("1414");
    private String svrConnChannel = "SYSTEM.DEF.SVRCONN";
        
    /** Creates a new instance of StatusPoll */
    public StatusPoll(String network) {
        this.networkName = network;
        this.entryPoint = EntryPoint.findInstance(network);
        this.repository = ObjectRepository.findInstance(networkName);   
    }
    
    public synchronized void run() {
        System.out.println("*Begin Status Update*");
        
        repository.setObjectsToUnmapped();
        int maxThreads = 32;
        int iterations = 0;
        ThreadGroup threadgroup = new ThreadGroup(networkName + "Status");     
        
        qMgrs = repository.getQMgrUniqueNames();         
        Iterator e = qMgrs.iterator(); 
        
        entryPoint.updateOutput("\n*Begin Status Update*\nUpdating : ");
        
        while ((e.hasNext()) && (entryPoint.pollingEnabled())){
            iterations = iterations + 1;            
              // thread routine.
              if (threadgroup.activeCount() < maxThreads){
                  this.qMgr = (WMQQMgr) repository.getFromRepository((String) e.next());
                  if ((qMgr!= null) && (qMgr.isPollable())){
                      entryPoint.updateOutput(qMgr.getCaption() + ", ");
                      MQObjectStatus status = new MQObjectStatus(threadgroup, String.valueOf(iterations), qMgr);                       
                      status.start();
                      System.out.println("status threads : " + threadgroup.activeCount());
                  }
              }
        }
        while (threadgroup.activeCount()>0){
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }        
        entryPoint.updateGui();
        entryPoint.updateOutput("\nStatus Update Done.\n");
        entryPoint.updateTimestamp();
        System.out.println("\nStatus Update Done.\n");
    }    
    
}
