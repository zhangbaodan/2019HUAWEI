package main.java.com.huawei.system;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import main.java.com.huawei.unit.Car;
import main.java.com.huawei.unit.Cross;
import main.java.com.huawei.unit.ReadFile;
import main.java.com.huawei.unit.Road;
import main.java.com.huawei.unit.Schedule;

public class JudgeSystem {

	
	private Map<Integer, Car> carMap;
	private Map<Integer, Road> roadMap;
	private TreeMap<Integer, Cross> crossMap;

	
	
	private String carPath;
	private String roadPath;
	private String crossPath;
	private String answerPath;
	private String presetAnswerPath;
	
	
	public JudgeSystem(String[] args) {
		// TODO Auto-generated constructor stub
		//纯判题器系统
		this.carPath = args[0];
		this.roadPath = args[1];
        this.crossPath = args[2];
        this.presetAnswerPath = args[3];
        this.answerPath = args[4];
        //加载数据
		Loading();
		//进入判题器系统
		Simulation simulation = new Simulation(carMap, roadMap, crossMap);
		
		
	}
	
	
	private void Loading() {
		ReadFile readFile = new ReadFile(carPath, roadPath, crossPath,answerPath,presetAnswerPath);
		
		carMap = readFile.getCarMap();
		roadMap = readFile.getRoadMap();
		crossMap = readFile.getCrossMap();
		initRoadCarList();
	}
	private void initRoadCarList() {
		for(int roadId : roadMap.keySet()) {
			roadMap.get(roadId).initSchedule();
		}
		
		for(int carId : carMap.keySet()) {
			Car car = carMap.get(carId);
			int roadId = car.getRoutes().get(0);
			int startCross = car.getStartCross();
			int realTime = car.getRealTime();
			boolean isPriority = car.isPriority();
			Schedule schedule = new Schedule(carId, realTime);
			roadMap.get(roadId).addSchedule(schedule, startCross, isPriority);
		}
		for(int roadId : roadMap.keySet()) {
			roadMap.get(roadId).sortSchedule();
		}
		
		
	}
	

	
}