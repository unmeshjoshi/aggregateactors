
STEP=5 #need to wait or else get exception that the broker already registerd with zookeeper

echo "Starting seed app"

./target/universal/stage/bin/seed-app 2>&1 > seed.log &

echo "Waiting seed app to start"
sleep $STEP

echo "Waiting screen app to start"
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

 
tail -f seed.log screen.log screen-admin.log order-service.log 
