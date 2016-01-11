
package com.github.umeshkrpatel.LogMeter.data;

import android.content.Context;
import android.support.v4.util.LruCache;

import java.util.ArrayList;

/**
 * {@link LruCache} Holding the Contact Name and Photo_ID for Number
 *
 * @author Umesh Kumar Patel
 */
public final class ContactCache extends LruCache<String, ArrayList<String> > {

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
     * @return Cached Name {@link String}
     */
    public String getName(final Context context, final String key, final String format) {
        ArrayList<String> strings = get(key);
        if (strings == null) {
            strings = ContactFinder.findContactForNumber(context, key, format);
        }
        if (format!=null)
            return String.format(format, strings.get(0));
        else
            return strings.get(0);
    }

    /**
     * Return get(key) formatted with format.
     *
     * @param key    key
     * @param format format
     * @return Cached Photo_ID {@link String}
     */
    public String getPhotoUri(final Context context, final String key, final String format) {
        ArrayList<String> strings = get(key);
        if (strings == null) {
            strings = ContactFinder.findContactForNumber(context, key, format);
        }
        return strings.get(1);
    }
}
