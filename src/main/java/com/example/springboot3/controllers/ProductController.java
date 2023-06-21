package com.example.springboot3.controllers;

import com.example.springboot3.dtos.ProductRecordDto;
import com.example.springboot3.models.ProductModel;
import com.example.springboot3.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        ProductModel save = productRepository.save(productModel);
        save.add(linkTo(methodOn(this.getClass()).getProduct(productModel.getIdProduct())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(save);
    }

    @GetMapping
    public ResponseEntity<List<ProductModel>> getAllProduct() {
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.findAll()
                .stream()
                .map(productModel -> productModel.add(linkTo(methodOn(this.getClass()).getProduct(productModel.getIdProduct())).withSelfRel()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> getProduct(@PathVariable UUID id) {
        return productRepository.findById(id)
                .map(productModel -> {
                    productModel.add(linkTo(methodOn(this.getClass()).getProduct(productModel.getIdProduct())).withSelfRel());
                    return ResponseEntity.status(HttpStatus.OK).body(productModel);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> updateProduct(@PathVariable UUID id, @RequestBody @Valid ProductRecordDto productRecordDto) {
        return productRepository.findById(id)
                .map(productModel -> {
                    BeanUtils.copyProperties(productRecordDto, productModel);
                    productModel.add(linkTo(methodOn(this.getClass()).getProduct(productModel.getIdProduct())).withSelfRel());
                    return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable UUID id) {
        return productRepository.findById(id)
                .map(productModel -> {
                    productRepository.deleteById(id);
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
