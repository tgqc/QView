/*
 * HTModelNodeComposite.java
 * www.bouthier.net
 *
 * The MIT License :
 * -----------------
 * Copyright (c) 2001-2003 Christophe Bouthier
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

import java.util.Enumeration;
import java.util.Vector;

/**
 * The HTModelNodeComposite class implements the Composite design pattern
 * for HTModelNode.
 * It represents a HTModelNode which is not a leaf.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTModelNodeComposite 
    extends HTModelNode {

    private Vector childrenModels     = null; // childrenModels of this node

    private double globalWeight = 0.0;  // sum of childrenModels weight


  /* --- Constructor --- */

    /**
     * Constructor for root node.
     * 
     * 
     * @param node     the encapsulated HTNode
     * @param htModel    the tree htModel using this HTModelNode
     */
    HTModelNodeComposite(HTNode node, HTModel model) {
        this(node, null, model);
    }

    /**
     * Constructor.
     * 
     * 
     * 
     * @param node      the encapsulated HTNode
     * @param parentModel    the parentModel node
     * @param htModel     the tree htModel using this HTModelNode
     */
    HTModelNodeComposite(HTNode node, HTModelNodeComposite parent, 
                         HTModel model) {
        super(node, parent, model);
        this.childrenModels = new Vector();

        HTNode childNode = null;
        HTModelNode childModel = null;
        for (Enumeration e = node.children(); e.hasMoreElements(); ) {
            childNode = (HTNode) e.nextElement();
            if (childNode.isLeafNode()) {
                childModel = new HTModelNode(childNode, this, model);
            } else {
                childModel = new HTModelNodeComposite(childNode, this, model);
            }
            addChild(childModel);
        }
        
        // here the down of the tree is built, so we can compute the weight
        computeWeight();
    }

    void addNewChild(HTree hyperTree, HTNode parentNode, HTNode childNode){
        if (this.getNode()==parentNode){
            //need to add childNode to this node
            HTModelNodeComposite childModel = new HTModelNodeComposite(childNode, this, htModel);
            addChild(childModel);
            System.out.println("Added HTModelNodeComposite "+childNode.getName()+ " to "+ parentNode.getName());
            hyperTree.getView().addNewChild(this, childModel);
        } else {
            // try the childrenModels (they may be the parentNode node
            HTModelNode childModel= null;
            for (Enumeration e = children(); e.hasMoreElements(); ) {
                childModel = (HTModelNode) e.nextElement();
                childModel.addNewChild(hyperTree, parentNode, childNode);
            } //for
        } //else
    //    computeWeight();
    } //addChild

    

  /* --- Weight Managment --- */

    /**
     * Compute the Weight of this node.
     * As the weight is computed with the log
     * of the sum of childModel's weight, we must have all childrenModels 
     * built before starting the computing.
     */
    void computeWeight() {
        globalWeight= 0;
//        weight= 1;
        HTModelNode childModel = null;
         
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childModel = (HTModelNode) e.nextElement();
            globalWeight += childModel.getWeight();
        } 
        if (globalWeight != 0.0) {
            weight += Math.log(globalWeight);
        }
    }


  /* --- Tree management --- */

    /**
     * Returns the childrenModels of this node, 
     * in an Enumeration.
     * 
     * 
     * @return the childrenModels of this node
     */
    Enumeration children() {
        return childrenModels.elements();
    }

    /**
     * 
     * Adds the HTModelNode as a childrenModels.
     * 
     * 
     * @param child    the child
     */
    void addChild(HTModelNode child) {
        childrenModels.addElement(child);
    }

    /**
     * Returns <CODE>false</CODE> as this node
     * is an instance of HTModelNodeComposite.
     *
     * @return    <CODE>false</CODE>
     */
    boolean isLeaf() {
        return false;
    }


  /* --- Hyperbolic layout --- */

    /**
     * Layout this node and its childrenModels in the hyperbolic space.
     * Mainly, divide the width angle between childrenModels and
     * put the childrenModels at the right angle.
     * Compute also an optimized length to the childrenModels.
     * 
     * 
     * 
     * @param sector    the sector
     * @param length    the parentModel-child length
     */
   void layout(HTSector sector, double length) {
        super.layout(sector, length);   

        if (parentModel != null) {
            sector.translate(parentModel.getCoordinates());
            HTCoordE h = new HTCoordE(getCoordinates());
            h.fx = -h.fx;
            h.fy = -h.fy;
            sector.translate(h);
        }

        int nbrChild = childrenModels.size();
        double l1 = (0.95 - htModel.getLength());
        double l2 = Math.cos((20.0 * Math.PI) / (2.0 * nbrChild + 38.0)); 
        length = htModel.getLength() + (l1 * l2);

        double   alpha = sector.getAngle();
        double   omega = sector.A.arg();
        HTCoordE K     = new HTCoordE(sector.A);

        // It may be interesting to sort childrenModels by weight instead
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            HTModelNode child = (HTModelNode) e.nextElement();
            HTSector childSector = new HTSector();
            childSector.A = new HTCoordE(K); 
            omega += (alpha * (child.getWeight() / globalWeight));
            K.fx = Math.cos(omega);
            K.fy = Math.sin(omega); 
            childSector.B = new HTCoordE(K);
            child.layout(childSector, length);
        }

    }


  /* --- ToString --- */

    /**
     * Returns a string representation of the object.
     *
     * @return    a String representation of the object
     */
    public String toString() {
        String result = super.toString();
        HTModelNode childModel = null;
        result += "\n\tChildren :";
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childModel = (HTModelNode) e.nextElement();
            result += "\n\t-> " + childModel.getName();
        }
        return result;
    }

}

