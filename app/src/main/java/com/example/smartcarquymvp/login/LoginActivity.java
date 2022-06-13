package com.example.smartcarquymvp.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartcarquymvp.NetworkImpl;
import com.example.smartcarquymvp.R;
import com.example.smartcarquymvp.control.ControlActivity;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class LoginActivity extends AppCompatActivity implements ContractLogin.LoginView{

    private TextView lblRegister, lblForgetPassword;
    private EditText txtUsername, txtPassword, txtAddress;
    private Button btnLogin;

    private String username;
    private String uuid;
    private String ipAddress;
    private String password;
    private SSLSocketFactory socketFactory;
    private SharedPreferences sharedPreferences;

    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createUUID();
        uuid = sharedPreferences.getString("UUID", null);
        username = sharedPreferences.getString("username",null);


        //Tạo socket từ Certicate
        setSocketFactory();

        //Tạo presenter cho Login View
        presenter = new LoginPresenter(LoginActivity.this, new NetworkImpl(),socketFactory);

        if(uuid != null && username != null){
            presenter.autoLogin();
        }

        lblRegister = findViewById(R.id.lblRegister);
        lblForgetPassword = findViewById(R.id.lblForgetPassword);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        txtAddress = findViewById(R.id.txtAddress);
        btnLogin = findViewById(R.id.btnLogin);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = String.valueOf(txtUsername.getText());
                password = String.valueOf(txtPassword.getText());
                ipAddress = String.valueOf(txtAddress.getText());
                presenter.sendAccountLogin();
            }
        });
    }

    @Override
    public String getIPAddressServer() {
        return ipAddress;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void changeToControlActivity() {
        Intent it = new Intent(LoginActivity.this, ControlActivity.class);
        startActivity(it);
        Log.v("SmartCar","Changed activity");
        finish();
    }

    @Override
    public void getToast(String notification) {
        Toast.makeText(LoginActivity.this, notification, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void saveUsernamePreference(){
        sharedPreferences = getSharedPreferences("DataPreferences", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("username",username);
        edit.commit();
    }

    private void createUUID(){
        //This method only run a times
        sharedPreferences = getSharedPreferences("DataPreferences", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("FrstTime", false)) {
            // Do update you want here
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("FrstTime", true);
            edit.putString("UUID",uuid);
            edit.commit();
        }
    }

    private void setSocketFactory(){
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = getResources().openRawResource(R.raw.certifate); //open inputstream for keystore file in "raw" folder
            Certificate cert = cf.generateCertificate(caInput);
            System.out.println("cert=" + ((X509Certificate) cert).getSubjectDN());
            caInput.close(); //close inputstream

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);  //load the keystore from file (using password specified when certificate was imported into keystore)
            keyStore.setCertificateEntry("ca", cert);
            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");  //configure SSL Context to use TLS v1.2
            sslContext.init(null, tmf.getTrustManagers(), null);
            socketFactory = sslContext.getSocketFactory();
        }catch(IOException e){
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}