package nl.arthurvlug.chess;

public class TreeNode {
	private TreeNode parent;
	private String name;
	private int value = 0;

	public TreeNode(String name, TreeNode parent) {
		this.name = name;
		this.parent = parent;
		
		switch (name) {
			case "1": value = 3; break;
			case "2": value = 1; break;
			case "3": value = 2; break;
		}
	}

	public static TreeNode root() {
		return new TreeNode("", null);
	}
	
	@Override
	public String toString() {
		String string = name;
		if(parent != null && !parent.name.isEmpty()) {
			string = parent.toString() + " " + string;
		}
		return string;
	}
}
