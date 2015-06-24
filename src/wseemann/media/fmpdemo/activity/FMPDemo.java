/*
 * FFmpegMediaPlayer: A unified interface for playing audio files and streams.
 *
 * Copyright 2014 William Seemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wseemann.media.fmpdemo.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import wseemann.media.fmpdemo.service.IMediaPlaybackService;
import wseemann.media.fmpdemo.service.MusicUtils;
import wseemann.media.fmpdemo.service.MusicUtils.ServiceToken;
import wseemann.media.fmpdemo.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import arabiannight.tistory.com.adapter.CustomAdapter;


public class FMPDemo extends FragmentActivity implements ServiceConnection {

    private IMediaPlaybackService mService = null;
	private ServiceToken mToken;
	
	private DbOpenHelper mDbOpenHelper;
	private Cursor mCursor;
	private InfoClass mInfoClass;
	private ArrayList<InfoClass> mInfoArray;
	private CustomAdapter mAdapter;
	
    private EditText[] mEditTexts;
    private ListView mListView;
    
    private LinearLayout listView;
    private LinearLayout rightContainer;
    
    private TextView title_View;
    private Uri uri;
    
    private EditText uriText;
    private Button for_PlayButton;
    private Button listButton;
    private Button goButton;
    private Button homeButton;
    private Button backButton;
    
    private boolean boolMusicPlaying = false;
    
    boolean isListVisible = false;
    boolean isMainVisible = true;
    
    //private EditText uriText;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fmpdemo);
		
		uriText = (EditText) findViewById(R.id.uri);
		listView = (LinearLayout)findViewById(R.id.ListView);
		rightContainer = (LinearLayout)findViewById(R.id.Right_Container);
		title_View = (TextView)findViewById(R.id.Title);
		
		listView.setVisibility(View.INVISIBLE);
		rightContainer.setVisibility(View.VISIBLE);
		
		//for_PlayButton = (Button)findViewById(R.id.play);
		homeButton = (Button)findViewById(R.id.home);
		
		
		
		
        setLayout();
        
        // DB Create and Open
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        
        /*
        mDbOpenHelper.insertColumn("源��깭�씗","01000001111" , "angel@google.com");
        mDbOpenHelper.insertColumn("�넚�삙援�","01333331111" , "asdffff@emdo.com");
        mDbOpenHelper.insertColumn("�궦�떆�옲","01234001111" , "yaya@hhh.com");
        mDbOpenHelper.insertColumn("�젣�떆移�","01600001111" , "tree777@atat.com");
        mDbOpenHelper.insertColumn("�꽦�쑀由�","01700001111" , "tiger@tttt.com");
        mDbOpenHelper.insertColumn("源��깭�슦","01800001111" , "gril@zzz.com");
        */
        
//        startManagingCursor(mCursor);
        
        
        mInfoArray = new ArrayList<InfoClass>();
        
        doWhileCursorToArray();
    
        
        mAdapter = new CustomAdapter(this, mInfoArray);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemLongClickListener(longClickListener);
        mListView.setOnItemClickListener(ClickListener);
        
		//
    	
    	Intent intent = getIntent();
    	
    	// Populate the edit text field with the intent uri, if available
    	uri = intent.getData();
    	
    	if (intent.getExtras() != null &&
    			intent.getExtras().getCharSequence(Intent.EXTRA_TEXT) != null) {
			uri = Uri.parse(intent.getExtras().getCharSequence(Intent.EXTRA_TEXT).toString());
		}
    	
    	if (uri != null) {
    		try {
    			uriText.setText(URLDecoder.decode(uri.toString(), "UTF-8"));
    		} catch (UnsupportedEncodingException e1) {
    		}
    	}
    	
		setIntent(null);
		
		
		
		backButton = (Button)findViewById(R.id.back);
		backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isListVisible == true){
					listView.setVisibility(View.INVISIBLE);
					rightContainer.setVisibility(View.VISIBLE);
					
					isListVisible = false;
					isMainVisible = true;
				} else{
					Intent showOptions = new Intent(Intent.ACTION_MAIN);
					showOptions.addCategory(Intent.CATEGORY_HOME);
					startActivity(showOptions);							
				}
				
			}
		});
		
		homeButton = (Button)findViewById(R.id.home);
		homeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent showOptions = new Intent(Intent.ACTION_MAIN);
				showOptions.addCategory(Intent.CATEGORY_HOME);
				startActivity(showOptions);				

			}
		});
		
    	goButton = (Button) findViewById(R.id.play);
    	goButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
	
				if(boolMusicPlaying==false){
					// Clear the error message
					uriText.setError(null);
					
					// Hide the keyboard
					InputMethodManager imm = (InputMethodManager)
						FMPDemo.this.getSystemService(
						Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(uriText.getWindowToken(), 0);
					
					String uri = uriText.getText().toString();
					
					if (uri.equals("")) {
						uriText.setError(getString(R.string.uri_error));
						return;
					}
					
					String uriString = uriText.getText().toString();
					
					try {
						long [] list = new long[1];
						list[0] = MusicUtils.insert(FMPDemo.this, uriString);
						mService.open(list, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					goButton.setBackgroundResource(R.drawable.icon_list_play);
					boolMusicPlaying = true;
				}else{
					goButton.setBackgroundResource(R.drawable.icon_list_stop);
					boolMusicPlaying = false;
					try {
						mService.pause();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			}
		});
    	
    	listButton = (Button)findViewById(R.id.ListButton);
    	listButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				listView.setVisibility(View.VISIBLE);
				rightContainer.setVisibility(View.INVISIBLE);
				
				isListVisible = true;
				isMainVisible = false;
				
			}
		});
    	
        mToken = MusicUtils.bindToService(this, this);
	}
	
	@Override
	public void onDestroy() {
		MusicUtils.unbindFromService(mToken);
		mService = null;
		mDbOpenHelper.close();
		
		super.onDestroy();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = IMediaPlaybackService.Stub.asInterface(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		finish();
	}
	
    private void doWhileCursorToArray(){
        
        mCursor = null;
        mCursor = mDbOpenHelper.getAllColumns();
        //DLog.e(TAG, "COUNT = " + mCursor.getCount());
          
        while (mCursor.moveToNext()) {
              
            mInfoClass = new InfoClass(
                    mCursor.getInt(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex("name")),
                    mCursor.getString(mCursor.getColumnIndex("contact")),
                    mCursor.getString(mCursor.getColumnIndex("email"))
                    );
              
            mInfoArray.add(mInfoClass);
        }
          
        mCursor.close();
    }		
    
    public void onClick(View v){
        switch (v.getId()) {
        case R.id.btn_add:
            mDbOpenHelper.insertColumn
                    (
                    mEditTexts[Constants.NAME].getText().toString().trim(),
                    mEditTexts[Constants.CONTACT].getText().toString().trim(),
                    mEditTexts[Constants.EMAIL].getText().toString().trim()
                    );
              
            mInfoArray.clear();
              
            doWhileCursorToArray();
              
            mAdapter.setArrayList(mInfoArray);          
            mAdapter.notifyDataSetChanged();
              
            mCursor.close();
              
            break;
  
        default:
            break;
        }
    }    
    
    private OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                int position, long arg3) {
              
            //DLog.e(TAG, "position = " + position);
              
            //boolean result = mDbOpenHelper.deleteColumn(position + 1);
        	//boolean result = mDbOpenHelper.deleteColumn(position + 1);
        	boolean result = mDbOpenHelper.deleteColumn(mInfoArray.get(position)._id);
            //DLog.e(TAG, "result = " + result);
              
            if(result){
                mInfoArray.remove(position);
                mAdapter.setArrayList(mInfoArray);
                mAdapter.notifyDataSetChanged();
            }else {
                Toast.makeText(getApplicationContext(), "INDEX를 확인해 주세요.", 
                        Toast.LENGTH_LONG).show();
            }
              
            return false;
        }
    };

    private OnItemClickListener ClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1,
                int position, long arg3) {
              
        	
        	/*
            mInfoClass = new InfoClass(
                    mCursor.getInt(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex("name")),
                    mCursor.getString(mCursor.getColumnIndex("contact")),
                    mCursor.getString(mCursor.getColumnIndex("email"))
                    );*/
                    
        	
        	//title_View.setText(mInfoArray.get(position).email);
        	
        	title_View.setText(mInfoArray.get(position).name.toString());
            
            title_View.setText(mInfoClass.name);
			listView.setVisibility(View.INVISIBLE);
			rightContainer.setVisibility(View.VISIBLE);
			
			isListVisible = false;
			isMainVisible = true;
			
			
			uriText.setText(mInfoArray.get(position).contact.toString());
			
			boolMusicPlaying = false;
			goButton.performClick();
			
			
			/*
			uriText.setError(null);
			
			
			
			// Hide the keyboard
			InputMethodManager imm = (InputMethodManager)
				FMPDemo.this.getSystemService(
				Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(uriText.getWindowToken(), 0);
			
			//String uri = uriText.getText().toString();
			String uri = title_View.toString();
			
			if (uri.equals("")) {
				uriText.setError(getString(R.string.uri_error));
				return;
			}
			
			
			//String uriString = uriText.getText().toString();
			String uriString = title_View.toString();
			
			try {
				long [] list = new long[1];
				list[0] = MusicUtils.insert(FMPDemo.this, uriString);
				mService.open(list, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			 */
			
        }
    };
    
    private void setLayout(){
        mEditTexts = new EditText[]{
                (EditText)findViewById(R.id.et_name),
                (EditText)findViewById(R.id.et_contact),
                (EditText)findViewById(R.id.et_email)
        };
          
        mListView = (ListView) findViewById(R.id.lv_list);
    }        
    /**
     * DB에서 받아온 값을 ArrayList에 Add
     */
}
