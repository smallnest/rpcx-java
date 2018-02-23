package com.colobu.rpcx.protocol;

import lombok.Data;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Data
public class Message {
    public static byte magicNumber = 0x08;

    byte[] header;
    String servicePath;
    String serviceMethod;
    Map<String,String> metadata;
    byte[] payload;

    public Message() {
        header = new byte[12];
        header[0] = magicNumber;
        servicePath = "";
        serviceMethod = "";
        metadata = new HashMap<>();
        payload = new byte[]{};
    }
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
    }

    public byte[] encode() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream(24);
        os.write(header);

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

    private void decodeMetadata(byte[] b) {
        // TODO
    }

    private byte[] encodeMetadata() {
        // TODO
        return new byte[]{};
    }
}
