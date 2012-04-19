/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.qview.gui.nbexplorer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import javax.swing.ActionMap;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.qview.control.GuiDataAdapter;

public final class NodeExplorerTopComponent extends TopComponent implements ExplorerManager.Provider, Lookup.Provider {
    
    private static NodeExplorerTopComponent instance;   
    public static final String PREFERRED_ID = "NodeExplorerTopComponent";
    public static final String ICON_PATH = "org/qview/gui/ExplorerIcon.gif";
    private static final String MODE = "explorer"; //open mode

    private ExplorerManager manager = new ExplorerManager();
    private ExplorerManager manager2 = new ExplorerManager();
    private final BeanTreeView view = new BeanTreeView();
    private Lookup[] lookupArray = null;
    private ProxyLookup proxylookup = null;
    
//    private ObjectRepository repository;
    private PropertyChangeListener listener;    
    
    private NodeExplorerTopComponent() { 
//        System.out.println("NodeExplorerTopComponent()");
        setName(NbBundle.getMessage(NodeExplorerTopComponent.class, "CTL_NodeExplorerTopComponent"));
        setToolTipText(NbBundle.getMessage(NodeExplorerTopComponent.class, "HINT_NodeExplorerTopComponent"));
        setIcon(Utilities.loadImage("org/qview/gui/TopIcon.gif", true));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
        view.setRootVisible(true);
        
        RootNode root = RootNode.findInstance();
        
        try {
            manager.setRootContext(root);           
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }        
               
        ActionMap map = this.getActionMap();
        manager2 = (ExplorerManager) manager.clone();
        this.associateLookup(ExplorerUtils.createLookup(manager2, map));

//        TopComponent.Registry.PROP_ACTIVATED_NODES.
        this.manager.addPropertyChangeListener(listener = new
                PropertyChangeListener() {            
            public void propertyChange(PropertyChangeEvent ev) {                
                NodeExplorerTopComponent.findInstance().changedSelected(ev);
            }
        });

        instance = this;        
    }

    public void open()
    {
        Mode m = WindowManager.getDefault().findMode(MODE);
        m.dockInto(this);
        super.open();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public ExplorerManager getExplorerManagerClone() {
        // TODO - cleanup
        // faster not to re-map down to the selected node if already mapped
//        if (!manager2.getRootContext().getName().equals("MQ Environment")){
//            manager2 = (ExplorerManager) manager.clone();
//        }
        // set explored context and select nodes
//        try {
//            manager2.setExploredContextAndSelection(manager.getExploredContext(), manager.getSelectedNodes());
//        } catch (PropertyVetoException ex) {
//            ex.printStackTrace();
//        }
        return manager2;
    }
    protected void componentActivated(){
        ExplorerUtils.activateActions(manager, true);
    }
    protected void componentDeactivated(){
        ExplorerUtils.activateActions(manager, false);
    }

    public String getNetworkName(){
        return (String)manager.getSelectedNodes()[0].getValue("networkName");
    }
     
    public static synchronized NodeExplorerTopComponent getDefault() {        
        System.out.println("getDefault()");
        if (instance == null) {
            instance = new NodeExplorerTopComponent();
        }
        return instance;
    }
    
    public static void resetExplorer(){        
        RootNode.destroyNodes();        
        instance = new NodeExplorerTopComponent();
        findInstance().revalidate();
//        instance.requestActive();
    }

    public void refreshExplorer(){
        RootNode.refreshNode();
    }

     /**
     * Obtain the NodeExplorerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized NodeExplorerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Browse component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof NodeExplorerTopComponent) {
            return (NodeExplorerTopComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
//    public static synchronized NodeExplorerTopComponent findInstance() {
////        System.out.println("findInstance()");
//        if (instance == null) {
//            instance = new NodeExplorerTopComponent();            
//        }
//        return instance;
//    }  
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }          
    
    public void componentOpened() {        
        // TODO add custom code on component opening
    }
    
    public void componentClosed() {
        // TODO add custom code on component closing
    }
    
    /** replaces this in object stream */
    
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    protected Object writeReplace() {
        return new ResolvableHelper();
    }
    
    private static final class ResolvableHelper implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        public Object readResolve() {
            System.out.println("readResolve()");
            return NodeExplorerTopComponent.getDefault();
        }
        
    }
        
    public void changedSelected(PropertyChangeEvent ev){
        // faster not to re-map down to the selected node if already mapped
        if (!manager2.getRootContext().getName().equals("MQ Environment")){
            manager2 = (ExplorerManager) manager.clone();
        }
        // set explored context and select nodes
        try {
            manager2.setExploredContextAndSelection(manager.getExploredContext(), manager.getSelectedNodes());
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
        if ((ev.getNewValue() != null) && (ev.getNewValue().getClass() == Node[].class)){
            GuiDataAdapter.findInstance().changedSelected((Node[])ev.getNewValue());
        }
    }


}
