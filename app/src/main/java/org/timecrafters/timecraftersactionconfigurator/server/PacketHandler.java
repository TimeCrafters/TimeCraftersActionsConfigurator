package org.timecrafters.timecraftersactionconfigurator.server;

import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Reader;

import java.lang.reflect.Array;
import java.util.Arrays;

public class PacketHandler {
    private boolean hostIsConnection = false;

    public PacketHandler(boolean isHostConnection) {
        this.hostIsConnection = isHostConnection;
    }

    public void handle(String message) {
        Packet packet = Packet.fromStream(message);

        if (packet.isValid()) {
            handOff(packet);
        } else {
            // TODO: log rejected packet.
        }
    }

    public void handOff(Packet packet) {
        switch(packet.packetType) {
            case HANDSHAKE: {
                handleHandShake(packet);
                return;
            }

            case HEARTBEAT: {
                handleHeartBeat(packet);
                return;
            }

            case DOWNLOAD_CONFIG: {
                handleDownloadConfig(packet);
                return;
            }

            case UPLOAD_CONFIG: {
                handleUploadConfig(packet);
                return;
            }

            default: {
                return;
            }
        }
    }

    // NO-OP
    public void handleHandShake(Packet packet) {}
    // NO-OP
    public void handleHeartBeat(Packet packet) {}
    // NO-OP
    public void handleDownloadConfig(Packet packet) {}

    public void handleUploadConfig(Packet packet) {
        if (hostIsConnection) {
            // save and reload menu
        } else {
            // save
        }
    }

    public Packet packetHeartBeat() {
        return Packet.create(Packet.PacketType.HEARTBEAT, Packet.PROTOCOL_HEARTBEAT);
    }

    public Packet packetUploadConfig() {
        return Packet.create(Packet.PacketType.UPLOAD_CONFIG, Reader.rawConfigFile());
    }
}
