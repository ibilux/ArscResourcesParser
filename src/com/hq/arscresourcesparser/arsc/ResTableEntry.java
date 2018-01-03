package com.hq.arscresourcesparser.arsc;

import com.hq.arscresourcesparser.common.Utils;
import com.hq.arscresourcesparser.stream.PositionInputStream;
import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */

public class ResTableEntry {

    // If set, this is a complex entry, holding a set of name/value. It is followed by an array of ResTableMap structures.
    public static final int FLAG_COMPLEX = 0x0001;
    // If set, this resource has been declared public, so libraries are allowed to reference it.
    public static final int FLAG_PUBLIC = 0x0002;

    public int size;    // short
    public int flags;   // short
    public ResStringPoolRef key;    // Reference into ResTablePackage::keyStrings identifying this entry.

    public int entryId; // 16bit  0x7f01nnnn
    public String keyStr;

    public static ResTableEntry parseFrom(PositionInputStream mStreamer) throws IOException {
        ResTableEntry entry = new ResTableEntry();
        parseFrom(mStreamer, entry);
        return entry;
    }

    public static void parseFrom(PositionInputStream mStreamer, ResTableEntry entry) throws IOException {
        entry.size = Utils.readShort(mStreamer);
        entry.flags = Utils.readShort(mStreamer);
        entry.key = ResStringPoolRef.parseFrom(mStreamer);
    }
    
    @Override
    public String toString() {
        return " ";
    }

    public void translateValues(ResStringPoolChunk globalStringPool,
                                ResStringPoolChunk typeStringPool,
                                ResStringPoolChunk keyStringPool) {
        keyStr = keyStringPool.getString((int) key.index);
    }
}
