# camel-spring-boot-xa

An example project showing how to do XA transactions with Camel + Narayana on Spring Boot.

## Requirements

- [Apache Maven 3.x](http://maven.apache.org)
- [Apache Artemis 2.0.0](https://activemq.apache.org/artemis/)
- [MySQL 5.7.18](https://www.mysql.com/oem/)
  - [Docker Image](https://hub.docker.com/r/mysql/mysql-server/)

## Preparing

Install and run Apache Artemis [[https://activemq.apache.org/artemis/docs/2.0.0/using-server.html](https://activemq.apache.org/artemis/docs/2.0.0/using-server.html)]

_Note: You might want to configure the `redelivery-delay` and `max-delivery-attempts` in the Artemis `broker.xml` file so that you can see the retries occurring. Take a look at the docs for more info: [[https://activemq.apache.org/artemis/docs/2.0.0/undelivered-messages.html](https://activemq.apache.org/artemis/docs/2.0.0/undelivered-messages.html)]._

Install and run MySQL [https://dev.mysql.com/doc/refman/5.7/en/installing.html]

_Note: For my tests, I chose to run the docker image [https://hub.docker.com/r/mysql/mysql-server/]. You can run it using the command `docker run --name mysql -e MYSQL_DATABASE=example -e MYSQL_ROOT_PASSWORD=Abcd1234 -e MYSQL_ROOT_HOST=172.17.0.1 -p 3306:3306 -d mysql/mysql-server:5.7`. You can then connect and run SQL statements using the command `docker exec -it mysql mysql -uroot -p`._

Build the project source code

```
$ cd $PROJECT_ROOT
$ mvn clean install
```

## Running the example standalone

```
$ cd $PROJECT_ROOT
$ mvn spring-boot:run
```

## Testing the code

There are a couple of test files in the `src/test/data` folder. Any file containing the word "error" will cause a rollback/retry and eventually land on the DLQ.

```
$ cd $PROJECT_ROOT
$ cp src/test/data/message_01.txt target/messages/
```

If the file successfully processed, you will see a message in the DB. You can check it using the SQL command `select * from example.MESSAGES;`. If the file threw an exception (because it had the word "error" in it), it will be retried until the `max-delivery-attempts` is exhausted and then be placed on the `DLQ`. Another route will then pick it up and log it.

## Running the example in OpenShift

It is assumed that:

- OpenShift platform is already running, if not you can find details how to [Install OpenShift at your site](https://docs.openshift.com/container-platform/3.3/install_config/index.html).
- Your system is configured for Fabric8 Maven Workflow, if not you can find a [Get Started Guide](https://access.redhat.com/documentation/en/red-hat-jboss-middleware-for-openshift/3/single/red-hat-jboss-fuse-integration-services-20-for-openshift/)
- You've already created a [PersistentVolume](https://kubernetes.io/docs/concepts/storage/persistent-volumes/) of sufficient size (at least what is requested in src/main/kube/pvc.yaml).

Create a new project:

```
$ oc new-project camel-spring-boot-xa
```

Create the [ServiceAccount](https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/):

```
$ oc create -f src/main/kube/serviceaccount.yml
```

Create the [PersistentVolumeClaim](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims):

```
$ oc create -f src/main/kube/pvc.yml
```

Create the [ConfigMap](https://kubernetes.io/docs/user-guide/configmap/):

```
$ oc create -f src/main/kube/configmap.yml
```

Create the [Secret](https://kubernetes.io/docs/concepts/configuration/secret/):

```
$ oc create -f src/main/kube/secret.yml
```

Add the [Secret](https://kubernetes.io/docs/concepts/configuration/secret/) to the [ServiceAccount](https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/) created earlier:

```
$ oc secrets add sa/camel-spring-boot-xa-sa secret/camel-spring-boot-xa-secret
```

Add the view role to the [ServiceAccount](https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/):

```
$ oc policy add-role-to-user view system:serviceaccount:camel-spring-boot-xa:camel-spring-boot-xa-sa
```

The example can be built and run on OpenShift using the following goals:

```
$ mvn clean install fabric8:resource fabric8:build fabric8:deploy
```

## Testing the code

Follow the same directions from 'Testing the code' section above, but copy the messages into '<PV_MOUNT>/messages/'.

## Notes

- This project contains an `org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory` class implementation. It is the patched version from JIRA issue [ARTEMIS-1255](https://issues.apache.org/jira/browse/ARTEMIS-1255). It will have to be included until the patch is merged into the official codebase.
- This project can only run as a single instance (ie, you cannot scale it up). This is due to the fact that all pods will try to share an Narayana ObjectStore directory and will have conflicts.