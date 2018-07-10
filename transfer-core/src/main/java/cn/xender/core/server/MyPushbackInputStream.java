package cn.xender.core.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Created by Xushuo on 2015/1/14.
 */
public class MyPushbackInputStream extends PushbackInputStream {

    public MyPushbackInputStream(InputStream in) {
        super(in);
    }

    public MyPushbackInputStream(InputStream in, int size) {
        super(in, size);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if (buf == null) {
            throw streamClosed();
        }
        checkOffsetAndCount(buffer.length, byteOffset, byteCount);


        int copiedBytes = 0, copyLength = 0, newOffset = byteOffset;
        // Are there pushback bytes available?
        if (pos < buf.length) {
            copyLength = (buf.length - pos >= byteCount) ? byteCount : buf.length - pos;
            System.arraycopy(buf, pos, buffer, newOffset, copyLength);
            newOffset += copyLength;
            copiedBytes += copyLength;
            // Use up the bytes in the local buffer
            pos += copyLength;
        }

        if(copiedBytes > 0){
            return copiedBytes;
        }else{

            int inCopied = in.read(buffer, newOffset, byteCount - copiedBytes);
            if (inCopied > 0) {
                return inCopied + copiedBytes;
            }
            if (copiedBytes == 0) {
                return inCopied;
            }
            return copiedBytes;
        }


    }

    private IOException streamClosed() throws IOException  {
        throw new IOException("PushbackInputStream is closed");
    }

    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
}
