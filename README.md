# gearpump-broker

Cloud foundry broker for GEARPUMP.

GearPump broker spawns dashboard on Cloud Foundry (using application-broker) and submits yarn service.
That's why it needs yarn service instance and gearpump-dashboard already prepared to work.

# How to use it?
To use gearpump-broker, you need to build it from sources configure, deploy, create instance and bind it to your app. Follow steps described below.

## Build
Run command for compile and package.:
```
mvn clean package
```

## Deploy
Push broker binary code to cloud foundry (use cf client).:
```
cf push gearpump-broker -p target/gearpump-broker-*.jar -m 512M -i 1 --no-start
```

## Kerberos configuration
Broker should be bind to existing kerberos provided service. This will provide default kerberos configuration, for REALM and KDC host.

## Configure

For strict separation of config from code (twelve-factor principle), configuration must be placed in environment variables.

Broker configuration params list (environment properties):

* obligatory :
  * USER_PASSWORD - password to interact with service broker
* optional :
  * BASE_GUID - base id for catalog plan creation (uuid)
  * GEARPUMP_URI - gearpump address
  * CF_CATALOG_SERVICENAME - service name in cloud foundry catalog (default: gearpump)
  * CF_CATALOG_SERVICEID - service id in cloud foundry catalog (default: gearpump)

## Zookeeper configuration
Broker instance should be bind with zookeeper broker instance to get zookeeper configuration.
```
cf bs <app> <zookeeper-instance>
```

## Push service broker application

Check, if in your organization, there are following services:
* sso
* zookeeper-gearpump
* kerberos-service (user-provided-service)
* hdfs-gearpump
* yarn-gearpump

If not, create them.

Before building, put a binary gearpump version to path: ``src/main/resources/gearpump``.

In manifest yml, define gearpump version by following the template - if the binary is called ``gearpump-pack-2.11.5-0.7.1-SNAPSHOT.tar.gz``, the version is: ``GEARPUMP_PACK_VERSION: "2.11.5-0.7.1-SNAPSHOT"``

Make sure, that there’s application-broker up and running, and dashboard service available (probably in seedorg/seedspace).

Build broker using ``mvn clean package``.

Set ``CF_CATALOG_SERVICENAME: gearpump-broker``, ``CF_CATALOG_SERVICEID: gearpump-broker``, ``GEARPUMP_DASHBOARD_NAME: dashboard`` variables in manifest.yml.

Push application using ``cf push``

Create new service broker:
```
cf create-service-broker gearpump-broker <user> <password> https://gearpump-broker.<platform_domain>
cf enable-service-access gearpump
```

Now you can create new service broker from marketplace ``cf cs gearpump shared <instance_name>`` or from console in section "marketplace".

## Binding broker instance

Broker instance can be bind with cf client :
```
cf bs <app> gearpump-instance
```
or by configuration in app's manifest.yml :
```yaml
  services:
    - gearpump-instance
```

To check if broker instance is bound, use cf client :
```
cf env <app>
```
and look for :
```yaml
  "gearpump": [
   {
    "credentials": {
     "kerberos": {
      "kdc": "ip-10-10-9-198.us-west-2.compute.internal",
      "krealm": "US-WEST-2.COMPUTE.INTERNAL"
     }
    },
    "label": "gearpump",
    "name": "gearpump-instance",
    "plan": "shared",
    "tags": []
   }
  ]
```
in VCAP_SERVICES.
