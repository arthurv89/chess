package nl.arthurvlug.chess;

import lombok.Getter;

public class TreeNode implements Comparable<TreeNode> {
	@Getter
	private TreeNode parent;
	private String move;
	@Getter
	private int value = 0;
	
	public String getAncestorsAndCurrent() {
		return parent + " " + move;
	}
	
	public TreeNode() { }

	public TreeNode(String move, TreeNode parent) {
		this.move = move;
		this.parent = parent;
	}
	
	public void setScore() {
		switch (move) {
			case "A": value = 3; break;
			case "B": value = 1; break;
			case "C": value = 2; break;
		}
	}

	public static TreeNode root() {
		return new TreeNode("",  null);
	}
	
	@Override
	public String toString() {
		String string = "";
		if(parent != null) {
			string = parent.toString() + " ";
		}
		return string + move + " (v=" + value + ")";
	}

	@Override
	public int compareTo(TreeNode o) {
		return value - o.value;
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
}
