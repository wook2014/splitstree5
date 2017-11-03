/*
 *  Copyright (C) 2016 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class Basic {
    static boolean debugMode = true;
    static final PrintStream origErr = System.err;
    static final PrintStream origOut = System.out;
    static final PrintStream nullOut = new PrintStream(new NullOutStream());
    static private CollectOutStream collectOut;


    /**
     * set debug mode. In debug mode, stack traces are printed
     *
     * @param mode
     */
    static public void setDebugMode(boolean mode) {
        debugMode = mode;
    }

    /**
     * Get debug mode. In debug mode, stack traces are printed
     *
     * @return debug mode
     */
    static public boolean getDebugMode() {
        return debugMode;
    }

    /**
     * Ignore all output written to System.err
     *
     * @return the current PrintStream connected to System.err
     */
    public static PrintStream hideSystemErr() {
        PrintStream current = System.err;
        System.setErr(nullOut);
        return current;
    }

    /**
     * send the system err messages to System out
     */
    public static void sendSystemErrToSystemOut() {
        System.setErr(origOut);
    }

    public static void startCollectionStdErr() {
        collectOut = new CollectOutStream();
        System.setErr(new PrintStream(collectOut));
    }

    public static String stopCollectingStdErr() {
        if (collectOut != null) {
            String result = collectOut.toString();
            collectOut = null;
            return result;
        } else
            return "";
    }

    /**
     * Restore the System.err to the given PrintStream
     *
     * @param ps the print stream
     */
    public static void restoreSystemErr(PrintStream ps) {
        System.setErr(ps);
    }

    /**
     * Restore System.err to the standard error stream, even if it was
     * set to something else in between
     */
    public static void restoreSystemErr() {
        System.setErr(origErr);
    }

    /**
     * Ignore all output written to System.out
     *
     * @return the current PrintStream connected to System.out
     */
    public static PrintStream hideSystemOut() {
        PrintStream current = System.out;
        System.setOut(nullOut);
        return current;
    }

    /**
     * Restore the System.out stream to the given PrintStream
     *
     * @param ps the new print stream
     */
    public static void restoreSystemOut(PrintStream ps) {
        System.setOut(ps);
    }

    /**
     * Restore System.out to the standard output stream, even if it was
     * set to something else in between
     */
    public static void restoreSystemOut() {
        System.setOut(origOut);
    }

    /**
     * Catch an exception.
     *
     * @param ex Exception
     */
    public static void caught(Throwable ex) {
        if (debugMode) {
            System.err.println("Caught:");
            ex.printStackTrace();
        } else
            System.err.println(ex.getMessage());
    }

    /**
     * returns the short name of a class
     *
     * @param clazz
     * @return short name
     */
    public static String getShortName(Class clazz) {
        return getSuffix(clazz.getName());
    }

    /**
     * returns the suffix of a file name
     *
     * @param fileName
     * @return file name extension
     */
    public static String getSuffix(String fileName) {
        if (fileName == null)
            return null;
        int pos = fileName.lastIndexOf(".");
        if (pos == -1 || pos == fileName.length() - 1)
            return null;
        else {
            return fileName.substring(pos + 1);
        }
    }

    /**
     * converts a string containing spaces into an array of strings.
     *
     * @param str
     * @return array of strings that where originally separated by spaces
     */
    public static String[] toArray(String str) {
        List<String> list = new LinkedList<>();

        for (int j, i = skipSpaces(str, 0); i < str.length(); i = skipSpaces(str, j)) {
            for (j = i + 1; j < str.length(); j++)
                if (Character.isSpaceChar(str.charAt(j)))
                    break; // found next space
            list.add(str.substring(i, j));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * skip all spaces starting at position i
     *
     * @param str
     * @param i
     * @return first position containing a non-space character or str.length()
     */
    public static int skipSpaces(String str, int i) {
        while (i < str.length() && Character.isSpaceChar(str.charAt(i)))
            i++;
        return i;
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @return string representation
     */
    public static String toString(int[] array) {
        return toString(array, 0, array.length, ", ");
    }

    /**
     * returns an array of integers as a string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(int[] array, String separator) {
        return toString(array, 0, array.length, separator);
    }

    /**
     * returns an array of integers as astring
     *
     * @param array
     * @return string representation
     */
    public static String toString(int[] array, int offset, int length, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        length = Math.min(offset + length, array.length);
        for (int i = offset; i < length; i++) {
            int x = array[i];
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(x);
        }
        return buf.toString();
    }

    /**
     * returns an array of floats as string
     *
     * @param array
     * @param separator
     * @return
     */
    public static String toString(float[] array, String separator) {
        return toString(array, 0, array.length, separator, false);
    }

    /**
     * returns an array of floats as string
     *
     * @param array
     * @param offset
     * @param length
     * @param separator
     * @param roundToInts
     * @return
     */
    public static String toString(float[] array, int offset, int length, String separator, boolean roundToInts) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        length = Math.min(offset + length, array.length);
        for (int i = offset; i < length; i++) {
            float x = array[i];
            if (first)
                first = false;
            else
                buf.append(separator);
            if (roundToInts)
                buf.append(Math.round(x));
            else
                buf.append(x);
        }
        return buf.toString();
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(Object[] array, String separator) {
        return toString(array, 0, array.length, separator);
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @param offset    where to start reading array
     * @param length    how many entries to read
     * @param separator
     * @return string representation
     */
    public static String toString(Object[] array, int offset, int length, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (int i = 0; i < length; i++) {
            Object anArray = array[i + offset];
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(anArray);
        }
        return buf.toString();
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(long[] array, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (long a : array) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(a);
        }
        return buf.toString();
    }


    /**
     * returns an array of double as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(double[] array, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (double a : array) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(a);
        }
        return buf.toString();
    }

    /**
     * returns an array of double as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(String format, double[] array, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (double a : array) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(String.format(format, a));
        }
        return buf.toString();
    }

    /**
     * returns a collection of objects a separated string
     *
     * @param collection
     * @param separator
     * @return string representation
     */
    public static String toString(Collection collection, String separator) {
        if (collection == null)
            return "";
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (Object aCollection : collection) {
            if (aCollection != null) {
                if (first)
                    first = false;
                else if (separator != null)
                    buf.append(separator);
                buf.append(aCollection);
            }
        }
        return buf.toString();
    }

    /**
     * returns a set a comma separated string
     *
     * @param set
     * @return string representation
     */
    public static String toString(BitSet set) {
        if (set == null)
            return "null";

        final StringBuilder buf = new StringBuilder();

        int startRun = 0;
        int inRun = 0;
        boolean first = true;
        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
            if (first) {
                first = false;
                buf.append(i);
                startRun = inRun = i;
            } else {
                if (i == inRun + 1) {
                    inRun = i;
                } else if (i > inRun + 1) {
                    if (inRun == startRun || i == startRun + 1)
                        buf.append(",").append(i);
                    else if (inRun == startRun + 1)
                        buf.append(",").append(inRun).append(",").append(i);
                    else
                        buf.append("-").append(inRun).append(",").append(i);
                    inRun = startRun = i;
                }
            }
        }
        // dump last:
        if (inRun == startRun + 1)
            buf.append(",").append(inRun);
        else if (inRun > startRun + 1)
            buf.append("-").append(inRun);
        return buf.toString();
    }

    /**
     * gets members of bit set as as string
     *
     * @param set
     * @param separator
     * @return string
     */
    public static String toString(BitSet set, char separator) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (int i = set.nextSetBit(0); i != -1; i = set.nextSetBit(i + 1)) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(i);
        }
        return buf.toString();
    }


    /**
     * kinda generic array construction
     *
     * @param collection
     * @param <T>
     * @return array
     */
    public static <T> T[] toArray(final Collection<T> collection) {
        return (T[]) collection.toArray();
    }

    /**
     * gets a inputstream. If file ends on gz or zip opens appropriate unzipping stream
     *
     * @param fileName
     * @return input stream
     * @throws IOException
     */
    public static InputStream getInputStreamPossiblyZIPorGZIP(String fileName) throws IOException {
        final File file = new File(fileName);
        if (file.isDirectory())
            throw new IOException("Directory, not a file: " + file);
        if (!file.exists())
            throw new IOException("No such file: " + file);
        final InputStream ins;
        if (fileName.toLowerCase().endsWith(".gz")) {
            ins = new GZIPInputStream(new FileInputStream(file));
        } else if (fileName.toLowerCase().endsWith(".zip")) {
            ZipFile zf = new ZipFile(file);
            Enumeration e = zf.entries();
            ZipEntry entry = (ZipEntry) e.nextElement(); // your only file
            ins = zf.getInputStream(entry);
        } else
            ins = new FileInputStream(file);
        return ins;
    }

    /**
     * gets a outputstream. If file ends on gz or zip opens appropriate zipping stream
     *
     * @param fileName
     * @return input stream
     * @throws IOException
     */
    public static OutputStream getOutputStreamPossiblyZIPorGZIP(String fileName) throws IOException {
        OutputStream outs = new FileOutputStream(fileName);
        if (fileName.toLowerCase().endsWith(".gz")) {
            outs = new GZIPOutputStream(outs);
        } else if (fileName.toLowerCase().endsWith(".zip")) {
            final ZipOutputStream out = new ZipOutputStream(outs);
            ZipEntry e = new ZipEntry(Basic.replaceFileSuffix(fileName, ""));
            out.putNextEntry(e);
        }
        return outs;
    }

    /**
     * is this a gz or zip file?
     *
     * @param fileName
     * @return true, if gz or zip file
     */
    public static boolean isZIPorGZIPFile(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".gz") || fileName.endsWith(".zip");
    }

    /**
     * replace the suffix of a file
     *
     * @param fileName
     * @param newSuffix
     * @return new file name
     */
    public static String replaceFileSuffix(String fileName, String newSuffix) {
        return replaceFileSuffix(new File(fileName), newSuffix).getPath();
    }

    /**
     * replace the suffix of a file
     *
     * @param file
     * @param newSuffix
     * @return new file
     */
    public static File replaceFileSuffix(File file, String newSuffix) {
        String name = Basic.getFileBaseName(file.getName());
        if (newSuffix != null && !name.endsWith(newSuffix))
            name = name + newSuffix;
        return new File(file.getParent(), name);
    }

    public static String getFileNameWithoutZipOrGZipSuffix(String fileName) {
        if (Basic.isZIPorGZIPFile(fileName))
            return replaceFileSuffix(fileName, "");
        else
            return fileName;
    }

    /**
     * returns name with .suffix removed
     *
     * @param name
     * @return name without .suffix
     */
    public static String getFileBaseName(String name) {
        if (name != null) {
            int pos = name.lastIndexOf(".");
            if (pos > 0)
                name = name.substring(0, pos);
        }
        return name;
    }

    /**
     * returns the suffix of a file name. Returns null name is null
     *
     * @param name
     * @return suffix   or null
     */
    public static String getFileSuffix(String name) {
        if (name == null)
            return null;
        name = getFileNameWithoutPath(name);
        int index = name.lastIndexOf('.');
        if (index > 0)
            return name.substring(index);
        else
            return "";
    }

    /**
     * returns name with path removed
     *
     * @param name
     * @return name without path
     */
    public static String getFileNameWithoutPath(String name) {
        if (name != null) {
            int pos = name.lastIndexOf(File.separatorChar);
            if (pos != -1 && pos < name.length() - 1) {
                name = name.substring(pos + 1);
            }
        }
        return name;
    }

    /**
     * convert a string with spaces and/or underscores to camel case
     *
     * @param string
     * @return camel case
     */
    public static String toCamelCase(String string) {
        int pos = 0;
        while (pos < string.length() && (Character.isWhitespace(string.charAt(pos)) || string.charAt(pos) == '_'))
            pos++;
        boolean afterWhiteSpace = false;
        StringBuilder buf = new StringBuilder();
        while (pos < string.length()) {
            final char ch = string.charAt(pos);
            if (Character.isWhitespace(ch) || ch == '_')
                afterWhiteSpace = true;
            else if (afterWhiteSpace) {
                buf.append(Character.toUpperCase(ch));
                afterWhiteSpace = false;
            } else
                buf.append(Character.toLowerCase(ch));
            pos++;
        }
        return buf.toString();
    }

    /**
     * convert a string with spaces and/or underscores to camel case
     *
     * @param string
     * @return camel case
     */
    public static String fromCamelCase(String string) {
        boolean afterWhiteSpace = true;
        boolean afterCapital = false;
        StringBuilder buf = new StringBuilder();
        for (int pos = 0; pos < string.length(); pos++) {
            final char ch = string.charAt(pos);
            if (Character.isUpperCase(ch)) {
                if (!afterWhiteSpace && !afterCapital) {
                    buf.append(" ");
                }
                afterCapital = true;
            } else
                afterCapital = false;
            buf.append(ch);
            afterWhiteSpace = (Character.isWhitespace(ch));
        }
        return buf.toString();
    }

    /**
     * removes all text between any pair of left- and right-delimiters.
     * No nesting
     *
     * @param str
     * @param leftDelimiter
     * @param rightDelimiter
     * @return string with comments removed
     */
    public static String removeComments(String str, char leftDelimiter, char rightDelimiter) {
        StringBuilder buf = new StringBuilder();

        boolean inComment = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (inComment && ch == rightDelimiter) {
                inComment = false;
            } else if (ch == leftDelimiter) {
                inComment = true;
            } else if (!inComment)
                buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * returns true, if string can be parsed as int
     *
     * @param next
     * @return true, if int
     */
    public static boolean isInteger(String next) {
        try {
            Integer.parseInt(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * returns true, if string can be parsed as long
     *
     * @param next
     * @return true, if int
     */
    public static boolean isLong(String next) {
        try {
            Long.parseLong(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    /**
     * returns true, if string can be parsed as float
     *
     * @param next
     * @return true, if int
     */
    public static boolean isFloat(String next) {
        try {
            Float.parseFloat(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * returns true, if string can be parsed as double
     *
     * @param next
     * @return true, if int
     */
    public static boolean isDouble(String next) {
        try {
            Double.parseDouble(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * attempts to parse the string as an integer, skipping leading chars and trailing characters, if necessary.
     * Returns 0, if no number found
     *
     * @param string
     * @return value or 0
     */
    public static int parseInt(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && Character.isDigit(string.charAt(finish)))
                        finish++;
                    if (start < finish)
                        return Integer.parseInt(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    /**
     * attempts to parse the string as a long, skipping leading chars and trailing characters, if necessary
     *
     * @param string
     * @return value or 0
     */
    public static long parseLong(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && Character.isDigit(string.charAt(finish)))
                        finish++;
                    if (start < finish)
                        return Long.parseLong(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    /**
     * attempts to parse the string as an float, skipping leading chars and trailing characters, if necessary
     *
     * @param string
     * @return value or 0
     */
    public static float parseFloat(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
                            || string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-'))
                        finish++;
                    if (start < finish)
                        return Float.parseFloat(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    /**
     * attempts to parse the string as a double, skipping leading chars and trailing characters, if necessary
     *
     * @param string
     * @return value or 0
     */
    public static double parseDouble(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
                            || string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-' || string.charAt(finish) == '+'))
                        finish++;
                    if (start < finish)
                        return Double.parseDouble(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    /**
     * split string on given character. Note that results are subsequently trimmed
     *
     * @param aLine
     * @param splitChar
     * @return split string, trimmed
     */
    public static String[] split(String aLine, char splitChar) {
        return split(aLine, splitChar, Integer.MAX_VALUE);
    }

    /**
     * split string on given character. Note that results are subsequently trimmed
     *
     * @param aLine
     * @param splitChar
     * @return split string, trimmed
     */
    public static String[] split(String aLine, char splitChar, int maxTokens) {
        if (aLine.length() == 0 || maxTokens <= 0)
            return new String[0];

        // need to ignore last position if it is the split character
        final int length = (aLine.charAt(aLine.length() - 1) == splitChar ? aLine.length() - 1 : aLine.length());

        // count the number of tokens
        int count = 1;
        if (maxTokens > 1) {
            for (int i = 0; i < length; i++) {
                if (aLine.charAt(i) == splitChar) {
                    if (count < maxTokens)
                        count++;
                    else
                        break;
                }
            }
        }

        final String[] result = new String[count];
        int prev = 0;
        int which = 0;
        int pos = 0;
        for (; pos < length; pos++) {
            if (aLine.charAt(pos) == splitChar) {
                result[which++] = aLine.substring(prev, pos).trim();
                prev = pos + 1;
                if (which == count)
                    return result;
            }
        }
        if (pos > prev) {
            result[which] = aLine.substring(prev, pos).trim();
        }
        return result;
    }

    /**
     * counts the number of occurrences of c in text
     *
     * @param text
     * @param c
     * @return count
     */
    public static int countOccurrences(String text, char c) {
        int count = 0;
        if (text != null) {
            for (int i = 0; i < text.length(); i++)
                if (text.charAt(i) == c)
                    count++;
        }
        return count;
    }

    /**
     * counts the number of occurrences of word in text
     *
     * @param text
     * @param word
     * @return count
     */
    public static int countOccurrences(String text, String word) {
        int count = 0;
        for (int i = text.indexOf(word); i != -1; i = text.indexOf(word, i + 1))
            count++;
        return count;
    }

    /**
     * Finds the value of the given enumeration by name, case-insensitive.
     *
     * @return enumeration value or null
     */
    public static <T extends Enum<T>> T valueOfIgnoreCase(Class<T> enumeration, String name) {
        for (T enumValue : enumeration.getEnumConstants()) {
            if (enumValue.name().equalsIgnoreCase(name)) {
                return enumValue;
            }
        }
        return null;
    }

    /**
     * surrounds word with quotes if it contains character that is not a digit, letter or _
     *
     * @param str
     * @return str, quoted is necessary
     */
    public static String quoteIfNecessary(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetterOrDigit(str.charAt(i)) && str.charAt(i) != '_')
                return "'" + str + "'";
        }
        return str;
    }
}


/**
 * silent stream
 */
class NullOutStream extends OutputStream {
    public void write(int b) {
    }
}

class CollectOutStream extends OutputStream {
    private StringBuilder buf = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
        Basic.origErr.write(b);
        buf.append((char) b);
    }

    public String toString() {
        return buf.toString();
    }
}