package nl.arthurvlug.chess;

import lombok.Getter;
import lombok.Setter;

public class TreeNode implements Comparable<TreeNode> {
	@Getter
	private TreeNode parent;
	@Getter
	private String move;
	@Getter
	@Setter
	private int score = 0;
	
//	public String getAncestorsAndCurrent() {
//		return parent + " " + move;
//	}
//	
	public TreeNode() { }

	public TreeNode(String move, TreeNode parent) {
		this.move = move;
		this.parent = parent;
	}
	
	public static TreeNode ROOT_MIN = new TreeNode("",  null) {{
		setScore(Integer.MIN_VALUE);
	}};
	
	public static TreeNode ROOT_MAX = new TreeNode("",  null) {{
		setScore(Integer.MAX_VALUE);
	}};
	
	String getCurrentAndAncestors() {
		String s = "";
		s += (parent != null) ? parent.getCurrentAndAncestors() + "" : "";
		s += move;
		return s;
	}
	
	@Override
	public String toString() {
		return getCurrentAndAncestors() + " (v=" + score + ")";
	}

	@Override
	public int compareTo(TreeNode o) {
		return score - o.score;
	}
	
	@Override
	public boolean equals(Object obj) {
		TreeNode other = (TreeNode) obj;
		return getCurrentAndAncestors().equals(other.getCurrentAndAncestors());
	}
}
