package it.subito.cart.mapper;

import it.subito.cart.domain.dto.OrderItemResponse;
import it.subito.cart.domain.dto.OrderResponse;
import it.subito.cart.domain.entity.OrderEntity;
import it.subito.cart.domain.entity.OrderItemEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper for Order and OrderItem entities to DTOs.
 */
@Component
public class OrderMapper {

    public OrderResponse toResponse(OrderEntity order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
            .map(this::toItemResponse)
            .collect(Collectors.toList());

        return OrderResponse.builder()
            .orderId(order.getId())
            .totalNetPrice(order.getTotalNetPrice())
            .totalVatAmount(order.getTotalVatAmount())
            .totalGrossPrice(order.getTotalGrossPrice())
            .currency(order.getCurrency())
            .items(itemResponses)
            .createdAt(order.getCreatedAt())
            .build();
    }

    public OrderItemResponse toItemResponse(OrderItemEntity item) {
        return OrderItemResponse.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitNetPrice(item.getUnitNetPrice())
            .vatRate(item.getVatRate())
            .lineNetPrice(item.getLineNetPrice())
            .lineVatAmount(item.getLineVatAmount())
            .lineGrossPrice(item.getLineGrossPrice())
            .currency(item.getCurrency())
            .build();
    }
}

