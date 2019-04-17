package main.java.com.huawei.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.IntPredicate;

import main.java.com.huawei.unit.Car;
import main.java.com.huawei.unit.Cross;
import main.java.com.huawei.unit.ReadFile;
import main.java.com.huawei.unit.Road;
import main.java.com.huawei.unit.Schedule;
class Edge implements Comparable<Edge>{
	int to ; 
	double cost;
	Edge(int to_,double cost_){
		to = to_;
		cost = cost;
	}
	@Override
	public int compareTo(Edge o) {
		// TODO Auto-generated method stub
		return this.cost < o.cost ? 1 : -1;
	}
}
public class PathPlanning {
	private double maxNumber= 9999999999.0;
	private String roadPath; 
	private String carPath;
	private String crossPath;
	private String presetAnswerPath;
	private String answerPath;
	
	private Map<Integer,Car> carMap;
	private Map<Integer,Road> roadMap;
	private TreeMap<Integer,Cross> crossMap;
	private TreeMap<Integer, ArrayList<Integer>> maxSpeedNormalCarIdsMap;
	private TreeMap<Integer, ArrayList<Integer>> maxSpeedPriorityCarIdsMap;
	private int crossNum;
	private ArrayList<Integer> bossCarIds;
	private ArrayList<Integer> presetCarIds;
	private ArrayList<Integer> priorityCarIds;
	private ArrayList<Integer> normalCarIds;
	private ArrayList<Schedule> priorityCarAllowTime;
	private int prowlCount = -1;

	private ArrayList<Schedule> presetSchedules;
	private Map<Integer, Boolean> visited;
	private Map<Integer, Integer> path;
	private Map<Integer, Double> dis;  //到达对应cross的代价
	private Map<Integer, Double> times;  //到达对应cross的时间
	public PathPlanning(String roadPath, String carPath, String crossPath,String presetAnswerPath)  {
		// TODO Auto-generated constructor stub 
		this.roadPath = roadPath;
		this.carPath = carPath;
		this.crossPath = crossPath;
		this.presetAnswerPath = presetAnswerPath;
		this.answerPath = answerPath;
	
		
		Loading();
		crossNum = crossMap.size();
		Parameter.setParameter(carMap);
		setCross2Road();
		classifyCar();
		setValue();
		
		addPresetCarInRoad();
		
		sortRoadSchedule();
		
		setPriorityCarSchedule();

		maxSpeedNormalCarIdsMap = setMaxSpeedCarIdsMap(normalCarIds);	
	

		
	}
	private void setPriorityCarSchedule() {
		priorityCarAllowTime = new ArrayList<Schedule>();
		for(int carId : priorityCarIds) {
			int allowTime = carMap.get(carId).getAllowStartTime();
			priorityCarAllowTime.add(new Schedule(carId, allowTime));
		}
		Collections.sort(priorityCarAllowTime);
		
		
		
		
	}
	private void sortRoadSchedule() {
		for(int roadId : roadMap.keySet()) {
			roadMap.get(roadId).sortSchedule();
		}
	}
	private void classifyCar() {
		presetCarIds = new ArrayList<Integer>();
		priorityCarIds = new ArrayList<Integer>();
		normalCarIds = new ArrayList<Integer>();
		for(int carId : carMap.keySet()) {
			boolean isPriority =carMap.get(carId).isPriority();
			boolean isPreset = carMap.get(carId).isPreset();
			if(isPriority && isPreset) {
				prowlCount++;
			}
			if(isPreset == true) {
				presetCarIds.add(carId);
			}
			else if(isPreset == false && isPriority == true) {
				priorityCarIds.add(carId);
			}
			else {
				normalCarIds.add(carId);
			}
			
		}
	}
	private void  addPresetCarInRoad() {
		for(int roadId : roadMap.keySet()) {
			roadMap.get(roadId).initSchedule();
		}
		for(int carId : presetCarIds) {
			
			Car car = carMap.get(carId);
			
			
			int roadId = car.getRoutes().get(0);
			int startCross = car.getStartCross();
			int realTime = car.getRealTime();
			boolean isPriority = car.isPriority();
			
			ArrayList<Integer> routes = car.getRoutes();
			int startCrossId = car.getStartCross();
			for(int id : routes) {
				
				roadMap.get(id).addCar(carId,startCrossId,isPriority);
				startCrossId = roadMap.get(id).getNeighborCross(startCrossId); 
			}	
			
			
			Schedule schedule = new Schedule(carId, realTime);
			roadMap.get(roadId).addSchedule(schedule, startCross, isPriority);
				
		}
		
	}

	public PathPlanning(TreeMap<Integer, Cross> crossMap,Map<Integer, Road> roadMap) {
		// TODO Auto-generated constructor stub
		this.crossMap = crossMap;
		this.roadMap = roadMap;
		this.crossNum = crossMap.size();
		setCross2Road();
		setValue();
		
	}

	private TreeMap<Integer, ArrayList<Integer>> setMaxSpeedCarIdsMap(ArrayList<Integer> carIds) {
		TreeMap<Integer, ArrayList<Integer>> maxSpeedCarIdsMap =  new TreeMap<Integer, ArrayList<Integer>>();
		for(int carId : carIds) {
			int maxSpeed = carMap.get(carId).getMaxSpeed();
			if(!maxSpeedCarIdsMap.containsKey(maxSpeed))
				maxSpeedCarIdsMap.put(maxSpeed,new ArrayList<Integer>());
			maxSpeedCarIdsMap.get(maxSpeed).add(carId);
		}
		return maxSpeedCarIdsMap;
	}
	
	public boolean reSetRoutes(Car car,Map<Integer, Road> roadMap) {
		
		
		//这里我们先分析下第一个选择的路能不能放进去
		
		
		int carId = car.getId();
		int startCross = car.getStartCross();
		int endCross = car.getEndCross();
		if(car.isPriority())
			updateValue(-1,roadMap);
		else
			updateValue(car.getMaxSpeed(), roadMap);
		Cross cross = crossMap.get(startCross);
		ArrayList<Integer> roadIds = cross.getRoadIds();

		int flag = 0;
		for(int roadId : roadIds) {
		
			if(roadId == -1) {
				continue;
			}
			Road road = roadMap.get(roadId);
			//如果与startCross连接的路是满的话,不会选择这条路
			
			if(!road.canJoinFromCross(startCross))
				continue;
			if(road.getViableLaneId(startCross)==-1) {
				
				cross.setNewValue(road.getNeighborCross(startCross), maxNumber);
			}
			else {
				flag = 1;
			}
		}
		//如果与startCross连接的路都是满的话,直接返回
		if(flag == 0) {
			return false;
		}
		
		
		
		dijkstra(car,roadMap,startCross,carId,true);
		
		ArrayList<Integer> routes = getRoutes(startCross, endCross);
		
		Map<Integer, Double> predictRoad2EndTime = getPredictRoad2EndTime(car.getMaxSpeed(),routes);	
		car.setPredictTotalTime(predictRoad2EndTime.get(routes.get(0)));		
		car.setRoutes(routes);
		return true;
	}
	
	public boolean reSetRoutes(Car car,int startCross,Map<Integer, Road> roadMap,int beforeCrossId,int step,int nextRoad) {	
		
		
		int carId = car.getId();
		int endCross = car.getEndCross();
		updateValue(car.getMaxSpeed(),roadMap);
		
		crossMap.get(startCross).setNewValue(beforeCrossId,maxNumber);
		
		
		dijkstra(car,roadMap,startCross,carId,false);

		ArrayList<Integer> newRoutes = new ArrayList<Integer>();
		ArrayList<Integer> routes = getRoutes(startCross, endCross);
		newRoutes.add(nextRoad);
		newRoutes.addAll(routes);
		
		Map<Integer, Double> predictRoad2EndTime = getPredictRoad2EndTime(car.getMaxSpeed(),newRoutes);
		car.setRoutes(newRoutes);
		car.setPredictTotalTime(predictRoad2EndTime.get(routes.get(0)));
		if(predictRoad2EndTime.get(routes.get(0)) == 0) {
			System.exit(0);
		}
		return true;
	}
	
	

	
	private void setCross2Road() {
		for(int crossId : crossMap.keySet()) {
			Map<Integer,Integer> cross2Road = new HashMap<Integer, Integer>();
			ArrayList<Integer> roadIds = crossMap.get(crossId).getRoadIds();
			ArrayList<Integer> neighborCrossIds = new ArrayList<Integer>();
	      	for(int roadId : roadIds) {
	      		if(roadId == -1) {
	      			continue;
	      		}
	      		Road road = roadMap.get(roadId);
	      		int neighborCrossId  = road.getNeighborCross(crossId);
	      		
	      		
	      		if(neighborCrossId !=-1) {
	      			cross2Road.put(neighborCrossId, roadId);
	      			neighborCrossIds.add(neighborCrossId);
	      		}
	      	}
	      	crossMap.get(crossId).setCross2Road(cross2Road); //
	      	crossMap.get(crossId).setNeighborCrossIds(neighborCrossIds);
		}
	}
	private void setValue() {
		for(int crossId : crossMap.keySet()) {
			Cross cross = crossMap.get(crossId);
			ArrayList<Integer> neighborCrossIds = cross.getNeighborCrossIds();
			Map<Integer, Double> neighborCrossValue = new HashMap<Integer, Double>();
			for(int neighborCrossId : neighborCrossIds) {
				
				Road road = roadMap.get(cross.getRoadId(neighborCrossId));
		
				double distance = 1.0 *road.getLength();
				neighborCrossValue.put(neighborCrossId,distance);
				
			}
			cross.setNeighborCrossValue(neighborCrossValue);
					
		}
		
	}
	
	
	private Map<Integer,Double> getPredictRoad2EndTime(int carMaxSpeed, ArrayList<Integer> roadList){
		//计算在这条道路上行驶时候，预计什么时候结束
		Map<Integer,Double> predictRoad2EndTime = new HashMap<Integer, Double>();
		double sumTime = 0.0;
		int roadId = -1;
		int length = -1;
		int roadMaxSpeed = -1;
		
		for(int i = roadList.size()-1; i>=0;i--) {
			roadId = roadList.get(i);
			length = roadMap.get(roadId).getLength();
			roadMaxSpeed = roadMap.get(roadId).getMaxSpeed();
			sumTime = sumTime + 1.0*length/Math.min(roadMaxSpeed, carMaxSpeed);
			predictRoad2EndTime.put(roadId, sumTime);
		}
		return predictRoad2EndTime;
	}
	private void updateValue(int carMaxSpeed ,Map<Integer, Road> roadMap){ //按照个体的速度，采用一定的权重
		
		
		for(int crossId : crossMap.keySet()) {
			
			Cross cross = crossMap.get(crossId);
			ArrayList<Integer> neighborCrossIds = cross.getNeighborCrossIds();
		
			Map<Integer, Double> neighborCrossNewValue = new HashMap<Integer, Double>();
			for(int neighborCrossId : neighborCrossIds) {
				int roadId = crossMap.get(crossId).getRoadId(neighborCrossId);
				int roadMaxSpeed = roadMap.get(roadId).getMaxSpeed();
				
				int laneNum = roadMap.get(roadId).getNumber();
				
				double crowdValue = roadMap.get(roadId).getCrowdValue(crossId);
				
				int notArrivedPriorityCarCount = roadMap.get(roadId).getNotArrivedCarCount(crossId,true);
				int notArrivedNormalCarCount = roadMap.get(roadId).getNotArrivedCarCount(crossId,false);
				
				double timeValue =cross.getNeighborCrossValue(neighborCrossId)/ Math.min(roadMaxSpeed, carMaxSpeed);
				
				
				double speedValue = Math.abs(roadMaxSpeed - carMaxSpeed) ;
									
					//在线道路评估方式
				double newValue =
					1.0 * Parameter.maxSpeedParameter.get(carMaxSpeed).laneNumberWeight * laneNum +
							  
							
					1.0 * Parameter.maxSpeedParameter.get(carMaxSpeed).timeValueWeight * timeValue +
							
					1.0 * Parameter.maxSpeedParameter.get(carMaxSpeed).speedValueWeight * speedValue+
							
					1.0 * Parameter.maxSpeedParameter.get(carMaxSpeed).crowdValueWeight * crowdValue +
							
					1.0 * Parameter.maxSpeedParameter.get(carMaxSpeed).notArrivedPriorityCarCountWeight * notArrivedPriorityCarCount +
					
					1.0 * Parameter.maxSpeedParameter.get(carMaxSpeed).notArrivedNormalCarCountWeight * notArrivedNormalCarCount;
				neighborCrossNewValue.put(neighborCrossId,newValue);
			}
			cross.setNeighborCrossNewValue(neighborCrossNewValue);
		}
					
	}
		

	private boolean dijkstra(Car car,Map<Integer, Road> roadMap,int startCross ,int carId,boolean isFirstJoin) {
		visited = new HashMap<Integer, Boolean>();
		path = new HashMap<Integer, Integer>();
		dis = new HashMap<Integer, Double>();
		times = new HashMap<Integer, Double>();
		Cross cross = crossMap.get(startCross);
		ArrayList<Integer> sneighborCrossIds = cross.getNeighborCrossIds();
	
		
		for(int crossId : crossMap.keySet()) {
			visited.put(crossId,false); 
			if(sneighborCrossIds.contains(crossId)) {
				dis.put(crossId,cross.getNeighborCrossNewValue(crossId));
			}
			else {
				dis.put(crossId,maxNumber);
			}
			path.put(crossId,-1);
			times.put(crossId,0.0);
		}
		visited.put(startCross,true);
		for(int i = 1;i<crossNum;i++) {
			int p = -1;
			double minn = maxNumber;
			for(int crossId : crossMap.keySet()) {
				if(visited.get(crossId) == false && dis.get(crossId)<minn) {
					p = crossId;
					minn = dis.get(crossId);
				}
			}		
			visited.put(p,true);
			int roadId;
			if(path.get(p) == -1) {
				roadId = cross.getRoadId(p);
			}
			else {
				roadId = crossMap.get(path.get(p)).getRoadId(p);
			}
			double t = 1.0 * roadMap.get(roadId).getLength() / Math.min(roadMap.get(roadId).getMaxSpeed(),car.getMaxSpeed());
			times.put(p,t);
			
			Cross crossTemp = crossMap.get(p);
			ArrayList<Integer> neighborCrossIds = crossTemp.getNeighborCrossIds();
			
			for(int neighborCrossId : neighborCrossIds) {
			
				double cost = crossTemp.getNeighborCrossNewValue(neighborCrossId);
				
				
				
				if(visited.get(neighborCrossId)==false && cost<maxNumber) { 
					
					int preCrossId = path.get(p);
					if(preCrossId == -1) {
						preCrossId = startCross;
					}
					int preRoadId = crossMap.get(preCrossId).getRoadId(p);
					
					int nextRoadId = crossMap.get(p).getRoadId(neighborCrossId);
					
					int priority = crossMap.get(p).getPassPriority(preRoadId, nextRoadId);
					
					double priorityValue =  1.0 * Parameter.maxSpeedParameter.get(car.getMaxSpeed()).priorityWeight * priority;
					
					double tempTime = 1.0 * roadMap.get(nextRoadId).getLength() / Math.min(roadMap.get(nextRoadId).getMaxSpeed(),car.getMaxSpeed());
					
					double tt = times.get(p) + tempTime;
					
					
					if(dis.get(p)  +  cost + priorityValue < dis.get(neighborCrossId)) {
						dis.put(neighborCrossId,dis.get(p) +  cost + priorityValue);
						path.put(neighborCrossId,p);
						times.put(neighborCrossId,tt);
					}
				}
			}	
		}
		return true;
	}
	
//	private void dijkstraPriorityQueue(Car car,Map<Integer, Road> roadMap,int startCross , double[][] newValue,int carId) {
//		visited  = new boolean[crossNum+1];
//		path = new int[crossNum+1];
//		dis = new double[crossNum+1];
//		int flag = 0;
//		Queue<Edge> que = new PriorityQueue<Edge>();
//		que.add(new Edge(startCross, 0.0));
//
//		for(int i = 1;i<=crossNum;i++) {
//			dis[i] = maxNumber; 
//			visited[i] = false;
//			path[i] = -1;
//		}
//		dis[startCross] = 0;
//		while(que.isEmpty() == false) {
//			Edge temp = que.poll();
//			int crossId = temp.to;
//			if(dis[crossId] < temp.cost) {
//				continue;
//			}
//			if(visited[crossId] == true) {
//				continue;
//			}
//			visited[crossId] = true;
//			ArrayList<Integer> neighborCrossIds = crossMap.get(crossId).getNeighborCrossIds();
//			for(int j = 1; j<=crossNum; j++) {
//				double cost = newValue[crossId][j];
//				if(visited[j]== false && cost< maxNumber) {
//					int preCrossId = path[crossId];
//					if(preCrossId == -1) {
//						preCrossId = startCross;
//					}
//					int preRoadId = crossMap.get(preCrossId).getRoadId(crossId);
//					
//					int nextRoadId = crossMap.get(crossId).getRoadId(j);
//					
//					int priority = crossMap.get(crossId).getPassPriority(preRoadId, nextRoadId);
//					double priorityValue =  1.0 * Parameter.maxSpeedParameter.get(car.getMaxSpeed()).priorityWeight * priority;
//					if(dis[crossId] + cost + priorityValue < dis[j]) {
//						dis[j] = dis[crossId] + cost + priorityValue;
////						if(flag == 0) {
////							flag = 1;
////						}
////						else 
////							path[neighborCrossId]=crossId;
//						que.add(new Edge(neighborCrossId, dis[neighborCrossId]));
//					}
//				}
//			}
//			
//		}
//		
//		
//		
//		
//		
//
//	}
	ArrayList<Integer> getRoutes(int id,int endCrossId){
		
		
		Stack<Integer> q = new Stack<Integer>();
		ArrayList<Integer> routes = new ArrayList<Integer>();
		ArrayList<Integer> crossList = new ArrayList<Integer>();
		
		crossList.add(id);
		int p = endCrossId;
	    while(path.get(p)!=-1){
	        q.push(p);
	        p=path.get(p);
	    }
	    q.push(p);
	    while(!q.empty()){
	    	
	      	crossList.add(q.peek());  
	        q.pop();
	    }     
	    for(int i = 1;i<crossList.size();i++) {
	    	
	    	  
	      	int preCrossId = crossList.get(i-1);
	      	int tempCrossId = crossList.get(i);
	      	int tempRoadId = crossMap.get(preCrossId).getRoadId(tempCrossId);
	      	if(tempRoadId == -1) {
	      		System.out.println("没有这条路！");
	      	}
	      	routes.add(tempRoadId);
	    }
	    return routes;
	}


	public Map<Integer, Car> getCarMap() {
		return carMap;
	}
	public Map<Integer, Road> getRoadMap() {
		return roadMap;
	}
	public TreeMap<Integer, Cross> getCrossMap() {
		return crossMap;
	}
	public ArrayList<Schedule> getPresetSchedules() {
		return presetSchedules;
	}
	public TreeMap<Integer, ArrayList<Integer>> getMaxSpeedNormalCarIdsMap() {
		return maxSpeedNormalCarIdsMap;
	}
	public TreeMap<Integer, ArrayList<Integer>> getMaxSpeedPriorityCarIdsMap() {
		return maxSpeedPriorityCarIdsMap;
	}
	public ArrayList<Schedule> getPriorityCarAllowTime() {
		return priorityCarAllowTime;
	}
	private void Loading() {
		ReadFile readFile = new ReadFile(carPath, roadPath, crossPath,presetAnswerPath);
		carMap = readFile.getCarMap();
		roadMap = readFile.getRoadMap();
		crossMap = readFile.getCrossMap();

	}
	public int getProwlCount() {
		return prowlCount;
	}
}