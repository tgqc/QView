/*
 * HTModel.java
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

/**
 * The HTModel class implements the model for the HyperTree.
 * It's a tree of HTModelNode and HTModelNodeComposite, each keeping the
 * initial layout of the tree in the Poincarre's Model.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTModel {

    private HTModelNode fRootModel   = null; // the fRootModel of the tree's model 
    private static final double flp = 0.4;
    private static final double flk = 0.08;

    private double      fLength = flp;  // distance between node and children
    private int         fNodeCount  = 0;    // number of fNodeCount


  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * @param fRootModel    the fRootModel of the real tree
     */
    HTModel(HTNode rootNode) {
        if (rootNode.isLeafNode()) {
            this.fRootModel = new HTModelNode(rootNode, this);
        } else {
            this.fRootModel = new HTModelNodeComposite(rootNode, this);
        }
        this.fRootModel.layoutHyperbolicTree();
    }


  /* --- Accessor --- */

    /**
     * Returns the fRootModel of the tree model.
     * 
     * 
     * @return the fRootModel of the tree model
     */
    HTModelNode getRoot() {
        return fRootModel;
    }


  /* --- Length --- */

    /**
     * Returns the distance between a node and its children
     * in the hyperbolic space.
     *
     * @return    the distance
     */
    double getLength() {
        return fLength;
    }

    void setLengthPoincare() {
        fLength = flp;
    }

    void setLengthKlein() {
        fLength = flk;
    }

    void layoutHyperbolicTree() {
        fRootModel.layoutHyperbolicTree();
    }


  /* --- Number of fNodeCount --- */
  
    /**
     * Increments the number of fNodeCount.
     */
    void incrementNumberOfNodes() {
        fNodeCount++;
    }
    
    /**
     * Returns the number of fNodeCount.
     * 
     * 
     * @return the number of fNodeCount
     */
    int getNumberOfNodes() {
        return fNodeCount;
    }

}

