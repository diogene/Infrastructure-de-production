
# PACKAGER UNE APPLICATION

L’objectif de ce TP est de mettre en place une infrastructure pour exposer une application.

L’application exemple est l’application petclinic de spring.

{:toc}

---

## Les éléments de notation

* Réponse au [qcm](https://docs.google.com/forms/d/1YAidZb2c8ftaFd-GVhQTh_mBp_-SfeOdjG5L64y5DDU)
* pour chaque partie, mettez dans bitbucket une résumé de vos actions.

## La gestion des droits

la plupart des binaires (mvnw, yaml2json, ...) téléchargés n'auront pas les droits d'execution. pour leur donner le droit :
```bash
chmod +x <binaire>
```

---
## La gestion de code 

La comprehension du fonctionnement de git et de maven est indispensable pour mettre en oeuvre les deploiements simples et encore plus pour les deploiements automatisés.

### git

1. Configurer votre git
   créer le fichier .gitconfig s'il n'existe pas :
   ```bash
   touch ~/.gitconfig
   ```
   dans la section alias ajouter :
   ```ini
   [alias]
   st = status --short --branch
   lga = log --graph --pretty=tformat:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%an %cr)%Creset' --abbrev-commit --date=relative --all -30
   ```

2. Télécharger les sources  `git clone https://github.com/spring-petclinic/spring-framework-petclinic.git`{:.language-bash .highlight}
1. utilisation de la commande reset pour positionner le pointeur de courant sur le deuxième commit
   ```bash
   git lga
   ```
   Aucune intersection n'est visible 
   ```bash
   git reset --soft HEAD~2
   git st
   ```
2. création d'une nouvelle branche
   ```bash
   git checkout -b branche-travail
   git st
   ```
3. commit des fichiers
   ```bash
   git commit . -m "mon travail de TP"
   git st
   git log
   git lga
   ```
   Une nouvelle branche apparait sur le graphe
4. merge des travaux de la nouvelle branche vers le master
   ```bash
   git checkout master
   git merge branche-travail
   git lga
   ```
   Le commit de merge est visible
   ```bash
   *   bf6efcf - (HEAD -> master) Merge branch 'branche-travail' (MOULRON Diogène 23 seconds ago)
   ```
5. Préparation pour faire le rebase
   ```bash
   git reset 9528000 --soft
   git st
   git lga
   git checkout -b branche-travail
   git commit . -m "mon travail de TP"
   ```
6. rebase des travaux de la nouvelle branche vers le master
   ```bash
   git checkout master
   cat ' texte pour commit ' >> readme.md
   git commit . -m "commit master de TP"
   git lga
   git checkout branche-travail
   git rebase master
   git st
   ```
   aucun commit visible pour signaler le rebase, seul le point de branchement par rapport au master est visible
7. un exemple de retravail de l'historique
   ```bash
   git checkout master
   git rebase -i <commit id>
   ```
   fusionner les deux derniers commits

### maven

A partir des sources suivante, on va modifier la facon dont fonctionne maven :

1. `git clone https://github.com/diogene/Infrastructure-de-production.git`{:.language-bash .highlight}
2. aller dans le repertoire `cd codes`{:.language-bash .highlight}
3. Lancer la compilation sur le projet spring framework, methode standard  `./mvnw clean install`{:.language-bash .highlight}

Il existe de nombreuses options pour le lancement de maven qui permettent son utilisation dans des chaines de continous delivery :

* -amd -pl  module :  permet d'agir sur un module en particulier. l'option amd permet de dire a maven d'agir, aussi, sur les modules dont dépend le courant
* -f ./module/pom.xml : lance la commande maven sur le pom spécifié
* -DscmCommentPrefix="[skip ci]" : ajoute en début de commit un element
* -Darguments="-Dmaven.deploy.skip=true"  : indique a maven les arguments a ajouter pour toutes les commandes intra maven

1. compiler uniquement le module `spring-petclinic-visits-service`
2. compiler le module `spring-petclinic-tracing-server` et toutes ses dépendances 

---
## Le deploiement continue avec gitlab

### un peu de yml

avant de commencer a creer son gitlab-ci il faut faire de yml.

1. télécharger le convertiseur [yaml2json](https://github.com/bronze1man/yaml2json)
   ```bash
   yaml2json < source.yml > dest.json
   ```
2. maitriser les blocs litérals

   ```yaml
   # Les commandes qui s'executent avant le script de chaque job
   before_script:
     - export JAVA_HOME=$JAVA_HOME_DEFAUT
     - >
       if [[ "$FORCE_JAVA7" == "true" ]]; then
         export JAVA_HOME=$JAVA_HOME_7
       fi
     - export M2_HOME=$M2_HOME
   ```
   quel est la forme du json
3. maitriser les ancres

   ```yaml
   # Les commandes qui s'executent avant le script de chaque job
   common_script: &before
     - export JAVA_HOME=$JAVA_HOME_DEFAUT
     - >
       if [[ "$FORCE_JAVA7" == "true" ]]; then
         export JAVA_HOME=$JAVA_HOME_7
       fi
     - export M2_HOME=$M2_HOME
    
   before_script: *before
   ```
   quel est la forme du json
4. maitriser les extensions

   ```yaml
   # Les commandes qui s'executent avant le script de chaque job
   common_script: &before
     - export JAVA_HOME=$JAVA_HOME_DEFAUT
     - >
       if [[ "$FORCE_JAVA7" == "true" ]]; then
         export JAVA_HOME=$JAVA_HOME_7
       fi
     - export M2_HOME=$M2_HOME
    
    job<<: *before
      stage: specific
      tags:
        - oslxgit02  
   ```
   quel est la forme du json

---
## Deployer son application

### préparation
La **première étape** est de compiler l'application exemple : spring-petclinic

1. Télécharger les sources  (source)
   ```bash
   git clone https://github.com/spring-petclinic/spring-framework-petclinic.git
   git clone https://github.com/spring-projects/spring-petclinic.git
   ```
2. Changer la version de java
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
   ```
3. Lancer la compilation sur le projet spring petclinc en mode war :
   ```bash
   cd spring-framework-petclinic
   ./mvnw clean install
   ```
4. Lancer la compilation sur le projet spring petclinc en mode jar :
   ```bash
   cd spring-petclinic
   ./mvnw clean install
   ```

### Lancement via Tomcat
La **deuxième étape** consiste a installer un tomcat :

1. Télécharger le tomcat 8 [binaires windows](http://apache.mirrors.ovh.net/ftp.apache.org/dist/tomcat/tomcat-8/v8.5.57/bin/apache-tomcat-8.5.57-windows-x64.zip) ou [binaires linux](http://apache.mirrors.ovh.net/ftp.apache.org/dist/tomcat/tomcat-8/v8.5.57/bin/apache-tomcat-8.5.57.zip)
2. démarrer tomcat. 
3. Connaitre le port du tomcat (dans la console trouver la ligne : INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler [http-nio-**8080**]
4. tester l'application (copier le fichier petclinic.war du projet spring-framework-petclinic dans le répertoire webapp).
<br/> tester l'application avec : `http://localhost:8080/petclinic/`
4. Visualiser le status des applications installées `http://localhost:8080/manager/status`
<br/> Le navigateur doit demander un login et un mot de passe. Modifier le fichier tomcat-users.xml en conséquence
5. Visualiser le status des applications installées
6. Visualiser les applications installées
7. Visualiser les logs de l'accès-log dans [tomcat]/logs/acces-log.log
8. Changer le port d'utilisation 
   ```xml
   <Connector port="8080" protocol="HTTP/1.1" ...
   ```


### Lancement via le jar
La **troisème étape** consiste a directement lancer le jar :

1. démarrer l'application par le jar 
   ```bash
   cd spring-petclinic-rest
   java -jar target/spring-petclinic-2.1.0.BUILD-SNAPSHOT.jar
   ```
2. Visualiser les logs de l'accès-log
3. Changer le port de demarrage du tomcat embarqué
   ```bash
   server.port=8081
   ```
   ```bash
   Ou en ligne de commande via --server.port=8083
   ```
4. Configurer le projet spring-boot pour avoir les logs
   ```properties
   server.tomcat.basedir: /home/etud/o2122505/M2/Infra_de_prod/TP1_Tomcat/apache-tomcat-8.5.34/
   server.tomcat.accesslog.enabled=true
   server.tomcat.accesslog.prefix=access_log
   server.tomcat.accesslog.suffix=.log
   server.tomcat.accesslog.pattern='%t %a "%r" %s %D'
   ```

### Lancement dans tomcat via docker
1. Installer l'image tomcat :
   ```bash
    docker pull tomcat:8.5-jre8-slim
   ```
2. lancer la machine docker, les éléments de lancement sont:
   * utiliser le war fourni
   * Le tomcat, dans l'image docker, est installée dans /usr/local/tomcat

### Lancement en jar via docker
Via docker il faut passer par un docker compose :

   ```yaml
   version: '3'
   services:
     petclinic:
       image: java
       volumes:
         - /home/etud/diogene.moulron/spring-petclinic/target:/usr/src/petclinic:ro
       ports:
         - "8090:8080"
       command: "bash -c 'java -jar /usr/src/petclinic/spring-petclinic-2.3.0.BUILD-SNAPSHOT.jar'"
   ```
