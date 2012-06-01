/*
 * HTDrawNode.java
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.util.HashMap;

/**
 * The HTDrawNode class contains the drawing coordinates of a HTModelNode 
 * for the HTView. 
 * It implements the Composite design pattern.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTDrawNode {

    protected   HTDraw            fModelDraw    = null;  // drawing fModelDraw
    private   HTModelNode         fNodeModel     = null;  // encapsulated HTModelNode
 
    private   HTCoordE            fze       = null;  // current euclidian coords
    private   HTCoordE            fOldZe    = null;  // old euclidian coords
    protected HTCoordS            fzs       = null;  // current screen coords

    private   HTDrawNodeComposite fFatherDraw   = null;  // fFatherDraw of this fNodeModel
    private   HTDrawNode          fBrotherDraw  = null;  // fBrotherDraw of this fNodeModel

    private   HTNodeLabel         fNodeLabel    = null;  // fNodeLabel of the fNodeModel

    protected boolean             fFastMode = false; // fast mode
    protected boolean             fLongNameMode = false; // long name displayed
    protected boolean             fKleinMode = false; // klein mode
// NEU
    protected double              fProjType = -2.0; // projection type
// FIN NEU


  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * @param fFatherDraw    the fFatherDraw of this fNodeModel
     * @param fNodeModel      the encapsulated HTModelNode
     * @param fModelDraw     the drawing fModelDraw
     */
    HTDrawNode(HTDrawNodeComposite fatherDraw, HTModelNode nodeModel, HTDraw modelDraw) {
        this.fFatherDraw = fatherDraw;
        this.fNodeModel = nodeModel;
        nodeModel.setDrawNode(this);
        this.fModelDraw = modelDraw;

        fNodeLabel = new HTNodeLabel(this);

        fze = new HTCoordE(nodeModel.getCoordinates());
        fOldZe = new HTCoordE(fze);
        fzs = new HTCoordS();
    }


  /* --- Brother --- */
  
    /**
     * Sets the fBrotherDraw of this fNodeModel.
     * 
     * 
     * 
     * 
     * 
     * @param fBrotherDraw    the borther of this fNodeModel
     */
    void setBrother(HTDrawNode brotherDraw) {
        this.fBrotherDraw = brotherDraw;
    }


  /* --- Encapsulated HYModelNode --- */

    /**
     * Returns the encapsulated HTModelNode.
     *
     * @return    the encapsulated HTModelNode
     */
    HTModelNode getHTModelNode() {
        return fNodeModel;
    }


  /* --- Color --- */

    /**
     * Returns the color of the fNodeModel.
     * 
     * 
     * 
     * @return the color of the fNodeModel
     */
    Color getColor() {
        return fNodeModel.getNode().getColor();
    }

  /* --- Shape --- */

    /**
     * Returns the shape of the fNodeModel.
     * 
     * 
     * 
     * @return the shape of the fNodeModel
     */
    int getShape() {
        return fNodeModel.getNode().getShape();
    } //getShape

  /* --- Name --- */

    /**
     * Returns the name of this fNodeModel.
     * 
     * 
     * 
     * @return the name of this fNodeModel
     */
    String getName() {
        return fNodeModel.getName();
    }

    String getUniqueName() {
        return fNodeModel.getUniqueName();
    }

    public HTModelNode getFNodeModel() {
        return fNodeModel;
    }

    HTNode getNode(){
        return fNodeModel.getNode();
    } //getNode

  /* --- Coordinates --- */

    /**
     * Returns the current coordinates of this fNodeModel.
     * WARNING : this is NOT a copy but the true object
     * (for performance).
     * 
     * 
     * 
     * @return the current coordinates
     */
    HTCoordE getCoordinates() {
        return fze;
    } 

    HTCoordE getOldCoordinates() {
        return fOldZe;
    }

    HTCoordS getScreenCoordinates() {
        return fzs;
    }

    /**
     * Refresh the screen coordinates of this fNodeModel.
     * 
     * 
     * 
     * @param sOrigin   the origin of the screen plane
     * @param sMax      the (xMax, yMax) point in the screen plane
     */
    void refreshScreenCoordinates(HTCoordS sOrigin, HTCoordS sMax) {
        if (fKleinMode) {
            fzs.projectionEtoS(fze.pToK(), sOrigin, sMax);
// NEU
        } else if (fProjType != 0.0) {
            fzs.projectionEtoS(fze.pToZ(fProjType), sOrigin, sMax);
// FIN NEU
        } else {
            fzs.projectionEtoS(fze, sOrigin, sMax);
        }
    } 


  /* --- Drawing --- */

    /**
     * Draws the branches from this fNodeModel to 
     * its children.
     * Overidden by HTDrawNodeComposite
     * 
     * 
     * 
     * @param g    the graphic context
     */
    void drawBranches(Graphics g) {}

    /**
     * Draws this fNodeModel.
     * 
     * 
     * 
     * @param g    the graphic context
     */
    void drawNodes(Graphics g) {
        if (fFastMode == false) {
            fNodeLabel.draw(g);
        }
    }

    void addNewChild(HTModelNodeComposite parentModel, HTModelNodeComposite childModel){
        if (this.fNodeModel==parentModel){
            // this is the fNodeModel we need to attach the new childModel to
            // **Except, we can't as this type of fNodeModel can't have children
            System.out.println("Unable to add node "+ childModel.getNode().getName()+" to parent "+ parentModel.getNode().getName()+" (Parent is HTDrawNode)");
        } //if           
    } //addNewChild
    void addChild(HTDrawNode childDraw) {
        fModelDraw.addHTDrawNode(childDraw);
    }
    void addChild(HTDrawNodeComposite childDraw) {
        fModelDraw.addHTDrawNode(childDraw);
    }
    HTDrawNode getChild(String key) {
        return fModelDraw.getHTDrawNode(key);
    }
    HashMap getChildren() {
        return fModelDraw.getHTDrawNodes();
    }

    
    /**
     * Returns the minimal distance between this fNodeModel
     * and his fFatherDraw and his fBrotherDraw.
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * @return the minimal distance
     */
    int getSpace() {
        int dF = -1;
        int dB = -1;
        
        if (fFatherDraw != null) {
            HTCoordS zF = fFatherDraw.getScreenCoordinates();
            dF = fzs.getDistance(zF);
        }
        if (fBrotherDraw != null) {
          	HTCoordS zB = fBrotherDraw.getScreenCoordinates();
        	dB = fzs.getDistance(zB);
        }
         
        if ((dF == -1) && (dB == -1)) {
            return -1;
        } else if (dF == -1) {
            return dB;
        } else if (dB == -1) {
            return dF;
        } else { 
            return Math.min(dF, dB);
        }
    }

  /* --- Translation --- */

    /**
     * Special transformation, optimized.
     *
     * @param alpha    first member
     * @param beta     second member
     */
    void specialTrans(HTCoordE alpha, HTCoordE beta) {
        fze.copy(fOldZe);
        fze.specialTrans(alpha, beta);
    }

    void translate(HTCoordE dest) {
        fze.copy(fOldZe);
        fze.translate(dest);
    }

    /**
     * Ends the translation.
     */
    void endTranslation() {
        fOldZe.copy(fze);
    }

    /**
     * Restores the hyperbolic tree to its origin.
     */
    void restore() {
        HTCoordE orig = fNodeModel.getCoordinates();
        fze.fx = orig.fx;
        fze.fy = orig.fy;
        fOldZe.copy(fze);
    }

    /**
     * Sets the fast mode, where nodes are no more drawed.
     *
     * @param mode    setting on or off.
     */
    void fastMode(boolean mode) {
        if (mode != fFastMode) {
            fFastMode = mode;
        }
    }


    /**
     * Sets the long name mode, where full names are drawn.
     *
     * @param mode     setting on or off.
     */
    void longNameMode(boolean mode) {
        if (mode != fLongNameMode) {
            fLongNameMode = mode;
        }
    }

    void setQuadMode(boolean mode) {
    }

    /**
     * Sets the klein mode.
     *
     * @param mode     setting on or off.
     */
    void kleinMode(boolean mode) {
        if (mode != fKleinMode) {
            fKleinMode = mode;
        }
    }

// NEU
    /**
     * Sets the projection type...
     *
     * @param z    the value of the projection
     */
    void changeProjType(double z) {
        if (z != fProjType) {
            fProjType = z;
        }
    }
// FIN NEU

    /**
     * Returns the long name mode.
     *
     * @return    is the long name mode on or off ?
     */
    boolean getLongNameMode() {
        return fLongNameMode;
    }


  /* --- Node searching --- */

    /**
     * Returns the fNodeModel (if any) whose screen coordinates' zone
     * contains thoses given in parameters.
     * 
     * 
     * 
     * 
     * @param fzs    the given screen coordinate
     * @return the searched HTDrawNode if found;
     *              <CODE>null</CODE> otherwise
     */
    HTDrawNode findNode(HTCoordS zs) {
        if (fNodeLabel.contains(zs)) {
            return this;
        } else {
            return null;
        }
    }


  /* --- ToString --- */

    /**
     * Returns a string representation of the object.
     *
     * @return    a String representation of the object
     */
    public String toString() {
        String result = getName() + 
                        "\n\t" + fze + 
                        "\n\t" + fzs; 
        return result;
    }

}

