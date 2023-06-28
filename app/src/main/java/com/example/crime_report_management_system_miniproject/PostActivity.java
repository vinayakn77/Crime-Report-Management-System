package com.example.crime_report_management_system_miniproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crime_report_management_system_miniproject.Java_Classes.DataBar;
import com.example.crime_report_management_system_miniproject.Java_Classes.GPSTracker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private ImageButton imgbtn;
    private EditText titleText;
    private EditText descText;
    private TextView latText;
    private TextView lngText;
    private Button submitBtn;
    private Button mapBtn;
    private Uri imagePath = null;
    private ProgressDialog progressBar;

    private DatabaseReference databaseRef,dba,mRef,mRefm, dbloc;
    private StorageReference storageRef;

    private FirebaseAuth mAuth;
    private String typex="";

    static final int GALLERY_REQUEST = 1;


    String[] crimes = { "Murder", "Eve-Teasing", "Theft", "Assault", "Rape", "Kidnapping"};
    private Spinner spin;

    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        imgbtn = (ImageButton) findViewById(R.id.imageSelect);
        titleText = (EditText) findViewById(R.id.titleSelect);
        descText = (EditText) findViewById(R.id.descSelect);
        latText = (TextView) findViewById(R.id.latSelect);
        lngText = (TextView) findViewById(R.id.lngSelect);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        mapBtn = (Button) findViewById(R.id.mapbutt);
        progressBar = new ProgressDialog(this);

        storageRef = FirebaseStorage.getInstance().getReference();
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Crime");
        dbloc = FirebaseDatabase.getInstance().getReference().child("Loc");
        mRef = FirebaseDatabase.getInstance().getReference().child("Stat").child("Year");
        mRefm = FirebaseDatabase.getInstance().getReference().child("Stat").child("Month");
        dba = FirebaseDatabase.getInstance().getReference().child("Admin_Crime");

        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,crimes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);

        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    !=PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will
                //execute every time, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// create class object
                GPSTracker gps = new GPSTracker(PostActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    String latitude = String.valueOf(gps.getLatitude());
                    String  longitude = String.valueOf(gps.getLongitude());

                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                            + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                    latText.setText(latitude);
                    lngText.setText(longitude);
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
//                startActivity(new Intent(PostActivity.this, MapActivity.class));
            }

        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }

        });
    }
    //for spinner crimetype
    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        typex = crimes[position];
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    //method for posting
    private void startPosting() {
        progressBar.setMessage("Posting");
        progressBar.show();
        final String postTitle = titleText.getText().toString().trim();
        final String postDesc = descText.getText().toString().trim();
        final String lat = latText.getText().toString().trim();
        final String lng = lngText.getText().toString().trim();
        final float lat1 = Float.parseFloat(lat);
        final float lng1 = Float.parseFloat(lng);
        final String type = typex;
        mAuth = FirebaseAuth.getInstance();
        if(!TextUtils.isEmpty(type) && !TextUtils.isEmpty(postTitle) && !TextUtils.isEmpty(postDesc) && imagePath != null && !lat.equals("0") && !lng.equals("0") ){
            //path where posts are to be stored on the server
            final StorageReference filePath = storageRef.child("Crime_images").child(UUID.randomUUID().toString());
            //final StorageReference filePath = storageRef;
            final FirebaseUser user1 = mAuth.getCurrentUser();
            final DatabaseReference newpost = databaseRef.push();
            final DatabaseReference loc = dbloc.push();
//            final DatabaseReference adminpost = dba.push();
            //uploading hobe

            filePath.putFile(imagePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  String image = taskSnapshot.getStorage().getDownloadUrl().toString());

                            //Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUri = uri.toString();
                                    newpost.child("image").setValue( downloadUri);
                                    //do your stuff- uri.toString() will give you download URL\\
                                }
                            });
                            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy");
                            final String year = sdf.format(new Date());
                            System.out.println(year);
                            sdf = new SimpleDateFormat("mm");
                            final String mon = sdf.format(new Date());
                            System.out.println(mon);


                            newpost.child("title").setValue(postTitle);
                            newpost.child("description").setValue(postDesc);
                            newpost.child("condition").setValue("Not Seen");
                            newpost.child("latitude").setValue(lat);
                            newpost.child("longitude").setValue(lng);

                            loc.child("lat1").setValue(lat1);
                            loc.child("lng").setValue(lng1);

                            newpost.child("type").setValue(type);
                            newpost.child("uid").setValue(user1.getUid());
                            newpost.child("year").setValue(year);
                            //newpost.child("day").setValue(day);
                            newpost.child("month").setValue(mon);


                            mRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds: dataSnapshot.getChildren()){

                                        DataBar p1 = ds.getValue(DataBar.class);
                                        String xs = ds.getRef().getKey();
                                        Boolean f = false;
                                        if(xs == year)
                                        {
                                            f=true;
                                            float xp = p1.x;
                                            xp++;
                                            ds.getRef().child("x").setValue(xp);
                                        }
                                        if(f==false)
                                        {
                                            ds.getRef().child("x").setValue(1);
                                        }




                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                            mRefm.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds: dataSnapshot.getChildren()){

                                        DataBar p1 = ds.getValue(DataBar.class);
                                        String xs = ds.getRef().getKey();
                                        Boolean f = false;

                                        String sz="";
                                        if(mon=="01") sz="1";
                                        if(mon=="02") sz="2";
                                        if(mon=="03") sz="3";
                                        if(mon=="04") sz="4";
                                        if(mon=="05") sz="5";
                                        if(mon=="06") sz="6";
                                        if(mon=="07") sz="7";
                                        if(mon=="08") sz="8";
                                        if(mon=="09") sz="9";
                                        if(mon=="10") sz="10";
                                        if(mon=="11") sz="11";
                                        if(mon=="12") sz="12";

                                        if(xs == sz)
                                        {
                                            f=true;
                                            float xp = p1.x;
                                            xp++;
                                            ds.getRef().child("x").setValue(xp);
                                        }
                                        if(f==false)
                                        {
                                            ds.getRef().child("x").setValue(1);
                                        }




                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });




                            newpost.child("year").setValue(year);
                            newpost.child("month").setValue(mon);
                            // newpost.child("day").setValue(day);


                            progressBar.dismiss();

                            startActivity(new Intent(PostActivity.this, UIActivity.class));
                            toastMessage("Posted");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.dismiss();
                            toastMessage("Failed to post");
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress =(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressBar.setMessage("Posting "+(int)progress+"%");
                        }
                    });
        }
        //if some fields are empty
        else
        {
            progressBar.dismiss();
            Toast.makeText(this, "Fill all the Fields", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK){

            imagePath = data.getData();
            imgbtn.setImageURI(imagePath);

        }
    }

    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
