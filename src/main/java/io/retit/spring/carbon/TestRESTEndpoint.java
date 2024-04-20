package io.retit.spring.carbon;

import io.opentelemetry.api.common.Attributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-rest-endpoint")
public class TestRESTEndpoint {

    @Autowired
    private ResourceDemandMeasurementService resourceDemandMeasurementService;

    @Autowired
    private TestService testService;

    @GetMapping
    public String getData() throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("GET", "getData");
        return "Published Metric with Attributes: " + attributes;
    }

    @PostMapping
    public String postData() throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("POST", "postData");
        return "Published Metric with Attributes: " + attributes;
    }

    @DeleteMapping
    public String deleteData() throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("DELETE", "deleteData");
        return "Published Metric with Attributes: " + attributes;
    }

    private Attributes callBusinessFunctionAndPublishMetric(String httpMethod, String apiCall) throws InterruptedException {

        ResourceDemandMeasurementService.Measurement startMeasurements = resourceDemandMeasurementService.measure();

        testService.veryComplexBusinessFunction();

        return resourceDemandMeasurementService.measureAndPublishMetrics(startMeasurements, httpMethod, apiCall);
    }

}
