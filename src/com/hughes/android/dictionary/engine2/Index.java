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

/**
 * 
 */

package com.hughes.android.dictionary.engine2;

import com.hughes.android.dictionary.DictionaryInfo;
import com.hughes.android.dictionary.DictionaryInfo.IndexInfo;
import com.hughes.android.dictionary.engine2.Language;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Index {

    public final Dictionary dictionary;
    public final Language language;
    public final String name;
    
    int mainTokenCount = 0;
    
    Index(Dictionary dictionary, Language language, final String name) {
        this.dictionary = dictionary;
        this.language = language;
        this.name = name;
    }

    public List<EntryRef> lookup(final String text, final AtomicBoolean cancel) {
        // Tokenize text.
        // Lookup each normalized token to get lists of EntryRefs.
        // Merge and rank list of EntryRefs
        // Group by language.
        return null;
    }

    public IndexInfo getIndexInfo() {
        return new DictionaryInfo.IndexInfo(language.getIsoCode(), 0, mainTokenCount);
    }

    public void print(PrintStream out) {
        // TODO Auto-generated method stub
        
    }
    
    private static final Charset UTF8 = Charset.forName("UTF8");
    public static byte[] toUTF8(final String s) {
        return s.getBytes(UTF8);
    }
    
}
