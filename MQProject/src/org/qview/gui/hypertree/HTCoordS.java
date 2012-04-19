/*
 * HTCoordS.java
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
 * The HTCoordS class implements the coordinates of a point
 * in the Screen space.
 * As the screen space is represented with finite pixels,
 * we just use int instead of float or double.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTCoordS {

    private static final int ZONE_LENGTH = 4; // size of the zone

    int fx = 0; // fx coord
    int fy = 0; // fy coord


  /* --- Constructor --- */

    /**
     * Constructor.
     * fx = 0 and fy = 0.
     */
    HTCoordS() {}

    /**
     * Constructor copying the given screen point.
     *
     * @param z    the screen point to copy 
     */
    HTCoordS(HTCoordS z) {
        this.fx = z.fx;
        this.fy = z.fy;
    }

    /**
     * Constructor fixing fx and fy.
     * 
     * 
     * 
     * @param fx    the fx coord
     * @param fy    the fy coord
     */
    HTCoordS(int x, int y) {
        this.fx = x;
        this.fy = y;
    }


  /* --- Projection --- */

    /**
     * Projects the given Euclidian point on the screen plane.
     * 
     * @param ze        the euclidian point
     * @param sOrigin   the origin of the screen plane
     * @param sMax      the (xMax, yMax) point in the screen plane
     */
    void projectionEtoS(HTCoordE ze, HTCoordS sOrigin, HTCoordS sMax) {
        fx = (int) Math.round(ze.fx * sMax.fx) + sOrigin.fx;
        fy = - (int) Math.round(ze.fy * sMax.fy) + sOrigin.fy;
    }


  /* --- Zone containing --- */

    /**
     * Is the given HTCoordS within the zone length 
     * of this HTCoordS ?
     *
     * @return    <CODE>true</CODE> if it is,
     *            <CODE>false</CODE> otherwise
     */
    boolean contains(HTCoordS zs) {
        int length = getDistance(zs);
        if (length <= ZONE_LENGTH) {
            return true;
        } else {
            return false;
        }
    }


  /* -- Distance --- */
  
    /**
     * Returns the distance between this point
     * and the given point.
     *
     * @param z    the given point
     * @return     the distance
     */
    int getDistance(HTCoordS z) {
        int d2 = (z.fx - fx) * (z.fx - fx) + (z.fy - fy) * (z.fy - fy);
        return (int) Math.round(Math.sqrt(d2));
    }


  /* --- ToString --- */

    /**
     * Returns a string representation of the object.
     *
     * @return    a String representation of the object
     */
    public String toString() {
        return "(" + fx + " : " + fy + ")S";
    }

}

