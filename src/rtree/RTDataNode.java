// Class for data node

package rtree;

import java.util.ArrayList;
import java.util.List;

public class RTDataNode extends RTNode {
	public RTDataNode(RTree rtree, RTNode parent) { super(rtree, parent, 0); }
	
	public boolean insert(Rectangle rec) {
		if (usedSpace < rtree.getCapacity()) {
			datas[usedSpace ++] = rec;
			RTDirNode parent = (RTDirNode) getParent();
			if (parent != null) { parent.adjustTree(this, null); }
		} else {
			RTDataNode[] splitNodes = splitLeaf(rec);
			RTDataNode l1 = splitNodes[0];
			RTDataNode l2 = splitNodes[1];
			if (isRoot()) { // Root is full, needs to split a new root
				RTDirNode rdir = new RTDirNode(rtree, Constants.NULL, level + 1);
				rtree.setRoot(rdir);
				rdir.addData(l1.getNodeRectangle());
				rdir.addData(l2.getNodeRectangle());
				l1.parent = rdir;
				l2.parent = rdir;
				rdir.children.add(l1);
				rdir.children.add(l2);
			} else {
				RTDirNode parentNode = (RTDirNode) getParent();
				parentNode.adjustTree(l1, l2);
			}
		}
		return true;
	}
	
	public RTDataNode[] splitLeaf(Rectangle rec) { // Split the leaf when the data amount reach its maximum
		int[][] group = null;
		switch(rtree.getTreeType()) { // Different type of split strategies
			case Constants.RTREE_LINEAR:
				break;
			case Constants.RTREE_QUADRATIC:
				group = quadraticSplit(rec);
				break;
			case Constants.RTREE_EXPONENTIAL:
				break;
			case Constants.RSTAR:
				break;
			default:
				throw new IllegalArgumentException("Invalid tree type.");
		}
		
		RTDataNode l1 = new RTDataNode(rtree, parent);
		RTDataNode l2 = new RTDataNode(rtree, parent);
		int[] group1 = null, group2 = null;
		if (group != null) {
			group1 = group[0];
			group2 = group[1];
			for (int i = 0; i < group1.length; i ++) {
				l1.addData(datas[group1[i]]);
			}
			for (int i = 0; i < group2.length; i ++) {
				l2.addData(datas[group2[i]]);
			}
		}
		return new RTDataNode[] {l1, l2};
	}
	
	@Override
	public RTDataNode chooseLeaf(Rectangle rec) {
		insertIndex = usedSpace;
		return this;
	}
	
	protected int delete(Rectangle rec) {
		for (int i = 0; i < usedSpace; i ++) {
			if (datas[i].equals(rec)) {
				deleteData(i);
				List<RTNode> deleteEntries = new ArrayList<>(); // The list for the data in the deleted node
				condenseTree(deleteEntries);
				for (int j = 0; j < deleteEntries.size(); j ++) { // Relocate these data
					RTNode node = deleteEntries.get(j);
					if (node.isLeaf()) { // Insert into leaf directly
						for (int k = 0; k < node.usedSpace; k ++) {
							rtree.insert(node.datas[k]);
						}
					} else { // Traverse the nodes, find leaves and insert
						List<RTNode> traverseNodes = rtree.traversePost(node);
						for (int k = 0; k < traverseNodes.size(); k ++) {
							RTNode traverseNode = traverseNodes.get(k);
							if (traverseNode.isLeaf()) {
								for (int t = 0; t < traverseNode.usedSpace; t ++) {
									rtree.insert(traverseNode.datas[t]);
								}
							}
						}
					}
				}
				return deleteIndex;
			}
		}
		return -1;
	}
	
	@Override
	protected RTDataNode findLeaf(Rectangle rec) {
		for (int i = 0; i < usedSpace; i ++) {
			if (datas[i].enclosure(rec)) {
				deleteIndex = i;
				return this;
			}
		}
		return null;
	}
	
	@Override
	protected List<Rectangle> searchLeaf(Rectangle rec) {
		List<Rectangle> res = new ArrayList<>();
		for (int i = 0; i < usedSpace; i ++) {
			if (rec.enclosure(datas[i])) {
				res.add(datas[i]);
			}
		}
		return res;
	}
}