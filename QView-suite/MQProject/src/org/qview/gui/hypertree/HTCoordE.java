/*
 * HTCoordE.java
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
 * The HTCoordE class implements the coordinates of a point
 * in the Euclidian space.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTCoordE {

    private static final double EPSILON = 1.0E-10; // epsilon

    double fx = 0.0; // fx coord
    double fy = 0.0; // fy coord


  /* --- Constructor --- */

    /**
     * Constructor.
     * fx = 0 and fy = 0.
     */
    HTCoordE() {}

    /**
     * Constructor copying the given euclidian point.
     *
     * @param z    the euclidian point to copy 
     */
    HTCoordE(HTCoordE z) {
        this.copy(z);
    }

    /**
     * Constructor fixing fx and fy.
     * 
     * 
     * 
     * @param fx    the fx coord
     * @param fy    the fy coord
     */
    HTCoordE(double x, double y) {
        this.fx = x;
        this.fy = y;
    }


  /* --- Copy --- */

    /**
     * Copy the given HTCoordE into this HTCoordE.
     *
     * @param z    the HTCoordE to copy
     */
    void copy(HTCoordE z) {
        this.fx = z.fx;
        this.fy = z.fy;
    }


  /* --- Projections --- */

    /**
     * Progects from Screen to Euclidian.
     * 
     * 
     * 
     * @param fx        the fx screen coordinate
     * @param fy        the fy screen coordinate
     * @param sOrigin   the origin of the screen plane
     * @param sMax      the (xMax, yMax) point in the screen plane
     */
    void projectionStoE(int x, int y, HTCoordS sOrigin, HTCoordS sMax) {
        this.fx = (double) (x - sOrigin.fx) / (double) sMax.fx;
        this.fy = -((double) (y - sOrigin.fy) / (double) sMax.fy);
    } 


  /* --- Validation --- */

    /**
     * Is this coordinate in the hyperbolic disc ?
     *
     * @return    <CODE>true</CODE> if this point is in;
     *            <CODE>false</CODE> otherwise
     */
    boolean isValid() {
        return (this.d2() < 1.0);
    }
 

  /* --- Transformation --- */

    /*
     * Some complex computing formula :
     *
     * arg(z)  = atan(fy / fx) if fx > 0
     *         = atan(fy / fx) + Pi if fx < 0
     *
     * d(z)    = Math.sqrt((z.fx * z.fx) + (z.fy * z.fy)) 
     *
     * conj(z) = | z.fx
     *           | - z.fy
     *
     * a * b   = | (a.fx * b.fx) - (a.fy * b.fy)
     *           | (a.fx * b.fy) + (a.fy * b.fx)
     *
     * a / b   = | ((a.fx * b.fx) + (a.fy * b.fy)) / d(b)
     *           | ((a.fy * b.fx) - (a.fx * b.fy)) / d(b)
     */
     
    /**
     * Conjugate the complex.
     */
    void conjugate() {
        fy = -fy;
    }

    /**
     * Multiply this coordinate by the given coordinate.
     *
     * @param z    the coord to multiply with
     */
    void multiply(HTCoordE z) {
        double tx = fx;
        double ty = fy;
        fx = (tx * z.fx) - (ty * z.fy);
        fy = (tx * z.fy) + (ty * z.fx);
    }
    
    /**
     * Divide this coordinate by the given coordinate.
     *
     * @param z    the coord to divide with
     */
    void divide(HTCoordE z) {
        double d = z.d2();
        double tx = fx;
        double ty = fy;
        fx = ((tx * z.fx) + (ty * z.fy)) / d;
        fy = ((ty * z.fx) - (tx * z.fy)) / d;
    }

    /**
     * Substracts the second coord to the first one
     * and put the result in this HTCoorE
     * (this = a - b).
     *
     * @param a    the first coord
     * @param b    the second coord
     */
    void sub(HTCoordE a, HTCoordE b) {
        fx = a.fx - b.fx;
        fy = a.fy - b.fy;
    }

    /**
     * Returns the angle between the fx axis and the line
     * passing throught the origin O and this point.
     * The angle is given in radians.
     * 
     * 
     * @return the angle, in radians
     */
    double arg() {
        double a = Math.atan(fy / fx);
        if (fx < 0) {
            a += Math.PI;
        } else if (fy < 0) {
            a += 2 * Math.PI;
        }
        return a;
    }

    /**
     * Returns the square of the distance from the origin 
     * to this point.
     *
     * @return    the square of the distance
     */
    double d2() {
        double d2 = (fx * fx) + (fy * fy);
        return d2;
    }

    /**
     * Returns the distance from the origin 
     * to this point.
     *
     * @return    the distance
     */
    double d() {
        return Math.sqrt(d2());
    }

    /**
     * Returns the distance from this point
     * to the point given in parameter.
     *
     * @param p    the other point
     * @return     the distance between the 2 points
     */
    double d(HTCoordE p) {
        return Math.sqrt((p.fx - fx) * (p.fx - fx) + (p.fy - fy) * (p.fy - fy));
    }

    /**
     * Translate this Euclidian point 
     * by the coordinates of the given Euclidian point.
     * 
     * @param t    the translation coordinates
     */
    void translate(HTCoordE t) {
        // z = (z + t) / (1 + z * conj(t))
        
        // first the denominator
        double denX = (fx * t.fx) + (fy * t.fy) + 1.0;
        double denY = (fy * t.fx) - (fx * t.fy) ;    

        // and the numerator
        double numX = fx + t.fx;
        double numY = fy + t.fy;

        // then the division (bell)
        double dd = (denX * denX) + (denY * denY);
        fx = ((numX * denX) + (numY * denY)) / dd;
        fy = ((numY * denX) - (numX * denY)) / dd;
    }

    HTCoordE pToK() {
        HTCoordE k = new HTCoordE();
        double d = 2.0 / (1.0 + d2());
        k.fx = (d * fx);
        k.fy = (d * fy);
        return k;
    }

    HTCoordE kToP() {
        HTCoordE k = new HTCoordE();
        double d = 1.0 / (1.0 + Math.sqrt(1 - d2()));
        k.fx = (d * fx);
        k.fy = (d * fy);
        return k;
    }

// NEU
    HTCoordE pToZ(double z) {
        HTCoordE k = new HTCoordE();
        double d = 2.0 / (2.0 - z * (1.0 - d2()));
        k.fx = (d * fx);
        k.fy = (d * fy);
        return k;
    }


    HTCoordE zToP(double z) {
        HTCoordE k = new HTCoordE();
        double s = (d2() - z + Math.sqrt((z*z*d2()) - (2*z*d2()) + 1)) / 
                   (1 - d2());
        double y1 = fx * (1 + s);
        double y2 = fy * (1 + s);
        double y3 = z + s;
        k.fx = y1 / (1+y3);
        k.fy = y2 / (1+y3);
        return k;
    }
// FIN NEU
    
    /**
     * Special transformation, optimized.
     * 
     * @param alpha    first member
     * @param beta     second member
     */
    void specialTrans(HTCoordE alpha, HTCoordE beta) {
        // z = (alpha * z + beta) / (conj(alpha) + conj(beta)*z)
 
        double dx = (this.fx * beta.fx) + (this.fy * beta.fy) + alpha.fx;
        double dy = (this.fy * beta.fx) - (this.fx * beta.fy) - alpha.fy;
        double d = (dx * dx) + (dy * dy);
        
        double tx = (this.fx * alpha.fx) - (this.fy * alpha.fy) + beta.fx;
        double ty = (this.fx * alpha.fy) + (this.fy * alpha.fx) + beta.fy;

        fx = ((tx * dx) + (ty * dy)) / d;
        fy = ((ty * dx) - (tx * dy)) / d;
    }


  /* --- ToString --- */

    /**
     * Returns a string representation of the object.
     *
     * @return    a String representation of the object
     */
    public String toString() {
        return "(" + fx + " : " + fy + ")E";
    }

}

