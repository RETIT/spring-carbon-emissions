package io.retit.spring.carbon;

import io.opentelemetry.api.common.Attributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is an example REST service that provides three endpoints for HTTP GET / POST and DELETE.
 */
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

    /**
     * This is an example of a business functionality that is being used by the REST service endpoints.
     * <p>
     * Before and after the service operation the resource demands of the business function are being measured.
     *
     * @param httpMethod - the HTTP method (GET/POST/DELETE) of the endpoint calling the business function
     * @param apiCall    - the name of the API call (getData, postData, deleteData) calling the business function
     * @return - the OpenTelemetry Attributes published along with the resource demand metrics
     */
    private Attributes callBusinessFunctionAndPublishMetric(String httpMethod, String apiCall) throws InterruptedException {

        ResourceDemandMeasurementService.Measurement startMeasurements = resourceDemandMeasurementService.measure();

        testService.veryComplexBusinessFunction();

        return resourceDemandMeasurementService.measureAndPublishMetrics(startMeasurements, httpMethod, apiCall);
    }

}
