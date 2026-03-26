package com.goolton.content_sdk

/**
 * 加载配置
 */
val LOAD_CONFIG =  BleMsg(Action.LOAD_APP_DATA,cmd = CMD.ACK)


/**
 * 查看文件列表
 */
val VIEW_FILE =  BleMsg(Action.VIEW_FILE,cmd = CMD.ACK)

/**
 * 添加文件
 */
val ADD_FILE = BleMsg(Action.ADD_FILE, cmd = CMD.ACK)


/**
 * 设置信息
 */
val DEVICE_INFO = BleMsg(Action.DEVICE_INFO, cmd = CMD.ACK)