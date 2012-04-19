/*
 * HyperTree.java
 * www.bouthier.net
 *
 * The MIT License :
 * -----------------
 * Copyright (c) 2001 Christophe Bouthier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.qview.gui.hypertree;

import java.util.ArrayList;

/**
 * The HyperTree class implements an hyperbolic tree representation for data.
 * <P>
 * An HyperTree is build from hierarchical data, given as a tree
 * of HTNode. So, the first parameter to give to build an HyperTree
 * is the HTNode which is the root of the to-be-represented tree.
 * The tree to be displayed by the HyperTree should be built before the call
 * to HyperTree, that is the root node should return children
 * when the children() method is called.
 * <P>
 * You can get a HTView (herited from JView) containing the HyperTree by calling
 * getView().
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
public class HTree {

    public static final int SHAPE_RECT= 1;
    public static final int SHAPE_METRIC_CIRC= 2;
    public static final int SHAPE_ROUND_RECT= 3;
    public static final int SHAPE_OVAL= 4;
    public static final int SHAPE_METRIC_SQAR= 5;
    public static final int SHAPE_DEFAULT= SHAPE_RECT;
    
    private HTModel model = null; // the model of the tree for the HyperTree
    private HTView view= null;
    
    // Added Start---------------------------------------------------------
    private HTNode selectedNode= null;
    private ArrayList nodeListeners= new ArrayList();
    // Added End-----------------------------------------------------------

  /* --- Constructor --- */

    /**
     * Constructor.
     *
     * @param root    the root of the tree to be represented;
     *                could not be <CODE>null</CODE>
     */
    public HTree(HTNode root) {
        model = new HTModel(root);        
//        setSelectedNode(root);
    }

    public void addChild(HTNode parent, HTNode child){
        model.getRoot().addNewChild(this, parent, child);
        //((HTModelNodeComposite)model.getRoot()).computeWeight();
        //model.getRoot().layoutHyperbolicTree();
        view.repaint();
    } //addChild

  /* --- View --- */

    /**
     * Returns a view of the hypertree.
     *
     * @return              the desired view of the hypertree
     */
    public HTView getView() {
        if (view==null){
            view= new HTView(model, this);
        }
        return view;
    } //getView
    
    // Added Start---------------------------------------------------------
    public void setSelectedNode(HTNode newNode){
        if(selectedNode!= newNode){
            HTNode oldNode= selectedNode;
            selectedNode= newNode;
            if (oldNode!= null){
                oldNode.setSelected(false);
            } //if
            if (newNode!= null){
                newNode.setSelected(true);
            } //if
            // This set recenters the hypertree on the selected node
            fireNodeChanged(oldNode, newNode);
            if (view!= null){
                HTDraw x= view.getHTDraw();
                if ((x!= null) && (newNode!= null) && (newNode.getModelNode()!= null) && 
                        (newNode.getModelNode().getDrawNode()!= null)){
                    x.translateToOrigin(newNode.getModelNode().getDrawNode());
                } //if
            } //if
        } //if
    } //setSelectedNode
    
    public void changeSelectedNode(HTNode newNode){
        if(selectedNode!= newNode){
            HTNode oldNode= selectedNode;
            selectedNode= newNode;
            if (oldNode!= null){
                oldNode.setSelected(false);
            } //if
            if (newNode!= null){
                newNode.setSelected(true);
            } //if            
        } //if
    } //changeSelectedNode
    
    public HTNode getSelectedNode(){
        return selectedNode;
    } //getSelectedNode
    
    private void fireNodeChanged(HTNode oldNode, HTNode newNode){
        for (int i = 0; i < nodeListeners.size(); i++) {
            ((NodeListener)nodeListeners.get(i)).nodeChanged(oldNode, newNode);
        } //for
    } //fireNodeChanged
    
    public void addNodeListener(NodeListener nli){
        nodeListeners.add(nli);
    } //addNodeListener
    
    public void removeNodeListener(NodeListener nli){
        nodeListeners.remove(nli);
    } //removeNodeListener
    // Added End-----------------------------------------------------------
    
}

