package it.subito.cart.service;

import it.subito.cart.domain.entity.ProductEntity;
import it.subito.cart.domain.exception.ProductNotFoundException;
import it.subito.cart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing products.
 * Provides operations to retrieve products from the database.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Retrieves a product by its ID.
     *
     * @param productId the product ID
     * @return the product entity
     * @throws ProductNotFoundException if the product does not exist
     */
    @Transactional(readOnly = true)
    public ProductEntity getProductById(Long productId) {
        log.info("Product lookup started: productId={}", productId);

        return productRepository.findById(productId)
            .map(product -> {
                log.info("Product lookup success: productId={}, name={}, unitNetPrice={}, currency={}",
                    product.getId(),
                    product.getName(),
                    product.getUnitNetPrice(),
                    product.getCurrency());
                return product;
            })
            .orElseThrow(() -> {
                log.warn("Product lookup failed: productId={} not found", productId);
                return new ProductNotFoundException(productId);
            });
    }

    /**
     * Retrieves products with pagination support.
     *
     * @param pageable pagination information
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<ProductEntity> getProductsPage(Pageable pageable) {
        log.info("Products page retrieval started: page={}, size={}",
            pageable.getPageNumber(), pageable.getPageSize());

        Page<ProductEntity> page = productRepository.findAll(pageable);

        log.info("Products page retrieval success: totalElements={}, totalPages={}, currentPageSize={}",
            page.getTotalElements(),
            page.getTotalPages(),
            page.getContent().size());

        return page;
    }
}

