package io.retit.spring.carbon;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;

@Service
public class ResourceDemandMeasurementService {
    private static final Logger LOGGER = Logger.getLogger(ResourceDemandMeasurementService.class.getName());

    @Autowired
    private OpenTelemetryService otelService;

    @AllArgsConstructor
    @Setter
    @Getter
    public static final class Measurement {
        long cpuTime;
        long bytes;
    }

    protected long getCurrentThreadCpuTime() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        return mxBean.getCurrentThreadCpuTime();
    }

    protected long getCurrentThreadMemoryConsumption() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        com.sun.management.ThreadMXBean sunThreadMXBean = (com.sun.management.ThreadMXBean) mxBean;
        return sunThreadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    public Measurement measure() {
        return new Measurement(getCurrentThreadCpuTime(), getCurrentThreadMemoryConsumption());
    }

    public Attributes measureAndPublishMetrics(Measurement startMeasurement, String httpMethod, String apiCall) {
        Measurement endMeasurement = measure();

        Attributes attributes = Attributes.of(AttributeKey.stringKey("httpmethod"), httpMethod, AttributeKey.stringKey("apicall"), apiCall);


        otelService.publishCpuTimeMetric((endMeasurement.getCpuTime() - startMeasurement.getCpuTime()) / 1_000_000, attributes);
        otelService.publishMemoryDemandMetric((endMeasurement.getBytes() - startMeasurement.getBytes()) / 1_000, attributes);
        otelService.publishCallCountMetric(attributes);

        return attributes;
    }

}
