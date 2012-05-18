/*
 * HTSimpleNode.java
 *
 * Created on April 16, 2006, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.gui.hypertree;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.qview.control.GuiDataAdapter;
import org.qview.gui.nbexplorer.PropSheet;
import org.qview.gui.nbexplorer.PropertiesNotifier;
import org.qview.data.ObjectRepository;
import org.qview.data.mqmodel.WMQChannel;
import org.qview.data.mqmodel.WMQCluster;
import org.qview.data.mqmodel.WMQObject;
import org.qview.data.mqmodel.WMQProcess;
import org.qview.data.mqmodel.WMQQMgr;
import org.qview.data.mqmodel.WMQQueue;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author sharon
 */
public class HTNodeBase extends AbstractNode implements HTNode {
    private static final int SHAPE_CLUSTER= HTree.SHAPE_OVAL;
    private static final int SHAPE_QMGR= HTree.SHAPE_ROUND_RECT;
    private static final int SHAPE_PROCESS= HTree.SHAPE_RECT;
    private static final int SHAPE_CHANNEL= HTree.SHAPE_METRIC_SQAR;
    private static final int SHAPE_QUEUE= HTree.SHAPE_METRIC_CIRC;
    private static final int SHAPE_DEFAULT= HTree.SHAPE_RECT;
    
    private WMQObject fMQObject = null; // the WMQObject encapsulated
    private ObjectRepository repository;
    private ChangeListener listener;
    private String uniqueName;
    private String networkName;
    private Hashtable fChildrenMQ = null; // the fChildrenMQ of this node
    private int fShape = SHAPE_DEFAULT;
    private Color fColour = Color.MAGENTA;
    private boolean fSelected= false;
    
    private HTModelNode fHTModelNode;
            
    
    /** Creates a new instance of HTSimpleNode */
    public HTNodeBase(String folder) {
        super(Children.LEAF);
        uniqueName = folder;
        fChildrenMQ= new Hashtable();        
        fShape= SHAPE_QUEUE;
        fColour= HTColours.Q_INNER;       
    } //HTSimpleNode
    public HTNodeBase(WMQObject mq) {
        super(Children.LEAF);
        repository = ObjectRepository.findInstance(networkName);
        fMQObject= mq;
        uniqueName = mq.getUniqueName();
        networkName = mq.getNetworkName();
        
        fChildrenMQ= new Hashtable();
        if (mq.getClass()== WMQChannel.class) {
            fShape= SHAPE_CHANNEL;
            fColour= HTColours.CH_INNER;
        } else if (mq.getClass()== WMQCluster.class) {
            fShape= SHAPE_CLUSTER;
            fColour= HTColours.CL_INNER;
        } else if (mq.getClass()== WMQQMgr.class) {
            fShape= SHAPE_QMGR;
            fColour= HTColours.QM_INNER;
        } else if (mq.getClass()== WMQProcess.class) {
            fShape= SHAPE_PROCESS;
            fColour= HTColours.PR_INNER;
        } else if (mq.getClass()== WMQQueue.class) {
            fShape= SHAPE_QUEUE;
            fColour= HTColours.Q_INNER;
        }//if

    } //HTSimpleNode

    public Action[] getActions(boolean context) {
        Action[] actions;
        Action[] result;
        Action[] newActions;        
        
//        this.setSheet(createSheet());        
//        super.setSheet(createSheet());        
//        this.refreshSheet();
        
        actions = new Action[] {                
                //SystemAction.get(PropertiesAction.class),
                //null,
        };        
        result = GuiDataAdapter.findInstance().getContext(this);
        
        int actionsSize = actions.length;
        int resultSize = result.length;        
        newActions = new Action[actionsSize+resultSize];
        
        for (int j=0;j<result.length;j++){
            newActions[j] = result[j];
        }
        for (int i=0;i<actionsSize;i++){
            newActions[result.length + i] = actions[i];
        }
        
        return newActions;
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
        Sheet sheet = super.getSheet();        
        PropSheet propSheet = new PropSheet(sheet, this);
        return sheet;
    }
    
    public Enumeration children() {
        return fChildrenMQ.elements();
    } //fChildrenMQ

    public boolean isLeafNode() {
        return false;
        // means we will be able to add fChildrenMQ if needed
        //return fChildrenMQ.isEmpty();
    } //isLeaf

    public String getName() {
        String caption;
        // TODO - delete: do not like this.
//        if ((fMQObject.getClass() == WMQQMgr.class) && HTreeTopComponent.findInstance(networkName).getShowLables()) {
//            caption = fMQObject.getCaption() + " " + System.getProperty("line.separator") + ((WMQQMgr)fMQObject).getConnName();
//        } else {
            caption = fMQObject.getCaption();
//        }
        return caption;
    } //getName

    public Color getColor() {
        return fColour;
    } //getColor

    public int getShape() {
        return fShape;
    } //getShape
    
    public void addChild(HTNodeBase child){
        fChildrenMQ.put(child.getName(), child);
    } //addChild
    public boolean hasChildren(){
        return !fChildrenMQ.isEmpty();
    }
    public void setChildren(Hashtable children) {
        fChildrenMQ = children;
    } //setChildren

    public void setSelected(boolean value) {
        fSelected= value;
    } //setSelected

    public boolean getSelected() {
        return fSelected;
    } //getSelected

    public HTModelNode getModelNode() {
        return fHTModelNode;
    }

    public void setModelNode(HTModelNode value) {
        fHTModelNode= value;
    }
    
    public String getUniqueName(){
        return this.uniqueName;
    }
    
    public String getNetworkName(){
        return this.networkName;
    }
    
    public WMQObject getMQObject(){
        return this.fMQObject;
    }

    public boolean isNodeHub() {
//        boolean isSenderChannel = (this.fMQObject.getClass() == WMQChannel.class) && (((Integer)((WMQChannel) this.fMQObject).getAttribute("Channel Type")) < 5);
//        boolean isSenderChannel = (this.fMQObject.getClass() == WMQChannel.class) && (((WMQChannel) this.fMQObject).getAttribute("Connection Name") != null);
//        return isSenderChannel;
        return this.fMQObject.getClass() == WMQQMgr.class;
    }
    
}
