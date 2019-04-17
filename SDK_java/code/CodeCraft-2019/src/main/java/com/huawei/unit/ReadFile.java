package main.java.com.huawei.unit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.plaf.synth.SynthScrollBarUI;
public class ReadFile {
	private String carPath;
	private String roadPath;
	private String crossPath;
	private String presetAnswerPath;
	private String answerPath;
	private Map<Integer,Car> carMap;
	private Map<Integer,Road> roadMap;
	private TreeMap<Integer,Cross> crossMap;

	public ReadFile(String carPath, String roadPath, String crossPath,String answerPath) {
		// TODO Auto-generated constructor stub		
		this.carPath = carPath; 
		this.roadPath = roadPath;
		this.crossPath = crossPath;

		this.answerPath = answerPath;
		setCarMap();
		setCrossMap();
		setRoadMap();
		setAnswer();
		
		
	}
	public ReadFile(String carPath, String roadPath, String crossPath,String answerPath,String presetAnswerPath) {
		// TODO Auto-generated constructor stub		
		this.carPath = carPath; 
		this.roadPath = roadPath;
		this.crossPath = crossPath;
		this.presetAnswerPath = presetAnswerPath;
		this.answerPath = answerPath;
		setCarMap();
		setCrossMap();
		setRoadMap();
		setAnswer();
		setPresetAnswer();
	}
	
	public ReadFile() {
		// TODO Auto-generated constructor stub
	}
	public void analysisAnswer() {
		Map<Integer, ArrayList<Integer>> realTimeCarIds = new TreeMap<Integer, ArrayList<Integer>>();
		Map<Integer, ArrayList<Integer>> startCrossCarIds = new TreeMap<Integer, ArrayList<Integer>>();
		for(int carId : carMap.keySet()) {
			int realTime = carMap.get(carId).getRealTime();
			if(realTimeCarIds.containsKey(realTime)) {
				realTimeCarIds.get(realTime).add(carId);
				
			}
			else {
				realTimeCarIds.put(realTime, new ArrayList<Integer>());
				realTimeCarIds.get(realTime).add(carId);
			}
			
			int startCross = carMap.get(carId).getStartCross();
			if(startCrossCarIds.containsKey(startCross)) {
				startCrossCarIds.get(startCross).add(carId);
				
			}
			else {
				startCrossCarIds.put(startCross, new ArrayList<Integer>());
				startCrossCarIds.get(startCross).add(carId);
			}
			
		}
		
		for(int realTime : realTimeCarIds.keySet()) {
			ArrayList<Integer> carIds = realTimeCarIds.get(realTime);
			Set<Integer> crossSet = new HashSet<Integer>();
			int[] crossSum = new int[65];
			 for(int carId : carIds) {
				 crossSum[carMap.get(carId).getStartCross()]++;
				 crossSet.add(carMap.get(carId).getStartCross());
			 }
			 System.out.println("realTime :" +realTime + "   sum : "+carIds.size() + " crossSum :" + crossSet.size());
			 for(int i = 1;i<65;i++) {
				 System.out.print(crossSum[i] + " ");
			 }
			 System.out.println();
		}
		for(int startCross : startCrossCarIds.keySet()) {
			ArrayList<Integer> carIds = startCrossCarIds.get(startCross);
			System.out.println("*********************************************");
			System.out.println("startCross :" + startCross );
			Map<Integer, ArrayList<Integer>> crossrealTimeCarIds = new TreeMap<Integer, ArrayList<Integer>>();
			
			for(int carId : carIds) {
				int realTime = carMap.get(carId).getRealTime();
				if(crossrealTimeCarIds.containsKey(realTime)) {
					crossrealTimeCarIds.get(realTime).add(carId);
					
				}
				else {
					crossrealTimeCarIds.put(realTime, new ArrayList<Integer>());
					crossrealTimeCarIds.get(realTime).add(carId);
				}
			}
//			System.out.println("同一个cross下面，个体的速度:");
//			for(int realTime : crossrealTimeCarIds.keySet()) {
//				ArrayList<Integer> crossCarIds = crossrealTimeCarIds.get(realTime);
//				
//				
//				
//				
//				System.out.println("realTime : " + realTime + " ");
//				for(int carId : crossCarIds) {
//					System.out.print(" maxSpeed : " + carMap.get(carId).getMaxSpeed()+ "   "+carId);
//				}
//				System.out.println();
//			}
//			System.out.println("*********************************************");
		}
	}
	private void setPresetAnswer() {
		ArrayList<ArrayList<Integer>> datas = getDatas(presetAnswerPath);
		System.out.println(":"+datas.size());
		for(ArrayList<Integer> data : datas) {
		
			int id = data.get(0);
			if(carMap.get(id).isPreset() == false) {
				System.out.println("nani?");
				System.exit(0);
			}
			int realTime = data.get(1);
			ArrayList<Integer> routes = new ArrayList<Integer>();
			for(int i = 2;i<data.size();i++) {
				routes.add(data.get(i));
			}
			
			Car car  = carMap.get(id);
			
			car.setRoutes(routes);
			car.setAllRoutes();
			car.setRealTime(realTime);
				
		}
	}
	private void setAnswer() {
		ArrayList<ArrayList<Integer>> datas = getDatas(answerPath);
		System.out.println(":"+datas.size());
		for(ArrayList<Integer> data : datas) {
		
			int id = data.get(0);

			int realTime = data.get(1);
			ArrayList<Integer> routes = new ArrayList<Integer>();
			for(int i = 2;i<data.size();i++) {
				routes.add(data.get(i));
			}
			
			Car car  = carMap.get(id);
			
			car.setRoutes(routes);
			car.setAllRoutes();
			car.setRealTime(realTime);
				
		}
	}
	public ArrayList<ArrayList<Integer>> getDatas(String path) {	
		System.out.println(path);	
		ArrayList<String> lines = new ArrayList<String>();
        //打开文件  
        try {	          	 
            InputStream instream = new FileInputStream(path);   
            if (instream != null)   
            {           	
                InputStreamReader inputreader = new InputStreamReader(instream);  
                BufferedReader buffreader = new BufferedReader(inputreader);  
                String line;  
                //分行读取  
                while (( line = buffreader.readLine()) != null) {   
              	  lines.add(line+"\n");
                }                  
                instream.close();  
            }  
        }  
        catch (java.io.FileNotFoundException e) {  
            System.out.println("The File doesn't not exist.");  
        }   
        catch (IOException e) {
      	    System.out.println("Io error."); 
        } 
        
        ArrayList<ArrayList<Integer>> datas = new ArrayList<ArrayList<Integer>>();
        for(String line : lines) {
        	if(line.contains("#"))
        		continue;
        	String[] temp = line.replace("(", "").replace(")", "").trim().split(",");
        	
        	ArrayList<Integer> temp1 = new ArrayList<Integer>();
        	for(String a : temp) {
        	
        		temp1.add(Integer.parseInt(a.trim()));
        	}
        	datas.add(temp1);
        }
        return datas;
	}
	private void setCarMap() {
		ArrayList<ArrayList<Integer>> datas = getDatas(carPath);
		carMap = new HashMap<Integer, Car>();
		for(ArrayList<Integer> data : datas) {
			int id = data.get(0);
			if(data.size() == 7) {
			Car car = new Car(data.get(0), data.get(1), data.get(2), 
					data.get(3), data.get(4),data.get(5),data.get(6));
			
			carMap.put(id, car);
			}
			else {
				Car car = new Car(data.get(0), data.get(1), data.get(2), 
						data.get(3), data.get(4));
				
				carMap.put(id, car);
				
			}
		}
	}
	private void setRoadMap() {
		ArrayList<ArrayList<Integer>> datas = getDatas(roadPath);
		roadMap = new HashMap<Integer, Road>();
		for(ArrayList<Integer> data : datas) {
			int id = data.get(0);
			Road road = new Road(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5), data.get(6));
			roadMap.put(id, road);
		}
	}

	private void setCrossMap() {
		ArrayList<ArrayList<Integer>> datas = getDatas(crossPath);
		crossMap = new TreeMap<Integer, Cross>();
		for(ArrayList<Integer> data : datas) {
			int id = data.get(0);
			Cross cross = new Cross(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4));
			crossMap.put(id, cross);
		}
	}
	
	 public static void writeAnswer(String answerPath,Map<Integer, Car> carMap) {
	        try {
	        	String answer = "";
	            File writeName = new File(answerPath); 
	            writeName.createNewFile(); 
	            try (FileWriter writer = new FileWriter(writeName);
	                 BufferedWriter out = new BufferedWriter(writer)
	            ) {
	            	for(int carId : carMap.keySet()) {
	            		if(carMap.get(carId).isPreset() == true) {
	            			continue;
	            		}
	            		
	            		int realTime = carMap.get(carId).getRealTime();
	            		answer = "(" + carId + "," + realTime ;
	            		
	            		ArrayList<Integer> routes = carMap.get(carId).getAllRoutes();
	            		
	            		for(int roadId : routes) {
	            			answer += ", " + roadId;
	            		}
	            		answer+=")";
	            	
	            		out.write(answer + "\r\n"); 
	            	}
	                out.flush(); 
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	 public static void writePriorityCarTime(String answerPath,Map<Integer, Car> carMap) {
	        try {
	        	String answer = "";
	            File writeName = new File(answerPath); 
	            writeName.createNewFile(); 
	            try (FileWriter writer = new FileWriter(writeName);
	                 BufferedWriter out = new BufferedWriter(writer)
	            ) {
	            	for(int carId : carMap.keySet()) {
	            		
	            		if(carMap.get(carId).isPriority()== false) {
	            			continue;
	            		}
	            		
	            		int realTime = carMap.get(carId).getRealTime();
	            		answer = "(" + carId + "," + realTime +" ," + carMap.get(carId).getEndTime() +" )";
	            		
	            	
	     
	            		out.write(answer + "\r\n"); 
	            	}
	                out.flush(); 
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	public Map<Integer, Car> getCarMap() {
		return carMap;
	}
	public TreeMap<Integer, Cross> getCrossMap() {
		return crossMap;
	}
	public Map<Integer, Road> getRoadMap() {
		return roadMap;
	}
}
