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

public class ChunkHeader {

    public static final int LENGTH = 2 + 2 + 4;
    public int type;
    public int headerSize;
    public long chunkSize;

    public static ChunkHeader parseFrom(PositionInputStream mStreamer) throws IOException {
        ChunkHeader chunk = new ChunkHeader();
        chunk.type = Utils.readShort(mStreamer);
        chunk.headerSize = Utils.readShort(mStreamer);
        chunk.chunkSize = Utils.readInt(mStreamer);
        return chunk;
    }
}
