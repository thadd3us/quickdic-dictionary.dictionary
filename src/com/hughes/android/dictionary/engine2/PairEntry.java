// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.hughes.android.dictionary.engine2;

import com.hughes.android.dictionary.engine.RowMatchType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PairEntry extends AbstractEntry {

    public final List<Pair> pairs;

    public PairEntry(final EntrySource entrySource) {
        super(entrySource);
        pairs = new ArrayList<Pair>(1);
    }

    public PairEntry(final EntrySource entrySource, final String lang1, final String lang2) {
        this(entrySource);
        this.pairs.add(new Pair(lang1, lang2));
    }

    public PairEntry(final EntrySource entrySource, final DataInputStream in)
            throws IOException {
        super(entrySource);
        final int size = in.readInt();
        pairs = new ArrayList<PairEntry.Pair>(size);
        for (int i = 0; i < size; ++i) {
            final String lang1 = in.readUTF();
            final String lang2 = in.readUTF();
            pairs.add(new Pair(lang1, lang2));
        }
    }
    
    @Override
    void writeChild(DataOutputStream out) throws IOException {
        out.writeInt(pairs.size());
        for (int i = 0; i < pairs.size(); ++i) {
            assert pairs.get(i).lang1.length() > 0;
            out.writeUTF(pairs.get(i).lang1);
            out.writeUTF(pairs.get(i).lang2);
        }
    }

    public RowMatchType matches(final List<String> searchTokens,
            final Pattern orderedMatchPattern,
            final boolean swapPairEntries) {
        final int side = swapPairEntries ? 1 : 0;
        final String[] pairSides = new String[pairs.size()];
        for (int i = 0; i < pairs.size(); ++i) {
            pairSides[i] = pairs.get(i).get(side).toLowerCase();
        }
        for (int i = searchTokens.size() - 1; i >= 0; --i) {
            final String searchToken = searchTokens.get(i);
            boolean found = false;
            for (final String pairSide : pairSides) {
                found |= pairSide.contains(searchToken);
            }
            if (!found) {
                return RowMatchType.NO_MATCH;
            }
        }
        for (final String pairSide : pairSides) {
            if (orderedMatchPattern.matcher(pairSide).find()) {
                return RowMatchType.ORDERED_MATCH;
            }
        }
        return RowMatchType.BAG_OF_WORDS_MATCH;
    }

    public String getRawText(final boolean compact) {
        if (compact) {
            return this.pairs.get(0).toStringTab();
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.pairs.size(); ++i) {
            if (i > 0) {
                builder.append(" | ");
            }
            builder.append(this.pairs.get(i).lang1);
        }
        builder.append("\t");
        for (int i = 0; i < this.pairs.size(); ++i) {
            if (i > 0) {
                builder.append(" | ");
            }
            builder.append(this.pairs.get(i).lang2);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return getRawText(false);
    }

    // -----------------------------------------------------------------------

    public static final class Pair {

        public final String lang1;
        public final String lang2;

        public Pair(final String lang1, final String lang2) {
            this.lang1 = lang1;
            this.lang2 = lang2;
            if (!(lang1.trim().length() > 0 && lang2.trim().length() > 0)) {
                System.err.println("poop");
            }
            assert lang1.trim().length() > 0 || lang2.trim().length() > 0 : "Empty pair!!!";
            assert lang1.trim().length() > 0 && lang2.trim().length() > 0 : "Empty pair!!!";
        }

        public Pair(final String lang1, final String lang2, final boolean swap) {
            this(swap ? lang2 : lang1, swap ? lang1 : lang2);
        }

        public String toString() {
            return lang1 + " :: " + lang2;
        }

        public String toStringTab() {
            return lang1 + "\t" + lang2;
        }

        public String get(int i) {
            if (i == 0) {
                return lang1;
            } else if (i == 1) {
                return lang2;
            }
            throw new IllegalArgumentException();
        }

    }

    @Override
    Subtype getSubtype() {
        return Subtype.PAIR;
    }

}
