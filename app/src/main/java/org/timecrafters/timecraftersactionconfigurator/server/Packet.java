package org.timecrafters.timecraftersactionconfigurator.server;

import java.util.Arrays;

class Packet {
    final static public byte PROTOCOL_VERSION = 0x01;
    final static public String PROTOCOL_CONTENT_START = "|";
    final static public String PROTOCOL_HEARTBEAT = "heartbeat";


    // NOTE: PacketType is cast to a byte, no more than 255 packet types can exist unless
    //       header is updated.
    public enum PacketType {
        HANDSHAKE,
        HEARTBEAT,
        DOWNLOAD_CONFIG,
        UPLOAD_CONFIG,
    }

    byte protocolVersion;
    PacketType packetType;
    int contentLength;
    String content;

    Packet(byte protocolVersion, PacketType packetType, int contentLength, String content) {
        this.protocolVersion = protocolVersion;
        this.packetType = packetType;
        this.contentLength = contentLength;
        this.content = content;
    }

    static public Packet fromStream(String message) {
        byte[] header;
        byte version;
        PacketType type;
        int length;
        String body;

        String[] slice = message.split(PROTOCOL_CONTENT_START, 1);
        header = slice[0].getBytes();
        body = slice[1];

        version = header[0];
        type = PacketType.values()[header[1]];
        length = Integer.parseInt( new String(Arrays.copyOfRange(header, 2, header.length - 1)) );

        return new Packet(version, type, length, body);
    }

    static public Packet create(PacketType packetType, String message) {
        return new Packet(PROTOCOL_VERSION, packetType, message.length(), message);
    }

    public boolean isValid() {
        byte[] messageBytes = bytes();

        return messageBytes[0] == PROTOCOL_VERSION &&
                isPacketTypeValid(messageBytes[1]);
    }

    public boolean isPacketTypeValid(byte messageByte) {
        return PacketType.values().length >= messageByte && PacketType.values()[messageByte] != null;
    }

    public String encodeHeader() {
        String string = "";
        string += PROTOCOL_VERSION;
        string += (byte) packetType.ordinal();
        string += contentLength;
        string += PROTOCOL_CONTENT_START;
        return string;
    }

    public byte[] bytes() {
        return ("" + encodeHeader() + content).getBytes();
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContent() {
        return content;
    }
}
