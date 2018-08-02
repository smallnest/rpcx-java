package com.colobu.rpcx.protocol;

import lombok.Data;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Message is a common class for requests and responses.
 */
@Data
public class Message {

    public static byte magicNumber = 0x08;

    byte[] header;
    public String servicePath;
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
        header[2] &=  ~0x03;
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

    /**
     * decode from an inputstream and fill this message with it.
     *
     * @param in input data
     * @throws Exception
     */
    public void decode(InputStream in) throws Exception {
        int magic = in.read();
        if (magic != magicNumber) {
            throw new Exception("read wrong magic number: " + magic);
        }
        header[0] = magicNumber;

        int n = IOUtils.read(in, header, 1, header.length - 1);
        if (n != header.length - 1) {
            throw new Exception("read wrong header length: " + n);
        }

        //byte [] bytes = ByteBuffer.allocate(4).putInt(17291729).array();
        //byte [] bytes = { 1, 7, -39, -47 };
        //System.out.println(ByteBuffer.wrap(bytes).getInt());

        byte[] lenBytes = new byte[4];
        n = IOUtils.read(in, lenBytes);
        if (n != 4) {
            throw new Exception("read wrong total length: " + n);
        }
        int totalLen = ByteBuffer.wrap(lenBytes).getInt();

        byte[] data = new byte[totalLen];
        n = IOUtils.read(in, data);
        if (n != totalLen) {
            throw new Exception("read wrong data length: " + n);
        }

        ByteBuffer buf = ByteBuffer.wrap(data);
        int len = buf.getInt();
        byte[] b = new byte[len];
        buf.get(b);
        servicePath = new String(b, "UTF-8");
        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
        serviceMethod = new String(b, "UTF-8");

        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
        decodeMetadata(b);

        len = buf.getInt();
        payload = new byte[len];
        buf.get(payload);

        //decompress
        if (getCompressType() == CompressType.Gzip) {
            GZIPInputStream zipStream = new GZIPInputStream(new ByteArrayInputStream(payload));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int ll;
            while ((ll = zipStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            payload = bos.toByteArray();
        }
    }

    /**
     * encode this message to a byte array.
     *
     * @return encoded data for this message.
     * @throws Exception
     */
    public byte[] encode() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream(24);
        os.write(header);

        //compress
        if (getCompressType() == CompressType.Gzip) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zipStream = new GZIPOutputStream(bos);
            zipStream.write(payload);
            payload = bos.toByteArray();
        }

        byte[] spBytes = servicePath.getBytes("UTF-8");
        byte[] smBytes = serviceMethod.getBytes("UTF-8");
        byte[] metaBytes = encodeMetadata();
        int totalLen = spBytes.length + 4 + smBytes.length + 4 + metaBytes.length + 4 + payload.length + 4;

        os.write(ByteBuffer.allocate(4).putInt(totalLen).array());

        os.write(ByteBuffer.allocate(4).putInt(spBytes.length).array());
        os.write(spBytes);
        os.write(ByteBuffer.allocate(4).putInt(smBytes.length).array());
        os.write(smBytes);

        os.write(ByteBuffer.allocate(4).putInt(metaBytes.length).array());
        os.write(metaBytes);

        os.write(ByteBuffer.allocate(4).putInt(payload.length).array());
        os.write(payload);

        return os.toByteArray();
    }

    private void decodeMetadata(byte[] b) throws UnsupportedEncodingException {

        ByteBuffer buf = ByteBuffer.wrap(b);
        int len;
        for (; ; ) {
            if (buf.remaining() < 4) {
                break;
            }
            len = buf.getInt();
            b = new byte[len];
            buf.get(b);
            String k = new String(b, "UTF-8");

            len = buf.getInt();
            b = new byte[len];
            buf.get(b);
            String v = new String(b, "UTF-8");
            metadata.put(k, v);
        }

    }

    private byte[] encodeMetadata() throws IOException {
        if (metadata.size() == 0) {
            return new byte[]{};
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            byte[] keyBytes = key.getBytes("UTF-8");
            os.write(ByteBuffer.allocate(4).putInt(keyBytes.length).array());
            os.write(keyBytes);

            String v = entry.getValue();
            byte[] vBytes = v.getBytes("UTF-8");
            os.write(ByteBuffer.allocate(4).putInt(vBytes.length).array());
            os.write(vBytes);
        }

        return os.toByteArray();
    }

    public void decode(ByteBuffer buffer) throws Exception {
        int magic = buffer.get();
        if (magic != magicNumber) {
            throw new Exception("read wrong magic number: " + magic);
        }
        buffer.rewind();
        buffer.get(this.header);


        int totalLen  = buffer.getInt();
        byte[] data = new byte[totalLen];
        buffer.get(data);

        ByteBuffer buf = ByteBuffer.wrap(data);
        int len = buf.getInt();
        byte[] b = new byte[len];
        buf.get(b);
        servicePath = new String(b, "UTF-8");

        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
        serviceMethod = new String(b, "UTF-8");

        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
        decodeMetadata(b);

        len = buf.getInt();
        payload = new byte[len];
        buf.get(payload);

        //decompress
        if (getCompressType() == CompressType.Gzip) {
            GZIPInputStream zipStream = new GZIPInputStream(new ByteArrayInputStream(payload));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer2 = new byte[1024];
            int ll;
            while ((ll = zipStream.read(buffer2)) != -1) {
                bos.write(buffer2, 0, len);
            }
            payload = bos.toByteArray();
        }
    }
}
