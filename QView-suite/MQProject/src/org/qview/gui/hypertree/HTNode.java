/*
 * HTNode.java
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

import java.awt.Shape;
import java.util.Enumeration;
import java.awt.Color;


/**
 * The HTNode interface should be implemented by 
 * object that are node of the tree that want to be 
 * displayed in the TreeMap.
 * <P>
 * If you have already a tree structure, just implements
 * this interface in node of the tree.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
public interface HTNode {

    /**
     * Returns the children of this node
     * in an Enumeration.
     * If this object does not have children,
     * it should return an empty Enumeration,
     * not <CODE>null</CODE>.
     * All objects contained in the Enumeration
     * should implements HTNode.
     *
     * @return    an Enumeration containing childs of this node
     */
    public Enumeration children();

    /**
     * Checks if this node is a leaf or not.
     * A node could have no children and still not
     * be a leaf.
     *
     * @return    <CODE>true</CODE> if this node is a leaf;
     *            <CODE>false</CODE> otherwise
     */
    public boolean isLeafNode();
    
    /**
     * Returns the name of this node.
     * Used to display a label in the hyperbolic tree.
     *
     * @return    the name of this node
     */
    public String getName();

    /**
     * Returns the color of the node.
     * Used in the drawing of the node label.
     *
     * @return    the color of the node
     */
    public Color getColor();

    /**
     * Returns the shape of the node.
     * Used in the drawing of the node label.
     *
     * @return    the shape of the node
     */
    public int getShape();
    
    void setSelected(boolean value);
    
    boolean getSelected();
    
    HTModelNode getModelNode();
    
    void setModelNode(HTModelNode value);
}

