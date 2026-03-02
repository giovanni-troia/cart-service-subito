package it.subito.cart.service;

import it.subito.cart.domain.dto.CreateOrderRequest;
import it.subito.cart.domain.dto.OrderItemRequest;
import it.subito.cart.domain.entity.OrderEntity;
import it.subito.cart.domain.entity.OrderItemEntity;
import it.subito.cart.domain.entity.ProductEntity;
import it.subito.cart.domain.exception.DomainException;
import it.subito.cart.domain.exception.DuplicateProductException;
import it.subito.cart.domain.exception.OrderNotFoundException;
import it.subito.cart.domain.exception.ProductNotFoundException;
import it.subito.cart.repository.OrderRepository;
import it.subito.cart.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public OrderEntity getOrderById(Long orderId) {
        log.info("Order lookup started: orderId={}", orderId);

        return orderRepository.findById(orderId)
            .map(order -> {
                log.info("Order lookup success: orderId={}, items={}, currency={}, totalGross={}",
                    order.getId(),
                    order.getItems() != null ? order.getItems().size() : 0,
                    order.getCurrency(),
                    safeGross(order));
                return order;
            })
            .orElseThrow(() -> {
                log.warn("Order lookup failed: orderId={} not found", orderId);
                return new OrderNotFoundException(orderId);
            });
    }

    @Transactional
    public OrderEntity createOrder(CreateOrderRequest request) {
        int itemsCount = request.getItems() != null ? request.getItems().size() : 0;

        log.info("Order creation started: itemsCount={}", itemsCount);
        if (log.isDebugEnabled()) {
            log.debug("Order creation request items: {}",
                request.getItems().stream()
                    .map(i -> String.format("{productId=%s,qty=%s}", i.getProductId(), i.getQuantity()))
                    .toList());
        }

        // 1) Check duplicates
        checkForDuplicates(request.getItems());

        // 2) Load products
        List<ProductEntity> products = loadProducts(request.getItems());

        // 3) Validate currency consistency
        validateCurrencyConsistency(products);

        // 4) Create order items with pricing
        List<OrderItemEntity> orderItems = createOrderItemEntities(products, request.getItems());

        // 5) Calculate totals
        BigDecimal totalNetPrice = orderItems.stream()
            // get line net for each object
            .map(OrderItemEntity::getLineNetPrice)
            // sum up, starting from zero
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVatAmount = orderItems.stream()
            // get line VAT for each object
            .map(OrderItemEntity::getLineVatAmount)
            // sum up, starting from zero
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Order totals computed: totalNet={}, totalVat={}, totalGross={}, currency={}",
            totalNetPrice, totalVatAmount, totalNetPrice.add(totalVatAmount), products.getFirst().getCurrency());

        // 6) Create order + link both sides
        OrderEntity order = OrderEntity.builder()
            .totalNetPrice(totalNetPrice)
            .totalVatAmount(totalVatAmount)
            .currency(products.getFirst().getCurrency())
            .build();

        orderItems.forEach(order::addItem);

        // 7) Persist
        OrderEntity savedOrder = orderRepository.save(order);

        log.info("Order creation success: orderId={}, items={}, totalNet={}, totalVat={}, totalGross={}, currency={}",
            savedOrder.getId(),
            orderItems.size(),
            savedOrder.getTotalNetPrice(),
            savedOrder.getTotalVatAmount(),
            savedOrder.getTotalGrossPrice(),
            savedOrder.getCurrency());

        return savedOrder;
    }

    private void checkForDuplicates(List<OrderItemRequest> items) {
        Set<Long> productIds = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!productIds.add(item.getProductId())) {
                log.warn("Order creation rejected: duplicate productId={} in request", item.getProductId());
                throw new DuplicateProductException(item.getProductId());
            }
        }
        log.debug("Duplicate check passed: uniqueProductIds={}", productIds.size());
    }

    private List<ProductEntity> loadProducts(List<OrderItemRequest> items) {
        log.debug("Loading products for order: requestedProductIds={}",
            items.stream().map(OrderItemRequest::getProductId).toList());

        return items.stream()
            .map(item -> productRepository.findById(item.getProductId())
                .orElseThrow(() -> {
                    log.warn("Order creation rejected: productId={} not found", item.getProductId());
                    return new ProductNotFoundException(item.getProductId());
                }))
            .toList();
    }

    private void validateCurrencyConsistency(List<ProductEntity> products) {
        String currency = products.getFirst().getCurrency();
        boolean allSameCurrency = products.stream().allMatch(p -> currency.equals(p.getCurrency()));

        if (!allSameCurrency) {
            Map<String, Long> byCurrency = products.stream()
                .collect(java.util.stream.Collectors.groupingBy(ProductEntity::getCurrency, java.util.stream.Collectors.counting()));

            log.warn("Order creation rejected: currency mismatch. currencies={}", byCurrency);

            throw new DomainException("All products must have the same currency");
        }

        log.debug("Currency check passed: currency={}", currency);
    }

    private List<OrderItemEntity> createOrderItemEntities(List<ProductEntity> products, List<OrderItemRequest> requests) {
        Map<Long, OrderItemRequest> byProductId = new HashMap<>();
        for (OrderItemRequest r : requests) {
            byProductId.put(r.getProductId(), r);
        }

        return products.stream()
            .map(product -> {
                OrderItemRequest request = byProductId.get(product.getId());
                if (request == null) {
                    log.warn("Inconsistent order request: missing request item for productId={}", product.getId());
                    throw new DomainException("Invalid order request: missing item for productId=" + product.getId());
                }

                BigDecimal lineNetPrice = PricingCalculator.calculateLineNetPrice(
                    product.getUnitNetPrice(),
                    request.getQuantity()
                );

                BigDecimal lineVatAmount = PricingCalculator.calculateVatAmount(
                    lineNetPrice,
                    product.getVatRate()
                );

                if (log.isDebugEnabled()) {
                    log.debug("Pricing computed: productId={}, qty={}, unitNet={}, vatRate={}, lineNet={}, lineVat={}, lineGross={}",
                        product.getId(),
                        request.getQuantity(),
                        product.getUnitNetPrice(),
                        product.getVatRate(),
                        lineNetPrice,
                        lineVatAmount,
                        lineNetPrice.add(lineVatAmount));
                }

                return OrderItemEntity.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(request.getQuantity())
                    .unitNetPrice(product.getUnitNetPrice())
                    .vatRate(product.getVatRate())
                    .lineNetPrice(lineNetPrice)
                    .lineVatAmount(lineVatAmount)
                    .currency(product.getCurrency())
                    .build();
            })
            .toList();
    }

    private BigDecimal safeGross(OrderEntity order) {
        try {
            return order.getTotalGrossPrice();
        } catch (Exception e) {
            return null;
        }
    }
}