package rtree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.jfree.ui.RefineryUtilities;

public class RTree {
	private RTNode root;
	private int treeType;
	private int capacity = -1;
	private double fillFactor = -1;
	private int dimension;
	private List<Rectangle> skyline = new ArrayList<>();
	
	public RTree(int capacity, double fillFactor, int treeType, int dimension) {
		this.capacity = capacity;
		this.fillFactor = fillFactor;
		this.treeType = treeType;
		this.dimension = dimension;
		root = new RTDataNode(this, Constants.NULL);
	}
	
	public int getDimension() { return dimension; }
	
	public void setRoot(RTNode root) { this.root = root; }
	
	public double getFillFactor() { return fillFactor; }
	
	public int getCapacity() { return capacity; }
	
	public int getTreeType() { return treeType; }
	
	public boolean insert(Rectangle rec) {
		if (rec == null) { throw new IllegalArgumentException("Rectangle cannot be null."); }
		if (rec.getLow().getDimension() != getDimension()) { throw new IllegalArgumentException("Rectangle dimension different than RTree dimension."); }
		RTDataNode leaf = root.chooseLeaf(rec);
		boolean res = leaf.insert(rec);
		
		// Uncomment this to update skyline for each insertion (program will be slowed when doing a massive deletion)
		/*List<Rectangle> sky = skyline();
		if (!sky.equals(skyline)) {
			skyline = sky;
			System.out.println(skyline);
		}*/
		return res;
	}
	
	public List<Rectangle> search(Rectangle rec) {
		if (rec == null) { throw new IllegalArgumentException("Rectangle cannot be null."); }
		if (rec.getLow().getDimension() != getDimension()) { throw new IllegalArgumentException("Rectangle dimension different than RTree dimension."); }
		List<Rectangle> res = root.searchLeaf(rec);
		
		// Another method for searching
		/*List<RTNode> nodes = traversePost(root);
		List<Rectangle> res = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i ++) {
			for (int j = 0; j < nodes.get(i).datas.length; j ++) {
				if (nodes.get(i).datas[j] != null && rec.enclosure(nodes.get(i).datas[j])) {
					res.add(nodes.get(i).datas[j]);
				}
			}
		}*/
		return res;
	}
	
	public int delete(Rectangle rec) {
		if (rec == null) { throw new IllegalArgumentException("Rectangle cannot be null."); }
		if (rec.getHigh().getDimension() != getDimension()) { throw new IllegalArgumentException("Rectangle dimension different than RTree dimension."); }
		RTDataNode leaf = root.findLeaf(rec);
		int res = -1;
		if (leaf != null) { res = leaf.delete(rec); }
		
		// Uncomment this to update skyline for each insertion (program will be slowed when doing a massive deletion)
		/*List<Rectangle> sky = skyline();
		if (!sky.equals(skyline)) {
			skyline = sky;
			System.out.println(skyline);
		}*/
		return res;
	}
	
	public List<Rectangle> skyline() { // Compute the skyline of the current tree with BBS
		List<Rectangle> res = new ArrayList<>();
		PriorityQueue<Rectangle> heap = new PriorityQueue<>();
		HashMap<Rectangle, RTNode> record = new HashMap<>(); // Indices of node and corresponding rectangle
		if (root instanceof RTDataNode) { // Only one node in the tree
			for (int i = 0; i < root.usedSpace; i ++) {
				if (isDominate(res, root.datas[i])) { heap.add(root.datas[i]); }
				else { res.add(root.datas[i]); }
			}
			return res;
		}
		RTDirNode r = (RTDirNode) root;
		for (int i = 0; i < r.usedSpace; i ++) { // Include all data from root in the heap
			heap.add(r.datas[i]);
			record.put(r.datas[i], r.getChild(i));
		}
		
		while (!heap.isEmpty()) {
			Rectangle rec = heap.poll();
			if (!isDominate(res, rec)) { // Rectangle is not dominated by current skyline, continue
				if (record.get(rec) != null) { // The corresponding node is an index node
					RTNode r1 = record.get(rec);
					for (int i = 0; i < r1.usedSpace; i ++) {
						if (!isDominate(res, r1.datas[i])) { // The indices that haven't been dominated, might contain new skyline points
							heap.add(r1.datas[i]);
							if (r1 instanceof RTDirNode) {
								RTDirNode r2 = (RTDirNode) r1;
								record.put(r2.datas[i], r2.getChild(i));
							} else { record.put(r1.datas[i], null); }
						} 
					}
				} else { res.add(rec); } // Data node
			}
		}
		
		Collections.sort(res, new Comparator<Rectangle>() { // Sort skyline points by their x-axis
            @Override
            public int compare(Rectangle r1, Rectangle r2) {
            	if (r1.getLow().getCoordinate(0) > r2.getLow().getCoordinate(0)) { return 1; }
            	else if (r1.getLow().getCoordinate(0) < r2.getLow().getCoordinate(0)) { return -1; }
            	return 0;
            }
        });
		return res;
	}
	
	public boolean isDominate(List<Rectangle> list, Rectangle rec) { // Check if the rectangle is dominated by the given skyline points
		for (int i = 0; i < list.size(); i ++) {
			if (list.get(i).getLow().getCoordinate(0) <= rec.getLow().getCoordinate(0)) {
				if (list.get(i).getLow().getCoordinate(1) <= rec.getLow().getCoordinate(1)) { return true; }
			}
		}
		return false;
	}
	
	public List<RTNode> traversePost(RTNode root) { // Acquire all nodes from this tree
		if (root == null) { throw new IllegalArgumentException("Node cannot be null."); }
		List<RTNode> list = new ArrayList<>();
		list.add(root);
		if (!root.isLeaf()) {
			for (int i = 0; i < root.usedSpace; i ++) {
				list.addAll(traversePost(((RTDirNode) root).getChild(i)));
			}
		}
		return list;
	}
	
	public List<Rectangle> getPoints(RTNode root) { // Acquire all data points from this tree
		if (root == null) { throw new IllegalArgumentException("Node cannot be null."); }
		List<Rectangle> list = new ArrayList<>();
		if (!root.isLeaf()) {
			for (int i = 0; i < root.usedSpace; i ++) {
				list.addAll(getPoints(((RTDirNode) root).getChild(i)));
			}
		} else {
			for (int i = 0; i < root.usedSpace; i ++) {
				list.add(root.datas[i]);
			}
		}
		return list;
	}
	
	public static void main(String args[]) throws Exception {
		//RTree tree = new RTree(4, 0.4f, Constants.RTREE_LINEAR, 2);
		RTree tree = new RTree(4, 0.4f, Constants.RTREE_QUADRATIC, 2);
		//RTree tree = new RTree(4, 0.4f, Constants.RTREE_EXPONENTIAL, 2);
		//RTree tree = new RTree(4, 0.4f, Constants.RSTAR, 2);
		
		// Test sample
		/*double[] f = { 5, 30, 25, 35, 15, 38, 23, 50, 10, 23, 30, 28, 13, 10, 18, 15, 23, 10, 28, 20, 28, 30, 33, 40, 38,
				13, 43, 30, 35, 37, 40, 43, 45, 8, 50, 50, 23, 55, 28, 70, 10, 65, 15, 70, 10, 58, 20, 63, };*/
		
		BufferedReader file = new BufferedReader(new FileReader("greek-earthquakes-1964-2000.txt"));
		List<Double> f = new ArrayList<>();
		String s;
		while ((s = file.readLine()) != null) {
			String[] tmp = s.split(" ");
			for (int i = 0; i < tmp.length; i ++) {
				f.add(Double.parseDouble(tmp[i])); // Get points from the given file
			}
		}
		
		System.out.println("---------------------------------");
		System.out.println("Begin insert.");
		for (int i = 0; i < f.size();) {
			Point p = new Point(new double[] {f.get(i ++), f.get(i ++)});
			final Rectangle rec = new Rectangle(p);
			tree.insert(rec);
			
			// Uncomment this to print the data of the node after each insertion
			/*Rectangle[] recs = tree.root.datas;
			System.out.println("level: " + tree.root.level);
			for (int j = 0; j < recs.length; j ++) {
				System.out.println(recs[j]);
			}*/
		}
		System.out.println("---------------------------------");
		System.out.println("Insert finished.");
		
		// Uncomment this to print the structure of this tree
		/*List<RTNode> nodeList = tree.traversePost(tree.root);
		for (int i = 0; i < nodeList.size(); i ++) {
			System.out.println(nodeList.get(i));
		}*/
		
		// Uncomment this to do a search example
		/*System.out.println("Search rectangle 20, 20, 40, 40");
		Point pa = new Point(new double[] {20, 20});
		Point pb = new Point(new double[] {40, 40});
		List<Rectangle> searchResult = tree.search(new Rectangle(pa, pb));
		for (int j = 0; j < searchResult.size(); j ++) {
			System.out.println(searchResult.get(j));
		}*/
		
		// Print skyline
		System.out.println("Skyline of this tree");
		tree.skyline = tree.skyline();
		for (int j = 0; j < tree.skyline.size(); j ++) {
			System.out.println(tree.skyline.get(j));
		}
		
		// Plotting all the points and connect skyline points through line
		List<Rectangle> others = tree.getPoints(tree.root);
		for (int i = 0; i < tree.skyline.size(); i ++) {
			others.remove(tree.skyline.get(i));
		}
		Chart chart = new Chart("Skyline", tree.skyline, others);
		chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
		
		System.out.println("---------------------------------");
		System.out.println("Begin delete.");
		for (int i = 0; i < f.size();) {
			Point p = new Point(new double[] {f.get(i ++), f.get(i ++)});
			final Rectangle rec = new Rectangle(p);
			tree.delete(rec);
			
			// Uncomment this to print the data of the node after each deletion
			/*Rectangle[] recs = tree.root.datas;
			System.out.println("level: " + tree.root.level);
			for (int j = 0; j < recs.length; j ++) {
				System.out.println(recs[j]);
			}*/
		}
		System.out.println("---------------------------------");
		System.out.println("Delete finished.");
		
		// Check if there's remaining node after deletion
		List<RTNode> nodeList2 = tree.traversePost(tree.root);
		for (int i = 0; i < nodeList2.size(); i ++) {
			System.out.println(nodeList2.get(i));
		}
	}
}