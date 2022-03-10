package com.devsuperior.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundExceptions;

@Service
public class UserService {

	@Autowired
	private UserRepository repository;

	@Transactional(readOnly = true)
	public Page<UserDTO> findallPaged(Pageable pageable) {
		Page<User> list = repository.findAll(pageable);
		return list.map(x -> new UserDTO(x));		
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> obj = repository.findById(id);
		User entity = obj.orElseThrow(() -> new ResourceNotFoundExceptions("Entity not found"));
		return new UserDTO(entity, entity.getCategories());
	}

	@Transactional
	public UserDTO insert(UserDTO dto) {
		User entity = new User();
		copyDTOtoEntity(dto, entity);
		
		entity = repository.save(entity);
		return new UserDTO(entity);
	}


	@Transactional
	public UserDTO update(Long id, UserDTO dto) {
		try {
		User entity = repository.getOne(id);
		copyDTOtoEntity(dto, entity);
		entity = repository.save(entity);
		return new UserDTO(entity);
		}
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundExceptions("Id not found" + id); 
		}
	}

	public void delete(Long id) {
		try {
		repository.deleteById(id);
		}
		catch(EmptyResultDataAccessException e) {
			throw new ResourceNotFoundExceptions("Id not found" + id);
		}
		catch(DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity Violation");
			
		}
	}
	
	private void copyDTOtoEntity(UserDTO dto, User entity) {
		
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();
		for (CategoryDTO catDTO : dto.getCategories()) {
			Category category = categoryRepository.getOne(catDTO.getId());
			entity.getCategories().add(category);
		}
	}
	
}

