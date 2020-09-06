# toilapp

### How to setup MongoDB for local development using Docker

In order to use multi-document transactions (=`@Transactional` annotation) you need to have a **replica set** mongodb
environment setup. 

Refer to **docker-compose.yml** file in the repository:

```yaml
version: '3.8'

services:
  mongo:
    image: mongo
    hostname: mongo.
    ports:
      - 27017:27017
    networks:
      default:
        aliases:
          - mongo.repl
    volumes:
      - ./db-data:/data/db1
    command:
      - --storageEngine
      - wiredTiger
      - --replSet
      - repl
    depends_on:
      - mongo-2
      - mongo-3

  mongo-2:
    image: mongo
    hostname: mongo-2.repl
    ports:
      - 27018:27017
    networks:
      default:
        aliases:
          - mongo-2.repl
    volumes:
      - ./db-data:/data/db2
    command:
      - --storageEngine
      - wiredTiger
      - --replSet
      - repl

  mongo-3:
    image: mongo
    hostname: mongo-3.repl
    ports:
      - 27019:27017
    networks:
      default:
        aliases:
          - mongo-3.repl
    volumes:
      - ./db-data:/data/db3
    command:
      - --storageEngine
      - wiredTiger
      - --replSet
      - repl
```

The important part is the command `--replSet REPL_NAME`. This defines the three mongo services
as a **replica set** of name **REPL_NAME**. The easiest way to setup the **PRIMARY** and **SECONDARIES** is by opening
a **mongo shell** on the database that should become the primary. In the example above it is the
mongo image which runs on port 27017. Inside the shell, run following commands:

```
mongo
rs.initiate()
```

Now you have successfully created a replica set mongodb environment. The only thing missing is
to create a new database in the primary with name **toilapp-db**.

This can be done again via the mongo shell:

```
use toilapp-db
```

For more details refer to:
- [Official MongoDB Manual](https://docs.mongodb.com/manual/tutorial/convert-standalone-to-replica-set/)
