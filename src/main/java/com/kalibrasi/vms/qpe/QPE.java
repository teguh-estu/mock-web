package com.kalibrasi.vms.qpe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.exec.util.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController	
public class QPE {
	private Tags tags = new Tags();
	private Map<String, Boolean> movement = new HashMap<String, Boolean>();
    
	   @RequestMapping("/qpe/getTagPosition")
	  	public Tags getTags() {
			tags.setCode("0");
			tags.setCommand("getPosition");
			tags.setResponseTS(System.currentTimeMillis());
			tags.setStatus("ok");
			tags.setVersion("2.1");
	  		return tags;
	  		
	  	}
		
	    
	    @RequestMapping(value = "/qpe/checkin", method = RequestMethod.POST)
	    public void checkin(@RequestBody Map data) throws Exception {
	    	if (!data.containsKey("tagId")) throw new Exception("tagId not exist");
			if (!data.containsKey("building")) throw new Exception("building not exist");
			removeTag((String) data.get("tagId")); 
			
			addTag((String) data.get("tagId"), (String) data.get("building"), (String) data.get("floor")); 
			
	    }
	    
	    @RequestMapping(value = "/qpe/checkout", method = RequestMethod.POST)
	    public void checkout(@RequestBody Map data) throws Exception {
	    	System.out.println(data);
	    	//if (!data.containsKey("tagId")) throw new Exception("tagId not exist");
			removeTag((String) data.get("tagId")); 
			
	    }
	    
	    @RequestMapping(value = "/qpe/move", method = RequestMethod.POST)
	    public void move(@RequestBody Map data) throws Exception {
	    	System.out.println(data);
	    	if (!data.containsKey("tagId")) throw new Exception("tagId not exist");
	    	if (!data.containsKey("movement")) throw new Exception("move not exist");
			movement.put((String)data.get("tagId"), (Boolean)data.get("movement"));
			
	    }
	    
	    private void removeTag(String tagId) {
	    	Tag tag = new Tag();
	    	tag.setId(tagId);
	    	
	    	synchronized("tag") {
	    		tags.getTags().remove(tag);
	    	}
	    }
	    
		private void addTag(String tagId, String building, String floor) {
			Tag tag = new Tag();
			tag.setAreaId("TrackingArea1");
			tag.setAreaName("KCM");
			
			if (floor == null || "".equals(floor)) floor = "1";//String.valueOf(getIntRandom(1, 5));
			
			tag.setCoordinateSystemId("coordinateSystem1");
			tag.setCoordinateSystemName(building + "_" + floor);
			
			Double x = getDoubleRandom(0, 10);
			Double y = getDoubleRandom(0, 10);
			
			List<Double> covariance = new ArrayList<Double>();
			covariance.add(5d);
			covariance.add(5d);
			covariance.add(5d);
			covariance.add(5d);
			tag.setCovarianceMatrix(covariance);
			
			List<Double> position = new CopyOnWriteArrayList<Double>();
			position.add(x);
			position.add(y);
			position.add(0d);
			
			tag.setPosition(position);
			tag.setSmoothedPosition(position);
			tag.setPositionAccuracy(1d);
			tag.setPositionTs(System.currentTimeMillis());
			tag.setId(tagId);
			tag.setName(tagId);
			
			List<Map<String,String>> zoneList = new ArrayList<Map<String, String>>();
			Map<String, String> zoneMap = new HashMap<String, String>();
			zoneMap.put("id", "0001");
			zoneMap.put("name", "Zone001");
			zoneList.add(zoneMap);
			tag.setZones(zoneList);
			synchronized("tag") {
				tags.getTags().add(tag);
			}
		}
		
		private static int getIntRandom(int min, int max) {
			return new Random().nextInt(max - min) + min;
		}
		
		private static double getDoubleRandom(double min, double max) {
			Random r = new Random();
			return (min + (max - min) * r.nextDouble());
		}

		@Scheduled(fixedRate=5000)
		public void modifyPosition() {
			
			//log.info("Start movind data");
			tags.setResponseTS(System.currentTimeMillis());
			List<Tag> tagList = tags.getTags();
			for (Tag tag: tagList) {
				Object move = movement.get((String) tag.getId());
				if (move == null || (Boolean) move) {
					tag.setPositionTs(System.currentTimeMillis());
					getNewPosition(tag.getSmoothedPosition(), tag.getCoordinateSystemName().split("_")[0]);
				}
			}
			//log.info("End moving data");
		}
		
		private static void getNewPosition(List<Double> oldPosition, String building) {
			List<Double> newPos = new ArrayList<Double>();
			
			Double x = oldPosition.get(0);
			Double y = oldPosition.get(1);
			Double z = oldPosition.get(2);
			double maxMovementPerSecond = 8;
			double movX = getDoubleRandom(-1, 1) * maxMovementPerSecond;
			double movY= getDoubleRandom(-1, 1) * maxMovementPerSecond;
			
			//Map<String, Double> mp = buildingData.get(building);
			Double maxX = 20d;//mp.get("x");
			Double maxY = 20d;// mp.get("y");
			
			
			if (x + movX <= maxX) {
				oldPosition.set(0, Math.abs(x + movX));
			} else {
				oldPosition.set(0,Math.abs(x - movX));
			}
			
			if (y + movY <= maxY) {
				oldPosition.set(1,Math.abs(y + movY));
			} else {
				oldPosition.set(1,Math.abs(y - movY));
			}
			
			
		}
		
		public class Tags {
			private String code;
			private String command;
			private String status;
			private Long responseTS;
			
			private List<Tag> tags = new ArrayList<Tag>();
			private String version;
			public String getCode() {
				return code;
			}
			public void setCode(String code) {
				this.code = code;
			}
			public String getCommand() {
				return command;
			}
			public void setCommand(String command) {
				this.command = command;
			}
			public String getStatus() {
				return status;
			}
			public void setStatus(String status) {
				this.status = status;
			}
			public Long getResponseTS() {
				return responseTS;
			}
			public void setResponseTS(Long responseTS) {
				this.responseTS = responseTS;
			}
			public List<Tag> getTags() {
				return tags;
			}
			public void setTags(List<Tag> tags) {
				this.tags = tags;
			}
			public String getVersion() {
				return version;
			}
			public void setVersion(String version) {
				this.version = version;
			}
			
			
			
		}
		
		public class Tag {
			/*"\"areaId\": \"TrackingArea1\",\n" +
								 "\"areaName\": \"KCM\",\n" +
								 "\"color\": \"#0000CC\",\n" +
								 "\"coordinateSystemId\": \"CoordinateSystem1\",\n" +
								 "\"coordinateSystemName\": \"A_" + ((i%4)+1) + "\",\n" +
								 "\"covarianceMatrix\": [\n" +
								 "8.9,\n" +
								 "-3.09,\n" +
								 "-3.09,\n" +
								 "5.56\n" +
								 "],\n" +
								 "\"id\": \""+tagId+"\",\n" +
								 "\"name\": \"Basket_0068\",\n" +
								 "\"position\": [\n" +
								 "4.85,\n" +
								 "26.36,\n" +
								 "0.8\n" +
								 "],\n" +
								 "\"positionAccuracy\": 1.47,\n" +
								 "\"positionTS\": 1430140978241,\n" +
								 "\"smoothedPosition\": [\n" +
								 "4.85,26.36,\n" +
								 "0.8\n" +
								 "],\n" +
								 "\"zones\": [{\n" +
								 "\"id\": \"Zone005\",\n" +
								 "\"name\": \"cashier\"\n" +
								 "}]\n" +
		*/
			private String areaId;
			private String areaName;
			private String coordinateSystemId;
			
			private String coordinateSystemName;
			private List<Double> covarianceMatrix;
			private String id;
			private String name;
			private List<Double> position;
			private Double positionAccuracy;
			private List<Double> smoothedPosition;
			private Long positionTs;
			private List<Map<String, String>> zones;
			public String getAreaId() {
				return areaId;
			}
			public void setAreaId(String areaId) {
				this.areaId = areaId;
			}
			public String getAreaName() {
				return areaName;
			}
			public void setAreaName(String areaName) {
				this.areaName = areaName;
			}
			public String getCoordinateSystemId() {
				return coordinateSystemId;
			}
			public void setCoordinateSystemId(String coordinateSystemId) {
				this.coordinateSystemId = coordinateSystemId;
			}
			public String getCoordinateSystemName() {
				return coordinateSystemName;
			}
			public void setCoordinateSystemName(String coordinateSystemName) {
				this.coordinateSystemName = coordinateSystemName;
			}
			public List<Double> getCovarianceMatrix() {
				return covarianceMatrix;
			}
			public void setCovarianceMatrix(List<Double> covarianceMatrix) {
				this.covarianceMatrix = covarianceMatrix;
			}
			public String getId() {
				return id;
			}
			public void setId(String id) {
				this.id = id;
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public List<Double> getPosition() {
				return position;
			}
			public void setPosition(List<Double> position) {
				this.position = position;
			}
			public Double getPositionAccuracy() {
				return positionAccuracy;
			}
			public void setPositionAccuracy(Double positionAccuracy) {
				this.positionAccuracy = positionAccuracy;
			}
			public List<Double> getSmoothedPosition() {
				return smoothedPosition;
			}
			public void setSmoothedPosition(List<Double> smoothedPosition) {
				this.smoothedPosition = smoothedPosition;
			}
			public Long getPositionTs() {
				return positionTs;
			}
			public void setPositionTs(Long positionTs) {
				this.positionTs = positionTs;
			}
			public List<Map<String,String>> getZones() {
				return zones;
			}
			public void setZones(List<Map<String, String>> zones) {
				this.zones = zones;
			}
			private QPE getOuterType() {
				return QPE.this;
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((id == null) ? 0 : id.hashCode());
				return result;
			}
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Tag other = (Tag) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (id == null) {
					if (other.id != null)
						return false;
				} else if (!id.equals(other.id))
					return false;
				return true;
			}
			
		}
		

}
