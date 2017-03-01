

package com.vinay.vinplayer.wifi;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {


	
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;

    private static ProgressDialog mProgressDialog;

    public static String WiFiServerIp = "";
	public static String WiFiClientIp = "";
	static Boolean ClientCheck = false;
	public static String GroupOwnerAddress = "";
	static long ActualFilelength = 0;
	static int Percentage = 0;
	public static String FolderName = "VinPlayer";

	static String path;

	Button button;
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);


		button= (Button) mContentView.findViewById(R.id.btn_start_client);

		if(SharedPreferencesHandler.getBooleanValues(getActivity(),"issender")){

		}else {
			button.setVisibility(View.GONE);
		}

		mContentView.findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				sendcommand();
				//getActivity().startService(command);
			}
		});

		mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        /*Intent intent = new Intent();
                        intent.setType("audio*//*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);*/
						send(Uri.parse("file:///"+VinMediaLists.allSongs.get(0).get("data")));

                    }
                });

        return mContentView;
    }



    
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
*/
   public  void sendcommand(){

	   Intent command =new Intent(getActivity(),FileTransferService.class);
	   command.setAction(FileTransferService.ACTION_SEND_COMMAND);

	   String Ip = SharedPreferencesHandler.getStringValues(
			   getActivity(), "WiFiClientIp");
	   String OwnerIp = SharedPreferencesHandler.getStringValues(
			   getActivity(), "GroupOwnerAddress");
	   if (OwnerIp != null && OwnerIp.length() > 0) {
		   CommonMethods.e("", "inside the check -- >");
		   // if(!info.groupOwnerAddress.getHostAddress().equals(LocalIp)){
		   String host=null;
		   int  sub_port =-1;

		   String ServerBool = SharedPreferencesHandler.getStringValues(getActivity(), "ServerBoolean");
		   if (ServerBool!=null && !ServerBool.equals("") && ServerBool.equalsIgnoreCase("true")) {

			   //-----------------------------
			   if (Ip != null && !Ip.equals("")) {
				   CommonMethods.e(
						   "in if condition",
						   "Sending data to " + Ip);
				   // Get Client Ip Address and send data
				   host=Ip;
				   sub_port=FileTransferService.PORT;
				   command
						   .putExtra(
								   FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
								   Ip);
			   }
		   } else {
			   CommonMethods.e(
					   "in else condition",
					   "Sending data to " + OwnerIp);

			   FileTransferService.PORT = 8888;

			   host=OwnerIp;
			   sub_port=FileTransferService.PORT;
			   command
					   .putExtra(
							   FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
							   OwnerIp);
		   }
		   command.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
		   if(host !=null && sub_port!=-1)
		   {
			   CommonMethods.e("Going to intiate service", "service intent for initiating transfer");
			   getActivity().startService(command);
		   }
		   else {
			   CommonMethods.DisplayToast(getActivity(),
					   "Host Address not found, Please Re-Connect");
		   }

	   } else {

		   CommonMethods.DisplayToast(getActivity(),
				   "Host Address not found, Please Re-Connect");
	   }
    }



	public void send(Uri uri1){
        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
    //	if(resultCode == getActivity().RESULT_OK){
			Uri uri=uri1;//data.getData();

			Log.i("asdfg",uri.toString());

		//	Log.i("asdfg",data.getData().toString());


    	        /*
    	         * get actual file name and size of file, it will be send to socket and recieved at other device.
    	         * File size help in displaying progress dialog actual progress.
    	         */
    		 String selectedfilePath = null;
    		 try {
    			 selectedfilePath =uri.getPath();
						 /*CommonMethods.getPath(uri,
     					getActivity());*/

     			Log.e("Original  ", selectedfilePath);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
    			String Extension = "";
    			if(selectedfilePath!=null){
    				File f = new File(selectedfilePath);
        			System.out.println("file name is   ::" + f.getName());
        			Long FileLength = f.length();
        			ActualFilelength = FileLength;
        			try {
        				Extension = f.getName();
        				Log.e("Name of File-> ", "" + Extension);
        			} catch (Exception e) {
        				// TODO: handle exception
        				e.printStackTrace();
        			}
    			}
    			else{
    				CommonMethods.e("", "path is null");
    				return;
    			}
    			
    			
    	        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
    	        statusText.setText("Sending: " + uri);
    	        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
    	        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
    	        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
    	        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
    	        /*
    	         * Choose on which device file has to send weather its server or client
    	         */
    	        String Ip = SharedPreferencesHandler.getStringValues(
    					getActivity(), "WiFiClientIp");
    			String OwnerIp = SharedPreferencesHandler.getStringValues(
    					getActivity(), "GroupOwnerAddress");
    			if (OwnerIp != null && OwnerIp.length() > 0) {
    				CommonMethods.e("", "inside the check -- >");
    				// if(!info.groupOwnerAddress.getHostAddress().equals(LocalIp)){
    				String host=null;
    				int  sub_port =-1;
    				
    				String ServerBool = SharedPreferencesHandler.getStringValues(getActivity(), "ServerBoolean");
    				if (ServerBool!=null && !ServerBool.equals("") && ServerBool.equalsIgnoreCase("true")) {
    					
    					//-----------------------------
    					if (Ip != null && !Ip.equals("")) {
    						CommonMethods.e(
    								"in if condition",
    								"Sending data to " + Ip);
    						// Get Client Ip Address and send data
    						host=Ip;
    						sub_port=FileTransferService.PORT;
    							serviceIntent
    									.putExtra(
    											FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
    											Ip);
//    							serviceIntent
//    									.putExtra(
//    											WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    											WiFiFileTransferService.CLIENTPORT);
    					
    					}
    				

    				} else {
    					CommonMethods.e(
    							"in else condition",
    							"Sending data to " + OwnerIp);
    					
    					FileTransferService.PORT = 8888;
    					
    					host=OwnerIp;
    					sub_port=FileTransferService.PORT;
    					serviceIntent
    							.putExtra(
    									FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
    									OwnerIp);

//    					serviceIntent
//    							.putExtra(
//    									WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    									WiFiFileTransferService.PORT);
    					// anuj
    					

    				}
    				

    				serviceIntent.putExtra(FileTransferService.Extension, Extension);

    				serviceIntent.putExtra(FileTransferService.Filelength,
    						ActualFilelength + "");
    				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
    				if(host !=null && sub_port!=-1)
    				{
    					CommonMethods.e("Going to intiate service", "service intent for initiating transfer");
    					showprogress("Sending...");
    					getActivity().startService(serviceIntent);
    				}
    				else {
    					CommonMethods.DisplayToast(getActivity(),
    							"Host Address not found, Please Re-Connect");
    					DismissProgressDialog();
    				}
    				
    			} else {
    				DismissProgressDialog();
    				CommonMethods.DisplayToast(getActivity(),
    						"Host Address not found, Please Re-Connect");
    			}
    	/*}
    	else{
    		CommonMethods.DisplayToast(getActivity(), "Cancelled Request");
    	}*/
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
//                info.groupOwnerAddress.getHostAddress());
        
//        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        if(info.groupOwnerAddress.getHostAddress()!=null)
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        else{
        	CommonMethods.DisplayToast(getActivity(), "Host Address not found");
        }
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        try {
			String GroupOwner = info.groupOwnerAddress.getHostAddress();
			if(GroupOwner!=null && !GroupOwner.equals("")) SharedPreferencesHandler.setStringValues(getActivity(),
					"GroupOwnerAddress", GroupOwner);
			mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
        if (info.groupFormed && info.isGroupOwner) {
        	/*
        	 * set shaerdprefrence which remember that device is server.
        	 */
        	SharedPreferencesHandler.setStringValues(getActivity(),
					"ServerBoolean", "true");
        	
            /*new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();*/
        	FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
					getActivity(), FileTransferService.PORT);
			if (FileServerobj != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					FileServerobj.executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { null });
					// FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Void);
				}
				else
					FileServerobj.execute();
			}
        }
        else  {
            // The other device acts as the client. In this case, we enable the
            // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
        	if (!ClientCheck) {
				firstConnectionMessage firstObj = new firstConnectionMessage(
						GroupOwnerAddress);
				if (firstObj != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						firstObj.executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR,
								new String[] { null });
					} else
						firstObj.execute();
				}
			}
        	
        	FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
					getActivity(), FileTransferService.PORT);
			if (FileServerobj != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					FileServerobj.executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { null });
				}
				else
					FileServerobj.execute();

			}

        }
        }
        catch(Exception e){
        	
        }

        
		
	 
    }


    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
        /*
         * Remove All the prefrences here
         */
        SharedPreferencesHandler.setStringValues(getActivity(),
    			"GroupOwnerAddress", "");
    	SharedPreferencesHandler.setStringValues(getActivity(),
    			"ServerBoolean", "");
    	SharedPreferencesHandler.setStringValues(getActivity(),
    			"WiFiClientIp", "");
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    static Handler handler;
    public static class FileServerAsyncTask extends AsyncTask<String, String, String> {

//        private TextView statusText;
        private Context mFilecontext;
		private String Extension, Key;
		private File EncryptedFile;
		private long ReceivedFileLength;
		private int PORT;

        public FileServerAsyncTask(Context context, int port) {
            this.mFilecontext = context;
//            this.statusText = (TextView) statusText;
            handler = new Handler();
			this.PORT = port;
//			myTask = new FileServerAsyncTask();
			if (mProgressDialog == null)
				mProgressDialog = new ProgressDialog(mFilecontext,
						ProgressDialog.THEME_HOLO_LIGHT);
        }
        

		@Override
        protected String doInBackground(String... params) {
            try {
				CommonMethods.e("File Async task port", "File Async task port-> " + PORT);
				// init handler for progressdialog
				ServerSocket serverSocket = new ServerSocket(PORT);

				Log.d(CommonMethods.Tag, "Server: Socket opened");
				Socket client = serverSocket.accept();

				//InputStream inputStream = client.getInputStream();
				if (false) {


					return "vinay";
				} else {

					Log.d("Client's InetAddresssss", "" + client.getInetAddress());

					WiFiClientIp = client.getInetAddress().getHostAddress();
					ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
					WiFiTransferModal obj = null;
					// obj = (WiFiTransferModal) ois.readObject();
					String InetAddress;
					try {
						obj = (WiFiTransferModal) ois.readObject();
						//if (obj!=null) {

						InetAddress = obj.getInetAddress();
						if (InetAddress != null
								&& InetAddress
								.equalsIgnoreCase(FileTransferService.inetaddress)) {
							CommonMethods.e("File Async Group Client Ip", "port-> "
									+ WiFiClientIp);
							SharedPreferencesHandler.setStringValues(mFilecontext,
									"WiFiClientIp", WiFiClientIp);
							CommonMethods
									.e("File Async Group Client Ip from SHAREDPrefrence",
											"port-> "
													+ SharedPreferencesHandler
													.getStringValues(
															mFilecontext,
															"WiFiClientIp"));
							//set boolean true which identifiy that this device will act as server.
							SharedPreferencesHandler.setStringValues(mFilecontext,
									"ServerBoolean", "true");
							ois.close(); // close the ObjectOutputStream object
							// after saving
							serverSocket.close();

							return "Demo";
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					final Runnable r = new Runnable() {

						public void run() {
							// TODO Auto-generated method stub
							mProgressDialog.setMessage("Receiving...");
							mProgressDialog.setIndeterminate(false);
							mProgressDialog.setMax(100);
							mProgressDialog.setProgress(0);
							mProgressDialog.setProgressNumberFormat(null);
//						mProgressDialog.setCancelable(false);
							mProgressDialog
									.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
							mProgressDialog.show();
						}
					};
					handler.post(r);
					Log.e("FileName got", obj.getFileName());
					if (obj.getFileName().equals("ok")) {
						ois.close();
						serverSocket.close();
						return "vinay";
					} else {

						final File f = new File(
								Environment.getExternalStorageDirectory() + "/"
										+ FolderName + "/"
										+ obj.getFileName());

						File dirs = new File(f.getParent());
						if (!dirs.exists())
							dirs.mkdirs();
						f.createNewFile();
				
				/*
				 * Recieve file length and copy after it
				 */
						this.ReceivedFileLength = obj.getFileLength();

						InputStream inputstream = client.getInputStream();


						copyRecievedFile(inputstream, new FileOutputStream(f),
								ReceivedFileLength);
						ois.close(); // close the ObjectOutputStream object after saving
						// file to storage.
						serverSocket.close();

				/*
				 * Set file related data and decrypt file in postExecute.
				 */
						this.Extension = obj.getFileName();
						this.EncryptedFile = f;

						return f.getAbsolutePath();
					}
				}

			}catch(IOException e){
					//Log.e(WiFiDirectActivity.TAG, e.getMessage());
//				Toast.makeText(VinPlayer.applicationContext,"command",Toast.LENGTH_SHORT).show();
						return null;
			}

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

				if(result.equals("vinay")){
					Toast.makeText(VinPlayer.applicationContext,"asdfghjkl;",Toast.LENGTH_SHORT).show();
					FileServerAsyncTask FileServerobj = new
							FileServerAsyncTask(mFilecontext,FileTransferService.PORT);

					if(path!=null) {
						MediaPlayer mediaPlayer = new MediaPlayer();
						mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
							try {
								mediaPlayer.setDataSource(path);
								mediaPlayer.prepare();
								mediaPlayer.start();
							} catch (IOException e) {
								e.printStackTrace();
							}

					}


					if(FileServerobj != null) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							FileServerobj.executeOnExecutor (AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null });

						}
						else FileServerobj.execute();

					}
				}
				else if(!result.equalsIgnoreCase("Demo")){

					path = result;/*
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse("file://" + result), "audio*//*");
					mFilecontext.startActivity(intent);*/
				}
            	else{
            		/*
					 * To initiate socket again we are intiating async task
					 * in this condition.
					 */
					  FileServerAsyncTask FileServerobj = new
					  FileServerAsyncTask(mFilecontext,FileTransferService.PORT); 
					  if(FileServerobj != null) { 
						  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					  FileServerobj.executeOnExecutor (AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null }); 
					  
					  }
					  else FileServerobj.execute();
					  
					  }
            	}
//                statusText.setText("File copied - " + result);
                
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
        	if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(mFilecontext);
			}
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
    	long total = 0;
		long test = 0;
		byte buf[] = new byte[FileTransferService.ByteSize];
		int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                try {
					total += len;
					if (ActualFilelength > 0) {
						Percentage = (int) ((total * 100) / ActualFilelength);
					}
					// Log.e("Percentage--->>> ", Percentage+"   FileLength" +
					// EncryptedFilelength+"    len" + len+"");
					mProgressDialog.setProgress(Percentage);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					Percentage = 0;
					ActualFilelength = 0;
				}
            }
            if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
/*
			Thread.sleep(1000);
			VinMedia.getInstance().startMusic(0,VinPlayer.applicationContext);*/

            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
		return true;
    }

    public static boolean copyRecievedFile(InputStream inputStream,
			OutputStream out, Long length) {


		if(length<250){
			java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
			String sg1 =  s.hasNext() ? s.next() : "";
				Toast.makeText(VinPlayer.applicationContext,sg1,Toast.LENGTH_SHORT).show();

		}else{

		byte buf[] = new byte[FileTransferService.ByteSize];
		byte Decryptedbuf[] = new byte[FileTransferService.ByteSize];
		String Decrypted;
		int len;
		long total = 0;
		int progresspercentage = 0;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				try {
					out.write(buf, 0, len);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					total += len;
					if (length > 0) {
						progresspercentage = (int) ((total * 100) / length);
					}
					mProgressDialog.setProgress(progresspercentage);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					if (mProgressDialog != null) {
						if (mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
					}
				}
			}
			// dismiss progress after sending
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			Log.d(WiFiDirectActivity.TAG, e.toString());
			return false;
		}

	}
		return true;
	}
    
    public void showprogress(final String task) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity(),
					ProgressDialog.THEME_HOLO_LIGHT);
		}
		Handler handle = new Handler();
		final Runnable send = new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				mProgressDialog.setMessage(task);
				// mProgressDialog.setProgressNumberFormat(null);
				// mProgressDialog.setProgressPercentFormat(null);
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
//				mProgressDialog.setCancelable(false);
				mProgressDialog.setProgressNumberFormat(null);
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.show();
			}
		};
		handle.post(send);
	}
    
    public static void DismissProgressDialog() {
		try {
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
    
    
    /*
     * Async class that has to be called when connection establish first time. Its main motive is to send blank message
     * to server so that server knows the IP address of client to send files Bi-Directional.
     */
    class firstConnectionMessage extends AsyncTask<String, Void, String> {

		String GroupOwnerAddress = "";

		public firstConnectionMessage(String owner) {
			// TODO Auto-generated constructor stub
			this.GroupOwnerAddress = owner;

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			CommonMethods.e("On first Connect", "On first Connect");
			
			Intent serviceIntent = new Intent(getActivity(),
					WiFiClientIPTransferService.class);

			serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

			if (info.groupOwnerAddress.getHostAddress() != null) {
				serviceIntent.putExtra(
						FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
						info.groupOwnerAddress.getHostAddress());

				serviceIntent.putExtra(
						FileTransferService.EXTRAS_GROUP_OWNER_PORT,
						FileTransferService.PORT);
				serviceIntent.putExtra(FileTransferService.inetaddress,
						FileTransferService.inetaddress);

			}

			getActivity().startService(serviceIntent);

			return "success";
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result!=null){
				if(result.equalsIgnoreCase("success")){
					CommonMethods.e("On first Connect",
							"On first Connect sent to asynctask");
					ClientCheck = true;
				}
			}
			
		}

	}
}
