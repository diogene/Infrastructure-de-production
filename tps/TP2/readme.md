# EXPOSER DES APPLICATIONS

L’objectif de ce TP est de mettre en place une infrastructure pour exposer une application.

----
<!-- TOC -->

- [EXPOSER DES APPLICATIONS](#exposer-des-applications)
  - [Les éléments de notation](#les-éléments-de-notation)
  - [Installer les composants](#installer-les-composants)
  - [Préparation de l'application](#préparation-de-lapplication)
  - [Installer la console d'administration](#installer-la-console-dadministration)
  - [Modification de notre application pour le cloud](#modification-de-notre-application-pour-le-cloud)
  - [Ajouter tous les menus](#ajouter-tous-les-menus)

<!-- /TOC -->



## Les éléments de notation
Envoyer par email les éléments suivant : 

* fichier de configuration
* Réponse au qcm

## Installer les composants 

La **première étape** consiste a installer le serveur de configuration et d'enregistrement des services

1. Télécharger consul [ici](https://www.consul.io/downloads.html)
2. Installer consul, il suffit de decompresser l'archive
3. Démarrer consul
   ```bash
   ./consul agent -server -data-dir="E:/dev/TP2/consul.io/data" -ui -bootstrap-expect=1 -client="0.0.0.0" -advertise="[IP de votre poste]"
   ```
4. Visualiser la console consul : http://localhost:8500/ui
![console consul](./console%20consul.png )

## Préparation de l'application

La **deuxième étape** est de compiler les applications exemples :

1. Penser au java 8
2. Générer les binaires
   ```bash
   git clone https://github.com/diogene/Infrastructure-de-production.git
   mvn clean install
   ```
## Installer la console d'administration
La console d'administration va nous permettre de visualisation les modifications faite dans l'application

1. lancer la console d'administration :
   ```bash
   java -jar spring-petclinic-admin-server/target/spring-petclinic-admin-server-2.0.4.jar
   ```
2. tester
   ```properties
   application.properties
   http://localhost:8080 
   ```
   L'interface d'administration ne propose pas tous les menus en raison de restriction de sécurité.
   ![interface spring boot administrator](./Spring%20Boot%20Admin%20-%20Google%20Chrome%2026_09_2018%2016_45_29.png)



## Modification de notre application pour le cloud
1. Lancer l'application de test
   ```bash
   /usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar spring-petclinic-visits-service/target/spring-petclinic-visits-service-2.0.4.jar
   ```
2. Tester le fonctionnement avec : http://localhost:[votre port]/owners/*/pets/8/visits
3. Ajouter dépendance spring
   ```xml 
   pom.xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
    
    
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-consul-all</artifactId>
   </dependency>
   ```
4. Modifier la classe de démarrage
   ```java
   import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
    
   @SpringBootApplication
   @EnableDiscoveryClient
   public class VisitsServiceApplication {
       ...
   }
   ```
5. localiser le fichiers bootstrap.yml
   ```yml
   bootstrap.yml
   spring:
     cloud:
       consul:
         host: localhost
         port: 8500
         config:
           enabled: true
           fail-fast: true
           format: yaml
     application:
       name: petclinic-visits
   ```
5. Redémarrer
6. L'application doit maintenant être enregistré et visible dans la console consul
![consule avec l'application](./Spring%20Boot%20Admin%20-%20Google%20Chrome%2026_09_2018%2016_51_27.png)


## Ajouter tous les menus
La console d'administration ne propose pas tous les menus en raison de limitation de sécurité. Nous allons ajouter ces informations dans la partie configuration de consul.

Pour avoir accès a tous les menus il faut configurer l’application avec le paramètre : 
   ```yml
   management:
     security:
       enabled: false
   ```