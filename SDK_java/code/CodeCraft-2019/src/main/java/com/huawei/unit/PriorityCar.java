package main.java.com.huawei.unit;

public class PriorityCar implements Comparable<PriorityCar>{
	private int carId;
	private int pos;
	private int lane;
	public PriorityCar(int carId, int pos, int lane) {
		// TODO Auto-generated constructor stub
		this.carId = carId;
		this.pos = pos;
		this.lane = lane;
				
	}
	public int getCarId() {
		return carId;
	}
	public int getLane() {
		return lane;
	}
	public int getPos() {
		return pos;
	}
	@Override
	public int compareTo(PriorityCar o) {
		// TODO Auto-generated method stub
		if(this.pos != o.pos) {
			return this.pos - o.pos;
		}	
		else {
			if(this.lane != o.lane) {
				return this.lane - o.lane;
			}
			else {
				System.out.println("?");
				System.exit(0);
				return -1;
			}
		}
	
		
	}
}
