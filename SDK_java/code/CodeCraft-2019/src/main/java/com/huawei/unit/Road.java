package main.java.com.huawei.unit;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import main.java.com.huawei.system.Parameter;
//这里面一共修正了两个BUG,一个是CarSum 一个是density
public class Road  implements Comparable<Road>,Cloneable{
	private int id;
	private int length;
	private int maxSpeed;
	private int number;
	private int startCrossId;
	private int endCrossId;
	private int isTwoWay; 
	private int flag = 0;
	
	private ArrayList<Schedule> forwardNormalSchedules= new ArrayList<Schedule>();
	private ArrayList<Schedule> forwardPrioritySchedules = new ArrayList<Schedule>();
	private ArrayList<Schedule> reverseNormalSchedules = new ArrayList<Schedule>();
	private ArrayList<Schedule> reversePrioritySchedules = new ArrayList<Schedule>();
	
	
	private TreeSet<Integer> readyToJoinForwardPriorityCarIds = new TreeSet<Integer>();
	private TreeSet<Integer> readyToJoinReversePriorityCarIds = new TreeSet<Integer>();
	
	private Set<Integer> forwardNormalCarSet; //所有选择这条路的车辆id信息
	private Set<Integer> reverseNormalCarSet; //所有选择这条路的车辆id信息
	private Set<Integer> forwardPriorityCarSet; //所有选择这条路的车辆id信息
	private Set<Integer> reversePriorityCarSet; //所有选择这条路的车辆id信息
 
	private int[][] map;
	public Road(int id, int length, int maxSpeed, int number, int startCrossId, int endCrossId, int isTwoWay) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.length = length;
		this.maxSpeed = maxSpeed;
		this.number = number;
		this.startCrossId = startCrossId;
		this.endCrossId = endCrossId;
		this.isTwoWay = isTwoWay;
	
		this.forwardNormalCarSet = new HashSet<Integer>();
		this.reverseNormalCarSet = new HashSet<Integer>();
		this.forwardPriorityCarSet = new HashSet<Integer>();
		this.reversePriorityCarSet = new HashSet<Integer>();
		
		if(isTwoWay == 1)
			//[1~number]表示从startCrossId到endCrossId道路，[number+1,number*2]表示反向道路
			map = new int[number*2+1][length]; 
		else
			map = new int[number+1][length];
		
		
	}
	public Set<Integer> getForwardPriorityCarSet() {
		return forwardPriorityCarSet;
	}
	//BUG函数
	public double getCrowdValue(int crossId) {
		double crowdValue = -1;
		int  capacty = getCapacity(crossId);
		crowdValue = 1.0*(length*number - capacty)/(length * number);
		return crowdValue;
	}
	//BUG函数
	public int getCarSum(int crossId,boolean isPriority) {
		if(crossId == startCrossId) {
			if(isPriority) {
				return forwardPriorityCarSet.size();
			}
			else {
				return forwardNormalCarSet.size();
			}
		}

		else if(crossId == endCrossId){
			if(isPriority) {
				return reversePriorityCarSet.size();
			}
			else {
				return reverseNormalCarSet.size();
			}
		}
		else {
			System.out.println("出错2");
			System.exit(0);
			return -1;
		}	
	}

	public ArrayList<Integer> getOutCarIds(int crossId){
		
		ArrayList<Integer> outArrayList = new ArrayList<Integer>();
		if(crossId == startCrossId && isTwoWay ==1) { //反向的
			for(int pos = 0;pos<length;pos++) {  //距离路口距离，从0开始
				for(int l = number+1;l<=number*2;l++) { //遍历道路，从编号小的开始
					if(map[l][pos] != 0)
						outArrayList.add(map[l][pos]);
				}
			}
		}
		else if(crossId == endCrossId) {
			for(int pos = 0;pos<length;pos++) {  //距离路口距离，从0开始
				for(int l = 1;l<=number;l++) { //遍历道路，从编号小的开始
					if(map[l][pos] != 0)
						outArrayList.add(map[l][pos]);
				}
			}	
		}
		else {	
		}
		return outArrayList;
	
		
	}
	public int  getLastPosCarId(int lane) {
		if(map[lane][length-1]==0)
			return -1;
		else 
			return map[lane][length-1];
	}
	public int getFirstPriorityCarId(Map<Integer, Car> carMap,int crossId){

		int carId = -1;
		boolean flag = false;
		int starLane = 1;
		int endLane = number;
		if(crossId == startCrossId) {
			if(isTwoWay == 0) {
				return -1;
			}
			starLane = number+1;
			endLane = 2*number;
		}
		//先看当前有没有可以通行的优先级车辆
		ArrayList<PriorityCar> priorityCarIds = new ArrayList<PriorityCar>();
		for(int l = starLane;l<=endLane;l++) {
			carId = getFirstCarIdInLane(l);
			if(carId == -1) {
				continue;
			}
			Car car  = carMap.get(carId);
			if(car.getFlag() == 0 || car.getFlag() == -1) {
				System.out.println("naaaani?");
				System.exit(0);
			}
			if(car.isPriority() == true && car.getFlag() == 2 && car.mightPassCross(maxSpeed)) {
				priorityCarIds.add(new PriorityCar(carId, car.getPosition(), car.getLaneId()));
				
			}
		}
		//有可以通行的优先级车辆
	
		if(priorityCarIds.size() != 0) {
			
			Collections.sort(priorityCarIds);
			return priorityCarIds.get(0).getCarId();
		}
		
		//最外面不存在优先级车辆,这时候就看pos
		for(int pos = 0;pos<length;pos++) {
			for(int l = starLane;l<=endLane;l++) {
				if(map[l][pos]!=0) {
					carId = map[l][pos];
					Car car  = carMap.get(carId);
					if(car.getFlag() == 0 || car.getFlag() == -1) {
						System.out.println("naaaani?");
						System.exit(0);
					}		

					if(car.getFlag() == 2 && car.mightPassCross(maxSpeed)) {
						if(car.isPriority() == true ) {
							printMap();
							System.out.println("naaaani@");
							System.exit(0);
						}
						return carId;
					}
					
				}
				
			}
		}
		return -1;
		
	}
	private int getFirstCarIdInLane(int lane) {
		for(int pos = 0;pos<length;pos++) {
			if(map[lane][pos] != 0)
				return map[lane][pos];
		}
		return -1;
	}
	public int getNumber() {
		return number;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getFlag() {
		return flag;
	}
	public void addCar(int carId,int crossId,boolean isPriority) {
		if(crossId == startCrossId) {
			if(isPriority) {
				forwardPriorityCarSet.add(carId);
			}
			else {
				forwardNormalCarSet.add(carId);
			}
			
		}
		else if(crossId == endCrossId) {
			if(isPriority) {
				reversePriorityCarSet.add(carId);
			}
			else {
				reverseNormalCarSet.add(carId);
			}
		}
		else {
			System.out.println("carId " + carId + "  " + crossId);
			System.out.println(startCrossId + "   " + endCrossId);
			
			System.out.println("出错9");
			System.exit(0);
		}
		
	
		
	}
	

	
	public int getIsTwoWay() {
		return isTwoWay;
	}
	
	
	public int getStartCrossId() {
		return startCrossId;
	}
	public int getEndCrossId() {
		return endCrossId;
	}
	public int getAheadCarId(int carId,int laneId, int position) {
		if(position == 0) {
			return -1;
		}
		for(int pos = position-1;pos>=0;pos--) {
			if(map[laneId][pos] != 0) {
				return map[laneId][pos];
			}
		}		
		return -1;
	}
	
	public int getNotArrivedCarCount(int crossId,boolean isPriority) {//之前已经选择了这条路，但是尚未到达的
		if(crossId == startCrossId) {
			if(isPriority) {
				return forwardPriorityCarSet.size();
			}
			else {
				return forwardNormalCarSet.size();
			}
		}
		else if(crossId == endCrossId){
			if(isPriority) {
				return reversePriorityCarSet.size();
			}
			else {
				return reverseNormalCarSet.size();
			}
		}
		else {
			System.out.println("出错2");
			System.exit(0);
			return -1;
		}	
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isConnected(int crossId) { //检测这条路和这个cross是否是连接的
		if(startCrossId == crossId || endCrossId == crossId) {
			return true;
		}
		return false;
	}
	
	public boolean checkTwoWay() {
		if(isTwoWay == 1) {
			return true;
		}
		return false;
	}
	public void updatePos(int carId, int laneId, int oldPos, int newPos) {		
		if(oldPos != -1) {
			if (map[laneId][oldPos] == carId) {
				map[laneId][oldPos] = 0;
			}
			else {
				System.out.println(id);
				System.out.println(map[laneId][oldPos]);
				System.out.println(carId);
				System.out.println("wrong............1");
				System.exit(0);
	
			}
		}
		if(newPos != -1) {
			if(map[laneId][newPos] == 0) {
				map[laneId][newPos] = carId;
			}
			else {
				if(map[laneId][newPos] == carId) {
					return;
				}
				System.out.println(map[laneId][newPos]);
				System.out.println("carId  :  " +carId + " " + "roadId  : " + id + "   lanId:" + laneId + " oldPos : " + oldPos);
				System.out.println("wrong............2");
				System.exit(0);
			}
		}
	}
	public ArrayList<Integer> getLastCarIds(int crossId) {
		ArrayList<Integer> carIds = new ArrayList<Integer>();
		if(crossId == startCrossId) { //正向车道
			for(int l = 1;l<=number;l++) {
				int f = 0;
				for(int pos = length-1;pos>=0;pos--) {
					
					if(map[l][pos]!=0) {
						carIds.add(map[l][pos]);
						f = 1;
						break;
					}
					
				}
				if(f == 0)
					carIds.add(-1);
			}
		}
		else if(crossId == endCrossId) { //反向车道
			for(int l = number+1;l<=2*number;l++) {
				int f = 0;
				for(int pos = length-1;pos>=0;pos--) {
					
					if(map[l][pos]!=0) {
						carIds.add(map[l][pos]);
						f = 1;
						break;
					}
					
				}
				if(f == 0)
					carIds.add(-1);
			}
		}
		else {
			System.out.println("cuo");
			System.exit(0);
			return null;
		}
		if(carIds.size() == number) {
			return carIds;
			
		}
		else {
			System.out.println("cuo");
			System.exit(0);
			return null;
			
		}
	}
	public int  getBeforeCrossId(int crossId) {
		if(crossId == endCrossId) {
			return startCrossId;
		}
		else if(crossId == startCrossId) {
			return endCrossId;
		}
		else {
			System.out.println("出错3");
			System.exit(0);
			return -1;
		}
	}
	public int getLength() {
		return length;
	}
	public int getMaxSpeed() {
		return maxSpeed;
	}
	public int getViableLaneId(int crossId) {
		
		if(crossId == startCrossId) { //正向驶入
			for(int l = 1;l<=number;l++) {
				if(map[l][length-1] == 0) { //只需检测最后一个位子是否有车就行
					return l;
				}
			}
		}
		else if(crossId == endCrossId && isTwoWay == 1) {//反向驶入
			for(int l = number+1;l<=2*number;l++) {
				if(map[l][length-1] == 0) {
					return l;
				}
			}
		}
		else {
//			System.out.println(crossId);
//			System.out.println("新车加入出bug");
//			System.exit(0);
		}
		
		return -1;
	}

	public int getMightWaitViableLaneId(Map<Integer,Car> carMap,int crossId) {
		
		if(crossId == startCrossId) { //正向驶入
			for(int l = 1;l<=number;l++) {
				if(map[l][length-1] == 0) { //只需检测最后一个位子是否有车就行
					return l;
				}
				else { //有车
					if(carMap.get(map[l][length-1]).getFlag() == 2) {
						return l;
					}
				}
			}
		}
		else if(crossId == endCrossId && isTwoWay == 1) {//反向驶入
			for(int l = number+1;l<=2*number;l++) {
				if(map[l][length-1] == 0) {
					return l;
				}
				if(carMap.get(map[l][length-1]).getFlag() == 2) {
					return l;
				}
			}
		}
		else {
//			System.out.println(crossId);
//			System.out.println("新车加入出bug");
//			System.exit(0);
		}
		
		return -1;
	}
	public ArrayList<Integer> getBeforeCarIds(int laneId,int crossId, int pos) {
		ArrayList<Integer> beforeCarIds = new ArrayList<Integer>();
		if(crossId == endCrossId) { //正向驶入
			if(pos == length - 1)
				return beforeCarIds;
			for(int i = pos+1 ;i<length;i++) {
				if(map[laneId][i] !=0) {
					beforeCarIds.add(map[laneId][i]);
				}
			}
			
		}
		else if(crossId == startCrossId && isTwoWay == 1) {//反向驶入

			if(pos == length - 1)
				return beforeCarIds;
			for(int i = pos+1 ;i<length;i++) {
				if(map[laneId][i] !=0) {
					beforeCarIds.add(map[laneId][i]);
				}
			}
		}
		else {
			
			System.out.println("新车加入出bug2");
			System.exit(0);
			return null;
		}
		return beforeCarIds;
	}
	public int getLastCarId(int laneId) {		
		for(int pos = length-1;pos>=0;pos--) {
			if(map[laneId][pos] != 0) {
				return map[laneId][pos];
			}
		}
		return -1;
	}
	public void isArrived(int carId,int crossId,boolean isPriority) {
		if(crossId == startCrossId) {
			if(isPriority) {
				if(forwardPriorityCarSet.contains(carId)) {
					forwardPriorityCarSet.remove(carId);
				}
				else {
					System.out.println("啥子情况....1");
					System.exit(0);
				}	
			}
			else {
				if(forwardNormalCarSet.contains(carId)) {
					forwardNormalCarSet.remove(carId);
				}
				else {
					System.out.println("啥子情况...2");
					System.exit(0);
				}
			}
			
		}
		else if(crossId == endCrossId) {
			if(isPriority) {
				if(reversePriorityCarSet.contains(carId)) {
					reversePriorityCarSet.remove(carId);
				}
				else {
					System.out.println("啥子情况...3");
					System.exit(0);
				}	
			}
			else {
				if(reverseNormalCarSet.contains(carId)) {
					reverseNormalCarSet.remove(carId);
				}
				else {
					System.out.println(carId);
					System.out.println(id);
					System.out.println("啥子情况...4");
					System.exit(0);
				}
			}	}	
		else {
			System.out.println("我卢本伟没有开挂");
			System.exit(0);
		}
	}
	public int getMaxPos(int laneId) {
	
		for(int pos = length-1;pos>=0;pos--) {
//			System.out.println(laneId + "----" + pos);
			if(map[laneId][pos] != 0) {
				if(pos+1 == length) {
//					System.out.println("当前车道没有空余位子");
					return -1;
				}
				return pos+1;
			}
		}
		
		return 0;
	}
	public ArrayList<Integer> getAllCarIds(int crossId){
		ArrayList<Integer> allCars = new ArrayList<Integer>();
		if(crossId == endCrossId && isTwoWay == 1) //是否是从右到左的反向道路
			
			for(int l = number+1 ;l<=number*2;l++) {
				for(int pos = 0;pos<length;pos++) {
					if(map[l][pos] != 0) {
						allCars.add(map[l][pos]);
					}
				}
			}
		else if(crossId == startCrossId){
			for(int l = 0 ;l<=number;l++) {
				for(int pos = 0;pos<length;pos++) {
					if(map[l][pos] != 0) {
						allCars.add(map[l][pos]);
					}
				}
			}
		}
		else {
			System.out.println("startCrossId :" + startCrossId + "  endCrossId :  " + endCrossId + " crossId  :" + crossId );
			System.out.println("getAllCarIs  is wrong...............");
		}
		return allCars;
	}
	public boolean isOccupied() {
		int sum = number;
		if(isTwoWay == 1)
			 sum = number*2;
		for(int l = 0 ;l<=sum;l++) {
			for(int pos = 0;pos<length;pos++) {
				if(map[l][pos] != 0) {
					return true;
				}
			}
		}
		return false;
	}
	public ArrayList<Integer> getAllCarIdsIn(int crossId){
		ArrayList<Integer> allCars = new ArrayList<Integer>();
		if(crossId == startCrossId && isTwoWay == 1) //是否是从右到左的反向道路
			
			for(int l = number+1 ;l<=number*2;l++) {
				for(int pos = 0;pos<length;pos++) {
					if(map[l][pos] != 0) {
						allCars.add(map[l][pos]);
					}
				}
			}
		else if(crossId == endCrossId){
			for(int l = 0 ;l<=number;l++) {
				for(int pos = 0;pos<length;pos++) {
					if(map[l][pos] != 0) {
						allCars.add(map[l][pos]);
					}
				}
			}
		}
		else {
			System.out.println("startCrossId :" + startCrossId + "  endCrossId :  " + endCrossId + " crossId  :" + crossId );
			System.out.println("getAllCarIs  is wrong...............");
		}
		return allCars;
	}
	
	public void printMap() {
		// TODO Auto-generated method stub
		System.out.println("----------------------------------------------------------------");
		System.out.println("roadId : " + id  + "   starCrossId: " + startCrossId + "    endCrossId :"+ endCrossId +
				"     maxSpeed : " + maxSpeed);
				
		System.out.println("正向车道  |路口| <------：");
		for(int l = 1;l<=number;l++) {
			for(int pos = 0;pos<length;pos++) {
				System.out.print(map[l][pos] + " ");
			}
			System.out.println();
		}
		if(isTwoWay == 1) {
			System.out.println("反向车道  |路口| <------：");
			for(int l = number+1;l<=2*number;l++) {
				for(int pos = 0;pos<length;pos++) {
					System.out.print(map[l][pos] + " ");
				}
				System.out.println();
			}
		}
		else {
			System.out.println("没有反向车道....");
		}
		System.out.println("----------------------------------------------------------------");
	}
	public int getNextCross(int laneId) {
		if(laneId <=number) {
			return endCrossId;
		}
		if(isTwoWay == 0) {
			System.out.println("单行道 车道位置出错了");
		}
		return startCrossId;
	}
	
	public boolean isForwardJoin(int crossId) {
		if(crossId == startCrossId)
			return true;
		else if(crossId == endCrossId)
			return false;
		else {
			System.out.println("没有这个路口");
			System.exit(0);
			return false;
		}
	}
	public ArrayList<Integer> getLaneCars(int lane){
		ArrayList<Integer> carIds = new ArrayList<Integer>();
		
			for(int pos = 0;pos<length;pos++) {
				if(map[lane][pos] != 0) {
					carIds.add(map[lane][pos]);
				}
			}
		return carIds;
	}
	public int getNeighborCross(int crossId) {
		if(crossId == startCrossId) {
			return endCrossId;
		}
		if(crossId == endCrossId && isTwoWay == 1) {
			return startCrossId;
		}
//		System.out.println("getNeighborCross is wrong....");
		return -1;
	}
	
	public int getCapacity(int crossId) {
		int sum = 0;
		if(crossId == startCrossId) {
			for(int l = 1;l<=number;l++) {
				for(int pos = length-1;pos>=0;pos--) {
					if(map[l][pos]==0)
						sum++;
					else
						break;
				}	
			}
			return sum;
		}
		else if(crossId == endCrossId && isTwoWay == 1){
			for(int l = number+1;l<=2*number;l++) {
				for(int pos = length-1;pos>=0;pos--) {
					if(map[l][pos]==0)
						sum++;
					else
						break;
				}	
			}
			return sum;
		}
		else {
			return 0;
		}
	
	}
	
	public int getAllowCarSum() {
		return this.length * this.number;
	}
	public void addPriorityCarId(int carId,int crossId) {
		if(crossId == startCrossId) {
			this.readyToJoinForwardPriorityCarIds.add(carId);
		}
		else if(crossId == endCrossId){
			this.readyToJoinReversePriorityCarIds.add(carId);
		}
		else {
			System.out.println("出错3");
			System.exit(0);
			return;
		}
		
	}
	public double getNextToTarRoadCarCount(Map<Integer, Car> carMap,int crossId,int tarRoadId) {
		double nextToTarRoadCarCount = 0;
		if(crossId == endCrossId) {
			//正向
			for(int l = 1;l<=number;l++) {
				for(int pos = length-1;pos>=0;pos--) {
					if(map[l][pos]!=0) {
						Car car  = carMap.get(map[l][pos]);
						if(car.getNextRoute() == tarRoadId) {
							if(car.isPriority())
								nextToTarRoadCarCount += Parameter.priorityCarValue;
							else {
								nextToTarRoadCarCount += 1;
							}
						}
					}
						
					
				}	
			}
			return nextToTarRoadCarCount;
		}
		else {
			//反向
			if(isTwoWay != 0) {
				

				for(int l = number;l<=number + number;l++) {
					for(int pos = length-1;pos>=0;pos--) {
						if(map[l][pos]!=0) {
							Car car  = carMap.get(map[l][pos]);
							if(car.getNextRoute() == tarRoadId) {
								if(car.isPriority())
									nextToTarRoadCarCount += Parameter.priorityCarValue;
								else {
									nextToTarRoadCarCount += 1;
								}
							}
						}
							
						
					}	
				}
			}
			return nextToTarRoadCarCount;
		}  
	}
	public boolean canJoinFromCross(int crossId) {
		
		if(crossId == startCrossId) {
			return true;
		}
		if(crossId == endCrossId && isTwoWay == 1) {
			return true;
		}
		return false;
	}
	public double getDensity(int crossId) {
		return 1.0*getCapacity(crossId)/getAllowCarSum();
	}
	
	public int[][] getMap() {
		return map;
	}
	@Override
	public Object clone() {
		Road o = null;
		try { 
		    o = (Road) super.clone();
		    o.map = this.map.clone();
		    for(int i = 0;i <this.map.length;i++){
		    	o.map[i] = this.map[i].clone();
		    	
		    	o.forwardNormalSchedules  = cloneSchedules(this.forwardNormalSchedules);
		    	o.forwardPrioritySchedules = cloneSchedules( this.forwardPrioritySchedules);
		    	o.reverseNormalSchedules = cloneSchedules(this.reverseNormalSchedules);
		    	o.reversePrioritySchedules = cloneSchedules( this.reversePrioritySchedules);    	
				
				o.forwardNormalCarSet = cloneSet(this.forwardNormalCarSet);
				o.forwardPriorityCarSet =cloneSet(this.forwardPriorityCarSet);
				o.reverseNormalCarSet = cloneSet(this.reverseNormalCarSet);
				o.reversePriorityCarSet =cloneSet(this.reversePriorityCarSet);
				
		    	o.readyToJoinForwardPriorityCarIds = (TreeSet<Integer>) this.readyToJoinForwardPriorityCarIds.clone();
		    	o.readyToJoinReversePriorityCarIds = (TreeSet<Integer>) this.readyToJoinReversePriorityCarIds.clone();
		    	
	        }
		    
		} catch (CloneNotSupportedException e) {
		    System.out.println(e.toString());
		}
		return o;
	}
	
	private ArrayList<Schedule> cloneSchedules(ArrayList<Schedule> tSchedules) {
		ArrayList<Schedule> oSchedules = new ArrayList<Schedule>();
		for(Schedule schedule : tSchedules) {
			oSchedules.add(new Schedule(schedule.getCarId(), schedule.getRealTime()));
		}
		return oSchedules;
	}
	private HashSet<Integer> cloneSet(Set<Integer> tSet) {
		HashSet<Integer> oSet = new HashSet<Integer>();	
		for(int i : tSet) {
			oSet.add(i);
		}
		return oSet;
	}
	public void initSchedule() {
		forwardNormalSchedules = new ArrayList<Schedule>();
		forwardPrioritySchedules = new ArrayList<Schedule>();
		
		reverseNormalSchedules = new ArrayList<Schedule>();
		reversePrioritySchedules = new ArrayList<Schedule>();
	}
	public void addSchedule(Schedule schedule,int crossId, boolean isPriority) {
		if(crossId == startCrossId) {
			if(isPriority == true) {
				forwardPrioritySchedules.add(schedule);
			}
			else {
				forwardNormalSchedules.add(schedule);
			}
		}
		else {
			if(isPriority == true) {
				reversePrioritySchedules.add(schedule);
			}
			else {
				reverseNormalSchedules.add(schedule);
			}
		}
	}

	public void sortSchedule() {
		Collections.sort(forwardNormalSchedules);
		Collections.sort(forwardPrioritySchedules);
		
		Collections.sort(reverseNormalSchedules);
		Collections.sort(reversePrioritySchedules);

	}
	public ArrayList<Schedule> getPrioritySchedules(int crossId){
		if(crossId == startCrossId) {
			return forwardPrioritySchedules;
		}
		else if(crossId == endCrossId) {
			return reversePrioritySchedules;
		}
		else {
			System.out.println("有问题1");
			System.exit(0);
			return null;
		}
			
	}
	
	
	private void updateScheduleInSet(ArrayList<Schedule> schedules,TreeSet<Integer> readyToJoinCarIds,int step) {
		ArrayList<Schedule> isJoinedCarIds = new ArrayList<Schedule>();
		for(Schedule schedule : schedules) {
			
			int realTime = schedule.getRealTime();
			if(realTime == step) {
				
				readyToJoinCarIds.add(schedule.getCarId());
				isJoinedCarIds.add(schedule);
			}
			else if(realTime < step) {
//				System.out.println("滕子京谪守巴陵郡");
//				System.exit(0);
			}
			else {
				break;
			}
		}
	}
	public void  updatReadyToJoinCarIds(int step) {
		updateScheduleInSet(forwardPrioritySchedules, readyToJoinForwardPriorityCarIds, step);
		updateScheduleInSet(reversePrioritySchedules, readyToJoinReversePriorityCarIds, step);		
	}
	
	public ArrayList<Integer> getCarIdInLane(int laneId){
		ArrayList<Integer> carIdInLane = new ArrayList<Integer>();
		if(laneId >number && isTwoWay == 0) {
			//如果没有反向路
			return carIdInLane;
		}
		
		for(int pos = 0;pos<length;pos++) {
			if(map[laneId][pos] != 0) {
				carIdInLane.add(map[laneId][pos]);
			}
		}
		return carIdInLane;
	}
	public TreeSet<Integer> getReadyToJoinForwardPriorityCarIds(){
		return readyToJoinForwardPriorityCarIds;
	}
	public TreeSet<Integer> getReadyToJoinReversePriorityCarIds() {
		return readyToJoinReversePriorityCarIds;
	}
	

	public int getPriorityCarCount() {
		return readyToJoinForwardPriorityCarIds. size() + readyToJoinReversePriorityCarIds.size();
	}
	public Map<String,Integer> getExistCarCount(Map<Integer, Car> carMap,int crossId){
		int normalCarCount = 0;
		int priorityCarCount = 0;
		if(crossId == startCrossId) {
			for(int l = 1;l<number;l++) {
				for(int pos = 0;pos<length;pos++) {
					if(map[l][pos] != 0) {
						if(carMap.get(map[l][pos]).isPriority()) {
							priorityCarCount++;
						}
						else {
							normalCarCount++;
						}
					}
				}
			}
		}
		Map<String,Integer> existCarCountMap = new HashMap<String, Integer>();
		existCarCountMap.put("normalCarCount",normalCarCount);
		existCarCountMap.put("priorityCarCount",priorityCarCount);
		return existCarCountMap;
	}
	
	public ArrayList<Schedule> getForwardPrioritySchedules() {
		return forwardPrioritySchedules;
	}
	public ArrayList<Schedule> getForwardNormalSchedules() {
		return forwardNormalSchedules;
	}
	public ArrayList<Schedule> getReverseNormalSchedules() {
		return reverseNormalSchedules;
	}
	public ArrayList<Schedule> getReversePrioritySchedules() {
		
		return reversePrioritySchedules;
	}
	public ArrayList<Schedule> getNormalSchedules(int crossId) {
		if(crossId == startCrossId) {
			return forwardNormalSchedules;
		}
		else {
			return reverseNormalSchedules;
		}
	}
	public TreeSet<Integer> getReadyToJoinPriorityCarIds(int crossId) {
		if(crossId == endCrossId) {
			return readyToJoinForwardPriorityCarIds;
		}
		else {
			return readyToJoinReversePriorityCarIds;
		}
	}
	public void updateRoadCarScheduleAndSet(Map<Integer, Car> carMap, int step) {
		updateSchedule(forwardPrioritySchedules, carMap, step);
		updateSchedule(reversePrioritySchedules, carMap, step);
		
		updateReadyToJoinCarIds(readyToJoinForwardPriorityCarIds, carMap, step);
		updateReadyToJoinCarIds(readyToJoinReversePriorityCarIds, carMap, step);
		
	}
	private void updateReadyToJoinCarIds(TreeSet<Integer> readyToJoinCarIds, Map<Integer, Car> carMap, int step) {
		ArrayList<Integer> prowlCarIds = new ArrayList<Integer>();
		for(int carId : readyToJoinCarIds) {
			if(carMap.get(carId).isPreset() == true) {
				prowlCarIds.add(carId);
			}
		}
		readyToJoinCarIds.removeAll(prowlCarIds);
	}
	private void updateSchedule(ArrayList<Schedule> schedules, Map<Integer, Car> carMap, int step) {
		ArrayList<Schedule> statSchedule = new ArrayList<Schedule>();
		for(Schedule schedule : schedules) {
			int realTime = schedule.getRealTime();
//			if(realTime<step) {
//				System.out.println("自胡马窥江去后");
//				System.exit(0);
//			}
			if(realTime == step) {
				if(carMap.get(schedule.getCarId()).getFlag() != -1) {
					statSchedule.add(schedule);
				}
			}
			else {
				break;
			}
		}
		schedules.removeAll(statSchedule);
		
	}
	public Set<Integer> getForwardNormalCarSet() {
		return forwardNormalCarSet;
	}
	public Set<Integer> getReverseNormalCarSet() {
		return reverseNormalCarSet;
	}
	@Override
	public int compareTo(Road o) {
		// TODO Auto-generated method stub
		return this.id - o.id;
	}
}

	