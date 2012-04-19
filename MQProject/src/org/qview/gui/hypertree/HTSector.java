/*
 * HTSector.java
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
 * The HTSector class implements the sector division.
 *
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTSector {

    HTCoordE A = null;
    HTCoordE B = null;


  /* --- Constructor --- */

    /**
     * Constructor.
     */
    HTSector() {
    }



  /* --- Angle --- */

    /**
     * Returns the angle of the sector.
     *
     * @return    the angle of the sector
     */
    double getAngle() {
        double angle = B.arg() - A.arg();
        if (angle < 0) {
            angle += 2*Math.PI;
        }
        return angle;
    }

    /**
     * Returns the angle of the bissectrice of the sector.
     *
     * @return    the bisector angle
     */
    double getBisectAngle() {
        return A.arg() + (getAngle() / 2);
    }


  /* --- Transformations --- */

    /**
     * Translates.
     *
     * @param t    the translation vector
     */
    void translate(HTCoordE t) {
        A.translate(t);
        B.translate(t);
    }

    public String toString() {
        String s = "A: " + A + "\n" +
                   "\tB: " + B + "\n" +
                   "\tAngle: " + getAngle() + "\n" +
                   "\tBisectAngle " + getBisectAngle() + "\n";
        return s;
    }

}

