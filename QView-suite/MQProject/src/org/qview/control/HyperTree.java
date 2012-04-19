/*
 * HyperView.java
 *
 * Created on 28 May 2006, 17:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.control;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.qview.data.ObjectRepository;
import org.qview.data.mqmodel.WMQChannel;
import org.qview.data.mqmodel.WMQCluster;
import org.qview.data.mqmodel.WMQObject;
import org.qview.data.mqmodel.WMQQMgr;
import org.qview.data.mqmodel.WMQQueue;
import org.qview.gui.hypertree.*;

/**
 *
 * @author T.R.Goodwill
 */
public class HyperTree {
    private static HashMap namedInstance = new HashMap();    
    private  HyperTree instance;    
    private  HTNodeBase root = null; // the root of the tree
    private HTree  hypertree = null; // the hypertree structure
    private ObjectRepository repository;
    private String instName;
    private boolean singleNode = false;
    private HashMap HTNodes = new HashMap();
    private HTView view;
    private boolean refresh = false;

    // channel and queue name filters
    private Pattern quPattern;
    private Pattern chPattern;
    
    /** Creates a new instance of HyperView */
    private HyperTree(String newName) {
//        CreateData.findInstance();
        System.out.println("*Drawing Hyperview*");
        this.instName = newName;        
        namedInstance.put(newName, this);
        repository = ObjectRepository.findInstance(newName);
        
        buildTree();
        
    }
    
    private void buildTree(){
        ArrayList clusters = repository.getClusters();
        if (!clusters.isEmpty()){
            WMQCluster rootCluster = (WMQCluster) repository.getFromRepository((String) clusters.get(0));            
            root = new HTNodeBase(rootCluster);
            System.out.println("root : " + rootCluster.getUniqueName());
            HTNodes.put(rootCluster.getUniqueName(), root);
            addClusterChildren(root, null);
        } else {            
            String rootMgrName = repository.getQMgrName(EntryPoint.findInstance(instName).getConnName());            
                // name of the node to which we first connected
            if (rootMgrName != null){
                WMQQMgr rootMgr = (WMQQMgr) repository.getFromRepository(rootMgrName);
                root = new HTNodeBase(rootMgr);
                System.out.println("root : " + rootMgr.getUniqueName());
                HTNodes.put(rootMgr.getUniqueName(), root);
                singleNode = true;
                addQMgrChildren(root, null);
            }
        }       
        
        if (root != null){
            hypertree = new HTree(root);      
            this.view = hypertree.getView();
//            if (!refresh){hypertree.setSelectedNode(root);} //only select node first time.
            hypertree.addNodeListener(new NodeListener(null));          
            HTreeTopComponent.findInstance(this.instName).setPanel(this.view, this.root);            
        } 
    }
    
    private void addClusterChildren(HTNodeBase node, WMQQMgr parent){       
        WMQCluster nextcl = (WMQCluster) node.getMQObject();
        ArrayList qmgrs = nextcl.getQMgrs();
        ArrayList nodes = new ArrayList(nextcl.getQMgrs().size());
        if (qmgrs != null){
            Iterator e = qmgrs.iterator();
            while (e.hasNext()){
                String nextMgrName = (String) e.next();
                WMQQMgr nextmgr = (WMQQMgr)repository.getFromRepository(nextMgrName);
                if ((nextmgr != null) && (!existsNode(nextMgrName))){
                    if ((parent == null) || !nextmgr.getUniqueName().equals(parent.getUniqueName())){
                        HTNodeBase mgrnode= new HTNodeBase(nextmgr);
                        nodes.add(mgrnode);
                        HTNodes.put(nextmgr.getUniqueName(), mgrnode);
                        node.addChild(mgrnode);
//                        addQMgrChildren(mgrnode, nextcl);
                    }
                }
            } 
            Iterator f = nodes.iterator();
            while (f.hasNext()){
                HTNodeBase nextNode = (HTNodeBase) f.next();                         
                if ((nextNode != null)){ 
                    addQMgrChildren(nextNode, nextcl);                   
                }
            } 
        }              
    }
    private void addQMgrChildren(HTNodeBase node, WMQObject parent){
        HTreeTopComponent thisTop = HTreeTopComponent.findInstance(this.instName);
        WMQQMgr nextqm = (WMQQMgr) node.getMQObject();
        if (nextqm != null){
            ArrayList qus = nextqm.getQueues();
            if ((qus != null)  && !(qus.isEmpty())){

                // get user defined queue name filter
                String queueFilter = thisTop.getQueueFilter();
                try {
                    this.quPattern = Pattern.compile(queueFilter);
                } catch (PatternSyntaxException ex) {
                    // bad pattern, simply reset the pattern
                    this.quPattern = Pattern.compile("^(?!SYSTEM).*");
                }
                // queue display params
                boolean localIsChecked = thisTop.getLocalQueuesCheck();
                boolean remoteIsChecked = thisTop.getRemoteQueuesCheck();
                boolean aliasIsChecked = thisTop.getAliasQueuesCheck();
                int minDepth = thisTop.getMinQueueDepth();

                // iterate through queues list
                Iterator g = qus.iterator();
                while (g.hasNext()){
                    WMQQueue nextqu = (WMQQueue)repository.getFromRepository((String) g.next());
                    if (nextqu != null) {
                        String nextquName = nextqu.getCaption();
                        //  check the queue name include mask
                        Matcher qum = quPattern.matcher(nextquName);
                        boolean quMatches = qum.matches();
                        // queue type
//                        boolean isSystemQueue = (nextqu.getCaption().startsWith("SYSTEM") || nextqu.getCaption().startsWith("AMQ") || nextqu.getCaption().startsWith("MQAI"));
                        boolean isLocalQueue = (nextqu.getAttribute("Q Type")!=null && (((Integer)nextqu.getAttribute("Q Type")).intValue() == 1));
                        boolean isRemoteQueue = (nextqu.getAttribute("Q Type")!=null && (((Integer)nextqu.getAttribute("Q Type")).intValue() == 6));
                        boolean isAliasQueue = (nextqu.getAttribute("Q Type")!=null && (((Integer)nextqu.getAttribute("Q Type")).intValue() == 3));

                        boolean depthAboveMin = nextqu.getAttribute("Current Q Depth")!=null && ((Integer)nextqu.getAttribute("Current Q Depth")).intValue() >= minDepth;

                        if (quMatches &&
                            (localIsChecked || !isLocalQueue) &&
                            (remoteIsChecked || !isRemoteQueue) &&
                            (aliasIsChecked || !isAliasQueue) &&
                            (depthAboveMin || !isLocalQueue)) {  //QA and QR will have zero depth, of course.
                            HTNodeBase qunode= new HTNodeBase(nextqu);
                            HTNodes.put(nextqu.getUniqueName(), qunode);
                            node.addChild(qunode);
                        }
                    }
                }
            }//queues

            ArrayList chs = nextqm.getChannels();
            if ((chs != null) && !(chs.isEmpty())){

                // get user defined queue name filter
                String channelFilter = thisTop.getChannelFilter();
                try {
                    this.quPattern = Pattern.compile(channelFilter);
                } catch (PatternSyntaxException ex) {
                    // bad pattern, simply reset the pattern
                    this.quPattern = Pattern.compile("^(?!SYSTEM).*");
                }
                this.chPattern = Pattern.compile(channelFilter);
                // channel display params
                boolean inactiveIsChecked = thisTop.getInactiveChannelsCheck();
                boolean runningIsChecked = thisTop.getRunningChannelsCheck();
                boolean connIsChecked = thisTop.getConnChannelsCheck();
                // iterate through channels list
                Iterator h = chs.iterator();
                while (h.hasNext()){
                    WMQChannel nextch = (WMQChannel)repository.getFromRepository((String) h.next());
                    if (nextch != null){
                        String nextchName = nextch.getCaption();
                        //  check the channel name include mask
                        Matcher chm = chPattern.matcher(nextchName);
                        boolean chMatches = chm.matches();
                        // channel type
                        boolean isActiveChannel = nextch.getStatus("Channel Status") != null && ((Integer)nextch.getStatus("Channel Status")).intValue() != 0;
                        boolean isRunningChannel = nextch.getStatus("Channel Status") != null && ((Integer)nextch.getStatus("Channel Status")).intValue() == 3;
                        boolean isConnChannel = nextch.getStatus("Channel Status") != null 
                                             && (((Integer)nextch.getStatus("Channel Status")).intValue() == 6
                                             ||  ((Integer)nextch.getStatus("Channel Status")).intValue() == 7);

                        //conditions to show channels
                        if  (chMatches &&
                            (inactiveIsChecked || isActiveChannel) &&
                            (connIsChecked || !isConnChannel) &&
                            (runningIsChecked || !isRunningChannel)) {
                            HTNodeBase chnode= new HTNodeBase(nextch);
                            HTNodes.put(nextch.getUniqueName(), chnode);
                            node.addChild(chnode);
                        }
                    }
                }
            }//channels

            ArrayList parentclusters = nextqm.getClusterList(); 
            if (parentclusters != null){
                Iterator f = parentclusters.iterator();
                while (f.hasNext()){
                    String nextClusName = (String) f.next();
                    WMQCluster nextcl = (WMQCluster)repository.getFromRepository(nextClusName);
                    if ((nextcl != null) && (!existsNode(nextClusName))){
                        if ((parent == null) || !nextcl.getUniqueName().equals(parent.getUniqueName())){
                            HTNodeBase clnode= new HTNodeBase(nextcl);
                            HTNodes.put(nextcl.getUniqueName(), clnode);
                            node.addChild(clnode);                
                            addClusterChildren(clnode, nextqm);
                        }
                    }
                }
                singleNode = false;
            }//parentclusters
            ArrayList Peers = nextqm.getPeers();
            if (Peers != null){
                Iterator i = Peers.iterator();
                while (i.hasNext()){                
                    String getMgr = repository.getQMgrName((String)i.next());
                    if (getMgr != null) {
                        WMQQMgr connMgr = (WMQQMgr)repository.getFromRepository(getMgr);
                        if ((parent == null) || (!getMgr.equals(parent.getUniqueName()))){
                            if (!existsNode(getMgr)){
                                HTNodeBase mgrnode = new HTNodeBase(connMgr);
                                HTNodes.put(connMgr.getUniqueName(), mgrnode);
                                node.addChild(mgrnode);
                                addQMgrChildren(mgrnode, connMgr);
                            } else {
                                System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
                                HTNodeBase mgrnode = (HTNodeBase) HTNodes.get(connMgr.getUniqueName());
                                if (mgrnode == null) {
                                    mgrnode = (HTNodeBase) HTNodes.get(connMgr.getConnName());
                                }
                                node.addSibling(mgrnode);
                                mgrnode.addSibling(node);
                            }
                        }
                    }     
                }
                singleNode = false;      
            }
            if (singleNode){
                HTreeTopComponent htc = HTreeTopComponent.findInstance(this.instName);
                htc.setDefaultView();
            }                
        }
    }
    
    // return HTNode of instName 'key'
    public HTNode getHTNode(String key){
        return (HTNode) HTNodes.get(key);
    }
    // return this HyperTree
    public HTree getHyperTree(){
        return hypertree;
    }    
    // return this view
    public HTView getHTView(){
        return view;         
    }
    public boolean existsNode(String nodeName){
        return HTNodes.keySet().contains(nodeName);
    }
    
    // return this instance, or create new instance
    public static HyperTree findInstance(String thisName){
        if (namedInstance.containsKey(thisName)){
            return (HyperTree) namedInstance.get(thisName);
        } else {
            return new HyperTree(thisName);
        }        
    }
    // refresh nodes
    public static void refreshNode() {        
        Iterator j = namedInstance.keySet().iterator();
        while (j.hasNext()){
            String instanceName = (String) j.next();
            HyperTree instance =  new HyperTree(instanceName); 
            namedInstance.put(instanceName, instance);
        }
//        instance = new HyperView(RootNode.getDefaultName());
    }
    public static HyperTree refreshNode(String instanceName) {        
        HyperTree instance = new HyperTree(instanceName); 
        namedInstance.put(instanceName, instance);
//        instance.getHyperTree().setSelectedNode();
        instance.refresh = true;
        return instance;
    }
    
}
