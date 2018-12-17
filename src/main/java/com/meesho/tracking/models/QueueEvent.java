package com.meesho.tracking.models;

import lombok.Data;

import java.io.*;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
@Data
public class QueueEvent implements Serializable {

    private static final long serialVersionUID = -3034034301980540282L;

    public String key;
    public byte[] value;
    public long postedTime;
    public long delayTime;

    public QueueEvent(String key, byte[] val, long visibleDelayInMillis) {
        this.key = key;
        this.value = val;
        this.postedTime = System.currentTimeMillis();
        this.delayTime = visibleDelayInMillis;
    }

    public byte[] ser() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;

        try {
            objOut = new ObjectOutputStream(out);
            objOut.writeObject(this);
            objOut.flush();
            return out.toByteArray();
        } finally {
            if (null != objOut)
                objOut.close();
        }
    }

    public static QueueEvent deser(byte[] data) throws IOException {

        ObjectInputStream objIn = null;
        try {
            objIn = new ObjectInputStream(new ByteArrayInputStream(data));
            return (QueueEvent) objIn.readObject();
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            if (null != objIn)
                objIn.close();
        }
    }

}
