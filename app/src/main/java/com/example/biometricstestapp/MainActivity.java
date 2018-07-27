package com.example.biometricstestapp;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private KeyStore keyStore;
    private BiometricPrompt mBiometricPrompt;

    // Unique identifier of a key pair
    private static final String KEY_NAME = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fingerprint);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSupportBiometricPrompt()) {

                    Log.i(TAG, "Try authentication");

                    // Create biometricPrompt
                    mBiometricPrompt = new BiometricPrompt.Builder(getApplicationContext())
                            .setDescription("Description")
                            .setTitle("Title")
                            .setSubtitle("Subtitle")
                            .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.i(TAG, "Cancel button clicked");
                                }
                            })
                            .build();
                    CancellationSignal cancellationSignal = getCancellationSignal();
                    BiometricPrompt.AuthenticationCallback authenticationCallback = getAuthenticationCallback();

                    try {
                        generateKey();
                        Cipher cipher = generateCipher();
                        BiometricPrompt.CryptoObject crypto = new BiometricPrompt.CryptoObject(cipher);
                        Log.i(TAG, "Show biometric prompt");
                        mBiometricPrompt.authenticate(crypto, cancellationSignal, getMainExecutor(), authenticationCallback);
                    } catch (Exception e) {
                        //
                    }
                }
            }
        });
    }

    private CancellationSignal getCancellationSignal() {
        // With this cancel signal, we can cancel biometric prompt operation
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                //handle cancel result
                Log.i(TAG, "Canceled");
            }
        });
        return cancellationSignal;
    }

    private BiometricPrompt.AuthenticationCallback getAuthenticationCallback() {
        // Callback for biometric authentication result
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                Log.i(TAG, "onAuthenticationSucceeded");
                super.onAuthenticationSucceeded(result);
                    Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        };
    }

    /**
     * Before generating a key pair with biometric prompt, we need to ensure that the device supports fingerprint, iris, or face.
     * Currently, there is no FEATURE_IRIS or FEATURE_FACE available on PackageManager
     * So, for now, only check FEATURE_FINGERPRINT
     * @return true oif supports biometricPrompt, false otherwise
     */
    private boolean isSupportBiometricPrompt() {
        PackageManager packageManager = this.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        }
        return false;
    }

    private void generateKey() throws Exception {
        try {
            // Get the reference to the key store
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            // Key generator to generate the key
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
            keyStore.load(null);
            keyGenerator.init( new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        }
        catch (Exception e) {
            Log.i(TAG, "exception generating key: " + e);
        }
    }

    private Cipher generateCipher() throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        }
        catch (Exception e) {
            Log.i(TAG, "cipher exception: " + e);
        }
        Log.i(TAG, "cipher is null yo :/");
        return null;
    }
}

