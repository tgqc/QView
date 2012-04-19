/*
 * HTDrawNodeComposite.java
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

import java.awt.Graphics;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * The HTDrawNodeComposite class implements the Composite design pattern
 * for HTDrawNode.
 * It represents a HTDrawNode which is not a leaf.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTDrawNodeComposite 
    extends HTDrawNode {

    private static HashMap HTDrawNodeCompositeMap = new HashMap();
    private static HashMap drawnPeerMap = new HashMap();
    //private Hashtable fGeodesicsTable = new Hashtable();

    private HTModelNodeComposite fNodeModel      = null; // encapsulated HTModelNode
    private Vector               fChildrenDraw  = null; // fChildrenDraw of this fNodeModel
    private Vector               fSiblingsDraw  = null; // fSiblingsDraw of this fNodeModel
    private Hashtable            fGeodesics = new Hashtable(); // fGeodesics linking the
                                                   // fChildrenDraw


  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * 
     * 
     * 
     * 
     * @param fatherDraw    the fatherDraw of this fNodeModel
     * @param fNodeModel      the encapsulated HTModelNode
     * @param fModelDraw     the drawing fModelDraw
     */
    HTDrawNodeComposite(HTDrawNodeComposite fatherDraw, 
                        HTModelNodeComposite nodeModel, HTDraw modelDraw) {
        super(fatherDraw, nodeModel, modelDraw);
        HTDrawNodeCompositeMap.put(this.getName(), this);
        this.fNodeModel = nodeModel;
        this.fChildrenDraw = new Vector();
        this.fSiblingsDraw = new Vector();
        //this.fGeodesics = new Hashtable();
        //this.drawnPeerMap = new HashMap();

        HTModelNode childModel = null;
        HTDrawNode childDraw = null;
        HTDrawNode brotherDraw = null;
        boolean first = true;
        boolean second = false;
        for (Enumeration e = nodeModel.children(); e.hasMoreElements(); ) {
            childModel = (HTModelNode) e.nextElement();
            if (childModel.isLeaf()) {
//                childDraw = (HTDrawNode) HTDrawNodeCompositeMap.get(childModel);
//                if (childDraw == null) {
                    childDraw = new HTDrawNode(this, childModel, modelDraw);
//                    HTDrawNodeCompositeMap.put(childModel.getName(), childDraw);
//                }
            } else {
//                childDraw = (HTDrawNodeComposite) HTDrawNodeCompositeMap.get(childModel);
//                if (childDraw == null) {
                    childDraw = new HTDrawNodeComposite(this,
                                          (HTModelNodeComposite) childModel, modelDraw);
//                    HTDrawNodeCompositeMap.put(childModel.getName(), childDraw);
//                }
            }
            addChild(childDraw);

            if (first) {
                brotherDraw = childDraw;
                first = false;
                second = true;
            } else if (second) {
                childDraw.setBrother(brotherDraw);
                brotherDraw.setBrother(childDraw);
                brotherDraw = childDraw;
                second = false;
            } else {
                childDraw.setBrother(brotherDraw);
                brotherDraw = childDraw;
            }  
        }

        if (nodeModel.getParent() == null) {
            mapSiblings();
        }


    }

    void mapSiblings() {
        System.out.println("&!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!&");
        Iterator i = HTDrawNodeCompositeMap.values().iterator();
        while (i.hasNext()) {
            HTDrawNodeComposite nextNode = (HTDrawNodeComposite) i.next();
            HTModelNodeComposite nodeModel = (HTModelNodeComposite) nextNode.getHTModelNode();
            HTModelNode siblingModel = null;
            HTDrawNode siblingDraw = null;
//            HTDrawNode brotherDraw = null;
            
//            boolean first = true;
//            boolean second = false;
            for (Enumeration f = nodeModel.siblings(); f.hasMoreElements(); ) {
                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                siblingModel = (HTModelNode) f.nextElement();
                if (siblingModel != null) {
                    System.out.println("&&&&  siblingModel exists!!!   &&&&");
                    if (siblingModel.isLeaf()) {
                        System.out.println("&&&&  HTDrawNode  &&&&  " + siblingModel.getName());
                        siblingDraw = (HTDrawNode) HTDrawNodeCompositeMap.get(siblingModel.getName());
                        if (siblingDraw == null) {
        //                    siblingDraw = new HTDrawNode(this, siblingModel, modelDraw);
        //                    HTDrawNodeCompositeMap.put(siblingModel, this);
                        }
                    } else {
                        System.out.println("&&&&  HTDrawNode  &&&&  " + siblingModel.getName());
                        siblingDraw = (HTDrawNodeComposite) HTDrawNodeCompositeMap.get(siblingModel.getName());
                        if (siblingDraw == null) {
        //                    siblingDraw = new HTDrawNodeComposite(this,
        //                                          (HTModelNodeComposite) siblingModel, modelDraw);
        //                    HTDrawNodeCompositeMap.put(siblingModel, this);
                        }
                    }
                }
                System.out.println("&&&& siblingDraw &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                System.out.println("&&&&  siblingDraw:  " + siblingDraw.toString() + "  &&&&");
                this.addSibling(siblingDraw);
                nextNode.addSibling(siblingDraw);
                System.out.println("&&&&  fSiblingsDraw:  " + fSiblingsDraw.toString());
    //            ((HTDrawNodeComposite) siblingDraw).removeSibling(this);

//                if (first) {
//                    brotherDraw = siblingDraw;
//                    first = false;
//                    second = true;
//                } else if (second) {
//                    siblingDraw.setBrother(brotherDraw);
//                    brotherDraw.setBrother(siblingDraw);
//                    brotherDraw = siblingDraw;
//                    second = false;
//                } else {
//                    siblingDraw.setBrother(brotherDraw);
//                    brotherDraw = siblingDraw;
//                }
            }
        }
    }

    void addNewChild(HTModelNodeComposite parentModel, HTModelNodeComposite childModel){
        if (this.fNodeModel==parentModel){
            // this is the fNodeModel we need to attach the new childModel to
            HTDrawNodeComposite newChildDraw= new HTDrawNodeComposite(this, childModel, this.fModelDraw);
            addChild(newChildDraw);
            HTModelNode childNode = null;
            HTDrawNode childDraw = null;
            HTDrawNode brotherDraw = null;
            boolean first = true;
            boolean second = false;
            for (Enumeration e = children(); e.hasMoreElements(); ) {
                childDraw = (HTDrawNode) e.nextElement();
                if (first) {
                    brotherDraw = childDraw;
                    first = false;
                    second = true;
                } else if (second) {
                    childDraw.setBrother(brotherDraw);
                    brotherDraw.setBrother(childDraw);
                    brotherDraw = childDraw;
                    second = false;
                } else {
                    childDraw.setBrother(brotherDraw);
                    brotherDraw = childDraw;
                } //else 
            } //for
            
            System.out.println("Added HTDrawNodeComposite "+ childModel.getNode().getName()+" to parent "+ parentModel.getNode().getName());
        } else {
            // try the fChildrenDraw on this fNodeModel
            HTDrawNode childDraw= null;
            for (Enumeration e = children(); e.hasMoreElements(); ) {
                childDraw = (HTDrawNode) e.nextElement();
                childDraw.addNewChild(parentModel, childModel);
            } //for
        } //else
            
    } //addNewChild

  /* --- Children --- */

    /**
     * Returns the fChildrenDraw of this fNodeModel, 
     * in an Enumeration.
     * 
     * 
     * 
     * 
     * 
     * @return the fChildrenDraw of this fNodeModel
     */
    Enumeration children() {
        return fChildrenDraw.elements();
    }
    /**
     * * @return the fSiblingDraw of this fNodeModel
     * */
    Enumeration siblings() {
        return fSiblingsDraw.elements();
    }

    /**
     * 
     * Adds the HTDrawNode as a fChildrenDraw.
     * 
     * 
     * 
     * 
     * @param childDraw    the childDraw
     */
    void addChild(HTDrawNode childDraw) {
        fChildrenDraw.addElement(childDraw);
        fGeodesics.put(childDraw, new HTGeodesic(getCoordinates(), 
                                            childDraw.getCoordinates()));
    }
    /**
     * Create HTDrawNode fSiblingsDraw for node sibling.
     * @param siblingDraw    the siblingDraw
     */
    void addSibling(HTDrawNode siblingDraw) {
        fSiblingsDraw.addElement(siblingDraw);
        fGeodesics.put(siblingDraw, new HTGeodesic(getCoordinates(), siblingDraw.getCoordinates()));
    }
    void removeSibling(HTDrawNode siblingDraw) {
        fSiblingsDraw.removeElement(siblingDraw);
    }


  /* --- Screen Coordinates --- */

    /**
     * Refresh the screen coordinates of this fNodeModel
     * and recurse on fChildrenDraw.
     * 
     * 
     * 
     * 
     * 
     * @param sOrigin   the origin of the screen plane
     * @param sMax      the (xMax, yMax) point in the screen plane
     */ 
    void refreshScreenCoordinates(HTCoordS sOrigin, HTCoordS sMax) {
        super.refreshScreenCoordinates(sOrigin, sMax);
        HTDrawNode childDraw = null;

        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.refreshScreenCoordinates(sOrigin, sMax);
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.refreshScreenCoordinates(sOrigin, sMax);
            }
            
        }

//        HTDrawNode siblingDraw = null;
//        for (Enumeration e = siblings(); e.hasMoreElements(); ) {
//            siblingDraw = (HTDrawNode) e.nextElement();
//            siblingDraw.refreshScreenCoordinates(sOrigin, sMax);
//            HTGeodesic geod = (HTGeodesic) fGeodesics.get(siblingDraw);
//            if (geod != null) {
//                geod.refreshScreenCoordinates(sOrigin, sMax);
//            }
//
//        }
    }


  /* --- Drawing --- */

    /**
     * Draws the branches from this fNodeModel to 
     * its fChildrenDraw.
     * 
     * 
     * 
     * 
     * 
     * @param g    the graphic context
     */
    void drawBranches(Graphics g) {
        HTDrawNode childDraw = null;
        HTDrawNode siblingDraw = null;

        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.draw(g);
            }
            childDraw.drawBranches(g);
            childDraw.drawSiblingBranches(g);
        }
    }
    /**
     * Draws the branches from this fNodeModel to
     * its fSiblingDraw.
     *
     *
     *
     *
     *
     * @param g    the graphic context
     */
    void drawSiblingBranches(Graphics g) {
        HTDrawNode siblingDraw = null;
        System.out.println("#######################  " + this.getName());
        System.out.println("####  " + fSiblingsDraw.toString());
// this could be recursive - peer to peer
        for (Enumeration e = siblings(); e.hasMoreElements(); ) {
            siblingDraw = (HTDrawNode) e.nextElement();
//            ((HTDrawNodeComposite) siblingDraw).removeSibling(this);
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(siblingDraw);
                       
            System.out.println("###################################################");
            System.out.println("####  " + geod.toString());
//            if (!drawnPeerMap.containsKey(this.getName() + siblingDraw.getName()) && !drawnPeerMap.containsKey(this.getName() + siblingDraw.getName() + this.getName())) {
            if (geod != null) {
                geod.draw(g, 0);
//                drawnPeerMap.put(this.getName() + siblingDraw.getName(),null);
//                drawnPeerMap.put(siblingDraw.getName() + this.getName(),null);
//                ((HTDrawNodeComposite) siblingDraw).removeSibling(this);
//                removeSibling(siblingDraw);
            }
//            }
            siblingDraw.drawSiblingBranches(g);
        }

    }

    /**
     * Draws this fNodeModel.
     * 
     * 
     * 
     * @param g    the graphic context
     */
    void drawNodes(Graphics g) {
        if (fFastMode == false) {
            super.drawNodes(g);
        
            HTDrawNode childDraw = null;
            for (Enumeration e = children(); e.hasMoreElements(); ) {
                childDraw = (HTDrawNode) e.nextElement();
                childDraw.drawNodes(g);
            }
        }
    }

    /**
     * Returns the minimal distance between this fNodeModel
     * and his father, his brother, and his fChildrenDraw.
     * 
     * 
     * 
     * 
     * 
     * @return the minimal distance
     */
    int getSpace() {
        int space = super.getSpace();
        
        if (! fChildrenDraw.isEmpty()) {
            HTDrawNode childDraw = (HTDrawNode) fChildrenDraw.firstElement();
            HTCoordS zC = childDraw.getScreenCoordinates();
            int dC = fzs.getDistance(zC);

            if (space == -1) {
                return dC;
            } else {
                return Math.min(space, dC);
            }
        } else {
            return space;
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
        super.specialTrans(alpha, beta);

        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.specialTrans(alpha, beta);
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.rebuild();
            }
        }

    }

    void translate(HTCoordE dest) {
        super.translate(dest);

        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.translate(dest);
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.rebuild();
            }
        }

    }
    
    /**
     * Ends the translation.
     */
    void endTranslation() {
        super.endTranslation();

        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.endTranslation();
        }
    }

    /**
     * Restores the hyperbolic tree to its origin.
     */
    void restore() {
        super.restore();

        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.restore();
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.rebuild();
            }
        }

    }

    /**
     * Sets the fast mode, where nodes are no more drawed.
     *
     * @param mode    setting on or off.
     */
    void fastMode(boolean mode) {
        super.fastMode(mode);
        
        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.fastMode(mode);
        }
    }

    /**
     * Sets the long name mode, where full name are drawn.
     *
     * @param mode    setting on or off.
     */
    void longNameMode(boolean mode) {
        super.longNameMode(mode);
        
        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.longNameMode(mode);
        }
    }

    void setQuadMode(boolean mode) {
        super.setQuadMode(mode);
        
        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.setQuadMode(mode);
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.setQuadMode(mode);
            }
        }

    }

    /**
     * Sets the klein mode.
     *
     * @param mode    setting on or off.
     */
    void kleinMode(boolean mode) {
        super.kleinMode(mode);
        
        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.kleinMode(mode);
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.kleinMode(mode);
                geod.rebuild();
            }
        }

    }

// NEU
    /**
     * Sets the projection type.
     *
     * @param z    the value of the projection
     */
    void changeProjType(double z) {
        super.changeProjType(z);

        HTDrawNode childDraw = null;
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            childDraw.changeProjType(z);
            HTGeodesic geod = (HTGeodesic) fGeodesics.get(childDraw);
            if (geod != null) {
                geod.changeProjType(z);
                geod.rebuild();
            }
        }

    }
// FIN NEU

    

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
        HTDrawNode resultDraw = super.findNode(zs);
        if (resultDraw != null) {
            return resultDraw;
        } else {
            HTDrawNode childDraw = null;
            for (Enumeration e = children(); e.hasMoreElements(); ) {
                 childDraw = (HTDrawNode) e.nextElement();
                 resultDraw = childDraw.findNode(zs);
                 if (resultDraw != null) {
                     return resultDraw;
                 } //if
            } //for
            return null;
        } //else
    }


  /* --- ToString --- */

    /**
     * Returns a string representation of the object.
     *
     * @return    a String representation of the object
     */
    public String toString() {
        String resultString = super.toString();
        HTDrawNode childDraw = null;
        resultString += "\n\tChildren :";
        for (Enumeration e = children(); e.hasMoreElements(); ) {
            childDraw = (HTDrawNode) e.nextElement();
            resultString += "\n\t-> " + childDraw.getName();
        }
        return resultString;
    }

}

