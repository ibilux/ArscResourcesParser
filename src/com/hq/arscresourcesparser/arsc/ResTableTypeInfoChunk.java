package com.hq.arscresourcesparser.arsc;

import com.hq.arscresourcesparser.common.Utils;
import com.hq.arscresourcesparser.stream.PositionInputStream;
import java.io.IOException;

import java.util.List;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class ResTableTypeInfoChunk extends BaseTypeChunk {

    public static final long NO_ENTRY = 0xffffffffL;

    public ChunkHeader header;
    public int typeId;      // 1byte    resource type 0x00ff0000
    public int res0;        // 1byte
    public int res1;        // 2byte
    public long entryCount;
    public long entriesStart;       // start of table entries.
    public ResTableConfig resConfig;

    // Data Block
    public long[] entryOffsets;     // offset of table entries.
    public ResTableEntry[] tableEntries;

    public static ResTableTypeInfoChunk parseFrom(PositionInputStream mStreamer, ResStringPoolChunk stringChunk) throws IOException {
        ResTableTypeInfoChunk chunk = new ResTableTypeInfoChunk();
        long start = mStreamer.getPosition();
        chunk.header = ChunkHeader.parseFrom(mStreamer);
        chunk.typeId = Utils.readUInt8(mStreamer);
        chunk.res0 = Utils.readUInt8(mStreamer);
        chunk.res1 = Utils.readShort(mStreamer);
        chunk.entryCount = Utils.readInt(mStreamer);
        chunk.entriesStart = Utils.readInt(mStreamer);
        chunk.resConfig = ResTableConfig.parseFrom(mStreamer);

        // read offsets table
        chunk.entryOffsets = new long[(int) chunk.entryCount];
        for (int i = 0; i < chunk.entryCount; ++i) {
            chunk.entryOffsets[i] = Utils.readInt(mStreamer);
        }
        // read entry
        chunk.tableEntries = new ResTableEntry[(int) chunk.entryCount];
        mStreamer.seek(start + chunk.entriesStart); // Locate entry start point.
        for (int i = 0; i < chunk.entryCount; ++i) {
            // This is important!
            if (chunk.entryOffsets[i] == NO_ENTRY || chunk.entryOffsets[i] == -1) {
                continue;
            }

            long cursor = mStreamer.getPosition();     // Remember the start cursor
            ResTableEntry entry = ResTableEntry.parseFrom(mStreamer);

            mStreamer.seek(cursor);                 // Rest cursor
            // We need to parse entry into ResTableMapEntry instead of ResTableMapEntry
            if (entry.flags == ResTableEntry.FLAG_COMPLEX) {
                entry = ResTableMapEntry.parseFrom(mStreamer);      // Complex ResTableMapEntry
            } else {
                entry = ResTableValueEntry.parseFrom(mStreamer);    // ResTableEntry follows a ResValue
            }
            int x1 = entry.entryId;                 // Remember entry index in tableEntries to recover ids in public.xml
            int x2 = i;
            entry.entryId = i;
            chunk.tableEntries[i] = entry;
        }

        return chunk;
    }

    @Override
    public String getChunkName() {
        return "ResTableTypeInfoChunk";
    }

    @Override
    public long getEntryCount() {
        return entryCount;
    }

    @Override
    public String getType() {
        return String.format("0x%s", (typeId));
    }

    public int getTypeId() {
        return typeId;
    }

    @Override
    public void translateValues(ResStringPoolChunk globalStringPool, ResStringPoolChunk typeStringPool, ResStringPoolChunk keyStringPool) {
        for (ResTableEntry entry : tableEntries) {
            if (entry != null) {
                entry.translateValues(globalStringPool, typeStringPool, keyStringPool);
            }
        }
    }

    public ResTableEntry getResource(int resId) {
        int entryId = resId & 0x0000ffff;
        //int entryIndex = (int) (resId & 0xffff);
        //return entryId < tableEntries.length ? tableEntries[entryId] : null;
        for (ResTableEntry entry : tableEntries) {
            //LogUtil.i("", "Entry ID : " + entry.entryId);
            if (entry.entryId == entryId) {
                return entry;
            }
        }
        return null;
    }

    public static String uniqueEntries2String(int packageId,
            ResStringPoolChunk typeStringPool,
            ResStringPoolChunk keyStringPool,
            List<ResTableTypeInfoChunk> typeInfos) {
        StringBuilder builder = new StringBuilder();

        int configCount = typeInfos.size();
        int entryCount;
        try {
            entryCount = (int) typeInfos.get(0).entryCount;
        } catch (Exception e) {
            entryCount = 0;
        }

        for (int i = 0; i < entryCount; ++i) {
            for (int j = 0; j < configCount; ++j) {
                String entryStr = typeInfos.get(j).buildEntry2String(i, packageId, typeStringPool, keyStringPool);
                if (entryStr != null && entryStr.length() > 0) {
                    builder.append(entryStr);
                    break;  // This entryId has done.
                }
            }
        }
        return builder.toString();
    }

    public String buildEntry2String(int entryId, int packageId, ResStringPoolChunk typeStringPool, ResStringPoolChunk keyStringPool) {
        for (ResTableEntry entry : tableEntries) {
            String typeStr = typeStringPool.getString(typeId - 1);  // from 1
            if (entry != null) {
                if (entry.entryId == entryId) {
                    return ("<public id=\"0x" + String.format("%02x", packageId) + String.format("%02x", typeId) + String.format("%04x", entryId) + "\" type=\"" + typeStr + "\" " + entry.toString() + "/>\"" + System.lineSeparator());
                }
            }
        }
        return null;
    }
}
