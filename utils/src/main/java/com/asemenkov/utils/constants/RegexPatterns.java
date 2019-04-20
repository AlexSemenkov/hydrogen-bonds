package com.asemenkov.utils.constants;

import java.util.regex.Pattern;

/**
 * @author asemenkov
 * @since Apr 22, 2018
 */
public class RegexPatterns {

    public static final Pattern INTEGER_PATTERN = Pattern.compile("\\D*(\\d+)\\D*");
    public static final Pattern STRING_PATTERN = Pattern.compile("\\W*(\\w+)\\W*");
    public static final Pattern FLOAT_PATTERN = Pattern.compile("\\s*(-?\\d*.\\d+)\\D*");

}
