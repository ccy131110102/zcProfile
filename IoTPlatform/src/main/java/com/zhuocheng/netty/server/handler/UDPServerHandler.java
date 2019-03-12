package com.zhuocheng.netty.server.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zhuocheng.constant.Constant;
import com.zhuocheng.exception.HttpRequestException;
import com.zhuocheng.exception.ProcessorException;
import com.zhuocheng.exception.ProfileHandleException;
import com.zhuocheng.handler.DeviceInfoHandler;
import com.zhuocheng.handler.DeviceMessageHandler;
import com.zhuocheng.handler.MessageStorageHandler;
import com.zhuocheng.handler.ServiceMessageHandler;
import com.zhuocheng.processor.DecodeProcessor;
import com.zhuocheng.processor.factory.ProcessorFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import redis.clients.jedis.JedisPool;

public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	@Autowired
	JedisPool jedisPool;

	private Logger logger = Logger.getLogger(UDPServerHandler.class);

	static AtomicInteger amount = new AtomicInteger(0);

	/**
	 * 在这个方法中，形参packet客户端发过来的DatagramPacket对象 DatagramPacket 类解释 1.官网是这么说的： The
	 * message container that is used for {@link DatagramChannel} to communicate
	 * with the remote peer. 翻译：DatagramPacket 是消息容器，这个消息容器被
	 * DatagramChannel使用，作用是用来和远程设备交流
	 * 2.看它的源码我们发现DatagramPacket是final类不能被继承，只能被使用。我们还发现DatagramChannel最终实现了AddressedEnvelope接口，接下来我们看一下AddressedEnvelope接口。
	 * AddressedEnvelope接口官网解释如下： A message that wraps another message with a
	 * sender address and a recipient address. 翻译：这是一个消息,这个消息包含发送者和接受者消息
	 * 3.那我们知道了DatagramPacket它包含了发送者和接受者的消息， 通过content()来获取消息内容
	 * 通过sender();来获取发送者的消息 通过recipient();来获取接收者的消息。
	 * 
	 * 4.public DatagramPacket(ByteBuf data, InetSocketAddress recipient) {}
	 * 这个DatagramPacket其中的一个构造方法，data 是发送内容;是发送都信息。
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

		String message = packet.content().toString(CharsetUtil.UTF_8);// 上面说了，通过content()来获取消息内容
		// String str = req + " " + packet.sender().getAddress() + " " +
		System.out.println("---------------------------------------");
		System.out.println("nettyServer接收：" + message);
		try {

			String resultMessage = nettyUdpServerProcess(message);

			System.out.println("nettyServer回复：" + resultMessage);
			System.out.println("---------------------------------------");

			ctx.writeAndFlush(new DatagramPacket(
					Unpooled.copiedBuffer("\r\n" + resultMessage.toUpperCase() + "\r\n", CharsetUtil.UTF_8),
					packet.sender()));
		} catch (Exception e) {
			e.printStackTrace();
			ctx.writeAndFlush(new DatagramPacket(
					Unpooled.copiedBuffer("\r\n" + "00000000000000000000" + "\r\n", CharsetUtil.UTF_8),
					packet.sender()));

		}
		// }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		cause.printStackTrace();
	}

	/**
	 * @throws IOException
	 * @throws ProfileHandleException
	 * @Description: 用于udp请求的响应
	 */
	public String nettyUdpServerProcess(String message) throws IOException {

		// 用于保存返回报文
		String sendStr = "";

		DecodeProcessor dprocessor;
		String combileMessageStr = null;

		System.out.println(message);

		// 根据当前设备的编号从redis中获取相应的设备及profile信息
		String deviceId = String.valueOf(Long.parseLong(new DeviceMessageHandler(message).getAddress(), 16));
		Map deviceInfoMap = DeviceInfoHandler.getInstance().getDeviceInfoByDeviceId(deviceId);
		String appId = (String) deviceInfoMap.get(Constant.DEVICEINFO_APPID);
		String profileId = (String) deviceInfoMap.get(Constant.DEVICEINFO_PROFILEID);

		String command = new DeviceMessageHandler(message).getCommand();
		String id = new DeviceMessageHandler(message).getId();

		// boolean hasNextMessage =
		// DeviceConfirmHandler.getInstance().hasMessageToConfirm(id, appId,
		// deviceId);
		// if (hasNextMessage) {
		// DeviceConfirmHandler.getInstance().confirmMessage(id, profileId,
		// appId, deviceId);
		// }

		try {
			if (command.equals("00")) {
				// 取暂存指令

				// 判断当前指令的ID是否改变，如果改变则说明上次“取暂存”已完毕，剔除队首消息
				boolean hasNextMessage = MessageStorageHandler.getInstance().isPacketIdChanged(id, appId, profileId,
						deviceId, Constant.COMMAND_SAVETYPE_DOWN);

				if (hasNextMessage) {
					// 获取当前暂存队首消息
					String messageStr = MessageStorageHandler.getInstance().retireMessage(id, appId, profileId,
							deviceId, Constant.COMMAND_SAVETYPE_DOWN);

					// 暂存队列不为空则拼接响应报文体
					if (messageStr != null) {
						Map messageMap = (Map) JSONObject.parse(messageStr, Feature.OrderedField);
						messageMap.put("id", id);
						combileMessageStr = ServiceMessageHandler.messageMapToMessage(messageMap);
						if (combileMessageStr != null) {
							combileMessageStr = combileMessageStr.toUpperCase();
						}
					}
				} else {
					// 暂存队列为空则返回0000通知设备暂存队列为空停止“取暂存”动作
					combileMessageStr = new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler
							.getInstance().hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN),
							command);
				}

				return combileMessageStr.toUpperCase();
				// return combileMessageStr.toUpperCase();
			} else if (command.equals("FF")) {
				// 如果当前命令字为FF，则直接响应响应确认报文FF
				return new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
						.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
						.toUpperCase();
			} else {
				// 既不是00也不是FF，则为上传动作，将相应上传信息解码并保存回调
				dprocessor = ProcessorFactory.createDecodeProcessor(message, jedisPool);
				dprocessor.publish(dprocessor.decode(), Constant.PUBLISH_TYPE_PROPERTIES);

				return new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
						.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command)
						.toUpperCase();

				// return new
				// DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
				// .hasNextMessage(appId, deviceId,
				// Constant.COMMAND_SAVETYPE_DOWN)).toUpperCase();
			}

		} catch (ProcessorException e) {
			// 写错误日志
			e.printStackTrace();
			logger.error(e.getMessage());
			return new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command).toUpperCase();
		} catch (HttpRequestException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			// e.printStackTrace();

			return new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command).toUpperCase();

			// return new DeviceMessageHandler(message).combileConfirmMessage(
			// MessageStorageHandler.getInstance().hasNextMessage(appId,
			// deviceId, Constant.COMMAND_SAVETYPE_DOWN))
			// .toUpperCase();

		} catch (ProfileHandleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command).toUpperCase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return new DeviceMessageHandler(message).combileConfirmMessage(MessageStorageHandler.getInstance()
					.hasNextMessage(appId, profileId, deviceId, Constant.COMMAND_SAVETYPE_DOWN), command).toUpperCase();
		}

	}
}
