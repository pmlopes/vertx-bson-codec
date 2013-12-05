package com.jetdrone.vertx.xson.java;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Date;

public final class ObjectId {

    private static final byte[] MACHINE = new byte[3];
    private static final int PID;
    private static int counter = 0;

    private final byte[] oid = new byte[12];

    static {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostname = "localhost";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] machine = md.digest(hostname.getBytes());
            ObjectId.MACHINE[0] = machine[0];
            ObjectId.MACHINE[1] = machine[1];
            ObjectId.MACHINE[2] = machine[2];
        } catch (Exception e) {
            ObjectId.MACHINE[0] = (byte) (Math.random() * 255);
            ObjectId.MACHINE[1] = (byte) (Math.random() * 255);
            ObjectId.MACHINE[2] = (byte) (Math.random() * 255);
        }

        int pid;
        try {
            String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
            int p = nameOfRunningVM.indexOf('@');
            pid = Integer.parseInt(nameOfRunningVM.substring(0, p));
        } catch (Exception e) {
            pid = (int) (Math.random() * 0x00ffffff);
        }

        PID = pid;

        // register a serializer on JSON
        JSON.addSerializer(new JsonSerializer<ObjectId>() {
            @Override
            public Class<ObjectId> handledType() {
                return ObjectId.class;
            }

            @Override
            public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (value == null) {
                    jgen.writeNull();
                } else {
                    jgen.writeString(value.toString());
                }
            }
        });
    }

    private static byte charToByte(char ch) {
        switch (ch) {
            case '0':
                return 0x00;
            case '1':
                return 0x01;
            case '2':
                return 0x02;
            case '3':
                return 0x03;
            case '4':
                return 0x04;
            case '5':
                return 0x05;
            case '6':
                return 0x06;
            case '7':
                return 0x07;
            case '8':
                return 0x08;
            case '9':
                return 0x09;
            case 'a':
            case 'A':
                return 0x0a;
            case 'b':
            case 'B':
                return 0x0b;
            case 'c':
            case 'C':
                return 0x0c;
            case 'd':
            case 'D':
                return 0x0d;
            case 'e':
            case 'E':
                return 0x0e;
            case 'f':
            case 'F':
                return 0x0f;
        }
        throw new RuntimeException("Invalid Hex character: " + ch);
    }

    private static char intToChar(int ch) {
        switch (ch) {
            case 0x00:
                return '0';
            case 0x01:
                return '1';
            case 0x02:
                return '2';
            case 0x03:
                return '3';
            case 0x04:
                return '4';
            case 0x05:
                return '5';
            case 0x06:
                return '6';
            case 0x07:
                return '7';
            case 0x08:
                return '8';
            case 0x09:
                return '9';
            case 0x0a:
                return 'a';
            case 0x0b:
                return 'b';
            case 0x0c:
                return 'c';
            case 0x0d:
                return 'd';
            case 0x0e:
                return 'e';
            case 0x0f:
                return 'f';
        }
        throw new RuntimeException("Invalid Hex character: " + ch);
    }

    private static byte hexToByte(char ch0, char ch1) {
        return (byte) ((charToByte(ch0) & 0xff) << 4 | (charToByte(ch1) & 0xff));
    }

    public ObjectId() {
        this((int) (System.currentTimeMillis() / 1000), counter++);
    }

    private ObjectId(int timestamp, int increment) {
        oid[0] = (byte) ((timestamp >> 24) & 0xff);
        oid[1] = (byte) ((timestamp >> 16) & 0xff);
        oid[2] = (byte) ((timestamp >> 8) & 0xff);
        oid[3] = (byte) (timestamp & 0xff);
        oid[4] = MACHINE[0];
        oid[5] = MACHINE[1];
        oid[6] = MACHINE[2];
        oid[7] = (byte) ((PID >> 8) & 0xff);
        oid[8] = (byte) (PID & 0xff);
        oid[9] = (byte) ((increment >> 16) & 0xff);
        oid[10] = (byte) ((increment >> 8) & 0xff);
        oid[11] = (byte) (increment & 0xff);
    }

    public ObjectId(String hex) {
        for (int i = 0; i < 12; i++) {
            oid[i] = hexToByte(hex.charAt(i * 2), hex.charAt(i * 2 + 1));
        }
    }

    public ObjectId(byte[] hex) {
        System.arraycopy(hex, 0, oid, 0, 12);
    }

    public int getTimestamp() {
        return oid[0] << 24 | (oid[1] & 0xff) << 16 | (oid[2] & 0xff) << 8 | (oid[3] & 0xff);
    }

    public Date getDate() {
        return new Date(getTimestamp() * 1000l);
    }

    public byte[] getMachine() {
        byte[] machine = new byte[3];
        System.arraycopy(oid, 4, machine, 0, 3);
        return machine;
    }

    public int getPid() {
        return (oid[7] & 0xff) << 8 | (oid[8] & 0xff);
    }

    public int getIncrement() {
        return (oid[9] & 0xff) << 16 | (oid[10] & 0xff) << 8 | (oid[11] & 0xff);
    }

    public byte[] getBytes() {
        return oid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (int i = 0; i < 12; i++) {
            result = prime * result + oid[i];
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        ObjectId rhs = (ObjectId) obj;

        for (int i = 0; i < 12; i++) {
            if (oid[i] != rhs.oid[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(intToChar((oid[i] >> 4) & 0x0f));
            sb.append(intToChar(oid[i] & 0x0f));
        }

        return sb.toString();
    }
}
