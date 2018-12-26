package rlp.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author rlp
 * @since 1.0
 */
public class Parser {

    /**
     * This represents the integer value that indicates the end of file, when a file is reading by a FileReader, and then
     * this integer value is check if the all characters already be read.
     * @since 1.0
     */
    public static final int EOF = -1;

    /**
     * This represents the character buffer that store the all characters that are read from the file.
     * @since 1.0
     */
    private final List<Character> characterBuffer = new LinkedList<>();

    /**
     * This represents the file that will be read.
     * @sincee 1.0
     */
    private final File file;

    private int pos = 0;
    private int linePos = 0;
    private int skipArrays = 0;

    /**
     * This represents the map that store the all complexes objects that is read by the parser and registered.
     * @since 1.0
     */
    private final Map<String, ComplexObject> complexObjectMap = new LinkedHashMap<>();

    public Parser(final File file) {
        if (file == null) {
            throw new NullPointerException("file must not be null");
        }

        this.file = file;
    }

    /**
     * This method parse the file configuration.
     * @since 1.0
     */
    public void parse() {
        this.readToBuffer();

        final StringBuilder key = new StringBuilder();
        final StringBuilder value = new StringBuilder();

        boolean valueParse = false;
        boolean inString = false;
        boolean inArray = false;
        boolean inMap = false;

        // @Note This represents the while-loop condition, that do the all parse values from configuration.
        while (this.pos < this.characterBuffer.size()) {
            final char c = this.next();

            // @Note Check if the position from the character in the line is 0, and check if the character is equals of
            // comment token "#", then jump the line.
            if ((this.linePos == 0 && c == '#') && !inString) {
                this.jumpLine();
                continue;
            }

            // @Note Check if the current character represents a "String Token", that represents by the character ("),
            // then change the boolean value about the inner string.
            if (c == '\"') inString = !inString;

            // @Note If the current character represents a whitespace, and is not in a string, then continue this
            // character to this character not read.
            if (this.isWhitespace(c) && !inString) continue;

            // @Note If the current character represents a carriage return, and the next character represents the
            // new line, then return it, and jump the next character that represents the new line.
            if (this.isCarriageReturn(c) && this.isNewLine(this.peek(0))) {
                this.jump(1);
                this.linePos = 0;
                continue;
            }

            // @Note Check if the current token represents a right bracket "[", that represents that the parser join in
            // an array, and the other check if the current token represents a left bracket "]", that represents that the parser
            // come out from array.
            if (c == '[') {
                inArray = true;
                this.skipArrays++;
            }
            else if (c == ']') {
                if (this.skipArrays == 1) inArray = false;
                this.skipArrays--;
            }

            // @Note Check if the current token represents a right brace "{", that represents that the parser join in
            // a map, and the other check if the current token represents a left brace "}", that represents that the parser
            // come out from map.
            if (c == '{') inMap = true;
            if (c == '}') inMap = false;

            // @Note This represents that the key parse is end, and now starts the value parse from this key-value.
            if (c == '=' && !inMap) {
                valueParse = true;
                continue;
            }

            // @Note This represents the comma token (,), that represents that a new key-value will be parsed.
            if ((c == ',' || this.pos == this.characterBuffer.size()) && !inString && !inArray && !inMap) {
                // @Note This represents the parsed value from the key-value storage.
                final Object parsedValue = this.parseValue(value.toString());

                // @Note Put the key-value into the map.
                this.complexObjectMap.put(key.toString(), new ComplexObject(parsedValue));

                // @Note Reset the options.
                valueParse = false;

                key.delete(0, key.length());
                value.delete(0, value.length());

                // continue;
            }

            // @Note The append of the character must be before of check about the comma token ",", that represents
            // that a new key-value will be storage.
            if (!valueParse) key.append(c);
            else value.append(c);
        }
    }

    // ... Internal Methods ...

    private Object parseValue(final String value) {
        // @Note If the string value starts with "[" and ends with "]", this represents an array.
        if (value.startsWith("[") && value.endsWith("]")) {
            return this.parseArray(value.substring(1, value.length() - 1));
        }

        // @Note If the string valu starts with "{" and ends with "}", this represents a map.
        if (value.startsWith("{") && value.endsWith("}")) {
            return this.parseMap(value.substring(1, value.length() - 1));
        }

        // @Note Check if the value represents a string, then parse this value as string.
        if (value.startsWith("\"")) { // isString.
            // @Note Is not necessary parse the string, then return the value.
            //      -rlp, 24 December 2018
            return value;
        }

        // @Note Check if the value represents a character, then parse this value as character.
        if (value.startsWith("\'")) { // isCharacter.
            // @Note This value represents a character, then the representation for any character is by format "'?'",
            // then get the character that is positioned on the middle from this value, that represents the character
            // that has index 1.

            // @Incomplete: Has a problem that the characters can be wrote with "\/u????", etc. Then must be allow it
            // to be parsed.
            return value.charAt(1);
        }

        // @Note This conditions check if the value represents boolean values.
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;

        // @Note Check if the string represents a numeric string, then parse the number.
        if (this.isNumericString(value)) {
            return this.parseNumber(value);
        }

        return null;
    }

    /**
     * This method parse the map from a string value, into a map of objects.
     * @param value the value that will be parsed from string.
     * @return the map of objects.
     * @since 1.0
     */
    private Map<Object, Object> parseMap(final String value) {
        System.out.println("parseMap, Value: " + value);

        final Map<Object, Object> map = new LinkedHashMap<>();
        final char[] charArray = value.toCharArray();

        final StringBuilder sbKey = new StringBuilder();
        final StringBuilder sbValue = new StringBuilder();

        boolean valueParse = false;
        boolean inString = false;
        boolean inArray = false;

        int skipArrays = 0;

        for (int i = 0; i < charArray.length; i++) {
            final char c = charArray[i];

            // @Note Check if the current character represents the string token ("), then this means that the current
            // character join into a string or come out from a string.
            if (c == '\"') inString = !inString;

            // @Note Check if the current character represents the right bracket "[", that means the parse join in the
            // array, or if the current character represents the left bracket "]", this means that the parse come out
            // from the array.
            if (c == '[') {
                inArray = true;
                skipArrays++;
            }
            else if (c == ']'){
                if (skipArrays == 1) inArray = false;
                skipArrays--;
            }

            // @Note Check if the character represents the equals token "=", then this means that what will be parse is
            // the value.
            if (c == '=') {
                valueParse = true;
                continue;
            }

            // @Note Check if the current character represents the comma token ",", then this means that a new key-value
            // will be parsed.
            if (c == ',' && !inString && !inArray) {
                // @Note Put the key-value into the map, but the parsed value.
                map.put(sbKey.toString(), this.parseValue(sbValue.toString()));

                sbKey.delete(0, sbKey.length());
                sbValue.delete(0, sbValue.length());

                valueParse = false;
                continue;
            }

            if (!valueParse) sbKey.append(c);
            else sbValue.append(c);
        }

        return map;
    }

    /**
     * This method parse the array from a string value, into a List of objects.
     * @param value the value that will be parsed from string.
     * @return the list of objects.
     * @since 1.0
     */
    private List<Object> parseArray(final String value) {
        final List<Object> list = new LinkedList<>();
        final StringBuilder elementValue = new StringBuilder();
        final char[] charArray = value.toCharArray();

        boolean inString = false;
        boolean inArray = false;

        // @Note This represents a way to prevent the bug that occurs with the parse arrays in arrays, because this that
        // this method only parse the "general array", that contains the other arrays.
        //      -rlp, 26 December 2018
        int skipArrays = 0;

        for (int i = 0; i < charArray.length; i++) {
            final char c = charArray[i];

            // @Note Check if the current string represents a string token, that is represented by (").
            if (c == '\"') inString = !inString;

            if (c == '[') {
                inArray = true;
                skipArrays++;
            }
            if (c == ']') {
                if (skipArrays == 1) inArray = false;
                skipArrays--;
            }

            // @Note If the current character is a whitespace, and is not in a string, then continue it.
            if (this.isWhitespace(c) && !inString) continue;

            // @Note This represents the comma token that is in a array and is represented by ",", and means that a new
            // element will be parsed.
            if (c == ',' && !inString && !inArray) {
                // @Note Parse the current element value to the object value, and add into the list.
                list.add(this.parseValue(elementValue.toString()));

                elementValue.delete(0, elementValue.length());
                continue;
            }

            elementValue.append(c);
        }

        return list;
    }

    /**
     * This method parse the string that represents a number literal.
     * @param value the value that will be parsed.
     * @return the number that is parsed from string value.
     * @since 1.0
     */
    private Object parseNumber(final String value) {
        // @Note Check if the number has type declaration. (i. e. 10L, 2.0d, 1b)
        final boolean hasTypeDeclaration = !Character.isDigit(value.charAt(value.length() - 1));

        // @Note Parse the number by boxed-type from each data type.
        if (hasTypeDeclaration) {
            final int typeDeclarationIndex = value.length() - 1;
            final char typeDeclaration = value.charAt(typeDeclarationIndex);

            // @Note The number literal value has the type declaration that represents a non-digit character, then must
            // remove this non-digit character from the value. Is important that creates a new string to re-assign the
            // value without the type declaration because before this modify the debugging output about the input value.
            //      -rlp, 26 December 2018
            final String reassignedValue = value.substring(0, typeDeclarationIndex);

            // @Note Check if the type declaration is a "long".
            if (typeDeclaration == 'l' || typeDeclaration == 'L') {
                return Long.parseLong(reassignedValue);
            }
            // @Note Check if the type declaration is a "double".
            else if (typeDeclaration == 'd' || typeDeclaration == 'D') {
                return Double.parseDouble(reassignedValue);
            }
            // @Note Check if the type declaration is a "float".
            else if (typeDeclaration == 'f' || typeDeclaration == 'F') {
                return Float.parseFloat(reassignedValue);
            }
            // @Note Check if the type declaration is an "integer".
            else if (typeDeclaration == 'i' || typeDeclaration == 'I') {
                return Integer.parseInt(reassignedValue);
            }
            // @Note Check if the type declaration is a "short".
            else if (typeDeclaration == 's' || typeDeclaration == 'S') {
                return Short.parseShort(reassignedValue);
            }
            // @Note Check if the type declaration is a "byte".
            else if (typeDeclaration == 'b' || typeDeclaration == 'B') {
                return Byte.parseByte(reassignedValue);
            }
        }
        // @Note This part represents the numbers that doesn't has explicit-type declaration.
        else {
            // @Note If the value contains the "." token, this represents that the number is a floating-point number or
            // if the value contains "," this represents a currency number.
            if (value.contains(".") || value.contains(",")) {
                // @Note The default data type that a floating-number is parsed is into "double".
                return Double.parseDouble(value);
            }
            // @Note This part include only integer numbers, "byte", "short", "integer" and "long".
            else {
                // @Note The approach of if-else removes the strange bug that occurs when use ternary operator (?:),
                // then continues use it.
                if (value.length() > 10) {
                    return Long.parseLong(value);
                } else {
                    return Integer.parseInt(value);
                }
            }
        }

        throw new RuntimeException("The number \"" + value + "\" can\'t be parsed.");
    }

    /**
     * This method read the all characters that contains in the file into the character buffer.
     * @since 1.0
     */
    private void readToBuffer() {
        try (final FileReader reader = new FileReader(this.file)) {
            // @Note This represents the character value, on this character value is cast to character and put into
            // the character buffer.
            int c;
            while ((c = reader.read()) != EOF) {
                this.characterBuffer.add((char) c);
            }
        } catch (IOException e) {
            System.out.println("An error occured when read the file into the buffer.");
            e.printStackTrace();
        }
    }

    /**
     * This method get the character that is in the current position, and increase the position.
     * @return the character that is in the current position.
     * @since 1.0
     */
    private char next() {
        return this.characterBuffer.get(this.pos++);
    }

    /**
     * This method jump a amount of characters.
     * @param amount the amount of characters that the parser jump.
     * @since 1.0
     */
    private void jump(final int amount) {
        this.pos += amount;
    }

    /**
     * This method jump the all line.
     * @since 1.0
     */
    private void jumpLine() {
        while (!(this.isCarriageReturn(this.next()))) {
            this.jump(1);
        }

        // @Note Not remove this jump from here, because this method jump the character of new line.
        this.jump(1);
    }

    /**
     * This method get the character that is in the buffer of "amount" positions. The "amount" number must be non negative,
     * and the amount of nubmers can not exceed the limit from the character buffer size.
     * @param amount the amount of characters to peek the character.
     * @return the peek character.
     * @since 1.0
     */
    private char peek(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be non negative.");
        if (this.pos + amount > this.characterBuffer.size()) throw new IllegalArgumentException("amount of \"" + amount
                + "\" exceeds the limit of the buffer.");

        return this.characterBuffer.get(this.pos + amount);
    }

    /**
     * This method check if the character represents a whitespace character.
     * @param c the character that will be checked.
     * @return true if the character is a whitespace, otherwise returns false.
     */
    private boolean isWhitespace(final char c) {
        return c == 32;
    }

    /**
     * This method check if the character represents a carriage return.
     * @param c the character that will be checked.
     * @return true if the character is a carriage return, otehrwise returns false.
     */
    private boolean isCarriageReturn(final char c) {
        return c == 13;
    }

    /**
     * This method check if the character represents a new line.
     * @param c the character that will be checked.
     * @return true if the character is a new line, otherwise returns false.
     */
    private boolean isNewLine(final char c) {
        return c == 10;
    }

    /**
     * This method check if the string is a numeric string, that means that the all characters from the string is a number.
     * @param string the string that will be checked.
     * @return true if string is a numeric string, otherwise returns false.
     * @since 1.0
     */
    private boolean isNumericString(final String string) {
        final char[] charArray = string.toCharArray();

        for (int i = 0; i < charArray.length; i++) {
            final char c = charArray[i];

            // @Note This represents a some exceptions when check if the string represents a numeric string, because the
            // value can be a floating point and can contains the character ".", or the value can be a currency value and
            // contains the "," character.
            //      -rlp, 24 December 2018
            if (c == '.' || c == ',') continue;

            // @Note This represents an exception that when the last character from the string is a "b" or "s", declare
            // that the number is a byte when "b" or short when "s".
            if (i == charArray.length - 1 &&
                    ((c == 'B' || c == 'S' || c == 'F' || c == 'D' || c == 'L' || c == 'I') ||
                            (c == 'b' || c == 's' || c == 'f' || c == 'd' || c == 'l' || c == 'i'))) continue;

            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method returns the map that contains the all complexes object.
     *
     * @Note This method is not to be public, then this method is only available
     * to be called while the testing functions.
     *
     * @return the complexes object map.
     * @since 1.0
     */
    public Map<String, ComplexObject> getComplexObjectMap() {
        return complexObjectMap;
    }

    /**
     * This method get the value by the key.
     * @param key the key from the value.
     * @return the ComplexObject that is referred by the key.
     */
    public ComplexObject get(final String key) {
        return this.complexObjectMap.get(key);
    }
}
