package com.xiaomakj.bdvoice.play;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fujiayi on 2017/11/27.
 * <p>
 * 解决大文件的输入问题。
 */

public class FileAudioInputStream extends InputStream {

    private InputStream in;

    public static final float SPEED = 2;

    public FileAudioInputStream(String file) throws FileNotFoundException {
        in = new FileInputStream(file);
    }

    public FileAudioInputStream(InputStream in) {
        this.in = in;
    }

    private long firstRead = -1;
    private long returnCount;

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if (firstRead == -1) {
            firstRead = System.currentTimeMillis();
        }
        long limit = (long) ((System.currentTimeMillis() - firstRead) * 32 * SPEED);
        long count = Math.min((limit - returnCount), byteCount);
        if (count <= 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            int r = in.read(buffer, byteOffset, (int) count);
            if (r >= 0) {
                returnCount += r;
            }
            return r;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (null != in) {
            in.close();
        }
    }
}
