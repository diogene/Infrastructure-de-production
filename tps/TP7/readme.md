****# ARCHITECTURE DE MICROSERVICE : gateway

L’objectif de ce TP est mettre en oeuvre une infrastructure pour gérer tous les flux de microservice.

![infrastructure a monitorer](Caching%20Cluster%20Architecture.svg)

## Les éléments de notation

* fichier de configuration, a mettre dans votre repository git

## La mise en place des composants

1. Intellj est utile uniquement pour modifier l'application. toutes les autres manipulations doivent être réalisées en ligne de commande
2. Télécharger les sources
   ```bash
   git clone https://github.com/diogene/Infrastructure-de-production.git
   ```
3. compiler le projet 
   ```bash
   cd Infrastructure-de-production/codes
   ./mvnw clean install
   ```

### preparation de l'application

installer la sécurité basique dans le module vets. pour ce faire reportez vous au TP3.

> :exclamation: Le systeme de sécurité étant e spring security 5 il faut changer la création des utilisateurs.

```java
   @Override
   protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(User.withUsername("admin").password("{noop}demo").roles("ADMIN"))
                .withUser(User.withUsername("user").password("{noop}demo").roles("USER"));
    }
```

### l'ui

`spring-petclinic-ui` est l'implémentation de l'UI petclinic écrit en Angularjs avec [wro](https://wro4j.readthedocs.io/en/stable/GettingStarted/) qui est un composant java apportant tous les mécanismes associés a developpement front dans le mode java :  JsHint, CssLint, JsMin, Google Closure compressor, YUI Compressor, UglifyJs, Dojo Shrinksafe, Css Variables Support, JSON Compression, Less, Sass, ...

### Lancement des composants 

dans un premier temps, tous les composants vont être lancer en mode autonome, sans utiliser consul. Pour ce faire, lancer les commandes :

dans le répertoire code, sous windows et avec powershell :

```bash
java -jar ./spring-petclinic-customers-service/target/spring-petclinic-customers-service-2.0.4.jar --spring.profiles.active=simple 
java -jar ./spring-petclinic-vets-service/target/spring-petclinic-vets-service-2.0.4.jar --spring.profiles.active=simple
java -jar ./spring-petclinic-visits-service/target/spring-petclinic-visits-service-2.0.4-exec.jar --spring.profiles.active=simple
java -jar ./spring-petclinic-ui/target/spring-petclinic-ui.jar --spring.profiles.active=simple
```

> :exclamation: Il est imperatif de spécifier le profil a activer, ici c'est `simple`


l'application petclinc est disponible a l'adresse : http://localhost:8080/
![ui petclinc](spring%20petclinic%20ui.png)

Ici rien ne fonctionne !
Pour le voir, lancer la `devTool`

### aggregation des flux

il faut aggréger les flux en utilisant le pattern embedded facade :
![image du pattern](gateway%20facade.png)

ajouter la dependance spring :

```xml
   <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-gateway</artifactId>
   </dependency>
```

dans le fichier application properties :

```yml
spring:
  profiles: simple
  cloud:
    gateway:
      enabled: true
      routes:
        - id: vets-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/vet/{segment}
          filters:
            - SetPath=/api/{segment}
        - id: visits-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/visit/**
          filters:
            - StripPrefix=2
        - id: customers-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/customer/**
          filters:
            - StripPrefix=2

server:
  port: ${PORT:8080} 
logging:
  level:
    org:
      springframework: DEBUG
```

pour chaque type d'appel on defini la nouvelle destination. `StripPrefix` permet de ne garder que `/customer/**` (par exemple) pour le transfert de la request

### filtre de sécurité

le clic sur le menu [Veterinarians](http://localhost:8080/#!/vets) demande un login et un mot de passe :
![ui petclinc vets](spring%20petclinic%20ui%20vets.png)

il y a plusieurs facon de procéder : 
- avec l'utilisation d'un filtre prédéterminer de type header 
- en écrivant son propre filtre

#### filtre prédéterminé

Un filtre prédéterminé est un filtre présent dans la liste des filtres possibles de spring cloud. Ici il suffit de prendre le filtre permettant d'ajouter un elements dans le header. 

> :exclamation: c'est la méthode la plus simple et celle qui est le plus adaptée au TP mais dans la réalité cette méthode n'est pas acceptable car le mot de passe utilisé est mis en clair dans la configuration. Dans un cas réel il faudrait utiliser le filtre custom.

ajouter dans la configuration de votre application :

```yml
    - AddRequestHeader=Authorization, Basic YWRtaW46ZGVtbw==
```

recompiler : 
```bash
.\mvnw clean install -pl spring-petclinic-ui
```
relancer :
```bash
java -jar ./spring-petclinic-ui/target/spring-petclinic-ui.jar --spring.profiles.active=simple
```

![ui petclinc valorisé](spring%20petclinic%20ui%20veterinarians.png)

#### filtre custom

la forme la plus adapter pour ajouter le sécurité pour l'appel de vet est :
```yml
    - BasicRequestHeader=admin, $\{vet.service.password}
```

ici le mot de passe est basé via une propriété qui peut être, soit dans un des fichiers lue, soit via une variable d'environnement.

```java
public class BasicRequestHeaderGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory
		implements EnvironmentAware {

   // chargement de l'environnement      
	private Environment env;

	@Override
	public GatewayFilter apply(NameValueConfig config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

         // si la valeur commence par $\ alors elle doit être lu via l'environnement
			if (config.getValue().startsWith("$\\")) {
				String basic = config.getName() + ":" + this.env.getProperty(config.getValue());
				request = exchange.getRequest().mutate().header("Authorization", "Basic " +  Base64.getEncoder().encodeToString(basic.getBytes()))
						.build();

         // sinon on met la valeur directment dans la basic auth
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
```

Dans l'`ApiGatewayApplication` ajouter la déclaration du bean
```java
   @Bean
	public BasicRequestHeaderGatewayFilterFactory basicRequestHeaderGatewayFilterFactory() {
		return new BasicRequestHeaderGatewayFilterFactory();
	}

```

recompiler : 
```bash
.\mvnw clean install -pl spring-petclinic-ui
```
relancer :
```bash
java -jar ./spring-petclinic-ui/target/spring-petclinic-ui.jar --spring.profiles.active=simple -Dvet.service.password=demo
```

on voit l'utilisation du nouveau filtre dans la log :
```bash
2020-11-07 16:59:30.794 DEBUG 8112 --- [ctor-http-nio-3] o.s.c.g.h.RoutePredicateHandlerMapping   : Mapping [Exchange: GET http://localhost:8080/api/vet/vets] to Route{id='vets-service', uri=http://localhost:8082, order=0, predicate=org.springframework.cloud.gateway.support.ServerWebExchangeUtils$$Lambda$970/0x0000000801245c40@6c1181a4, gatewayFilters=[OrderedGatewayFilter{delegate=org.springframework.samples.petclinic.api.**BasicRequestHeaderGatewayFilterFactory**$$Lambda$984/0x000000080124a440@7016d095, order=1}, OrderedGatewayFilter{delegate=org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory$$Lambda$987/0x000000080124b440@501d5540, order=2}]}
2020-11-07 16:59:30.794 DEBUG 8112 --- [ctor-http-nio-3] o.s.c.g.h.RoutePredicateHandlerMapping   : [d3ebd9d2] Mapped to org.springframework.cloud.gateway.handler.FilteringWebHandler@2c306a57
``` 


## En mode consul

1. Télécharger consul [ici](https://www.consul.io/downloads.html)
2. Installer consul, il suffit de decompresser l'archive
3. Démarrer consul
   ```bash
   ./consul agent -dev -node machine
   ```
4. Visualiser la console consul : http://localhost:8500/ui
![console consul](../TP2/console%20consul.png)
La capture d'écran montre aussi le composant suivant

pour faire fonctionner `vet` avec consul il faut faire une modification dans la partie securité et autoriser `/actuator/health` :

```java
http.csrf().disable() .authorizeRequests() .antMatchers("/actuator/health").permitAll() .anyRequest().authenticated() .and() .httpBasic() .and() .logout().permitAll()
```

il faut modifier le application.yml pour prendre ne compte consul et récupérer les bonne url

```yml
spring:
  cloud:
    gateway:
      enabled: true
      routes:
        - id: vets-service
          uri: lb://vets-service
          predicates:
            - Path=/api/vet/{segment}
          filters:
            - SetPath=/api/{segment}
        - id: visits-service
          uri: lb://visits-service
          predicates:
            - Path=/api/visit/**
          filters:
            - StripPrefix=2
        - id: customers-service
          uri: lb://customers-service
          predicates:
            - Path=/api/customer/**
          filters:
            - StripPrefix=2

server:
  port: 0
```

ici on demande a consul quelle est le chemin pour atteindre les services `vets-service` ou `visits-service`, ...
lancer avec consul :
```bash
java -jar ./spring-petclinic-customers-service/target/spring-petclinic-customers-service-2.0.4.jar
java -jar ./spring-petclinic-vets-service/target/spring-petclinic-vets-service-2.0.4.jar
java -jar ./spring-petclinic-visits-service/target/spring-petclinic-visits-service-2.0.4-exec.jar
java -jar ./spring-petclinic-ui/target/spring-petclinic-ui.jar
```


Verifier que tous fonctionne en consul et que le load balancing est fonctionnel.

pour cela lancer plusieurs instance des composants.

donnée moi la log qui permet de voir que le load balancing est bon.
