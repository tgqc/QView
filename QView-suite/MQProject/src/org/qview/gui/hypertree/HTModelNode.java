/*
 * HTModelNode.java
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

/**
 * The HTModelNode class implements encapsulation of a HTNode
 * for the htModel. 
 * It keeps the original euclidian coordinates of the htNode.
 * It implements the Composite design pattern.
 * 
 * 
 * 
 * 
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTModelNode {

    private   HTNode               htNode   = null; // encapsulated HTNode

    protected HTModel              htModel  = null; // tree htModel
    protected HTModelNodeComposite parentModel = null; // parentModel htNode

    protected HTCoordE             z      = null; // Euclidian coordinates
    protected double               weight = 1.0;  // part of space taken 
    
    private HTDrawNode fHTDrawNode;
    // by this htNode


  /* --- Constructor --- */

    /**
     * Constructor for root htNode.
     * 
     * 
     * 
     * 
     * @param htNode     the encapsulated HTNode
     * @param htModel    the tree htModel using this HTModelNode
     */
    HTModelNode(HTNode node, HTModel model) {
        this(node, null, model);
    }

    
    void addNewChild(HTree hyperTree, HTNode parentNode, HTNode childNode){
        //htModel.getRoot().addNewChild(parentModel, childNode);
    } //addChild

     /**
     * Constructor.
     * 
     * 
     * 
     * 
     * 
     * @param htNode      the encapsulated HTNode
     * @param parentModel    the parentModel htNode
     * @param htModel     the tree htModel using this HTModelNode
     */
    HTModelNode(HTNode htNode, HTModelNodeComposite parentModel, HTModel htModel) {
        this.htNode = htNode;
        htNode.setModelNode(this);
        this.parentModel = parentModel;
        this.htModel = htModel;
        htModel.incrementNumberOfNodes();
         
        z = new HTCoordE();
    }


  /* --- Encapsulated htNode --- */

    /**
     * Returns the encapsulated htNode.
     * 
     * 
     * 
     * @return the encapsulated htNode
     */
    HTNode getNode() {
        return htNode;
    }


  /* --- Name --- */

    /**
     * Returns the name of this htNode.
     * 
     * 
     * 
     * @return the name of this htNode
     */
    String getName() {
        return htNode.getName();
    }

    String getUniqueName() {
        return ((HTNodeBase) htNode).getUniqueName();
    }



  /* --- Weight Managment --- */

    /**
     * Returns the weight of this htNode.
     * 
     * 
     * 
     * @return the weight of this htNode
     */
    double getWeight() {
        return weight;
    }


  /* --- Tree management --- */

    /**
     * Returns the parentModel of this htNode.
     * 
     * 
     * 
     * 
     * @return the parentModel of this htNode
     */
    HTModelNodeComposite getParent() {
        return parentModel;
    }

    /**
     * Returns <CODE>true</CODE> if this htNode
     * is not an instance of HTModelNodeComposite.
     * 
     * 
     * 
     * @return <CODE>true</CODE>
     */
    boolean isLeaf() {
        return true;
    }


  /* --- Coordinates --- */

    /**
     * Returns the coordinates of this htNode.
     * Thoses are the original hyperbolic coordinates, 
     * without any translations.
     * WARNING : this is NOT a copy but the true object
     * (for performance).
     * 
     * 
     * 
     * @return the original hyperbolic coordinates
     */
    HTCoordE getCoordinates() {
        return z;
    }


  /* --- Hyperbolic layout --- */

    /**
     * Layouts the nodes in the hyperbolic space.
     */
    void layoutHyperbolicTree() {
        HTSector sector = new HTSector();
        double eps = 0.01;
        double d = Math.sqrt(1 - (eps * eps));
        sector.A = new HTCoordE(d, eps);
        sector.B = new HTCoordE(d, -eps);
        this.layout(sector, htModel.getLength());
    }

    /**
     * Layout this htNode in the hyperbolic space.
     * First set the point at the right distance,
     * then translate by father's coordinates.
     * Then, compute the right angle and the right width.
     * 
     * 
     * 
     * 
     * @param sector    the sector
     * @param length    the parentModel-child length
     */
    void layout(HTSector sector, double length) {
        // Nothing to do for the root htNode
        if (parentModel == null) {
            return;
        }
        
        HTCoordE zp = parentModel.getCoordinates();

        double angle = sector.getBisectAngle();

        // We first start as if the parentModel was the origin.
        // We still are in the hyperbolic space.
        z.fx = length * Math.cos(angle);
        z.fy = length * Math.sin(angle);

        // Then translate by parentModel's coordinates
        z.translate(zp);
    } 


  /* --- ToString --- */

    /**
     * Returns a string representation of the object.
     *
     * @return    a String representation of the object
     */
    public String toString() {
        String result = getName() +
                        "\n\t" + z +
                        "\n\tWeight = " + weight; 
        return result;
    }

    public HTDrawNode getDrawNode() {
        return fHTDrawNode;
    }

    public void setDrawNode(HTDrawNode value) {
        fHTDrawNode= value;
    }
}

