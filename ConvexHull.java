import java.util.*;

import Jcg.geometry.*;
import Jcg.geometry.kernel.*;
import Jcg.convexhull2d.ConvexHull_2;

/**
 * @author Luca Castelli Aleardi, Ecole Polytechnique (INF562)
 * 
 * Implementation of Andrew's algorithm for the computation of 2d convex hulls
 */
public class ConvexHull implements ConvexHull_2 {
	
	/** Predicates used to perform geometric computations and tests (approximate, exact or filtered computations)*/
	GeometricPredicates_2 predicates;

	/**
	 * Set the choice of geometric predicates
	 */
	public ConvexHull() {
		// this.predicates=new FilteredPredicates_2();
		this.predicates=new ApproximatePredicates_2();
		// this.predicates=new ExactPredicates_2();
	}
	
	class SortPointsByCoordinates implements Comparator<Point_2> {
		
		public int compare(Point_2 p1, Point_2 p2) {
			return p1.compareTo(p2);
		}
	}
	
	/**
	 * Compute the upper Hull of the point set
	 * 
	 * @param sortedPoints a list of points already sorted (according to a given order)
	 * 
	 * @return the ordered list of points on the upper Hull
	 */
    private ArrayList<Point_2> computeUpperHull(ArrayList<Point_2> sortedPoints) {
		if (sortedPoints.size() <= 2)
			return sortedPoints;
		ArrayList<Point_2> hull = new ArrayList<Point_2>();
		hull.add(sortedPoints.get(0));
		hull.add(sortedPoints.get(1));
		int n = sortedPoints.size();
		for (int i = 2; i < n; i++) {
			Point_2 p = sortedPoints.get(i);
			while (hull.size() >= 2 && this.predicates.orientation(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) != -1) {
				hull.remove(hull.size() - 1);
			}
			hull.add(p);
		}
		return hull;
    }

	/**
	 * Compute the lower Hull of the point set
	 * 
	 * @param sortedPoints a list of points already sorted (according to a given order)
	 * 
	 * @return the ordered list of points on the lower Hull
	 */
    private ArrayList<Point_2> computeLowerHull(ArrayList<Point_2> sortedPoints) {
		if (sortedPoints.size() <= 2)
			return sortedPoints;
		ArrayList<Point_2> hull = new ArrayList<Point_2>();
		hull.add(sortedPoints.get(0));
		hull.add(sortedPoints.get(1));
		int n = sortedPoints.size();
		for (int i = 2; i < n; i++) {
			Point_2 p = sortedPoints.get(i);
			while (hull.size() >= 2 && this.predicates.orientation(hull.get(hull.size() - 2), p, hull.get(hull.size() - 1)) != -1) {
				hull.remove(hull.size() - 1);
			}
			hull.add(p);
		}
		return hull;
	}
    
    
	/**
	 * Compute the convex hull of the input point set
	 * 
	 * @param points a point cloud (points are not sorted)
	 * 
	 * @return the ordered set of points on the convex hull
	 */
    public PointCloud_2 computeConvexHull(PointCloud_2 points) {
		ArrayList<Point_2> sortedPoints = new ArrayList<Point_2>(points.listOfPoints());
		Collections.sort(sortedPoints, new SortPointsByCoordinates());
		ArrayList<Point_2> lowerHull = computeLowerHull(sortedPoints);
		ArrayList<Point_2> upperHull = computeUpperHull(sortedPoints);
		Collections.reverse(upperHull);
		lowerHull.addAll(upperHull);
		PointCloud_2 hull = new PointCloud_2(lowerHull);
		return hull;
    }

}