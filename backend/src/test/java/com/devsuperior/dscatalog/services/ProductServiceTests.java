package com.devsuperior.dscatalog.services;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.repositories.tests.Factory;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundExceptions;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private ProductRepository repository;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private PageImpl<Product> page;
	private Product product;
	private Category category;
	private ProductDTO productDTO;

	@BeforeEach
	void setUP() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		product = Factory.createdProduct();
		category = Factory.createdCategory();
		productDTO = Factory.createdProductDTO();

		page = new PageImpl<>(List.of(product));

		when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

		when(repository.save(ArgumentMatchers.any())).thenReturn(product);

		when(repository.findById(existingId)).thenReturn(Optional.of(product));
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		when(repository.getOne(existingId)).thenReturn(product);
		when(repository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

		when(categoryRepository.getOne(existingId)).thenReturn(category);
		when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

		doNothing().when(repository).deleteById(existingId);

		doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);

		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}

	public void updateShouldResourceNotFoundExceptionsWhenIdNonExist() {

		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			service.update(nonExistingId, productDTO);
		});

	}
	
	@Test

	public void updateShouldReturnProductDTOWhenIdExist() {

		ProductDTO result = service.update(existingId, productDTO);

		Assertions.assertNotNull(result);

	}

	@Test

	public void findByIdShouldReturnProductDTOWhenIdExist() {

		ProductDTO result = service.findById(existingId);

		Assertions.assertNotNull(result);
	}

	public void findByIdShouldResourceNotFoundExceptionsWhenIdNoExist() {

		Assertions.assertThrows(ResourceNotFoundExceptions.class, () -> {
			service.findById(nonExistingId);
		});

	}

	@Test

	public void findAllPagedShouldReturnPage() {

		Pageable pageable = PageRequest.of(0, 10);

		Page<ProductDTO> result = service.findallPaged(pageable);

		Assertions.assertNotNull(result);
		verify(repository, times(1)).findAll(pageable);

	}

	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});

		verify(repository, times(1)).deleteById(dependentId);
		;

	}

	@Test
	public void deleteShouldThrowResourceNotFoundExceptionsWhenIdDoesNotExists() {

		Assertions.assertThrows(ResourceNotFoundExceptions.class, () -> {
			service.delete(nonExistingId);
		});

		verify(repository, times(1)).deleteById(nonExistingId);
		;

	}

	@Test
	public void deleteShouldDoNothingWhenIdExists() {

		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});

		verify(repository, times(1)).deleteById(existingId);
		;

	}

}
