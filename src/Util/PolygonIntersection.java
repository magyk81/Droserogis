package Util;

import java.awt.geom.Line2D;

//======================================================================================================================
//This class tests intersections of convex polygons.
//======================================================================================================================
public class PolygonIntersection
{

    //==================================================================================================================
    // poly1 and poly2 are assumed to be arrays of vertices of closed, convex polygons with the last element assumed
    //    to be connected to the first.
    //
    // returns true iff the polygons 1 and 2 intersect.
    //==================================================================================================================
    public static boolean isIntersect(Vec2[] poly1, Vec2[] poly2)
    {
        for (int i=0; i<poly1.length; i++)
        {
            int ii = (i+1) % poly1.length;

            for (int k=0; k<poly2.length; k++)
            {
                int kk = (k + 1) % poly2.length;

                if (isIntersect(poly1[i], poly1[ii], poly2[k], poly2[kk])) return true;
            }
        }

        return false;
    }







    //==================================================================================================================
    // poly is assumed to be an array of vertices of a closed, convex polygon with the last element assumed to be
    //    connected to the first.
    //
    // rect is assumed to be an axis aligned rectangle.
    //
    // returns true iff the polygon and rectangle intersect.
    //==================================================================================================================
    public static boolean isIntersect(Vec2[] poly, Rect rect)
    {
        for (int i=0; i<poly.length; i++)
        {
            int ii = (i+1) % poly.length;
            if (isIntersect(poly[i], poly[ii], rect)) return true;
        }

        return false;
    }





    //==================================================================================================================
    // p1 and p2 are assumed to be endpoints of a line segment.
    //
    // rect is assumed to be an axis aligned rectangle.
    //
    // returns true iff the line segment and rectangle intersect.
    //==================================================================================================================
    public static boolean isIntersect(Vec2 p1, Vec2 p2, Rect rect)
    {
        if (p1.x < rect.getLeft() && p2.x < rect.getLeft()) return false;
        if (p1.y < rect.getTop()  && p2.y < rect.getTop())  return false;

        if (p1.x > rect.getRight()  && p2.x > rect.getRight())  return false;
        if (p1.y > rect.getBottom() && p2.y > rect.getBottom()) return false;
        return true;
    }







    //==================================================================================================================
    // p1 and p2 are assumed to be endpoints line segment a.
    // p3 and p4 are assumed to be endpoints line segment b.
    //
    // returns true iff the line segments a and b intersect.
    //==================================================================================================================
    public static boolean isIntersect(Vec2 p1, Vec2 p2, Vec2 p3, Vec2 p4)
    {
        return Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }





    //==================================================================================================================
    // Test cases. To run, add vm option: -enableassertions
    //==================================================================================================================
    public static void main(String[] args)
    {

        Rect rect1 = new Rect(10,0, 20, 10);

        Vec2[] poly1 =
        {
                new Vec2( 0,40),
                new Vec2(20,20),
                new Vec2(30,30),
                new Vec2(10,50)
        };

        assert(isIntersect(poly1, rect1) == false);
        rect1.setHeight(20);
        assert(isIntersect(poly1, rect1) == true);


        Rect rect2 = new Rect(20,30, 40, 10);
        assert(isIntersect(poly1, rect2) == true);
        rect2.setLeft(31);
        assert(isIntersect(poly1, rect2) == false);


        Vec2[] sword =
        {
                new Vec2( 0,10),
                new Vec2(10, 0),
                new Vec2(40,30),
                new Vec2(30,40)
        };

        Vec2[] tri1 =
        {
                new Vec2(40,0),
                new Vec2(50,10),
                new Vec2(30,10)
        };

        Vec2[] tri2 =
        {
                new Vec2(40,0),
                new Vec2(50,10),
                new Vec2(20,20)
        };


        assert(isIntersect(sword, tri1) == false);
        assert(isIntersect(sword, tri2) == true);

    }
}