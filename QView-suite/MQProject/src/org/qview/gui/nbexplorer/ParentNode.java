/*
 * LeafNode.java
 *
 * Based on the 'PropChildren' class, Netbeans project,
 * Created on June 13, 2005, 2:08 PM
 *
 * This node constructs the 'parent' org.openide.nodes.Node objects,
 * and refers to the 'Tree' class to extrapolate children.
 */

package org.qview.gui.nbexplorer;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.qview.control.ExplorerTree;
import org.qview.control.GuiDataAdapter;
import org.qview.data.mqmodel.WMQObject;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author T.Goodwill
 */
public class ParentNode extends AbstractNode {    
    private static ResourceBundle bundle = NbBundle.getBundle(LeafNode.class);
    private WMQObject mqobject = null;
    private String key;
    private String nodeType;    
//    private WMQObject mqobject = new WMQObject();
    private ChangeListener listener;
     
    public ParentNode(String key) {
        super(new ExplorerTree());
        this.key = key;
        setIconBase("org/qview/gui/LeafIcon");
        //setDefaultAction(SystemAction.get(PropertiesAction.class));
        super.setName(key);
        setShortDescription(bundle.getString("HINT_LeafNode"));
        RootNode.findInstance().addToNBNodes(this.getName(), this);
    }
   /* Constructors with Children.Keys refers to the 'Tree' class to extrapolate children
    */
    public ParentNode(String key, Children.Keys newKeys) {
        super(newKeys);
        this.key = key;
        super.setName(key);
        super.setDisplayName(key);
        setShortDescription(bundle.getString("HINT_LeafNode"));
        RootNode.findInstance().addToNBNodes(this.getName(), this);
    }
    public ParentNode(String key, Children.Keys newKeys, String networkName, String type) {
        this(key, newKeys);
        this.nodeType = type;
        // This node is not an MQ object, therefore name is prefixed by QMgr ID.
        this.setDisplayName(type);
        this.setValue("networkName", networkName);
        // QMgr children may show or hide system objects
        this.setValue("nodeType", type);
        this.setValue("system", "hide");
        setIconBase("org/qview/gui/" + type + "Icon");
        setShortDescription(bundle.getString("HINT_" + type));
    }
    public ParentNode(WMQObject obj, Children.Keys newKeys, String networkName, String type) {
        this(obj.getUniqueName(), newKeys);
        this.setDisplayName(obj.getCaption());
        this.mqobject = obj;
        this.nodeType = type;
        this.setValue("networkName", networkName);
        this.setValue("nodeType", type);        
        setIconBase("org/qview/gui/" + type + "Icon");
        setShortDescription(bundle.getString("HINT_" + type));
    }

    public void hideSystem(boolean hidden){
        Node[] nodes = this.getChildren().getNodes();
        // iterate through nodes to determine if they represent system objects
        for (int i=0;i<nodes.length;i++){
            Node thisNode = nodes[i];
            boolean isSystem = (thisNode.getDisplayName().startsWith("SYSTEM") || thisNode.getDisplayName().startsWith("AMQ") || thisNode.getDisplayName().startsWith("MQAI"));

            if (isSystem){
                thisNode.setHidden(hidden);
            }
        }
    }
    
    public Action[] getActions(boolean context) {
        Action[] actions;
        Action[] result;
        Action[] newActions;

        if ( (nodeType.equals("Queues")) || (nodeType.equals("Channels")) ) {
            actions = new Action[] {
                new ShowSystemAction(this),
                SystemAction.get(ToolsAction.class),
                SystemAction.get(RefreshNodeAction.class),
                SystemAction.get(OpenLocalExplorerAction.class),
                SystemAction.get(PropertiesAction.class),
                null,
            };
        } else {
            actions = new Action[] {
                SystemAction.get(ToolsAction.class),
                SystemAction.get(RefreshNodeAction.class),
                SystemAction.get(OpenLocalExplorerAction.class),
                SystemAction.get(PropertiesAction.class),
                null,
            };
        }
          
        result = GuiDataAdapter.findInstance().getContext(this);
        
        int actionsSize = actions.length;
        int resultSize = result.length;        
        newActions = new Action[actionsSize+resultSize];
        
        for (int i=0;i<actionsSize;i++){
            newActions[i] = actions[i];
        }
        for (int j=0;j<result.length;j++){
            newActions[actionsSize+j] = result[j];
        }        
        return newActions;
    }
    
    public void getProperties(){
        SystemAction.get(PropertiesAction.class);
    }
    
    public WMQObject getMQObject(){
        return this.mqobject;
    }
        
    public Node cloneNode() {
        return new LeafNode(key);
    }
    protected Sheet createSheet() {    
        Sheet cleanSheet = super.createSheet();
        PropSheet propSheet = new PropSheet(cleanSheet, this);
        Sheet sheet = propSheet.getPropSheet();
        PropertiesNotifier.addChangeListener(listener = new
                ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                firePropertyChange("value", null, null);
                refreshSheet();
            }
        });
        return sheet;
    }
    public Sheet refreshSheet() {        
        Sheet sheet = super.getSheet();        
        PropSheet propSheet = new PropSheet(sheet, this);       
        return sheet;        
    }
    protected void finalize() throws Throwable {
        super.finalize();
        if (listener != null)
            PropertiesNotifier.removeChangeListener(listener);
    }
    public boolean canRename() {
        return false;
    }
    public void setName(String nue) {
//        PropertiesNotifier.changed();
    }
    public boolean canDestroy() {
        return false;
    }
    public void destroy() throws IOException {
//        PropertiesNotifier.changed();
    }

    private static class ShowSystemAction extends AbstractAction {
        private Node node;
        private String label;
        private String nodeName;
        public ShowSystemAction(ParentNode node){
            String system = (String) node.getValue("system");
            this.node = node;
            if ((system != null) && (system.equals("show"))){
                this.label = "Hide System";
            } else {
                this.label = "Show System";
            }
            putValue(Action.NAME, label);
        }
        public void actionPerformed(ActionEvent ae) {
            String system = (String) node.getValue("system");
            if ((system == null) || (system.equals("hide"))){
                node.setValue("system", "show");
//                ((ParentNode)node).hideSystem(false);
            } else {
                node.setValue("system", "hide");
//                ((ParentNode)node).hideSystem(true);
            }
//            NodeExplorerTopComponent.findInstance().requestActive();
//            NodeExplorerTopComponent.findInstance().reloadExplorer();
            ((ParentNode)node).refreshSheet();
            RootNode.refreshNode();
        }
    }

}