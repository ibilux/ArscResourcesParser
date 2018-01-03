package com.hq.arscresourcesparser.arsc;

import com.hq.arscresourcesparser.stream.PositionInputStream;
import java.io.IOException;


/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */

public class ResTableMap {

    // Bag resource ID
    public ResTableRef name;

    // Bag resource item value
    public ResValue value;

    public static ResTableMap parseFrom(PositionInputStream mStreamer) throws IOException {
        ResTableMap tableMap = new ResTableMap();
        tableMap.name = ResTableRef.parseFrom(mStreamer);
        tableMap.value = ResValue.parseFrom(mStreamer);
        return tableMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-10s {%s}\n", "name", name.toString()));
        builder.append("value:\n" + value.toString());
        return builder.toString();
    }

    public void translateValues(ResStringPoolChunk globalStringPool,
                                ResStringPoolChunk typeStringPool,
                                ResStringPoolChunk keyStringPool) {
        value.translateValues(globalStringPool, typeStringPool, keyStringPool);
    }
}
