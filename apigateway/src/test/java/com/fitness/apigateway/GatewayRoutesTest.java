package com.fitness.apigateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class GatewayRoutesTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void testRoutesConfigured() {
        assertNotNull(routeLocator);
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertNotNull(routes);
        assertEquals(2, routes.size());

        boolean hasUserRoute = routes.stream().anyMatch(r -> "user-service".equals(r.getId()));
        boolean hasActivityRoute = routes.stream().anyMatch(r -> "activity-service".equals(r.getId()));

        assertEquals(true, hasUserRoute);
        assertEquals(true, hasActivityRoute);
    }
}
