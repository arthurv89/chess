package nl.arthurvlug.chess.engine.ace.transpositiontable;

import com.google.common.base.Preconditions;

public class TranspositionTable {
	private HashElement[] hash_table;
	public static final int hashfEXACT = 1;
	public static int hashfALPHA = 2;
	public static int hashfBETA = 4;

	private final int TableSizeMask;

	public TranspositionTable(int hashTableSize) {
		// Check that the hash table length is a power of 2
		Preconditions.checkArgument((hashTableSize & (hashTableSize - 1)) == 0);

		hash_table = new HashElement[hashTableSize];
		TableSizeMask = hashTableSize-1;
	}

	public HashElement get(final int zobristHash) {
		int hashKey = hashKey(zobristHash);
		final HashElement hashElement = hash_table[hashKey];
		if (hashElement != null && hashElement.key == zobristHash) {
			return hashElement;

			// TODO: Find out what this means
//			RememberBestMove();
		}
		return null;
	}

	private int hashKey(final int zobristKey) {
		return zobristKey & TableSizeMask;
	}

	public void set(int depth, int val, int hashf, final Integer bestMove, final int zobristHash) {
		int hashKey = hashKey(zobristHash);
		HashElement phashe = hash_table[hashKey];
		if(phashe == null) {
			phashe = new HashElement();
		}
		phashe.key = zobristHash;
		phashe.val = val;
		phashe.best = bestMove;
		phashe.hashf = hashf;
		phashe.depth = depth;
		hash_table[hashKey] = phashe;
	}
}
