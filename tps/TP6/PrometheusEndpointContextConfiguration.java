package org.springframework.samples.petclinic.prometheus;

import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

import io.prometheus.client.CollectorRegistry;

/**
 * Created by jzietsman on 1/28/16.
 */
@ManagementContextConfiguration
class PrometheusEndpointContextConfiguration {

	@Bean
	public PrometheusEndpoint prometheusEndpoint(CollectorRegistry registry) {
		return new PrometheusEndpoint(registry);
	}

	@Bean
	@ConditionalOnBean(PrometheusEndpoint.class)
	@ConditionalOnEnabledEndpoint("prometheus")
	public PrometheusMvcEndpoint prometheusMvcEndpoint(PrometheusEndpoint prometheusEndpoint) {
		return new PrometheusMvcEndpoint(prometheusEndpoint);
	}

	@Bean
	public CollectorRegistry collectorRegistry() {
		return CollectorRegistry.defaultRegistry;
	}

	@Bean
	@ExportMetricWriter
	public MetricWriter prometheusMetricWriter(CollectorRegistry registry) {
		return new PrometheusMetricWriter(registry);
	}

}
