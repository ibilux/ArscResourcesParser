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
public class ResTableConfig {

    public long size;    // 4    size of config object

    // union 4bytes
    public int mcc, mnc; // 2 + 2
    public long imsi;    // 4

    // union 4bytes
    public int language, country;   // 2 + 2
    public long locale;             // 4

    // union 4bytes
    public int orientation, touchScreen, density;   // 1 + 1 + 2
    public long screenType;                         // 4

    // union 4bytes
    public int keyboard, navigation, inputFlags, inputPad0; // 1 + 1 + 1 + 1
    public long input;                                      // 4

    // union 4bytes
    public int screenWidth, screenHeight;   // 2 + 2
    public long screenSize;                 // 4

    // union 4bytes
    public int sdkVersion, minorVersion;    // 2 + 2
    public long version;                    // 4

    // union 4bytes
    public int screenLayout, uiModeByte, smallestScreenWidthDp; // 1 + 1 + 2
    public long screenConfig;                                   // 4

    // union 4bytes
    public int screenWidthDp, screenHeightDp;   // 2 + 2
    public long screenSizeDp;                   // 4

    public byte[] localeScript;   // 4
    public byte[] localeVariant;  // 8

    public static ResTableConfig parseFrom(PositionInputStream mStreamer) throws IOException {
        ResTableConfig config = new ResTableConfig();
        long cursor = mStreamer.getPosition();
        long start = cursor;

        config.size = Utils.readInt(mStreamer);
        cursor += 4;

        config.mcc = Utils.readShort(mStreamer);
        config.mnc = Utils.readShort(mStreamer);
        mStreamer.seek(cursor);     // Reset cursor to get union value.
        config.imsi = Utils.readInt(mStreamer);
        cursor += 4;

        config.language = Utils.readShort(mStreamer);
        config.country = Utils.readShort(mStreamer);
        mStreamer.seek(cursor);
        config.locale = Utils.readInt(mStreamer);
        cursor += 4;

        config.orientation = Utils.readUInt8(mStreamer);
        config.touchScreen = Utils.readUInt8(mStreamer);
        config.density = Utils.readShort(mStreamer);
        mStreamer.seek(cursor);
        config.screenType = Utils.readInt(mStreamer);
        cursor += 4;

        config.keyboard = Utils.readUInt8(mStreamer);
        config.navigation = Utils.readUInt8(mStreamer);
        config.inputFlags = Utils.readUInt8(mStreamer);
        config.inputPad0 = Utils.readUInt8(mStreamer);
        mStreamer.seek(cursor);
        config.input = Utils.readInt(mStreamer);
        cursor += 4;

        config.screenWidth = Utils.readShort(mStreamer);
        config.screenHeight = Utils.readShort(mStreamer);
        mStreamer.seek(cursor);
        config.screenSize = Utils.readInt(mStreamer);
        cursor += 4;

        config.sdkVersion = Utils.readShort(mStreamer);
        config.minorVersion = Utils.readShort(mStreamer);
        mStreamer.seek(cursor);
        config.version = Utils.readInt(mStreamer);
        cursor += 4;

        config.screenLayout = Utils.readUInt8(mStreamer);
        config.uiModeByte = Utils.readUInt8(mStreamer);
        config.smallestScreenWidthDp = Utils.readShort(mStreamer);
        mStreamer.seek(cursor);
        config.screenConfig = Utils.readInt(mStreamer);
        cursor += 4;

        config.screenWidthDp = Utils.readShort(mStreamer);
        config.screenHeightDp = Utils.readShort(mStreamer);
        mStreamer.seek(cursor);
        config.screenSizeDp = Utils.readInt(mStreamer);
        cursor += 4;
        {
            byte[] buf;
            buf = new byte[4];
            mStreamer.read(buf);
            config.localeScript = buf;
            buf = new byte[8];
            mStreamer.read(buf);
            config.localeVariant = buf;
        }

        mStreamer.seek(start + config.size);

        return config;
    }
}
