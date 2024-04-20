package io.retit.spring.carbon;

import io.ApiClient;
import io.ApiException;
import io.climatiq.CloudComputingServiceApi;
import io.climatiq.model.CloudComputingCpuRequest;
import io.climatiq.model.CloudComputingMemoryRequest;
import io.climatiq.model.EstimationEstimateResponse;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This job publishes Climatiqe data continuously via OpenTelemetry to Prometheus.
 */
@Slf4j
@Service
public class ClimatiqBackgroundPublisher {

    @Value("${io.retit.quarkus.carbon.cloud.provider}")
    String cloudProvider;
    @Value("${io.retit.quarkus.carbon.cloud.provider.region}")
    String region;
    @Value("${climatiq.api.key}")
    String climatiqApiKey;

    @Autowired
    private OpenTelemetryService otelService;

    /**
     * This method is useful of you want to compute the SCI score for dedicated services
     *
     * @throws ApiException - in case the call to Climatiq fails
     */
    @Scheduled(fixedDelay = 60000)
    void scheduleCO2OfResources() throws ApiException {

        ApiClient climatiqApiClient = new ApiClient();
        climatiqApiClient.addDefaultHeader("Authorization", "Bearer " + climatiqApiKey);
        CloudComputingServiceApi cloudComputingServiceApi = new CloudComputingServiceApi(climatiqApiClient);

        Attributes attributes = Attributes.of(AttributeKey.stringKey("region"), region, AttributeKey.stringKey("provider"), cloudProvider);

        publishCpuCO2EmissionsForRegionAndProvider(cloudComputingServiceApi, attributes);

        publishMemoryCO2EmissionsForRegionAndProvider(cloudComputingServiceApi, attributes);
    }

    private void publishMemoryCO2EmissionsForRegionAndProvider(CloudComputingServiceApi cloudComputingServiceApi, Attributes attributes) throws ApiException {
        CloudComputingMemoryRequest cloudComputingMemoryRequest = new CloudComputingMemoryRequest();

        cloudComputingMemoryRequest.region(region);
        cloudComputingMemoryRequest.data(1.0);
        cloudComputingMemoryRequest.dataUnit("GB");
        cloudComputingMemoryRequest.duration(1.0);
        cloudComputingMemoryRequest.durationUnit("m");

        EstimationEstimateResponse memoryEstimateResponse = cloudComputingServiceApi.cloudComputingServiceMemory(cloudProvider, cloudComputingMemoryRequest);
        double memCo2eInKg = memoryEstimateResponse.getCo2e();

        otelService.publishMemoryCO2EmissionsForRegionAndProvider((long) (memCo2eInKg * 1000 * 1000), attributes);
    }

    private void publishCpuCO2EmissionsForRegionAndProvider(final CloudComputingServiceApi cloudComputingServiceApi, final Attributes attributes) throws ApiException {
        CloudComputingCpuRequest cloudComputingCpuRequest = new CloudComputingCpuRequest();

        cloudComputingCpuRequest.region(region);
        cloudComputingCpuRequest.cpuCount(1);
        cloudComputingCpuRequest.duration(1.0);
        cloudComputingCpuRequest.durationUnit("m");

        EstimationEstimateResponse cpuEstimateResponse = cloudComputingServiceApi.cloudComputingServiceCpu(cloudProvider, cloudComputingCpuRequest);
        double co2eInKg = cpuEstimateResponse.getCo2e();

        otelService.publishCpuCO2EmissionsForRegionAndProvider((long) (co2eInKg * 1000 * 1000), attributes);
    }
}
