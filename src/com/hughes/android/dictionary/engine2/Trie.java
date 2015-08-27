package com.hughes.android.dictionary.engine2;

import java.util.Arrays;

public class Trie {
    // For a list of nodes.
    int[] nodeStarts;
    short[] nodeLengths;
    
    byte[] childKeyBytes;
    int[] childNextNodeIndices;
    
    public int find(byte[] bytes) {
        int node = 0;
        int bytesIndex = 0;
        while (bytesIndex < bytes.length) {
            final int start = nodeStarts[node];
            final int end = start + nodeLengths[node];
            int searchResult = Arrays.binarySearch(childKeyBytes, start, end, bytes[bytesIndex]);
            if (searchResult < 0) {
                return -1;
            }
            node = childNextNodeIndices[searchResult];
            ++bytesIndex;
        }
        return node;
    }
}
