package main.java.com.huawei.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import main.java.com.huawei.unit.Car;
import main.java.com.huawei.unit.ReadFile;

public class DataAnalysis {
	private String map1 = "config_map2";
	private Map<Integer, Car> carMap;
	private ArrayList<Integer> priorityCarIds = new ArrayList<Integer>();
	private ArrayList<Integer> prowlCarIds = new ArrayList<Integer>();
	
	private ArrayList<Integer> presetCarIds = new ArrayList<Integer>();
	private ArrayList<Integer> normalCarIds = new ArrayList<Integer>();

	private ArrayList<Data> dataMaps = new ArrayList<Data>();
	
	public DataAnalysis() {
		// TODO Auto-generated constructor stub
		
		String[] path = new String[4];
		path[0] ="/home/poorlemon/Work/HuaWei/SDK/newData/"+map1+"/car.txt";
		path[1] ="/home/poorlemon/Work/HuaWei/SDK/newData/"+map1+"/road.txt";
		path[2] ="/home/poorlemon/Work/HuaWei/SDK/newData/"+map1+"/cross.txt";
		path[3] ="/home/poorlemon/Work/HuaWei/SDK/newData/"+map1+"/presetAnswer.txt";	
		
		

		Data dataMap1 = LoadData(path);
	
		
		dataMaps.add(dataMap1);

		
		
		DataCount();
		CarCount();
		SpeedAnalysis();
//		StartCrossCarCount();
//		StartCrossCarSpeed();
	}
	private void SpeedAnalysis() {
		System.out.println("---------------普通车辆------------------------");
		getCarSpeedAnalysis(normalCarIds,false);
		System.out.println("---------------普通车辆------------------------");
		System.out.println();
		System.out.println();
		System.out.println("---------------优先非预置车辆------------------------");
		getCarSpeedAnalysis(priorityCarIds,false);
		System.out.println("---------------优先非预置车辆------------------------");
		System.out.println();
		System.out.println();
		
		System.out.println("---------------预置车辆------------------------");
		getCarSpeedAnalysis(presetCarIds,true);
		System.out.println("---------------预置车辆------------------------");
		System.out.println();
		System.out.println();
	
		
	
	}
	private void getCarSpeedAnalysis(ArrayList<Integer> carIds,boolean isPreset) {
		Map<Integer, ArrayList<Integer>> maxSpeedCarIds = new HashMap<Integer, ArrayList<Integer>>();
		Map<Integer, Integer> maxSpeedCarCount = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> maxSpeedCarMaxAllowTime = new TreeMap<Integer, Integer>();
		for(int carId :carIds) {
			
			int maxSpeed = carMap.get(carId).getMaxSpeed();
			int allowTime = -1;
			if(!isPreset) {
				allowTime = carMap.get(carId).getAllowStartTime();
			}
			else {
				allowTime = carMap.get(carId).getRealTime();
			}
			 
			if(!maxSpeedCarMaxAllowTime.containsKey(maxSpeed)) {
				maxSpeedCarMaxAllowTime.put(maxSpeed,-1);
			}
			if(maxSpeedCarMaxAllowTime.get(maxSpeed) < allowTime) {
				maxSpeedCarMaxAllowTime.put(maxSpeed,allowTime);
			}
			
			if(!maxSpeedCarIds.containsKey(maxSpeed)) { 
				maxSpeedCarIds.put(maxSpeed, new ArrayList<Integer>());
			}
			maxSpeedCarIds.get(maxSpeed).add(carId);
			
		}
		for(int startCross : maxSpeedCarIds.keySet()) {
				maxSpeedCarCount.put(startCross, maxSpeedCarIds.get(startCross).size());
		}
		System.out.println("车辆速度以及对应数量    : " + maxSpeedCarCount);
		System.out.println("车辆速度以及最晚出发时间 : " + maxSpeedCarMaxAllowTime);
		
	}
	private void CarCount() {
	
		
			carMap = dataMaps.get(0).getCarMap();

//			ArrayList<Integer> prowl
			for(int carId : carMap.keySet()) {
				Car car = carMap.get(carId);
				if(car.isPriority() == true && car.isPreset()) {
					prowlCarIds.add(carId);
				}
				if(car.isPriority() == true && car.isPreset() == false) {
					priorityCarIds.add(carId);
				}
				if(car.isPreset() == true) {
					presetCarIds.add(carId);
				}
				if(car.isPreset() == false && car.isPriority() == false) {
					normalCarIds.add(carId);
				}
			}
			System.out.println("---------------车辆数目信息------------------------");
			System.out.println("总车数               : " +  carMap.size());
			System.out.println("优先通行(非预设)车数   : " + priorityCarIds.size());
			System.out.println("预先设路车数          : " + presetCarIds.size());
			System.out.println("优先同行(预设)车数     : " + prowlCarIds.size());
			System.out.println("普通车辆数            : " + normalCarIds.size());
			System.out.println("---------------车辆数目信息------------------------");
			System.out.println();
			System.out.println();
			
			
			
//			System.exit(0);
			
		
	}
	private void DataCount() {
		System.out.println("---------------总数目信息------------------------");
		for(Data data : dataMaps){
			System.out.println(data.getMap());
			System.out.println("车辆数目： " + data.getCarMap().size());
			System.out.println("道路数目 ： " + data.getRoadMap().size());
			System.out.println("路口数目 ： " + data.getCrossMap().size());
		}
		System.out.println("---------------总数目信息------------------------");
		System.out.println();
		System.out.println();
	}
	private void StartCrossCarCount() {
		
		for(Data data : dataMaps){
			
			System.out.println(data.getMap());
			Map<Integer, Car> carMap = data.getCarMap();
			Map<Integer, ArrayList<Integer>> startCrossCarIds = new HashMap<Integer, ArrayList<Integer>>();
			Map<Integer, Integer> startCrossCarCount = new TreeMap<Integer, Integer>();
			for(int carId : carMap.keySet()) {
				int starCross = carMap.get(carId).getEndCross();
				if(!startCrossCarIds.containsKey(starCross)) {
					startCrossCarIds.put(starCross, new ArrayList<Integer>());
				}
				startCrossCarIds.get(starCross).add(carId);
			}
			for(int startCross : startCrossCarIds.keySet()) {
				startCrossCarCount.put(startCross, startCrossCarIds.get(startCross).size());
				
				
			}
			System.out.println(startCrossCarCount);
		}
	}
	private void StartCrossCarSpeed() {
		
		for(Data data : dataMaps){
			System.out.println(data.getMap());
			Map<Integer, Car> carMap = data.getCarMap();
			Map<Integer, ArrayList<Integer>> startCrossCarIds = new HashMap<Integer, ArrayList<Integer>>();
			Map<Integer, Integer> startCrossCarCount = new TreeMap<Integer, Integer>();
			for(int carId : carMap.keySet()) {
				int starCross = carMap.get(carId).getStartCross();
				if(!startCrossCarIds.containsKey(starCross)) {
					startCrossCarIds.put(starCross, new ArrayList<Integer>());
				}
				startCrossCarIds.get(starCross).add(carId);
			}
			for(int startCross : startCrossCarIds.keySet()) {
				ArrayList<Integer > carIds = startCrossCarIds.get(startCross);
				Map<Integer, ArrayList<Integer>> maxSpeedCarIds = new TreeMap<Integer, ArrayList<Integer>>();
				Map<Integer, Integer> maxSpeedCarCount = new TreeMap<Integer, Integer>();
				for(int carId  : carIds) {
					int maxSpeed = carMap.get(carId).getMaxSpeed();
					if(!maxSpeedCarIds.containsKey(maxSpeed)) {
						maxSpeedCarIds.put(maxSpeed,new ArrayList<Integer>());
					}
					maxSpeedCarIds.get(maxSpeed).add(carId);
				}
				for(int maxSpeed : maxSpeedCarIds.keySet()) {
					maxSpeedCarCount.put(maxSpeed,maxSpeedCarIds.get(maxSpeed).size());
				}
				
				System.out.println("cross :" +startCross + "   " +maxSpeedCarCount);
			
			}
			System.out.println(startCrossCarCount);
		}
	}
	
	
	private Data LoadData(String[] path) {
			
		ReadFile readFile = new ReadFile(path[0], path[1], path[2],path[3]);
		
		Data data = new Data(map1,readFile.getCarMap(), readFile.getRoadMap(), readFile.getCrossMap());
		return data;
	}
	public static void main(String[] args) {
		DataAnalysis analysis = new DataAnalysis();
	}
}

