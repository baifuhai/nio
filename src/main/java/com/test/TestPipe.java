package com.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

import org.junit.Test;

/**
 * 管道（Pipe）
 *
 * 管道是2个线程之间的单向数据连接。
 * Pipe 有一个 source 通道和一个 sink 通道。
 * 数据会被写到 sink 通道，从 source 通道读取。
 */
public class TestPipe {

	@Test
	public void test01() throws IOException {
		//1. 获取管道
		Pipe pipe = Pipe.open();
		
		//2. 将缓冲区中的数据写入管道
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.put("hello".getBytes());
		buf.flip();
		
		Pipe.SinkChannel sinkChannel = pipe.sink();
		sinkChannel.write(buf);
		
		sinkChannel.close();
		
		//3. 读取缓冲区中的数据
		ByteBuffer buf2 = ByteBuffer.allocate(1024);
		
		Pipe.SourceChannel sourceChannel = pipe.source();
		while (sourceChannel.read(buf2) != -1) {
			buf2.flip();
			System.out.println(new String(buf2.array(), 0, buf2.limit()));
			buf2.clear();
		}
		
		sourceChannel.close();
	}
	
}
