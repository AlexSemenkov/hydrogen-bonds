package com.asemenkov.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author asemenkov
 * @since Jun 8, 2018
 */
public class DecimalFormatters {

    private static final DecimalFormatSymbols DFS_US = new DecimalFormatSymbols(Locale.US);
    public static final DecimalFormat DF_1_12 = new DecimalFormat("#.############", DFS_US);
    public static final DecimalFormat DF_1_8 = new DecimalFormat("#.########", DFS_US);
    public static final DecimalFormat DF_1_4 = new DecimalFormat("#.####", DFS_US);
    public static final DecimalFormat DF_1_3 = new DecimalFormat("#.###", DFS_US);

}
