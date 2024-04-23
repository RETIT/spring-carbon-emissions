package io.retit.spring.carbon;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This service is used to publish resource demand and CO2 emission data as OpenTelemetry metrics.
 */
@Slf4j
@Component
public class OpenTelemetryService {

    /**
     * This method is called once during startup to initialize the OpenTelemetry SDK.
     */
    @PostConstruct
    public void init() {
        // This call is only required during development as Spring restarts automatically for code changes
        GlobalOpenTelemetry.resetForTest();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "spring-carbon-emissions-testservice")));

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
                .setResource(resource)
                .build();

        OpenTelemetrySdk.builder()
                .setMeterProvider(sdkMeterProvider)
                .buildAndRegisterGlobal();

    }

    /**
     * This method publishes the CPU CO2 emission data as OpenTelemetry metric.
     *
     * @param co2inmgramm - the CPU CO2 emissions in milligram.
     * @param attributes  - the attributes to be published along with the CPU CO2 emission metric.
     */
    public void publishCpuCO2EmissionsForRegionAndProvider(long co2inmgramm, Attributes attributes) {
        log.info("Publish cpu CO2e mg value " + co2inmgramm);
        // Record data
        getLongCounter("cpu_co2_emissions",
                "CO2 Emissions of CPU", "mg").add(co2inmgramm, attributes);
    }

    /**
     * This method publishes the memory CO2 emission data as OpenTelemetry metric.
     *
     * @param co2inmgramm - the memory CO2 emissions in milligram.
     * @param attributes  - the attributes to be published along with the memory CO2 emission metric.
     */
    public void publishMemoryCO2EmissionsForRegionAndProvider(long co2inmgramm, Attributes attributes) {
        log.info("Publish memory CO2e mg value " + co2inmgramm);
        // Record data

        getLongCounter("memory_co2_emissions",
                "CO2 Emissions of Memory", "mg")
                .add(co2inmgramm, attributes);
    }

    /**
     * This method publishes the CPU demand as OpenTelemetry metric.
     *
     * @param cpuTimeInMS - the CPU time in milliseconds.
     * @param attributes  - the attributes to be published along with the CPU time metric.
     */
    public void publishCpuTimeMetric(long cpuTimeInMS, Attributes attributes) {
        log.info("Publish cpu demand for service invocation " + cpuTimeInMS + " ms");
        // Record data

        getLongCounter("cpu_demand",
                "CPU Demand Metric", "ms")
                .add(cpuTimeInMS, attributes);
    }

    /**
     * This method publishes the memory demand as OpenTelemetry metric.
     *
     * @param memoryDemandInKByte - the memory demand in kilobytes.
     * @param attributes          - the attributes to be published along with the memory demand metric.
     */
    public void publishMemoryDemandMetric(long memoryDemandInKByte, Attributes attributes) {

        log.info("Publish memory demand for service invocation " + memoryDemandInKByte + " kByte");
        // Record data
        getLongCounter("memory_demand",
                "Memory Demand Metric", "kByte")
                .add(memoryDemandInKByte, attributes);
    }

    /**
     * This method publishes the call count of  specific service as OpenTelemetry metric. Each
     * individual call is tracked as a single call, therefore there is no call count parameter.
     *
     * @param attributes - the attributes to be published along with the memory demand metric.
     */
    public void publishCallCountMetric(Attributes attributes) {
        // Record data
        getLongCounter("call_count",
                "Tracks the number of calls to a service", "1")
                .add(1, attributes);
    }

    /**
     * Returns the OpenTelemetry Meter that is required to publish OpenTelemetry metrics.
     *
     * @return - an OpenTelemetry Meter instance.
     */
    private Meter getOpenTelemetryMeter() {
        // Gets or creates a named meter instance
        return GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();
    }

    /**
     * Creates an OpenTelemetry LongCounter with the provided name, description and unit.
     *
     * @param counterName - the name of the LongCounter.
     * @param description - the description of the LongCounter.
     * @param unit        - the unit of the LongCounter.
     * @return a LongCounter instance.
     */
    private LongCounter getLongCounter(final String counterName, final String description, final String unit) {
        return getOpenTelemetryMeter()
                .counterBuilder(counterName)
                .setDescription(description)
                .setUnit(unit)
                .build();
    }

}
