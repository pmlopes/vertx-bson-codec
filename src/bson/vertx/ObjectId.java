package bson.vertx;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public final class ObjectId {

    private static final byte[] MACHINE = new byte[3];
    private static final int PID;
    private static int counter = 0;

    // 4 bytes
    private final int timestamp;
    // 3 bytes
    private final byte[] machine = new byte[3];
    // 2 bytes
    private final int pid;
    // 3 bytes
    private final int increment;

    static {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 3 bytes MACHINE
            byte[] machine = md.digest(hostname.getBytes());
            ObjectId.MACHINE[0] = machine[0];
            ObjectId.MACHINE[1] = machine[1];
            ObjectId.MACHINE[2] = machine[2];
        } catch (NoSuchAlgorithmException e) {
            ObjectId.MACHINE[0] = '\0';
            ObjectId.MACHINE[1] = '\0';
            ObjectId.MACHINE[2] = '\0';
        }
        // PID
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        PID = Integer.parseInt(pid);
    }

    public ObjectId() throws Exception {
        // 4 bytes timestamps
        timestamp = (int) (System.currentTimeMillis() / 1000);
        System.arraycopy(MACHINE, 0, machine, 0, 3);
        pid = PID;
        increment = counter++;
    }

    private static byte charToInt(char ch) {
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

    private static byte hexToByte(char ch0, char ch1) {
        return (byte) ((charToInt(ch0) & 0xFF) << 4 | (charToInt(ch1) & 0xFF));
    }

    public ObjectId(String hex) {
        timestamp = hexToByte(hex.charAt(0), hex.charAt(1)) << 24 | (hexToByte(hex.charAt(2), hex.charAt(3)) & 0xFF) << 16 | (hexToByte(hex.charAt(4), hex.charAt(5)) & 0xFF) << 8 | (hexToByte(hex.charAt(6), hex.charAt(7)) & 0xFF);
        machine[0] = hexToByte(hex.charAt(8), hex.charAt(9));
        machine[1] = hexToByte(hex.charAt(10), hex.charAt(11));
        machine[2] = hexToByte(hex.charAt(12), hex.charAt(13));
        pid = (hexToByte(hex.charAt(14), hex.charAt(15)) & 0xFF) << 8 | (hexToByte(hex.charAt(16), hex.charAt(17)) & 0xFF);
        increment = (hexToByte(hex.charAt(18), hex.charAt(19)) & 0xFF) << 16 | (hexToByte(hex.charAt(20), hex.charAt(21)) & 0xFF) << 8 | (hexToByte(hex.charAt(22), hex.charAt(23)) & 0xFF);
    }

    public ObjectId(byte[] hex) {
        timestamp = hex[0] << 24 | (hex[1] & 0xFF) << 16 | (hex[2] & 0xFF) << 8 | (hex[3] & 0xFF);
        System.arraycopy(hex, 4, machine, 0, 3);
        pid = (hex[7] & 0xFF) << 8 | (hex[8] & 0xFF);
        increment = (hex[9] & 0xFF) << 16 | (hex[10] & 0xFF) << 8 | (hex[11] & 0xFF);
    }

    public int getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return new Date(timestamp * 1000l);
    }

    public byte[] getMachine() {
        return machine;
    }

    public int getPid() {
        return pid;
    }

    public int getIncrement() {
        return increment;
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean equals(Object other) {
        throw new RuntimeException("Not implemented yet");
    }
}
