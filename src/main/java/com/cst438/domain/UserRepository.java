package com.cst438.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer>{
	List<User> findAllByOrderByIdAsc();
	User findByEmail(String email);

	Optional<User> findById(int id);
}
