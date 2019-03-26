package com.zhuocheng.netty.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zhuocheng.netty.server.handler.UDPServerHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

@Component
public class UDPServer {
	@Value("#{nettyConfig['udpport']}") 
	int udpPort;
	
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	
	public void run(int port) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup(8, new DefaultThreadFactory("udpServer", true));
		Bootstrap b = new Bootstrap();
		// 由于我们用的是UDP协议，所以要用NioDatagramChannel来创建
		b.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)// 支持广播
				.handler(new UDPServerHandler());// UDPServerHandler是业务处理类
		b.bind(port).sync().channel().closeFuture().await();
	}

//	@PostConstruct
	public void runServer() throws Exception {
		System.out.println("---------udpServerStart");
		cachedThreadPool.execute(new Runnable() {
			public void run() {
				try {
					new UDPServer().run(udpPort);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
}
