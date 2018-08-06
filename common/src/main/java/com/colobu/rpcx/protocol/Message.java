package com.colobu.rpcx.protocol;

import lombok.Data;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * @author goodjava@qq.com
 */
@Data
public class Message {

    public static byte magicNumber = 0x08;

    public byte[] header;
    /**
     * java-->className
     */
    public String servicePath;
    /**
     * java-->methodName
     */
    public String serviceMethod;
    public Map<String, String> metadata;
    public byte[] payload;

    public Message() {
        header = new byte[12];
        header[0] = magicNumber;
        servicePath = "";
        serviceMethod = "";
        metadata = new HashMap<>();
        payload = new byte[]{};
    }


    public Message(String servicePath, String serviceMethod) {
        this();
        this.servicePath = servicePath;
        this.serviceMethod = serviceMethod;
    }

    public Message(String servicePath, String serviceMethod, Map<String, String> metadata) {
        this(servicePath, serviceMethod);
        this.metadata = metadata;
    }

    public Message(String servicePath, String serviceMethod, Map<String, String> metadata, byte[] payload) {
        this(servicePath, serviceMethod, metadata);
        this.payload = payload;
    }


    public byte getVersion() {
        return header[1];
    }

    public void setVersion(byte version) {
        header[1] = version;
    }

    public MessageType getMessageType() {
        if ((header[2] & 0x80) == 0) {
            return MessageType.Request;
        }

        return MessageType.Response;
    }

    public void setMessageType(MessageType mt) {
        if (mt == MessageType.Request) {
            header[2] &= ~0x80;
        } else {
            header[2] |= 0x80;
        }
    }

    public void setMessageType(byte type) {
        header[2] = type;
    }


    public boolean isHeartbeat() {
        return (header[2] & 0x40) != 0;
    }

    public void setHeartbeat(boolean heartbeat) {
        if (heartbeat) {
            header[2] |= 0x40;
        } else {
            header[2] &= ~0x40;
        }
    }

    public boolean isOneway() {
        return (header[2] & 0x20) != 0;
    }

    public void setOneway(boolean oneway) {
        if (oneway) {
            header[2] |= 0x20;
        } else {
            header[2] &= ~0x20;
        }
    }


    public CompressType getCompressType() {
        int v = (header[2] & 0x1C) >> 2;
        return CompressType.getValue(v);
    }

    public void setCompressType(CompressType ct) {
        int v = ct.value();
        header[2] &= ~0x1C;
        header[2] |= (v << 2) & 0x1C;
    }

    public MessageStatusType getMessageStatusType() {
        int v = header[2] & 0x03;
        return MessageStatusType.getValue(v);
    }

    public void setMessageStatusType(MessageStatusType mst) {
        int v = mst.value();
        header[2] &= ~0x03;
        header[2] |= v & 0x03;
    }

    public SerializeType getSerializeType() {
        int v = (header[3] & 0xF0) >> 4;
        return SerializeType.getValue(v);
    }

    public void setSerializeType(SerializeType st) {
        int v = st.value();
        header[3] &= 0x0F;
        header[3] |= (v << 4) & 0xF0;
    }

    public long getSeq() {
        ByteBuffer buf = ByteBuffer.wrap(header);
        buf.position(4);
        return buf.getLong();
    }

    public void setSeq(long seq) {
        ByteBuffer buf = ByteBuffer.wrap(header);
        buf.position(4);
        buf.putLong(seq);
    }


    private byte[] compress() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zipStream = new GZIPOutputStream(bos);
        zipStream.write(payload);
        return bos.toByteArray();
    }


    /**
     * 编码
     *
     * @return
     * @throws Exception
     */
    public byte[] encode() throws Exception {
        if (getCompressType() == CompressType.Gzip) {
            this.payload = compress();
        }

        byte[] spBytes = servicePath.getBytes("UTF-8");
        byte[] smBytes = serviceMethod.getBytes("UTF-8");
        byte[] metaBytes = encodeMetadata();

        int headLen = header.length;
        int bodyLen = spBytes.length + 4 + smBytes.length + 4 + metaBytes.length + 4 + payload.length + 4;

        int capacity = headLen + 4 + bodyLen;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(header);

        buffer.putInt(bodyLen);

        buffer.putInt(spBytes.length);
        buffer.put(spBytes);

        buffer.putInt(smBytes.length);
        buffer.put(smBytes);

        buffer.putInt(metaBytes.length);
        buffer.put(metaBytes);

        buffer.putInt(payload.length);
        buffer.put(payload);

        return buffer.array();
    }


    private byte[] encodeMetadata() throws IOException {
        if (metadata.size() == 0) {
            return new byte[]{};
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream(20);

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            byte[] keyBytes = key.getBytes("UTF-8");
            os.write(ByteBuffer.allocate(4).putInt(keyBytes.length).array());
            os.write(keyBytes);

            String v = entry.getValue();
            if (null == v) {
                v = "null";
            }
            byte[] vBytes = v.getBytes("UTF-8");
            os.write(ByteBuffer.allocate(4).putInt(vBytes.length).array());
            os.write(vBytes);
        }

        return os.toByteArray();
    }


}
