package main.java.com.huawei.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import main.java.com.huawei.unit.Car;

public class Parameter {
	//仿真参数
	public static int blockStep = 5;                                //记录前多少秒的时间片数据
	public static ArrayList<Integer> startSpeed; 
	public static boolean isPrintLastPriorityCarEndTime = false;
	public static int blockSize = 3;                                //记录前多少秒的时间片数据
	public static int blockGoBackStep = 5;                          //如果发生死锁，往前退回时间片数量
	public static double roadDensityDelta= 0.3;                     //如果道路空余量阈值小于该值，则不加新车
	public static HashMap<Integer, SpeedParameter> maxSpeedParameter;
	public static boolean isReplanPath = false;
	public static int priorityCarDelay = 10;
	public static double priorityCarValue = 1.5;
	
	public static int joinToNextSpeedRate = 0;
	//路径规划参数
	public static double timeValueWeight = 1.0;                     //通过道路的预计时间权重
	public static double priorityWeight = 1;	                    //道路优先级，路径规划中，我们希望尽可能直行
	public static double carSumWeight = 2.0;                        //当前道路一共有多少辆
	public static double crowdValueWeight = 60.0;                   //道路拥挤程度
	public static double speedValueWeight = 60.0;                   //车辆最大速度与道路最大速度差异
	public static double notArrivedCarCountWeight = 10.0;            //已经选择该道路但尚未到达
	public static double laneNumberWeight =-1.0;                    //车道宽度
	public static double crossCrowdValueWeight = 10.0;              //路口拥挤程度
	public static double rePlanPathValueWeight = 0.01;               //当超过预设到达终点时间阈值后，重新路径规划
	public static int priorityCarCount = 1200;               //当超过预设到达终点时间阈值后，重新路径规划
	public static int normalCarStartTime = 1500;
	public static double forbidTimeAllowCarCountRate =0.5;
	public static boolean isPresetOnly = false;
	public static int  stopStep = -1;
	
	public static void setParameter(Map<Integer, Car> carMap) {
		// TODO Auto-generated constructor stub
		
		//多图调参，不鼓励，不支持，不反对～
		//根据两个地图中公共的某辆车对应出发cross(一般不一样)进行区分两张地图
		int startCross = carMap.get(52619).getStartCross();
				
		if(startCross == 589){
			setParameterMap1();
		}
		else if(startCross == 1974) {
			setParameterMap2();
		}
		else {
			System.out.println("没有这个地图参数");
			System.exit(0);
		}
	}
	
	
	private static void setParameterMap1() {
		
		//仿真参数
		blockStep = 5;  //每隔五秒记录一次仿真数据,便于回滚
		blockSize = 10;  //一共保存最近的10次数据
		
		priorityCarCount = 4000; //优先级车辆加入时候,地图上最大允许车辆总数
		normalCarStartTime = 75; //允许普通车辆加入的最早时间
		
	  	forbidTimeAllowCarCountRate = 0.8;
		maxSpeedParameter = new HashMap<Integer, SpeedParameter>();
		stopStep  = -1; //如果不需要中途停止，设置为-1											
		startSpeed = new ArrayList<Integer>();
		startSpeed.add(4);
		startSpeed.add(6);
		startSpeed.add(8);
		startSpeed.add(10);
		startSpeed.add(12);
		startSpeed.add(14);
		startSpeed.add(16);
		//crowValueWeight,speedValueWeight,notArrivedCarCountWeight
		
		//-1表示优先级车辆
		maxSpeedParameter.put(-1,new SpeedParameter(0,5, 1,1, 1,70));
		
		//对应速度下普通车辆的参数
		maxSpeedParameter.put(4,new SpeedParameter(4800,1, 1200, 10, 800,1000));
		maxSpeedParameter.put(6,new SpeedParameter(3600,1, 1200, 10, 500,1200));
		maxSpeedParameter.put(8,new SpeedParameter(2800,1, 700, 100, 500,100));
		maxSpeedParameter.put(10,new SpeedParameter(2200,50, 100, 10, 100,1));
		maxSpeedParameter.put(12,new SpeedParameter(1600,100, 200, 200, 300,1));
		maxSpeedParameter.put(14,new SpeedParameter(1800,500, 500, 50, 80,1));
		maxSpeedParameter.put(16,new SpeedParameter(2000,1000, 100, 10, 50,1));

	}

	private static void setParameterMap2() {
		//仿真参数
		
		 isReplanPath = true;

		 blockStep = 10;
		 blockSize = 10;

		 priorityCarCount = 4000;
	   
		 normalCarStartTime = 80; //这个不能调太小

		 forbidTimeAllowCarCountRate = 0.8;

		 joinToNextSpeedRate = 0;
		 
		 maxSpeedParameter = new HashMap<Integer, SpeedParameter>();

		 startSpeed = new ArrayList<Integer>();
		 startSpeed.add(4);
		 startSpeed.add(6);
		 startSpeed.add(8);
		 startSpeed.add(10);
		 startSpeed.add(12);
		 startSpeed.add(14);
		 startSpeed.add(16);
		 
		 stopStep  = -1; //如果不需要中途停止，设置为-1
	   //crowValueWeight,speedValueWeight,notArrivedCarCountWeight
		 maxSpeedParameter.put(-1,new SpeedParameter(0,5, 1,1, 1,40));

		 maxSpeedParameter.put(4,new SpeedParameter(4500,1, 500, 10, 500,1000));
		 maxSpeedParameter.put(6,new SpeedParameter(3500,1, 500, 10, 500,1000));
		 maxSpeedParameter.put(8,new SpeedParameter(4000,10, 100, 10, 100,1));
		 maxSpeedParameter.put(10,new SpeedParameter(3400,100, 100, 10, 100,1));
		 maxSpeedParameter.put(12,new SpeedParameter(3000,100, 100, 200, 100,1));
		 maxSpeedParameter.put(14,new SpeedParameter(2500,1000, 100, 100, 50,1));
		 maxSpeedParameter.put(16,new SpeedParameter(2600,1000, 100, 10, 100,1));
	}

	
}
