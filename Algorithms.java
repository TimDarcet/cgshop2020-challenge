import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;


import Jcg.geometry.Point_;
import Jcg.geometry.Point_2;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;
import Jcg.triangulations2D.Delaunay_2;
import Jcg.triangulations2D.TriangulationDSFace_2;

public class Algorithms {

	/** 
	 * Compute and return the 2D Delaunay triangulation
	 * 
	 * @param points the input point set
	 */
	public static Delaunay_2 computeDelaunay(Point_2[] points){
		System.out.print("Computing a Delaunay triangulation of the input points...");

		Delaunay_2 delaunay = new Delaunay_2();
		for (int i=0; i<points.length; i++) {
			delaunay.insert(points[i]);
		}
		Collection<TriangulationDSFace_2<Point_2>> facesDel = delaunay.finiteFaces();
		int n=delaunay.finiteVertices().size();
		int b=delaunay.convexHullEdges().size();
		int e=delaunay.finiteEdges().size();
		int f=delaunay.finiteFaces().size();
		System.out.println("\t"+n+" vertices");
		System.out.println("\t"+b+" boundary vertices");
		System.out.println("\t"+e+" edges");
		System.out.println("\t"+f+" faces");

		return delaunay;
	}

	/** 
	 * Perform a random edge decimation
	 * 
	 * @param mesh  half-edge representation of the planar mesh
	 */
	public static boolean randomDecimation(Polyhedron_3<Point_2> mesh){
		int randidx = ThreadLocalRandom.current().nextInt(0, mesh.halfedges.size());
		Halfedge<Point_2> he = mesh.halfedges.get(randidx);
		int i=0;
		while (i < 2 * mesh.halfedges.size() && (he == null || !checkRemoval(mesh, he))) {
			randidx = ThreadLocalRandom.current().nextInt(0, mesh.halfedges.size());
			he = mesh.halfedges.get(randidx);
			System.out.println("Invalid, retrying...");
			i++;
		}
		if (i >= 2 * mesh.halfedges.size()) {
			he = null;
			System.out.println("Let's try another way");
			for (Halfedge<Point_2> htest : mesh.halfedges) {
				if (htest != null && checkRemoval(mesh, htest)) {
					he = htest;
					break;
				}
			}
		}
		if (he != null) {
			System.out.println("Found!");
			removeEdge(mesh, he);
			return true;
		}
		return false;
	}

	/** 
	 * Check whether an edge can be safely removed
	 * 
	 * @param mesh  half-edge representation of the planar mesh
	 */
	public static boolean checkRemoval(Polyhedron_3<Point_2> mesh, Halfedge<Point_2> h){
		Vertex<Point_2> v = h.vertex;
		Point_2 p0 = v.getPoint();
		Point_2 p1 = h.next.vertex.getPoint();
		Point_2 p2 = h.opposite.prev.prev.vertex.getPoint();
		double crossProd = (p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x);
		if (crossProd <= Math.ulp(crossProd))
			return false;
		p0 = h.opposite.vertex.getPoint();
		p1 = h.opposite.next.vertex.getPoint();
		p2 = h.prev.prev.vertex.getPoint();
		crossProd = (p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x);
		if (crossProd <= Math.ulp(crossProd))
			return false;
		return true;
	}

	/** 
	 * Check the convexity of a face in a planar map
	 * 
	 * @param face  a face in planar mesh
	 */
	public static boolean isConvex(Face<Point_2> face){
		throw new Error("To be completed");
	}
	
	/** 
	 * Remove one edge (and its opposite halfedge) from the mesh. The two incident faces are merged. <br>
	 * 
	 * Warning: the two removed half-edges are not "deleted" from the set of stored halfedges.
	 * This holds also for the removed face.
	 * 
	 * @param mesh  half-edge representation of a (planar or surface) mesh
	 */
	public static<X extends Point_> void removeEdge(Polyhedron_3<X> mesh, Halfedge<X> e){
		if(e==null || e.getOpposite()==null) // edge not define
			return;
		if(e.next==null || e.prev==null || e.vertex==null) // not valid edge
			return;
		
		//System.out.print("Removing halfedge ");
		
		Face<X> f1=e.getFace();
		Face<X> f2=e.getOpposite().getFace();
		
		// retrieve edges and vertices incident to the edge 'e'
		Halfedge<X> next1=e.getNext();
		Halfedge<X> previous1=e.getPrev();
		Halfedge<X> next2=e.getOpposite().getNext();
		Halfedge<X> previous2=e.getOpposite().getPrev();
		Vertex<X> source=e.getOpposite().getVertex();
		Vertex<X> dest=e.getVertex();
		
		// update references between half-edges incident to 'e'
		next1.prev=previous2;
		previous2.next=next1;
		previous1.next=next2;
		next2.prev=previous1;
		// set the edges incident to the two extremities of 'e'
		source.setEdge(previous1);
		dest.setEdge(previous2);
		f1.setEdge(next1);
		
		// set all references of edges incident to the old face 'f2'
		Halfedge<X> pEdge=next2;
		while(pEdge!=next1) {
			pEdge.setFace(f1);
			pEdge=pEdge.next;
		}
		//System.out.println("\nend setting face references");
		//System.out.println(""+toString(mesh));
		
		// mark the 2 half-edges and one incident face as "removed" (null)
		mesh.halfedges.set(e.index, null);
		mesh.halfedges.set(e.getOpposite().index, null);
		mesh.facets.set(f2.index, null);
	}
	
	/** 
	 * Delete all edges and faces that are "marked as deleted" (e.g. null)
	 * 
	 * @param mesh  half-edge representation of a (planar or surface) mesh
	 */
	public static<X extends Point_> void cleanMesh(Polyhedron_3<X> mesh){
		System.out.print("Running garbage collector...");
		if(mesh==null)
			return;

		ArrayList<Face<X>> faces=new ArrayList<Face<X>>();
		for(Face<X> f: mesh.facets) { // copying faces
			if(f!=null)
				faces.add(f);
		}
		ArrayList<Halfedge<X>> edges=new ArrayList<Halfedge<X>>();
		for(Halfedge<X> e: mesh.halfedges) { // copying half-edges
			if(e!=null)
				edges.add(e);
		}
		
		mesh.facets=faces; // replace faces
		mesh.halfedges=edges; // replace halfedges
		mesh.resetMeshIndices(); // recompute all face and edge indices
		System.out.println("done");
		mesh.isValid(true);
	}

	/** 
	 * Print the half-edge
	 * 
	 * @param e  half-edge
	 */
	public static String toString(Halfedge h){
		return "h"+h.index+" ("+h.getOpposite().getVertex().index+", "+h.getVertex().index+")";
	}
	
	/** 
	 * Print the mesh
	 * 
	 * @param mesh  half-edge representation of a (planar or surface) mesh
	 */
	public static String toString(Polyhedron_3 mesh){
		if(mesh==null)
			return "";
		
		String result="";
		ArrayList<Halfedge> edges=mesh.halfedges;
		for(Halfedge e: edges) { // copying half-edges
			if(e!=null) {
				if(e.next!=null && e.prev!=null)
					result=result+toString(e)+"\t next="+toString(e.next)+"\t previous="+toString(e.prev);
				else
					result=result+toString(e)+"\t next=null\t previous=null";
			}
			result=result+"\n";
		}
		return result;
	}
	
}
