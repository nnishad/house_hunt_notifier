package com.nikhilnishad.househunt.service;

import java.util.List;
import java.util.stream.Collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikhilnishad.househunt.model.LocationEmailMapping;
import com.nikhilnishad.househunt.model.RequestAddUser;
import com.nikhilnishad.househunt.repository.LocationEmailMappingDao;

@Service
public class ConfigService {

	@Autowired
	private LocationEmailMappingDao locationEmailMappingDao;

	public String addUserToLocation(RequestAddUser inputRequest) {
		LocationEmailMapping foundMapping = locationEmailMappingDao.findByLocationPreference(inputRequest.getLocationPref());
		if(foundMapping!=null) {
			if(foundMapping
					.getUserEmailList()
					.contains(inputRequest.getEmail())) return "Email already tagged to this address!";
			foundMapping.getUserEmailList().add(inputRequest.getEmail());
			locationEmailMappingDao.save(foundMapping);
		}
		else {
			LocationEmailMapping newMapping=new LocationEmailMapping();
			newMapping.setLocationPreference(inputRequest.getLocationPref());
			newMapping.setUserEmailList(List.of(inputRequest.getEmail()));
			locationEmailMappingDao.save(newMapping);
		}
		
		return "Added!";
	}

	public List<LocationEmailMapping> getUserLocationPrefs(String email) {
		
		List<LocationEmailMapping> dbResponseMapping = locationEmailMappingDao.findByUserEmailListIn(email);
		if(dbResponseMapping.isEmpty()) return null;
		return dbResponseMapping.parallelStream().map(locationMapping->{
			locationMapping.setUserEmailList(null);
			return locationMapping;
			}).toList();
	}

	public String removeUserLocationMapping(RequestAddUser inputRequest) {
		System.out.println(inputRequest.getLocationPref().getFurnishTypes());
		LocationEmailMapping foundMapping = locationEmailMappingDao.findByLocationPreference(inputRequest.getLocationPref());
		if(foundMapping!=null) {
			boolean removalStatus = foundMapping.getUserEmailList().remove(inputRequest.getEmail());
			if(removalStatus) {
				locationEmailMappingDao.save(foundMapping);
			}
			else {
				return "Already not tagged to this address";
			}
		}
		else {
			return "Address not exist in records";
		}
		return "Untagged from given location";
	}
	
	
}
