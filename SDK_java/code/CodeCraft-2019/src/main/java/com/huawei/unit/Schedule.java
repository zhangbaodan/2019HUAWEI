package main.java.com.huawei.unit;

public class Schedule implements Comparable<Schedule>,Cloneable{
	private int carId;
	private int realTime;
	private int allowTime;
	private boolean isStart;
	public Schedule(int carId, int realTime) {
		// TODO Auto-generated constructor stub
		this.carId = carId;
		this.realTime = realTime;
		this.isStart =false;
	}
	
	
	@Override
	public int compareTo(Schedule o) {
		// TODO Auto-generated method stub
		if(this.realTime != o.realTime) {
			return this.realTime - o.realTime;
		}	
		return this.carId - o.carId;
	}
	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}
	public boolean isStart() {
		return isStart;
	}
	public int getCarId() {
		return carId;
	}
	public int getRealTime() {
		return realTime;
	}
	public void setRealTime(int realTime) {
		this.realTime = realTime;
	}
	public void increaserealTime(int delayTime) {
		this.realTime+= delayTime;
	}
	public Object clone() {
		Schedule o = null;
		try { 
		    o = (Schedule) super.clone();
		} catch (CloneNotSupportedException e) {
		    System.out.println(e.toString());
		}
		return o;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "carId : " + carId + "       realTime : " + realTime;
	}
	
}