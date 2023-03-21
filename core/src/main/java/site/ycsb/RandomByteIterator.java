/**
 * Copyright (c) 2010-2016 Yahoo! Inc., 2017 YCSB contributors All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package site.ycsb;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;

/**
 *  A ByteIterator that generates a random sequence of bytes.
 */
public class RandomByteIterator extends ByteIterator {
  private final long len;
  private long off;
  private int bufOff;
  private final byte[] buf;

  @Override
  public boolean hasNext() {
    return (off + bufOff) < len;
  }

  private void fillBytesImpl(byte[] buffer, int base) {
    // System.out.println("__fillBytesImpl");
    int bytes = ThreadLocalRandom.current().nextInt();

    switch (buffer.length - base) {
    default:
      buffer[base + 5] = (byte) (((bytes >> 25) & 95) + ' ');
    case 5:
      buffer[base + 4] = (byte) (((bytes >> 20) & 63) + ' ');
    case 4:
      buffer[base + 3] = (byte) (((bytes >> 15) & 31) + ' ');
    case 3:
      buffer[base + 2] = (byte) (((bytes >> 10) & 95) + ' ');
    case 2:
      buffer[base + 1] = (byte) (((bytes >> 5) & 63) + ' ');
    case 1:
      buffer[base + 0] = (byte) (((bytes) & 31) + ' ');
    case 0:
      break;
    }
    // System.out.println("buffer: " + buffer);
  }

  private void fillBytes() {
    // System.out.println("_fillBytes");
    if (bufOff == buf.length) {
      fillBytesImpl(buf, 0);
      bufOff = 0;
      off += buf.length;
    }
  }

  public RandomByteIterator(long len) {
    // System.out.println("RandomByteIterator");
    this.len = len;
    this.buf = new byte[6];
    this.bufOff = buf.length;
    fillBytes();
    this.off = 0;
  }

  public byte nextByte() {
    fillBytes();
    bufOff++;
    return buf[bufOff - 1];
  }

  @Override
  public int nextBuf(byte[] buffer, int bufOffset) {
    // System.out.println("__nextBuf");
    int ret;
    if (len - off < buffer.length - bufOffset) {
      ret = (int) (len - off);
    } else {
      ret = buffer.length - bufOffset;
    }
    int i;
    for (i = 0; i < ret; i += 6) {
      fillBytesImpl(buffer, i + bufOffset);
    }
    off += ret;
    return ret + bufOffset;
  }

  @Override
  public long bytesLeft() {
    return len - off - bufOff;
  }

  @Override
  public void reset() {
    off = 0;
  }

  /** Consumes remaining contents of this object, and returns them as a byte array. */
  //ANCHOR - return "a" bytes
  public byte[] toArray() {
    // System.out.println("___toArray()");
    long left = bytesLeft();  // left = 100
    /* change left / 2 accordingly to adjust compression ratio 
       ex) 3 for 33% 4 for 25%... etc */
    // int compressionRatio = (int) left / 2;
    int compressionRatio = 0;

    if (left != (int) left) {
      throw new ArrayIndexOutOfBoundsException("Too much data to fit in one array!");
    }
    byte[] tmp1 = new byte[(int) left - compressionRatio]; // array for holding random data
    byte[] tmp2 = new byte[compressionRatio]; // array for holding "a" data

    // assign random values in tmp1
    int bufOffset = 0;
    while (bufOffset < tmp1.length) {
      bufOffset = nextBuf(tmp1, bufOffset);
    }

    //assign "a" values in tmp2 
    String s = "a".repeat(tmp2.length);
    tmp2= s.getBytes();

    //concatenate two byte arrays into ret
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try{
      outputStream.write(tmp1);
      outputStream.write(tmp2);
    } catch (IOException e){
      System.out.println(e);
    }
    byte[] ret = outputStream.toByteArray();

    // System.out.println("len: " + ret.length);
    // System.out.println("midway: " + new String(ret));

    return ret;
  }

}
