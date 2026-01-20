package com.vaultx.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseService {
    private static FirebaseService instance;
    private Firestore db;

    private FirebaseService() {
        try {
            // NOTE: You must place your serviceAccountKey.json in src/main/resources/
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
            
            if (serviceAccount == null) {
                System.err.println("Firebase serviceAccountKey.json NOT found in resources. Banking features will be disabled.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://vaultx-banking.firebaseio.com") // Replace with your URL
                    .setStorageBucket("vaultx-banking.appspot.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            db = FirestoreClient.getFirestore();
            System.out.println("Firebase initialized successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public Firestore getDb() {
        return db;
    }
}
