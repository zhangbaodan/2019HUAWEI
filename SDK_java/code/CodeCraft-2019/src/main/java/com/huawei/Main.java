package main.java.com.huawei;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import javax.sound.midi.Synthesizer;



import main.java.com.huawei.system.PathPlanning;
import main.java.com.huawei.system.Simulation;
import main.java.com.huawei.unit.Car;
import main.java.com.huawei.unit.ReadFile;
import main.java.com.huawei.unit.Road;
import main.java.com.huawei.unit.Schedule;
public class Main {
    public static void main(String[] args)
    {
     	
    	 if (args.length != 5) {
 
             return;
         }
         String carPath = args[0];
         String roadPath = args[1];
         String crossPath = args[2];
         String presetAnswerPath = args[3];
         String answerPath = args[4];
         
        //速度分析以及路径规划系统
        PathPlanning pathPlanning = new PathPlanning(roadPath, carPath, crossPath,presetAnswerPath);
        
        //仿真系统:动态路径规划+动态车辆出发时间设定
		Simulation simulation = new Simulation(pathPlanning.getCarMap(), pathPlanning.getRoadMap(),
				pathPlanning.getCrossMap(),pathPlanning.getMaxSpeedNormalCarIdsMap(),pathPlanning.getPriorityCarAllowTime(),
				pathPlanning.getProwlCount());			
		//获得车辆运行结果
		Map<Integer, Car> carMap = simulation.getCarMap();
				
		//写入answer文件
	    ReadFile.writeAnswer(answerPath,carMap);

	    
  	}
  
}