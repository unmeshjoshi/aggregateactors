An application to demonstrate event sourcing and CQRS with Akka persistence

docker run --rm -p 9042:9042 --name scylla1 scylladb/scylla
docker run --rm -p 9043:9042  --name scylla2 scylladb/scylla --seeds="$(docker inspect --format='{{ .NetworkSettings.IPAddress }}' scylla1)"
docker run --rm -p 9044:9042--name scylla3 scylladb/scylla --seeds="$(docker inspect --format='{{ .NetworkSettings.IPAddress }}' scylla1)"

docker run --rm --name some-scylla -it -p 9042:9042 scylladb/scylla
for cassandra
docker-compose up


$ docker run --name some-scylla2 -d scylladb/scylla --seeds="$(docker inspect --format='{{ .NetworkSettings.IPAddress }}' some-scylla)"


sbt ";project seatavailability; runMain com.moviebooking.apps.SeedApp"
sbt ";project seatavailability; runMain com.moviebooking.apps.ShardApp"
sbt ";project seatavailability; runMain com.moviebooking.apps.ScreenApp"
sbt ";project seatavailability; runMain com.moviebooking.services.SeatAvailabilityService"

curl -i -X POST  http://192.168.43.138:8082/reserve-seats?screenId=Screen1