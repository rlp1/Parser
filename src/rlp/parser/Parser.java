package rlp.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author rlp
 * @since 1.0
 */
public class Parser {

    private static final int END_OF_STREAM = -1;

    private final List<Character> buffer = new LinkedList<>();
    private final Map<String, ComplexObject> variables = new LinkedHashMap<>();

    private final File file;

    private final Logger logger = Logger.getLogger("Parser");

    private int pos;
    private int linePos = 0;

    private boolean stringInner;
    private boolean arrayInner;
    private boolean keyValueInner;

    private boolean varDecl = true;

    public Parser(final File file) {
        if (file == null) throw new NullPointerException("file");
        this.file = file;
    }

    public void parse() {
        //
        // @Note This represents a "lazy" initialization, because the read from the characters of the file into the
        // buffer can be made also when the constructor is declared because only each class is parsed by Parse class
        // instance.
        //
        this.readToBuffer(file);

        final StringBuilder varDeclaration = new StringBuilder();
        final StringBuilder valueDeclaration = new StringBuilder();

        while (this.pos < this.buffer.size()) {
            final char c = this.next();

            if (this.isCarriageReturn(c) && this.isNewLine(this.peek())) {
                this.jump();
                continue;
            }

            // @Note Check if the character is not in inner in string declaration, and if the current character is a
            // whitespace, then continue the while-loop.
            if (!this.stringInner && this.isWhitespace(c)) continue;

            // @Note This token represents a "#" comment token, that make the jump from the line until a carriage
            // return.
            if (c == '#' && !this.stringInner) {
                this.jumpLine();
                continue;
            }

            // @Note Check if the current character represents this token "=". Because this token represents the break
            // from the variable name declaration to set the variable value declaration and the vice-versa.
            if (c == '=' && !this.keyValueInner && !this.stringInner) {
                this.varDecl = !this.varDecl;
                continue;
            }

            //
            // @Note This variable "isLastCharacter" is used to fix a bug that always last variable value must be has
            // a "," that represents the token that informs to parser that will parse a next variable key and value,
            // then without this, it's not happen, then is used the check from the last character to not need check the
            // token ",".
            //  -rlp, 03/11/2018 09:17
            //
            final boolean isLastCharacter = this.pos == this.buffer.size();

            // @Note Check if the current character represents this token ",". Because this token represents the break
            // from the variable declaration to make another variable declaration, then must be dispatch the current
            // variable declaration into the map and clear the "varDeclaration" and "valueDeclaration" string buffers.
            if (isLastCharacter || c == ',' && !this.stringInner && !this.arrayInner && !this.keyValueInner) {
                // @Note This condition is check because the conditions use the "continue" when the characters are a
                // token and it is not appended into the key or value declaration, but when the condition above is
                // conditioned by the "isLastCharacter" this represents a character and not a token and it's will be
                // not appended without this condition.
                if (isLastCharacter) valueDeclaration.append(c);

                final String name = varDeclaration.toString();
                final String value = valueDeclaration.toString();

                System.out.println("Value: " + valueDeclaration.toString());

                final ComplexObject complexObjectValue = ComplexObject.newComplexObject(this.parseValue(value));

                this.variables.put(name, complexObjectValue);

                varDeclaration.delete(0, varDeclaration.length());
                valueDeclaration.delete(0, valueDeclaration.length());

                this.varDecl = true;
                this.linePos = 0;
                continue;
            }


            // @Note Check if the current character represents this token """. Because this token represents the inner
            // from string characters.
            if (c == '\"') this.stringInner = !this.stringInner;
            if (c == '[') this.arrayInner = true;
            if (c == ']') this.arrayInner = false;
            if (c == '{') this.keyValueInner = true;
            if (c == '}') this.keyValueInner = false;

            if (varDecl) varDeclaration.append(c);
            else valueDeclaration.append(c);

            this.linePos++;
        }
    }

    private Object parseValue(final String value) {
        final boolean isMap = value.startsWith("{");

        if (isMap) {
            boolean mapStringInner = false;
            boolean varDecl = true;
            final Map<Object, Object> map = new HashMap<>();

            final StringBuilder mapKey = new StringBuilder();
            final StringBuilder mapValue = new StringBuilder();

            for (final char c : value.toCharArray()) {
                if (c == '{') continue;
                if (this.isWhitespace(c) && !mapStringInner) continue;

                // @Note This token represents the break parse from the current value to the next value.
                if ((c == '}' || c == ',') && !mapStringInner) {
                    map.put(mapKey.toString(), mapValue.toString());

                    mapKey.delete(0, mapKey.length());
                    mapValue.delete(0, mapValue.length());

                    varDecl = true;
                    continue;
                }

                // @Note This token represents the "open/close" from a string.
                if (c == '\"') mapStringInner = !mapStringInner;

                // @Note This token represents the change from the variable name declaration to the variable value
                // declaration.
                if (c == '=') {
                    varDecl = !varDecl;
                    continue;
                }

                if (varDecl) mapKey.append(c);
                else mapValue.append(c);
            }

            return map;
        }

        final boolean isArray = value.startsWith("[");

        if (isArray) {
            boolean arrayStringInner = false;

            final StringBuilder arrayValue = new StringBuilder();
            final List<Object> list = new ArrayList<>();

            int pos = 0;

            for (final char c : value.toCharArray()) {
                final int currentPos = pos++;

                // @Note This represents the first bracket from the array that is "[". Then, how this is not appended
                // into the character and will not be parsed, continue it.
                if (currentPos == 0) continue;
                if (this.isWhitespace(c) && !arrayStringInner) continue;

                // @Note This token represents the break parse from the current value to the next value.
                if (c == ',' && !arrayStringInner) {
                    final Object parsedArrayValue = this.parseValue(arrayValue.toString());

                    list.add(parsedArrayValue);

                    arrayValue.delete(0, arrayValue.length());
                    continue;
                }

                // @Note This token represents the "open/close" from a string.
                if (c == '\"') arrayStringInner = !arrayStringInner;

                arrayValue.append(c);
            }

            return list;
        }

        final boolean isNumberLiteral = this.isNumberLiteral(value);
        final boolean isFloatingPointNumber = isNumberLiteral && value.contains(".");
        final boolean isLongNumber = isNumberLiteral && !isFloatingPointNumber && value.length() > 10;
        final boolean isDoubleFloatingPointNumber = isNumberLiteral && isFloatingPointNumber && this.isDoubleFloatingPoint(value);

        // @Note Numbers
        if (isNumberLiteral) {
            if (isFloatingPointNumber) {
                //
                // @Note A strange bug that occurs with the last statement from the block of "if (isNumberLiteral)",
                // make that this catch of if-statements must be created to fix this bug, the last statement
                // must be verified to can found the possible error.
                //  -rlp 31/10/2018 22:17
                //
                if(isDoubleFloatingPointNumber) {
                    return Double.parseDouble(value);
                } else {
                    return Float.parseFloat(value);
                }
            } else {
                return isLongNumber ? Long.parseLong(value) : Integer.parseInt(value);
            }
        }
        // @Note Strings
        else {
            return value;
        }
    }

    // @Note This method returns the "List<Object>" instead of already the "ComplexObject", because the main method to
    // do this work is set to "parse" method.
    private List<Object> parseArray(final String array) {
        return null;
    }

    private void readToBuffer(final File file) {
        try (final FileReader reader = new FileReader(file)) {
            int c; // char
            while ((c = reader.read()) != END_OF_STREAM) {
                this.buffer.add((char) c);
            }
        } catch (IOException e) {
            this.logger.severe("Occur an error when read the file \"" + file.getName() + "\" into buffer.");
            e.printStackTrace();
        }
    }

    private char next() {
        // @Note Get the current character that is located in the current position, and increment position.
        return this.buffer.get(this.pos++);
    }

    private char peek(final int n) {
        if (n < 0) throw new IllegalArgumentException("n can\'t be less than 0.");
        return this.buffer.get(this.pos + n);
    }

    private char peek() {
        return this.peek(0);
    }

    private void jump(final int n) {
        this.pos += n;
    }

    private void jump() {
        this.jump(1);
    }

    private void jumpLine() {
        // @Note Walk the line.
        for (int currentPos = this.pos; currentPos < this.buffer.size(); currentPos++) {
            if (this.isCarriageReturn(this.next())) {
                this.jump();
                break;
            }
        }
    }

    private boolean isWhitespace(final char c) {
        return c == 32;
    }

    private boolean isCarriageReturn(final char c) {
        return c == 13;
    }

    private boolean isNewLine(final char c) {
        return c == 10;
    }

    private boolean isNumberLiteral(final String string) {
        for (final char c : string.toCharArray()) {
            // @Note The number can be a "1.0".
            if (c == '.') continue;

            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDoubleFloatingPoint(final String string) {
        // @Note Single line code. The first part of the boolean condition check, represents the check from the integral
        // part of the floating-point number. In the second part of the boolean condition check, represents the check
        // from the mantissa of the floating-point number.
        // return string.substring(0, string.indexOf('.')).length() > 10 || string.substring(string.indexOf('.') + 1).length() > 7;

        final String mantissa = string.substring(string.indexOf('.') + 1);

        // @Note Check if the mantissa length is bigger than 7, this represents that the current flaoting-point number
        // is a double floating-point number, but if the mantissa is less than or equals 7 this is not guarantee that
        // the number will be a floating-point number or a double floating-point number, in this case check the integer
        // from the number.
        if (mantissa.length() > 7) return true;

        final String integralPart = string.substring(0, string.indexOf('.'));

        // @Note When the integral part from a number is bigger than 10, this represents a long number.
        return integralPart.length() > 10;
    }

    // @Test
    public List<Character> getBuffer() {
        return buffer;
    }

    // @Test
    public Map<String, ComplexObject> getVariables() {
        return variables;
    }

    // @Test
    public ComplexObject get(final String key) { return this.variables.get(key); }
}
