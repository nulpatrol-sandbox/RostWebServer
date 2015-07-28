package khaniukov.server;

import khaniukov.server.AccessControl.htaccessFileException;
import khaniukov.server.controller.ServerController;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * A set of static methods which providing different utility functions
 *
 * @author Rostislav Khaniukov
 */
public final class Utils {

    /**
     * Fetch file extension from path
     *
     * @param path path to file
     * @return an extension of specified file
     * @throws IOException
     */
    public static String getFileExtension(String path) throws IOException {
        String extension = "";
        int i = path.lastIndexOf('.');
        int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (i > p)
            extension = path.substring(i + 1);
        if (extension.equals("")) {
            throw new IOException("Specified file has not extension");
        }
        return extension;
    }

    /**
     * Print stacktrace to error log
     * @param e Exception to logging
     */
    public static void logStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        ServerController.errorLogger.error(sw.toString());
    }

    /**
     * Validate network mask
     * @param netMask network mask for validating
     * @throws htaccessFileException
     */
    public static void checkNetMask(String netMask) throws htaccessFileException {
        String[] ipParts = netMask.split("\\.");
        StringBuilder binaryIP = new StringBuilder("");
        for (String ipPart : ipParts) {
            binaryIP.append(Integer.toString(Integer.parseInt(ipPart), 2));
        }
        if (binaryIP.toString().contains("01")) {
            throw new htaccessFileException("Bad mask");
        }
    }

    /**
     * Check if string is like IP-address.
     * @param s string with potential IP-address
     * @return true if string is IP-like, false in another case
     */
    public static boolean isIPLike(String s) {
        if (s.endsWith(".")) {
            return false;
        }

        String PATTERN = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){0,3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])?$";
        Pattern pattern = Pattern.compile(PATTERN);

        return pattern.matcher(s).matches();
    }

    /**
     * Pack bytes in integer
     * @param bytes bytes to packing
     * @return integer packed from bytes
     */
    public static int pack(byte[] bytes) {
        int val = 0;
        for (byte oneByte : bytes) {
            val <<= 8;
            val |= oneByte & 0xff;
        }
        return val;
    }

    /**
     * Unpack integer to array of bytes
     * @param bytes packed integer
     * @return unpacked array
     */
    public static byte[] unpack(int bytes) {
        short[] tmp = new short[] {
                (short)((bytes >>> 24) & 0xff),
                (short)((bytes >>> 16) & 0xff),
                (short)((bytes >>>  8) & 0xff),
                (short)((bytes       ) & 0xff)
        };
        return new byte[]{
                (tmp[0] < 0) ? (byte) (tmp[0] + 256) : (byte) tmp[0],
                (tmp[1] < 0) ? (byte) (tmp[1] + 256) : (byte) tmp[1],
                (tmp[2] < 0) ? (byte) (tmp[2] + 256) : (byte) tmp[2],
                (tmp[3] < 0) ? (byte) (tmp[3] + 256) : (byte) tmp[3]
        };
    }

    /**
     * Convert CIDR network mask to canonical view of mask
     * @param mask CIDR mask in range 1-30
     * @return Canonical view of network mask
     * @throws UnknownHostException
     */
    public static InetAddress CIDRMaskToInetAddress(byte mask) throws UnknownHostException {
        int m = 0xffffffff << (32 - mask);
        return InetAddress.getByAddress(unpack(m));
    }

    /**
     * Calculate network address using IP-address and network mask
     * @param IPWithMask IP-address and network mask separated by slash
     * @return network address
     * @throws UnknownHostException
     */
    public static InetAddress getNetworkFromIPandNetMask(String IPWithMask) throws UnknownHostException {
        InetAddress IP      = InetAddress.getByName(IPWithMask.split("/")[0]);
        InetAddress netMask = InetAddress.getByName(IPWithMask.split("/")[1]);
        return InetAddress.getByAddress(unpack(pack(IP.getAddress())
                                             & pack(netMask.getAddress())));
    }

    /**
     * Read default file
     * @param name name of file
     * @return file as array of bytes
     */
    public static byte[] readDefaultPage(int name) {
        try {
            URL url = Utils.class.getResource("/default_pages/" + name + ".html");
            if (url == null) throw new NoSuchFileException("Cannot find /default_pages/" + name + ".html");
            return Files.readAllBytes(Paths.get(url.toURI()));
        } catch (Exception e) {
            Utils.logStackTrace(e);
        }
        return null;
    }
}
