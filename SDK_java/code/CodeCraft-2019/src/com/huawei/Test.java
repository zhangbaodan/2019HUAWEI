package com.huawei;
import java.util.TreeSet;

import main.java.com.huawei.Main;
import main.java.com.huawei.system.JudgeSystem;
import main.java.com.huawei.unit.Road;
public class Test {
	public static boolean isOldData = true;
	
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		//程序主入口，有两种进入模式，由isTest参数控制．
		//当isTest = 0时候表示运行模式，最后可以输出路径规划的结果到本地
		//当isTest = 1时候表示判题器模式，读取本地结果文件以及车辆等文件后，输出运行结果到控制台
		
		long startTime=System.currentTimeMillis();   //获取开始时间
		String config = "config_map2";

		int isTest = 0;
		Main test = new Main();
	
		String[] configer = new String[5];
		
		configer[0] = "/home/poorlemon/Work/HuaWei/SDK/newData/"+config+"/car.txt";
		configer[1] ="/home/poorlemon/Work/HuaWei/SDK/newData/"+config+"/road.txt";				
		configer[2] = "/home/poorlemon/Work/HuaWei/SDK/newData/"+config+"/cross.txt";
		configer[3] = "/home/poorlemon/Work/HuaWei/SDK/newData/"+config+"/presetAnswer.txt";
		configer[4] = "/home/poorlemon/Work/HuaWei/SDK/newData/"+config+"/answer.txt";		
	
		if(isTest == 1) {
			//纯判题器系统
			JudgeSystem judgeSystem = new JudgeSystem(configer);
		}
		else {
			//动态路径规划+回滚局部不发车解死锁+分速调参发车
			test.main(configer);
		}
		long endTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
	}
}
