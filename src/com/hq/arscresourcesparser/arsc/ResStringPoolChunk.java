package com.hq.arscresourcesparser.arsc;

import com.hq.arscresourcesparser.common.Utils;
import com.hq.arscresourcesparser.stream.PositionInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class ResStringPoolChunk {

    // If set, the string index is sorted by the string values (based on strcmp16()).
    private static final int SORTED_FLAG = 1;
    // String pool is encoded in UTF-8
    private static final int UTF8_FLAG = 1 << 8;

    // Header Block 0x001c
    public ChunkHeader header;
    // Number of style span arrays in the pool (number of uint32_t indices
    // follow the string indices).
    public long stringCount;
    // Number of style span arrays in the pool (number of uint32_t indices
    // follow the string indices).
    public long styleCount;
    public long flags;
    // Index from header of the string data (Offset from this chunk starting).
    public long stringsStart;
    // Index from header of the style data.
    public long stylesStart;
    // Data Block
    public long stringOffsetArray[];    // Offset from string pool. The first one is 0x00000000
    public long styleOffsetArray[];
    public List<String> strings;
    public List<String> styles;

    public static ResStringPoolChunk parseFrom(PositionInputStream mStreamer) throws IOException {
        long baseCursor = mStreamer.getPosition();

        ResStringPoolChunk chunk = new ResStringPoolChunk();
        chunk.header = ChunkHeader.parseFrom(mStreamer);
        chunk.stringCount = Utils.readInt(mStreamer);
        chunk.styleCount = Utils.readInt(mStreamer);
        chunk.flags = Utils.readInt(mStreamer); // read flag
        chunk.stringsStart = Utils.readInt(mStreamer);
        chunk.stylesStart = Utils.readInt(mStreamer);

        // the string index is sorted by the string values if true
        boolean sorted = (chunk.flags & SORTED_FLAG) != 0;
        // string use utf-8 format if true, otherwise utf-16
        boolean utf8 = (chunk.flags & UTF8_FLAG) != 0;

        long[] strOffsets = chunk.stringOffsetArray = new long[(int) chunk.stringCount];
        long[] styleOffsets = chunk.styleOffsetArray = new long[(int) chunk.styleCount];
        List<String> strings = chunk.strings = new ArrayList<>((int) chunk.stringCount);
        List<String> styles = chunk.styles = new ArrayList<>((int) chunk.styleCount);

        // read strings offset
        for (int i = 0; i < chunk.stringCount; ++i) {
            strOffsets[i] = Utils.readInt(mStreamer);
        }
        for (int i = 0; i < chunk.styleCount; ++i) {
            styleOffsets[i] = Utils.readInt(mStreamer);
        }
        for (int i = 0; i < chunk.stringCount; ++i) {
            long start = baseCursor + chunk.stringsStart + strOffsets[i];
            mStreamer.seek(start);
            //int len = (Utils.readShort(mStreamer) & 0x7f00) >> 8;
            //int len = Utils.readShort(mStreamer);
            /*
             * Each String entry contains Length header (2 bytes to 4 bytes) + Actual String + [0x00]
             * Length header sometime contain duplicate values e.g. 20 20
             * Actual string sometime contains 00, which need to be ignored
             * Ending zero might be  2 byte or 4 byte
             * 
             * TODO: Consider both Length bytes and String length > 32767 characters 
             */
 /*
            int len =0; 
            byte[] buf2 = new byte[2];
            mStreamer.read(buf2);
            if (buf2[0] == buf2[1])  // Its repeating, happens for Non-Manifest file. e.g. 20 20
                len = buf2[0];
            else
                len = Utils.getShort(buf2);
             */
            if (utf8) {
                //  The lengths are encoded in the same way as for the 16-bit format
                // but using 8-bit rather than 16-bit integers.
                int strlen = Utils.readUInt8(mStreamer);
                int len = Utils.readUInt8(mStreamer);
                String str = Utils.readString(mStreamer, len);
                strings.add(str);
            } else {
                // The length is encoded as either one or two 16-bit integers as per the commentRef...
                //int len = (Utils.readShort(mStreamer) & 0x7f00) >> 8;
                int len = Utils.readShort(mStreamer);
                String str = Utils.readString16(mStreamer, len * 2);
                strings.add(str);
            }
            //String str = Utils.readString16(mStreamer, len); // The last byte is 0x00
            //String str = s.readNullEndString(len); // The last byte is 0x00
        }
        for (int i = 0; i < chunk.styleCount; ++i) {
            long start = baseCursor + chunk.stylesStart + styleOffsets[i];
            mStreamer.seek(start);
            int len = (Utils.readShort(mStreamer) & 0x7f00) >> 8;
            //String str = Utils.readString32(mStreamer, len); // The last byte is 0x00
            String str = Utils.readString(mStreamer, len);
            styles.add(str);
        }

        return chunk;
    }

    public String getString(int idx) {
        try{
            return strings != null && idx < strings.size() ? strings.get(idx) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getStyle(int idx) {
        return styles != null && idx < styles.size() ? styles.get(idx) : null;
    }

}
