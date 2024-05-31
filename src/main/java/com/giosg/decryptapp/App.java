package com.giosg.decryptapp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Chat message decryption example app
 */
public class App {
    private static String appNameAndUsage = "-k <privatekey.pem> -p <privatekey password> -c <chat.json> -m <messages.json> -o <output file>";

    public static void main( String[] args )
    {
        System.out.println( "Hello from decrypter!\n" );

        // Define options
        Options options = new Options();

        Option help = new Option("h", "help", false, "Show help");
        options.addOption(help);

        Option privkey = new Option("k", "key", true, "Private key PEM file path");
        privkey.setRequired(true);
        options.addOption(privkey);

        Option passwd = new Option("p", "pass", true, "Private key password");
        passwd.setRequired(false);
        options.addOption(passwd);

        Option chat = new Option("c", "chat", true, "Chat JSON file path");
        chat.setRequired(true);
        options.addOption(chat);

        Option messagesOpt = new Option("m", "messages", true, "Messages JSON file path");
        messagesOpt.setRequired(true);
        options.addOption(messagesOpt);

        Option output = new Option("o", "out", true, "Output file path");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp(appNameAndUsage, options);
                System.exit(0);
            }

            String pemFilePath = cmd.getOptionValue("key");
            String pemFilePassword = cmd.getOptionValue("pass");
            String chatJsonPath = cmd.getOptionValue("chat");
            String messagesJsonPath = cmd.getOptionValue("messages");

            try {
                PrivateKey privateKey = PEMKeyDecryption.loadPrivateKey(pemFilePath, pemFilePassword);

                String base64EncryptedText = getEncryptedSymmetricKeyFromChat(chatJsonPath);
                byte[] decryptedBytes = PEMKeyDecryption.decrypt(base64EncryptedText, privateKey);
                String decryptedAESKey = new String(decryptedBytes, "UTF-8");

                System.out.println("decryptedAESKey: " + decryptedAESKey);

                JSONObject AESKeyJson = new JSONObject(decryptedAESKey);
                String aesKeyString = AESKeyJson.getString("aesKeyString");
                SecretKey aesKey = AESDecryption.createAESKey(aesKeyString);

                System.out.println("aesKey bytes length: " + aesKey.getEncoded().length);


                JSONObject messageData = getChatMessages(messagesJsonPath);
                JSONArray messages = messageData.getJSONArray("results");


                boolean outputToFile = cmd.hasOption("out");

                if (!outputToFile) {
                    System.out.println("************* Messages *************\n\n");
                } else {
                    System.out.println("Writing to file: " + cmd.getOptionValue("out"));
                }

                ArrayList<String> outputMessages = new ArrayList<>();
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject message = messages.getJSONObject(i);
                    try {
                        // System.out.println("message: " + message.toString());
                        String base64EncryptedChatMessage = message.getString("encrypted_message").replaceAll("\n", "").replaceAll("\r", "");
                        String decryptedMessage = AESDecryption.decrypt(base64EncryptedChatMessage, aesKey);
                        String pubNameField = "sender_public_name";
                        String senderName = (message.has(pubNameField) && !message.isNull(pubNameField)) ? message.getString(pubNameField) : "Visitor";
                        String senderId = message.getString("sender_id");
                        String name = senderId + " : " + senderName;
                        if (!outputToFile) {
                            System.out.println(name + " -> " + decryptedMessage);
                        } else {
                            message.put("decrypted_message", decryptedMessage);
                            outputMessages.add(message.toString(2));
                        }

                    } catch (org.json.JSONException e) {
                        if (message.getString("type") == "msg") {
                            // It is normal that it doesn't exist for join and leave type messages for example
                            System.err.println("Unexpectetly did not find exncrypted content: ");
                            e.printStackTrace();
                        }
                    }
                }
                if (outputToFile) {
                    writeOutputToFile(cmd.getOptionValue("out"), outputMessages);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(appNameAndUsage, options);
            System.exit(1);
        }
    }

    public static String getEncryptedSymmetricKeyFromChat(String chatJsonFilePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(chatJsonFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = contentBuilder.toString();
        JSONObject Chat = new JSONObject(json);

        String encryptedSymmetricKey = Chat.getString("encrypted_symmetric_key");
        return encryptedSymmetricKey.replaceAll("\n", "").replaceAll("\r", "");
    }

    public static JSONObject getChatMessages(String messagesJsonFilePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(messagesJsonFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = contentBuilder.toString();
        JSONObject messages = new JSONObject(json);
        return messages;
    }

    public static void writeOutputToFile(String outputFilePath, ArrayList<String> outputMessages) {
        // Write the ArrayList to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (String item : outputMessages) {
                writer.write(item);
                writer.newLine(); // Add a new line after each item
            }
            System.out.println("List written to file " + outputFilePath + " successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
