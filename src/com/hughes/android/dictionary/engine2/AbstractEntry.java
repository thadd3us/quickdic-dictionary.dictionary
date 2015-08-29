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

import com.hughes.util.raf.Serializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractEntry {
    final EntrySource entrySource;

    abstract Subtype getSubtype();
    
    protected AbstractEntry(EntrySource entrySource) {
        this.entrySource = entrySource;
    }

    abstract void writeChild(final DataOutputStream out) throws IOException;
    
    static class EntrySerializer implements Serializer<AbstractEntry> {
        
        final Dictionary dictionary;
        
        EntrySerializer(Dictionary dictionary) {
            this.dictionary = dictionary;
        }

        @Override
        public void write(DataOutputStream out, AbstractEntry t) throws IOException {
            out.writeShort(t.entrySource.index());
            out.writeShort(t.getSubtype().ordinal());
            t.writeChild(out);
        }

        @Override
        public AbstractEntry read(DataInputStream in) throws IOException {
            final short entrySouceIndex = in.readShort();
            final EntrySource entrySource = dictionary.sources.get(entrySouceIndex);
            final short subtypeIndex = in.readShort();
            return Subtype.values()[subtypeIndex].create(dictionary, entrySource, in);
        }
        
    };
    
    enum Subtype {
        PAIR {
            @Override
            public AbstractEntry create(Dictionary dictionary, EntrySource entrySource, DataInputStream in) throws IOException {
                return new PairEntry(entrySource, in);
            }
        };
        
        public abstract AbstractEntry create(Dictionary dictionary, EntrySource entrySource, DataInputStream in) throws IOException;
    };
}
