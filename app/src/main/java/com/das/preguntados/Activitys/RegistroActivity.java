package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.das.preguntados.Common.generateAuthToken;
import com.das.preguntados.Dialogs.DialogMessage;
import com.das.preguntados.R;
import com.das.preguntados.WS.registroWS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //Gestion de eventos
        gestionarButtonRegistrar();
        gestionarLoginText();
    }


    private void gestionarLoginText(){
        //Gestiona los eventos del textView register_LoginText
        TextView textRegister= findViewById(R.id.register_LoginText);
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lanzar actividad login
                finish(); //Finalizo mi actividad
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private void gestionarButtonRegistrar(){
        //Gestiona los eventos del button buttonRegistrar
        Button buttonRegistrar= findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gestionarRegistro();
            }
        });
    }
    private void gestionarRegistro(){
        //Obtengo referencias a los editText
        EditText email_text= findViewById(R.id.register_EmailText);
        EditText nombre_text=findViewById(R.id.register_NombreText);
        EditText usuario_text=findViewById(R.id.register_UsuarioText);
        EditText password_text=findViewById(R.id.register_PasswordText);
        //Obtengo datos introducidos en los editText
        String email=email_text.getText().toString().trim();
        String nombre=nombre_text.getText().toString().trim();
        String usuario=usuario_text.getText().toString().trim();
        String password=password_text.getText().toString().trim();
        //Comprobaciones de datos antes de enviar a BD
        if (email.isEmpty() || nombre.isEmpty() || usuario.isEmpty() || password.isEmpty()){ //Si alguno de los campos es vacio, muestro mensaje de error
            gestionarError(getString(R.string.register_error_camposObligatorios));
        }
        else if (!comprobarCorreo(email)){ //Si el correo no es correcto
            gestionarError(getString(R.string.register_error_correoIncorrecto));
        }
        else if (email.length()>50){ //Si el correo tiene mas de 50 caracteres
            gestionarError(getString(R.string.register_error_longitudCorreo));
        }
        else if (nombre.length()>40){ //Si el nombre completo tiene mas de 40 caracteres
            gestionarError(getString(R.string.register_error_longitudNombre));
        }
        else if (usuario.length()>25){
            gestionarError(getString(R.string.register_error_longitudUsuario));
        }
        else { //Envio los datos a BD
            //Genero el token de autenticacion
            String token=generateAuthToken.generateToken();
            //Peticion contra BD
            Log.d("registro","Verificaciones correctas. Comienza petición a base de datos");
            registrarUsuario(email,nombre,usuario,password,token);
        }
    }
    private void registrarUsuario(String email, String nombre, String usuario, String password, String token){
        //Envia la peticion a BD para registrar un usuario
        Data datos = new Data.Builder()
                .putString("email",email)
                .putString("nombre",nombre)
                .putString("usuario",usuario)
                .putString("clave",password)
                .putString("token",token)
                .build();
        OneTimeWorkRequest registerOtwr= new OneTimeWorkRequest.Builder(registroWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(registerOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta, teniendo en cuenta que es de la forma CODIGO#VALOR
                        if(workInfo != null && workInfo.getState().isFinished()){

                            String resultado=workInfo.getOutputData().getString("resultado");
                            if (resultado!=null) {
                                String[] respuesta= resultado.split("#");
                                if (respuesta[0].equals("ERR")){ //Hay un error
                                    gestionarError(gestionarMensajesError(respuesta[1]));
                                }
                                else if (respuesta[0].equals("OK")){ //Registro completado
                                    Log.d("registro","Usuario "+usuario+" registrado correctamente");
                                }
                            }

                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(registerOtwr);

    }
    private boolean comprobarCorreo(String correo){
        //Valida si un email es correcto o no.
        Pattern patronEmail = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mather = patronEmail.matcher(correo);
        return mather.find();
    }
    private void gestionarError(String message){
        //Muestra un dialog con el error indicado
        DialogFragment dialogoError= DialogMessage.newInstance(getString(R.string.register_alertTitle),message);
        dialogoError.show(getSupportFragmentManager(), "errorRegistro");
        //Muestro el error en los logs
        Log.d("registro",message);
    }

    private String gestionarMensajesError(String message){
        //Gestiona los mensajes de error recibidos en la respuesta
        if (message.equals("register_error_conexionBD")) {
            return getString(R.string.register_error_conexionBD);
        }
        if (message.equals("register_error_consultaBD")){
            return getString(R.string.register_error_consultaBD);
        }
        if (message.equals("register_error_correoExiste")){
            return getString(R.string.register_error_correoExiste);
        }
        if (message.equals("register_error_usuarioExiste")) {
            return getString(R.string.register_error_usuarioExiste);
        }
        if (message.equals("register_error_camposObligatorios")){
            return getString(R.string.register_error_camposObligatorios);
        }
        return "Ha sucedido un error al registrar";
    }
}