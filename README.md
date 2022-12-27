# using PostgreSQL as a message bus 

This is based on the [_amazing_ article](https://www.javaadvent.com/2022/12/using-postgres-as-a-message-queue.html) and contributor of this feature, the creator of ByteBuddy and fellow Java Champion, [Rafael Winterhalter (@rafaelcodes)](https://twitter.com/rafaelcodes).


## Notes 
Make sure you evaluate the trigger commented out in the `schema-postgresql.sql` file. 

Make sure the dependency on Postgres is _not_ `runtime`! We need specific classes unique to that `.jar`!