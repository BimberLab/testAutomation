package org.labkey.serverapi.reader;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static java.lang.Math.min;

public class StringUtilsLabKey
{
    /**
     * Instead of relying on the platform default character encoding, use this Charset
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final Random RANDOM = new Random();
    private static final int MAX_LONG_LENGTH = String.valueOf(Long.MAX_VALUE).length() - 1;

    // Finds the longest common prefix present in all elements of the passed in string collection. In other words,
    // the longest string (prefix) such that, for all s in strings, s.startsWith(prefix). An empty collection returns
    // the empty string and a single element collection returns that string.
    public static String findCommonPrefix(@NotNull Collection<String> strings)
    {
        if (strings.isEmpty())
            return "";

        List<String> list = new ArrayList<>(strings);

        if (strings.size() == 1)
            return list.get(0);

        Collections.sort(list);
        String first = list.get(0);
        String last = list.get(list.size() - 1);
        int i = 0;

        while (first.charAt(i) == last.charAt(i))
            i++;

        return first.substring(0, i);
    }

    /**
     * Generate a String of random digits of the specified length. Will not have leading zeros
     */
    public static String getUniquifier(int length)
    {
        if (length <= 0)
        {
            return "";
        }
        return String.valueOf(RANDOM.nextInt(9) + 1) + getPaddedUniquifier(length - 1);
    }

    /**
     * Generate a String of random digits of the specified length. May contain leading zeros
     */
    public static String getPaddedUniquifier(int length)
    {
        StringBuilder builder = new StringBuilder(length);
        int chunkLength = MAX_LONG_LENGTH;
        long maxValue = Double.valueOf(Math.pow(10, MAX_LONG_LENGTH)).longValue();
        while (length > 0)
        {
            if (length > MAX_LONG_LENGTH)
            {
                length -= MAX_LONG_LENGTH;
            }
            else
            {
                chunkLength = length;
                maxValue = Double.valueOf(Math.pow(10, chunkLength)).longValue();
                length = 0;
            }
            String unpadded = String.valueOf(Math.abs(RANDOM.nextLong()) % maxValue);
            builder.append(StringUtils.repeat('0', chunkLength - unpadded.length()));
            builder.append(unpadded);
        }
        return builder.toString();
    }

    // Joins provided strings, separating with separator but skipping any strings that are null, blank, or all whitespace.
    public static String joinNonBlank(String separator, String... stringsToJoin)
    {
        StringBuilder sb = new StringBuilder();
        String sep = "";

        for (String s : stringsToJoin)
        {
            if (StringUtils.isNotBlank(s))
            {
                sb.append(sep);
                sb.append(s);
                sep = separator;
            }
        }

        return sb.toString();
    }

    /**
     * Recognizes strings that start with http://, https://, ftp://, or mailto:
     */
    private static final String[] URL_PREFIXES = {"http://", "https://", "ftp://", "mailto:"};

    public static boolean startsWithURL(String s)
    {
        if (s != null)
        {
            for (String prefix : URL_PREFIXES)
                if (StringUtils.startsWithIgnoreCase(s, prefix))
                    return true;
        }

        return false;
    }

    // Does the string have ANY upper-case letters?
    public static boolean containsUpperCase(String s)
    {
        for (char ch : s.toCharArray())
            if (Character.isUpperCase(ch))
                return true;

        return false;
    }


    public static boolean isText(String s)
    {
        for (char c : s.toCharArray())
        {
            if (c <= 32)
            {
                if (Character.isWhitespace(c))
                    continue;
            }
            else if (c < 127)
            {
                continue;
            }
            else if (c == 127)
            {
                // DEL??
                return false;
            }
            else if (Character.isValidCodePoint(c))
            {
                continue;
            }
            return false;
        }
        return true;
    }


    /**
     * <p>Replaces all occurrences of a String within another String, ignoring case.</p>
     *
     * @return String with replacements
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement)
    {
        return text.replaceAll("(?i)" + Pattern.quote(searchString), replacement);
    }


    /**
     * <p>Replaces first occurrence of a String within another String, ignoring case.</p>
     *
     * @return String with replacements
     */
    public static String replaceFirstIgnoreCase(final String text, final String searchString, final String replacement)
    {
        return text.replaceFirst("(?i)" + Pattern.quote(searchString), replacement);
    }


    // Outputs a formatted count and a noun that's pluralized (by simply adding "s")
    public static String pluralize(long count, String singular)
    {
        return pluralize(count, singular, singular + "s");
    }


    // Outputs a formatted count and a noun that's pluralized (outputting the plural parameter if appropriate)
    public static String pluralize(long count, String singular, String plural)
    {
        return Formats.commaf0.format(count) + " " + (1 == count ? singular : plural);
    }

    // splits strings at camel case boundaries
    // copied from http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
    public static String splitCamelCase(String s)
    {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

    public static int toInt(Object value)
    {
        if (null == value)
            return 0;
        else if (String.class.isInstance(value))
            return Integer.valueOf((String) value);
        else if (Number.class.isInstance(value))
            return ((Number) value).intValue();

        throw new IllegalArgumentException("Unable to get int value for value parameter");
    }

    // Domain names can contain only ASCII alphanumeric characters and dashes and may not start or end with a dash.
    // Each domain name can be at most 63 characters.
    public static Pattern domainNamePattern = Pattern.compile("(?!-)[A-Za-z0-9-]{0,62}[A-Za-z0-9]$");

    public static boolean isValidDomainName(String name)
    {
        return !StringUtils.isEmpty(name) && domainNamePattern.matcher(name).matches();
    }

    /**
     * Given a name, transforms it into a valid domain for an internet address, if possible, according to the constraints
     * specified here: https://tools.ietf.org/html/rfc1035
     *
     * @param name the name to be transformed.
     * @return null if the given string contains no characters that can be transformed in the order given to make a valid domain name ; a string containing only alphanumeric characters and dashes
     * that does not start with a dash or
     */
    public static String getDomainName(String name)
    {
        if (StringUtils.isEmpty(name))
            return null;
        // decompose non-ASCII characters into component characters.
        String normalizedName = Normalizer.normalize(name.trim().toLowerCase(), Normalizer.Form.NFD);
        // replaces spaces with dashes and remove all characters that are not alpanumeric or a dash
        normalizedName = normalizedName.replaceAll(" ", "-").replaceAll("[^A-Za-z0-9-]", "");
        int start = 0;
        int end = min(63, normalizedName.length()); // a sub-domain can be at most 63 characters in length
        while (start < end && normalizedName.charAt(start) == '-')
            start++;
        while (end > start && normalizedName.charAt(end - 1) == '-')
            end--;
        if (end - start == 0)
            return null;
        if (start > 0 || end < normalizedName.length())
            return normalizedName.substring(start, end);
        else
            return normalizedName;
    }
}
