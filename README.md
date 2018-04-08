An end to end application to demonstrate the usage of Akka family of frameworks along with event sourcing and CQRS with Akka persistence. It consists of following building blocks

1. A movie booking web site built with Play framework
2. RESTful APIs backing the website built with Akka HTTP
3. Event Sourcing with Persistent Actors backing HTTP APIs. Persistent Actors form the Write side of CQRS
4. Reactive Kafka to subscribe to Events and populate Redis read store
