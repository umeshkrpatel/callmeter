
package com.github.umeshkrpatel.LogMeter.data;

import android.content.Context;
import android.support.v4.util.LruCache;

/**
 * {@link LruCache} holding number to name entries.
 *
 * @author flx
 */
public final class ContactCache extends LruCache<String, String> {

    /**
     * Maximum cache size.
     */
    private static final int MAX_CACHE_SIZE = 100;

    /**
     * Single instance.
     */
    private static ContactCache instance;

    /**
     * hide public constructor.
     */
    private ContactCache() {
        super(MAX_CACHE_SIZE);
    }

    /**
     * Get the cache.
     *
     * @return the single {@link ContactCache} instance
     */
    public static ContactCache getInstance() {
        if (instance == null) {
            instance = new ContactCache();
        }
        return instance;
    }

    /**
     * Return get(key) formatted with format.
     *
     * @param key    key
     * @param format format
     * @return formatted {@link String}
     */
    public String get(final Context context, final String key, final String format) {
        String s = get(key);
        if (s == null) {
            s = ContactFinder.findContactForNumber(context, key, format);
        }
        return String.format(format, s);
    }
}
