package com.legitmod.feature;

public class NameProtect {
    public static boolean enabled  = false;
    public static String  fakeName = "Ezz";

    /** Replace all occurrences of the real username with fakeName in display strings. */
    public static String filter(String text, String realName) {
        if (!enabled || text == null || realName == null || realName.isEmpty()) return text;
        return text.replace(realName, fakeName);
    }
}
