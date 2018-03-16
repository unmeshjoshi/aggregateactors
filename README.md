An application to demonstrate event sourcing and CQRS with Akka persistence

docker run --rm --name some-scylla -it -p 9042:9042 scylladb/scylla
for cassandra
docker-compose up

sbt ";project seatavailability; runMain com.moviebooking.apps.SeedApp"
sbt ";project seatavailability; runMain com.moviebooking.apps.ShardApp"
sbt ";project seatavailability; runMain com.moviebooking.apps.ScreenApp"
sbt ";project seatavailability; runMain com.moviebooking.services.SeatAvailabilityService"

curl -i -X POST  http://192.168.43.138:8082/reserve-seats?screenId=Screen1