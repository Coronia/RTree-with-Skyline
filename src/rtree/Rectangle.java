// The rectangle that cover several points
// In this program, data point is denoted by rectangle with the same low and high coordinate

package rtree;

public class Rectangle implements Cloneable, Comparable<Rectangle> {
	private Point low;
	private Point high;
	
	public Rectangle(Point p1, Point p2) { // Create rectangle that covers an area
		if (p1 == null || p2 == null) { throw new IllegalArgumentException("Points cannot be null."); }
		if (p1.getDimension() != p2.getDimension()) { throw new IllegalArgumentException("Points must be of same dimension."); }
		for (int i = 0; i < p1.getDimension(); i ++) {
			if (p1.getCoordinate(i) > p2.getCoordinate(i)) { throw new IllegalArgumentException("Wrong coordinates"); }
		}
		low = (Point) p1.clone();
		high = (Point) p2.clone();
	}
	
	public Rectangle(Point p) { // Create rectangle for a single point
		if (p == null) { throw new IllegalArgumentException("Points cannot be null."); }
		low = (Point) p.clone();
		high = (Point) p.clone();
	}
	
	public Point getLow() { return (Point) low.clone(); }
	
	public Point getHigh() { return (Point) high.clone(); }
	
	public Rectangle getUnion(Rectangle rec) { // Get the minimum rectangle that contains both of the 2 targeted rectangles
		if (rec == null) { throw new IllegalArgumentException("Rectangle cannot be null."); }
		if (rec.getDimension() != getDimension()) { throw new IllegalArgumentException("Rectangle must be of same dimension."); }
		double[] min = new double[getDimension()];
		double[] max = new double[getDimension()];
		for (int i = 0; i < getDimension(); i ++) {
			min[i] = Math.min(low.getCoordinate(i), rec.low.getCoordinate(i));
			max[i] = Math.max(high.getCoordinate(i), rec.high.getCoordinate(i));
		}
		return new Rectangle(new Point(min), new Point(max));
	}
	
	public double getArea() {
		double area = 1;
		for (int i = 0; i < getDimension(); i ++) {
			area *= high.getCoordinate(i) - low.getCoordinate(i);
		}
		return area;
	}
	
	public static Rectangle getUnion(Rectangle[] rec) { // Get the minimum rectangle that contains all the targeted rectangles
		if (rec == null || rec.length == 0) { throw new IllegalArgumentException("Rectangle array is empty."); }
		Rectangle res = (Rectangle) rec[0].clone();
		for (int i = 1; i < rec.length; i ++) {
			res = res.getUnion(rec[i]);
		}
		return res;
	}
	
	@Override
	protected Object clone() {
		Point p1 = (Point) low.clone();
		Point p2 = (Point) high.clone();
		return new Rectangle(p1, p2);
	}
	
	@Override
	public String toString() {
		if (!low.equals(high)) { return "Rectangle Low: " + low + ", High: " + high; } // For rectangle
		return low.toString(); // For single point
	}
	
	public double intersectArea(Rectangle rec) { // Calculate the area of intersection with another rectangle
		if (isIntersect(rec)) { return 0; }
		double area = 1;
		for (int i = 0; i < rec.getDimension(); i ++) { // Multiply the intersected edges of each dimension
			double l1 = this.low.getCoordinate(i);
			double h1 = this.high.getCoordinate(i);
			double l2 = rec.low.getCoordinate(i);
			double h2 = rec.high.getCoordinate(i);
			
			if (l1 <= l2 && h1 <= h2) { area *= (h1 - l1) - (l2 - l1); } // Left
			else if (l1 >= l2 && h1 >= h2) { area *= (h2 - l2) - (l1 - l2); } // Right
			else if (l1 >= l2 && h1 <= h2) { area *= h1 - l1; } // within
			else if (l1 <= l2 && h1 >= h2) { area *= h2 - l2; } // enclosure
		}
		return area;
	}
	
	public boolean isIntersect(Rectangle rec) { // Judge if it's intersect with the targeted rectangle
		if (rec == null) { throw new IllegalArgumentException("Rectangle cannot be null."); }
		if (rec.getDimension() != getDimension()) { throw new IllegalArgumentException("Rectangle must be of same dimension."); }
		for (int i = 0; i < getDimension(); i ++) {
			if (low.getCoordinate(i) > rec.high.getCoordinate(i) || high.getCoordinate(i) < rec.low.getCoordinate(i)) { return false; }
		}
		return true;
	}
	
	private int getDimension() { return low.getDimension(); }
	
	public boolean enclosure(Rectangle rec) { // Judge if the targeted rectangle is inside it
		if (rec == null) { throw new IllegalArgumentException("Rectangle cannot be null."); }
		if (rec.getDimension() != getDimension()) { throw new IllegalArgumentException("Rectangle must be of same dimension."); }
		for (int i = 0; i < getDimension(); i ++) {
			if (rec.low.getCoordinate(i) < low.getCoordinate(i) || rec.high.getCoordinate(i) > high.getCoordinate(i)) { return false; }
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Rectangle) {
			Rectangle rec = (Rectangle) obj;
			if (low.equals(rec.getLow()) && high.equals(rec.getHigh()) ) { return true; }
		}
		return false;
	}

	@Override
	public int compareTo(Rectangle arg0) { // Compare 2 rectangles by their mindists
		if (arg0 instanceof Rectangle) {
			Rectangle r = (Rectangle) arg0;
			if (getLow().getDistance() > r.getLow().getDistance()) { return 1; }
			else if (getLow().getDistance() < r.getLow().getDistance()) { return -1; }
			else if (getHigh().getDistance() > r.getHigh().getDistance()) { return 1; }
			else if (getHigh().getDistance() < r.getHigh().getDistance()) { return -1; }
		}
		return 0;
	}
}