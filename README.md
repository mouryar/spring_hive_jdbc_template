# A Simple Spring Micro service to query hive using JdbcTemplate

### Create a simple spring boot app in Spring Tool Suite IDE or Eclipse IDE. 
### Add bellow dependencies to the pom.xml 
```
<dependency>
			<groupId>org.mortbay.jetty</groupId>
			<artifactId>jetty</artifactId>
			<version>6.1.26</version>
			<exclusions>
				<exclusion>
					<groupId>org.mortbay.jetty</groupId>
					<artifactId>servlet-api</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
		<dependency>
			<groupId>org.apache.hive</groupId>
			<artifactId>hive-jdbc</artifactId>
			<version>1.2.1000.2.4.2.12-1</version>
			<exclusions>
				<exclusion>
					<artifactId>slf4j-log4j12</artifactId>
					<groupId>org.slf4j</groupId>
				</exclusion>
				<exclusion>
					<artifactId>log4j</artifactId>
					<groupId>log4j</groupId>
				</exclusion>
				<exclusion>
					<groupId>javax.servlet</groupId>
					<artifactId>servlet-api</artifactId>
				</exclusion>
				<exclusion>
					<groupId>org.eclipse.jetty.aggregate</groupId>
					<artifactId>jetty-all</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-jdbc</artifactId>
		</dependency>
```
### Add Horton works repo to the pom.xml
```
<repositories>
		<repository>
			<id>hortonworks</id>
			<name>Horton works Repo</name>
			<url>http://repo.hortonworks.com/content/repositories/releases/</url>
		</repository>
	</repositories>
```
### Add properties in application.properties or application.yml in src/main/resources folder as shown bellow.
 
  **mourya.hive.connectionURL**:  Hive connection string. We need to add principal to connection string if Kerberos authentication is enabled.   
  **mourya.hive.usename**: hive database username  
  **mourya.hive.password**: hive database password    
  
  **Note:** Need to fill bellow parameters if Kerberos authentication is enabled.  
  
  **mourya.kerberos.keyTabLocation**: Local keytab location  
  **mourya.kerberos.krb5Location**: local kerberos file location  
  **mourya.kerberos.priniciple**  
 
```
mourya:
  hive:
    connectionURL: jdbc:hive2://localhost:10000/default;principal=hive/localhost@ATHENA.MIT.EDU
    username: mourya
    password: password
  kerberos:
    keyTabLocation: ~/Security/mourya.keytab
    krb5Location: ~/krb5.conf
    priniciple: mourya@ATHENA.MIT.EDU
```
    
### Create a config class to initialize JdbcTemplate
```
import java.io.IOException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class HiveDataSourceConfig {

	@Value("${mourya.hive.connectionURL}")
	private String hiveConnectionURL;

	@Value("${mourya.hive.username}")
	private String userName;

	@Value("${mourya.hive.password}")
	private String password;

	@Value("${mourya.kerberos.keyTabLocation}")
	private String keyTabLocation;

	@Value("${mourya.kerberos.krb5Location}")
	private String krb5Location;

	@Value("${mourya.kerberos.priniciple}")
	private String priniciple;

	public DataSource getHiveDataSource() throws IOException {
		
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "true");
		System.setProperty("java.security.krb5.conf", this.krb5Location);
		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		conf.set("hadoop.security.authentication", "kerberos");
		UserGroupInformation.setConfiguration(conf);
		UserGroupInformation.loginUserFromKeytab(this.priniciple, this.keyTabLocation);
		
		
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl(this.hiveConnectionURL);
		dataSource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");
		dataSource.setUsername(this.userName);
		dataSource.setPassword(this.password);
		
		return dataSource;
	}
	
	@Bean(name = "jdbcTemplate")
	public JdbcTemplate getJDBCTemplate() throws IOException {
		return new JdbcTemplate(getHiveDataSource());
	}
}
```
### Create a Rest Controller in the Spring boot application
```
package com.mourya.sample.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hive")
public class SampleHiveController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@RequestMapping(value = "/{schemaName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, Object>>> showTables(@PathVariable String schemaName) {
		List<Map<String, Object>> rows = null;
		jdbcTemplate.execute("use " + schemaName);
		rows = jdbcTemplate.queryForList("show tables");

		return new ResponseEntity<List<Map<String, Object>>>(rows, HttpStatus.OK);
	}

	@RequestMapping(value = "/databases", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, Object>>> showDatabaeses() {
		List<Map<String, Object>> rows = null;
		rows = jdbcTemplate.queryForList("show databases");
		return new ResponseEntity<List<Map<String, Object>>>(rows, HttpStatus.OK);
	}
}
```
### In command prompt or terminal go to root folder of the project and install by using bellow command.  
```
mvn clean install -DskipTests
```
### Start the application
```
Java –jar target/SpringHiveJDBCTemplate -0.0.1-SNAPSHOT.jar
```
### In terminal or command prompt use curl command to test the result. 
```
Curl http://localhost:8082/hive/databases
```
