package com.nikhilnishad.househunt.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nikhilnishad.househunt.model.LocationEmailMapping;
import com.nikhilnishad.househunt.model.LocationFilter;

@Repository
public interface LocationEmailMappingDao extends MongoRepository<LocationEmailMapping, String> {

	LocationEmailMapping findByLocationPreference(LocationFilter locationFilter);
	List<LocationEmailMapping> findByUserEmailListIn(String userEmail);
}

