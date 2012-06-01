/*
 * ModelAdapter.findInstance().java
 *
 * Created on 7 March 2007, 15:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.control;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.actions.PropertiesAction;
import org.openide.explorer.ExplorerManager;
//import org.openide.util.Exceptions;
import org.qview.control.Discovery;
import org.qview.gui.hypertree.HTreeTopComponent;
import org.qview.gui.nbexplorer.LeafNode;
import org.qview.gui.nbexplorer.ParentNode;
import org.qview.control.DataGuiAdapter;
import org.qview.data.mqmodel.WMQCluster;
import org.qview.data.mqinterface.RemMqsc;
import org.qview.control.StatusPoll;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.qview.gui.hypertree.HTNode;
import org.qview.gui.hypertree.HTNodeBase;
import org.qview.gui.nbexplorer.NodeExplorerTopComponent;
import org.qview.gui.nbexplorer.RootNode;
import org.qview.control.EntryPoint;
import org.qview.data.ObjectRepository;
import org.qview.data.mqmodel.WMQChannel;
import org.qview.data.mqmodel.WMQObject;
import org.qview.data.mqmodel.WMQQMgr;
import org.qview.data.mqmodel.WMQQueue;
import org.qview.data.mqinterface.MQConstants;
import org.qview.data.mqinterface.MQObjectAlter;
import org.qview.data.mqmodel.WMQProcess;
import org.qview.gui.hypertree.HTree;

/**
 *
 * @author T.R.Goodwill
 */
public class GuiDataAdapter {
    private static GuiDataAdapter instance;
    private HashMap editablePropsMap = new HashMap();
    private HashMap valTypeMap = new HashMap();
    private MQConstants mqconstants;
    
    /** Creates a new instance of ModelAdapter */
    public GuiDataAdapter() {
        instance = this;
        mqconstants = MQConstants.findInstance();
    } 
    public static GuiDataAdapter findInstance() {
        if (instance == null) {
            instance = new GuiDataAdapter();            
        }
        return instance;
    }

    public boolean adminEnabled() {
        return (((Integer)EntryPoint.getRootAttr("Enable Admin")).intValue() == 1);
    }

    public boolean hopEnabled() {
        return (((Integer)EntryPoint.getRootAttr("Enable Hop")).intValue() == 1);
    }

    public boolean localAddrEnabled() {
        return (((Integer)EntryPoint.getRootAttr("Enable LclAddr")).intValue() == 1);
    }
    
//-----------------Property window-----------------------------------------------    
    
    public boolean setProperty(String networkName, String propSetName, String NodeKey, String propKey, Object nueValue){        
        ObjectRepository repository = ObjectRepository.findInstance(networkName);
        HashMap propsMap = new HashMap();
        Object value = revertValues(nueValue);
        boolean success = true;
        System.out.println("setProperty : " + networkName + NodeKey + propSetName + propKey + nueValue);        
        if (repository.getFromRepository(NodeKey) != null){
            WMQObject mqobj = repository.getFromRepository(NodeKey);            
            if (propSetName.equalsIgnoreCase("Attributes")){
                System.out.println("  Called setAttribute : " + propKey + value);
                success = alterObject(mqobj, propKey, value);
                if (success){
                    mqobj.setAttribute(propKey, value);                        
                }                    
            } else if (propSetName.equalsIgnoreCase("Status")){
                System.out.println("  Called setStatus : " + propKey + value);
                success = alterObject(mqobj, propKey, value);
                if (success){
                    mqobj.setStatus(propKey, value);                     
                }
            } else if (propSetName.equalsIgnoreCase("Discovery")){
                System.out.println("  Called setDiscover : " + propKey + value);
                ((WMQQMgr)mqobj).setDiscovery(propKey, value);
            }
        } else if (propSetName.equalsIgnoreCase("Discovery Config")){
            System.out.println("  Called setAttribute : " + propKey + value);
            EntryPoint.setRootAttr(propKey, value);
        } else { //EntryPoint
            System.out.println("  Called ep.setValue : " + propKey + value);
            EntryPoint ep = (EntryPoint)EntryPoint.findInstance(NodeKey);             
            ep.setDiscovery(propKey, value);                  
            if (propKey.equalsIgnoreCase("Monitoring Interval")){
                ep.setTimer();
            }
        }

        return success;
    }
    
//    public Object getProperty(String networkName, String propSetName, String NodeKey, String propKey, String parent){        
//        HashMap propsMap = new HashMap();
//        HashMap attrMap = new HashMap();
//        String value = null;     
//        propsMap = getProperties(networkName, NodeKey, parent);
//        attrMap = (HashMap) propsMap.get(propSetName);
//        return attrMap.get(propKey); 
//    }    
    
    public HashMap getProperties(String networkName, String key, String parent){
        ObjectRepository repository = ObjectRepository.findInstance(networkName);
        String nodeType = (String) RootNode.findInstance().getNBNode(key).getValue("nodeType");
        HashMap propsMap = new HashMap();
        HashMap valTypeMap = new HashMap();
        
        if (repository.getFromRepository(key) != null){
            WMQObject mqobj = repository.getFromRepository(key);
            HashMap statusMap = new HashMap();
            HashMap attrMap = new HashMap();
            HashMap discMap = new HashMap();
//            System.out.println("Object name : " + mqobj.getUniqueName());
//            System.out.println("Object class : " + mqobj.getClass().toString());
            if ((mqobj.getClass() == WMQQMgr.class) || (mqobj.getClass() == WMQQueue.class) || (mqobj.getClass() == WMQChannel.class)){
                discMap = updateValues(mqobj.getDiscoveryMap());
                statusMap = updateValues(mqobj.getStatiiMap());
                attrMap = updateValues(mqobj.getAttributesMap());

                propsMap.put("Discovery", discMap);
                propsMap.put("Status", statusMap);                
                propsMap.put("Attributes", attrMap);                
            } else {
                attrMap = updateValues(mqobj.getAttributesMap());
                propsMap.put("Attributes", attrMap);
            }            
        } else if (nodeType.equalsIgnoreCase("Queues")){
            Node thisNode = RootNode.findInstance().getNBNode(key);
            WMQQMgr mgr = null;
            mgr = (WMQQMgr)repository.getFromRepository(parent);
            Object value = "";
            HashMap statusMap = new HashMap();

            if (mgr != null){
                ArrayList qlist = mgr.getQueues();            
                Iterator e = qlist.iterator();
                while (e.hasNext()) {
                    String item = (String) e.next();
                    WMQQueue que = (WMQQueue) repository.getFromRepository(item);
                    String queueName = que.getCaption();
                    boolean notSystem = !queueName.startsWith("SYSTEM") && !queueName.startsWith("AMQ") && !queueName.startsWith("MQAI");
                    boolean showSystemIsSelected = ( (String)thisNode.getValue("system")).equalsIgnoreCase("show");

                    // check to see if node property "system" is show or hide
                    if (notSystem || showSystemIsSelected){
                        value = que.getAttribute("Current Q Depth");
                        if (value != null){
                            statusMap.put(queueName, value);
                        } else {
                            statusMap.put(queueName, "none");
                        }
                    }
                }
            }
            propsMap.put("Status", statusMap);
        } else if (nodeType.equalsIgnoreCase("Channels")){
            Node thisNode = RootNode.findInstance().getNBNode(key);
            WMQQMgr mgr = null;
            mgr = (WMQQMgr)repository.getFromRepository(parent);
            Object value = "";
            HashMap statusMap = new HashMap();
            if (mgr != null){
                ArrayList chlist = mgr.getChannels();            
                Iterator e = chlist.iterator();
                while (e.hasNext()) {
                    String item = (String) e.next();
                    WMQChannel ch = (WMQChannel) repository.getFromRepository(item);
                    String channelName = ch.getCaption();
                    boolean notSystem = !channelName.startsWith("SYSTEM") && !channelName.startsWith("AMQ") && !channelName.startsWith("MQAI");
                    boolean showSystemIsSelected = ( (String)thisNode.getValue("system")).equalsIgnoreCase("show");
                    
                    // check to see if node property "system" is show or hide
                    if (notSystem || showSystemIsSelected){
                        value = mqconstants.getStatusName("Channel Status", (Integer) ch.getStatus("Channel Status"));
                        if (value != null){
                            statusMap.put(ch.getCaption(), value);
                        } else {
                            statusMap.put(ch.getCaption(), "Inactive");
                        }
                    }
                }
                
            }
            propsMap.put("Status", statusMap);
        } else if (nodeType.equalsIgnoreCase("Unclustered")){
            HashMap statusMap = new HashMap();
            statusMap.put("Catagory", "Unclustered");
            propsMap.put("Attributes", statusMap);
        } else if (parent == null){
            propsMap.put("Discovery Config", updateValues(EntryPoint.getRootAttrMap()));
        } else { //EntryPoint 
            EntryPoint ep = (EntryPoint)EntryPoint.existingInstance(key);
            if (ep != null){
                propsMap.put("Discovery", updateValues(ep.getDiscoveryMap()));
            }
        }        
        return propsMap;
    }
    
    public HashMap updateValues(HashMap thisMap){
        String valType = null;
        Object value = null;
        Class valClass = null;
        Boolean boolValue = null;
        ArrayList dropdown = null;
        String stringValue = null;
        String item = null;
        Iterator f = thisMap.keySet().iterator();
        while (f.hasNext()){
            Object next = f.next();
            if (next.getClass() == Integer.class) {
                item = ((Integer) next).toString();
            } else {
                item = (String) next;
            }
            value = thisMap.get(item);
            valClass = value.getClass();
            valType = getValType(item);  // legacy

            if ((valType != null) && (valType.equals("Boolean"))) {
                boolValue = Boolean.valueOf(((Integer)value).intValue() == 1);
                thisMap.put(item, boolValue);
//                System.out.println("*****" + item + " : " + value + " : " + boolValue);
            } else if ((valClass == ArrayList.class) || ((valType != null) && (valType.equals("ArrayList")))){
//                dropdown = MQConstants... TODO
                String listAsString = ((ArrayList) value).toString();
                thisMap.put(item, listAsString);
            } else if (valClass == Integer.class) {
                try {
                    stringValue = mqconstants.getStatusName(item, (Integer) value);
                    if (stringValue != null) {
                        thisMap.put(next, stringValue);
                    }
                } catch (Exception ex) {
                    // do nothing
                }
            }
        }
        return thisMap;
    }
    
    public Object revertValues(Object nueValue){
        Object replaceValue = null;
        if (nueValue.getClass() == Boolean.class){
            if (((Boolean) nueValue).booleanValue()){
                replaceValue = Integer.valueOf("1");
            } else {
                replaceValue = Integer.valueOf("0");
            }
//        } else if (nueValue.getClass() == ArrayList.class){
            // parse dropdown... TODO
        } else {
            replaceValue = nueValue;
        }
        return replaceValue;
    }    
  
//    public boolean isHidden(String propSet){        
//        boolean isHidden = false;
//        if (propSet == "Attributes"){
//            isHidden = true;
//        } 
//        return isHidden;
//    }
    
    public boolean isEditable(String item, String propSet){        
        boolean isEditable = false;
        if (editablePropsMap.isEmpty()){
            setEditableMap();            
        }
//        System.out.println("Admin Enabled............." + (Integer)EntryPoint.getRootAttr("Admin Enabled"));
        isEditable = (((((Integer)EntryPoint.getRootAttr("Enable Admin")).intValue() == 1) && editablePropsMap.containsKey(item)) || (propSet.startsWith("Discovery")));
        return isEditable;
    }    
    
    public String getValType(String item){
        if (valTypeMap.isEmpty()){
            setValTypeMap();
        }
        return (String) valTypeMap.get(item);
    }
         
    public void setEditableMap(){
        editablePropsMap.put("Max Msg Length", null);
        editablePropsMap.put("Max Q Depth", null);
        editablePropsMap.put("Def Priority", null);
        editablePropsMap.put("Backout Threshold", null);
        editablePropsMap.put("Short Retry Interval", null);
        editablePropsMap.put("Long Retry Interval", null);
        editablePropsMap.put("Heartbeat Interval", null);
        editablePropsMap.put("Disc Interval", null);
        editablePropsMap.put("Channel Desc", null);
        editablePropsMap.put("Inhibit Put", null);
        editablePropsMap.put("Inhibit Get", null);
        editablePropsMap.put("Def Persistence", null);        
//        editablePropsMap.put("Q Threshold Critical", null);
//        editablePropsMap.put("Q Threshold Warning", null);
        editablePropsMap.put("Accounting Conn Override", null);
        editablePropsMap.put("Inhibit Event", null);
        editablePropsMap.put("Channel Event", null);
        editablePropsMap.put("Performance Event", null);
        editablePropsMap.put("Start Stop Event", null);        
        editablePropsMap.put("Authority Event", null);
        editablePropsMap.put("Local Event", null);
        editablePropsMap.put("Logger Event", null);
        editablePropsMap.put("User Data", null);
        editablePropsMap.put("Trigger Data", null);
        editablePropsMap.put("Trigger Control", null);
        
        editablePropsMap.put("Q Threshold Critical", null);
        editablePropsMap.put("Q Threshold Warning", null);
        editablePropsMap.put("Polling Enabled", null);
        editablePropsMap.put("Monitoring Enabled", null);
        editablePropsMap.put("Monitoring Interval", null);
        editablePropsMap.put("Enable Admin", null);
        editablePropsMap.put("SvrConnChl", null);                
    }
    
    public void setValTypeMap(){
        // returns non-Integer data type, eg, Boolean, ArrayList (for dropdowns - TODO).
        valTypeMap.put("Inhibit Put", "Boolean");
        valTypeMap.put("Inhibit Get", "Boolean");
        valTypeMap.put("Def Persistence", "Boolean");
        valTypeMap.put("Monitoring Enabled", "Boolean");
        valTypeMap.put("Polling Enabled", "Boolean");
        valTypeMap.put("Explore Peers", "Boolean");
        valTypeMap.put("Enable Admin", "Boolean");
        valTypeMap.put("Enable LclAddr", "Boolean");
        valTypeMap.put("Enable Hop", "Boolean");
        valTypeMap.put("Accounting Conn Override", "Boolean");
        valTypeMap.put("Inhibit Event", "Boolean");
        valTypeMap.put("Channel Event", "Boolean");
        valTypeMap.put("Performance Event", "Boolean");
        valTypeMap.put("Start Stop Event", "Boolean");
        valTypeMap.put("Authority Event", "Boolean");
        valTypeMap.put("Local Event", "Boolean");
        valTypeMap.put("Logger Event", "Boolean");
        valTypeMap.put("Trigger Control", "Boolean");
    }
//-----------------NBNode metrics-----------------------------------------------  
    
    public String GetNodeState(HTNode node){
        String state = null;
        HTNodeBase simpleNode = (HTNodeBase)node;
        WMQObject obj = simpleNode.getMQObject(); 
        if (obj.getClass() == WMQQMgr.class){  
            WMQQMgr mqqmgr = (WMQQMgr) obj;
            if (!mqqmgr.getUpdated().equals("true")) {
                state = "none";
            } else {
                state = "ok";
            }
        }
        return state;
    }
    
    public HashMap getNodeMetricMap(HTNode node){
        HashMap metricMap = new HashMap();
        String metric = null;
        String state = null;
        int minSize = 6;
        int maxSize = 18;
        int size = 6;
        int depth = 0;        
        int warn = 1;
        int crit = 1;
        HTNodeBase simpleNode = (HTNodeBase)node;
        WMQObject obj = simpleNode.getMQObject();        
        if (obj.getClass() == WMQQueue.class){            
            Object depthField = ((WMQQueue)obj).getAttribute("Current Q Depth");            
            Object maxDepthField = ((WMQQueue)obj).getAttribute("Max Q Depth");            
            if ((depthField != null) && (maxDepthField != null) && (depthField.getClass() == Integer.class)) {                
                // depth for calculations, metric for display string.
                depth = ((Integer)depthField).intValue();
                metric = ((Integer)depthField).toString();
                
                boolean isMonitored = ((((Integer)((WMQQueue)obj).getDiscovery("Monitoring Enabled")).intValue() == 1) && (((Integer)((WMQQueue)obj).getParentQM().getDiscovery("Monitoring Enabled")).intValue() == 1));
                if (isMonitored){ 
                    int maxDepth = ((Integer)maxDepthField).intValue();
                    int relativeSize = 1;
                    
                    // get warning threshold from heirarchy of objects.
                    String warnThreshold = (String) ((WMQQueue)obj).getDiscovery("Q Threshold Warning");
                    if (warnThreshold.equalsIgnoreCase("")){
                        warnThreshold = (String) ((WMQQueue)obj).getParentQM().getDiscovery("Q Threshold Warning");
                    }
                    if (warnThreshold.equalsIgnoreCase("")){
                        warnThreshold = (String) EntryPoint.findInstance(((WMQQueue)obj).getNetworkName()).getDiscovery("Q Threshold Warning");
                    }
                    if (!warnThreshold.equalsIgnoreCase("")){
                        try{                    
                            warn = Integer.parseInt(warnThreshold.split("%")[0]);                       
                        } catch (NumberFormatException ex) {
                            warn = maxDepth;
                            warnThreshold = "";                
                        }
                    } else {
                        warn = maxDepth;
                        warnThreshold = "";    
                    }

                    if (warnThreshold.endsWith("%")){                    
                        float percent = (float)warn / (float)100;
                        warn = (int)((float)percent*(float)maxDepth);
                    }                    
                    
                     // get critical threshold from heirarchy of objects.
                    String critThreshold = (String) ((WMQQueue)obj).getDiscovery("Q Threshold Critical");
                    if (critThreshold.equalsIgnoreCase("")){
                        critThreshold = (String) ((WMQQueue)obj).getParentQM().getDiscovery("Q Threshold Critical");
                    }
                    if (critThreshold.equalsIgnoreCase("")){
                        critThreshold = (String) EntryPoint.findInstance(((WMQQueue)obj).getNetworkName()).getDiscovery("Q Threshold Critical");
                    }
                    if (!critThreshold.equalsIgnoreCase("")){
                        try{
                            crit = Integer.parseInt(critThreshold.split("%")[0]);                        
                        } catch (NumberFormatException ex) {
                            crit = maxDepth;
                            critThreshold = "";
                        }
                    } else {
                        crit = maxDepth;
                        critThreshold = "";
                    }
                    
                    // if percentage, calculate threshold
                    if (critThreshold.endsWith("%")){
                        float percent = (float)crit / (float)100;                    
                        crit = (int)((float)percent * (float)maxDepth);
                    }
                    
                    // calculate relative size of HT circle
                    relativeSize = (int)(((float)depth/(float)crit)*(float)maxSize);
                    relativeSize = Math.min(relativeSize, maxSize);                    
                    size = Math.max(relativeSize, minSize);

                    if (depth>crit){
                        state = "crit";
                    } else if (depth>warn){
                        state = "warn";
                    } else {
                        state = "ok";
                    }
                } else {                               
                    size = minSize;
                    state = "ok";
                }
            } else {
                metric = "";                
                size = minSize;
                state = "none";
            }            
        } else if (obj.getClass() == WMQChannel.class){
            metric = mqconstants.getStatusName("Channel Status", (Integer) ((WMQChannel)obj).getStatus("Channel Status"));  
//            System.out.println("get chstatus : " + ((WMQChannel)obj).getStatus("Channel Status").toString());
//            metric = (String) ((WMQChannel)obj).getStatus("Channel Status");
//            metric = null;
            if ((metric == null) || (metric.equalsIgnoreCase("Inactive"))){
                metric = "";
                state = "none";
            } else if ((metric.equalsIgnoreCase("Binding")) || (metric.equalsIgnoreCase("Retrying"))){
                state = "crit";
            }else if ((metric.equalsIgnoreCase("Stopped")) || (metric.equalsIgnoreCase("Stopping"))){
                state = "warn";
            } else {
                state = "ok";
            }
            size = minSize;            
        }
        metricMap.put("metric", metric);
        metricMap.put("size", Integer.valueOf(String.valueOf(size)));
        metricMap.put("state", state);
        return metricMap;
    }
    
//-----------------Alter Objects-----------------------------------------------      
    
    public boolean alterObject(WMQObject mqobj, String key, Object value){                
        boolean success = MQObjectAlter.Alteration(mqobj, key, value);
        new AdHocUpdate(mqobj.getParentQM());
        return success;
    }

//----------------- Sync views / properties -----------------------------------------------
     /*
     * Send node name to select node. Creates copy of root node to navigate down,
     */
    public Node selectNode(String nodeName, String networkName) {
        // This class creates a clone of the rootNode in order to link the HTnode to a (hidden) selected NBNode,
        // This provides the simplest mechanism to create a link to the properties page.
        NodeExplorerTopComponent nbTop = NodeExplorerTopComponent.findInstance();
        ExplorerManager manager2 = nbTop.getExplorerManagerClone();

        Node[] nodes = new Node[1];
        Node node = ((RootNode)manager2.getRootContext()).getNBNode(nodeName);
//        String networkName = this.getNetworkName();
        ObjectRepository repository = null;
        if (networkName != null){
            repository = ObjectRepository.findInstance(networkName);
        }

        // if NBnode exists, and nodeName really does represent a WMQObject...
        if ((node == null) && (networkName != null) && (repository.getFromRepository(nodeName) != null)){
            try {
                manager2.setExploredContextAndSelection((RootNode) manager2.getRootContext(), ((RootNode) manager2.getRootContext()).getChildren().getNodes());
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex){}
            
            WMQObject obj = (WMQObject)repository.getFromRepository(nodeName);
            Node networkNode = ((RootNode)manager2.getRootContext()).getChildren().findChild(obj.getNetworkName());
            try {
                manager2.setExploredContextAndSelection(networkNode, networkNode.getChildren().getNodes());
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }

            if (obj.getClass() == WMQQMgr.class){ // If QMgr class, map via clustername, or 'unclustered' designation
                System.out.println("QMgr name : " + obj.getUniqueName());
                Node[] thisNode = new Node[1];

                String clusterName = ((WMQQMgr)obj).getClusterName();
                Node parentNode = (networkNode.getChildren().findChild(clusterName));

                if (clusterName == null){
                    System.out.println("Unclustered");
                    parentNode = (networkNode.getChildren().findChild(networkName + "." + "Unclustered"));
                }

                if (parentNode != null){
                    thisNode[0] = parentNode.getChildren().findChild(nodeName);
                    if (thisNode[0] != null){
                        try {
                            manager2.setExploredContextAndSelection(parentNode, thisNode);
                        } catch (PropertyVetoException ex) {
                            ex.printStackTrace();
                        } catch (IllegalArgumentException ex){
                            ex.printStackTrace();
                        }
                    }
                }
            } else {
                Node[] thisNode = new Node[1];
                Node[] thatNode = new Node[1];
                Node parentNode = null;
                if ((obj.getClass() != WMQCluster.class) && (obj.getParentQM().getClusterName() != null)){
                    parentNode = (networkNode.getChildren().findChild(obj.getParentQM().getClusterName()));//
                }
                if (parentNode == null) {
                    parentNode = (networkNode.getChildren().findChild(networkName + "." + "Unclustered"));
                }
                if (parentNode == null) {
                    parentNode = (networkNode);
                }

                if ((parentNode != null) && (obj.getParentQM() != null)){ 
                    thisNode[0] = parentNode.getChildren().findChild(obj.getParentQM().getUniqueName());
                    if (thisNode[0] != null){
                        try {
                            manager2.setExploredContextAndSelection(parentNode, thisNode);
        //                    manager.setExploredContext(parentNode);
                        } catch (PropertyVetoException ex) {
                            ex.printStackTrace();
                        } catch (IllegalArgumentException ex){}

                        thatNode = new Node[1];

                        // If Queue or Channel Process, map via 'Queues' or 'Channels' or 'Processes' node.
                        if (obj.getClass() == WMQQueue.class){
                            parentNode = thisNode[0].getChildren().findChild(thisNode[0].getName() + "." + "Queues");
                        } else if (obj.getClass() == WMQChannel.class){
                            parentNode = thisNode[0].getChildren().findChild(thisNode[0].getName() + "." + "Channels");
                        } else if (obj.getClass() == WMQProcess.class){
                            parentNode = thisNode[0].getChildren().findChild(thisNode[0].getName() + "." + "Processes");
                        } else if (obj.getClass() == WMQCluster.class){
                            parentNode = thisNode[0].getChildren().findChild(thisNode[0].getName() + "." + "Cluster");
                        }

                        if (parentNode != null){
                            thatNode[0] = parentNode.getChildren().findChild(nodeName);
                            try {
                                manager2.setExploredContextAndSelection(parentNode, thatNode);
                                manager2.setExploredContext(thatNode[0]);
    //                            manager2.setSelectedNodes(thatNode);
                            } catch (PropertyVetoException ex) {
                                ex.printStackTrace();
                            } catch (IllegalArgumentException ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                }
//                System.out.println("  explored : " + manager2.getExploredContext().toString());
            }
            node = ((RootNode)manager2.getRootContext()).getNBNode(nodeName);
        }
        if (node != null){
            nodes[0] = node;
            //set focus to update properties component
            try {
                manager2.setSelectedNodes(nodes);
            } catch (PropertyVetoException vex) {
                vex.printStackTrace();
            }
            
            // the following are not necessary under Platform 6.5 and Java 1.5
//            nbTop.refreshExplorer();
//            nbTop.requestActive();
            
            nbTop.setActivatedNodes(nodes);
            nbTop.validate();
        }
        return node;
    }

    public void changedSelected(Node[] node){
        if ((node.length > 0) && !((String)node[0].getValue("nodeType")).equals("EntryPoint") && !((String)node[0].getValue("nodeType")).equals("Top")){
            HTNode htnode = HyperTree.findInstance((String)node[0].getValue("networkName")).getHTNode(node[0].getName());
            if (htnode != null) {
                HTree htree = HyperTree.findInstance((String)node[0].getValue("networkName")).getHyperTree();
                if ((htnode != null) && (htree != null)){
                     htree.setSelectedNode(htnode);
                }
            }
        }
        // as of NB 6.5, this code will prevent properties from aligning with HT node selection
//        if ((node != null) && (node.length > 0)){
//            selectNode((String)node[0].getName(), (String)node[0].getValue("networkName"));
//        }

    }

    
//-----------------Context Menu-----------------------------------------------          

    public void nodeChanged(HTNodeBase selectedNode, String networkName){     
        this.selectNode(selectedNode.getUniqueName(), networkName);
    }
    
    public Action[] getContext(AbstractNode node){
        WMQObject mqObj = null;
        if (node.getClass() == LeafNode.class){
            mqObj = ((LeafNode)node).getMQObject();            
        } else if (node.getClass() == ParentNode.class){
            mqObj = ((ParentNode)node).getMQObject();
        } 
        return getNBNodeActions(node, mqObj);
    }
    public Action[] getContext(HTNodeBase node){
        WMQObject mqObj = node.getMQObject();        
        return getHTNodeActions(node, mqObj);        
//        AbstractNode expNode = (AbstractNode) NodeExplorerTopComponent.findInstance().selectNode(node.getUniqueName(), node.getNetworkName());
//        return expNode.getActions(true);
////        return getContext(expNode);
    }
    
    public Action[] getNBNodeActions(AbstractNode node, WMQObject mqobject) {
        Action[] result = null;
        String nodeType = (String) node.getValue("nodeType");

        if (mqobject != null){
            if (mqobject.getClass() == WMQQueue.class){           
                if (adminEnabled()){
                    // if admin is enabled, show Enable/Disable actions        
                    result = new Action[] {
                        new GuiDataAdapter.EnableAction(mqobject),               
                        new GuiDataAdapter.BrowseAction(mqobject),
        //                new PutAction(this.mqobject),
                    }; 
                } else {
                    result = new Action[] {               
                        new GuiDataAdapter.BrowseAction(mqobject),
        //                new PutAction(this.mqobject),
                    }; 
                }                       
            } else if (mqobject.getClass() == WMQChannel.class){
                if (adminEnabled()){
                    // if admin is enabled, show Start/Stop actions      
                    result = new Action[] {
                        new GuiDataAdapter.StartAction(mqobject),
                    }; 
                } else {
                    result = new Action[] { 
                    };
                }
            } else if (mqobject.getClass() == WMQQMgr.class){
                result = new Action[] {  
                    new GuiDataAdapter.ExploreNodeAction(mqobject),
                    new GuiDataAdapter.UpdateStatusNodeAction(mqobject),
                    null,
                    new GuiDataAdapter.MqscAction(mqobject),
                };
            } else if (mqobject.getClass() == WMQCluster.class){
                result = new Action[] {

                };
            }    
        } else if (nodeType.equalsIgnoreCase("Unclustered")) {
            result = new Action[] {
            };
        } else if (nodeType.equalsIgnoreCase("Queues") || nodeType.equalsIgnoreCase("Channels")) {
            result = new Action[] {
            };
        }  else if (nodeType.equalsIgnoreCase("EntryPoint")) {
            // TODO - reset.
            result = new Action[] {
            };
        }
        return result;
    }
    public Action[] getHTNodeActions(HTNodeBase node, WMQObject mqobject) {
        Action[] result = null;
        if (mqobject.getClass() == WMQQueue.class){           
            if (adminEnabled()){
                // if admin is enabled, show Enable/Disable Action
                result = new Action[] {            
                    new GuiDataAdapter.RefreshAction(mqobject),
    //                new ModelAdapter.PropertiesAction(mqobject),
                    null,
                    new GuiDataAdapter.EnableAction(mqobject),                
                    new GuiDataAdapter.BrowseAction(mqobject),
    //                new PutAction(this.mqobject),
                };        
            } else {
                result = new Action[] {            
                    new GuiDataAdapter.RefreshAction(mqobject),
    //                new ModelAdapter.PropertiesAction(mqobject),
                    null,
                    new GuiDataAdapter.BrowseAction(mqobject),
                };        
            }           
        } else if (mqobject.getClass() == WMQChannel.class) {  
            if (adminEnabled()){
                          // if admin is enabled, show Start/Stop Action
                result = new Action[] {            
                    new GuiDataAdapter.RefreshAction(mqobject),
    //                new ModelAdapter.PropertiesAction(mqobject),
                    null,
                    new GuiDataAdapter.StartAction(mqobject),
                }; 
            } else {
                result = new Action[] {            
                    new GuiDataAdapter.RefreshAction(mqobject),
    //                new ModelAdapter.PropertiesAction(mqobject),
                }; 
            }
        } else if (mqobject.getClass() == WMQQMgr.class){
            result = new Action[] {
                new GuiDataAdapter.ExploreNodeAction(mqobject),
                new GuiDataAdapter.UpdateStatusNodeAction(mqobject),
                new GuiDataAdapter.RefreshAction(mqobject),
                new GuiDataAdapter.MqscAction(mqobject),                
//                new ModelAdapter.PropertiesAction(mqobject),
            };
        } else if (mqobject.getClass() == WMQCluster.class){
            result = new Action[] {
                new GuiDataAdapter.RefreshAction(mqobject),
//                new ModelAdapter.PropertiesAction(mqobject),
            };
        }

        return result;
    }
    
    public Action getPropertiesAction(){
         // TODO - get the real thing from the node
         // return SystemAction.get(PropertiesAction.class);
         return PropertiesAction.get(PropertiesAction.class);
    }
    
    /** Creating an action for enabling/disabling Queue */
    private static class RefreshAction extends AbstractAction {       
        private String name;
        private String label;
        private String network;
        public RefreshAction(WMQObject obj){
            name = obj.getUniqueName();
            network = obj.getNetworkName();
            this.label = "Refresh";            
            putValue(Action.NAME, label);
        }
        
        public void actionPerformed(ActionEvent ae) {            
            HTreeTopComponent.refreshInstance(network);
        }        
    }
    /** Creating an action for properties pop-up */
//    private static class ShowPropertiesAction extends AbstractAction {
//        private String name;
//        private String label;
//        private String network;
//        public ShowPropertiesAction(WMQObject obj){
//            name = obj.getUniqueName();
//            network = obj.getNetworkName();
//            this.label = "Properties";
//            putValue(Action.NAME, label);
//        }
//
//        public void actionPerformed(ActionEvent ae) {
////            NodeExplorerTopComponent.findInstance().getProperties(name, network);
//        }
//
//    }
    /** Creating an action for enabling/disabling Queue */
    private static class EnableAction extends AbstractAction {       
        private String instanceName;
        private String label;
        private WMQQueue mqqu = null;
        public EnableAction(WMQObject obj){
            instanceName = obj.getNetworkName();
            mqqu = (WMQQueue)obj;
            if (((Integer)mqqu.getAttribute("Inhibit Put")).intValue() == 0){
                this.label = "Put Disable";
            } else {
                this.label = "Put Enable";
            }
            putValue(Action.NAME, label);
        }
        
        public void actionPerformed(ActionEvent ae) {
            //TODO - at present does not care about success - call is threaded, does not wait.
            boolean success;
            if (label.equalsIgnoreCase("Put Disable")){
                success = GuiDataAdapter.findInstance().alterObject((WMQObject)mqqu, "Inhibit Put", Integer.valueOf("1"));
                label = "Put Enable";
            } else {
                success = GuiDataAdapter.findInstance().alterObject((WMQObject)mqqu, "Inhibit Put", Integer.valueOf("0"));
                label = "Put Disable";
            }
            putValue(Action.NAME, label);
            HyperTree.findInstance(instanceName);
            HTreeTopComponent.refreshInstance(instanceName);
            RootNode.refreshNode();
        }        
    } 
    /** Creating an action for starting/stopping channel */
    private static class StartAction extends AbstractAction {       
        private String instanceName;
        private String label;
        WMQChannel mqch = null;
        public StartAction(WMQObject obj){
            instanceName = obj.getNetworkName();
            mqch = (WMQChannel)obj; 
            if (((Integer)mqch.getStatus("Channel Status")).intValue() != 6){ // 'Stopped'                
                this.label = "Stop";
            } else {
                this.label = "Start";
            }          
            putValue(Action.NAME, label);
        }
        
        public void actionPerformed(ActionEvent ae) {
            //TODO - at present does not care about success - call is threaded, does not wait.
            // call routine in "ModelAdapter"
            boolean success = GuiDataAdapter.findInstance().alterObject((WMQObject)mqch, "Channel Status", (Object)label);
            if (this.label.equalsIgnoreCase("Start")) {
                this.label = "Stop";
            } else {
                this.label = "Start";
            }
            putValue(Action.NAME, label);
            HyperTree.findInstance(instanceName);
            HTreeTopComponent.refreshInstance(instanceName);
            RootNode.refreshNode();
        }
        
    }
    
        /** Creating an action for starting/stopping channel */
    private static class MqscAction extends AbstractAction {       
        private String name;
        private String label;
        WMQQMgr qmgr = null;
        public MqscAction(WMQObject obj){
            name = obj.getCaption();
            qmgr = (WMQQMgr)obj; 
            this.label = "remMQSC";                
            putValue(Action.NAME, label);
        }
        
        public void actionPerformed(ActionEvent ae) {
            new RemMqsc(qmgr);
        }
        
    }
    
    /** Creating an action for browsing Queue */
    private static class BrowseAction extends AbstractAction {       
        private String name;
        private String label;
        private WMQQueue mqqu = null;
        public BrowseAction(WMQObject obj){
            name = obj.getCaption();
            this.label = "Browse";
            this.mqqu = (WMQQueue) obj;
            putValue(Action.NAME, label);
        }
        
        public void actionPerformed(ActionEvent ae) {
            //TODO            
            DataGuiAdapter.findInstance().browseMsgs(mqqu, false);
        }        
    }

    /** Creating an action for puting message to Queue */
    private static class PutAction extends AbstractAction {       
        private String name;
        private String label;
        private WMQQueue mqqu = null;
        public PutAction(WMQObject obj){
            name = obj.getCaption();
            this.label = "Put Msgs";
            mqqu = (WMQQueue)obj;
            putValue(Action.NAME, label);            
        }
        
        public void actionPerformed(ActionEvent ae) {
            //TODO            
            DataGuiAdapter.findInstance().openPut(mqqu);
        }        
    }
    
    /** Creating an action for network discovery */
    private static class DiscoverAction extends AbstractAction {        
        private String label;
        private String networkName;
        public DiscoverAction(WMQObject obj){
            networkName = obj.getNetworkName();
            this.label = "Discover...";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            Discovery disc = new Discovery(networkName);
            disc.start(); 
        }       
    }
    
    /** Creating an action for network status update */
    private static class StatusPollAction extends AbstractAction {        
        private String label;
        private String networkName;
        public StatusPollAction(WMQObject obj){
            networkName = obj.getNetworkName();
            this.label = "Status...";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            StatusPoll nextPoll = new StatusPoll(networkName);
            nextPoll.start();      
        }       
    }
    
    private static class ExploreNodeAction extends AbstractAction {        
        private String label;        
        private WMQQMgr qMgr;
        public ExploreNodeAction(WMQObject obj){
            qMgr = (WMQQMgr)obj;
            this.label = "Explore QMgr";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            AdHocUpdate adHoc = new AdHocUpdate(qMgr);
            adHoc.AdHocExplore();
        }       
    }
        
    private static class UpdateStatusNodeAction extends AbstractAction {        
        private String label;        
        private WMQQMgr qMgr;
        public UpdateStatusNodeAction(WMQObject obj){
           qMgr = (WMQQMgr)obj;
            this.label = "Update Status";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            AdHocUpdate adHoc = new AdHocUpdate(qMgr);
            adHoc.AdHocStatusUpdate();      
        }       
    }
 
    /** Creating an action for show/hide System Queues */
    private static class ShowAction extends AbstractAction {
        private String label;
        private Node node;
        public ShowAction(Node thisNode){
            this.node = thisNode;
            String showSystem = (String) thisNode.getValue("system");
            if (showSystem.equals("hide")){
                this.label = "Show System";
            } else {
                this.label = "Hide System";
            }          
            putValue(Action.NAME, label);
        }        
        public void actionPerformed(ActionEvent ae) {
            if (this.label.equalsIgnoreCase("Hide System")){
                this.node.setValue("system", "hide");
                this.label = "Show System";
            } else {
                this.node.setValue("system", "show");
                this.label = "Hide System";
            }
            putValue(Action.NAME, label);
            RootNode.refreshNode();
        }        
    }

}
