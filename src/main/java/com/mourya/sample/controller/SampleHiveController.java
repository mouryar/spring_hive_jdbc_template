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
