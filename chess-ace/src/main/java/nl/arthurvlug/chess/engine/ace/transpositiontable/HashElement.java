package nl.arthurvlug.chess.engine.ace.transpositiontable;

public class HashElement {
	public int key;
	public Object best;
	public int val;
	public int hashf;
	public int depth;
	public int flags;
}
