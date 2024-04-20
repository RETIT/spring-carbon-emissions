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

@Slf4j
@Component
public class OpenTelemetryService {


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

    public void publishCpuCO2EmissionsForRegionAndProvider(long co2inmgramm, Attributes attributes) {
        log.info("Publish cpu CO2e mg value " + co2inmgramm);
        // Record data
        getLongCounter("cpu_co2_emissions",
                "CO2 Emissions of CPU", "mg").add(co2inmgramm, attributes);
    }

    public void publishMemoryCO2EmissionsForRegionAndProvider(long co2inmgramm, Attributes attributes) {
        log.info("Publish memory CO2e mg value " + co2inmgramm);
        // Record data

        getLongCounter("memory_co2_emissions",
                "CO2 Emissions of Memory", "mg")
                .add(co2inmgramm, attributes);
    }

    public void publishCpuTimeMetric(long cpuTimeInMS, Attributes attributes) {
        log.info("Publish cpu demand for service invocation " + cpuTimeInMS + " ms");
        // Record data

        getLongCounter("cpu_demand",
                "CPU Demand Metric", "ms")
                .add(cpuTimeInMS, attributes);
    }

    public void publishMemoryDemandMetric(long memoryDemandInKByte, Attributes attributes) {

        log.info("Publish memory demand for service invocation " + memoryDemandInKByte + " kByte");
        // Record data
        getLongCounter("memory_demand",
                "Memory Demand Metric", "kByte")
                .add(memoryDemandInKByte, attributes);
    }

    public void publishCallCountMetric(Attributes attributes) {
        // Record data
        getLongCounter("call_count",
                "Tracks the number of calls to a service", "1")
                .add(1, attributes);
    }


    private Meter getOpenTelemetryMeter() {
        // Gets or creates a named meter instance
        return GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();
    }

    private LongCounter getLongCounter(final String counterName, final String description, final String unit) {
        return getOpenTelemetryMeter()
                .counterBuilder(counterName)
                .setDescription(description)
                .setUnit(unit)
                .build();
    }

}
