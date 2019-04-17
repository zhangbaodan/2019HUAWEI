package main.java.com.huawei.unit;

import java.util.ArrayList;
import java.util.Map;

public class Cross implements Cloneable{
	private int id;
	private ArrayList<Integer> roadIds;
	private int upRoadId = -1;
	private int rightRoadId = -1;
	private int downRoadId = -1;
	private int leftRoadId = -1;
	private Map<Integer, Integer> cross2Road;
	private ArrayList<Integer> startCrossCarIds;
	private ArrayList<Integer> neighborCrossIds;
	private Map<Integer, Double> neighborCrossValue;
	private Map<Integer, Double> neighborCrossNewValue;
	private double value;
	public Cross(Integer id, Integer upRoadId, Integer rightRoadId, Integer downRoadId, Integer leftRoadId) {
		// TODO Auto-generated constructor stub
		this.id = id;
		roadIds = new ArrayList<Integer>();
		startCrossCarIds = new ArrayList<Integer>();
		this.upRoadId = upRoadId;
		this.rightRoadId = rightRoadId;
		this.downRoadId = downRoadId;
		this.leftRoadId =  leftRoadId;
		roadIds.add(upRoadId);
		roadIds.add(rightRoadId);
		roadIds.add(downRoadId);
		roadIds.add(leftRoadId);
		
		
	}
	public void addStartCrossCarId(int carId) {
	
		this.startCrossCarIds.add(carId);
	}
	
	public ArrayList<Integer> getStartCrossCarIds() {
		return startCrossCarIds;
	}
	public ArrayList<Integer> getRoadIds() {
		ArrayList<Integer> newRoadIds = new ArrayList<Integer>();
		newRoadIds.add(upRoadId);
		newRoadIds.add(rightRoadId);
		newRoadIds.add(downRoadId);
		newRoadIds.add(leftRoadId);
		return newRoadIds;
	}
	public int getPassPriority(int tempRoadId, int nextRoadId) {
		int tempIndex = -1;
		int nextIndex = -1;
		if(nextRoadId == -1) {
			//到达终点算直行
			return 0;
			
		}
		for(int i =0;i<4;i++) {
			if(roadIds.get(i) == tempRoadId)
				tempIndex = i;
			if(roadIds.get(i) == nextRoadId)
				nextIndex = i;
		}
		if(tempIndex == -1 || nextIndex == -1 || tempIndex == nextIndex) {
			
			System.out.println("tempRoadId : " + tempRoadId + "    nextRoadId  :" + nextRoadId);
			System.out.println(roadIds);
			System.out.println("道路通过优先级出错.....");
			System.exit(0);
			return -1;
		}
		int temp = tempIndex - nextIndex;
		if(temp == 2 || temp == -2) { //直行
			return 0; 
		}
		else if(temp == -1 || temp == 3){ //左转
			return 1;
		}
		
		else //右转
			return 2;  
	}
	public int getArivedCarStrightRoadId(int roadId) {
		int tempIndex = -1;
		for(int i = 0;i<4;i++) {
			if(roadIds.get(i) == roadId) {
				tempIndex = i;
			}
		}
		if(tempIndex == 0) 
			return roadIds.get(2);
		else if(tempIndex == 1)
			return roadIds.get(3);
		else if(tempIndex == 2)
			return roadIds.get(0);
		else if(tempIndex == 3)
			return roadIds.get(1);
		else {
			System.out.println("到达终点车辆，算作直行，获得执行对面路出错");
			System.exit(0);
			return -1;
		}
		
	}
	public int getStrightRoadId(int roadId) {
		
		
		
		int tempIndex = -1;
		for(int i = 0;i<4;i++) {
			if(roadIds.get(i) == roadId) {
				tempIndex = i;
			}
		}
		if(tempIndex == 0) 
			return roadIds.get(2);
		else if(tempIndex == 1)
			return roadIds.get(3);
		else if(tempIndex == 2)
			return roadIds.get(0);
		else if(tempIndex == 3)
			return roadIds.get(1);
		else {
			System.out.println("优先级判定出错");
			System.exit(0);
			return -1;
		}
			
	}
	
	public int getLeftRoadId(int roadId) {
		int tempIndex = -1;
		for(int i = 0;i<4;i++) {
			if(roadIds.get(i) == roadId) {
				tempIndex = i;
			}
		}
		if(tempIndex == 0) 
			return roadIds.get(3);
		else if(tempIndex == 1)
			return roadIds.get(0);
		else if(tempIndex == 2)
			return roadIds.get(1);
		else if(tempIndex == 3)
			return roadIds.get(2);
		else {
			System.out.println("优先级判定出错");
			System.exit(0);
			return -1;
		}
			
	}
	public int getRightRoadId(int roadId) {
		int tempIndex = -1;
		for(int i = 0;i<4;i++) {
			if(roadIds.get(i) == roadId) {
				tempIndex = i;
			}
		}
		if(tempIndex == 0) 
			return roadIds.get(1);
		else if(tempIndex == 1)
			return roadIds.get(2);
		else if(tempIndex == 2)
			return roadIds.get(3);
		else if(tempIndex == 3)
			return roadIds.get(0);
		else {
			System.out.println("优先级判定出错");
			System.exit(0);
			return -1;
		}
			
	}
	public int getId() {
		return id;
	}
	public void setCross2Road(Map<Integer, Integer> cross2Road) {
		this.cross2Road = cross2Road;
	}
	public int getRoadId(int crossId) {
		// TODO Auto-generated method stub
		if(!cross2Road.containsKey(crossId)) {
			
			System.out.println("没有这条路");
			return -1;
		}
		return cross2Road.get(crossId);
		
	}
	public void setNeighborCrossValue(Map<Integer, Double> neighborCrossValue) {
		this.neighborCrossValue = neighborCrossValue;
	}
	public double getNeighborCrossValue(int crossId) {
		return neighborCrossValue.get(crossId);
	}
	public void setNeighborCrossIds(ArrayList<Integer> neighborCrossIds) {
		this.neighborCrossIds = neighborCrossIds;
	}
	public ArrayList<Integer> getNeighborCrossIds() {
		return neighborCrossIds;
	}
	public void setNeighborCrossNewValue(Map<Integer, Double> neighborCrossNewValue) {
		this.neighborCrossNewValue = neighborCrossNewValue;
	}
	public double getNeighborCrossNewValue(int crossId) {
		return neighborCrossNewValue.get(crossId);
	}
	public void setValue(double value) {
		this.value = value;
	}
	public double getValue() {
		return value;
	}
	public Cross clone() {
		Cross o = null;
		try { 
		    o = (Cross) super.clone();
		    o.startCrossCarIds = new ArrayList<Integer>();
		    for(int carId  : this.startCrossCarIds) {
		    	o.startCrossCarIds.add(carId);
		    }
		    
		} catch (CloneNotSupportedException e) {
		    System.out.println(e.toString());
		}
		return o;
	}
	public void setNewValue(int beforeCrossId,double value) {
		// TODO Auto-generated method stub
		neighborCrossNewValue.put(beforeCrossId,value);
		
	}
}