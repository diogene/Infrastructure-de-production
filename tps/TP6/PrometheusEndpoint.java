package org.springframework.samples.petclinic.prometheus;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

/**
 * Created by jzietsman on 1/28/16.
 */
class PrometheusEndpoint extends AbstractEndpoint<String> {

    CollectorRegistry registry;

    PrometheusEndpoint(CollectorRegistry registry) {
        super("prometheus",false, true);
        this.registry = registry;
    }

    @Override
    public String invoke() {
        Writer writer = new StringWriter();
        try {
			TextFormat.write004(writer, registry.metricFamilySamples());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return writer.toString();
    }
}
