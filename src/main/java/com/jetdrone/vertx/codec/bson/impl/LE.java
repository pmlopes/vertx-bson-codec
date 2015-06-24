package com.jetdrone.vertx.codec.bson.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;

import java.nio.charset.Charset;

public final class LE {

  private static final Charset UTF8 = Charset.forName("UTF-8");

  public static void appendBoolean(Buffer buffer, boolean value) {
    buffer.appendByte(value ? (byte) 0x01 : (byte) 0x00);
  }

  public static void appendByte(Buffer buffer, byte value) {
    buffer.appendByte(value);
  }

  public static void appendShort(Buffer buffer, short value) {
    buffer.appendShort(Short.reverseBytes(value));
  }

  public static void appendInt(Buffer buffer, int value) {
    buffer.appendInt(Integer.reverseBytes(value));
  }

  public static void appendLong(Buffer buffer, long value) {
    buffer.appendLong(Long.reverseBytes(value));
  }

  public static void appendFloat(Buffer buffer, float value) {
    int fvalue = Float.floatToRawIntBits(value);
    appendInt(buffer, fvalue);
  }

  public static void appendDouble(Buffer buffer, double value) {
    long dvalue = Double.doubleToRawLongBits(value);
    appendLong(buffer, dvalue);
  }

  public static void appendBytes(Buffer buffer, byte[] value) {
    buffer.appendBytes(value);
  }

  public static void appendChar(Buffer buffer, char value) {
    buffer.appendByte((byte) value);
  }

  public static void appendCString(Buffer buffer, String value) {
    byte[] bytes = value.getBytes(UTF8);
    // validate if it is a real C string
    for (byte aByte : bytes) {
      if (aByte == '\0') {
        throw new EncodeException("Key: '" + value + "' is not a CString");
      }
    }
    buffer.appendBytes(bytes);
    buffer.appendByte((byte) 0x00);
  }

  // TODO: this is wrong i am mixing BSON encoding with generic LE encoding
  public static void appendString(Buffer buffer, String value) {
    byte[] bytes = value.getBytes(UTF8);
    appendInt(buffer, bytes.length + 1);
    buffer.appendBytes(bytes);
    buffer.appendByte((byte) 0x00);
  }

  public static void setByte(Buffer buffer, int pos, byte value) {
    buffer.setByte(pos, value);
  }

  public static void setBytes(Buffer buffer, int pos, byte[] value) {
    buffer.setBytes(pos, value);
  }

  public static void setShort(Buffer buffer, int pos, short value) {
    buffer.setShort(pos, Short.reverseBytes(value));
  }

  public static void setInt(Buffer buffer, int pos, int value) {
    buffer.setInt(pos, Integer.reverseBytes(value));
  }

  public static void setLong(Buffer buffer, int pos, long value) {
    buffer.setLong(pos, Long.reverseBytes(value));
  }

  public static void setFloat(Buffer buffer, int pos, float value) {
    int fvalue = Float.floatToRawIntBits(value);
    setInt(buffer, pos, fvalue);
  }

  public static void setDouble(Buffer buffer, int pos, double value) {
    long dvalue = Double.doubleToRawLongBits(value);
    setLong(buffer, pos, dvalue);
  }

  public static boolean getBoolean(Buffer buffer, int pos) {
    byte b = buffer.getByte(pos);
    if (b == (byte) 0x00) {
      return false;
    }
    if (b == (byte) 0x01) {
      return true;
    }
    throw new DecodeException((int) b + " is not a valid boolean value");
  }

  public static byte getByte(Buffer buffer, int pos) {
    return buffer.getByte(pos);
  }

  public static byte[] getBytes(Buffer buffer, int pos, int length) {
    return buffer.getBytes(pos, pos + length);
  }

  public static int getInt(Buffer buffer, int pos) {
    return Integer.reverseBytes(buffer.getInt(pos));
  }

  public static long getLong(Buffer buffer, int pos) {
    return Long.reverseBytes(buffer.getLong(pos));
  }

  public static float getFloat(Buffer buffer, int pos) {
    return Float.intBitsToFloat(getInt(buffer, pos));
  }

  public static double getDouble(Buffer buffer, int pos) {
    return Double.longBitsToDouble(getLong(buffer, pos));
  }

  public static String getCString(Buffer buffer, int pos) {
    int end = pos;
    while (buffer.getByte(end) != (byte) 0x00) {
      end++;
    }
    return buffer.getString(pos, end, "UTF-8");
  }

  public static String getString(Buffer buffer, int pos, int length) {
    return buffer.getString(pos, pos + length, "UTF-8");
  }
}
