/*
 * LeafNode.java
 *
 * Based on the 'PropChildren' class, Netbeans project,
 * Created on June 13, 2005, 2:08 PM
 *
 * This node constructs the 'child', or leaf org.openide.nodes.Node objects.
 * There are unique Action Properties, and no children.
 */

package org.qview.gui.nbexplorer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.qview.control.GuiDataAdapter;
import org.qview.data.mqmodel.WMQObject;
import org.qview.data.ObjectRepository;
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
 */
public class LeafNode extends AbstractNode {
    private static ResourceBundle bundle = NbBundle.getBundle(LeafNode.class);
    private WMQObject mqobject = null;
    private String key;
    private ChangeListener listener;
    public LeafNode(String key) {
        super(Children.LEAF);
        this.key = key;
        setName(key);
        setIconBase("org/qview/gui/LeafIcon");      
        setShortDescription(bundle.getString("HINT_LeafNode"));
        RootNode.findInstance().addToNBNodes(this.getName(), this);        
    }
    public LeafNode(String key, String networkName, String type) {
        this(key);
        this.setValue("networkName", networkName);
        this.setValue("nodeType", type);        
        setIconBase("org/qview/gui/" + type + "Icon");
        //setDefaultAction(SystemAction.get(PropertiesAction.class));       
        setShortDescription(bundle.getString("HINT_" + type)); 
    }
    public LeafNode(WMQObject obj, String networkName, String type) {        
        this(obj.getUniqueName());
        this.setValue("networkName", networkName);
        this.setValue("nodeType", type);
        this.mqobject = obj;
        setName(obj.getUniqueName());
        setDisplayName(obj.getCaption());
        // Is the object a system object? Should it be hidden?
        boolean hideSystemSelected = true;
        boolean isSystemQueue = (this.getDisplayName().startsWith("SYSTEM") || this.getDisplayName().startsWith("AMQ") || this.getDisplayName().startsWith("MQAI"));
        if (this.getParentNode() != null && (this.getParentNode().getValue("system") != null)) {
            hideSystemSelected = ((String) this.getParentNode().getValue("system")).equals("hide");
        }
        // if both true, set to hidden
         this.setHidden(hideSystemSelected && isSystemQueue);
         
//         if ((obj.getClass() == WMQChannel.class) && (((Integer)obj.getStatus("Channel Status")).intValue() == 6)){ // 'Stopped'                
//             setIconBase("org/qview/gui/warning");
//        } else {
            setIconBase("org/qview/gui/" + type + "Icon");
//        }
        //setDefaultAction(SystemAction.get(PropertiesAction.class));       
        setShortDescription(bundle.getString("HINT_" + type)); 
        
    }

    /** Overridden. Sets the visiblity of the node.
    * Actually removes the node from it parent, keeping a WeakReference to
    * the parent,
    * so it can be re-added (when this is set to false again).
    * Also keeps a reference to this node while hidden to prevent from being
    * GC'd
    **/
//    public void setHidden(boolean state){
//        if( isHidden() != state ){
//            Node parent;
//            if( state ){
//                parent = getParentNode();
//                parent.getChildren().remove( new Node[]{this} );
//                hiddenParent = new WeakReference( parent );
//                hiddenInstance = this;
//            }else{
//                parent = (Node)hiddenParent.get();
//                if( parent != null )
//                parent.getChildren().add( new Node[]{this} );
//                hiddenInstance = null;
//            }
//        }
//        super.setHidden(state);
//    }
//    /** Remembers the parent while hidden **/
//    private WeakReference hiddenParent;
//    /** Holds a reference to myself while in hidden mode **/
//    private Node hiddenInstance;

    /** Defining the action (context menu) for queue and channel nodes */
    public Action[] getActions(boolean context) {
        Action[] actions;
        Action[] result;
        Action[] newActions;
        
        actions = new Action[] {            
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
                null,                
        };        
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
    /** Creating a property sheet the node - call to 'PropSheet' to populate */
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
    /** On refresh and change event, re-populate property sheet  */
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
    
    public String getName(){
        if (this.mqobject != null) {
            return this.mqobject.getUniqueName();
        } else {
            return key;
        }        
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
        
}