package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.repositories.tests.Factory;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundExceptions;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private ProductService service;
	private Long existingID;
	private Long nonExistingId;
	private Long DependentID;
	private PageImpl<ProductDTO> page;
	private ProductDTO productDTO;

	@BeforeEach
	void setUp() throws Exception {

		existingID = 1L;
		nonExistingId = 2L;
		DependentID = 3L;

		productDTO = Factory.createdProductDTO();
		page = new PageImpl<>(List.of(productDTO));

		when(service.findallPaged(any())).thenReturn(page);

		when(service.findById(existingID)).thenReturn(productDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundExceptions.class);

		when(service.update(eq(existingID), any())).thenReturn(productDTO);
		when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundExceptions.class);

		when(service.insert(any())).thenReturn(productDTO);
		
		doNothing().when(service).delete(existingID);
		doThrow(ResourceNotFoundExceptions.class).when(service).delete(nonExistingId);
		doThrow(DatabaseException.class).when(service).delete(DependentID);
		
	}

	
	
	@Test
	
	public void deleteShouldReturnNoContentWhenIdExist() throws Exception{
		
		ResultActions result = 
				mockMvc.perform(delete("/products/{id}", existingID)
					.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNoContent());
		
	}
	
	@Test
	
	public void deleteShouldReturnNotFoundWhenDoesNotExistId() throws Exception {
		
		ResultActions result = 
				mockMvc.perform(delete("/products/{id}", nonExistingId)
					.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}
	
	@Test
	
	public void insertShouldReturnCreatedNewProductDTO () throws Exception {
		
		String jsonbody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc.perform(post("/products")
				.content(jsonbody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	@Test

	public void updateShouldReturnProductDTOWhenIdExist() throws Exception {

		String jsonbody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc.perform(put("/products/{id}", existingID)
				.content(jsonbody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}

	@Test

	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

		String jsonbody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
				.content(jsonbody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
		
	}

	@Test

	public void findAllShouldReturnPage() throws Exception {

		ResultActions result = 
				mockMvc.perform(get("/products")
					.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());

	}

	@Test
	public void findByIdShouldReturnProductWhenIdExist() throws Exception {

		ResultActions result = 
				mockMvc.perform(get("/products/{id}", existingID)
					.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}

	@Test
	public void findByIdShouldReturnNotFoundProductWhenIdDoesNotExist() throws Exception {

		ResultActions result = 
				mockMvc.perform(get("/products/{id}", nonExistingId)
					.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}
}
