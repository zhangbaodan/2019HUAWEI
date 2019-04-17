package main.java.com.huawei.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Car implements Comparable<Car>,Cloneable{
	private int id;
	private int startCross;
	private int endCross;
	private int maxSpeed;
	private int realTime;
	private int allowStartTime;
	private boolean isFatherCar;
	private int roadId; //道路编号
	private int laneId;//车道编号
	private int position = -1;//距离路口位置
	private int nextCross;
	private int routeLength = -1; 
	private int flag = -1;
	private double predictTotalTime = -1;
	private ArrayList<Integer>  routes;
	private ArrayList<Integer>	 allRoutes;
	private boolean isPriority = false;
	private boolean isPreset = false;
	private boolean changedRoad = false;
	private boolean isPassRoad = false;
	private int planPathTime = 0;
	private int endTime= -1;
	 

	private Map<Integer, Double> predictRoad2EndTime; //用于统计在当前道路下，不考虑冲突情况下，大概需要花多长时间到达终点
	

	
	public Car(int id,int startCross,int endCross,int maxSpeed, int allowTime,int priority,int preset) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.startCross = startCross;
		this.endCross = endCross;
		this.maxSpeed = maxSpeed;	
		this.allowStartTime = allowTime;
		
		if(priority == 1)
			this.isPriority = true;
		if(preset == 1)
			this.isPreset = true;
		
		this.predictRoad2EndTime = new HashMap<Integer, Double>();
	}
	public Car(int id,int startCross,int endCross,int maxSpeed, int allowTime) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.startCross = startCross;
		this.endCross = endCross;
		this.maxSpeed = maxSpeed;	
		this.allowStartTime = allowTime;
		

		this.isPriority = false;

		this.isPreset = false;
		
		this.predictRoad2EndTime = new HashMap<Integer, Double>();
	}

	public void setPredictTotalTime(double predictTotalTime) {
		this.predictTotalTime = predictTotalTime;
	}
	public void setPredictRoad2EndTime(Map<Integer, Double> predictRoad2EndTime) {
		this.predictRoad2EndTime = predictRoad2EndTime;
		
	}
	public Map<Integer, Double> getPredictRoad2EndTime() {
		return predictRoad2EndTime;
	}


	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getFlag() {
		return flag;
	}
	public double getPredictTotalTime() {
		return predictTotalTime;
	}
	
	public void resetAllRoutes(int roadId) {
		ArrayList<Integer> newRoads = new ArrayList<Integer>();
		for(int id : allRoutes) {
			
			if(roadId == id) {
				break;
			}
			newRoads.add(id);
		}
		
		for(int id : routes) {
			
			newRoads.add(id);
		}
		allRoutes = newRoads;
	}


	public void setLaneId(int laneId) {
		this.laneId = laneId;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	public int getId() {
		return id;
	}
	public int getRoadId() {

		return roadId;
	}
	public void setRoadId(int roadId) {
		if(routes.size()==0) {
			System.out.println("已经没有需要跳跃的道路了......");
		}
		else {
			if(routes.get(0) == roadId) {
				routes.remove(0);
			}
		}
		this.roadId = roadId;
	}
	public int getLaneId() {
		return laneId;
	}
	public int getEndCross() {
		return endCross;
	}
	public int getMaxSpeed() {
		return maxSpeed;
	}
	public int getStartCross() {
		return startCross;
	}
	public int getRealTime() {
		return realTime;
	}
	
	public void setRoutes(ArrayList<Integer> routes) {
		this.routes = new ArrayList<Integer>();
		for(int i = 0;i<routes.size();i++) {
			this.routes.add(routes.get(i));
		}
	}
	
	public void setAllRoutes() {
		if(allRoutes == null) {
			allRoutes = new ArrayList<Integer>();
			for(int roadId : routes) {
				allRoutes.add(roadId);
			}
		}
		
	}
	public ArrayList<Integer> getAllRoutes() {
		return allRoutes;
	}
	public ArrayList<Integer> getRoutes() {
		return routes;
	}
	
	public int getNextRoute() {
		if(routes == null) {
			System.out.println("没有做路径规划");
			System.out.println("carId : " + id + " isPreset :" + isPreset );
		}
		if(routes.size() == 0) {

			return -1;
		}
		int temp = routes.get(0);
//		routes.remove(0);
//		System.out.println("-----------------------" +temp);
		return temp;
	}

	public boolean mightPassCross(int roadMaxSpeed) {
		int realSpeed = Math.min(roadMaxSpeed, maxSpeed);
		if(realSpeed > position) {
			return true;
		}
		return false;
	}
	public void setPlanPathTime(int planPathTime) {
		this.planPathTime = planPathTime;
	}
	public int getPlanPathTime() {
		return planPathTime;
	}
	
	public int getPosition() {
		return position;
	}
	
	public boolean isArrived(int roadMaxSpeed) {
		int realSpeed = Math.min(roadMaxSpeed, maxSpeed);
		if(routes.size()==0) { //没有需要到达的终点了
			if(endCross == nextCross && position < realSpeed) {
				return true;
			}
		}
		
		return false; 
	}

	public void setNextCross(int nextCross) {
		this.nextCross = nextCross;
	}
	public void setRealTime(int realTime) {
		
		this.realTime = realTime;
	}
	public void updateInNewRoad(int roadId, int laneId, int position, int nextCross) { //到达新的路以后所需要做的更新

		this.roadId = roadId;
		if(routes.size() == 0) {
			System.out.println("更新道路时候，原来道路列表里面是空的...........");
		}
		if(roadId == routes.get(0)) {
			routes.remove(0);
		}
		this.laneId = laneId;
		this.position = position;
		this.nextCross = nextCross;
		
		
	}
	public int getNextCross() {
		return nextCross;
	}

	public void setRouteLength(int routeLength) {
		this.routeLength = routeLength;
	}
	public int getRouteLength() {
		return routeLength;
	}

	public int getFirstRoadId(){
		
		return routes.get(0);
	}
	@Override
	public int compareTo(Car o) {
		// TODO Auto-generated method stub
		if(this.realTime != o.realTime)
			return this.realTime - o.realTime;
		else
			return this.id - o.id;
	
	
	}
	public void setFatherCar(boolean isFatherCar) {
		this.isFatherCar = isFatherCar;
	}
	public boolean isFatherCar() {
		return isFatherCar;
	}
	
	public void clearRoutes() {
		this.routes.clear();
	}
	public int getAllowStartTime() {
		return allowStartTime;
	}
	public void setStartCross(int startCross) {
		this.startCross = startCross;
	}
	public void setChangedRoad(boolean changedRoad) {
		this.changedRoad = changedRoad;
	}
	public boolean isChangedRoad() {
		return changedRoad;
	}
	public Car clone() {
		Car o = null;
		try { 
		    o = (Car) super.clone();
		    if(allRoutes!=null) {
			    o.routes = new ArrayList<Integer>();
			    for(int roadId: routes) {
			    	o.routes.add(roadId);
			    }
		    }
		    if(allRoutes!=null) {
			    o.allRoutes = new ArrayList<Integer>();
			    for(int roadId: allRoutes) {
			    	o.allRoutes.add(roadId);
			    }
		    }
//		    = (ArrayList<Integer>) routes.clone();
		    
		} catch (CloneNotSupportedException e) {
		    System.out.println(e.toString());
		}
		return o;
	}
	public boolean isPassRoad() {
		return isPassRoad;
	}

	public void setPassRoad(boolean isPassRoad) {
		this.isPassRoad = isPassRoad;
	}
	public boolean isPreset() {
		return isPreset;
	}
	public boolean isPriority() {
		return isPriority;
	}
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	public int getEndTime() {
		return endTime;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Id : "+id+ "   falg : "+ flag + "    realTime:" + realTime + "  endTime : " + endTime + 
				"   maxSpeed :" + maxSpeed + 
				" pos :" + position + " startCrossId :" + startCross +
				" isPriority :" + isPriority + " isPreset : " + isPreset;
	}
}
