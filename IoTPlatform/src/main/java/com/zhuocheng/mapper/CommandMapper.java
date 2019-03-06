package com.zhuocheng.mapper;

import java.util.ArrayList;
import java.util.Date;

import org.apache.ibatis.annotations.Param;

import com.zhuocheng.model.Command;

public interface CommandMapper {
	
	/**
	 * 描述：获取所有命令信息
	 * 
	 * @param 
	 * @return 命令信息列表
	 */
	public ArrayList<Command> getAllCommandsInfo();
	
	/**
	 * 描述：根据设备唯一编号获取该设备对应的命令列表
	 * 
	 * @param deviceId
	 * @return 命令列表
	 */
	public ArrayList<Command> getAllCommandsInfoByDeviceId(String deviceId);
	
	/**
	 * 描述：插入一条命令信息
	 * 
	 * @param command(命令信息实体)
	 * @return 返回插入条数(如果为1则表示插入成功，为0则表示插入失败)
	 */
	public int insertOneCommandInfo(Command command);
	
	/**
	 * 描述：更新命令状态
	 * 
	 * @param command(设备信息实体)
	 * @return 返回影响条数(如果为1则表示更新成功，为0则表示更新失败)
	 */
	public int updateCommandState(Command command);
	
	/**
	 * 描述：按条件分页查询指令
	 * 
	 * @param command(设备信息实体)
	 * @return 返回命令列表
	 */
	public ArrayList<Command> selectCommand(@Param("commandDirection") String commandDirection,@Param("deviceId") String deviceId, @Param("appId") String appId,@Param("profileId") String profileId,@Param("serviceId") String serviceId,@Param("serviceType") String serviceType,@Param("startDate") Date startDate,@Param("endDate") Date endDate,@Param("isPage") boolean isPage,@Param("startIndex") int startIndex,@Param("size") int size);
	/**
	 * 描述：按条件获取记录总数
	 * 
	 * @param command(设备信息实体)
	 * @return 返回影响条数(如果为1则表示更新成功，为0则表示更新失败)
	 */
	public int selectCommandCount(@Param("commandDirection") String commandDirection,@Param("deviceId") String deviceId, @Param("appId") String appId,@Param("profileId") String profileId,@Param("serviceId") String serviceId,@Param("serviceType") String serviceType,@Param("startDate") Date startDate,@Param("endDate") Date endDate);
}
