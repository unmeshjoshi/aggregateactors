ps ax | grep -i 'SeedApp' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'AdminService' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'OrderService' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'EventPublisher' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'MovieService' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'KafkaSubscriber' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'MovieBookingApp' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9