// The point that form the rectangle

package rtree;

public class Point implements Cloneable {
	private double[] data;
	
	public Point(double[] data) {
		if (data == null) { throw new IllegalArgumentException("Coordinates cannot be null."); }
		if (data.length < 2) { throw new IllegalArgumentException("Point dimension should be greater than 1."); }
		this.data = new double[data.length];
		System.arraycopy(data,  0,  this.data,  0,  data.length);
	}
	
	@Override
	protected Object clone() {
		double[] copy = new double[data.length];
		System.arraycopy(data,  0,  copy,  0,  data.length);
		return new Point(copy);
	}
	
	@Override
	public String toString() {
		String res = "(";
		for (int i = 0; i < data.length - 1; i ++) {
			res += data[i] + ",";
		}
		res += data[data.length - 1] + ")";
		return res;
	}
	
	public int getDimension() { return data.length; }
	
	public double getCoordinate(int index) { return data[index]; }
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Point) {
			Point point = (Point) obj;
			if (point.getDimension() != getDimension()) { throw new IllegalArgumentException("Points must be of equal dimensions to be compared."); }
			for (int i = 0; i < getDimension(); i ++) {
				if (getCoordinate(i) != point.getCoordinate(i)) { return false; }
			}
			return true;
		}
		return false;
	}
	
	public double getDistance() { // Calculate the square of mindist of the point (distance to point o)
		double res = 0;
		for (int i = 0; i < data.length; i ++) {
			res += data[i] * data[i];
		}
		return res;
	}
}