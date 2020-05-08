package sample.note;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatBoxActivity extends AppCompatActivity {
	public RecyclerView myRecylerView ;
	public List<Message> MessageList ;
	public ChatBoxAdapter chatBoxAdapter;
	public  EditText messagetxt ;
	public  Button send ;
	//declare socket object
	private Socket socket;

	public String Nickname ;

	@lombok.SneakyThrows
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_box);

		messagetxt = findViewById(R.id.message);
		send = findViewById(R.id.send);
		// get the nickame of the user
		Nickname= getIntent().getExtras().getString(define.NICKNAME);
		Log.d("nick name", Nickname);
		//connect you socket client to the server
		Log.d("chat server url", define.CHAT_SERVER);
		socket = IO.socket(define.CHAT_SERVER);
		socket.connect();
		socket.emit("join", Nickname);

		//setting up recycler
		MessageList = new ArrayList<>();
		myRecylerView = findViewById(R.id.messagelist);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		myRecylerView.setLayoutManager(mLayoutManager);
		myRecylerView.setItemAnimator(new DefaultItemAnimator());



		// message send action
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//retrieve the nickname and the message content and fire the event messagedetection
				if(!messagetxt.getText().toString().isEmpty()){
					socket.emit("messagedetection",Nickname,messagetxt.getText().toString());

					messagetxt.setText(" ");
				}


			}
		});

		//implementing socket listeners
		socket.on("userjoinedthechat", new Emitter.Listener() {
			@Override
			public void call(final Object... args) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String data = (String) args[0];

						Toast.makeText(ChatBoxActivity.this,data,Toast.LENGTH_SHORT).show();

					}
				});
			}
		});
		socket.on("userdisconnect", new Emitter.Listener() {
			@Override
			public void call(final Object... args) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String data = (String) args[0];

						Toast.makeText(ChatBoxActivity.this,data,Toast.LENGTH_SHORT).show();

					}
				});
			}
		});
		socket.on("message", new Emitter.Listener() {
			@Override
			public void call(final Object... args) {
				runOnUiThread(new Runnable() {
					@lombok.SneakyThrows
					@Override
					public void run() {
						JSONObject data = (JSONObject) args[0];
						//extract data from fired event

						String nickname = data.getString("senderNickname");
						String message = data.getString("message");

						// make instance of message

						Message m = new Message(nickname,message);


						//add the message to the messageList

						MessageList.add(m);

						// add the new updated list to the dapter
						chatBoxAdapter = new ChatBoxAdapter(MessageList);

						// notify the adapter to update the recycler view

						chatBoxAdapter.notifyDataSetChanged();

						//set the adapter for the recycler view

						myRecylerView.setAdapter(chatBoxAdapter);



					}
				});
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		socket.disconnect();
	}
}
