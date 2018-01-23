package com.kalibrasi.vms;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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

	@Autowired
	Environment env;

	RethinkDB r = RethinkDB.r;

	@RequestMapping(value="/api/enrollment", method=RequestMethod.POST)
	public void enrollment(@RequestParam("file") MultipartFile file, @RequestParam("data") String data) {
		
		System.out.println(file.getName());		
		System.out.println(data);
		
		//r.db("vms_mock").table("visitor").insert(data).run(db.getConnection());
	}
	
	@RequestMapping(value="/api/update", method=RequestMethod.POST)
	public void update(@RequestBody Map data) {
		r.db("vms_mock").table("visitor").replace(data).run(db.getConnection());
	}
	
	@RequestMapping("/api/visitor")
	public Object getVisitor() {
		return r.db("vms_mock").table("visitor").orderBy(r.asc("name")).run(db.getConnection());	
	}
	
}
