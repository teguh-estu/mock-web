package com.kalibrasi.vms;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Cursor;

import org.apache.http.client.HttpClient;

@SpringBootApplication
@RestController
@Configuration
public class Application extends WebMvcConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}
	
	@Autowired
	Database db;

	//@Autowired
	//Environment env;

	RethinkDB r = RethinkDB.r;

	@RequestMapping(value="/api/enrollment", method=RequestMethod.POST)
	public void enrollment(@RequestParam("file") MultipartFile file, @RequestParam("data") String data) throws Exception {
		
		File hostedFile = new File(file.getOriginalFilename());
		hostedFile.createNewFile(); 
	    FileOutputStream fos = new FileOutputStream(new File("www/img", hostedFile.getName())); 
	    fos.write(file.getBytes());
	    fos.close(); 

	    Map map = new Gson().fromJson(data, Map.class);
	    map.put("imageUrl", "img/" + hostedFile.getName());
	    map.put("onVisit", false);
		
		r.db("vms_mock").table("visitor").insert(map).run(db.getConnection());
	}
	
	@RequestMapping(value="/api/update", method=RequestMethod.POST)
	public void update(@RequestBody Map data) throws Exception {
		System.out.println(new Gson().toJson(data));
		r.db("vms_mock").table("visitor").filter(r.hashMap("visitorId", data.get("visitorId"))).update(data).run(db.getConnection());
		
		//begin:added by WK, 20180125 to call api checkin		
		Map visitorMap = new HashMap();
		Cursor<HashMap> visitors = r.db("vms_mock").table("visitor").filter(r.hashMap("visitorId", data.get("visitorId"))).run(db.getConnection());
		while (visitors.hasNext()) {
		    System.out.println(">>> found document!!");
		    visitorMap = visitors.next();
		}
	    
		Map sentMap = new HashMap();
		sentMap.put("visitorId", visitorMap.get("visitorId"));
		sentMap.put("tagId", visitorMap.get("tagId"));
		sentMap.put("purpose", visitorMap.get("purpose"));		
		sentMap.put("name", visitorMap.get("name"));
		
		//InetAddress addr = InetAddress.getLocalHost();
		String localhost = "localhost"; //addr.getHostName();
		StringBuilder photoUrl = new StringBuilder();
		photoUrl.append("http://");
		photoUrl.append(localhost);
		photoUrl.append(":");
		//photoUrl.append(env.getProperty("server.port"));
		photoUrl.append("8081");
		photoUrl.append("/");
		photoUrl.append(visitorMap.get("imageUrl"));
		sentMap.put("photoUrl", photoUrl.toString());
		  
		sentMap.put("checkinTime", new Date().getTime()); 
		//sentMap.put("type", visitorMap.get("isVip") ? "VIP" : ""); 
		sentMap.put("type", ""); 
		sentMap.put("destination", visitorMap.get("destination"));
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost("http://localhost/api/checkin");
	    request.addHeader("content-type", "application/json");
	    request.setEntity(new StringEntity(new Gson().toJson(sentMap)));
	    httpClient.execute(request);		
		System.out.println("call rtls api/checkin succeed!");
		//end
	}
	
	@RequestMapping("/api/visitor")
	public Object getVisitor() {
		return r.db("vms_mock").table("visitor").orderBy(r.asc("name")).run(db.getConnection());	
	}
	
}
