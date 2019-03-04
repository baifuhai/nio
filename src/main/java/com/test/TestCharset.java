package com.test;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 字符集（Charset）
 * 		编码：字符串 -> 字节数组
 * 		解码：字节数组  -> 字符串
 *
 */
public class TestCharset {
	
	@Test
	public void test01() {
		Map<String, Charset> map = Charset.availableCharsets();
		
		for (Entry<String, Charset> entry : map.entrySet()) {
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
	}
	
	@Test
	public void test02() throws IOException {
		Charset charset = Charset.forName("GBK");
		
		//获取编码器
		CharsetEncoder charsetEncoder = charset.newEncoder();
		
		//获取解码器
		CharsetDecoder charsetDecoder = charset.newDecoder();
		
		CharBuffer charBuf = CharBuffer.allocate(1024);
		charBuf.put("我们！");
		
		charBuf.flip();
		
		//编码
		//ByteBuffer byteBuf = charset1.encode(charBuf);
		ByteBuffer byteBuf = charsetEncoder.encode(charBuf);
		System.out.println(new String(byteBuf.array(), charset));
		
		//解码
		//CharBuffer charBuf2 = charset1.decode(byteBuf);
		CharBuffer charBuf2 = charsetDecoder.decode(byteBuf);
		System.out.println(charBuf2.toString());
	}
	
}
