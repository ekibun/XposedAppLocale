package com.flo354.xposed.applocale;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Manages a list of valid locales for the system
 */
public class LocaleList {

    /*
     * From AOSP code - listing available languages to present to the user
     */
    private static class LocaleInfo implements Comparable<LocaleInfo> {
        static final Collator sCollator = Collator.getInstance();

        String label;
        Locale locale;

        public LocaleInfo(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }

        @Override
        public String toString() {
            return this.label;
        }

        @Override
        public int compareTo(@NonNull LocaleInfo another) {
            return sCollator.compare(this.label, another.label);
        }
    }

    private String[] localeCodes;

    private String[] localeDescriptions;

    public LocaleList(Context context, String defaultLabel) {
        final Locale[] locales = Locale.getAvailableLocales();
        final int origSize = locales.length;
        final LocaleInfo[] preprocess = new LocaleInfo[origSize];
        int finalSize = 0;
        for (final Locale l : locales) {
            preprocess[finalSize++] = new LocaleInfo(toTitleCase(l.getDisplayName()), l);
        }

        final LocaleInfo[] localeInfos = new LocaleInfo[finalSize];
        System.arraycopy(preprocess, 0, localeInfos, 0, finalSize);
        Arrays.sort(localeInfos);

        localeCodes = new String[localeInfos.length + 1];
        localeDescriptions = new String[localeInfos.length + 1];
        localeCodes[0] = Common.DEFAULT_LOCALE;
        localeDescriptions[0] = defaultLabel;
        for (int i = 1; i < finalSize + 1; i++) {
            localeCodes[i] = localeInfos[i - 1].locale.toLanguageTag();
            localeDescriptions[i] = localeInfos[i - 1].label;
        }
    }

    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String getDisplayName(Locale loc) {
        return loc.getDisplayName(loc);
    }

    private static String getLocaleCode(Locale loc) {
        String result = loc.getLanguage();
        if (loc.getCountry().length() > 0)
            result += "_" + loc.getCountry();
        if (loc.getVariant().length() > 0)
            result += "_" + loc.getVariant();
        return result;
    }

    /**
     * Retrieve the locale code at a specific position in the list.
     */
    public String getLocale(int pos) {
        return localeCodes[pos];
    }

    /**
     * Retrieve the position where the specified locale code is, or 0 if it was
     * not found.
     */
    public int getLocalePos(String locale) {
        for (int i = 1; i < localeCodes.length; i++) {
            if (localeCodes[i].equals(locale))
                return i;
        }
        return 0;
    }

    public String[] getLocaleCodes() {
        return localeCodes;
    }

    /**
     * Retrieve an ordered list of the locale descriptions
     */
    public List<String> getDescriptionList() {
        return Arrays.asList(localeDescriptions);
    }

}
