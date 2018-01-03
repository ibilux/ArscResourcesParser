package com.hq.arscresourcesparser.arsc;

import com.hq.arscresourcesparser.stream.PositionInputStream;
import java.io.ByteArrayInputStream;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class ArscFile {

    private static final String TAG = ArscFile.class.getSimpleName();

    private static final int RES_TABLE_TYPE = 0x0002;
    private static final int RES_STRING_POOL_TYPE = 0x0001;
    private static final int RES_TABLE_PACKAGE_TYPE = 0x0200;

    private ByteArrayInputStream mStreamer;
    public ResFileHeaderChunk arscHeader;
    public ResStringPoolChunk resStringPoolChunk;
    public ResTablePackageChunk resTablePackageChunk;

    public ArscFile() {

    }

    public void parse(byte[] sBuf) throws IOException {
        mStreamer = new ByteArrayInputStream(sBuf);

        byte[] headBytes;
        byte[] chunkBytes;
        long cursor = 0;
        ChunkHeader header;

        // Preload file header. The chunkSize represents the complete file length.
        chunkBytes = new byte[ResFileHeaderChunk.LENGTH];
        mStreamer.read(chunkBytes, 0, chunkBytes.length);
        header = ChunkHeader.parseFrom(new PositionInputStream(new ByteArrayInputStream(chunkBytes)));
        if (header.type != RES_TABLE_TYPE) {
            return;
        }
        // Post load file header.
        mStreamer.reset();
        chunkBytes = new byte[header.headerSize];
        cursor += mStreamer.read(chunkBytes, 0, chunkBytes.length);
        arscHeader = ResFileHeaderChunk.parseFrom(new PositionInputStream(new ByteArrayInputStream(chunkBytes)));

        do {
            headBytes = new byte[ChunkHeader.LENGTH];
            cursor += mStreamer.read(headBytes, 0, headBytes.length);
            header = ChunkHeader.parseFrom(new PositionInputStream(new ByteArrayInputStream(headBytes)));

            // Chunk size = ChunkInfo + BodySize
            chunkBytes = new byte[(int) header.chunkSize];
            System.arraycopy(headBytes, 0, chunkBytes, 0, ChunkHeader.LENGTH);
            cursor += mStreamer.read(chunkBytes, ChunkHeader.LENGTH, (int) header.chunkSize - ChunkHeader.LENGTH);
            //LogUtil.i(TAG, header.toRowString().replace("\n", ""), "cursor=0x" + PrintUtil.hex4(cursor));

            switch (header.type) {
                case RES_STRING_POOL_TYPE:
                    resStringPoolChunk = ResStringPoolChunk.parseFrom(new PositionInputStream(new ByteArrayInputStream(chunkBytes)));
                    break;
                case RES_TABLE_PACKAGE_TYPE:
                    resTablePackageChunk = ResTablePackageChunk.parseFrom(new PositionInputStream(new ByteArrayInputStream(chunkBytes)), resStringPoolChunk);
                    break;
                default:
                //LogUtil.e("Unknown type: 0x" + PrintUtil.hex2(header.type));
            }

        } while (cursor < sBuf.length);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(4096);
        builder.append(arscHeader).append('\n');
        builder.append(resStringPoolChunk).append('\n');
        builder.append(resTablePackageChunk).append('\n');
        return builder.toString();
    }

    public String buildPublicXml() {
        return resTablePackageChunk.buildEntry2String();
    }

    public ResTableEntry getResource(int resId) {
        long pkgId = (resId & 0xff000000L) >> 24;
        //short packageId = (short) (resId >> 24 & 0xff);
        if (resTablePackageChunk.pkgId == pkgId) {
            return resTablePackageChunk.getResource(resId);
        } else {
            return null;
        }
    }
}
