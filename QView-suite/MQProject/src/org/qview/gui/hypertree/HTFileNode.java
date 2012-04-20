/*
 * HTFileNode.java
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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.Color;
import java.util.Date;

/**
 * The HTFileNode implements an example of HTNode encapsulating a File.
 * <P>
 * The color legend is :
 * <UL>
 *   <IL> white  for files less than a hour old
 *   <IL> green  for files less than a day old
 *   <IL> yellow for files less than a week old
 *   <IL> orange for files less than a month old
 *   <IL> red    for files less than a year old
 *   <IL> blue   for files more than a year old
 * </UL>
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
public class HTFileNode
    implements HTNode {

    private File      fFile     = null; // the File encapsulated
    private Hashtable fChildrenNode = null; // the fChildrenNode of this node
    private int fShape          = HTree.SHAPE_DEFAULT;
    private boolean fSelected= false;

    private HTModelNode fHTModelNode;

  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * @param fFile    the File encapsulated in this node
     */
    public HTFileNode(File file) {
        this.fFile = file;
        fChildrenNode = new Hashtable();

        if (! isLeafNode()) {
            String[] fileList = file.list();
            for (int i = 0; i < fileList.length; i++) {
                File tempFile = new File(file.getPath() +
                                        File.separator + fileList[i]);
                HTFileNode child = new HTFileNode(tempFile);
                addChild(child);
            } //for
        } //if
        if (file.isDirectory()){
            fShape= HTree.SHAPE_ROUND_RECT;
        } else {
            fShape= HTree.SHAPE_METRIC_CIRC;
        } //lese
    } //HTFileNode


  /* --- Tree management --- */

    /**
     * Add childNode to the node.
     * 
     * 
     * @param childNode    the HTFileNode to add as a childNode
     */
    protected void addChild(HTFileNode childNode) {
        fChildrenNode.put(childNode.getName(), childNode);
    }


  /* --- HTNode --- */

    /**
     * Returns the fChildrenNode of this node in an Enumeration.
     * If this node is a fFile, return a empty Enumeration.
     * Else, return an Enumeration full with HTFileNode.
     * 
     * 
     * 
     * @return an Enumeration containing childs of this node
     */
    public Enumeration children() {
        return fChildrenNode.elements();
    }

    /**
     * Returns true if this node is not a directory.
     *
     * @return    <CODE>false</CODE> if this node is a directory;
     *            <CODE>true</CODE> otherwise
     */
    public boolean isLeafNode() {
        return (! fFile.isDirectory());
    }
    
    /**
     * Returns the name of the fFile.
     * 
     * 
     * @return the name of the fFile
     */
    public String getName() {
        return fFile.getName();
    }

    /**
     * Returns the color of the fFile.
     * 
     * 
     * @return the color of the fFile
     */
    public Color getColor() {
        if (fFile.getName().startsWith("QMGR"))
            return HTColours.QM_INNER;
        else if (fFile.getName().startsWith("Chan"))
            return HTColours.CH_INNER;
        else if (fFile.getName().startsWith("Que"))
            return HTColours.Q_INNER;
        else if (fFile.getName().startsWith("Pro"))
            return HTColours.PR_INNER;
        else
            return HTColours.CL_INNER;
    } //getColor
    
    /**
     * Returns the fShape of the fFile.
     * 
     * 
     * 
     * @return the name of the fFile
     */
    public int getShape() {
        return fShape;
    } //getShape
    
    public void setSelected(boolean value) {
        fSelected= value;
    } //setSelected

    public boolean getSelected() {
        return fSelected;
    } //getSelected

    public HTModelNode getModelNode() {
        return fHTModelNode;
    }

    public void setModelNode(HTModelNode value) {
        fHTModelNode= value;
    }

}

