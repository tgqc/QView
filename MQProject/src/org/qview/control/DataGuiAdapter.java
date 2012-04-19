/*
 * GuiAdapter.findInstance().java
 *
 * Created on 11 December 2006, 13:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.control;

import java.util.ArrayList;
import java.util.Iterator;
import org.qview.data.mqmodel.WMQQueue;
import org.qview.gui.browse.BrowseTopComponent;
import org.qview.gui.OutputTopComponent;
import org.qview.gui.hypertree.HTreeTopComponent;
import org.qview.gui.nbexplorer.NodeExplorerTopComponent;
import org.qview.gui.nbexplorer.RootNode;
import org.qview.data.mqinterface.MessageUtils;
import org.openide.windows.TopComponent;

/**
 *
 * @author T.R.Goodwill
 */
public class DataGuiAdapter {
    
    private static DataGuiAdapter instance;    
    
    /**
     * Creates a new instance of GuiAdapter
     */
    public DataGuiAdapter() {
        instance = this;
    }    
    public synchronized static DataGuiAdapter findInstance() {
        if (instance == null) {
            instance = new DataGuiAdapter();            
        }
        return instance;
    }
    
//---browse window-----------------------------------------------------------    
    
    public synchronized BrowseTopComponent openBrowse(WMQQueue queue){
        BrowseTopComponent btc = BrowseTopComponent.findInstance();
        btc.setQueue(queue);        
        TopComponent win = btc;
            win.open();
            win.requestActive();
            
        if (!btc.getQueueName().equals(queue.getCaption())) {
            btc.setQueue(queue);
        }      
        
        return btc;
    }
    
    public synchronized void browseMsgs(WMQQueue queue, boolean convert) {        
        BrowseTopComponent btc = openBrowse(queue);
        System.out.println("browseMsgs... " + queue.getCaption());
        MessageUtils browseMessages = new MessageUtils(queue, btc, convert);
        browseMessages.start();
    }
    public synchronized void browseMsgs(WMQQueue queue, boolean convert, boolean destructive, String msgId) {
        BrowseTopComponent btc = openBrowse(queue);
        System.out.println("browseMsgs... " + queue.getCaption());
        MessageUtils browseMessages = new MessageUtils(queue, btc, convert, destructive, msgId);
        browseMessages.start();
    }

    public synchronized void openPut(WMQQueue queue) {                                      
        // simply opens browse window for input.
        BrowseTopComponent btc = openBrowse(queue);        
    }
    public synchronized void putMsgs(WMQQueue queue, Object window, ArrayList msgList) {                                      
        // put msgs to queue
        System.out.println("msgList " + msgList.size());
        MessageUtils putMessages = new MessageUtils(queue, window, msgList);
        putMessages.start();
    }
    
    public synchronized void deleteMsgs(WMQQueue queue, Object window, ArrayList msgList){
        BrowseTopComponent btc = (BrowseTopComponent) window;
        MessageUtils deleteMessages = new MessageUtils(queue, btc, msgList, true);
        deleteMessages.start();
//        TODO -  needs qdepth update.
    }

    public synchronized void displayText(Object window, String text){
        BrowseTopComponent btc = (BrowseTopComponent) window;
        // TODO
        btc.addText(text);
    }
    
    public synchronized void displayMessages(Object window, ArrayList msgList){
        System.out.println("%%%%%% display messages " + window.toString() + " " + msgList.toString());
        BrowseTopComponent btc = (BrowseTopComponent) window;
        // TODO
        btc.setMessageList(msgList);
        btc.displayBrowse();        
    } 
    
//---output window------------------------------------------------------------  
    
    public synchronized void sendToOutputWindow(String output){
        OutputTopComponent otc = OutputTopComponent.findInstance();
        otc.setText(output);
        otc.requestActive();
    }
    public synchronized void addToOutputWindow(String output){
        OutputTopComponent otc = OutputTopComponent.findInstance();
        otc.addText(output);
//        otc.requestActive();
    }

//---refresh displays------------------------------------------------------------        
    
    public synchronized void refreshProps() {
        RootNode.refreshNode();        
    }
    // refresh nodes
    public synchronized void refreshNodes() { 
        // iterate through all entry points (networks) and refresh all.
        ArrayList entryPoints = EntryPoint.getEntryPoints();
        Iterator e = entryPoints.iterator();
        while (e.hasNext()){
            String instanceName = (String) e.next();
            HyperTree.findInstance(instanceName);                   
            HTreeTopComponent.refreshInstance(instanceName);
        }
        // refresh explorer root node
        RootNode.refreshNode();
    }
    public synchronized void refreshNode(String instanceName) {
        // find seleted HT node name in order to recenter after refresh
        HyperTree.findInstance(instanceName);
        HTreeTopComponent.refreshInstance(instanceName);
        // refresh explorer root node        
        RootNode.refreshNode();

    }
    public synchronized void resetNode() {
        NodeExplorerTopComponent.resetExplorer();
    }   

}
