# SUIVI DES LOGS

L’objectif de ce TP est de mettre en place une infrastructure pour suivre en temps réel les logs des applications. Ce TP doit être réalisé uniquement sous Docker .


## Les éléments de notation

* fichier de configuration, a mettre dans votre repository git
* Réponse au [qcm](https://docs.google.com/forms/d/11AR2u_pmATVlzOvOcdh_lP7Yx-S0ORKtD9p21kLYIJQ)

## Préparation du test de performance
### Installation

Le générateur de log et les données de géolocalisation sont issus d'un workshop donné par xebia : https://github.com/xebia-france/workshop-kibana

1. télécharger Logstash
    ```bash
    docker pull docker.elastic.co/logstash/logstash:7.4.0
    ```
2. installer Elasticsearch 
3. télécharger Kibana
4. télécharger le générateur de log, [log-generator.jar](log-generator.jar)
5. télécharger les données de géolocalisation, [GeoLite2-City.mmdb.gz](GeoLite2-City.mmdb.gz)
6. télécharger la dernière release de [cerebro](https://github.com/lmenezes/cerebro/releases)

## Configuration des composants
### démarrer les composants 

L'arborescence à mettre en œuvre pour ce TP :

![arboresence](image2019-10-15_14-59-1.png)

Pour tous vos travaux vous utiliserez les liens pour autoriser les communications entre les différents containers.

La partie du docker compose pour lancer elasticsearch est :

```yml
elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.4.0
    environment:
        - xpack.security.enabled=false
        - xpack.monitoring.enabled=false
        - xpack.ml.enabled=false
        - xpack.graph.enabled=false
        - xpack.watcher.enabled=false
        - bootstrap.memory_lock=true
        - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
        - discovery.zen.minimum_master_nodes=1
        - discovery.type=single-node
        - http.cors.enabled:true
        - http.cors.allow-origin:*
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    volumes:
      - /home/etud/diogene.moulron/TP/05/elastic/data:/usr/share/elasticsearch/data:rw
      - /home/etud/diogene.moulron/TP/05/elastic/plugins:/usr/share/elasticsearch/plugins:rw
    ports:
      - "9200:9200"
      - "9300:9300
```

Démarrer logstash :

```yml
logstash:
    image: docker.elastic.co/logstash/logstash:7.4.0
    environment:       
        - xpack.monitoring.enabled=false
        - config.reload.automatic=true
        - verbose=true
    expose:
        - "6540"
    ports:
        - "6540:5000"
    links:
        - elasticsearch:elasticsearch
    volumes:       
        - /home/etud/diogene.moulron/elastic/logstash/conf/pipeline:/usr/share/logstash/pipeline
        - /home/etud/diogene.moulron/elastic/logstash/conf/log4j2.properties:/usr/share/logstash/config/log4j2.properties
```

Le fichier [log4j2](log4j2.properties) et ajouter un fichier logstash.conf avec la configuration par defaut.


```json
input {
     tcp {
        port => 5000
    }
}
filter {}
output {
    stdout { codec => rubydebug{metadata =>  true } }
}
```


Démarrage de kibana :
``̀yml
kibana:
    image: docker.elastic.co/kibana/kibana:7.4.0
    environment:
       - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
        - "5601:5601"
```

> :warning:  Les urls ne sont pas a change

les port correspondent à :

    - 5000: Logstash TCP input
    - 9200: Elasticsearch HTTP
    - 9300: Elasticsearch TCP transport
    - 5601: Kibana

1. Ecrire les lignes de commande docker run de chaque composant
2. Ecrire le docker-compose de lancement des composants (elasticsearch, logstash, kibana)

> :bangbang: Attention aux droits de vos répertoires
> :bangbang: Attention a l'utilisation des ports sur votre machine

Pour toutes les lignes de commande et pour toutes les références entre serveurs, il faut utiliser les alias à la place des ip et/ou host

pour stopper tous les containers 
```bash
docker rm -f $(docker ps -a -q)
```

si vous souhaitez avoir des informations sur la santé et les index de elasticsearch :

Démarrage de cerebro :

```yml
cerebro:
    image: openjdk:8
    volumes:
      - /home/etud/diogene.moulron/elastic/cerebro:/usr/src/cerebro:ro
    ports:
      - "9000:9000"
    command: "bash -c 'cp -r /usr/src/cerebro/cerebro-0.8.4.tgz /tmp/cerebro-0.8.4.tgz; tar -xvf /tmp/cerebro-0.8.4.tgz; /cerebro-0.8.4/bin/cerebro '"
```

Il est aussi possible de démarrer cerebro directement depuis votre poste.


### Configuration et lancer logstash

* Créer un fichier de configuration (logstash.conf) dans le répertoire conf
* Copier le fichier d'information de géolocalisation dans le répertoire conf
* Créer un répertoire patterns dans la conf

* Ajouter une entrée dans logstash.conf
  ```
    input {
        tcp {
            port => 5000
        }
    }
  ```
* ajouter la zone de filtre dans logstash.conf
  ```
    filter {
     
    }
  ```
* configurer la sortie vers ES dans logstash.conf
  ```
    output {
        elasticsearch {
            hosts => "elasticsearch:9200"
        }
    }
  ```
* Lancer le générateur de log pour valoriser ES
  ```bash
    Usage: <main class> [options]
    Options:
            --help
        Default: false
    * -logs, -n
        Number of logs to generate
        -repeat, -r
        Repeat every N milliseconds
        Default: 0
        -threads, -t
        Number of threads to use
        Default: 1
    
    java -jar log-generator.jar -n 5000 -r 1500 | nc 192.168.0.12 5000
    ```
    sous Windows il est possible de lancer [netcat-win32](https://eternallybored.org/misc/netcat/netcat-win32-1.11.zip)
    ```̀bash
    java -jar log-generator.jar -n 500 -r 1500 | netcat-1.11\nc64 192.168.0.12 5000
    ```
    Le log généré est 

    ```bash
    11-11-2016 16:28:24.508 [pool-1-thread-1] INFO com.github.vspiewak.loggenerator.SearchRequest - id=27,ip=90.84.144.93,category=Portable,brand=Apple
    11-11-2016 16:28:24.525 [pool-1-thread-1] INFO com.github.vspiewak.loggenerator.SellRequest - id=28,ip=92.90.16.190,email=client29@gmail.com,sex=M,brand=Apple,name=iPhone 5C,model=iPhone 5C - Jaune - Disque 32Go,category=Mobile,color=Jaune,options=Disque 32Go,price=699.0
    11-11-2016 16:28:24.540 [pool-1-thread-1] INFO com.github.vspiewak.loggenerator.SearchRequest - id=29,ip=93.31.186.100,category=Portable
    11-11-2016 16:28:24.549 [pool-1-thread-1] INFO com.github.vspiewak.loggenerator.SearchRequest - id=30,ip=109.211.12.248,category=Baladeur,brand=Apple,color=Argent,options=Disque 16Go
    11-11-2016 16:28:24.557 [pool-1-thread-1] INFO com.github.vspiewak.loggenerator.SellRequest - id=31,ip=86.73.160.167,email=client32@gmail.com,sex=M,brand=Apple,name=iPad mini,model=iPad mini - Blanc,category=Tablette,color
```

* Consulter les logs générés sous kibana (http://192.168.0.12:5601/)
  la forme doit être du type
    ```json
        {
        "_index": "logstash-2016.11.11",
        "_type": "logs",
        "_id": "AVhUJpEVMJugJY_jEVP5",
        "_score": null,
        "_source": {
            "@timestamp": "2016-11-11T16:09:22.450Z",
            "geoip": {},
            "port": 38968,
            "@version": "1",
            "host": "172.17.0.1",
            "message": "11-11-2016 17:08:51.558 [pool-1-thread-2] INFO com.github.vspiewak.loggenerator.SearchRequest - id=3770,ip=81.251.86.65,category=Baladeur,color=Jaune,options=Disque 32Go\r",
            "tags": [
            "_geoip_lookup_failure"
            ]
        },
        "fields": {
            "@timestamp": [
            1478880562450
            ]
        },
        "sort": [
            1478880562450
        ]
        }
    ```