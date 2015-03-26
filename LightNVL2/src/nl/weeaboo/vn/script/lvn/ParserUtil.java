package nl.weeaboo.vn.script.lvn;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

final class ParserUtil {

    private static final char ZERO_WIDTH_SPACE = 0x200B;

    private ParserUtil() {
    }

    public static String concatLines(String[] lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

    public static boolean isCollapsibleSpace(char c) {
        return c == ' ' || c == '\t' || c == '\f' || c == ZERO_WIDTH_SPACE;
    }

    public static String collapseWhitespace(String s, boolean trim) {
        char chars[] = new char[s.length()];
        s.getChars(0, chars.length, chars, 0);

        int r = 0;
        int w = 0;
        while (r < chars.length) {
            char c = chars[r++];

            if (isCollapsibleSpace(c)) {
                //Skip any future characters if they're whitespace
                while (r < chars.length && isCollapsibleSpace(chars[r])) {
                    r++;
                }

                if (w == 0 && trim) {
                    continue; //Starts with space
                } else if (r >= chars.length && trim) {
                    continue; //Ends with space
                }
            }

            chars[w++] = c;
        }

        return new String(chars, 0, w);
    }

    /**
     * @return {@code true} if the given string consists of only whitespace.
     */
    public static boolean isWhitespace(String string) {
        return isWhitespace(string, 0, string.length());
    }
    public static boolean isWhitespace(String string, int from, int to) {
        int n = from;
        while (n < to) {
            int c = string.codePointAt(n);
            if (!Character.isWhitespace(c)) {
                return false;
            }
            n += Character.charCount(c);
        }
        return true;
    }

    public static boolean isWord(String string) {
        return isWord(string, 0, string.length());
    }
    public static boolean isWord(String string, int from, int to) {
        int n = from;
        while (n < to) {
            int c = string.codePointAt(n);
            if (Character.isLetterOrDigit(c)) {
                return true;
            }
            n += Character.charCount(c);
        }
        return false;
    }

    static int findBlockEnd(String str, int off, char endChar) {
        CharacterIterator itr = new StringCharacterIterator(str, off);
        return findBlockEnd(itr, endChar, null);
    }
    static int findBlockEnd(CharacterIterator itr, char endChar, StringBuilder out) {
        boolean inQuotes = false;
        int brackets = 0;

        for (char c = itr.current(); c != CharacterIterator.DONE; c = itr.next()) {
            if (c == '\\') {
                if (out != null) out.append(c);
                c = itr.next();
            } else if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (brackets <= 0 && c == endChar) {
                    break;
                }
                else if (c == '[') brackets++;
                else if (c == ']') brackets--;
            }

            if (out != null && c != CharacterIterator.DONE) {
                out.append(c);
            }
        }
        return itr.getIndex();
    }

}
