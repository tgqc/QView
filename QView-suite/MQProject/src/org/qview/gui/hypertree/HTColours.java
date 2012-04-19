/*
 * HTColours.java
 *
 * Created on May 4, 2006, 9:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.gui.hypertree;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author sharon
 */
public class HTColours {
    public static final Color GEODESIC= new Color(200,200,200,255);
    private static final int NODE_ALPHA= 200; //transparency
    public static final Color QM_INNER= new Color(255,255,200,NODE_ALPHA);
    public static final Color CH_INNER= new Color(200,255,200,NODE_ALPHA);
    public static final Color PR_INNER= new Color(255,200,200,NODE_ALPHA);
    public static final Color CL_INNER= new Color(200,255,255,NODE_ALPHA);    
    public static final Color Q_INNER= new Color(200,200,255,NODE_ALPHA);
    public static final Color WARN = Color.ORANGE;
    public static final Color CRIT = Color.RED;
    public static final Color BACKGROUND= Color.WHITE;
    public static final Color NODE_OUTLINE= Color.DARK_GRAY;
    public static final Color NODE_WEAK_OUTLINE= Color.LIGHT_GRAY;
    public static final Color NODE_TEXT= Color.DARK_GRAY;
    public static final Color NODE_SELECTED_OUTLINE= Color.RED;
    
    /** Creates a new instance of HTColours */
    public HTColours() {
    }
    
}
