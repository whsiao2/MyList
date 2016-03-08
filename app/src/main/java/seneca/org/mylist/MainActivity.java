package seneca.org.mylist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.TextView;
import android.graphics.Color;
import android.view.ContextMenu;
import android.os.Environment;
import java.io.*;
import android.support.v4.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;

    String[] colourNames;
    String[] colourValues;
    TextView info;
    String savingColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //check & set permission
        //Ref: http://stackoverflow.com/questions/33139754/android-6-0-marshmallow-cannot-write-to-sd-card
        boolean hasPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colourNames = getResources().getStringArray(R.array.listArray);
        colourValues = getResources().getStringArray(R.array.listValues);
        ListView lv = (ListView) findViewById(R.id.listView);
        info = (TextView) findViewById(R.id.textView);

        String readColor = ReadSDCard("mysdcard.txt");
        if (readColor != "") {
            savingColor = readColor;
            readColourAndSetBackground(readColor);
            String str_info = getResources().getString(R.string.txt_info);
            str_info += (" " + readColor + " colour!");
            info.setText(str_info);
            Toast.makeText(getBaseContext(), "Read Colour from SD card successfully!", Toast.LENGTH_SHORT).show();
        } else {
            String str_info = getResources().getString(R.string.txt_info);
            str_info += " nothing!";
            info.setText(str_info);
        }

        ArrayAdapter aa = new ArrayAdapter(this, R.layout.activity_listview, colourNames);
        lv.setAdapter(aa);
        registerForContextMenu(lv);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                savingColor = colourNames[position];
                String str_info = getResources().getString(R.string.txt_info);
                str_info += (" " + colourNames[position] + " colour!");
                info.setText(str_info);

                String colorString = colourValues[position];
                setActivityBackgroundColor(Color.parseColor("#" + colorString.substring(2)));//Color.parseColor only accepts the form of string: #FFFFFF

                Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Write colour to SDCard");
        menu.add(0, v.getId(), 0, "Read colour from SDCard");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Write colour to SDCard") {

            if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                Toast.makeText(this, "External SD card not mounted", Toast.LENGTH_LONG).show();
            }

            File sdCardDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/myDir");
            if (!sdCardDir.exists()) {
                sdCardDir.mkdirs();
            }
            File myFile = new File(sdCardDir, "mysdcard.txt");
            if (!myFile.exists())
                Toast.makeText(getBaseContext(), "file is not existed!", Toast.LENGTH_SHORT).show();

            try {
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(savingColor);
                myOutWriter.close();
                fOut.close();
                Toast.makeText(getBaseContext(), "Write colour to SD card successfully!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else if (item.getTitle() == "Read colour from SDCard") {
            try {
                String readColor = ReadSDCard("mysdcard.txt");
                if (readColor != "") {
                    readColourAndSetBackground(readColor);
                    Toast.makeText(getBaseContext(), "Read Colour from SD card successfully!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            return false;
        }
        return true;
    }

    //Set Activity's background colour
    //ref: http://stackoverflow.com/questions/8961071/android-changing-background-color-of-the-activity-main-view
    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    public String ReadSDCard(String strSDfilePath) {
        File sdCardDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/myDir");
        File myFile = new File(sdCardDir.getAbsolutePath(),strSDfilePath);
        StringBuilder sb = new StringBuilder();

        try {
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            String line = null;
            while ((line = myReader.readLine()) != null) {
                sb.append(line);
            }
            myReader.close();
            fIn.close();

        } catch (Exception e) {
            return "";
        }
        return sb.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                } else
                {
                    Toast.makeText(MainActivity.this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
    public void readColourAndSetBackground(String colorName) {
        int index = -1;
        for (int i = 0; i < colourNames.length; i++) {
            if (colourNames[i].equals(colorName)) {
                index = i;
                break;
            }
        }
        String colorString = colourValues[index];
        setActivityBackgroundColor(Color.parseColor("#" + colorString.substring(2)));//Color.parseColor only accepts the form of string: #FFFFFF
    }
}
