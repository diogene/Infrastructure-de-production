# INFRASTRUCTURE DE MICROSERVICE

L’objectif de ce TP est mettre en oeuvre une infrastructure pour gérer tous les flux de microservice.

![infrastructure a monitorer](Caching%20Cluster%20Architecture.svg)

## Les éléments de notation

* fichier de configuration, a mettre dans votre repository git

## La mise en place des composants

La **première étape** consiste a installer le serveur de configuration et d'enregistrement des services

1. Télécharger consul [ici](https://www.consul.io/downloads.html)
2. Installer consul, il suffit de decompresser l'archive
3. Démarrer consul
   ```bash
   ./consul agent -dev -node machine
   ```
4. Visualiser la console consul : http://localhost:8500/ui
![console consul](../TP2/console%20consul.png)
La capture d'écran montre aussi le composant suivant


La **seconde étape** consiste a compiler les sources du projet cible :

1. Télécharger les sources
   ```bash
   git clone https://github.com/diogene/Infrastructure-de-production.git
   ```
2. compiler le projet 
   ```bash
   cd Infrastructure-de-production/codes
   ./mvnw clean install
   ```

### preparation de l'application

installer la sécurité basique dans le module vets. pour ce faire reportez vous au TP2.

### l'agent

télécharger l'agent opentelemetry [ici](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v0.9.0/opentelemetry-javaagent-all.jar)

### le collecteur

### l'ui

### Lancement des composants 

dans un premier temps, tous les composants vont être lancer en mode autonome, sans utiliser consul. Pour ce faire, lancer les commandes :


```bash
dans le répertoire code :

jav  -javaagent:C:/Users/dioge/OneDrive/Documents/personnel/cours/tps/TP7/opentelemetry-javaagent-all.jar -Dotel.exporter=jaeger -jar ./target/spring-petclinic-customers-service-2.0.4.jar --spring.profiles.active=simple 
java -javaagent:C:/Users/dioge/OneDrive/Documents/personnel/cours/tps/TP7/opentelemetry-javaagent-all.jar -Dotel.exporter=jaeger  -jar ./target/spring-petclinic-vets-service-2.0.4.jar --spring.profiles.active=simple
java -javaagent:C:/Users/dioge/OneDrive/Documents/personnel/cours/tps/TP7/opentelemetry-javaagent-all.jar -Dotel.exporter=jaeger  -jar ./target/spring-petclinic-visits-service-2.0.4.jar --spring.profiles.active=simple
java -javaagent:C:/Users/dioge/OneDrive/Documents/personnel/cours/tps/TP7/opentelemetry-javaagent-all.jar -Dotel.exporter=jaeger  -jar ./target/spring-petclinic-ui.jar --spring.profiles.active=simple
```

l'application petclinc est disponible a l'adresse : 

### aggregation des flux

### filtre de sécurité

### filtre de 