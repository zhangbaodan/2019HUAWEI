package main.java.com.huawei.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PreStep {
	private int step;
	private int sumTime;
	private Map<Integer,Car> carMap;
	private Map<Integer,Road> roadMap;
	private TreeMap<Integer, Cross> crossMap;
	private Set<Integer> finishedCarIds;  //已经完成的车列表
	private Set<Integer> existCarIds;
	private Set<Integer> waittingCarIds; //等待处理的车列表，用于处理死循环
	private TreeMap<Integer, ArrayList<Integer>> maxSpeedNormalCarIdsMap;
	private ArrayList<Schedule> priorityCarAllowTime;
	public PreStep(int step) {
		// TODO Auto-generated constructor stub
		this.step = step;
	}
	public void setPreStep(Map<Integer,Car> carMap,Map<Integer,Road> roadMap,Map<Integer,Cross> crossMap,
							Set<Integer> finishedCarIds, Set<Integer> existCarIds,
							TreeMap<Integer, ArrayList<Integer>> maxSpeedNormalCarIdsMap,
							ArrayList<Schedule> priorityCarAllowTime,int sumTime) {
		this.carMap = new HashMap<Integer, Car>();
		for(int carId : carMap.keySet()) {
			this.carMap.put(carId,(Car) carMap.get(carId).clone());
		}
		this.roadMap = new HashMap<Integer, Road>();
		for(int roadId : roadMap.keySet()) {
			this.roadMap.put(roadId,(Road) roadMap.get(roadId).clone());
		}
		this.crossMap = new TreeMap<Integer, Cross>();
		for(int crossId : crossMap.keySet()) {
			this.crossMap.put(crossId,(Cross) crossMap.get(crossId).clone());
		}
		this.finishedCarIds = new HashSet<Integer>();
		for(int carId : finishedCarIds) {
			this.finishedCarIds.add(carId);
		}
		this.existCarIds = new HashSet<Integer>();
		for(int carId : existCarIds) {
			this.existCarIds.add(carId);
		}
		this.waittingCarIds = new HashSet<Integer>();
		this.maxSpeedNormalCarIdsMap = new TreeMap<Integer, ArrayList<Integer>>();
		for(int maxSpeed : maxSpeedNormalCarIdsMap.keySet()) {
			this.maxSpeedNormalCarIdsMap.put(maxSpeed,new ArrayList<Integer>());
			for(int carIds : maxSpeedNormalCarIdsMap.get(maxSpeed)) {
				this.maxSpeedNormalCarIdsMap.get(maxSpeed).add(carIds);
			}
		}
		this.priorityCarAllowTime = new ArrayList<Schedule>();
		for(Schedule schedule : priorityCarAllowTime) {
			this.priorityCarAllowTime.add(new Schedule(schedule.getCarId(), schedule.getRealTime()));
		}
		this.sumTime = sumTime;
	}
	
	public int getStep() {
		return step;
	}
	public int getSumTime() {
		return sumTime;
	}
	public Map<Integer, Car> getCarMap() {
		return carMap;
	}
	public Set<Integer> getExistCarIds() {
		return existCarIds;
	}
	public Set<Integer> getFinishedCarIds() {
		return finishedCarIds;
	}
	public Map<Integer, Road> getRoadMap() {
		return roadMap;
	}
	
	public Set<Integer> getWaittingCarIds() {
		return waittingCarIds;
	}
	public TreeMap<Integer, Cross> getCrossMap() {
		return crossMap;
	}
	public TreeMap<Integer, ArrayList<Integer>> getMaxSpeedNormalCarIdsMap() {
		return maxSpeedNormalCarIdsMap;
	}
	public ArrayList<Schedule> getPriorityCarAllowTime() {
		return priorityCarAllowTime;
	}
	
}