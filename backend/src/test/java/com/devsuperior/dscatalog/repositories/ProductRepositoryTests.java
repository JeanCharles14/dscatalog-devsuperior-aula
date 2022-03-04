package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.tests.Factory;

@DataJpaTest
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repository;

	private long existingID;
	private long nonExistingId;
	private long countTotalProducts;

	@BeforeEach
	void setUp() throws Exception {
		existingID = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
	}

	@Test
	public void findbyIdShouldReturnOptionalProductNoEmptyWhenIdExisting () {
		
		repository.findById(existingID);
		
		Optional<Product> result = repository.findById(existingID);
		
		Assertions.assertTrue(result.isPresent());
		
	}
	
	@Test
	public void findbyIdShouldReturnOptionalProductEmptyWhenIdNonExisting () {
		
		repository.findById(nonExistingId);
		
		Optional<Product> result = repository.findById(nonExistingId);
		
		Assertions.assertFalse(result.isPresent());
		
	}
	
	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {

		Product product = Factory.createdProduct();
		product.setId(null);

		product = repository.save(product);

		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());

	}

	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		repository.deleteById(existingID);

		Optional<Product> result = repository.findById(existingID);

		Assertions.assertFalse(result.isPresent());

	}

	@Test
	public void deleteShoudThrowEmptyResultDataAccessExceptionWhenIdDoesNotExisting() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExistingId);
		});

	}

}
