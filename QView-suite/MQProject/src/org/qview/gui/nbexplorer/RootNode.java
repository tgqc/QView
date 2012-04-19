/*
 * RootNode.java
 *
 * Based on the 'PropNode' class, Netbeans project,
 * created on June 13, 2005, 1:20 PM
 *
 * This node represents the root node of the org.openide.nodes.Node object class,
 * and is fed into the node explorer 'NodeExplorer'.
 */

package org.qview.gui.nbexplorer;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.qview.control.ExplorerTree;
import org.qview.gui.hypertree.HTreeTopComponent;
import org.qview.control.Discovery;
import org.qview.control.EntryPoint;
import org.qview.data.ObjectRepository;
import org.qview.control.StatusPoll;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


import org.openide.util.datatransfer.NewType;
/**
 *
 * @author T.Goodwill
 */
public class RootNode extends AbstractNode {
    private static ResourceBundle bundle = NbBundle.getBundle(RootNode.class);    
    private static RootNode rootNode;
    private static String topName = "MQ Environment";    
    private HashMap NBNodes = new HashMap();
    private ChangeListener listener;
    private String key;
//    private static EntryPoint cp;
    public RootNode() {
        super(new ExplorerTree(topName));
        this.setValue("nodeType", "Top");
//        this.setValue("networkName", topName);
        setIconBase("org/qview/gui/TopIcon");
//        setIcon(Utilities.loadImage("org/qview/gui/TopIcon.gif", true));
        super.setName(topName);
        setDisplayName(topName);
        this.key = this.getName();
        setShortDescription(bundle.getString("HINT_NBNode"));
//        this.setHidden(true);
        NBNodes.put(this.getName(), this);
    }
   /* Constructors with Children.Keys refers to the 'Tree' class to extrapolate children
    */
    public RootNode(String key, Children.Keys newKeys) {
        super(newKeys);        
        this.key = key;
        super.setName(key);
        this.setValue("nodeType", "EntryPoint");
        this.setValue("networkName", key);
//        defaultConnectName = key;
        setShortDescription(bundle.getString("HINT_EntryPoint"));
        setIconBase("org/qview/gui/EntryPointIcon");    
        RootNode.findInstance().addToNBNodes(this.getName(), this);
    }

    public String getRefreshCommandKey(){
        return SystemAction.get(RefreshNodeAction.class).ACTION_COMMAND_KEY;
    }
        
    public Action[] getActions(boolean context) {
        Action[] result;
        if (this == rootNode) {
            result = new Action[] {
                    SystemAction.get(RefreshNodeAction.class),                    
//                    new ShowAction(),                    
//                    null,
                    SystemAction.get(NewAction.class),                                   
            };
        } else {
            result = new Action[] {
                    SystemAction.get(RefreshNodeAction.class),
                    new DiscoverAction(this),
                    new StatusPollAction(this),
                    null,
//                    new ShowAction(),
                    SystemAction.get(OpenLocalExplorerAction.class),
                    new HTreeAction(this),
                    null,
                    SystemAction.get(NewAction.class),
                    new RenameAction(this),
                    new DestroyAction(this),
//                    new cleanUnmappedMgrsAction(this),
                    SystemAction.get(ToolsAction.class),
                    null,
                    SystemAction.get(PropertiesAction.class)
            };
        }
            
        return result;
    }
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.qview.gui.nbexplorer");
    }
    public Node cloneNode() {
        return new RootNode();
    }
    
    protected Sheet createSheet() {    
        Sheet cleanSheet = super.createSheet();
        PropSheet propSheet = new PropSheet(cleanSheet, this);
        Sheet sheet = propSheet.getPropSheet();
        PropertiesNotifier.addChangeListener(listener = new
                ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                firePropertyChange("value", null, null);
            }
        });
        return sheet;
    }
    protected Sheet refreshSheet() { 
        Sheet sheet = this.getSheet();        
        PropSheet propSheet = new PropSheet(sheet, this);
        return sheet;        
    }
    protected void finalize() throws Throwable {
        super.finalize();
        if (listener != null)
            PropertiesNotifier.removeChangeListener(listener);
    }
   
    public NewType[] getNewTypes() {
        return new NewType[] { new NewType() {
            public String getName() {
                return bundle.getString("LBL_NewProp");
            }
            public HelpCtx getHelpCtx() {
                return new HelpCtx("org.qview.gui.nbexplorer");
            }
            public void create() throws IOException {
                String title = "Create New Entry-Point";
                String msg = "New Entry-Point Name:";
                NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(msg, title);
                DialogDisplayer.getDefault().notify(desc);
                String key = desc.getInputText();
//                if ("".equals(key)) return;
//                msg = bundle.getString("MSG_NewProp_dialog_value");
//                desc = new NotifyDescriptor.InputLine(msg, title);
//                DialogDisplayer.getDefault().notify(desc);
//                String value = desc.getInputText();
                EntryPoint ep = EntryPoint.findInstance(key);
                PropertiesNotifier.changed();
            }
        } };
    }
    
    private static class DiscoverAction extends AbstractAction {        
        private String label;
        private String nodeName;
        public DiscoverAction(RootNode node){
            nodeName = node.getName();
            this.label = "Discover...";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            Discovery disc = new Discovery(nodeName);
            disc.start(); 
        }       
    }
    private static class StatusPollAction extends AbstractAction {        
        private String label;
        private String nodeName;
        public StatusPollAction(RootNode node){
            nodeName = node.getName();
            this.label = "Status...";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            StatusPoll nextPoll = new StatusPoll(nodeName);
            nextPoll.start();   
        }       
    }
    private static class HTreeAction extends AbstractAction {        
        private String label;
        private String nodeName;
        public HTreeAction(RootNode thisNode){
            nodeName = thisNode.getName();
            this.label = "Show HyperTree";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            HTreeTopComponent.findInstance(nodeName);
        }       
    }
//    private static class cleanUnmappedMgrsAction extends AbstractAction {        
//        private String label;
//        private String nodeName;
//        public cleanUnmappedMgrsAction(RootNode node){
//            nodeName = node.getName();
//            this.label = "Clear Unmapped";
//            putValue(Action.NAME, label);            
//        }        
//        public void actionPerformed(ActionEvent ae) {            
//            ObjectRepository.findInstance((String) this.getValue("networkName")).cleanUnmappedMgrs();
//            RootNode.refreshNode();
//        }       
//    }      
    private static class ShowAction extends AbstractAction {        
        private String label;
        public ShowAction(){            
            if (RootNode.findInstance().isHidden()) {
                this.label = "Show Top";
            } else {
                this.label = "Hide Top";
            }           
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {
            if (this.label.equals("Hide Top")) {
                RootNode.findInstance().setHidden(true);
            } else {
                RootNode.findInstance().setHidden(false);
            }
            RootNode.refreshNode();
        }       
    }
    private static class RenameAction extends AbstractAction {        
        private String label;        
        private Node node;
        public RenameAction(RootNode thisNode){
            node = thisNode;            
            this.label = "Rename node";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            String title = "Rename Entry-Point";
            String msg = "New Entry-Point Name:";
            NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(msg, title);
            DialogDisplayer.getDefault().notify(desc);
            String key = desc.getInputText();

            node.setName(key);
            SystemAction.get(RefreshNodeAction.class).actionPerformed(ae);

        }       
    }
    private static class DestroyAction extends AbstractAction {        
        private String label;        
        private Node node;
        public DestroyAction(RootNode thisNode){
            node = thisNode;            
            this.label = "Destroy node";
            putValue(Action.NAME, label);            
        }        
        public void actionPerformed(ActionEvent ae) {            
            if (node.canDestroy()){
                try {
                    node.destroy();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }            
        }       
    }
        
    public boolean canRename() {
        // allow rename by over-typing node lable
        boolean isEPNode = ((String) this.getValue("nodeType")).equals("EntryPoint");
        return isEPNode;
    }
    public void setName(String nue) {
        String nodeType = (String) this.getValue("nodeType");
        if (nodeType.equalsIgnoreCase("EntryPoint")){
            EntryPoint ep = EntryPoint.findInstance(this.getName());
            ep.setName(nue);
//            EntryPoint.destroyInstance(this.getName());
            ObjectRepository.renameRepository(this.getName(), nue);

            super.setName(nue);
            this.setDisplayName(nue);
            this.key = nue;
            NBNodes.remove(nue);
            NBNodes.put(nue, this);

        }        
    }
    public boolean canDestroy() {
        boolean isEPNode = ((String) this.getValue("nodeType")).equals("EntryPoint");
        return isEPNode;
    }
    public void destroy() throws IOException {
        String nodeType = (String) this.getValue("nodeType");
        if (nodeType.equalsIgnoreCase("EntryPoint")){
            EntryPoint.destroyInstance(this.getName());            
            HTreeTopComponent.findInstance(this.getName()).close();
            ObjectRepository.destroyInstance(this.getName());        
        }
//        super.destroy();
        RootNode.refreshNode();
    }
    
    public void addToNBNodes(String key, Node node){
        NBNodes.put(key, node);
    }
    public Node getNBNode(String key){
        return (Node)NBNodes.get(key);
    }
    
    public static RootNode findInstance() {
        if (rootNode == null) {
            rootNode = new RootNode();
        }
        return rootNode;
    }
    
    public static RootNode refreshNode() {        
//        PropertiesNotifier.changed();
        RootNode root = RootNode.findInstance();
        
        try {
            SystemAction.get(RefreshNodeAction.class).actionPerformed(new ActionEvent(root, 1001, root.getRefreshCommandKey()));
        } catch (AssertionError ae) {
            // do nothing
        }

        HashMap NBNodeMap = (HashMap) findInstance().NBNodes.clone();
        Iterator e = NBNodeMap.keySet().iterator();
        while (e.hasNext()){
            Node nextNode = (Node) NBNodeMap.get(e.next());
            if (nextNode.getClass() == ParentNode.class){
                ((ParentNode)nextNode).refreshSheet();
            } else if (nextNode.getClass() == LeafNode.class){
                ((LeafNode)nextNode).refreshSheet();
            }
        }        
        return rootNode;
    }
    
    public static String getTopName(){
        return topName;
    }    
    
    public static void destroyNodes(){
        findInstance().NBNodes = new HashMap();
        try {
            findInstance().destroy();            
        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }

}