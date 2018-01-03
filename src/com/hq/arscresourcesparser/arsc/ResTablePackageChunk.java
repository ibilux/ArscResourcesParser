package com.hq.arscresourcesparser.arsc;

import com.hq.arscresourcesparser.common.Utils;
import com.hq.arscresourcesparser.stream.PositionInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class ResTablePackageChunk {

    public static final String TAG = ResTablePackageChunk.class.getSimpleName();

    public static final int RES_TABLE_TYPE_SPEC_TYPE = 0x0202;
    public static final int RES_TABLE_TYPE_TYPE = 0x0201;

    // Header Block 0x0120
    public ChunkHeader header;
    public long pkgId;                 // 0x0000007f->UserResources  0x00000001->SystemResources
    public String packageName;
    public long typeStringOffset;   // Offset in this chunk
    public long lastPublicType;     // Num of type string
    public long keyStringOffset;    // Offset in chunk
    public long lastPublicKey;      // Num of key string

    // DataBlock
    public ResStringPoolChunk typeStringPool;
    public ResStringPoolChunk keyStringPool;
    public List<BaseTypeChunk> typeChunks;

    // Create Index
    public Map<Integer, List<BaseTypeChunk>> typeInfoIndexer;

    public static ResTablePackageChunk parseFrom(PositionInputStream mStreamer, ResStringPoolChunk stringChunk) throws IOException {
        ResTablePackageChunk chunk = new ResTablePackageChunk();
        chunk.header = ChunkHeader.parseFrom(mStreamer);
        chunk.pkgId = Utils.readInt(mStreamer);
        chunk.packageName = Utils.readString16(mStreamer, 128 * 2);
        chunk.typeStringOffset = Utils.readInt(mStreamer);
        chunk.lastPublicType = Utils.readInt(mStreamer);
        chunk.keyStringOffset = Utils.readInt(mStreamer);
        chunk.lastPublicKey = Utils.readInt(mStreamer);

        // Data Block
        mStreamer.seek(chunk.typeStringOffset);
        chunk.typeStringPool = ResStringPoolChunk.parseFrom(mStreamer);
        mStreamer.seek(chunk.keyStringOffset);
        chunk.keyStringPool = ResStringPoolChunk.parseFrom(mStreamer);

        // TableTypeSpecType   TableTypeType
        mStreamer.seek(chunk.keyStringOffset + chunk.keyStringPool.header.chunkSize);
        chunk.typeChunks = new ArrayList<>();
        int resCount = 0;
        StringBuilder logInfo = new StringBuilder();
        while (mStreamer.available() > 0) {
            int x = mStreamer.available();
            logInfo.setLength(0);
            resCount++;
            ChunkHeader header = ChunkHeader.parseFrom(mStreamer);

            BaseTypeChunk typeChunk = null;
            if (header.type == RES_TABLE_TYPE_SPEC_TYPE) {
                mStreamer.seek(mStreamer.getPosition() - ChunkHeader.LENGTH);
                typeChunk = ResTableTypeSpecChunk.parseFrom(mStreamer, stringChunk);
            } else if (header.type == RES_TABLE_TYPE_TYPE) {
                mStreamer.seek(mStreamer.getPosition() - ChunkHeader.LENGTH);
                typeChunk = ResTableTypeInfoChunk.parseFrom(mStreamer, stringChunk);
            }
            if (typeChunk != null) {
                logInfo.append(typeChunk.getChunkName()).append(" ")
                        .append(String.format("type=%s ", typeChunk.getType()))
                        .append(String.format("count=%s ", typeChunk.getEntryCount()));
            } else {
                logInfo.append("None TableTypeSpecType or TableTypeType!!");
            }

            if (typeChunk != null) {
                chunk.typeChunks.add(typeChunk);
            }
        }

        chunk.createResourceIndex();
        for (int i = 0; i < chunk.typeChunks.size(); ++i) {
            chunk.typeChunks.get(i).translateValues(stringChunk, chunk.typeStringPool, chunk.keyStringPool);
        }

        return chunk;
    }

    private void createResourceIndex() {
        typeInfoIndexer = new HashMap<Integer, List<BaseTypeChunk>>();
        for (BaseTypeChunk typeChunk : typeChunks) {
            // The first chunk in typeList should be ResTableTypeSpecChunk
            List<BaseTypeChunk> typeList = typeInfoIndexer.get(typeChunk.getTypeId());
            if (typeList == null) {
                typeList = new ArrayList<BaseTypeChunk>();
                typeInfoIndexer.put(typeChunk.getTypeId(), typeList);
                if (typeChunk.getTypeId() == 2) {
                    int x = 4;
                }
            }
            typeList.add(typeChunk);
        }
    }

    public ResTableEntry getResource(int resId) {
        int typeId = (resId & 0x00ff0000) >> 16;
        //short typeIdx = (short) ((resId >> 16) & 0xff);
        List<BaseTypeChunk> typeList = typeInfoIndexer.get(typeId); // The first chunk in typeList should be ResTableTypeSpecChunk
        for (int i = 1; i < typeList.size(); ++i) {
            if (typeList.get(i) instanceof ResTableTypeInfoChunk) {
                ResTableTypeInfoChunk x = (ResTableTypeInfoChunk) typeList.get(i);
                ResTableEntry entry = ((ResTableTypeInfoChunk) typeList.get(i)).getResource(resId);
                if (entry != null) {
                    return entry;
                }
            }
        }
        return null;
    }

    public String buildEntry2String() {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + System.lineSeparator());
        builder.append("<resources>" + System.lineSeparator());

        for (int i = 0; i < typeChunks.size(); ++i) {
            // All entries exist in ResTableTypeInfoChunk
            if (typeChunks.get(i) instanceof ResTableTypeSpecChunk) {
                // Extract following ResTableTypeInfoChunks
                List<ResTableTypeInfoChunk> typeInfos = new ArrayList<ResTableTypeInfoChunk>();
                for (int j = i + 1; j < typeChunks.size(); ++j) {
                    if (typeChunks.get(j) instanceof ResTableTypeInfoChunk) {
                        typeInfos.add((ResTableTypeInfoChunk) typeChunks.get(j));
                    } else {
                        break;
                    }
                }
                i += typeInfos.size();
                // Unique ResTableTypeInfoChunks
                String entry = ResTableTypeInfoChunk.uniqueEntries2String((int) pkgId & 0xff, typeStringPool, keyStringPool, typeInfos);
                builder.append("\t" + entry + System.lineSeparator());
            }
        }

        builder.append("</resources>");
        return builder.toString();
    }
}
