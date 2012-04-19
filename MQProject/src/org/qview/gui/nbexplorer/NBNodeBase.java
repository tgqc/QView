/*
 * NBNode.java
 *
 * Created on 1 May 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.gui.nbexplorer;

import org.qview.data.ObjectRepository;
import org.qview.data.mqmodel.WMQObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author T.R.Goodwill
 */

/**
 * This class will eventually be the 'base' node class
 */
public class NBNodeBase extends AbstractNode {      
    private Object dataObject = null;
    
    public NBNodeBase(String key) {
        super(Children.LEAF);
    }
    public NBNodeBase(String key, String type, String networkName) {
        super(Children.LEAF);
        this.setValue("networkName", networkName);
        this.setValue("nodeType", type);        
    }
    public NBNodeBase(Object obj, String objName, String type, String networkName) {        
        this(objName);
        this.setValue("networkName", networkName);
        this.setValue("nodeType", type);        
        this.dataObject = obj;        
    }
    
    public Object getMQObject(){
        return this.dataObject;
    }
}