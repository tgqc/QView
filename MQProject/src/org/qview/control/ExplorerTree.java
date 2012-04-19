/*
 * Tree.java
 *
 * Based on the 'PropChildren' class, Netbeans project,
 * created on June 13, 2005, 2:07 PM
 *
 * This class builds the tree structure the org.openide.nodes.Node object class,
 * and is attached to the root node 'RootNode'.
 */

package org.qview.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.qview.data.ObjectRepository;
import org.qview.data.mqmodel.WMQCluster;
import org.qview.data.mqmodel.WMQObject;
import org.qview.data.mqmodel.WMQQMgr;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.qview.gui.nbexplorer.*;

/**
 *
 * @author T.Goodwill
 */
public class ExplorerTree extends Children.Keys {
    private static String defaultName = RootNode.getTopName();
    private String networkName;   
    private ObjectRepository repository;
    private ChangeListener listener;    
    private String nodeName;
    private String parentName = "";
    private String nodeType;
    
    public ExplorerTree(){
        super();        
        this.nodeName = defaultName;        
        repository = ObjectRepository.findInstance(networkName);        
    }
    public ExplorerTree(EntryPoint ep){
        super();
        this.nodeName = ep.getName();
        networkName = this.nodeName;
        repository = ObjectRepository.findInstance(networkName);        
    }
    public ExplorerTree(String thisName){
        super();
        this.nodeName = thisName;        
    }
    public ExplorerTree(String thisName, String parentNm){
        super();
        this.nodeName = thisName;
        this.parentName = parentNm;        
    }
    
    protected void addNotify() {  
        refreshList();
        PropertiesNotifier.addChangeListener(listener = new
                ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                refreshList();                
            }
        });
    }
    protected void removeNotify() {
        if (listener != null) {
            PropertiesNotifier.removeChangeListener(listener);
            listener = null;
        }
        setKeys(Collections.EMPTY_SET);
    }
    
    protected Node[] createNodes(Object key) {     
        ObjectRepository repository = null;
        String networkName = (String) this.getNode().getValue("networkName");
        Node[] node;
        // TODO - refresh tree with possible additional notes without destroying existing nodes (and explored context)
//        if (this.getNode().getChildren() != null) {
//            Node thisNode = this.getNode().getChildren().findChild(nodeName);
//            if (thisNode != null) {
//                return (new Node[]{thisNode});
//            }
//        }

        if (networkName != null){
            repository = ObjectRepository.findInstance(networkName);
        }
        if (nodeType.equals("Top")){
            node = new Node[] { new RootNode((String) key, new ExplorerTree((EntryPoint)EntryPoint.findInstance((String)key))) };
        } else if ((nodeType.equals("EntryPoint")) || (nodeType.equals("Cluster")) || (nodeType.equals("Clusters"))){      // || (nodeType.equals("Unclustered"))
            if (repository.getFromRepository((String)key) != null) {                
                WMQObject obj = repository.getFromRepository((String)key);            
                node = new Node[] { new ParentNode(obj, new ExplorerTree((String)key), networkName, obj.getTypeString()) };
            } else if ((repository.getQMgr((String)key)) != null) {
                WMQObject obj = repository.getQMgr((String)key);
                node = new Node[] { new ParentNode(obj, new ExplorerTree(obj.getUniqueName()), networkName, obj.getTypeString()) };
            } else {
                node = new Node[] { new ParentNode((String) key, new ExplorerTree((String)key), networkName, (String) key) };
            }            
        } else if (nodeType.equals("QMgr")){
            WMQObject obj = repository.getFromRepository(this.nodeName);
            if (!obj.getUpdated().equalsIgnoreCase("false")) {
                String newNodeName = (String) key;
                String newNodeType = newNodeName;
                System.out.println("newNodeName: " + newNodeName);
                // for Qmgr children (object classes/catagories), key is preficed by QMgr ID, suffixed by class name.
                if (newNodeName.substring(newNodeName.lastIndexOf(".")) != null) {
                    newNodeType = newNodeName.substring(newNodeName.lastIndexOf(".")+1);
                }
                node = new Node[] { new ParentNode((String) key, new ExplorerTree((String)key, this.nodeName), networkName, newNodeType) };
            } else node = null;
        } else if ( (nodeType.equals("Queues"))|| (nodeType.equals("Channels")) ){
            WMQObject obj = repository.getFromRepository((String)key);
//            //conditions for displaying system objects
//            boolean notSystem = !obj.getCaption().startsWith("SYSTEM") && !obj.getCaption().startsWith("AMQ") && !obj.getCaption().startsWith("MQAI");
//            boolean showSystemIsSelected = (((String)(this.getNode().getValue("system"))).equals("show") && !obj.getCaption().startsWith("AMQ"));
//            if (notSystem || showSystemIsSelected){
                node = new Node[] { new LeafNode(obj, networkName, obj.getTypeString()) };
//            } else node = null;
        } else node = null;

        return node;
    }    
    
    private void refreshList() {
        if (this.getNode().getClass() == ParentNode.class){
            ParentNode nodeObject = (ParentNode)this.getNode();            
            nodeObject.refreshSheet();
        } else if (this.getNode().getClass() == LeafNode.class){
            LeafNode nodeObject = (LeafNode)this.getNode(); 
            nodeObject.refreshSheet();
        }
        List keys = getChildList();        
        setKeys(keys); 
    }
    
    private ArrayList getChildList(){        
        ObjectRepository repository = null;
        String networkName = (String) this.getNode().getValue("networkName");
        if (networkName != null){
            repository = ObjectRepository.findInstance(networkName);
        }
        ArrayList newChildList = new ArrayList();        
        this.nodeType = (String) this.getNode().getValue("nodeType");
        System.out.println("getChildList() : " + nodeType);
        if (nodeType.equalsIgnoreCase("Top")){             
            if (EntryPoint.getEntryPoints().isEmpty()) {
//                EntryPoint.reloadEntryPoints();
//                if (EntryPoint.getEntryPoints().isEmpty()) {
                    EntryPoint.findInstance(EntryPoint.GetDefaultEPName());
//                }
            }
            newChildList = EntryPoint.getEntryPoints();
            if (!newChildList.isEmpty()){
                Collections.sort(newChildList);
            } 
        } else if (nodeType.equalsIgnoreCase("EntryPoint")){
            repository = ObjectRepository.findInstance(this.nodeName);
            networkName = this.nodeName;
            
            ArrayList clusters = repository.getClusters();
            if (!clusters.isEmpty()){
                Collections.sort(clusters);
            } 
            newChildList = clusters;
            
            ArrayList unclustered = repository.getUnclusteredQMgrs();
            if (!unclustered.isEmpty()){
                Collections.sort(unclustered);
            } 
            Iterator e = unclustered.iterator();            
            while (e.hasNext()) { 
                newChildList.add((String)e.next());
            }            
//            if (!repository.getUnclusteredQMgrs().isEmpty() && !newChildList.contains("Unclustered")){
//                newChildList.add("Unclustered");                
//            }
        } else if (nodeType.equalsIgnoreCase("Cluster")){            
            WMQCluster clus = (WMQCluster)repository.getFromRepository(this.nodeName);
            System.out.println("WMQCluster clus : " + clus.getUniqueName());
            if (clus != null){
                newChildList = clus.getQMgrs();
                if (!newChildList.isEmpty()){
                    Collections.sort(newChildList);
                }                 
            }            
//        } else if (nodeType.equalsIgnoreCase("Unclustered")){ 
//            newChildList = repository.getUnclusteredQMgrs();
        } else if (nodeType.equalsIgnoreCase("QMgr")){            
            newChildList.add(this.getNode().getName() + "." + "Channels");
            newChildList.add(this.getNode().getName() + "." + "Queues");
        } else if (nodeType.equalsIgnoreCase("Queues")){                     
            WMQQMgr qmgr = (WMQQMgr) repository.getFromRepository(this.parentName);
            if (qmgr != null) {
                newChildList = qmgr.getQueues();
                if (!newChildList.isEmpty()){
                    Collections.sort(newChildList);
                } 
            }            
        } else if (nodeType.equalsIgnoreCase("Channels")){                       
            WMQQMgr qmgr = (WMQQMgr) repository.getFromRepository(this.parentName);
            if (qmgr != null) {
                 newChildList = qmgr.getChannels();
                     if (!newChildList.isEmpty()){
                    Collections.sort(newChildList);
                } 
            } 
        }
        return newChildList;
    }   
 
}