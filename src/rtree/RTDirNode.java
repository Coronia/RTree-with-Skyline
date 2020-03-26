// Class for index node

package rtree;

import java.util.ArrayList;
import java.util.List;

public class RTDirNode extends RTNode {
	protected List<RTNode> children;
	
	public RTDirNode(RTree rtree, RTNode parent, int level) {
		super(rtree, parent, level);
		children = new ArrayList<>();
	}
	
	public RTNode getChild(int index) { return children.get(index); }
	
	@Override
	public RTDataNode chooseLeaf(Rectangle rec) { // Choose the leaf to be split (data node)
		int index = 0;
		switch (rtree.getTreeType()) { // Different type of split strategies
		case Constants.RTREE_LINEAR:
			break;
		case Constants.RTREE_QUADRATIC:
			break;
		case Constants.RTREE_EXPONENTIAL:
			index = findEnlarge(rec);
			break;
		case Constants.RSTAR:
			if (level == 1) { index = findOverlap(rec); }
			else { index = findEnlarge(rec); }
			break;
		default:
			throw new IllegalStateException("Invalid tree type.");
		}
		insertIndex = index;
		return getChild(index).chooseLeaf(rec);
	}

	private int findOverlap(Rectangle rec) { // Find the node with the least overlap area with its data
		double overlap = Double.POSITIVE_INFINITY;
		int sel = -1;
		for (int i = 0; i < usedSpace; i ++) {
			RTNode node = getChild(i);
			double ol = 0;
			for (int j = 0; j < node.datas.length; j ++) {
				ol += rec.intersectArea(node.datas[j]);
			}
			
			if (ol < overlap) {
				overlap = ol;
				sel = i;
			}
			else if (ol == overlap) { // If having same overlap areas then choose the smaller rectangle
				double area1 = datas[i].getUnion(rec).getArea() - datas[i].getArea();
				double area2 = datas[sel].getUnion(rec).getArea() - datas[sel].getArea();
				if (area1 == area2) { sel = datas[sel].getArea() <= datas[i].getArea() ? sel : i; }
				else { sel = area1 < area2 ? i : sel; }
			}
		}
		return sel;
	}
	
	private int findEnlarge(Rectangle rec) { // Find the node with the largest area enlargement
		double area = Double.POSITIVE_INFINITY;
		int sel = -1;
		for (int i = 0; i < usedSpace; i ++) {
			double enlarge = datas[i].getUnion(rec).getArea() - datas[i].getArea();
			if (enlarge < area) {
				area = enlarge;
				sel = i;
			} else if (enlarge == area) { sel = datas[sel].getArea() < datas[i].getArea() ? sel : i; }
		}
		return sel;
	}
	
	public void adjustTree(RTNode n1, RTNode n2) { // Adjust the tree recursively after insertion
		datas[insertIndex] = n1.getNodeRectangle();
		children.set(insertIndex, n1);
		if (n2 != null) { insert(n2); }
		else if (!isRoot()) {
			RTDirNode parent = (RTDirNode) getParent();
			parent.adjustTree(this, null);
		}
	}
	
	protected boolean insert(RTNode node) {
		if (usedSpace < rtree.getCapacity()) {
			datas[usedSpace ++] = node.getNodeRectangle();
			children.add(node);
			node.parent = this;
			RTDirNode parent = (RTDirNode) getParent();
			if (parent != null) { parent.adjustTree(this, null); }
			return false;
		} else { // Non-leaf needs to be split
			RTDirNode[] a = splitIndex(node);
			RTDirNode n1 = a[0];
			RTDirNode n2 = a[1];
			if (isRoot()) { // Set a new root
				RTDirNode newRoot = new RTDirNode(rtree, Constants.NULL, level + 1);
				newRoot.addData(n1.getNodeRectangle());
				newRoot.addData(n2.getNodeRectangle());
				newRoot.children.add(n1);
				newRoot.children.add(n2);
				n1.parent = newRoot;
				n2.parent = newRoot;
				rtree.setRoot(newRoot);
			} else {
				RTDirNode p = (RTDirNode) getParent();
				p.adjustTree(n1, n2);
			}
		}
		return true;
	}
	
	private RTDirNode[] splitIndex(RTNode node) { // Split index node
		int[][] group = null;
		switch (rtree.getTreeType()) { // Different type of split strategies
		case Constants.RTREE_LINEAR:
			break;
		case Constants.RTREE_QUADRATIC:
			group = quadraticSplit(node.getNodeRectangle());
			children.add(node);
			node.parent = this;
			break;
		case Constants.RTREE_EXPONENTIAL:
			break;
		case Constants.RSTAR:
			break;
		default:
			throw new IllegalStateException("Invalid tree type.");
		}
		
		RTDirNode index1 = new RTDirNode(rtree, parent, level);
		RTDirNode index2 = new RTDirNode(rtree, parent, level);
		int[] group1 = group[0];
		int[] group2 = group[1];
		for (int i = 0; i < group1.length; i ++) {
			index1.addData(datas[group1[i]]);
			index1.children.add(this.children.get(group1[i]));
			this.children.get(group1[i]).parent = index1;
		}
		for (int i = 0; i < group2.length; i ++) {
			index2.addData(datas[group2[i]]);
			index2.children.add(this.children.get(group2[i]));
			this.children.get(group2[i]).parent = index2;
		}
		return new RTDirNode[] {index1, index2};
	}
	
	@Override
	protected RTDataNode findLeaf(Rectangle rec) {
		for (int i = 0; i < usedSpace; i ++) {
			if (datas[i].enclosure(rec)) {
				deleteIndex = i;
				RTDataNode leaf = children.get(i).findLeaf(rec);
				if (leaf != null) { return leaf; }
			}
		}
		return null;
	}
	
	@Override
	protected List<Rectangle> searchLeaf(Rectangle rec) {
		List<Rectangle> res = new ArrayList<>();
		for (int i = 0; i < usedSpace; i ++) {
			if (rec.getUnion(datas[i]) != null) {
				res.addAll(children.get(i).searchLeaf(rec));
			}
		}
		return res;
	}
}