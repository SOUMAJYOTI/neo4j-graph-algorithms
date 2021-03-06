package org.neo4j.graphalgo.impl.msbfs;

/**
 * 32-bit wide BitSets per node.
 * This class represents the fixed-size bit field as described in [1].
 * It's equivalent to a {@code List<BitSet>}, but implemented more efficiently
 * as a sparse {@code int[]}.
 * The {@code ω} parameter is fixed to 32, that is, only 32 sources can be
 * tracked with this bit set.
 * The MS-BFS algorithm runs multiple instances in parallel if the number of
 * sources exceed 32.
 * <p>
 * [1]: <a href="http://www.vldb.org/pvldb/vol8/p449-then.pdf">The More the Merrier: Efficient Multi-Source Graph Traversal</a>
 */
final class MultiBitSet32 {

    private final int[] bits;

    /**
     * Creates a new bit set for {@code nodeCount} nodes.
     *
     * @throws IllegalArgumentException if there isn't enough memory to hold the data.
     */
    MultiBitSet32(int nodeCount) {
        try {
            bits = new int[nodeCount];
        } catch (OutOfMemoryError | NegativeArraySizeException e) {
            IllegalArgumentException iae =
                    new IllegalArgumentException("Invalid nodeCount: " + nodeCount);
            iae.addSuppressed(e);
            throw iae;
        }
    }

    /**
     * Sets a particular bit (in [0, 32)) for a node.
     */
    void setBit(int nodeId, int bit) {
        assert bit < 32;
        bits[nodeId] |= (1 << bit);
    }

    /**
     * Sets all bits for a node.
     */
    void set(int nodeId, int bit) {
        bits[nodeId] = bit;
    }

    /**
     * Returns the BitSet for {@code nodeId} as a {@code long}.
     */
    int get(int nodeId) {
        return bits[nodeId];
    }

    /**
     * Returns the next node that has some bits set and is at least {@code fromNodeId}.
     * May return {@code fromNodeId} itself, if it has bits set.
     * If there are no such nodes, return -1.
     * If there aren't any nodes that have any bit set (all sets are empty), return -2.
     */
    int nextSetNodeId(int fromNodeId) {
        for (int i = fromNodeId; i < bits.length; i++) {
            if (bits[i] != 0) {
                return i;
            }
        }
        return -2 + Integer.signum(fromNodeId);
    }
}
