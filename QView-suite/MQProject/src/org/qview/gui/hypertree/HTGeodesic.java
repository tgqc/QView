/*
 * HTGeodesic.java
 * www.bouthier.net
 *
 * The MIT License :
 * -----------------
 * Copyright (fScreenControlPoint) 2001 Christophe Bouthier
 *
 * Permission is hereby granted, free of charge, to any person obtaining fScreenPointA
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
import java.awt.Shape;

//QUAD
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
//QUAD


/**
 * The HTGeodesic class implements fScreenPointA geodesic 
 * linking to points in the Poincarre model.
 * 
 * 
 * @author Christophe Bouthier [bouthier@loria.fr]
 * @version 1.0
 */
class HTGeodesic {

    private static final double EPSILON = 1.0E-10; // epsilon

    private static final int    LINE    = 0;       // draw fScreenPointA line
    private static final int    ARC     = 1;       // draw an arc

    private int      fGeodesicType = LINE; // fGeodesicType of the geodesic

    private HTCoordE fzFirst   = null; // first point (Euclidian)
    private HTCoordE fzSecond   = null; // second point (Euclidian)
    private HTCoordE fzControl   = null; // control point (Euclidian)
    private HTCoordE fzCentre   = null; // center of the geodesic;

    //CURVE
    private double  fRay     = 0.0;  // ray of the geod
    private double  fAlpha = 0.0;  // fAlpha factor
    private double  fBeta  = 0.0;  // fBeta factor

    private int     fNum     = 10;   // number of segment in the approximation
    private HTCoordE[] ftz = new HTCoordE[(2 * fNum) + 1];
    private HTCoordS[] fts = new HTCoordS[(2 * fNum) + 1];
    //CURVE

    private HTCoordS fScreenPointA    = null; // first point (on the screen)
    private HTCoordS fScreenPointB    = null; // second point (on the screen)
    private HTCoordS fScreenControlPoint    = null; // control point (on the screen)

    private int      fd    = 0;

    private boolean  fKleinMode = false;
    private double   fProjType  = -2.0;

    //QUAD
    private QuadCurve2D fCurve = new QuadCurve2D.Double(); 
    //QUAD
    
    private boolean fQuadMode = true;


  /* --- Constructor --- */

    /**
     * Constructor.
     * 
     * 
     * 
     * 
     * 
     * @param fzFirst       the first point
     * @param fzSecond       the second point
     */
    HTGeodesic(HTCoordE za, HTCoordE zb) {
        this.fzFirst    = za;
        this.fzSecond    = zb;
      
        fzControl = new HTCoordE();
        fzCentre = new HTCoordE();

        //CURVE
        for (int i = 0; i < ftz.length; ++i) {
            ftz[i] = new HTCoordE();
            fts[i] = new HTCoordS();
        }
        //CURVE

        fScreenPointA = new HTCoordS();
        fScreenPointB = new HTCoordS();
        fScreenControlPoint = new HTCoordS();

        rebuild();
    }


  /* --- Refresh --- */

    /**
     * Refresh the screen coordinates of this node.
     *
     * @param sOrigin   the origin of the screen plane
     * @param sMax      the (xMax, yMax) point in the screen plane
     */
    void refreshScreenCoordinates(HTCoordS sOrigin, HTCoordS sMax) {
        if (fKleinMode) {
            fScreenPointA.projectionEtoS(fzFirst.pToK(), sOrigin, sMax);
            fScreenPointB.projectionEtoS(fzSecond.pToK(), sOrigin, sMax);
        } else if (fProjType != 0.0) {
            fScreenPointA.projectionEtoS(fzFirst.pToZ(fProjType), sOrigin, sMax);
            fScreenPointB.projectionEtoS(fzSecond.pToZ(fProjType), sOrigin, sMax);
            fScreenControlPoint.projectionEtoS(fzControl.pToZ(fProjType), sOrigin, sMax);

            fd = ((fScreenPointA.fx - fScreenPointB.fx) * (fScreenPointA.fx - fScreenPointB.fx)) + ((fScreenPointA.fy - fScreenPointB.fy) * (fScreenPointA.fy - fScreenPointB.fy));
            if (fQuadMode) {
                if (fd != 0) {
                    //QUAD
                    fCurve.setCurve(fScreenPointA.fx, fScreenPointA.fy, fScreenControlPoint.fx, fScreenControlPoint.fy, fScreenPointB.fx, fScreenPointB.fy);
                    //QUAD
                }
            } else {
                //CURVE
                for (int i = 0; i < ftz.length; ++i) {
                    fts[i].projectionEtoS(ftz[i].pToZ(fProjType), sOrigin, sMax);
                }
                //CURVE
            }
        } else {
            fScreenPointA.projectionEtoS(fzFirst, sOrigin, sMax);
            fScreenPointB.projectionEtoS(fzSecond, sOrigin, sMax);
            fScreenControlPoint.projectionEtoS(fzControl, sOrigin, sMax);

            fd = ((fScreenPointA.fx - fScreenPointB.fx) * (fScreenPointA.fx - fScreenPointB.fx)) + ((fScreenPointA.fy - fScreenPointB.fy) * (fScreenPointA.fy - fScreenPointB.fy));
            if (fQuadMode) {
                if (fd != 0) {
                    //QUAD
                    fCurve.setCurve(fScreenPointA.fx, fScreenPointA.fy, fScreenControlPoint.fx, fScreenControlPoint.fy, fScreenPointB.fx, fScreenPointB.fy);
                    //QUAD
                }
            } else {
                //CURVE
                for (int i = 0; i < ftz.length; ++i) {
                     fts[i].projectionEtoS(ftz[i], sOrigin, sMax);
                }
                //CURVE
            }
        }
    }


  /* --- Rebuild --- */

    /**
     * Builds the geodesic.
     */
    void rebuild() {
      if (! fKleinMode) {
        if ( // fzFirst == origin
             (Math.abs(fzFirst.d()) < EPSILON) || 
             
             // fzSecond == origin
             (Math.abs(fzSecond.d()) < EPSILON) || 
             
             // fzFirst = lambda.fzSecond
             (Math.abs((fzFirst.fx / fzSecond.fx) - (fzFirst.fy / fzSecond.fy)) < EPSILON) )
        {    
            fGeodesicType = LINE;
        } else {
            fGeodesicType = ARC;

            double da = 1 + fzFirst.fx * fzFirst.fx + fzFirst.fy * fzFirst.fy;
            double db = 1 + fzSecond.fx * fzSecond.fx + fzSecond.fy * fzSecond.fy;
            double dd = 2 * (fzFirst.fx * fzSecond.fy - fzSecond.fx * fzFirst.fy);
 
            fzCentre.fx = (fzSecond.fy * da - fzFirst.fy * db) / dd;
            fzCentre.fy = (fzFirst.fx * db - fzSecond.fx * da) / dd;

            double det = (fzSecond.fx - fzCentre.fx) * (fzFirst.fy - fzCentre.fy) - 
                         (fzFirst.fx - fzCentre.fx) * (fzSecond.fy - fzCentre.fy);
            double fa  = fzFirst.fy * (fzFirst.fy - fzCentre.fy) - fzFirst.fx * (fzCentre.fx - fzFirst.fx);
            double fb  = fzSecond.fy * (fzSecond.fy - fzCentre.fy) - fzSecond.fx * (fzCentre.fx - fzSecond.fx);

            fzControl.fx = ((fzFirst.fy - fzCentre.fy) * fb - (fzSecond.fy - fzCentre.fy) * fa) / det;
            fzControl.fy = ((fzCentre.fx - fzFirst.fx) * fb - (fzCentre.fx - fzSecond.fx) * fa) / det; 

            if (! fQuadMode) {
                //CURVE
                fRay = Math.sqrt(fzCentre.d2() - 1);
                double p = ((fzFirst.fx - fzCentre.fx) * (fzSecond.fx - fzCentre.fx)) + 
                           ((fzFirst.fy - fzCentre.fy) * (fzSecond.fy - fzCentre.fy)); 
                fAlpha = Math.acos(p / (fRay*fRay));
                HTCoordE cPrim = new HTCoordE();
                cPrim.sub(fzControl, fzCentre);
                fBeta = cPrim.arg();

                ftz[0].fx = fzCentre.fx + (fRay * Math.cos(fBeta));
                ftz[0].fy = fzCentre.fy + (fRay * Math.sin(fBeta));

            
                for (int j = 1; j <= fNum; ++j) {
                    double dj = (double) j;
                    double dn = (double) (2 * fNum);

                    double pair = fBeta - (fAlpha * (dj / dn));
                    double impair  = fBeta + (fAlpha * (dj / dn));
                    int indPair = 2 * j;
                    int indImpair = (2 * j) - 1;
                
                    ftz[indPair].fx = fzCentre.fx + (fRay * Math.cos(pair));
                    ftz[indPair].fy = fzCentre.fy + (fRay * Math.sin(pair));

                    ftz[indImpair].fx = fzCentre.fx + (fRay * Math.cos(impair));
                    ftz[indImpair].fy = fzCentre.fy + (fRay * Math.sin(impair));
                }
                //CURVE
            }
        }
      }
    }


  /* --- Draw --- */

    /**
     * Draws this geodesic.
     *
     * @param g    the graphic context
     */
    void draw(Graphics g) {
        g.setColor(HTColours.GEODESIC);
        //QUAD
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON); 
        //QUAD
          if (fKleinMode) {
                g.drawLine(fScreenPointA.fx, fScreenPointA.fy, fScreenPointB.fx, fScreenPointB.fy);
          } else {
            switch(fGeodesicType) {
            case LINE:
                g.drawLine(fScreenPointA.fx, fScreenPointA.fy, fScreenPointB.fx, fScreenPointB.fy);
                break;
            case ARC:
                if (fd != 0) {
                    if (fQuadMode) {
                        //QUAD
                        g2.draw(fCurve);
                        //QUAD
                    } else {
                        //CURVE
                        drawCurve(g);
                        //CURVE
                    }
                }
                break;
            default:
                break;
            }
          }
         //QUAD
         } else {
             System.err.println("Error : Hypertree 1.0 requires Java 1.2 "
                                + "or superior.");
         }
         //QUAD
    } 


//CURVE
  /* --- Draw fCurve --- */

    /**
     * Draws the fCurve specified by the given points.
     * 
     * 
     * @param g    the graphic context
     */
    private void drawCurve(Graphics g)
    {
         int d = ((fts[0].fx - fts[2 * fNum].fx) * (fts[0].fx - fts[2 * fNum].fx)) +
                 ((fts[0].fy - fts[2 * fNum].fy) * (fts[0].fy - fts[2 * fNum].fy));
         if (d == 0) {
             return;
         }

         if (HTDraw.fIsTranslating) {
             g.drawLine(fScreenPointA.fx, fScreenPointA.fy, fScreenPointB.fx, fScreenPointB.fy);
             return;
         }
         
         g.drawLine(fts[0].fx, fts[0].fy, fts[1].fx, fts[1].fy);
         g.drawLine(fts[0].fx, fts[0].fy, fts[2].fx, fts[2].fy);

         int p = 0;
         for (int j = 1; j < fNum; ++j) {
             p = 2 * j;
             g.drawLine(fts[p].fx, fts[p].fy, fts[p+2].fx, fts[p+2].fy);
             p = (2 * j) - 1;
             g.drawLine(fts[p].fx, fts[p].fy, fts[p+2].fx, fts[p+2].fy);
         }
    }
//CURVE

  /* --- Klein --- */

    /**
     * Sets the klein mode.
     *
     * @param mode    setting on or off
     */
    void kleinMode(boolean mode) {
        if (mode != fKleinMode) {
            fKleinMode = mode;
        }
    }

    /**
     * Sets the projection fGeodesicType.
     * 
     * 
     * @param z    the value of the projection
     */
    void changeProjType(double z) {
        if (z != fProjType) {
            fProjType = z;
        }
    }

    void setQuadMode(boolean mode) {
        if (fQuadMode != mode) {
            fQuadMode = mode;
            rebuild();
        }
    }

    boolean getQuadMode() {
        return fQuadMode;
    }
    
  /* --- ToString --- */

    /**
     * Returns fScreenPointA string representation of the object.
     * 
     * 
     * @return fScreenPointA String representation of the object
     */
    public String toString() {
        String result = "Geodesic betweens : " +
                        "\n\t A: " + fzFirst + 
                        "\n\t B: " + fzSecond +
                        "\n\t is ";
        switch(fGeodesicType) {
        case LINE:
            result += "a line.";
            break;
        case ARC:
            result += "an arc.";
            break;
        default:
            result += "nothing ?";
            break;
        }
        return result;
    }

}

