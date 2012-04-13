package org.jscsi.target.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a utility class with static methods useful for dealing with
 * <i>key-value</i> pairs.
 * 
 * @author Andreas Ergenzinger
 */
public final class TextParameter {

    /**
     * Returns the <i>key-value</i> pairs contained in a null
     * character-separated text data segment in an array of {@link String}s.
     * <p>
     * If the parameter equals <code>null</code> an empty {@link List} will be returned.
     * 
     * @param keyValuePairs
     *            a login request or text negotiation text data segment
     * @return a {@link Vector} of {@link String}s containing the purged key
     *         value pairs
     */
    public static List<String> tokenizeKeyValuePairs(final String keyValuePairs) {
        final List<String> result = new Vector<String>();
        if (keyValuePairs == null)
            return result;
        final String[] split = keyValuePairs.split(TextKeyword.NULL_CHAR);
        for (int i = 0; i < split.length; ++i)
            if (split[i].length() > 0)// does not mean key-value pair is
                                      // RFC-conform
                result.add(split[i]);
        return result;
    }

    /**
     * Concatenates the <i>key-value</i> pair elements from the specified {@link Collection} to a
     * null-character-separated {@link String} that can
     * be sent as a text parameter data segment.
     * 
     * @param keyValuePairs
     *            a {@link Collection} of <i>key-value</i> pairs
     * @return null-character-separated {@link String} containing all elements
     */
    public static String concatenateKeyValuePairs(final Collection<String> keyValuePairs) {
        final StringBuilder sb = new StringBuilder();
        for (String s : keyValuePairs) {
            sb.append(s);
            sb.append(TextKeyword.NULL_CHAR);
        }
        return sb.toString();
    }

    /**
     * Returns the suffix of a specified {@link String}. The length of the
     * suffix is equal to the length of <i>string</i> minus the lenght of
     * <i>prefix</i>, but of course only if the beginning of <i>string</i> does
     * equal <i>prefix<i>. If <i>prefix</i> is not a prefix of <i>string</i>, <code>null</code> is returned.
     * 
     * @param string
     *            the {@link String} whose suffix we want to have returned
     * @param prefix
     *            a prefix of <i>string</i>
     * @return the suffix or <code>null</code>
     */
    public static String getSuffix(final String string, final String prefix) {
        if (string == null || prefix == null || prefix.length() > string.length())
            return null;
        final String stringPrefix = string.substring(0, prefix.length());
        if (stringPrefix.equals(prefix))
            return string.substring(prefix.length());
        return null;
    }

    /**
     * Splits a <i>key=value</i> pair and returns an array with the separated
     * <i>key</i> and <i>value</i> parts.
     * <p>
     * If the parameter does not match this required pattern, then <code>null</code> will be returned.
     * 
     * @param keyValuePair
     *            a {@link String} with a <i>key</i> prefix of length > 0, a '='
     *            in the middle and a <i>value</i> suffix of length > 0
     * @return array with the separated <i>key</i> and <i>value</i> parts or <code>null</code>.
     */
    public static String[] splitKeyValuePair(final String keyValuePair) {
        String[] split = keyValuePair.split(TextKeyword.EQUALS);
        if (split.length != 2 || split[0].length() == 0 || split[1].length() == 0)
            return null;
        return split;
    }

    /**
     * Splits a String of (one or more) values at the ',' signs and returns the
     * values in an array of Strings.
     * <p>
     * Returns <code>null</code> if <i>values</i> parameter is <code>null</code>.
     * 
     * @param values
     *            a comma-separated String of text parameter values
     * @return a String array of values or <code>null</code>
     */
    public static String[] splitValues(final String values) {
        if (values == null)
            return null;
        return values.split(TextKeyword.COMMA);
    }

    /**
     * Returns an array of Strings containing only those String values present
     * in both input String arrays <i>a</i> and <i>b</i>.
     * <p>
     * The order of elements in the returned array equals that in array <i>a</i>.
     * <p>
     * If <i>a</i> or <i>b</i> or one of their elements is <code>null</code>, <code>null</code> is returned.
     * 
     * @param a
     *            an array of Strings (element order will be preserved)
     * @param b
     *            an array of Strings
     * @return an array of shared Strings or <code>null</code>
     */
    public static String[] intersect(String[] a, String[] b) {
        if (a == null || b == null)
            return null;
        final int maxLength = Math.max(a.length, b.length);// prevent growing of
                                                           // the ArrayList
        final ArrayList<String> intersection = new ArrayList<String>(maxLength);
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < b.length; ++j) {
                if (a[i] == null || b[j] == null)
                    return null;
                if (a[i].matches(b[j])) {
                    // add element to intersection and check next String in a
                    intersection.add(a[i]);
                    break;
                }
            }
        }
        String[] result = new String[intersection.size()];
        result = intersection.toArray(result);
        return result;
    }

    /**
     * A methods for parsing boolean values from the <i>value</i> part of a
     * <i>key=value</i> pair String. If <i>value</i> equals <i>"Yes"</i>, then <code>true</code> will be
     * returned, if <i>value</i> equals <i>"No"</i>,
     * then <code>false</code> will be returned. In all other cases the method
     * will return <code>null</code>.
     * 
     * @param value
     *            a String containing
     * @return <code>true</code>, <code>false</code>, or <code>null</code>
     */
    public static Boolean parseBooleanValue(final String value) {
        if (value == null)
            return null;
        if (TextKeyword.YES.equals(value))
            return true;
        if (TextKeyword.NO.equals(value))
            return false;
        return null;
    }

    private static final Pattern TEXT_VALUE_PATTERN = Pattern.compile("[\\[\\]a-zA-Z0-9.:;_@/+-]+");

    /**
     * Checks if the <i>value</i> parameter is a properly formatted String
     * value, i.e. if it only contains the allowed characters. The list of legal
     * characters is specified in RFC3720, section 5.1. All characters from that
     * list except for '~' and the null character are considered legitimate.
     * <p>
     * If those constraints are violated, the method returns <code>null</code>.
     * 
     * @param value
     *            the <i>value</i> part of a <i>key=value</i> pair, which is a
     *            String text parameter
     * @return the properly formatted value String or <code>null</code>
     */
    public static boolean checkTextValueFormat(final String value) {
        if (value == null)
            return false;
        final Matcher matcher = TEXT_VALUE_PATTERN.matcher(value);
        return matcher.matches();
    }

    /**
     * Joins a <i>key</i> and a <i>value</i> {@link String} to a
     * <i>key=value</i> pair as required by iSCSI text parameter negotiation and
     * returns the result.
     * 
     * @param key
     *            the <i>key</i> part
     * @param value
     *            the <i>value</i> part
     * @return the concatenated <i>key=value</i> pair
     */
    public static String toKeyValuePair(final String key, final String value) {
        return key + TextKeyword.EQUALS + value;
    }

    /**
     * Translates boolean values to either <code>Yes</code> (<code>true</code>)
     * or <code>No</code> (<code>false</code>).
     * 
     * @param value
     *            the value to translate
     * @return <code>Yes</code> or <code>No</code>
     */
    public static String booleanToTextValue(final boolean value) {
        if (value)
            return TextKeyword.YES;
        return TextKeyword.NO;
    }
}
