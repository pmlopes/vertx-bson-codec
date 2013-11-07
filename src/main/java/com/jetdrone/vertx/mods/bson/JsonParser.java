package com.jetdrone.vertx.mods.bson;

import org.vertx.java.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class JsonParser {

    private final Buffer buffer;
    private int index;
    private byte current;
    private Buffer captureBuffer;
    private int captureStart;

    JsonParser(Buffer buffer) {
        this.buffer = buffer;
        captureStart = -1;
    }

    Object parse() {
        read();
        skipWhiteSpace();
        Object result = readValue();
        skipWhiteSpace();
        isNotEndOfText();
        return result;
    }

    private Object readValue() {
        switch (current) {
            case 'n':
                return readNull();
            case 't':
                return readTrue();
            case 'f':
                return readFalse();
            case '"':
                return readString();
            case '[':
                return readArray();
            case '{':
                return readObject();
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return readNumber();
            default:
                isEndOfText();
                throw new RuntimeException("Expected value");
        }
    }

    private List readArray() {
        read();
        List<Object> array = new ArrayList<>();
        skipWhiteSpace();
        if (readByte((byte) ']')) {
            return array;
        }
        do {
            skipWhiteSpace();
            array.add(readValue());
            skipWhiteSpace();
        } while (readByte((byte) ','));
        if (!readByte((byte) ']')) {
            isEndOfText();
            throw new RuntimeException("Expected ',' or ']'");

        }
        return array;
    }

    private Map readObject() {
        read();
        Map<String, Object> object = new HashMap<>();
        skipWhiteSpace();
        if (readByte((byte) '}')) {
            return object;
        }
        do {
            skipWhiteSpace();
            String name = readName();
            skipWhiteSpace();
            if (!readByte((byte) ':')) {
                isEndOfText();
                throw new RuntimeException("Expected ':'");

            }
            skipWhiteSpace();
            object.put(name, readValue());
            skipWhiteSpace();
        } while (readByte((byte) ','));
        if (!readByte((byte) '}')) {
            isEndOfText();
            throw new RuntimeException("Expected ',' or '}'");

        }
        return object;
    }

    private String readName() {
        if (current != '"') {
            isEndOfText();
            throw new RuntimeException("Expected name");

        }
        return readStringInternal();
    }

    private Object readNull() {
        read();
        readRequiredByte((byte) 'u');
        readRequiredByte((byte) 'l');
        readRequiredByte((byte) 'l');
        return null;
    }

    private Object readTrue() {
        read();
        readRequiredByte((byte) 'r');
        readRequiredByte((byte) 'u');
        readRequiredByte((byte) 'e');
        return true;
    }

    private Object readFalse() {
        read();
        readRequiredByte((byte) 'a');
        readRequiredByte((byte) 'l');
        readRequiredByte((byte) 's');
        readRequiredByte((byte) 'e');
        return false;
    }

    private void readRequiredByte(byte ch) {
        if (!readByte(ch)) {
            isEndOfText();
            throw new RuntimeException("Expected '" + ch + "'");

        }
    }

    private Object readString() {
        return readStringInternal();
    }

    private String readStringInternal() {
        read();
        startCapture();
        while (current != '"') {
            if (current == '\\') {
                pauseCapture();
                readEscape();
                startCapture();
            } else if (current < 0x20) {
                isEndOfText();
                throw new RuntimeException("Expected valid string character");

            } else {
                read();
            }
        }
        String string = endCapture();
        read();
        return string;
    }

    private void readEscape() {
        read();
        switch (current) {
            case '"':
            case '/':
            case '\\':
                captureBuffer.appendByte((byte) current);
                break;
            case 'b':
                captureBuffer.appendByte((byte) '\b');
                break;
            case 'f':
                captureBuffer.appendByte((byte) '\f');
                break;
            case 'n':
                captureBuffer.appendByte((byte) '\n');
                break;
            case 'r':
                captureBuffer.appendByte((byte) '\r');
                break;
            case 't':
                captureBuffer.appendByte((byte) '\t');
                break;
            case 'u':
                int hex = 0;
                for (int i = 0; i < 4; i++) {
                    read();
                    if (!isHexDigit()) {
                        isEndOfText();
                        throw new RuntimeException("Expected hexadecimal digit");

                    }
                    hex = hex << 4 | getHexDigit(current);
                }
                char unicode = (char) hex;
                captureBuffer.appendString(new String(new char[] {unicode}));
                break;
            default:
                isEndOfText();
                throw new RuntimeException("Expected valid escape sequence");

        }
        read();
    }

    private Object readNumber() {
        startCapture();
        readByte((byte) '-');
        int firstDigit = current;
        if (!readDigit()) {
            isEndOfText();
            throw new RuntimeException("Expected digit");

        }
        if (firstDigit != '0') {
            while (readDigit()) {
            }
        }
        boolean frac = readFraction();
        boolean exp = readExponent();

        if (!frac && !exp) {
            return new Long(endCapture());
        } else {
            return new Double(endCapture());
        }
    }

    private boolean readFraction() {
        if (!readByte((byte) '.')) {
            return false;
        }
        if (!readDigit()) {
            isEndOfText();
            throw new RuntimeException("Expected digit");

        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readExponent() {
        if (!readByte((byte) 'e') && !readByte((byte) 'E')) {
            return false;
        }
        if (!readByte((byte) '+')) {
            readByte((byte) '-');
        }
        if (!readDigit()) {
            isEndOfText();
            throw new RuntimeException("Expected digit");

        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readByte(byte ch) {
        if (current != ch) {
            return false;
        }
        read();
        return true;
    }

    private boolean readDigit() {
        if (!isDigit()) {
            return false;
        }
        read();
        return true;
    }

    private void skipWhiteSpace() {
        while (isWhiteSpace()) {
            read();
        }
    }

    private void read() {
        isEndOfText();
        if (index == buffer.length()) {
            current = -1;
            return;
        }
        current = buffer.getByte(index++);
    }

    private void startCapture() {
        if (captureBuffer == null) {
            captureBuffer = new Buffer();
        }
        captureStart = index - 1;
    }

    private void pauseCapture() {
        int end = current == -1 ? index : index - 1;
        captureBuffer.appendBuffer(buffer.getBuffer(captureStart, end));
        captureStart = -1;
    }

    private String endCapture() {
        int end = current == -1 ? index : index - 1;
        String captured;
        if (captureBuffer.length() > 0) {
            captureBuffer.appendBuffer(buffer.getBuffer(captureStart, end));
            captured = captureBuffer.toString();
            captureBuffer = new Buffer();
        } else {
            captured = buffer.getString(captureStart, end);
        }
        captureStart = -1;
        return captured;
    }

    private boolean isWhiteSpace() {
        return current == ' ' || current == '\t' || current == '\n' || current == '\r';
    }

    private boolean isDigit() {
        return current >= '0' && current <= '9';
    }

    private boolean isHexDigit() {
        return current >= '0' && current <= '9'
                || current >= 'a' && current <= 'f'
                || current >= 'A' && current <= 'F';
    }

    private int getHexDigit(int ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'f') {
            return 10 + ch - 'a';
        }
        if (ch >= 'A' && ch <= 'F') {
            return 10 + ch - 'A';
        }

        throw new RuntimeException("Expected hex character '" + (char) ch + "'");
    }

    private void isEndOfText() {
        if (current == -1) {
            throw new RuntimeException("Unexpected end of input");
        }
    }

    private void isNotEndOfText() {
        if (current != -1) {
            throw new RuntimeException("Unexpected end of input");
        }
    }
}
