package br.com.bossini.fatec_ipi_noite_pdm_firebase_auth_firestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static android.widget.Toast.makeText;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mensagensRecyclerView;
    private ChatAdapter adapter;
    private List <Mensagem> mensagens;
    private FirebaseUser fireUser;
    private CollectionReference collMensagensReference;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_CODE_GPS = 1001;
    private double latitudeAtual;
    private double longitudeAtual;

    private EditText mensagemEditText;

    private void setupFirebase (){
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        collMensagensReference =
                FirebaseFirestore.
                        getInstance().
                        collection("mensagens");

        collMensagensReference.addSnapshotListener((result, e) -> {
           mensagens.clear();
           for (DocumentSnapshot doc : result.getDocuments()){
               Mensagem m = doc.toObject(Mensagem.class);
               mensagens.add(m);
           }
           Collections.sort(mensagens);
           adapter.notifyDataSetChanged();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebase();

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }
        else{

            ActivityCompat.requestPermissions(this,
                    new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mensagemEditText =
                findViewById(R.id.mensagemEditText);
        mensagensRecyclerView =
                findViewById(R.id.mensagensRecyclerView);
        mensagens = new ArrayList<>();
        adapter = new ChatAdapter(this, mensagens);
        LinearLayoutManager llm =
                new LinearLayoutManager(this);
        mensagensRecyclerView.setAdapter(adapter);
        mensagensRecyclerView.setLayoutManager(llm);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                latitudeAtual = lat;
                longitudeAtual = lon;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle
                    extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }

        };

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GPS){
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                //permissão concedida, ativamos o GPS
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, locationListener);
                }
            }
            else{
                //usuário negou, não ativamos
                makeText(this, "Acesso negado!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    public void enviarMensagem(View view) {
        String texto =
                mensagemEditText.getText().toString();
        Mensagem m =
                new Mensagem (texto,
                        new java.util.Date(), fireUser.getEmail());
        collMensagensReference.add(m);
        mensagemEditText.setText("");
    }

    public void enviarLocalizacao(View view) {
        Mensagem m =
                new Mensagem ("geo:"+latitudeAtual+","+longitudeAtual,
                        new java.util.Date(), fireUser.getEmail(), true);
        m.setF1(latitudeAtual);
        m.setF2(longitudeAtual);

        collMensagensReference.add(m);
        mensagemEditText.setText("");
    }
}
class ChatViewHolder extends RecyclerView.ViewHolder{
    public TextView dataNomeTextView;
    public TextView mensagemTextView;
    public ImageButton btnLoc;
    public ChatViewHolder (View raiz){
        super (raiz);
        dataNomeTextView =
                raiz.findViewById(R.id.dataNomeTextView);
        mensagemTextView =
                raiz.findViewById(R.id.mensagemTextView);
        btnLoc =
                raiz.findViewById(R.id.btnLoc);
    }
}



class ChatAdapter extends RecyclerView.Adapter <ChatViewHolder>{

    private Context context;
    private List <Mensagem> mensagens;

    public ChatAdapter(Context context, List<Mensagem> mensagens){
        this.context = context;
        this.mensagens = mensagens;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;
        LayoutInflater inflater = LayoutInflater.from(context);
        View raiz = inflater.inflate(
            R.layout.list_item_loc,
            parent,
            false
        );
        return new ChatViewHolder(raiz);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Mensagem m = mensagens.get(position);
        if(m.getIsLocation()){
            holder.dataNomeTextView.setText(
                    context.getString(
                            R.string.data_nome2,
                            m.getEmail()
                    )
            );
            holder.btnLoc.setVisibility(View.VISIBLE);
            holder.btnLoc.setOnClickListener(v -> {

                Uri gmmIntentUri = Uri.parse(String.format(m.getTexto()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);

            });
            if(m.getF1() == 0 && m.getF2() == 0){
                holder.mensagemTextView.setText("O GPS está desligado ou não há permissão para acessar sua localização");
            }else{
                holder.mensagemTextView.setText(m.getTexto());
            }
        }else{
            holder.dataNomeTextView.setText(
                    context.getString(
                            R.string.data_nome,
                            DateHelper.format(m.getData()),
                            m.getEmail()
                    )
            );
            holder.btnLoc.setVisibility(View.GONE);
            holder.mensagemTextView.setText(m.getTexto());
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }
}

