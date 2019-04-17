package main.java.com.huawei.system;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import main.java.com.huawei.unit.Car;

public class CalculateTime {
	private int maxNumber = 99999999;
	private int T,Tsum,Tpri,Tsumpri;
	private double Te,Tesum;
	
	private double a,b;
	
	private int carSum,priorityCarSum;
	
	
	
	private int carMaxSpeed,priorityCarMaxSpeed,carMinSpeed,priorityCarMinSpeed;
	
	private int carMaxEndTime,priorityCarMaxEndTime;
	
	private int carMinAllowTime,carMaxAllowTime,priorityCarMaxAllowTime,priorityCarMinAllowTime;
	
	private Set<Integer> carStartCross,carEndCross,priorityCarStartCross,priorityCarEndCross;
	
	
	public CalculateTime(Map<Integer,Car> carMap) {
		// TODO Auto-generated constructor stub
		System.out.println(carMap.size());
		calTime(carMap);
		calAB();
		calAnswer();
	}
	
	private void calTime(Map<Integer,Car> carMap) {
		Tsum = 0;
		Tsumpri = 0;
		Tpri = 0;
		
		carSum = carMap.size();
		priorityCarSum = 0;
		
		carMaxSpeed = -1;
		carMinSpeed = maxNumber;
		
		priorityCarMaxSpeed = -1;
		priorityCarMinSpeed = maxNumber;
		
		carMaxEndTime = -1;
		priorityCarMaxEndTime = -1;
		
		carMinAllowTime = maxNumber; //这里的出发时间群里面说是计划出发时间
		carMaxAllowTime = -1;
		
		priorityCarMinAllowTime = maxNumber;
		priorityCarMaxAllowTime = -1;
		
		carStartCross = new HashSet<Integer>();
		carEndCross = new HashSet<Integer>();
		
		priorityCarStartCross =  new HashSet<Integer>();
		priorityCarEndCross = new HashSet<Integer>();
				
		int endTime = -1;
		int allowTime = -1;
		int maxSpeed = -1;
		int startCross = -1;
		int endCross = -1;
		
		for(int carId : carMap.keySet()) {
			
			Car car = carMap.get(carId);
			
			endTime = car.getEndTime();
			allowTime = car.getAllowStartTime();
			maxSpeed = car.getMaxSpeed();
			startCross = car.getStartCross();
			endCross = car.getEndCross();
			
			Tsum += endTime - allowTime;
			
			//速度
			if(maxSpeed > carMaxSpeed) {
				carMaxSpeed = maxSpeed;
			}
			if(maxSpeed < carMinSpeed){
				carMinSpeed = maxSpeed;
			}
			//出发时间
			if(allowTime > carMaxAllowTime) {
				carMaxAllowTime = allowTime;
			}
			if(allowTime < carMinAllowTime) {
				carMinAllowTime = allowTime;
			}
			//结束时间
			if(endTime > carMaxEndTime) {
				carMaxEndTime = endTime;			
			}
			
			//地点分布
			carStartCross.add(startCross);
			carEndCross.add(endCross);
			
			if(car.isPriority() == true) {
				priorityCarSum++;
				
				Tsumpri += endTime - allowTime;
				
				//速度
				if(maxSpeed > priorityCarMaxSpeed) {
					priorityCarMaxSpeed = maxSpeed;
				}
				if(maxSpeed < priorityCarMinSpeed){
					priorityCarMinSpeed = maxSpeed;
				}
				//出发时间
				if(allowTime > priorityCarMaxAllowTime) {
					priorityCarMaxAllowTime = allowTime;
				}
				if(allowTime < priorityCarMinAllowTime) {
					priorityCarMinAllowTime = allowTime;
				}
				//结束时间
				if(endTime > priorityCarMaxEndTime) {
					priorityCarMaxEndTime = endTime;			
				}

				//分布
				priorityCarStartCross.add(startCross);
				priorityCarEndCross.add(endCross);
				
				
			}
			
		}
		Tpri = priorityCarMaxEndTime - priorityCarMinAllowTime;
		T = carMaxEndTime;
	}
	private void calAB() {
		a = 0.05 * carSum / priorityCarSum + 
			(0.2375 * carMaxSpeed / carMinSpeed) / (1.0 * priorityCarMaxSpeed / priorityCarMinSpeed) +
			(0.2375 * carMaxAllowTime / carMinAllowTime) / (1.0 * priorityCarMaxAllowTime / priorityCarMinAllowTime) +
			(0.2375 * carStartCross.size() / priorityCarStartCross.size()) + 
			(0.2375 * carEndCross.size() / priorityCarEndCross.size());
		b = 0.8 * carSum / priorityCarSum + 
			(0.05 * carMaxSpeed / carMinSpeed) / (1.0 * priorityCarMaxSpeed / priorityCarMinSpeed) +
			(0.05 * carMaxAllowTime / carMinAllowTime) / (1.0 * priorityCarMaxAllowTime / priorityCarMinAllowTime) +
			(0.05 * carStartCross.size() / priorityCarStartCross.size()) + 
			(0.05 * carEndCross.size() / priorityCarEndCross.size()); 
			
	}
	private void calAnswer() {

		Te = a*Tpri + T;
		Tesum = b*Tsumpri  + Tsum;
		System.out.println("a : " + a + "  b : " + b);
		System.out.println("T : " + T  + "   Tpri : " + Tpri + " Tsumpri : " + Tsumpri + " Tsum :" + Tsum);
		System.out.println("Te : " + Te + "    Tesum : " + Tesum);
	}
}
