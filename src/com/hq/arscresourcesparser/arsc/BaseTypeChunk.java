package com.hq.arscresourcesparser.arsc;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public abstract class BaseTypeChunk {

    public abstract String getChunkName();

    public abstract long getEntryCount();

    public abstract String getType();

    public abstract int getTypeId();

    public abstract void translateValues(ResStringPoolChunk globalStringPool,
                                ResStringPoolChunk typeStringPool,
                                ResStringPoolChunk keyStringPool);
}
