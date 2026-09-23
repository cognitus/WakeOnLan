package de.florianisme.wakeonlan.wol;

import android.util.Log;

import com.google.common.base.Strings;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.ui.modify.BroadcastHelper;

public class WolSender {

    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    public static void sendWolPacket(Device device) {
        Runnable sendWolRunnable = new Runnable() {

            @Override
            public void run() {
                sendPacket(device.broadcastAddress);
                new BroadcastHelper().getBroadcastAddress().ifPresent(inetAddress -> sendPacket(inetAddress.getHostAddress()));
            }

            private void sendPacket(String broadcastAddress) {
                if (Strings.isNullOrEmpty(broadcastAddress)) {
                    return;
                }

                try (DatagramSocket socket = new DatagramSocket()) {
                    DatagramPacket packet = PacketBuilder.buildMagicPacket(broadcastAddress, device.macAddress, device.port, device.secureOnPassword);
                    socket.send(packet);
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), "Error while sending magic packet: ", e);
                }
            }
        };

        EXECUTOR.execute(sendWolRunnable);
    }

}
