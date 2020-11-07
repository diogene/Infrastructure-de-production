package org.springframework.samples.petclinic.api;

import java.util.Base64;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class BasicRequestHeaderGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory
		implements EnvironmentAware {

	private Environment env;

	@Override
	public GatewayFilter apply(NameValueConfig config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			if (config.getValue().startsWith("$\\")) {
				String basic = config.getName() + ":" + this.env.getProperty(config.getValue());
				request = exchange.getRequest().mutate().header("Authorization", "Basic " +  Base64.getEncoder().encodeToString(basic.getBytes()))
						.build();
			} else {
				String basic = config.getName() + ":" + config.getValue();
				request = exchange.getRequest().mutate().header("Authorization", "Basic " +  Base64.getEncoder().encodeToString(basic.getBytes()))
				.build();
			}

			return chain.filter(exchange.mutate().request(request).build());
		};
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.env = environment;
	}
}