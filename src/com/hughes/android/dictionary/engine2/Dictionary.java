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

import com.hughes.android.dictionary.DictionaryInfo;
import com.hughes.android.dictionary.engine.PairEntry;
import com.hughes.util.CachingList;
import com.hughes.util.raf.RAFList;
import com.hughes.util.raf.RAFListSerializer;
import com.hughes.util.raf.RAFSerializable;
import com.hughes.util.raf.compressed.CompressedList;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dictionary {

    static final int CURRENT_DICT_VERSION = 7;
    static final String END_OF_DICTIONARY = "END OF DICTIONARY";

    // persisted
    final int dictFileVersion;
    final long creationMillis;
    public final String dictInfo;
    public final List<AbstractEntry> entries;
    public final List<EntrySource> sources;
    public final List<Index> indices;

    /**
     * dictFileVersion 1 adds: <li>links to sources? dictFileVersion 2 adds: <li>
     * counts of tokens in indices.
     */

    public Dictionary(final String dictInfo) {
        this.dictFileVersion = CURRENT_DICT_VERSION;
        this.creationMillis = System.currentTimeMillis();
        this.dictInfo = dictInfo;
        entries = new ArrayList<AbstractEntry>();
        sources = new ArrayList<EntrySource>();
        indices = new ArrayList<Index>();
    }

    public Dictionary(final RandomAccessFile raf) throws IOException {
        dictFileVersion = raf.readInt();
        if (dictFileVersion < 0 || dictFileVersion > CURRENT_DICT_VERSION) {
            throw new IOException("Invalid dictionary version: " + dictFileVersion);
        }
        creationMillis = raf.readLong();
        dictInfo = raf.readUTF();

        // Load the sources, then seek past them, because reading them later
        // disrupts the offset.
        try {
            final RAFList<EntrySource> rafSources = RAFList.create(raf, new EntrySource.Serializer(
                    this), raf.getFilePointer());
            sources = new ArrayList<EntrySource>(rafSources);
            raf.seek(rafSources.getEndOffset());

            entries = CachingList.create(
                    CompressedList.create(raf, new AbstractEntry.EntrySerializer(this), raf.getFilePointer()),
                    4096);
            indices = CachingList.createFullyCached(RAFList.create(raf, indexSerializer,
                    raf.getFilePointer()));
        } catch (RuntimeException e) {
            final IOException ioe = new IOException("RuntimeException loading dictionary");
            ioe.initCause(e);
            throw ioe;
        }
        final String end = raf.readUTF();
        if (!end.equals(END_OF_DICTIONARY)) {
            throw new IOException("Dictionary seems corrupt: " + end);
        }
    }

    public void print(final PrintStream out) {
        out.println("dictInfo=" + dictInfo);
        for (final EntrySource entrySource : sources) {
            out.printf("EntrySource: %s %d\n", entrySource.name, entrySource.numEntries);
        }
        out.println();
        for (final Index index : indices) {
            out.printf("Index: %s %s\n", index.language, index.name);
            index.print(out);
            out.println();
        }
    }

    public DictionaryInfo getDictionaryInfo() {
        final DictionaryInfo result = new DictionaryInfo();
        result.creationMillis = this.creationMillis;
        result.dictInfo = this.dictInfo;
        for (final Index index : indices) {
            result.indexInfos.add(index.getIndexInfo());
        }
        return result;
    }

    public static DictionaryInfo getDictionaryInfo(final File file) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            final Dictionary dict = new Dictionary(raf);
            final DictionaryInfo dictionaryInfo = dict.getDictionaryInfo();
            dictionaryInfo.uncompressedFilename = file.getName();
            dictionaryInfo.uncompressedBytes = file.length();
            raf.close();
            return dictionaryInfo;
        } catch (IOException e) {
            return null;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
