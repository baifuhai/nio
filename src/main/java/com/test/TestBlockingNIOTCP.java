package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

public class TestBlockingNIOTCP {

	//客户端
	@Test
	public void client() throws IOException {
		//1. 获取通道
		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
		
		//2. 分配指定大小的缓冲区
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		//3. 读取本地文件，并发送到服务端
		FileChannel fileChannel = FileChannel.open(Paths.get("d:/1.jpg"), StandardOpenOption.READ);
		while (fileChannel.read(buf) != -1) {
			buf.flip();
			socketChannel.write(buf);
			buf.clear();
		}
		
		socketChannel.shutdownOutput();
		
		//接收服务端的反馈
		while (socketChannel.read(buf) != -1) {
			buf.flip();
			System.out.println(new String(buf.array(), 0, buf.limit()));
			buf.clear();
		}
		
		//4. 关闭通道
		fileChannel.close();
		socketChannel.close();
	}

	//服务端
	@Test
	public void server() throws IOException {
		//1. 获取通道
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		
		//2. 绑定连接
		serverSocketChannel.bind(new InetSocketAddress(9898));
		
		//3. 获取客户端连接的通道
		System.out.println("a");
		SocketChannel socketChannel = serverSocketChannel.accept();
		System.out.println("b");
		
		//4. 分配指定大小的缓冲区
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		//5. 接收客户端的数据，并保存到本地
		FileChannel fileChannel = FileChannel.open(Paths.get("d:/2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		while (socketChannel.read(buf) != -1) {
			buf.flip();
			fileChannel.write(buf);
			buf.clear();
		}
		
		//发送反馈给客户端
		buf.put("hello".getBytes());
		buf.flip();
		socketChannel.write(buf);
		
		//6. 关闭通道
		socketChannel.close();
		fileChannel.close();
		serverSocketChannel.close();
	}

}
