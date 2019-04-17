package main.java.com.huawei.system;

import java.util.ArrayList;
import java.util.Map;

import main.java.com.huawei.unit.Car;
import main.java.com.huawei.unit.Cross;
import main.java.com.huawei.unit.Road;


public class Data {
	private String map;
	private Map<Integer,Car> carMap;
	private Map<Integer,Road> roadMap;
	private Map<Integer,Cross> crossMap;

	
	public Data(String map,Map<Integer, Car> carMap, Map<Integer, Road> roadMap,
			Map<Integer, Cross> crossMap) {
		// TODO Auto-generated constructor stub
		this.map = map;
		this.carMap = carMap;
		this.roadMap = roadMap;
		this.crossMap = crossMap;
		
	}
	public Map<Integer, Car> getCarMap() {
		return carMap;
	}
	public Map<Integer, Cross> getCrossMap() {
		return crossMap;
	}
	public String getMap() {
		return map;
	}
	public Map<Integer, Road> getRoadMap() {
		return roadMap;
	}
	
	
	
}
