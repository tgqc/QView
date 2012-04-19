// Added Start-------------------------------------------------------------

/*
 * NodeListenerInterface.java
 *
 * Created on June 12, 2006, 4:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.gui.hypertree;

import org.qview.control.GuiDataAdapter;
import org.qview.control.ExplorerTree;

public class NodeListener {
    private ExplorerTree treeStructure= null;
    
    public NodeListener(ExplorerTree tree){
        treeStructure= tree;
    } //constructor
    
    public void nodeChanged(HTNode oldNode, HTNode newNode){
        // this code is called whenever the hypertree node changes
        
        // if editing oldNode then save it? 
        // treeStructure.<select newNode>
        // etc...
        
        // code below will fall over if either node is null
//        JOptionPane.showMessageDialog(null, "Node changed from "+ 
//                oldNode.getName()+ " to "+ newNode.getName(), "NodeListener", 
//                JOptionPane.PLAIN_MESSAGE);
        HTNodeBase simpleNode = (HTNodeBase)newNode;
        String networkName = simpleNode.getNetworkName(); //TODO
                // to ensure correct nbnode selection
        GuiDataAdapter.findInstance().nodeChanged(simpleNode, networkName);
//        NodeExplorerTopComponent.findInstance().selectNode(simpleNode.getUniqueName(), networkName);
    } //nodeChanged
    
} //NodeListenerInterface

// Added End---------------------------------------------------------------

