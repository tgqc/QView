/*
 * Connect.java
 *
 * Created on 1 June 2006, 12:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.control;

import org.qview.data.ObjectRepository;
import org.qview.data.mqinterface.*;
import org.qview.data.mqmodel.WMQQueue;
import org.qview.data.mqmodel.WMQQMgr;
/**
 * 
 * 
 */
public class Discovery extends Thread {    
    private static ObjectRepository repository;
    private EntryPoint entryPoint;
    private WMQQMgr qmgrObject = null;
    private WMQQueue queueObject = null;    
    private String hostName = "localhost";
    private Integer port = Integer.valueOf("1414");
    private String channel = "SYSTEM.ADMIN.SVRCONN";
    private String instName;    
    
    public Discovery(String conName) {
        this(EntryPoint.findInstance(conName));
    }
    public Discovery(EntryPoint ep) {
        
        entryPoint = ep;
        instName = ep.getName();
        
        if (ep.getDiscovery("Host") != null){
            hostName = (String) ep.getDiscovery("Host");
        } else {
            hostName = "localhost";
        }
        if (ep.getDiscovery("Port") != null){
            port = (Integer) ep.getDiscovery("Port");
        } else {
            port = Integer.valueOf("1414");
        }
        if (ep.getDiscovery("Channel") != null){
            channel = (String) ep.getDiscovery("Channel");
        } else {
            channel = "SYSTEM.DEF.SVRCONN";
        }
               
        repository = ObjectRepository.createInstance(instName, port, channel);
    }

    /** Iterate through unmapped Qmgrs in reposirtory and manage discovery threads.
     *  TODO - The total (possible) number of threads spawned is multiplied by the number of networks defined
     *  - may need to be synchronized (1 network at a time), or total threads capped */
    public void run() {
       
        System.out.println();
        System.out.println("*Discovering Network*");
        entryPoint.updateOutput("\n*Discovering Network*\n");        
        
//        repository.setObjectsToUnmapped();
        repository.orphanAll();
        repository.reportQMgr((hostName + "(" + port + ")"), hostName, port);
               
        int maxThreads = 32;
        int iterations = 0;
        ThreadGroup threadgroup = new ThreadGroup(instName + "Discovery");

        while ((repository.getUnmappedQMgr() != null) && (entryPoint.pollingEnabled())){
            iterations = iterations + 1;
            WMQQMgr qMgr = repository.getUnmappedQMgr();

            System.out.println("thread activecount: " + String.valueOf(threadgroup.activeCount()) + "\n" + "last qmgr: " + qMgr.getCaption() + "\n");

            // threaded QMgr discovery routine
              if ((threadgroup.activeCount() < maxThreads) && (qMgr != null)){ 
                  MQObjectDiscover discover = new MQObjectDiscover(threadgroup, String.valueOf(iterations), qMgr);
                  qMgr.setUpdated("updating");
                  discover.start();
                  System.out.println("threads : " + threadgroup.activeCount());
              }                            

              while ((threadgroup.activeCount()>0) && (repository.getUnmappedQMgr() == null)) {
                try {
                    sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                System.out.println("waiting thread activecount: " + String.valueOf(threadgroup.activeCount()) + "\n" + "last qmgr: " + qMgr.getCaption() + "\n");
                
              }
//              }
            entryPoint.updateGui();
        }//while          
                 
//        repository.removeOrphaned();
        StatusPoll nextPoll = new StatusPoll(instName);        
        nextPoll.start();
//        entryPoint.updateGui();
        
        while (threadgroup.activeCount()>0){
            try {
               sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        entryPoint.updateGui();
        entryPoint.setTimer();
        entryPoint.updateTimestamp();
//        repository.printAll();
        System.out.println("final thread waiting activecount: " + String.valueOf(threadgroup.activeCount()) + "\n");
    } // run
    

    
    
} // Discovery
