package com.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 一、通道（Channel）
 * 		用于源节点与目标节点的连接，负责缓冲区中数据的传输。
 * 		Channel 本身不存储数据，需要配合缓冲区进行传输。
 *
 * 二、通道的主要实现类
 * 		java.nio.channels.Channel 接口
 * 			|--FileChannel
 * 			|--SelectableChannel
 * 				|--SocketChannel
 * 				|--ServerSocketChannel
 * 				|--DatagramChannel
 * 				|--Pipe.SinkChannel
 * 				|--Pipe.SourceChannel
 *
 * 三、获取通道
 * 		1. Java 针对支持通道的类提供了 getChannel() 方法
 * 			本地 IO：
 * 			FileInputStream/FileOutputStream
 * 			RandomAccessFile
 *
 * 			网络IO：
 * 			Socket
 * 			ServerSocket
 * 			DatagramSocket
 * 		
 * 		2. 在 JDK 1.7 中的 NIO.2 针对各个通道提供了静态方法 open()
 * 		3. 在 JDK 1.7 中的 NIO.2 的 Files 工具类的 newByteChannel()
 *
 * 四、通道之间的数据传输
 * 		transferFrom()
 * 		transferTo()
 *
 * 五、分散（Scatter）与聚集（Gather）
 * 		分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
 * 		聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中
 *
 */
public class TestChannel {

	private long start;

	@Before
	public void before() {
		start = System.currentTimeMillis();
	}

	@After
	public void after() {
		long end = System.currentTimeMillis();
		System.out.println("time: " + (end - start) + " ms");
	}

	//使用通道和非直接缓冲区完成文件的复制
	@Test
	public void test01() {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			//1. 获取通道
			fis = new FileInputStream("d:/1.mkv");
			fos = new FileOutputStream("d:/2.mkv");
			inChannel = fis.getChannel();
			outChannel = fos.getChannel();
			
			//inChannel = FileChannel.open(Paths.get("d:/1.mkv"), StandardOpenOption.READ);
			//outChannel = FileChannel.open(Paths.get("d:/2.mkv"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			
			//inChannel = Files.newByteChannel(Paths.get("d:/1.mkv"), StandardOpenOption.READ);
			//outChannel = Files.newByteChannel(Paths.get("d:/2.mkv"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			
			//StandardOpenOption.CREATE			不存在则创建，存在则覆盖
			//StandardOpenOption.CREATE_NEW		不存在则创建，存在则报错
			
			//2. 分配指定大小的缓冲区
			ByteBuffer buf = ByteBuffer.allocate(1024);
			
			//3. 将通道中的数据存入缓冲区中
			while (inChannel.read(buf) != -1) {
				
				//切换到读取模式
				buf.flip();
				
				//将缓冲区中的数据写入通道中
				outChannel.write(buf);
				
				//清空缓冲区
				buf.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outChannel != null) {
				try {
					outChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inChannel != null) {
				try {
					inChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//使用通道和直接缓冲区（内存映射文件）完成文件的复制
	@Test
	public void test02() throws IOException {
		FileChannel inChannel = FileChannel.open(Paths.get("d:/1.mkv"), StandardOpenOption.READ);
		FileChannel outChannel = FileChannel.open(Paths.get("d:/2.mkv"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
		
		//内存映射文件
		MappedByteBuffer inMappedByteBuffer = inChannel.map(MapMode.READ_ONLY, 0, inChannel.size());
		MappedByteBuffer outMappedByteBuffer = outChannel.map(MapMode.READ_WRITE, 0, inChannel.size());
		
		//直接对缓冲区进行数据的读写操作
		byte[] dst = new byte[inMappedByteBuffer.limit()];
		inMappedByteBuffer.get(dst);
		outMappedByteBuffer.put(dst);
		
		inChannel.close();
		outChannel.close();
	}

	//通道之间的数据传输（直接缓冲区）
	@Test
	public void test03() throws IOException {
		FileChannel inChannel = FileChannel.open(Paths.get("d:/1.mkv"), StandardOpenOption.READ);
		FileChannel outChannel = FileChannel.open(Paths.get("d:/2.mkv"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		
		//inChannel.transferTo(0, inChannel.size(), outChannel);
		outChannel.transferFrom(inChannel, 0, inChannel.size());
		
		inChannel.close();
		outChannel.close();
	}

	//分散和聚集
	@Test
	public void test04() throws IOException {
		//1. 获取通道
		RandomAccessFile raf1 = new RandomAccessFile("d:/1.txt", "rw");
		//FileInputStream raf1 = new FileInputStream("d:/1.txt");
		FileChannel inChannel = raf1.getChannel();
		
		//2. 分配指定大小的缓冲区
		ByteBuffer buf1 = ByteBuffer.allocate(100);
		ByteBuffer buf2 = ByteBuffer.allocate(200);
		
		//3. 分散读取
		ByteBuffer[] bufs = new ByteBuffer[]{buf1, buf2};
		inChannel.read(bufs);
		
		//4. 切换到读取模式
		for (ByteBuffer byteBuffer : bufs) {
			byteBuffer.flip();
		}
		
		System.out.println(new String(buf1.array(), 0, buf1.limit()));
		System.out.println("=======================================");
		System.out.println(new String(buf2.array(), 0, buf2.limit()));
		
		raf1.close();
		inChannel.close();
		
		//4. 聚集写入
		RandomAccessFile raf2 = new RandomAccessFile("d:/2.txt", "rw");
		//FileOutputStream raf2 = new FileOutputStream("d:/2.txt");
		FileChannel outChannel = raf2.getChannel();
		
		outChannel.write(bufs);
		
		raf2.close();
		outChannel.close();
	}

}
