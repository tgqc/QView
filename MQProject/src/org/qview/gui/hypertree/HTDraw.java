/*
 * HTDraw.java
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

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Insets;
//PDA
import javax.swing.SwingUtilities;

//PDA


/**
 * The HTDraw class implements the drawing fHTModel for the HTView.
 * 
 * 
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTDraw {

    private final static int NBR_FRAMES = 5; // number of intermediates 
                                             // animation frames

    protected HTModel    fHTModel    = null;  // the tree fHTModel
    private HTView     fView     = null;  // the fView using this drawing fHTModel
    private HTDrawNode fDrawRoot = null;  // the root of the drawing tree 

    private HTCoordS   fsOrigin  = null;  // origin of the screen plane
    private HTCoordS   fsMax     = null;  // max point in the screen plane 

    private int        fBorder   = 5;
      
    private double[]   fRay      = null;
    
    private boolean    fFastMode = false; // fast mode
    private boolean    fLongNameMode = false; // long name mode
    private boolean    fTransNotCorrected = false;
    private boolean    fKleinMode = false; // klein mode
// NEU
    private double     fProjType = -2.0; // projection type 
// FIN NEU
    
    public static boolean fIsTranslating = false;


  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * 
     * @param fHTModel    the tree fHTModel to draw
     * @param fView     the fView using this drawing fHTModel
     */
    HTDraw(HTModel htModel, HTView view) {
        this.fView = view;
        this.fHTModel = htModel;
        HTModelNode rootModel = htModel.getRoot();
        fsOrigin = new HTCoordS();
        fsMax = new HTCoordS();

        fRay = new double[4];
        fRay[0] = htModel.getLength();

        for (int i = 1; i < fRay.length; i++) {
            fRay[i] = (fRay[0] + fRay[i - 1]) / (1 + (fRay[0] * fRay[i - 1]));
        }

        if (rootModel.isLeaf()) {
            fDrawRoot = new HTDrawNode(null, rootModel, this);
        } else {
            fDrawRoot = new HTDrawNodeComposite(null, 
                                            (HTModelNodeComposite) rootModel, this);
        }
    }


   /* --- fBorder --- */

    /**
     * Returns the fBorder.
     * 
     * 
     * @return the fBorder
     */
    int getBorder() {
        return fBorder;
    }
    
    void addNewChild(HTModelNodeComposite parentModel, HTModelNodeComposite childModel){
        fDrawRoot.addNewChild(parentModel, childModel);
    } //addNewChild

  /* --- Screen coordinates --- */

    /**
     * Refresh the screen coordinates of the drawing tree.
     */
    void refreshScreenCoordinates() {
        Insets insets = fView.getInsets();
        //PDA
        fsMax.fx = (fView.getWidth() - insets.left - insets.right) / 2 - fBorder;
        fsMax.fy = (fView.getHeight() - insets.top - insets.bottom) / 2 - fBorder;
        //fsMax.fx = (fView.getSize().width - insets.left - insets.right) / 2;
        //fsMax.fy = (fView.getSize().height - insets.top - insets.bottom) / 2;
        //PDA
        fsOrigin.fx = fsMax.fx + insets.left + fBorder;
        fsOrigin.fy = fsMax.fy + insets.top + fBorder;
        fDrawRoot.refreshScreenCoordinates(fsOrigin, fsMax);
    }

    /**
     * Returns the origin of the screen plane.
     * WARNING : this is not a copy but the original object.
     *
     * @return    the origin of the screen plane
     */
    HTCoordS getSOrigin() {
        return fsOrigin;
    }

    /**
     * Return the point representing the up right corner
     * of the screen plane, thus giving fx and fy maxima.
     * WARNING : this is not a copy but the original object.
     * 
     * 
     * 
     * @return the max point
     */
    HTCoordS getSMax() {
        return fsMax;
    }


  /* --- Drawing --- */

    /**
     * Draws the branches of the hyperbolic tree.
     *
     * @param g    the graphic context
     */
    void drawBranches(Graphics g) {
        fDrawRoot.drawBranches(g);
    }

    /**
     * Draws the nodes of the hyperbolic tree.
     *
     * @param g    the graphic context
     */
    void drawNodes(Graphics g) {
        fDrawRoot.drawNodes(g);
    }


  /* --- Translation --- */

    /**
     * Translates the hyperbolic tree by the given vector.
     *
     * @param t    the translation vector
     */
    void translate(HTCoordE zs, HTCoordE ze) {
        fIsTranslating = true;

        HTCoordE zst = null;
        HTCoordE zet = null;
        
        if (fKleinMode) {
            zst = zs.kToP();
            zet = ze.kToP();
        } else if (fProjType != 0.0) {
            zst = zs.zToP(fProjType);
            zet = ze.zToP(fProjType);
        } else {
            zst = new HTCoordE(zs);
            zet = new HTCoordE(ze);
        }
        
        if (fTransNotCorrected) {
            HTCoordE v = new HTCoordE();
            double de = zet.d2();
            double ds = zst.d2();
            double dd = 1.0 - de * ds;
            v.fx = (zet.fx * ( 1.0 - ds) - zst.fx * (1.0 - de)) / dd;
            v.fy = (zet.fy * ( 1.0 - ds) - zst.fy * (1.0 - de)) / dd;
            if (v.isValid()) {
                 fDrawRoot.translate(v);
            }
        } else {    
            HTCoordE zo = new HTCoordE(fDrawRoot.getOldCoordinates());
            zo.fx = - zo.fx;
            zo.fy = - zo.fy;
            HTCoordE zs2 = new HTCoordE(zst);
        
            zs2.translate(zo);

            HTCoordE t = new HTCoordE();
            double de = zet.d2();
            double ds = zs2.d2();
            double dd = 1.0 - de * ds;
            t.fx = (zet.fx * ( 1.0 - ds) - zs2.fx * (1.0 - de)) / dd;
            t.fy = (zet.fy * ( 1.0 - ds) - zs2.fy * (1.0 - de)) / dd;
        
            if (t.isValid()) {

                // alpha = 1 + conj(zo)*t
                HTCoordE alpha = new HTCoordE();
                alpha.fx = 1 + (zo.fx * t.fx) + (zo.fy * t.fy);
                alpha.fy = (zo.fx * t.fy) - (zo.fy * t.fx);
                // beta = zo + t
                HTCoordE beta = new HTCoordE();
                beta.fx = zo.fx + t.fx;
                beta.fy = zo.fy + t.fy;
           
                fDrawRoot.specialTrans(alpha, beta);
             }
        }
        fView.repaint();
    }

    void translateCorrected(HTCoordE zs, HTCoordE ze) {
        fIsTranslating = true;

        HTCoordE zo = new HTCoordE(fDrawRoot.getOldCoordinates());
        zo.fx = - zo.fx;
        zo.fy = - zo.fy;
        HTCoordE zs2 = new HTCoordE(zs);
        
        zs2.translate(zo);

        HTCoordE t = new HTCoordE();
        double de = ze.d2();
        double ds = zs2.d2();
        double dd = 1.0 - de * ds;
        t.fx = (ze.fx * ( 1.0 - ds) - zs2.fx * (1.0 - de)) / dd;
        t.fy = (ze.fy * ( 1.0 - ds) - zs2.fy * (1.0 - de)) / dd;
        
        if (t.isValid()) {

            // alpha = 1 + conj(zo)*t
            HTCoordE alpha = new HTCoordE();
            alpha.fx = 1 + (zo.fx * t.fx) + (zo.fy * t.fy);
            alpha.fy = (zo.fx * t.fy) - (zo.fy * t.fx);
            // beta = zo + t
            HTCoordE beta = new HTCoordE();
            beta.fx = zo.fx + t.fx;
            beta.fy = zo.fy + t.fy;
           
            fDrawRoot.specialTrans(alpha, beta);
            fView.repaint();
        }
    }

    /**
     * Signal that the translation ended.
     */
    void endTranslation() {
        fIsTranslating = false;
        fDrawRoot.endTranslation();
        fView.repaint();
    }

    /**
     * Translate the hyperbolic tree so that the given nodeDraw 
     * is put at the origin of the hyperbolic tree.
     * 
     * 
     * @param nodeDraw    the given HTDrawNode
     */
    public void translateToOrigin(HTDrawNode nodeDraw) {
        fView.stopMouseListening();
        fIsTranslating = true;
        AnimThread t = new AnimThread(nodeDraw);
        t.start();
    }
    
    /**
     * Restores the hyperbolic tree to its origin.
     */
    void restore() {
        fDrawRoot.restore();
        fView.repaint();
    }

    /**
     * Sets the fast mode, where nodes are no more drawed.
     *
     * @param mode    setting on or off.
     */
    void fastMode(boolean mode) {
        if (mode != fFastMode) {
            fFastMode = mode;
            fDrawRoot.fastMode(mode);
            if (mode == false) {
                fView.repaint();
            }
        }
    }
    
    boolean getFastMode() {
        return fFastMode;
    }
    
    /**
     * Sets the long name mode, where full names are drawn.
     *
     * @param mode    setting on or off.
     */
    void longNameMode(boolean mode) {
        if (mode != fLongNameMode) {
            fLongNameMode = mode;
            fDrawRoot.longNameMode(mode);
            fView.repaint();
        }
    }

    boolean getLongNameMode() {
        return fLongNameMode;
    }

    void transNotCorrectedMode(boolean mode) {
        fTransNotCorrected = mode;
    }

    boolean getTransNotCorrectedMode() {
        return fTransNotCorrected;
    }

    void setQuadMode(boolean mode) {
        fDrawRoot.setQuadMode(mode);
    }
    
    /**
     * Sets the klein mode.
     *
     * @param mode    setting on or off.
     */
    void kleinMode(boolean mode) {
        if (mode != fKleinMode) {
            HTCoordE zo = new HTCoordE(fDrawRoot.getCoordinates());
            
            fKleinMode = mode;
            if (fKleinMode) {
                fHTModel.setLengthKlein();
            } else {
                fHTModel.setLengthPoincare();
            }
            fHTModel.layoutHyperbolicTree();
            fDrawRoot.kleinMode(mode);
            
            restore();
            if (! fKleinMode) { // poincare
                translateCorrected(new HTCoordE(), zo.pToK());
            } else {           // klein
                translateCorrected(new HTCoordE(), zo.kToP());
            }
            endTranslation();
        }
    }

// NEU
    /**
     * Change projection type.
     *
     * @param z    the new value of proj type
     */
    void changeProjType(double z) {
        if (z != fProjType) {
            HTCoordE zo = new HTCoordE(fDrawRoot.getCoordinates());
            
            double oldProjType = fProjType;
            fProjType = z;
            //fHTModel.layoutHyperbolicTree();
            fDrawRoot.changeProjType(z);
            
            
            restore();
            translateCorrected(new HTCoordE(), zo.pToZ(oldProjType).zToP(z));
            endTranslation();
        }
        
    }

    double getProjType() {
        return fProjType;
    }
// FIN NEU    
    

  /* --- Node searching --- */

    /**
     * Returns the node (if any) whose screen coordinates' zone
     * contains thoses given in parameters.
     *
     * @param zs    the given screen coordinate
     * @return      the searched HTDrawNode if found;
     *              <CODE>null</CODE> otherwise
     */
    HTDrawNode findNode(HTCoordS zs) {
        return fDrawRoot.findNode(zs);
    }


  /* --- Inner animation thread --- */

    /**
     * The AnimThread class implements the thread that do the animation
     * when clicking on a node.
     */  
    class AnimThread
        extends Thread {

        private HTDrawNode node  = null; // node to put at the origin
        private Runnable   tTask = null; // translation task

        /**
         * Constructor.
         *
         * @param node    the node to put at the origin
         */
        AnimThread(HTDrawNode node) {
            this.node = node;
        }

        /**
         * Do the animation.
         */
        public void run() {
            HTCoordE zn = node.getOldCoordinates();
            HTCoordE zf = new HTCoordE();

            int frames = NBR_FRAMES;
            int nodes = fHTModel.getNumberOfNodes();
            
            double d = zn.d();
            for (int i = 0; i < fRay.length; i++) {
            	if (d > fRay[i]) {
                	frames += NBR_FRAMES / 2;
            	}
            }
                        
            double factorX = zn.fx / frames;
            double factorY = zn.fy / frames;
            
            for (int i = 1; i < frames; i++) {
                zf.fx = zn.fx - (i * factorX);
                zf.fy = zn.fy - (i * factorY);
                tTask = new TranslateThread(zn, zf);
                //PDA
                try {
                    SwingUtilities.invokeAndWait(tTask);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //tTask.run();
                //PDA
            }
       
            zf.fx = 0.0;
            zf.fy = 0.0;
            tTask = new LastTranslateThread(zn, zf);
            //PDA
            try {
                SwingUtilities.invokeAndWait(tTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //tTask.run();
            //PDA
            fIsTranslating = false;
        }
        
      /* --- Inner's inner --- */
      
        class TranslateThread
            implements Runnable {
            
            HTCoordE zStart = null; 
            HTCoordE zEnd   = null;
            
            TranslateThread(HTCoordE z1, HTCoordE z2) {
                zStart = z1;
                zEnd = z2;
            }
            
            public void run() {
                translate(zStart, zEnd);
			    fView.repaint();
            }        
        }
        
        class LastTranslateThread
            implements Runnable {
            
            HTCoordE zStart = null; 
            HTCoordE zEnd   = null;
            
            LastTranslateThread(HTCoordE z1, HTCoordE z2) {
                zStart = z1;
                zEnd = z2;
            }
            
            public void run() {
                translate(zStart, zEnd);
                endTranslation();
			    fView.repaint();
			    fView.startMouseListening();
            }        
        }

              
    }

}

