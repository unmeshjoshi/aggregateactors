
STEP=5 #need to wait or else get exception that the broker already registerd with zookeeper

echo "Starting seed app"

./target/universal/stage/bin/seed-app 2>&1 > seed.log &

echo "Waiting seed app to start"
sleep $STEP

echo "Starting screen app"
./target/universal/stage/bin/screen-app 2>&1 > screen.log &
echo "Waiting screen app to start"
sleep $STEP

echo "Waiting screen-admin-app to start"
./target/universal/stage/bin/screen-admin-service 2>&1 > screen-admin.log &
echo "Waiting screen-admin-app to start"
sleep $STEP

echo "Starting order service"
./target/universal/stage/bin/order-service 2>&1 > order-service.log &
echo "Waiting for order service to start"
sleep $STEP


echo "Starting event reader to publish to kafka"
./target/universal/stage/bin/event-reader 2>&1 > event-reader.log &
echo "Waiting for event reader to start"
sleep $STEP

#
#echo "Starting Kafka subscriber for Redis"
#./target/universal/stage/bin/kafka-subscriber 2>&1 > kafka-subscriber.log &
#echo "Waiting for Kafka subscriber for Redis to start"
#sleep $STEP

echo "Starting seat-availability-service"
./target/universal/stage/bin/seat-availability-service 2>&1 > seat-availability-service.log &
echo "Waiting for seat-availability-service to start"
sleep $STEP


tail -f seed.log screen.log screen-admin.log order-service.log event-reader.log kafka-subscriber.log seat-availability-service.log
