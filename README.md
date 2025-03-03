
# Example app to decrypt Giosg chat messages with Java

This is a simple Java app that decrypts chat messages from Giosg chat messages. It uses customers private key provided in PEM format to decrypt the AES key of the encrypted messages.

App is provided as an example how to implement decryption in Java but could be used as stand alone tool also.

See usage instructions below.

Example data is provided in the `example_data` directory along with example private key in `example_keys`.

## Pre build binaries

Pre build binaries can be found in the `bin` directory. You can run the app like this:

`java -cp bin/decrypt-app-1.2.jar com.giosg.decryptapp.App -k <privatekey.pem> -p <privatekey password> -c <chat.json> -m <messages.json>`. You can also specify to output decrypted messages to a file with `-o <output file>`.

Project comes with some example data so you can test it with the following command:

`java -cp bin/decrypt-app-1.2.jar com.giosg.decryptapp.App -k example_keys/private.pem -p salaisuus -c example_data/chat.json -m example_data/messages.json`.

There is also `run_test.sh` for unix like OS's and `run_test.bat` for Windows that run the app with example data.

## Development setup installation and building the app

Make sure you have Maven installed (version 3.6.3 was used to build this app):
`mvn --version`

Install dependencies:
`mvn dependency:resolve`

Build assembly package with all dependencies bundled into it:
`mvn clean compile assembly:single`

Run the app:
`java -cp target/decrypt-app-1.2-SNAPSHOT-jar-with-dependencies.jar com.giosg.decryptapp.App`

```
usage: -k <privatekey.pem> -p <privatekey password> -c <chat.json> -m
          <messages.json> -o <output file>
 -c,--chat <arg>       Chat JSON file path
 -h,--help             Show help
 -k,--key <arg>        Private key PEM file path
 -m,--messages <arg>   Messages JSON file path
 -o,--out <arg>        Output file path
 -p,--pass <arg>       Private key password
```

While developing you can also build and run the app with provided test data with `./build_and_run.sh`. When you want to release new version, copy the jar file from `target/` to `bin/decrypt-app-1.2.jar` and set correct version.
