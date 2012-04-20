/*
 * HTView.java
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

import java.awt.*;
import java.awt.event.MouseEvent;
//PDA
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import javax.swing.*;
//PDA


/**
 * The HTView class implements a view of the HyperTree.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
public class HTView extends JPanel implements Printable {
    //extends Panel {
    //PDA

    private HTModel    fHTModel  = null; // the tree fHTModel represented
    private HTDraw     fHTDraw   = null; // the drawing fHTModel
    private HTAction   fAction = null; // fAction manager
    private boolean    fFastMode = false;
    private boolean    fLongNameMode = false;
    private boolean    fCircleMode = false;
    private boolean    fTransNotCorrected = false;
    private boolean    fQuadMode = true;


  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * 
     * @param fHTModel    the tree fHTModel to view
     */
    public HTView(HTModel htModel, HTree hyperTree) {
        super(new BorderLayout());
        //PDA
        setPreferredSize(new Dimension(250, 250));
        //setSize(new Dimension(250, 250));
        //PDA
        setBackground(HTColours.BACKGROUND);
        
        this.fHTModel = htModel; 
        fHTDraw = new HTDraw(htModel, this);        
        fAction = new HTAction(fHTDraw, this, hyperTree);
        startMouseListening();
        //PDA
        ToolTipManager.sharedInstance().registerComponent(this);
        //PDA
    }
    
    void addNewChild(HTModelNodeComposite parentModel, HTModelNodeComposite childModel){
        fHTDraw.addNewChild(parentModel,childModel);
    } //addNewChild


  /* --- Node finding --- */

    /**
     * Returns the nodeDraw containing the mouse event.
     * <P>
     * This will be a HTNode.
     * 
     * 
     * @param event    the mouse event on a nodeDraw
     * @return the nodeDraw containing this event;
     *                 could be <CODE>null</CODE> if no nodeDraw was found
     */
    public Object getNodeUnderTheMouse(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        
        HTDrawNode nodeDraw = fHTDraw.findNode(new HTCoordS(x, y));
        if (nodeDraw != null) {
            return nodeDraw.getHTModelNode().getNode();
        } else {
            return null;
        }
    }
    
// NEU
  /* --- Change projection type --- */

    /**
     * Changes the value of the projection type.
     * Should be inferior or equal to 1.
     *
     * @param z    the value
     */
    public void setProjType(double z) {
        fHTDraw.changeProjType(z);
    }

    public double getProjType() {
        return fHTDraw.getProjType();
    }
// FIN NEU

// CONF
    public void setFastMode(boolean mode) {
        fFastMode = mode;
        fHTDraw.fastMode(mode);    
    }

    public boolean getFastMode() {
        return fFastMode;
    }

    public void setLongNameMode(boolean mode) {
        fLongNameMode = mode;
        fHTDraw.longNameMode(mode);
    }

    public boolean getLongNameMode() {
        return fLongNameMode;
    }

    public void setCircleMode(boolean mode) {
        fCircleMode = mode;
    }

    public boolean getCircleMode() {
        return fCircleMode;
    }

    public void setTranslationNotCorrected(boolean mode) {
        fTransNotCorrected = mode;
        fHTDraw.transNotCorrectedMode(mode); 
    }

    public boolean getTranslationNotCorrected() {
        return fTransNotCorrected;
    }

    public void restore() {
        fHTDraw.restore();
    }

    public void restorePoincare() {
        setProjType(0.0);
    }

    public void setQuadMode(boolean mode) {
        if (fQuadMode != mode) {
            fQuadMode = mode;
            fHTDraw.setQuadMode(mode);
            repaint();
        }
    }

    public boolean getQuadMode() {
        return fQuadMode;
    }
// FIN CONF

  /* --- Tooltip --- */

    /**
     * Returns the tooltip to be displayed.
     *
     * @param event    the event triggering the tooltip
     * @return         the String to be displayed
     */
    public String getToolTipText(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        
        HTDrawNode nodeDraw = fHTDraw.findNode(new HTCoordS(x, y));
        if (nodeDraw != null) {
            return nodeDraw.getName();
        } else {
            return null;
        }
    }

  /* --- Paint --- */

    /**
     * Paint the component.
     *
     * @param g    the graphic context
     */
    //PDA
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    //public void paint(Graphics g) {
    //   super.paint(g);
    //PDA
        if (fCircleMode) {
            int border = fHTDraw.getBorder();
            Insets i = getInsets();
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setRenderingHint(
                                            RenderingHints.KEY_ANTIALIASING,
                                            RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.drawOval(i.left + border, 
                       i.top + border, 
                       getWidth() - i.left - i.right - 2*border,
                       getHeight() - i.top - i.bottom - 2*border);
        }
        fHTDraw.refreshScreenCoordinates();
        fHTDraw.drawBranches(g);
        fHTDraw.drawNodes(g);
    }


  /* --- Thread-safe locking --- */
  
    /**
     * Stops the listening of mouse events.
     */
    void stopMouseListening() {
        this.removeMouseListener(fAction);
        this.removeMouseMotionListener(fAction);
    }
    
    /**
     * Starts the listening of mouse events.
     */
    void startMouseListening() {
        this.addMouseListener(fAction);
        this.addMouseMotionListener(fAction);
    }
    
    public HTDraw getHTDraw(){
        return fHTDraw;
    }
    
    public void changeKleinMode(boolean newMode) {
        fAction.switchLongNameMode(newMode);        
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        this.paint(g2d);
        return (PAGE_EXISTS);
    }

}

