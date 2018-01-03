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

public class ResTableTypeSpecChunk extends BaseTypeChunk {

    public ChunkHeader header;
    public int typeId;      // 1byte
    public int res0;    // 1byte
    public int res1;    // 2byte
    public long entryCount;
    public long[] entryConfig;

    public static ResTableTypeSpecChunk parseFrom(PositionInputStream mStreamer, ResStringPoolChunk stringChunk) throws IOException {
        ResTableTypeSpecChunk chunk = new ResTableTypeSpecChunk();
        chunk.header = ChunkHeader.parseFrom(mStreamer);
        chunk.typeId = Utils.readUInt8(mStreamer);
        chunk.res0 = Utils.readUInt8(mStreamer);
        chunk.res1 = Utils.readShort(mStreamer);
        chunk.entryCount = Utils.readInt(mStreamer);
        chunk.entryConfig = new long[(int) chunk.entryCount];

        for (int i=0; i<chunk.entryCount; ++i) {
            chunk.entryConfig[i] = Utils.readInt(mStreamer);
        }
        return chunk;
    }

    @Override
    public String getChunkName() {
        return "ResTableTypeSpecChunk";
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
        // Ignored
    }
}
