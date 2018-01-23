package com.kalibrasi.vms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

@Service
public class Database {
	
	@Autowired
	private Environment env;
	Connection connection;

	public Connection getConnection() {
		
		if (connection != null && connection.isOpen()) return connection;
		
		String host = System.getenv("DB_HOST") != null? System.getenv("DB_HOST"): env.getProperty("db.host");
		int port = Integer.parseInt(System.getenv("DB_PORT") != null? System.getenv("DB_PORT"): env.getProperty("db.port"));
		
		connection = RethinkDB.r.connection().hostname(host).port(port).connect();
		return connection;
		
	}
	
	
	
	
}
