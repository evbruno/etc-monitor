# Monitor

`./sbt run`

## Available Features

* Local `df` command *(sys exec)*
* Local `mem` command *(sys exec)*

## To be impl

* Package/assemble/uber-jar
* External config file
* SQL queries (jdbc)
* SSH remote commands
* Arbitrary commands (local & remote)
* Firebase integration
  * live update (stream/socket)
  * rest API
* Authentication / Authorization 
  * Basic auth
* Accumulator persistence
* Error alerts

## Samples / spiking

### Run Ubuntu with ssh (daemon) service

```
$ cd samples/
$ docker build -t ubuntu-ssh -f ubuntu.Dockerfile .
$ docker run -d --name some-ubuntu-ssh -p2222:22 ubuntu-ssh
$ ssh root@YOUR_IP -p 2222
# password is screencast
```

