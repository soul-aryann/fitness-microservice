package com.fitness.activityservice.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fitness.activityservice.model.Activity;


public interface ActivityRepository extends MongoRepository<Activity, String>{
    List<Activity> findByUserId(String userId);
}
