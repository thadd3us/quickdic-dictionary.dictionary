package com.hughes.android.dictionary.engine2;

import java.util.regex.Pattern;

public class Regex {
    
    public static final Pattern NON_CHAR = Pattern.compile("[^\\p{L}\\p{M}\\p{N}]+");


}
