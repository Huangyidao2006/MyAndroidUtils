package com.hj.android.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Created by hj at 2022/5/23 09:18
 */
public class UdpHelper {
    private final int mPort;

    private int mBufferSize = 1024 * 64;

    private DatagramChannel mUdpChannel;

    private UdpListener mUdpListener;

    public interface UdpListener {
        void onRecv(byte[] data, String ip, int port);
    }

    public UdpHelper(int port) {
        mPort = port;

        try {
            mUdpChannel = DatagramChannel.open();
            mUdpChannel.configureBlocking(false);
            mUdpChannel.socket().setBroadcast(true);
            mUdpChannel.socket().bind(new InetSocketAddress(port));

            if (mRecvThread == null) {
                mRecvThread = new RecvThread();
                mRecvThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUdpListener(UdpListener listener) {
        mUdpListener = listener;
    }

    public int send(byte[] data, String dstIP, int dstPort) {
        if (mUdpChannel == null) {
            return -1;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        try {
            int sent = 0;
            while (sent != data.length) {
                int len = mUdpChannel.send(buffer, new InetSocketAddress(dstIP, dstPort));
                sent += len;
            }

            return sent;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void destroy() {
        if (mRecvThread != null) {
            mRecvThread.stopRun();
            mRecvThread = null;

            if (mUdpChannel != null) {
                try {
                    mUdpChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mUdpChannel = null;
                }
            }
        }
    }

    private RecvThread mRecvThread;

    private class RecvThread extends Thread {
        private boolean mNeedStop;

        private Selector mSelector;

        public void stopRun() {
            mNeedStop = true;
            if (mSelector != null && mSelector.isOpen()) {
                try {
                    mSelector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            if (mUdpChannel == null) {
                return;
            }

            try {
                mSelector = Selector.open();
                mUdpChannel.register(mSelector, SelectionKey.OP_READ);

                ByteBuffer buffer = ByteBuffer.allocate(mBufferSize);

                while (!mNeedStop) {
                    if (mSelector.select(1000) > 0) {
                        Iterator<SelectionKey> it = mSelector.selectedKeys().iterator();
                        while (it.hasNext()) {
                            SelectionKey selectionKey = it.next();
                            if (selectionKey.isReadable()) {
                                final InetSocketAddress address = (InetSocketAddress) mUdpChannel.receive(buffer);
                                buffer.flip();

                                if (mUdpListener != null) {
                                    int len = buffer.limit();
                                    byte[] data = new byte[len];
                                    System.arraycopy(buffer.array(), 0, data, 0, len);

                                    mUdpListener.onRecv(data,
                                            address.getAddress().getHostAddress(),
                                            address.getPort());
                                }

                                buffer.clear();
                            }

                            it.remove();
                        }
                    }
                }

                if (mSelector.isOpen()) {
                    mSelector.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
