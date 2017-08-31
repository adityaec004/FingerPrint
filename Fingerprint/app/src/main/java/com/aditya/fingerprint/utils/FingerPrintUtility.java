package com.aditya.fingerprint.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created on 8/21/2017.
 */

public class FingerPrintUtility {
    private KeyStore keyStore;
    // Variable used for storing the key in the Android Keystore container
    private static final String KEY_NAME = "YourKey";
    private Cipher cipher;

    public FingerPrintUtility() {

    }

    /*public void callFingerPrint(AppCompatActivity context, View fingerPrint, TextView textView) {
        SharedPreferenceUtility sharedPreferenceUtility = new SharedPreferenceUtility(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        fingerPrint.setVisibility(View.VISIBLE);
        // Initializing both Android Keyguard Manager and Fingerprint Manager
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
            if (!fingerprintManager.isHardwareDetected()) {
                // Device doesn't support fingerprint authentication
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                // User hasn't enrolled any fingerprints to authenticate with
            } else {
                // Everything is ready for fingerprint authentication

                // Check whether the device has a Fingerprint sensor.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!fingerprintManager.isHardwareDetected()) {
                        *//**
                         * An error message will be displayed if the device does not contain the fingerprint hardware.
                         * However if you plan to implement a default authentication method,
                         * you can redirect the user to a default authentication activity from here.
                         * Example:
                         * Intent intent = new Intent(this, DefaultAuthenticationActivity.class);
                         * startActivity(intent);
                         *//*
                        textView.setText("Your Device does not have a Fingerprint Sensor");
//                TODO login if user have no finger print
                        sharedPreferenceUtility.setFingerPrintAvailableOrNOt(false);
                    } else {
                        // Checks whether fingerprint permission is set on manifest
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                            textView.setText("Fingerprint authentication permission not enabled");
                            sharedPreferenceUtility.setFingerPrintAvailableOrNOt(true);
                        } else {
                            // Check whether at least one fingerprint is registered
                            if (!fingerprintManager.hasEnrolledFingerprints()) {
                                textView.setText("Register at least one fingerprint in Settings");
                                sharedPreferenceUtility.setFingerPrintSetupOrNOt(false);

                            } else {
                                // Checks whether lock screen security is enabled or not
                                if (!keyguardManager.isKeyguardSecure()) {
                                    textView.setText("Lock screen security not enabled in Settings");
                                } else {
                                    sharedPreferenceUtility.setFingerPrintSetupOrNOt(true);
                                    generateKey();
                                    if (cipherInit()) {
                                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                                        FingerprintHandler helper = null;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            helper = new FingerprintHandler(context);
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            helper.startAuth(fingerprintManager, cryptoObject);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }*/

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean cipherInit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
//        return false;
    }
}
