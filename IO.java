import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import Jcg.geometry.PointCloud_2;
import Jcg.geometry.Point_2;
import Jcg.mesh.MeshBuilder;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.triangulations2D.Delaunay_2;
import Jcg.triangulations2D.TriangulationDSFace_2;
import Jcg.triangulations2D.TriangulationDSVertex_2;
import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 * This classprovide methods for dealing with input/outputand format conversions
 */

public class IO {
	
	/**
	 * Load a 2D point set from a Json file
	 * 
	 * @param filename  name of the input file
	 * @return  an array storing the input points
	 */
	public static Point_2[] loadPointSet(String filename){
		Point_2[] result=null; // the input points
		System.out.print("Reading JSON input file: "+filename+"...");
		JSONArray values;
		JSONObject json;
		
		json = loadFile(filename);
		System.out.println("ok");
		
		String type = json.getString("type");
		String name = json.getString("name");
		JSONArray inputPoints = json.getJSONArray("points");
		System.out.println("\ttype: "+type);
		System.out.println("\tname: "+name);
		System.out.println("\tsize: "+inputPoints.size());
		
		result=new Point_2[inputPoints.size()];
		for(int i=0;i<inputPoints.size();i++) {
			JSONObject point=inputPoints.getJSONObject(i);
			int index=point.getInt("i");
			double x=point.getDouble("x");
			double y=point.getDouble("y");
			result[i]=new Point_2(x, y);
		}
		System.out.println("Input point set loaded: "+result.length+" points");
		return result;
	}

	/**
	 * Load a JSON object from input file
	 * 
	 * @param filename name of the input file
	 */
	private static JSONObject loadFile(String filename) {
		JSONObject outgoing=null;
		BufferedReader reader = null;
		FileReader fr = null;

		try {
			fr = new FileReader(filename);
			reader = new BufferedReader(fr);
			outgoing = new JSONObject(reader);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		} finally {
			try {
				if (reader != null)
					reader.close();

				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				System.err.format("IOException: %s%n", ex);
			}
		}
		
		return outgoing;
	}
	
	/**
	 * Convert a Delaunay triangulation to the half-edge representation
	 * 
	 * @param del  a 2D Delaunay trianuglation
	 * @return  the half-edge representation of the triangulation
	 */
	public static Polyhedron_3<Point_2> polyhedronFromTriangulation(Point_2[] points, Delaunay_2 del){
		MeshBuilder<Point_2> builder=new MeshBuilder<Point_2>();
		
		Collection<TriangulationDSFace_2<Point_2>> facesDel = del.finiteFaces();
		int f=facesDel.size();
		
		// assign an index to each vertex
		Collection<TriangulationDSVertex_2<Point_2>> verticesDel = del.finiteVertices();
		int index=0;
		for(TriangulationDSVertex_2<Point_2> vertex: verticesDel) {
			vertex.index=index;
			index++;
		}
		
		int[] faceDegrees=new int[f];
		for(int i=0;i<f;i++)
			faceDegrees[i]=3; // all inner faces by default are triangular
		
		int[][] faces=new int[f][3];
		int counter=0;
		for(TriangulationDSFace_2<Point_2> face: facesDel) {
			int v0=face.vertex(0).index;
			int v1=face.vertex(1).index;
			int v2=face.vertex(2).index;
			//System.out.println("f"+counter+": "+v0+", "+v1+", "+v2);
			
			faces[counter][0]=v0;
			faces[counter][1]=v1;
			faces[counter][2]=v2;
			
			counter++;
		}
		
		Polyhedron_3<Point_2> result=builder.createMesh(points, faceDegrees, faces);
		result.isValid(true);
		return result;
	}

	public static float score(Polyhedron_3<Point_2> graph) {
		int c = 0;
		ArrayList<Point_2> points = new ArrayList<Point_2>();
		for (int i=0; i<graph.vertices.size(); i++) {
			points.add(graph.vertices.get(i).getPoint());
		}
		ConvexHull convH = new ConvexHull();
		c = convH.computeConvexHull(new PointCloud_2(points)).listOfPoints().size();
		int f = graph.facets.size();
		int m = graph.halfedges.size() / 2;
		int n = graph.vertices.size();
		float score1 = 1 - (float)f / (float)(2*(n-1) - c);
		float score2 = 1 - (float)m / (float)(3*(n-1) - c);
		return score2;
	}

}
