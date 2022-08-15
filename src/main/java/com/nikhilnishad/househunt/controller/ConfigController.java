package com.nikhilnishad.househunt.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nikhilnishad.househunt.model.LocationEmailMapping;
import com.nikhilnishad.househunt.model.RequestAddUser;
import com.nikhilnishad.househunt.service.ConfigService;
import com.nikhilnishad.househunt.service.NotifierService;

@RestController
public class ConfigController {

	@Autowired
	private ConfigService configService;
	
	@PostMapping("/userWithLocationPref")
	public ResponseEntity<String> addUserToLocation(@Valid @RequestBody RequestAddUser inputRequest){
		String response=configService.addUserToLocation(inputRequest);
		if(response!=null) {
			NotifierService.isDbUpdateFlag=true;
			return ResponseEntity.ok(response);
		}
		return ResponseEntity.badRequest().body(null);
	}
	
	@GetMapping("/userLocationPrefs/{email}")
	public ResponseEntity<List<LocationEmailMapping>> getUserLocationPrefs(@PathVariable String email){
		List<LocationEmailMapping> response=configService.getUserLocationPrefs(email);
		if(response!=null) {
			return ResponseEntity.ok(response);
		}
		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/userLocationMapping")
	public ResponseEntity<String> removeUserLocationMapping(@Valid @RequestBody RequestAddUser inputRequest){
		String response = configService.removeUserLocationMapping(inputRequest);
		NotifierService.isDbUpdateFlag=true;
		return ResponseEntity.ok(response);
	}
}
