package bson.vertx;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ObjectId {

    private static byte[] machine = new byte[3];
    private static byte[] pid = new byte[2];
    private static int counter = 0;

    private int increment;
    private int timestamp;

    static {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 3 bytes machine
            byte[] machine = md.digest(hostname.getBytes());
            ObjectId.machine[0] = machine[0];
            ObjectId.machine[1] = machine[1];
            ObjectId.machine[2] = machine[2];
        } catch (NoSuchAlgorithmException e) {
            ObjectId.machine[0] = '\0';
            ObjectId.machine[1] = '\0';
            ObjectId.machine[2] = '\0';
        }
        // pid
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        ObjectId.pid[0] = (byte) pid.charAt(0);
        ObjectId.pid[0] = (byte) pid.charAt(0);
    }

    public ObjectId() throws Exception {
        // 4 bytes timestamps
        timestamp = (int) (System.currentTimeMillis() / 1000);
        increment = counter++;
    }

    public ObjectId(String hex) {
        for (int i = 0; i < 12; i++) {
            System.out.println(Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16));
        }
    }
}
