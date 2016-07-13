package com.sellsword.aanandchakravarthy.contactssafe;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.Xml;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.graphics.pdf.PdfDocument;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;
//TODO items
//1)Add icon for app
//2)Add support for MOS permission dialog for external storage access



public class SafeActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 73;
    Button convertBtn = null;
    Button exportBtn = null;
    long starttime = 0;

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
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(getApplicationContext(),MyService.class);
        startService(serviceIntent);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        starttime  = System.currentTimeMillis();

        setContentView(R.layout.activity_safe);
        thisActivity = this;
        mycontacts = new ArrayList<ContactRep>();
        //bar = (ProgressBar)findViewById(R.id.progressBar);
        //bar.setVisibility(View.INVISIBLE);
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

        exportBtn = (Button) findViewById(R.id.exportButton);
        exportBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                PopupMenu popup = new PopupMenu(thisActivity, v);
                MenuInflater inflater = popup.getMenuInflater();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.topdfmenu:
                                exportToFile(mycontacts,OUTPUTFORMAT.PDF);
                                return true;
                            case R.id.totxtmenu:
                                exportToFile(mycontacts,OUTPUTFORMAT.TXT);
                                return true;
                            case R.id.toxmlmenu:
                                exportToFile(mycontacts,OUTPUTFORMAT.XML);
                                //exportToFile(mycontacts,OUTPUTFORMAT.PDF);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                inflater.inflate(R.menu.exportformats, popup.getMenu());
                popup.show();



                //exportToFile(mycontacts,OUTPUTFORMAT.TXT);
                //exportToFile(mycontacts,OUTPUTFORMAT.PDF);
            }
        });


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //String s = intent.getStringExtra(MyService.VASANTH_NO);
                // do something here.
                int val = intent.getIntExtra(MyService.PERCENT,0);
                //bar.setProgress(val);
                Log.d("vasanth","progress is " + val);
                ContactRep rep = (ContactRep)intent.getParcelableExtra(MyService.CONTACT);
                mycontacts.add(rep);
                if(val == 100){
                    //bar.setVisibility(View.INVISIBLE);
                    Collections.sort(mycontacts);
                    exportBtn.setEnabled(true);
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

    private String get_filename(){
        DateFormat dateFormat = new SimpleDateFormat("yy_MM_dd_HH_mm");
        Date date = new Date();
        return "contacts_" + dateFormat.format(date); //2014/08/06 15:59:48
    }

    public void exportToFile(ArrayList<ContactRep> contactsrep,OUTPUTFORMAT format)  {
        //File txtfile = getDocumentStorageDir("sample.txt");
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        Log.d("vasanth",path.getAbsolutePath());
        Log.d("vasanth","size is " + contactsrep.size());

        File txtfile = new File(path, get_filename() + format.ext);
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
                startHandlingActivity(txtfile,"application/pdf");

            }
            else if (format == OUTPUTFORMAT.XML){
                //TODO
                writeToXML(fos,contactsrep);
                startHandlingActivity(txtfile,"text/xml");
            }
            fos.flush();
            fos.close();

        }
        catch(IOException ioe){
            Log.e("vasanth","exception when handling file");
        }

    }

    private void startHandlingActivity(File txtfile,String targetMimetype){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(txtfile), targetMimetype);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
        catch(ActivityNotFoundException anfe){
            Toast.makeText(thisActivity,
                    "No Application Available to View" + targetMimetype,
                    Toast.LENGTH_SHORT).show();
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
    private void writeToXML(FileOutputStream fos,ArrayList<ContactRep> contactsrep) throws IOException{
        Log.d("vasanth","write to xml");
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.valueOf(true));
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "contactlist");
        for(ContactRep rep:contactsrep){
            serializer.startTag(null,"contact");
            serializer.attribute(null,"name",rep.name);
            serializer.startTag(null,"phonenos");
            for(String no:rep.phoneno){
                serializer.startTag(null,"no");
                serializer.attribute(null,"phoneno",no);
                serializer.endTag(null,"no");
            }
            serializer.endTag(null,"phonenos");
            serializer.endTag(null,"contact");
        }
        serializer.endTag(null,"contactlist");
        serializer.endDocument();
        serializer.flush();

    }
    private void writeToPDFContents(FileOutputStream fos,ArrayList<ContactRep> contactsrep) throws IOException {
        // open a new document
        //contactsrep = duplicateContacts(contactsrep);
        Paint black = new Paint();
        black.setAntiAlias(true);
        black.setARGB(255, 0, 0, 0);
        black.setTextSize(16);
        PrintedPdfDocument document = new PrintedPdfDocument(this,
                getPrintAttributes());
        int maxwidth = 0;
        for(ContactRep myrep:contactsrep){
             Rect bound = new Rect();
             black.getTextBounds(myrep.name,0,myrep.name.length()-1,bound);
            if (bound.width()> maxwidth){
                maxwidth = bound.width();
            }
        }
        Log.d("vasanth" ,"  maxwidth " + maxwidth);
        //Log.d("vasanth","maxlen is " + maxnamelen);
        // start a page
        int pageno = 0;
        PdfDocument.Page page = document.startPage(pageno);
        // draw something on the page
        //View content = getContentView();
        Canvas mycanvas = page.getCanvas();


        //mycanvas.drawPaint(black);
        String output = new String();
        String phonenos = new String();
        int x = 10;
        int y = 25;
        int height = mycanvas.getHeight();

        for(ContactRep rep:contactsrep){
            x = 10;
            output = rep.name;
            phonenos = "";

            for(String no:rep.phoneno){
                phonenos  = phonenos + " " + no;
            }
            if(y >= height){
                y = 25;
                document.finishPage(page);
                pageno = pageno + 1;
                page = document.startPage(pageno);
                mycanvas = page.getCanvas();
            }
            Log.d("vasanth", "output is  " +  output.toString());
            Rect myrect = new Rect();
            mycanvas.drawText(output,x,y,black);

            black.getTextBounds(output,0,output.length()-1,myrect);
            //black.setStyle(Paint.Style.STROKE);
            Rect mytransRect = new Rect(x,y,x+myrect.width(),y+myrect.height() );

           // mycanvas.drawRect(mytransRect,black);
            x = x + maxwidth + 10;
            mycanvas.drawText(phonenos,x,y,black);

            y = y + 20;
        }


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
    protected void onResume() {
        super.onResume();
        Log.d("vasanth","it look time " + (System.currentTimeMillis()-starttime) +  "milliseconds to resume");

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




