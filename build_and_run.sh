mvn clean compile assembly:single && java -cp "lib/*:target/decrypt-app-1.2-SNAPSHOT-jar-with-dependencies.jar" com.giosg.decryptapp.App -k example_keys/private.pem -p salaisuus -c example_data/chat.json -m example_data/messages.json