package net.gimite.nativeexe;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.gimite.nativeexe.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity {
	
    private TextView outputView;
	private Button localRunButton;
	private EditText localPathEdit;
	private Handler handler = new Handler();
	private EditText urlEdit;
	private Button remoteRunButton;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        outputView = (TextView)findViewById(R.id.outputView);
        localPathEdit = (EditText)findViewById(R.id.localPathEdit);
        localRunButton = (Button)findViewById(R.id.localRunButton);
        localRunButton.setOnClickListener(onLocalRunButtonClick);
        urlEdit = (EditText)findViewById(R.id.urlEdit);
        remoteRunButton = (Button)findViewById(R.id.remoteRunButton);
        remoteRunButton.setOnClickListener(onRemoteRunButtonClick);
    }
    
	private OnClickListener onLocalRunButtonClick = new OnClickListener() {
		public void onClick(View v) {
			String output = exec(localPathEdit.getText().toString());
			output(output);
		}
	};

	private OnClickListener onRemoteRunButtonClick = new OnClickListener() {
		public void onClick(View v) {
			final String url = urlEdit.getText().toString();
			final String localPath = "/data/data/net.gimite.nativeexe/a.out";
			output("Downloading...");
			Thread thread = new Thread(new Runnable() {
				public void run() {
					download(url, localPath);
					exec("/system/bin/chmod 744 " + localPath);
					output("Executing...");
					String output = exec(localPath);
					output(output);
				}
			});
			thread.start();
		}
	};

	// Executes UNIX command.
    private String exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void download(String urlStr, String localPath) {
    	try {
			URL url = new URL(urlStr);
			HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
			urlconn.setRequestMethod("GET");
			urlconn.setInstanceFollowRedirects(true);
			urlconn.connect();
			InputStream in = urlconn.getInputStream();
			FileOutputStream out = new FileOutputStream(localPath);
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			out.close();
			in.close();
			urlconn.disconnect();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    private void output(final String str) {
    	Runnable proc = new Runnable() {
			public void run() {
				outputView.setText(str);
			}
    	};
    	handler.post(proc);
    }
    
}