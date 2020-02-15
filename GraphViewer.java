import java.util.Collection;
import java.util.LinkedList;

import Jcg.geometry.Point_2;
import Jcg.geometry.Vector_2;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.triangulations2D.Delaunay_2;
import Jcg.triangulations2D.HalfedgeHandle;
import processing.core.PApplet;

	/**
	 * Main program that takes as input a JSON and a planar graph with convex faces
	 * and a minimal number of edges.
	 * 
	 * @author Luca Castelli Aleardi (INF562, 2020)
	 * /home/amturing/ownCloud/datasets/JSON/instances_01/challenge_instances/data/uniform
	 */
public class GraphViewer extends PApplet {	
	/** array storing the input points */
	public static Point_2[] pointSet=null; // it must be defined before launching the Processing viewer
	/** Delaunay triangulation */
	public Delaunay_2 delaunay=null; // it must be computed: by default this is undefined
	/** Half-edge representation of the planar graph */
	public Polyhedron_3<Point_2> graph=null; // it must be converted from the Delaunay triangulation: by default this is undefined
	/** coordinates of the bounding box containing the input points */
    protected double xmin=Double.MAX_VALUE, xmax=Double.MIN_VALUE, ymin=Double.MAX_VALUE, ymax=Double.MIN_VALUE;

    // parameters for edge rendering
    double boundaryThickness=5;
    private int backgroundColor=255;
        
    /** position of the mouse in the bounding box, after click */
    public Point_2 currentPosition;
    
    /** index of the point selected with mouse click (to show) */
    public int selectedPoint=-1;
        	
   	// parameters of the 2d frame/canvas	
    public static int sizeX=600; // horizontal size of the canvas (pizels)
    public static int sizeY=600; // vertical size of the canvas (pixels)
	Point_2 a,b; // range of the window (left bottom and right top corners)
	
	int n_removed=0;
	
	public void settings(){
		  System.out.println("Setting Canvas size: "+sizeX+" x "+sizeY);
		  this.size(sizeX,sizeY); // set the size of the Java Processing frame
	}
	
	public void setup(){
		  // set drawing parameters (size and range of the drawing layout)
		  double w2=sizeX/2.0;
		  double h2=sizeY/2.0;
		  this.a=new Point_2(0, 0); // left bottom corner (the drawing region is centered at the origin)
		  this.b=new Point_2(1000, 1000); // top right corner of the drawing region
		  
		  if(this.pointSet!=null)
			  this.updateBoundingBox(this.pointSet); // update the corners 'a' and 'b'
	}
	  
	/**
	 * Deal with keyboard events
	 */
	public void keyPressed(){
		  switch(key	) {
		case('d'):this.delaunay=Algorithms.computeDelaunay(pointSet); break;
		case('h'): {
			this.graph=IO.polyhedronFromTriangulation(pointSet, this.delaunay);
			this.graph.resetMeshIndices();
			//System.out.println(Algorithms.toString(this.graph)); // print the halfedges of the mesh
			this.delaunay=null; // erase the Delaunay triangulation (not useful anymore)
			break;
		}
		case('r'): {
			while (Algorithms.randomDecimation(this.graph)) {
			n_removed++;
			if (n_removed>this.graph.vertices.size()/10) {
				System.out.println("Too manny nulls, cleaning...");
				Algorithms.cleanMesh(this.graph);
				n_removed=0;
			}
			}
			Algorithms.cleanMesh(this.graph);
			break;
		}
		case('s'):System.out.println(IO.score(this.graph)); break;
		case('g'):Algorithms.cleanMesh(this.graph); break;
		case('w'):IO.writeEdges(graph); break;
		case('c'):Algorithms.convexifyBoundary(this.graph);break;
		case('-'):this.zoom(1.2); break;
		case('+'):this.zoom(0.8); break;
		}
	}
	  
	  public void zoom(double factor) {
		  Point_2 barycenter=Point_2.midPoint(a, b);
		  Vector_2 vA=(Vector_2)barycenter.minus(a);
		  Vector_2 vB=(Vector_2)barycenter.minus(b);
		  vA=vA.multiplyByScalar(factor);
		  vB=vB.multiplyByScalar(factor);
		  a=barycenter.sum(vA);
		  b=barycenter.sum(vB);
	  }

	  /**
	   * Main function for drawing all geometric objects (points, edges, ...)
	   */
	  public void draw(){
		  this.background(this.backgroundColor); // set the color of background (clean the background)

		  if(this.pointSet!=null) {
			  for(Point_2 p: this.pointSet)
				  this.drawPoint(p);
		  }

		  if(this.delaunay!=null) {
			  this.drawTriangulation(this.delaunay);
		  }
		  else if(this.graph!=null) {
			  this.drawPolyhedron(this.graph);
		  }

		  this.drawOptions();
	  }

	  /**
	   * Show options on the screen
	   */
	  public void drawOptions() {
		String label="press '-' or '+' for zooming\n"; // text to show
		label=label+"press 'd' for computing the Delaunay triangulation\n";
		label=label+"press 'h' for converting to the Half-edge representation\n";
		label=label+"press 'r' for performing edge decimation\n";
		label=label+"press 's' to display score\n";
		label=label+"press 'w' to write to file\n";
		label=label+"press 'c' to convexify boundary\n";
		label=label+"use 'left mouse click' to show vertex index\n";
		label=label+"use 'right mouse button' to drag the layout";
		
		int posX=2;
		int posY=2;
		int textHeight=84;
		
		//this.stroke(edgeColor, edgeOpacity);
		this.fill(200);
		this.rect((float)posX, (float)posY, 380, textHeight); // fill a gray rectangle
		this.fill(0);
		this.text(label, (float)posX+2, (float)posY+10); // draw the text
	  }

	  public void mousePressed() {
		  this.currentPosition=new Point_2(mouseX, mouseY);
		  this.selectedPoint=this.selectPoint(mouseX, mouseY);
	  }
	  
	  public void mouseDragged() {
		  if(mouseButton==RIGHT) { // translate the window
			  double norm=Math.sqrt(this.a.squareDistance(this.b).doubleValue());
			  double scaleFactor=norm/(this.sizeX);
			  
			  double deltaX=(mouseX-currentPosition.getX().doubleValue())*(scaleFactor);
			  double deltaY=(currentPosition.getY().doubleValue()-mouseY)*(scaleFactor);
		  
			  this.a.translateOf(new Vector_2(-deltaX, -deltaY)); // update the left bottom and right top vertices
			  this.b.translateOf(new Vector_2(-deltaX, -deltaY));
		  
			  this.currentPosition=new Point_2(mouseX, mouseY);
		  }
	  }

	  /**
	   * Select the vertex whose 2d projection is the closest to pixel (i, j)
	   */
	  public int selectPoint(int i, int j) {			  
		  int result=-1;
		  
		  double minDist=40.;
		  int k=0;
		  for(Point_2 p: this.pointSet) { // iterate over the vertices of g
			  int[] q=this.getPoint(p);
			  
			  double dist=Math.sqrt((q[0]-i)*(q[0]-i)+(q[1]-j)*(q[1]-j));
			  if(dist<minDist) {
				  minDist=dist;
				  result=k;
			  }
			  k++;
		  }
		  
		  this.selectedPoint=result;
		  
		  if(this.selectedPoint>=0)
			  System.out.println("Selected point: p"+this.selectedPoint+" "+this.pointSet[this.selectedPoint]);
		  
		  return result;
	  }

	  /**
	   * Draw a point 'p' on the canvas
	   */
	  public void drawPoint(Point_2 p) {
		  if(p==null) return;
		  
		int[] min=getPoint(p); // pixel coordinates of the point in the frame
		this.stroke(50, 255); // border color
		this.fill(50, 50, 50, 255); // node color
		
		int vertexSize=5; // basic vertex size

		this.ellipse((float)min[0], (float)min[1], vertexSize, vertexSize);
	  }

	  /**
	   * Draw a segment (p, q)
	   */
	  public void drawSegment(Point_2 p, Point_2 q) {
		  if(p==null || q==null) return;
		  
		int[] coordP=getPoint(p); // pixel coordinates of the point in the frame
		int[] coordQ=getPoint(q); // pixel coordinates of the point in the frame
		this.stroke(50, 255); // border color
		this.fill(50, 50, 50, 255); // node color
		
		int vertexSize=5; // basic vertex size

		this.line((float)coordP[0], (float)coordP[1], (float)coordQ[0], (float)coordQ[1]);
	  }

	  /**
	   * Draw a point 'p' on the canvas
	   */
	  public void drawTriangulation(Delaunay_2 tri) {
		  if(tri==null)
			  return;

		  LinkedList<Point_2[]> cSegmentsDel = new LinkedList<Point_2[]> ();
		  Collection<HalfedgeHandle<Point_2>> edges=tri.finiteEdges();
		  for (HalfedgeHandle<Point_2> e : edges) {
			  this.drawSegment(e.getVertex(0).getPoint(), e.getVertex(1).getPoint());
			  //cSegmentsDel.add(new Point_2[]{e.getVertex(0).getPoint(), e.getVertex(1).getPoint()});
		  }
	  }

	  /**
	   * Draw a point 'p' on the canvas
	   */
	  public void drawPolyhedron(Polyhedron_3<Point_2> mesh) {
		  if(mesh==null)
			  return;

		  for (Halfedge<Point_2> e: mesh.halfedges) {
			  if(e!=null && e.getOpposite()!=null && e.getVertex().index>e.getOpposite().getVertex().index) // draw each edge only once
				  this.drawSegment(e.getVertex().getPoint(), e.getOpposite().getVertex().getPoint());
		  }
	  }

		/**
		 * Return the integer coordinates of a pixel corresponding to a given point
		 * 
		 * Warning: we must take care of the following parameters:
		 * -) the size of the canvas
		 * -) the size of bottom and left panels
		 * -) the negative direction of y-coordinates (in java drawing)
		 */
		public int[] getPoint(Point_2 v) {
			double x=v.getX().doubleValue(); // coordinates of point v
			double y=v.getY().doubleValue();
			double xRange=b.getX().doubleValue()-a.getX().doubleValue(); // width and height of the drawing area
			double yRange=b.getY().doubleValue()-a.getY().doubleValue();
			int i= (int) (this.sizeX*( (x-a.getX().doubleValue()) / xRange )); // scale with respect to the canvas dimension
			int j= (int) (this.sizeY*( (y-a.getY().doubleValue()) / yRange ));
			//i=i+this.horizontalShift;
			j=this.sizeY-j; // y = H - py;
			
			int[] res=new int[]{i, j};
			return res;
		}
		
	    /**
	     * Update of the bounding box
	     */    
	    protected void updateBoundingBox(double x, double y) {
	    	if (x<xmin)
	    		xmin = x;
	    	if (x>xmax)
	    		xmax = x;
	    	if (y<ymin)
	    		ymin = y;
	    	if (y>ymax)
	    		ymax = y;
	    }
	    
	    /**
	     * Return the current coordinates of the bounding box
	     */    
	    public double[] boundingBox() {
	    	return new double[] {xmin, xmax, ymin, ymax};
	    }

	    /**
	     * Update the range of the drawing region (defined by corners points 'a' and 'b')
	     */    
	    public void updateBoundingBox(Point_2[] points) {
	    	
	    	for(Point_2 p: points) {
	    		this.updateBoundingBox(p.getX().doubleValue(), p.getY().doubleValue());
	    	}
	    	a=new Point_2(xmin-this.boundaryThickness, ymin-this.boundaryThickness);
	    	b=new Point_2(Math.max(xmax,ymax)+this.boundaryThickness, Math.max(xmax,ymax)+this.boundaryThickness);
	    	
	    	System.out.println("Bounding box: ("+xmin+", "+ymin+") - ("+xmax+", "+ymax+")");
	    }

	public static void main(String[] args) {
		System.out.println("Tools for the \"CG 2020 contest\"");
		if(args.length<1) {
			System.out.println("Error: one argument required: input file in JSON format");
			System.exit(0);
		}
		// uncomment the line below to show a 2D layout of the graph
		
		GraphViewer.pointSet=IO.loadPointSet(args[0]);
		GraphViewer gv = new GraphViewer();
		gv.delaunay=Algorithms.computeDelaunay(pointSet);
		gv.graph=IO.polyhedronFromTriangulation(pointSet, gv.delaunay);
		gv.graph.resetMeshIndices();
		// System.out.println(Algorithms.toString(this.graph)); // print the halfedges of the mesh
		// gv.delaunay=null; // erase the Delaunay triangulation (not useful anymore)
		Algorithms.convexifyBoundary(gv.graph);
		while (Algorithms.randomDecimation(gv.graph)) {
			gv.n_removed++;
			if (gv.n_removed > gv.graph.vertices.size()/10) {
				System.out.println("Too manny nulls, cleaning...");
				Algorithms.cleanMesh(gv.graph);
				gv.n_removed=0;
			}
		}
		Algorithms.cleanMesh(gv.graph);
		System.out.println(IO.score(gv.graph));
		Algorithms.cleanMesh(gv.graph);
		IO.writeEdges(gv.graph);

		// PApplet.main(new String[] { "GraphViewer" }); // launch the Processing viewer
	}



	
}
