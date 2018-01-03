package com.hq.arscresourcesparser.common;

import com.hq.arscresourcesparser.stream.PositionInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */

public class Utils {

    /**
     * read a char value from a byte array input stream
     *
     * @param mStreamer the byte array input stream
     * @return the short (8-bit) value
     * @throws java.io.IOException
     */
    public static short readUInt8(PositionInputStream mStreamer) throws IOException {
        byte[] bytes = new byte[1];
        mStreamer.read(bytes);
        return getUInt8(bytes);
    }

    /**
     * read a short value from a byte array input stream
     *
     * @param mStreamer the byte array input stream
     * @return the short (16-bit) value
     * @throws java.io.IOException
     */
    public static int readShort(PositionInputStream mStreamer) throws IOException {
        byte[] bytes = new byte[2];
        mStreamer.read(bytes);
        return getShort(bytes);
    }

    /**
     * read a int value from a byte array input stream
     *
     * @param mStreamer the byte array input stream
     * @return the int (32-bit) value
     * @throws java.io.IOException
     */
    public static long readInt(PositionInputStream mStreamer) throws IOException {
        byte[] bytes = new byte[4];
        mStreamer.read(bytes);
        return getInt(bytes);
    }
    
    /**
     * Read and Convert Chars (16-bit) to String. Terminated by 0x00 and Padding byte 0.
     *
     * @param mStreamer the byte array input stream
     * @param length string length
     * @return
     * @throws IOException
     */
    public static String readString(PositionInputStream mStreamer, int length) throws IOException {
        byte[] bytes = new byte[length];// The last byte is 0x00
        //mStreamer.read(bytes, 0,length);
        //bytes[length] = 0;
        mStreamer.read(bytes);
        try {
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    /**
     * Read and Convert Chars (16-bit) to String. Terminated by 0x00 and Padding byte 0.
     *
     * @param mStreamer the byte array input stream
     * @param length string length
     * @return
     * @throws IOException
     */
    public static String readString16(PositionInputStream mStreamer, int length) throws IOException {
        byte[] bytes = new byte[length];
        StringBuilder builder = new StringBuilder();
        mStreamer.read(bytes);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        byte[] buf_2 = new byte[2];
        while (in.read(buf_2) != -1) {
            int code = getShort(buf_2);
            if (code == 0x00)
                break;  // End of String
            else
                builder.append((char) code);
        }
        //builder.append((char) 0x00); // add null.
        return builder.toString();
    }

    /**
     * get a UInt8 value from a byte array
     *
     * @param bytes the byte array
     * @return the short (8-bit) value
     */
    public static short getUInt8(byte bytes[]) {
        return (short) (bytes[0] & 0xFF);
    }
    
    /**
     * get a short value from a byte array
     *
     * @param bytes the byte array
     * @return the short (16-bit) value
     */
    public static int getShort(byte[] bytes) {
        //return (short) ( ( data[1] & 0xFF << 8 ) + ( data[0] & 0xFF ) );
        return (int) (bytes[1] << 8 & 0xff00 | bytes[0] & 0xFF);
    }

    /**
     * get an int value from a byte array
     *
     * @param bytes the byte array
     * @return the int (32-bit) value
     */
    public static long getInt(byte[] bytes) {
        return (long) bytes[3]
                << 24 & 0xff000000
                | bytes[2]
                << 16 & 0xff0000
                | bytes[1]
                << 8 & 0xff00
                | bytes[0] & 0xFF;
    }
}
