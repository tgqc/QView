/*
 * HTAction.java
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

import java.awt.event.*;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.qview.control.GuiDataAdapter;

/**
 * The HTAction class manage the action on the hypertree :
 * drag of a node...
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTAction
    extends MouseAdapter
    implements MouseMotionListener {

    private HTDraw   fModelDraw      = null; // the drawing fModelDraw
    private HTView   fView       = null; // the fView

    private HTCoordE fStartPoint = null; // starting point of dragging
    private HTCoordE fEndPoint   = null; // ending point of dragging
    private HTCoordS fClickPoint = null; // clicked point    

    private boolean  fKleinMode = false;
    private boolean  kleinToggled = false;
    
    private HTree fHyperTree= null;
    
    private HTNodeBase nodeA = null;    


  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * 
     * @param fModelDraw    the drawing fModelDraw
     * @param fView     the fView
     */
    HTAction(HTDraw modelDraw, HTView view, HTree hyperTree) {
        this.fModelDraw = modelDraw;
        this.fView = view;
        this.fHyperTree= hyperTree;
        fStartPoint = new HTCoordE();
        fEndPoint = new HTCoordE();
        fClickPoint = new HTCoordS();
    }


  /* --- MouseAdapter --- */

    /**
     * Called when a user pressed the mouse button
     * on the hyperbolic tree.
     * Used to get the starting point of the drag.
     *
     * @param e    the MouseEvent generated when clicking
     */
    public void mousePressed(MouseEvent e) {
        
        /* TODO the following is a quick fix - the listener is broken */
        fClickPoint.fx = e.getX();
        fClickPoint.fy = e.getY();

        HTDrawNode nodeB = fModelDraw.findNode(fClickPoint);
        if (nodeB != null){
            nodeA = (HTNodeBase)nodeB.getNode();
            String networkName = nodeA.getNetworkName();
            GuiDataAdapter.findInstance().nodeChanged(nodeA, nodeA.getNetworkName());            
        }
        /* -- */
        
        if (e.isShiftDown()) {
            fModelDraw.fastMode(true);
        }
        if (e.isControlDown()) {
            fModelDraw.longNameMode(true);
        }
        fStartPoint.projectionStoE(e.getX(), e.getY(), 
                                  fModelDraw.getSOrigin(),
                                  fModelDraw.getSMax());
    }

    /**
     * Called when a user release the mouse button
     * on the hyperbolic tree.
     * Used to signal the end of the translation.
     *
     * @param e    not used here
     */
    public void mouseReleased(MouseEvent e) {
        if (fView.getFastMode() == false) {
            fModelDraw.fastMode(false);
        }
        if ((fView.getLongNameMode() == false) && !kleinToggled) {
            fModelDraw.longNameMode(false);
        }
        fModelDraw.endTranslation();
    }

    /**
     * Called when a user clicked on the hyperbolic tree.
     * Used to put the corresponding node (if any) at the
     * center of the hyperbolic tree.
     *
     * @param e    the MouseEvent generated when clicking
     */
    public void mouseClicked(MouseEvent e) {        
//        System.out.println(e.getButton() + " " + e.BUTTON2);        
        if (e.getButton() == e.BUTTON3){            
            fClickPoint.fx = e.getX();
            fClickPoint.fy = e.getY();

            HTDrawNode nodeB = fModelDraw.findNode(fClickPoint);
            if (nodeB != null){
                nodeA = (HTNodeBase)nodeB.getNode();
                //GuiDataAdapter.findInstance().nodeChanged(nodeA, nodeA.getNetworkName());
                createPopupMenu(e, nodeA);
            }
            
        } else if (e.getClickCount() > 1) {            
            fClickPoint.fx = e.getX();
            fClickPoint.fy = e.getY();
            HTDrawNode node = fModelDraw.findNode(fClickPoint);
            if (node != null) {                     
                //centers selected node
                fHyperTree.setSelectedNode(node.getNode());                    
                fModelDraw.translateToOrigin(node);
            }                  
        } else{
            if (e.isShiftDown()) {
                fModelDraw.restore();
            } else if (e.isControlDown()) {
                switchKleinMode();
            } else { 
                fClickPoint.fx = e.getX();
                fClickPoint.fy = e.getY();

                HTDrawNode node = fModelDraw.findNode(fClickPoint);
                if (node != null) {                     
                    //centers selected node
//                    fHyperTree.setSelectedNode(node.getNode());                    
//                    fModelDraw.translateToOrigin(node);
                    // alternative - select only.
                    fHyperTree.changeSelectedNode(node.getNode());
                    // TODO added to fix properties tracking - handling by the node listener is broken
                    nodeA = (HTNodeBase)node.getNode();
                    GuiDataAdapter.findInstance().nodeChanged(nodeA, nodeA.getNetworkName());
                }
            }
        }
        
    }

    private void switchKleinMode() {
        if (fKleinMode) {
            fKleinMode = false;
        } else {
            fKleinMode = true;
        }
        fModelDraw.kleinMode(fKleinMode);
    }
    
    public void switchLongNameMode(boolean newMode) {
//        fKleinMode = newMode;
        fModelDraw.longNameMode(newMode);
        kleinToggled = newMode;
//        fModelDraw.kleinMode(fKleinMode);
    }

  /* --- MouseMotionListener --- */

    /**
     * Called when a used drag the mouse on the hyperbolic tree.
     * Used to translate the hypertree, thus moving the focus.
     *
     * @param e    the MouseEvent generated when draging
     */
    public void mouseDragged(MouseEvent e) {
        if (fStartPoint.isValid()) {
            fEndPoint.projectionStoE(e.getX(), e.getY(), 
                                    fModelDraw.getSOrigin(),
                                    fModelDraw.getSMax());
            if (fEndPoint.isValid()) {
                fModelDraw.translate(fStartPoint, fEndPoint);
            }
        }
    }
    
    private void createPopupMenu(MouseEvent evt, HTNodeBase node) {
        JPopupMenu jPopupMenu1 = new JPopupMenu();
        JMenuItem menuItem;
        
//        Action[] actions = ModelAdapter.findInstance().getContext(node);
        Action[] actions = node.getActions(true);
        for (int i=0;i<actions.length;i++){
            menuItem = new JMenuItem(actions[i]);
            jPopupMenu1.add(menuItem);
        }
        menuItem = new JMenuItem();
        org.openide.awt.Actions.connect(menuItem, GuiDataAdapter.findInstance().getPropertiesAction(), true);
        //org.openide.awt.Actions.connect(menuItem, SystemAction.get(PropertiesAction.class), true);
        jPopupMenu1.add(menuItem);

        jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    /**
     * Called when the mouse mouve into the hyperbolic tree.
     * Not used here.
     *
     * @param e    the MouseEvent generated when mouving
     */
    public void mouseMoved(MouseEvent e) {}

}

