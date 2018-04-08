#!/usr/bin/env bash

wait_tcp_port() {
    local host="$1" port="$2"

    while ! nc -vz $host $port; do
        echo "waiting for port $host $port"
        sleep 1 # wait for 1/10 of the second before check again
    done
}

MAX_TRIES=5

function waitUntilContainerIsReady() {
  attempt=1
  while [ $attempt -le $MAX_TRIES ]; do
    if "$@"; then
      echo "$2 container is up!"
      break
    fi
    echo "Waiting for $2 container... (attempt: $((attempt++)))"
    sleep 5
  done

  if [ $attempt -gt $MAX_TRIES ]; then
    echo "Error: $2 not responding, cancelling set up"
    exit 1
  fi
}


function scyallaDbIsReady() {
  docker-compose -f docker-compose-scylla.yml  logs scyall0 | grep "storage_service - Starting listening for CQL clients on 172.25.0.2:9042"
}

get_default_ip() {
 default_ip="$(route | grep '^default' | grep -o '[^ ]*$' |xargs -n 1 ifconfig |grep 'inet addr:'| cut -d: -f2| awk '{ print $1}')"
 echo "Using default ip $default_ip"
}

docker-compose -f docker-compose-scylla.yml up -d

docker-compose -f docker-compose-scylla.yml logs 2>&1 > docker.log &

waitUntilContainerIsReady scyallaDbIsReady "Scyalla DB"

#wait_tcp_port localhost 7199
#wait_tcp_port localhost 9160
#wait_tcp_port localhost 32181
#wait_tcp_port localhost 29092
#wait_tcp_port localhost 6379
#

get_default_ip

echo "Starting seed app"

./target/universal/stage/bin/seed-app 2>&1 > seed.log &

wait_tcp_port $default_ip 2552
#
echo "Starting Admin Service"
./target/universal/stage/bin/admin-service 2>&1 > admin-service.log &

wait_tcp_port $default_ip 8082

echo "Starting Order Service"
./target/universal/stage/bin/order-service 2>&1 > order-service.log &

wait_tcp_port $default_ip 8083


echo "Starting Event Publisher"
./target/universal/stage/bin/event-publisher 2>&1 > event-publisher.log &


echo "Starting kafka-subscriber"
./target/universal/stage/bin/kafka-subscriber 2>&1 > kafka-subscriber.log &


echo "Starting Movie Service"
./target/universal/stage/bin/movie-service 2>&1 > movie-service.log &


echo "Starting Movie booking web app"
./target/universal/stage/bin/moviebookingapp 2>&1 > moviebookingapp.log &


tail -f docker.log admin-service.log seed.log order-service.log event-publisher.log kafka-subscriber.log movie-service.log moviebookingapp.log