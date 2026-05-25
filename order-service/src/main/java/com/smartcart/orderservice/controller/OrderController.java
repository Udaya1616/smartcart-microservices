package com.smartcart.orderservice.controller;

import com.smartcart.orderservice.dto.OrderRequest;
import com.smartcart.orderservice.dto.OrderResponse;
import com.smartcart.orderservice.security.JwtRoleService;
import com.smartcart.orderservice.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final JwtRoleService jwtRoleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@RequestHeader(name = "Authorization", required = false) String authorization,
                                    @Valid @RequestBody OrderRequest request) {
        JwtRoleService.UserToken user = jwtRoleService.requireUser(authorization);
        return orderService.placeOrder(user.userId(), authorization, request);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getUserOrders(@RequestHeader(name = "Authorization", required = false) String authorization,
                                             @PathVariable Long userId) {
        jwtRoleService.requireAdmin(authorization);
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/history")
    public List<OrderResponse> getMyOrderHistory(@RequestHeader(name = "Authorization", required = false) String authorization) {
        JwtRoleService.UserToken user = jwtRoleService.requireUser(authorization);
        return orderService.getUserOrders(user.userId());
    }

    @GetMapping
    public List<OrderResponse> getAllOrders(@RequestHeader(name = "Authorization", required = false) String authorization) {
        jwtRoleService.requireAdmin(authorization);
        return orderService.getAllOrders();
    }
}
