package main.java.com.huawei.system;

public class SpeedParameter {
	//每一个速度下路径规划参数
	public int maxSpeed;                                      //速度
	public int allowCarCount;								//最大允许发车数量
	public double timeValueWeight = 1.0;                     //通过道路的预计时间权重
	public double priorityWeight = 1;	                    //道路优先级，路径规划中，我们希望尽可能直行
	public double carSumWeight = 2.0;                        //当前道路一共有多少辆
	public double crowdValueWeight = 60.0;                   //道路拥挤程度
	public double speedValueWeight = 60.0;                   //车辆最大速度与道路最大速度差异
	public double notArrivedNormalCarCountWeight =10.0;            //已经选择该道路但尚未到达
	public double notArrivedPriorityCarCountWeight =10.0;            //已经选择该道路但尚未到达
	public double laneNumberWeight =-1.0;                    //车道宽度
	public double crossCrowdValueWeight = 10.0;              //路口拥挤程度
	public double rePlanPathValueWeight = 0.8;               //当超过预设到达终点时间阈值后，重新路径规划
	public SpeedParameter(int allowCarCount, double timeValueWeight,
			double crowdValueWeight,double speedValueWeight,double notArrivedCarNormalWeight,
			double notArrivedPriorityCarWeight) {
			// TODO Auto-generated constructor stub
		this.allowCarCount = allowCarCount;
		this.timeValueWeight = timeValueWeight;
		this.crowdValueWeight =crowdValueWeight;
		this.speedValueWeight = speedValueWeight;
		this.notArrivedNormalCarCountWeight = notArrivedCarNormalWeight;
		this.notArrivedPriorityCarCountWeight = notArrivedPriorityCarWeight;
		
	}
		
}
