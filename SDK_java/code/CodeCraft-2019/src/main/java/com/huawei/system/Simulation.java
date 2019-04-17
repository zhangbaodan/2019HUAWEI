package main.java.com.huawei.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.text.rtf.RTFEditorKit;

import main.java.com.huawei.unit.Car;
import main.java.com.huawei.unit.Cross;

import main.java.com.huawei.unit.PreStep;
import main.java.com.huawei.unit.Road;
import main.java.com.huawei.unit.Schedule;

public class Simulation {
	private int step = 1;  //时间计数
	private int maxSpeedInMap = -1; //刚开始的最小速度

	private Map<Integer,Car> carMap;
	private Map<Integer,Road> roadMap;
	private TreeMap<Integer,Cross> crossMap;


	private boolean underForbidTime = false;  //解死锁期间
	private int forbidEndTime = -1;
	private Set<Integer> finishedCarIds;  //已经完成的车
	private Set<Integer> existCarIds;     //目前路上存在的车
	private Set<Integer> waittingCarIds; //等待处理的车列表，用于处理死循环
	 
	private int sumTime = 0;
	private PathPlanning pathPlanning;   //用于做路径规划的类

	private TreeMap<Integer, ArrayList<Integer>> maxSpeedNormalCarIdsMap;  //尚未发车不同速度下的车辆
	
	private HashSet<Integer> lockCrossIds = new HashSet<Integer>();  //死锁的路口
	private HashSet<Integer> lockCarIds = new HashSet<Integer>(); //死锁的车辆
	private ArrayList<Schedule> priorityCarAllowTime;  //优先级车辆
	
	private TreeSet<Integer> lockTime = new TreeSet<Integer>(); //用于记录发生死锁的时间
	
	private ArrayList<Integer> lockMaxSpeed = new ArrayList<Integer>();  //发生死锁时候,道路上主要行驶车辆的速度
	
	private TreeMap<Integer,Integer> maxSpeedJoinEndTime = new TreeMap<Integer, Integer>(); //每一个速度下最后一辆车加入时间
	
	private Set<Integer> maxSpeeds = new HashSet<Integer>(); //记录一共有多少种车速
	
	private ArrayList<PreStep> preSteps; //记录某个时间下,仿真所有数据,用于回滚
	
	private boolean isTest = false;  //true:纯判题器 false:基于判题器规划车辆路径

	private int priorityCarInRoadCount = 0;
	private Car lastEndPriorityCar;  //最后一辆行驶完的优先级车辆
	private int prowlCount = -1;   //优先且预置的车辆数目
	private int priorityCount = -1; //优先且非预置车辆数目
	private int lastProwlCarEndTime = -1; //最后一辆优先且预置车辆结束时间
	private int lastPriorityCarEndTime = -1; //最后一辆优先且非预置车辆结束时间
	public Simulation(Map<Integer, Car> carMap, Map<Integer, Road> roadMap,
			TreeMap<Integer, Cross> crossMap,TreeMap<Integer, ArrayList<Integer>> maxSpeedNormalCarIdsMap,
			ArrayList<Schedule> priorityCarAllowTime,int prowlCount) {

		this.carMap = carMap;
		this.roadMap = roadMap;
		this.crossMap = crossMap;
		this.maxSpeedNormalCarIdsMap = maxSpeedNormalCarIdsMap;
		this.priorityCarAllowTime = priorityCarAllowTime;
		
		this.maxSpeedInMap = maxSpeedNormalCarIdsMap.firstKey();
		
		this.prowlCount = prowlCount;
		this.priorityCount = priorityCarAllowTime.size();


		existCarIds = new HashSet<Integer>();
		this.pathPlanning = new PathPlanning(crossMap,roadMap);
		waittingCarIds = new HashSet<Integer>();
		preSteps = new ArrayList<PreStep>();
		
		for(int maxSpeed  : maxSpeedNormalCarIdsMap.keySet()) {
			maxSpeeds.add(maxSpeed);
		}
		sumTime = 0;
		Parameter.setParameter(carMap);
		finishedCarIds = new HashSet<Integer>();
		waittingCarIds = new HashSet<Integer>();
		LetsGo();  //运行主入口
		CalculateTime calculateTime = new CalculateTime(this.carMap);
	}
	public Simulation(Map<Integer, Car> carMap, Map<Integer, Road> roadMap,
			TreeMap<Integer, Cross> crossMap) {
		//纯判题器
		isTest = true;
		this.carMap = carMap;
		this.roadMap = roadMap;
		this.crossMap = crossMap;

//		Parameter.setParameter(carMap);
		
		existCarIds = new HashSet<Integer>();
		this.pathPlanning = new PathPlanning(crossMap,roadMap);
		waittingCarIds = new HashSet<Integer>();
		preSteps = new ArrayList<PreStep>();

		sumTime = 0;
		finishedCarIds = new HashSet<Integer>();
		waittingCarIds = new HashSet<Integer>();

		LetsGo();
		
		CalculateTime calculateTime = new CalculateTime(this.carMap);
		
	}
	private void LetsGo() {
		step = 0;			
		while(true) { //暂时终止条件
			step++;
			if(Parameter.stopStep == step && isTest == false) {
				//这个用于调参,便于随时在某个仿真时刻停下来,输出信息
				System.out.println("最大速度加入时间　: "+maxSpeedJoinEndTime);
				System.out.println("目前为止，最后一辆到达终点优先级车辆信息：　" + lastEndPriorityCar);
				System.out.println("最后一辆预置优先级车辆到达时间　：　" + lastProwlCarEndTime);	
				System.out.println("最后一辆非预置优先级车辆到达时间　：　" + lastPriorityCarEndTime);
				System.out.println();
				System.exit(0);
			}
			
			printMessageInEachStep(); //输出每一步的具体信息
			if(isTest == false) {
				//在加入最后一个速度下的车辆时候,每隔50个仿真时刻,重新全图车辆做一次路径规划,因为我们在路径规划时候,主要致力于让车辆均匀分散.
				//这样会让车辆增加运行时间,重新做路径规划可以降低运行时间
				if(maxSpeedNormalCarIdsMap.size() <=1) {
					if(step % 50 == 0) {
						rePlanPath(existCarIds);
					}
				}	
			}			
			if(isTest == false ) {
				//优先级车辆不采用按速度发车,而是能发就发
				if(Parameter.isPresetOnly == false)
					addPriorityCarInRoad();
		
			}
			if(underForbidTime == true) {
				//前面发生死锁，禁止加入车辆		
				if(forbidEndTime <= step) {
					underForbidTime = false; //解除禁令
				}
			}
			 
			driveJustCurrentRoad(); //道路内车辆的标定与驱动
			

			driveCarInitList(true); //修正了一个逻辑问题,对于优先级车辆,能不能加进去,应该先判断,getVali那个是对没有优先级的			

			
			if(!driveCarIndWaitState()) { //驱动所有等待车辆进入终止状态,死锁则退出
				//输出死锁时候相关信息
				printWaittingCarMessages();
				if(isTest == true) {
					System.out.println("死锁");
					System.exit(0);;
				}
				else {
					//回滚处理死锁
					sloveLock();
					continue;
				}
			}
			
			driveCarInitList(false);  //应该没问题
			if(isTest == false)
				updateRoadCarScheduleAndSet();
			
			
			
			if(isFinish()) {
				break;
			}
			//每隔一个blockStep记录一下当前仿真数据,便于死锁时候回滚
			if(isTest == false && step % Parameter.blockStep == 0) {
				if(preSteps.size() < Parameter.blockSize) {
					PreStep preStep = new PreStep(step);
					preStep.setPreStep(carMap, roadMap, crossMap, finishedCarIds, existCarIds, maxSpeedNormalCarIdsMap, priorityCarAllowTime, sumTime);
					preSteps.add(preStep);
				}
				else {
					preSteps.remove(0);
					PreStep preStep = new PreStep(step);
					preStep.setPreStep(carMap, roadMap, crossMap, finishedCarIds, existCarIds, maxSpeedNormalCarIdsMap, priorityCarAllowTime, sumTime);
					preSteps.add(preStep);
				}
				
			}
			
		}
	}
	private void updateRoadCarScheduleAndSet() {
		for(int roadId : roadMap.keySet()) {
			roadMap.get(roadId).updateRoadCarScheduleAndSet(carMap,step);
		}
	}
	private void  addPriorityCarInRoad() {
		//我们让优先级车辆预先选好路
		priorityCarInRoadCount = 0;
		for(int roadId : roadMap.keySet()) {
			priorityCarInRoadCount += roadMap.get(roadId).getPriorityCarCount();
		}		
		int allowCarCount = Parameter.priorityCarCount;
		//这里我们还要减去当前能加，但是还没加进去的，每个路的库存。
		if(existCarIds.size() + priorityCarInRoadCount >= allowCarCount) {
			return;
		}
		int joinCarCount = allowCarCount - existCarIds.size() - priorityCarInRoadCount;
		//先无脑暴力加车
		if(joinCarCount<=0)
			return;
		ArrayList<Schedule> startCarSchdule = new ArrayList<Schedule>();
	
		for(Schedule schedule : priorityCarAllowTime) {
			if(schedule.getRealTime()  > step ) {		
				break;
			}
			else {
				
				int carId = schedule.getCarId();
				Car car = carMap.get(carId);
				if(pathPlanning.reSetRoutes(car, roadMap) ==false) {
					continue;
				}
				int roadId = car.getFirstRoadId();
				Road road = roadMap.get(roadId);
				road.addPriorityCarId(carId, car.getStartCross());
				startCarSchdule.add(schedule);
				joinCarCount--;
			}
			if(joinCarCount <=0) {
				break;
			}
			
		}
		priorityCarAllowTime.removeAll(startCarSchdule);
		
	}

	private void driveJustCurrentRoad() {
		for(int carId : carMap.keySet()) {
			carMap.get(carId).setFatherCar(false);
			carMap.get(carId).setChangedRoad(false);
		}
		
		
		for(int roadId : roadMap.keySet()) {
			//如果没有反向车道,那么反向车道的车辆列表里返回的是空,这和判题器逻辑有点差异
			//遍历时候是从前向后遍历,但不是一个车道一个车道遍历的,其结果应该不影响,我还是改了吧
			Road road = roadMap.get(roadId);
			
			//道路内信息处理
			if(isTest == false)
				road.updatReadyToJoinCarIds(step);
		
			
			int startCrossId = road.getStartCrossId();
			int endCrossId = road.getEndCrossId();	
			
			updateInRoadCars(roadId,endCrossId);
			updateInRoadCars(roadId,startCrossId);
			
		}
	}

	private void driveCarInitList(boolean isPriority) {
		//只上路优先级车辆,因为我们是基于判题器的动态调整,所以逻辑和他不一样
		if(isTest == true) { //纯判题器
			if(isPriority == true) {//只上路优先车辆
				for(int roadId : roadMap.keySet()) {
					Road road = roadMap.get(roadId);
					ArrayList<Schedule> forwardPrioritySchedules = road.getForwardPrioritySchedules();
					ArrayList<Schedule> reversePrioritySchedules = road.getReversePrioritySchedules();
					
					runCarInInitList(forwardPrioritySchedules);
					runCarInInitList(reversePrioritySchedules);
				}
			}
			else { //所有车辆都可以上路,但优先车辆优先上路
				//混合上路顺序为:  优先级,时间,车号
				for(int roadId : roadMap.keySet()) {
					Road road = roadMap.get(roadId);
					
					ArrayList<Schedule> forwardNormalSchedules = road.getForwardNormalSchedules();
					ArrayList<Schedule> reverseNormalSchedules = road.getReverseNormalSchedules();
					ArrayList<Schedule> forwardPrioritySchedules = road.getForwardPrioritySchedules();
					ArrayList<Schedule> reversePrioritySchedules = road.getReversePrioritySchedules();
					
					//先加优先级车辆
					runCarInInitList(forwardPrioritySchedules);
					runCarInInitList(reversePrioritySchedules);	
					//再加正常车辆
					runCarInInitList(forwardNormalSchedules);
					runCarInInitList(reverseNormalSchedules);
				}
			}
		}
		else //基于判题器的动态调整
			if(isPriority == true) {//只上路优先车辆
				for(int roadId : roadMap.keySet()) {
					Road road = roadMap.get(roadId);
				
					//先加优先级车辆
					joinPriorityCars(road,road.getStartCrossId());
					joinPriorityCars(road,road.getEndCrossId());
				}
			}
			else { //所有车辆都可以上路,但优先车辆优先上路
				//混合上路顺序为:  优先级,时间,车号
				if(step<Parameter.normalCarStartTime) {
					//普通车辆不允许上路
					for(int roadId : roadMap.keySet()) {
						Road road = roadMap.get(roadId);	
								
						ArrayList<Schedule> forwardNormalSchedules = road.getForwardNormalSchedules();
						ArrayList<Schedule> reverseNormalSchedules = road.getReverseNormalSchedules();
												
						joinPriorityCars(road,road.getStartCrossId());
						joinPriorityCars(road,road.getEndCrossId());
						
						//再加正常车辆
						runCarInInitList(forwardNormalSchedules);
						runCarInInitList(reverseNormalSchedules);
					}
				}
				else {
					//可以上路普通车辆,在上路普通车辆时候,一定要注意和预置车辆的上路顺序
					//这里每次在上路一辆普通车辆时候,都要检测对于road下是否有未上路的id小的预置车辆
					//如果有的话,把预置车辆上路完,再上路普通车辆
					//在普通车辆上路完以后,再把road下预置车辆上路(这时候预置车辆id一定大于普通车辆)
					for(int roadId : roadMap.keySet()) {
						Road road = roadMap.get(roadId);	
												
						joinPriorityCars(road,road.getStartCrossId());
						joinPriorityCars(road,road.getEndCrossId());
						
					}
					if(Parameter.isPresetOnly == false)
					updateNormalCarsBySpeed(maxSpeedNormalCarIdsMap);
					
					for(int roadId : roadMap.keySet()) {
						Road road = roadMap.get(roadId);
						ArrayList<Schedule> forwardNormalSchedules = road.getForwardNormalSchedules();
						ArrayList<Schedule> reverseNormalSchedules = road.getReverseNormalSchedules();
						runCarInInitList(forwardNormalSchedules);
						runCarInInitList(reverseNormalSchedules);
					}
				}
			}
			
	}
	private void joinPriorityCars(Road road,int crossId) {
		ArrayList<Schedule> schedules = road.getPrioritySchedules(crossId);
		TreeSet<Integer> readyToJoinPriorityCarIds = null;
		if(crossId == road.getStartCrossId())
			readyToJoinPriorityCarIds = road.getReadyToJoinForwardPriorityCarIds();
		else
			readyToJoinPriorityCarIds = road.getReadyToJoinReversePriorityCarIds();
		//优先级车辆不受到当前地图总车辆影响
		ArrayList<Integer> startCarIds = new ArrayList<Integer>();
		ArrayList<Schedule> startSchedules = new ArrayList<Schedule>();

		//先把历史上没有加进去的巡逻车加进去
		for(Schedule schedule : schedules) {
			if(schedule.getRealTime() < step) {
				
				if(addNewCar(carMap.get(schedule.getCarId()))) {
					startSchedules.add(schedule);
				}	
			}
			else {
				break;
			}
		}		
		schedules.removeAll(startSchedules);	
		for(int carId : readyToJoinPriorityCarIds) {
			Car car = carMap.get(carId);
			if(addNewCar(car)) {
				
				startCarIds.add(carId);
			}
		}
		readyToJoinPriorityCarIds.removeAll(startCarIds);
		return ;
				
	}
	private boolean driveCarIndWaitState() {
		int preWaittingCount = waittingCarIds.size();

		while(waittingCarIds.size()>0) {
			//TreeMap保证顺序递增
			for(int crossId : crossMap.keySet()) {
				Cross cross = crossMap.get(crossId);
				ArrayList<Integer> roadIds = cross.getRoadIds();
				Collections.sort(roadIds);		
				for(int roadId : roadIds) {
					if(roadId == -1)
						continue;
					Road road = roadMap.get(roadId);
					while(true) {
						
						int carId = road.getFirstPriorityCarId(carMap,crossId);
						Car car = carMap.get(carId); 
					
						if(carId == -1) {
							break;
						}
						int laneId =  car.getLaneId();
						car.setFatherCar(true);
						
						if(conflict(car,car.isPriority())==true) {

							break;
						}
						if(moveToNextRoad(car)) {

							if(car.isPassRoad() == true)
								driveAllCarJustOnRoadToEndState(road, laneId, crossId, true);
							else {
								driveAllCarJustOnRoadToEndState(road, laneId, crossId, false);
							}								
							if(isTest) {
								if(crossId == road.getEndCrossId()) {
									ArrayList<Schedule> prioritySchedules = road.getForwardPrioritySchedules();
									runCarInInitList(prioritySchedules);
								}
								else {
									ArrayList<Schedule> prioritySchedules = road.getReversePrioritySchedules();
									runCarInInitList(prioritySchedules);
								}

							}
							else {
								//先加优先级车辆
								if(crossId == road.getStartCrossId())
									joinPriorityCars(road,road.getEndCrossId());
								else {
									joinPriorityCars(road,road.getStartCrossId());
								}
							}

						}						
						else
							break;
						
					}
				}
			}
			
			if(preWaittingCount == waittingCarIds.size()) { 	
				return false;//发生死锁
			}
			else if(preWaittingCount > waittingCarIds.size()) 	
				preWaittingCount = waittingCarIds.size();//等待个体有所缓解
			else {
				System.out.println("are you ok?");
				System.exit(0);
			}
		}
		return true;
	}
	private boolean moveToNextRoad(Car car) {
		Road tempRoad = roadMap.get(car.getRoadId());
		Road nextRoad = roadMap.get(car.getNextRoute());
		int crossId = car.getNextCross();
		int carId = car.getId();
		int oldPosition = car.getPosition();
		int oldLaneId = car.getLaneId();
		//到达终点
		if(car.isArrived(tempRoad.getMaxSpeed())) {
			car.setFlag(3);	
			waittingCarIds.remove(carId);
			finishedCarIds.add(carId);
			sumTime += step-car.getAllowStartTime();
			car.setPosition(-1);
			car.setPassRoad(true);
			
			car.setEndTime(step);
			if(car.isPriority() == true) {			
				lastEndPriorityCar = car;
				if(car.isPreset()) {
					prowlCount--;
					if(prowlCount == 0) {
						lastProwlCarEndTime = step;
						if(Parameter.isPresetOnly == true) {
							System.out.println("最后一辆预置优先级车到达终点时间为　：　"+ step );
							System.exit(0);
						}
					}	
				}			
				else {
					priorityCount--;
					if(priorityCount == 0) {
						lastPriorityCarEndTime = step;
					}
				}
			}
			existCarIds.remove(car.getId());
			tempRoad.updatePos(carId, oldLaneId, oldPosition, -1);
			return true;
		}
	
		int s2 = calS2(car,tempRoad,nextRoad);
		
		boolean isNextRoadFull = isRoadFull(nextRoad,crossId);
		//下一条道路满(全是终止状态)或者下一条道路行驶距离为0
		if(isNextRoadFull || s2 ==0 ) {
			//停留在当前道路最前端
			tempRoad.updatePos(carId, oldLaneId, oldPosition, 0); //和下面一行代码不能反
			car.setPosition(0);
			car.setFlag(1);
			waittingCarIds.remove(carId);
			return true;
		}
		else {//下一条路可以进入
			//没有阻挡
			
			//如果当前道路是满的,但是最后一个车是等待状态,也算可以进入

			int nextLaneId = nextRoad.getMightWaitViableLaneId(carMap,crossId);
			int tempPos = nextRoad.getLength() - s2;
			if(nextLaneId == -1) {
				
				nextRoad.printMap();
				System.out.println("厉害了~");
				System.exit(0);
			}
			boolean isBlockAndWait = checkBlockAndWait(car, nextRoad, nextLaneId, tempPos);
		
			if(isBlockAndWait == false) {
				//没有阻挡或者有阻挡,但是阻挡车辆是终止状态

				int aheadCarId = nextRoad.getLastCarId(nextLaneId);
				
				int realPos;
				if(aheadCarId == -1) {
					realPos = tempPos;
				} 
				else {
					int maxPos = carMap.get(aheadCarId).getPosition() + 1; 
					realPos = Math.max(tempPos, maxPos);	
				}
				int nextCross = nextRoad.getNextCross(nextLaneId);
				car.updateInNewRoad(nextRoad.getId(), nextLaneId, realPos, nextCross);

				//更新旧道路
				tempRoad.updatePos(carId, oldLaneId, oldPosition, -1);
				//更新新道路
				nextRoad.updatePos(carId, nextLaneId, -1, realPos);
				car.setLaneId(nextLaneId);
				car.setPosition(realPos);
				car.setPassRoad(true);
				if(isTest == false)
					nextRoad.isArrived(carId,crossId,car.isPriority());
//				tempRoad.printMap();
//				nextRoad.printMap();
				car.setFlag(1);
				waittingCarIds.remove(carId);
				return true;
			}
			else {
				//有阻挡,且阻挡车辆为等待车辆
				return false;
			}	
		}
	}
	private int calS2(Car car, Road tempRoad, Road nextRoad) {
		
		int v1,v2,s1,s2,sv1,sv2;
		car.setFatherCar(true);
		
		v1 = Math.min(tempRoad.getMaxSpeed(), car.getMaxSpeed());
		v2 = Math.min(nextRoad.getMaxSpeed(),car.getMaxSpeed());
		sv1 = v1;
		sv2 = v2;
		s1 = car.getPosition();

		if(s1 >= sv1) {
			System.out.println("s1 : "+s1 + "  sv1 :" + sv1  );
			System.out.println("是不是出问题了.....");
			System.exit(0);
		}
		int temp = sv2 - s1;
	
		if(temp <= 0) { //仍然在之间道路上面的最前面，doublePass成功
			s2 = 0;
		}
		else {
			s2 = temp;
		}
		return s2;
	}
	private boolean isRoadFull(Road road,int crossId) {
		//下一条道路全满(全是终止状态)
		
		int number = road.getNumber();
		int startLane = 1;
		int endLane = number;
		if(crossId == road.getEndCrossId()) { //反向进入
			startLane = number+1;
			endLane = 2*number;
		}
		for(int l = startLane; l<= endLane; l++) {
			int carId = road.getLastPosCarId(l);
			if(carId == -1) { 
				return false;
			}
			else {
				if(carMap.get(carId).getFlag() == 2) { 
					return false;
				}
			}
		}
		//所有车道最后一个位子都是满的,并且所有状态都是终止状态
		return true;
	}
	private void runCarInInitList(ArrayList<Schedule> schedules) {
		ArrayList<Schedule> startSchedules = new ArrayList<Schedule>();
		
		for(Schedule schedule : schedules) {

			if(step < schedule.getRealTime()) {
				break;
			}
			if(addNewCar(carMap.get(schedule.getCarId()))) {
				
				startSchedules.add(schedule);
			}

		}
		schedules.removeAll(startSchedules);		
	}
	
	
	private void updateInRoadCars(int roadId,int crossId) {
		Road road = roadMap.get(roadId);
		int roadMapSpeed = road.getMaxSpeed();
		int number = road.getNumber();
		int startLaneId = 1;
		int endLaneId = number;
		if(crossId == road.getStartCrossId()) {
			startLaneId = number+1;
			endLaneId = number + number; 
		}
		for(int l = startLaneId;l<=endLaneId;l++) {
			ArrayList<Integer> carIdInLane = road.getCarIdInLane(l);
			for(int carId :  carIdInLane) {
				Car car = carMap.get(carId);
				
				int flag = car.getFlag();
				if(flag == -1) {//还没加入或者已经完成的
					System.out.println("秋名山老司机");
					
					System.exit(0);
				}
				//前方没有车辆
				if(road.getAheadCarId(carId, car.getLaneId(), car.getPosition())==-1) {
					//前方没有车辆阻挡,可以出路口 --------------a
					if(car.mightPassCross(roadMapSpeed)) {
						car.setFlag(2);
						waittingCarIds.add(carId);
					}
					//前方没有阻挡也不会出路口--------------------b
					else {
						int realSpeed = Math.min(car.getMaxSpeed(), roadMapSpeed);
						int oldPos = car.getPosition();
						int laneId = car.getLaneId();	
						int realPos = car.getPosition() - realSpeed;
						
						car.setPosition(realPos);
						road.updatePos(carId, laneId, oldPos, realPos);
						car.setFlag(1);

					}
				}
				else { //前方有车辆
				    //阻挡
					int realSpeed = Math.min(car.getMaxSpeed(), roadMapSpeed);
					int aheadId =  road.getAheadCarId(carId, car.getLaneId(), car.getPosition());
					int aheadPos = carMap.get(aheadId).getPosition();
					int carPos = car.getPosition();
					int s = carPos - aheadPos-1;
					if(s < realSpeed) { //前方有车辆阻挡
						if(carMap.get(aheadId).getFlag() == 2) { //前方有车辆阻挡，且阻挡车辆为等待车辆
							car.setFlag(2);
							waittingCarIds.add(carId);
						}
						else { //前放有车辆阻挡，且阻挡车辆为终止车辆
							int realPos = aheadPos+1;
							car.setPosition(aheadPos+1); //这里更新当前车辆的位置

							road.updatePos(carId,car.getLaneId(),carPos,realPos); //更新车辆在当前路的位置		
							car.setFlag(1);
						}
					}
					else //不阻挡 ---------------------------b
					{
						int realPos = car.getPosition() - realSpeed;
						int oldPos = car.getPosition();
						int laneId = car.getLaneId();
						car.setPosition(realPos);
						road.updatePos(carId, laneId, oldPos, realPos);
						car.setFlag(1);
					}
				}		
			}	
			
			
		}
		
		
		
		
	}
	private void driveAllCarJustOnRoadToEndState(Road road, int laneId,int crossId,boolean isRunInRoad) {

		ArrayList<Integer> beforeCarIds = road.getBeforeCarIds(laneId, crossId,0);
		int roadMaxSpeed = road.getMaxSpeed();
		for(int beforeCarId : beforeCarIds) {
			Car car = carMap.get(beforeCarId);
			
			if((car.getFlag() == 2) ){

				int aheadCarId = road.getAheadCarId(car.getId(), laneId, car.getPosition());
				if(aheadCarId == -1) {
					if(car.mightPassCross(roadMaxSpeed) == false) {
						//可以走
						int realSpeed = Math.min(car.getMaxSpeed(), roadMaxSpeed);
						int oldPos = car.getPosition();
						int tempPos = car.getPosition() - realSpeed;
						int realPos = tempPos;
						car.setPosition(realPos);
						road.updatePos(car.getId(), laneId, oldPos, realPos);
						waittingCarIds.remove(car.getId());
						car.setFlag(1);
					}
					else{
						return;
					}
				}
				else {
					//前面有车
					
					int realSpeed = Math.min(car.getMaxSpeed(), roadMaxSpeed);
					int oldPos = car.getPosition();
					int tempPos = car.getPosition() - realSpeed;
					int maxPos = carMap.get(aheadCarId).getPosition()+1;
					int realPos = Math.max(tempPos, maxPos) ;


					road.updatePos(car.getId(), laneId, oldPos, realPos);
					car.setPosition(realPos);
					waittingCarIds.remove(car.getId());
					car.setFlag(1);
					
				}
				
			}
		}
	}
	private boolean conflict(Car car,boolean isPriority) {
		
		int crossId = car.getNextCross();
		int nextRoadId = car.getNextRoute();
							
		int tempRoadId = car.getRoadId();
		
		Cross cross = crossMap.get(crossId);
		
		
		int priority = cross.getPassPriority(tempRoadId, nextRoadId);
		
		if(isPriority == true) {
			//优先级车辆,只和优先级车辆有冲突
			if(priority == 1) { //左转
				int strightRoadId = cross.getStrightRoadId(nextRoadId);
				if(updateFatherRoad(strightRoadId,0,crossId,isPriority,true)==2) {
					return true;
				}	
			}	
			else if(priority == 2) { //右转
	
				int strightRoadId = cross.getStrightRoadId(nextRoadId);
				int leftRoadId = cross.getLeftRoadId(nextRoadId);
				if(updateFatherRoad(leftRoadId,1,crossId,isPriority,true)==2 || 
						updateFatherRoad(strightRoadId,0,crossId,isPriority,true)==2) {
					return true;
				}		
			}
			else {
				return false;
			}
		}
		else {
			
			if(priority == 0) {
				if(nextRoadId == -1) {
					//到达终点车辆
					nextRoadId = cross .getArivedCarStrightRoadId(tempRoadId);
					if(nextRoadId == -1) {
						return false;
					}
					
				}
				
				int strightRoadId = cross.getStrightRoadId(nextRoadId);
				int leftRoadId = cross.getLeftRoadId(nextRoadId);
				int rightRoadId = cross.getRightRoadId(nextRoadId);
				//先考虑优先级车辆
				if(updateFatherRoad(rightRoadId, 2, crossId, isPriority, true) == 2||
					updateFatherRoad(leftRoadId,1,crossId,isPriority,true) == 2 || 
						updateFatherRoad(strightRoadId,0,crossId,isPriority,true) == 2) {
					return true;
				}
			}
			else if(priority == 1) {
				int strightRoadId = cross.getStrightRoadId(nextRoadId);
				int leftRoadId = cross.getLeftRoadId(nextRoadId);
				int rightRoadId = cross.getRightRoadId(nextRoadId);
				if(updateFatherRoad(rightRoadId, 2, crossId, isPriority, true) == 2||
					updateFatherRoad(leftRoadId,1,crossId,isPriority,true)==2 || 
						updateFatherRoad(strightRoadId,0,crossId,isPriority,false)==2) {
					return true;
				}
				
				
			}
			else if(priority == 2) {
				int strightRoadId = cross.getStrightRoadId(nextRoadId);
				int leftRoadId = cross.getLeftRoadId(nextRoadId);
				int rightRoadId = cross.getRightRoadId(nextRoadId);
				if(updateFatherRoad(rightRoadId, 2, crossId, isPriority, true) == 2||
					updateFatherRoad(leftRoadId,1,crossId,isPriority,false)==2 || 
						updateFatherRoad(strightRoadId,0,crossId,isPriority,false)==2) {
					return true;
				}
			}
			
			return false;
		}
		return false;
		
		
	}
	private void sloveLock() {
		//比如在100秒时候发生死锁,记录死锁车辆,以及死锁cross(以及cross周围的cross)
		//我们可以退回到90秒,在90-100秒期间为forbidTime,期间不允许死锁车辆以及死锁cross发车.
		//这样一般在100秒时候就解锁了,如果没解锁,那么退回到80秒,将80-100秒记为forbidTime
		//这里退回时间间隔以及保存的时间片数量都可以在Parameter中设置
		System.out.println("死锁了.................................................");
		//记录死锁时间以及对应速度
		if(!lockTime.contains(step)) {
			lockTime.add(step);
			lockMaxSpeed.add(maxSpeedInMap);
		}		
		System.out.println("当前保存的时间片数量： " + preSteps.size());
		
		
		int index = preSteps.size() - 1;
		
		if(index <= 0 ) {
			System.out.println("已经解过锁 没解开！");
			System.out.println("死锁发生时间 ： " + lockTime);
			System.out.println("当前地图速度为： "+ maxSpeedInMap);
			System.exit(0);
		}
		
		setLockCrossAndCarIds();
		
		
		
		int preStep = preSteps.get(index).getStep();
		
		carMap = preSteps.get(index).getCarMap();
		
		roadMap = preSteps.get(index).getRoadMap();
		crossMap = preSteps.get(index).getCrossMap();
	

		finishedCarIds = preSteps.get(index).getFinishedCarIds();
		existCarIds = preSteps.get(index).getExistCarIds();
		

		waittingCarIds = preSteps.get(index).getWaittingCarIds();
		maxSpeedNormalCarIdsMap = preSteps.get(index).getMaxSpeedNormalCarIdsMap();
		
		priorityCarAllowTime = preSteps.get(index).getPriorityCarAllowTime();
		
		
		sumTime = preSteps.get(index).getSumTime();		
				
 		underForbidTime = true;
		forbidEndTime = step;
		step = preStep;
		System.out.println("时间回到 ： " + step);
		
		
		preSteps.remove(index);
		
		
	}
	

	private void rePlanPath(Set<Integer> carIds) {
		//在进行二次路径规划时候,我们只能规划下下条路以及以后的路
		//因为下一条路往往会在上一个仿真时刻参与优先级排序以及冲突检测,所以不能改变下一条路
		for(int carId : carIds) {
			Car car = carMap.get(carId);
			car.setChangedRoad(true);
			if(car.getFlag() == -1 || car.getFlag() == 3) {
				continue;
			}
			if(car.isPreset()) {
				continue;
			}

			int nextRoad = car.getNextRoute();
			if(nextRoad == -1) {
				
				continue;
			}	
	
			//我们只改变下下条路以后的路
			
			int cross = car.getNextCross();
			if(cross == car.getEndCross())
				continue;
			ArrayList<Integer> routes = car.getRoutes();

				if(routes.size() <=2) {
					continue;
				}
					
				ArrayList<Integer> beforeRoutes = new ArrayList<Integer>();
				for(int id : routes) {
					beforeRoutes.add(id);
				}

			
				int nextRoadId = routes.get(0);		
				int nnCrossId = roadMap.get(nextRoadId).getNeighborCross(car.getNextCross());
				int crossId = nnCrossId;
				for(int i = 1;i<routes.size();i++) {

					int roadId = routes.get(i);
//					System.out.println(roadId);
					roadMap.get(roadId).isArrived(car.getId(),crossId,car.isPriority());
					crossId = roadMap.get(roadId).getNeighborCross(crossId);
				}
 
				pathPlanning.reSetRoutes(car, nnCrossId,roadMap,car.getNextCross(),step,nextRoadId);
				
				
				car.setPlanPathTime(step);

				
				
				
				car.resetAllRoutes(nextRoadId);
									
				routes = car.getRoutes();
				
				crossId = car.getNextCross();
				
				for(int roadId : routes) {
					roadMap.get(roadId).addCar(carId,crossId,car.isPriority());
					crossId = roadMap.get(roadId).getNeighborCross(crossId);
				}
			}
		
		
	}
	private int updateFatherRoad(int roadId,int priority,int crossId,boolean isPriority,boolean carMustBePriorityCar) {
		if(roadId != -1) {
			int firstPriorityCarid = roadMap.get(roadId).getFirstPriorityCarId(carMap, crossId);
			if(firstPriorityCarid == -1) {
				//当前车道没有优先级车辆
				return 1;
			}		
			
			Car car = carMap.get(firstPriorityCarid);
			car.setFatherCar(true);
			if(carMap.get(firstPriorityCarid).getFlag() == 1){
				System.out.println("what?");
				System.exit(0);	
				return 1;
			}
			else {

				boolean carIsPriority = car.isPriority();
				
				if(carMustBePriorityCar == true && carIsPriority == false) {
					return 1;
				}
					
				//当前车辆是优先级车辆,考虑车辆是非优先级车辆
				if(carIsPriority == false && isPriority == true) {
					return 1;
				}
				
				int tempRoadId = car.getRoadId();
				Cross cross = crossMap.get(crossId);
				int nextRoadId = car.getNextRoute();
				if(nextRoadId == -1) { //这个人要到达终点了
					
					if(priority == 0) {
						car.setFatherCar(true);
						if(carIsPriority == true)
							return 2;
						else if(carIsPriority == false && isPriority == true) {
							return 1;
						}
						else if(carIsPriority == false && isPriority == false) {
							return 2;
						}
					}
					else {
						car.setFatherCar(true);
						return 1;
					}
				}

				int tempPriority = cross.getPassPriority(tempRoadId, nextRoadId);
				if(tempPriority == priority) { //等待 但是和他走一条道
					car.setFatherCar(true);
					if(carIsPriority == true)
						return 2;
					else if(carIsPriority == false && isPriority == true) {
						return 1;
					}
					else if(carIsPriority == false && isPriority == false) {
						return 2;
					}
					else {
						System.out.println("&");
						System.exit(0);
						return 1;
					}
				}
				else {
				
					car.setFatherCar(true);
					return 1;
				}
			}
				
		}
		else {
			return 1;
		}

	
	}
	private ArrayList<Integer>  joinReadyCarsBySpeed(ArrayList<Integer> carIds,int joinCarCount) {
		Collections.sort(carIds); //应该是从小到大,这里可以优化,改成treeset
		ArrayList<Integer> startCar = new ArrayList<Integer>();
		for(int carId : carIds) {
			if(carId == 47356 && step>500) {
				System.out.println(step);
				System.out.println(carMap.get(carId));
				
			}
			Car car = carMap.get(carId);
			if(car.getFlag() != -1) {
				continue;
//				System.out.println(car);
////				System.exit(0);;
			}
			if(car.getAllowStartTime() <= step) {
				if(addNewCar(car) == true) {
					if(car.isPriority() == false)
						maxSpeedInMap = car.getMaxSpeed();
					startCar.add(carId);
					joinCarCount--;
				}
			}
			if(joinCarCount<=0) {
				return startCar;
			}
				
		}
		return startCar;
	}
	
	private void updateNormalCarsBySpeed(TreeMap<Integer, ArrayList<Integer>> maxSpeedCarIdsMap) {

		int allowCarCount;


		allowCarCount = Parameter.maxSpeedParameter.get(maxSpeedInMap).allowCarCount;			


		//在严打期间,降低增加人数
		if(underForbidTime) {
			allowCarCount = (int) (Parameter.forbidTimeAllowCarCountRate * 
					Parameter.maxSpeedParameter.get(maxSpeedInMap).allowCarCount);
		}
		
		
		if(existCarIds.size() > allowCarCount) {
			return;
		}
		int joinCarCount = allowCarCount - existCarIds.size();
		//先无脑暴力加车
		if(joinCarCount<=0)
			return;
			if(maxSpeedCarIdsMap.size() == 0||Parameter.startSpeed.size() == 0) {
				return;
			}			
//			int maxSpeed = maxSpeedCarIdsMap.firstKey();
			int maxSpeed = -1;
			for(int speed : Parameter.startSpeed) {
				if(maxSpeedCarIdsMap.containsKey(speed)) {
					maxSpeed = speed;
					break;
				}
			}
			
			ArrayList<Integer> carIds =  maxSpeedCarIdsMap.get(maxSpeed);
			
			ArrayList<Integer> startCarIds = null;
		
		
			startCarIds = joinReadyCarsBySpeed(carIds, joinCarCount);
				
			joinCarCount -= startCarIds.size();
				
			maxSpeedCarIdsMap.get(maxSpeed).removeAll(startCarIds);
			
			if(maxSpeedCarIdsMap.get(maxSpeed).size()== 0) {
				System.out.println("---------------------速度　" +  maxSpeed + "  已经全部加入-----------------------------");
				maxSpeedJoinEndTime.put(maxSpeed,step);
				maxSpeedCarIdsMap.remove(maxSpeed);
				maxSpeeds.remove(maxSpeed);
				
				if(maxSpeedCarIdsMap.size() != 0)
					maxSpeedInMap = maxSpeedCarIdsMap.firstKey();
			}
			else if( maxSpeedCarIdsMap.get(maxSpeed).size() <= Parameter.joinToNextSpeedRate &&
					maxSpeedCarIdsMap.size() >=2) {
				ArrayList<Integer> leftCarIds = maxSpeedCarIdsMap.get(maxSpeed);
				int nextMaxSpeed = -1;
				for(int i = 0;i<Parameter.startSpeed.size();i++) {
					if(maxSpeed == Parameter.startSpeed.get(i)) {
						nextMaxSpeed = Parameter.startSpeed.get(i+1);
					}
				}
				maxSpeedCarIdsMap.get(nextMaxSpeed).addAll(leftCarIds);
				maxSpeedJoinEndTime.put(maxSpeed,step);
				maxSpeedCarIdsMap.remove(maxSpeed);
				maxSpeeds.remove(maxSpeed);
				
				if(maxSpeedCarIdsMap.size() != 0)
					maxSpeedInMap = maxSpeedCarIdsMap.firstKey();
				
				
			}
			return;
		
	}

	private void addLowIdCar(int carId, int roadId, int crossId) {

		ArrayList<Schedule> normalCarSchedule = roadMap.get(roadId).getNormalSchedules(crossId);
		
		ArrayList<Schedule> startCarIds = new ArrayList<Schedule>();
		for(Schedule schedule : normalCarSchedule) {
			int id = schedule.getCarId();
			Car car = carMap.get(schedule.getCarId());
			if(car.getRealTime() < step|| (car.getRealTime() == step && id < carId)) {
				if(addNewCar(car) == false) {
					break;
				}
				else {
					startCarIds.add(schedule);
				}
			}
			else {
				break;
			}
		}
		normalCarSchedule.removeAll(startCarIds);

	}
	
	private boolean addNewCar(Car car) {
		if(car.isPreset() == false && car.isPriority() == false) {
			if(underForbidTime == true) {
				//前面发生死锁，禁止加入车辆		
				if(forbidEndTime > step) {
					if(lockCrossIds.contains(car.getStartCross()) || lockCarIds.contains(car.getId()))
						return false;		 
				}
				else {
					underForbidTime = false; //解除禁令

				}
			}
		}

		if(car.isPreset() == false && car.isPriority() == false && isTest == false) {
			if(pathPlanning.reSetRoutes(car, roadMap) == false)
				return false;
		}
		int roadId = car.getNextRoute();
	
		Road road = roadMap.get(roadId);

		int laneId = -1;
		if(isTest == false && car.isPreset() == false &&car.isPriority() == false) {
			addLowIdCar(car.getId(), roadId, car.getStartCross());
		}
		
		
		if(car.isPriority() == true) {
			laneId = road.getMightWaitViableLaneId(carMap, car.getStartCross());
			//加不进去,没有空位,而且都是满的
			if(laneId == -1) {
				return false;
			}				
			int realMaxSpeed = Math.min(car.getMaxSpeed(), road.getMaxSpeed());
			int tempPos = road.getLength() - realMaxSpeed;
			if(checkBlockAndWait(car,road,laneId,tempPos) == true) {
				return false;
			}
		}
		else {
			laneId = road.getViableLaneId(car.getStartCross()); //获取新车应该进入的车道
		}
		if(laneId == -1) { //如果没路 或者道路太拥挤
			return false;
		}	
		car.setAllRoutes();
		int carId = car.getId();
		
		
		
		if(car.isPreset()==false || isTest == true) {
			ArrayList<Integer> routes = car.getRoutes();
			int startCrossId = car.getStartCross();

			for(int id : routes) {
				roadMap.get(id).addCar(carId,startCrossId,car.isPriority());
				startCrossId = roadMap.get(id).getNeighborCross(startCrossId); 
			}	
		}
	
		
		car.setRoadId(roadId); //这里确保可以加入道路,不然里面会删除一些道路
		
		car.setPlanPathTime(step);
		
		if(car.isPreset() == false)
			car.setRealTime(step);
		
		int maxRoadSpeed = road.getMaxSpeed();
		int maxCarSpeed = car.getMaxSpeed();
		int realSpeed = Math.min(maxCarSpeed, maxRoadSpeed);
		
		car.setLaneId(laneId);
		int maxPos = road.getMaxPos(laneId); //其可能达到的最大位置，也就是距离路口距离最小
		if(maxPos == -1) {
			System.exit(0);
		}
		int tempPos = road.getLength() - realSpeed;
		int realPos = Math.max(maxPos, tempPos);
		
		car.setPosition(realPos); //这里更新当前车辆的位置
		road.updatePos(car.getId(),laneId,-1,realPos);	
	
		
		if(isTest == false)
			road.isArrived(car.getId(),car.getStartCross(),car.isPriority()); //从未到达车辆列表中删除F
	
		car.setFlag(1);
		
		car.setNextCross(road.getNextCross(laneId));
		existCarIds.add(car.getId());
		return true;

	}
	private boolean checkBlockAndWait(Car car ,Road road,int lane,int realPos) {
		int lastCarId = road.getLastCarId(lane);
		if(lastCarId == -1) {
			return false;
		}
		Car lastCar = carMap.get(lastCarId);
		int flag = lastCar.getFlag();
		if(flag == 1) {
			return false;
		}
		else if(flag == 2) {
			
			int lastPos = lastCar.getPosition();
			if(realPos <= lastPos) {
				return true;
			}
			else {
				return false;
			}
				
		}
		else {
			System.out.println("沧海月明珠有泪");
			System.exit(0);
			return true;
		}
		
	}
	
	private boolean isFinish() {
		if(finishedCarIds.size() == carMap.size()) {
			System.out.println("***********************************");
			System.out.println("    所有车辆均到达目的地        ");
			System.out.println("    总时间：" + (step) + "  carSumTime: "+sumTime);
			System.out.println("    解锁次数： " + lockTime.size());
			System.out.println("    解锁时间 : " + lockTime);
			System.out.println("    解锁速度 : " + lockMaxSpeed);
			System.out.println("    最大速度加入结束时间 ： "+ maxSpeedJoinEndTime);
			System.out.println("***********************************");
			return true;
		}
		return false; 
	}
	private void setLockCrossAndCarIds() {
		lockCarIds = new HashSet<Integer>();
		lockCrossIds = new HashSet<Integer>();
		for(int carId : waittingCarIds) {
			Car car = carMap.get(carId);
			lockCarIds.add(carId);
			lockCrossIds.add(car.getNextCross());
		}
		
		//对cross进行二次扩展
		lockCrossIds = extendCrossIds(lockCrossIds);

	}
	private HashSet<Integer> extendCrossIds(HashSet<Integer> crossIds) {
		HashSet<Integer> extendCrossIds = new HashSet<Integer>();
		for(int crossId : crossIds) {
			 Cross cross = crossMap.get(crossId);
			 extendCrossIds.add(crossId);
			 ArrayList<Integer> neighborCrossIds = cross.getNeighborCrossIds();
			 for(int id : neighborCrossIds) {
				 extendCrossIds.add(id);
			 }
		}
	
		return extendCrossIds;
	}
	
	
	
	private void printWaittingCarMessages() {
		
		
		Set<Integer> crossIds = new HashSet<Integer>(); //死锁相关路口
		
		Set<Integer> normalCarIds = new TreeSet<Integer>(); //非优先,非预置车辆的普通车辆
		
		for(int carId : waittingCarIds) {
			Car car = carMap.get(carId);
		
			if(car.isPreset() == false && car.isPriority() == false) {
				normalCarIds.add(carId);
			}
		
			crossIds.add(car.getNextCross());
			
		}
		//死锁的普通车辆在对应速度下的个数,便于调参
		Map<Integer, Integer> maxSpeedCarIds = new HashMap<Integer, Integer>();
		
		for(int carId : normalCarIds) {
			int maxSpeed = carMap.get(carId).getMaxSpeed();
			if(!maxSpeedCarIds.containsKey(maxSpeed)) {
				maxSpeedCarIds.put(maxSpeed,0);
			}
			maxSpeedCarIds.put(maxSpeed, maxSpeedCarIds.get(maxSpeed)+1);
		}

		System.out.println("死锁车辆中对应速度下的车辆数　："+maxSpeedCarIds);
		
	
	}
	private void printMessageInEachStep() {
		System.out.println("step : " + step + 
				"   existCarCount : " + existCarIds.size()+ 
				"   finished : " + finishedCarIds.size()+
				"   unfinished: "+(carMap.size() - finishedCarIds.size())+
				"   unjoined :" +(carMap.size() - finishedCarIds.size()- existCarIds.size()));
		
	}


	

	public Map<Integer, Road> getRoadMap() {
		return roadMap;
	}
	public Map<Integer, Car> getCarMap() {
		return carMap;
	}
	public Map<Integer, Cross> getCrossMap() {
		return crossMap;
	}
	
	public int getStep() {
		return step;
	}
}
