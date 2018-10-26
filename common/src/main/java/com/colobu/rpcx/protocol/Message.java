package com.colobu.rpcx.protocol;

import com.colobu.rpcx.common.Bytes;
import com.colobu.rpcx.rpc.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
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

    public Message(String servicePath, String serviceMethod) {
        this();
        this.servicePath = servicePath;
        this.serviceMethod = serviceMethod;
    }

    public Message() {
        header = new byte[12];
        header[0] = magicNumber;
        payload = new byte[]{};
        this.metadata = new HashMap<>(2);
    }


    public Message(String servicePath, String serviceMethod, MessageType messageType, long seq) {
        this(servicePath, serviceMethod);
        this.setMessageType(messageType);
        this.setSeq(seq);
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

    //如果返回结果有error,这里需要设置
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
        return Bytes.bytes2long(header, 4);
    }

    public void setSeq(long seq) {
        System.arraycopy(Bytes.long2bytes(seq), 0, header, 4, 8);
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

        byte[] spBytes = servicePath.getBytes();
        byte[] smBytes = serviceMethod.getBytes();
        ByteBuf metaBytes = encodeMetadata();

        int headLen = header.length;
        int bodyLen = spBytes.length + 4 + smBytes.length + 4 + metaBytes.capacity() + 4 + payload.length + 4;

        int capacity = headLen + 4 + bodyLen;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(header);

        buffer.putInt(bodyLen);

        buffer.putInt(spBytes.length);
        buffer.put(spBytes);

        buffer.putInt(smBytes.length);
        buffer.put(smBytes);

        buffer.putInt(metaBytes.capacity());
        buffer.put(metaBytes.array());

        buffer.putInt(payload.length);
        buffer.put(payload);

        return buffer.array();
    }


    private ByteBuf encodeMetadata() {
        if (metadata.size() == 0) {
            return Unpooled.EMPTY_BUFFER;
        }

        ByteBuf buf = Unpooled.buffer(metadata.size() * 15);

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            byte[] keyBytes = key.getBytes();
            buf.writeInt(keyBytes.length);
            buf.writeBytes(keyBytes);

            String v = entry.getValue();
            if (null == v) {
                v = "null";
            }
            byte[] vBytes = v.getBytes();
            buf.writeInt(vBytes.length);
            buf.writeBytes(vBytes);
        }
        return buf.capacity(buf.writerIndex());
    }


    /**
     * 业务解码
     */
    public void decode(byte[] data) throws UnsupportedEncodingException {
        ByteBuffer buf = ByteBuffer.wrap(data);
        //servicePath
        int len = buf.getInt();
        byte[] b = new byte[len];
        buf.get(b);
        servicePath = new String(b, "UTF-8");
        //serviceMethod
        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
        serviceMethod = new String(b, "UTF-8");
        //metadata
        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
        decodeMetadata(b);
        //payload
        len = buf.getInt();
        byte[] payload = new byte[len];
        buf.get(payload);
        this.payload = payload;
    }

    private void decodeMetadata(byte[] b) throws UnsupportedEncodingException {
        int blen = b.length;
        if (blen > 8) {
            int len;
            int index = 0;
            byte[] data = null;
            for (; ; ) {
                if (blen - index < 4) {
                    break;
                }
                len = Bytes.bytes2int(b, index);
                index = index + 4;
                data = new byte[len];
                System.arraycopy(b, index, data, 0, len);
                String key = new String(data, "UTF-8");
                index = index + len;


                len = Bytes.bytes2int(b, index);
                index = index + 4;
                data = new byte[len];
                System.arraycopy(b, index, data, 0, len);
                String value = new String(data, "UTF-8");
                index = index + len;
                this.metadata.put(key, value);
            }
        }
    }


    public Map<String, String> getMetadata() {
        return metadata;
    }
}
