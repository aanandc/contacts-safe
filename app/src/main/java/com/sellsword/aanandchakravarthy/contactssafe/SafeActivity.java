package com.sellsword.aanandchakravarthy.contactssafe;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import android.graphics.pdf.PdfDocument;

public class SafeActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 73;
    Button convertBtn = null;
    Button exportBtn = null;
    BroadcastReceiver receiver;
    ProgressBar bar;
    Activity thisActivity = null;
    ArrayList<ContactRep> mycontacts = null;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Got the permission
                    startService();
                }
            }

        }

    }

    private void startService(){
        bar.setVisibility(View.VISIBLE);
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(getApplicationContext(),MyService.class);
        startService(serviceIntent);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe);
        thisActivity = this;
        bar = (ProgressBar)findViewById(R.id.progressBar);
        bar.setVisibility(View.INVISIBLE);
        exportBtn = (Button) findViewById(R.id.exportButton);
        exportBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //exportToFile(mycontacts,OUTPUTFORMAT.TXT);
                exportToFile(mycontacts,OUTPUTFORMAT.PDF);
            }
        });
        convertBtn = (Button)findViewById(R.id.convertButton);
        mycontacts = new ArrayList<ContactRep>();
        convertBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(thisActivity,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?

                    ActivityCompat.requestPermissions(thisActivity,
                            new String[]{Manifest.permission.READ_CONTACTS},MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                }
                else{
                    startService();
            }
        }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //String s = intent.getStringExtra(MyService.VASANTH_NO);
                // do something here.
                int val = bar.getProgress();
                val = intent.getIntExtra(MyService.PERCENT,0);
                bar.setProgress(val);
                Log.d("vasanth","progress is " + val);
                ContactRep rep = (ContactRep)intent.getParcelableExtra(MyService.CONTACT);
                mycontacts.add(rep);
                if(val == 100){
                    bar.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MyService.VASANTH_NO)
        );
    }

    public void exportToFile(ArrayList<ContactRep> contactsrep,OUTPUTFORMAT format)  {
        //File txtfile = getDocumentStorageDir("sample.txt");
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        Log.d("vasanth",path.getAbsolutePath());
        Log.d("vasanth","size is " + contactsrep.size());
        File txtfile = new File(path, "sample" + format.ext);
        try {
            if(!path.mkdirs()){
                Log.d("vasanth","Directory not created");
            }
            FileOutputStream fos = new FileOutputStream(txtfile);
            if (format == OUTPUTFORMAT.TXT){
                writeTextContents(fos,contactsrep);
            }
            else if (format == OUTPUTFORMAT.PDF){
                writeToPDFContents(fos,contactsrep);
            }
            else if (format == OUTPUTFORMAT.XML){
                //TODO
            }
            fos.flush();
            fos.close();

        }
        catch(IOException ioe){
            Log.e("vasanth","exception when handling file");
        }

    }

    private PrintAttributes getPrintAttributes() {
        PrintAttributes.Builder builder = new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("res1","Resolution",50,50)).setMinMargins(new PrintAttributes.Margins(5, 5, 5, 5));
        PrintAttributes printAttributes = builder.build();
        return printAttributes;
    }

    private View getContentView() {
        return findViewById(R.id.RelativeMainLayout);
    }

    private ArrayList<ContactRep> duplicateContacts(ArrayList<ContactRep> input){
        ArrayList<ContactRep> newreps = new ArrayList<ContactRep>();
        for(int i=0;i<10;i++){
            newreps.addAll(input);
        }
        return newreps;
    }

    private void writeToPDFContents(FileOutputStream fos,ArrayList<ContactRep> contactsrep) throws IOException {
        // open a new document
        //contactsrep = duplicateContacts(contactsrep);
        PrintedPdfDocument document = new PrintedPdfDocument(this,
                getPrintAttributes());
        // start a page
        int pageno = 0;
        PdfDocument.Page page = document.startPage(pageno);
        // draw something on the page
        //View content = getContentView();
        Canvas mycanvas = page.getCanvas();

        Paint black = new Paint();
        black.setAntiAlias(true);
        black.setARGB(255, 0, 0, 0);
        black.setTextSize(16);
        //mycanvas.drawPaint(black);
        String output = new String();
        int x = 10;
        int y = 25;
        int height = mycanvas.getHeight();

        for(ContactRep rep:contactsrep){
            output = rep.name;
            for(String no:rep.phoneno){
                output = output + " " + no;
            }
            if(y >= height){
                y = 25;
                document.finishPage(page);
                pageno = pageno + 1;
                page = document.startPage(pageno);
                mycanvas = page.getCanvas();
            }
            mycanvas.drawText(output,x,y,black);
            y = y + 20;
        }
        Log.d("vasanth",output.toString());

        //page.

        //content.draw(page.getCanvas());

        // finish the page
        document.finishPage(page);

        // add more pages

        // write the document content
        document.writeTo(fos);

        //close the document
        document.close();
    }
    private void writeTextContents (FileOutputStream fos,ArrayList<ContactRep> contactsrep) throws IOException {
        OutputStreamWriter outDataWriter  = new OutputStreamWriter(fos);
        for(ContactRep rep:contactsrep) {
            outDataWriter.write("\n"+rep.name);

            for(String no:rep.phoneno){
                outDataWriter.write(" " + no);
            }
        }
        outDataWriter.flush();
        outDataWriter.close();
    }

    public File getDocumentStorageDir(String documentname) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), documentname);
        if (!file.mkdirs()) {
            Log.e("vasanth", "Directory not created");
        }
        return file;
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
}


enum OUTPUTFORMAT{
  TXT(".txt"),PDF(".pdf"),XML(".xml");
    public String ext;
    OUTPUTFORMAT(String myext){
        ext = myext;
    }
}




