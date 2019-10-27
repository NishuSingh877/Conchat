package com.example.conchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String myName,myMobile;
    EditText editText;
    Button buttonK;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    DatabaseReference databaseReference;
    private FirebaseRecyclerOptions adapter;
    private List<FirebaseTextMessage> messageList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText=findViewById(R.id.tye);
        buttonK=findViewById(R.id.send);
        recyclerView=findViewById(R.id.re);
        linearLayout=findViewById(R.id.suggestionParent);

        if (getIntent().hasExtra("name")){
            myName  =getIntent().getStringExtra("name");
            myMobile=getIntent().getStringExtra("mobile");

        }

        databaseReference= FirebaseDatabase.getInstance().getReference().child("chats");
        buttonK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message= editText.getText().toString();
                MessagePojo pojo=new MessagePojo();
                pojo.setMobile(myMobile);
                pojo.getName(myName);
                pojo.setMessage(message);
                pojo.setTimestamp(System.currentTimeMillis());
                databaseReference.push().setValue(pojo);
                editText.setText("");

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new FirebaseRecyclerOptions.Builder<MessagePojo>()
                .setQuery(databaseReference.orderByChild("timestamp"),MessagePojo.class)
                .build();
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessagePojo pojo=dataSnapshot.getValue(MessagePojo.class);
                assert pojo != null;
                if (pojo.getMobile().equals(myMobile)){
                    messageList.add(FirebaseTextMessage.createForLocalUser(pojo.getMessage(),pojo.getTimestamp()));
                }else {
                    messageList.add(FirebaseTextMessage.createForRemoteUser(pojo.getMessage(),pojo.getTimestamp(),pojo.getMobile()));
                }
                suggestReplies();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




     /*   conversation.add(FirebaseTextMessage.createForLocalUser(
                "heading out now", System.currentTimeMillis()));
        conversation.add(FirebaseTextMessage.createForRemoteUser(
                "Are you coming back soon?", System.currentTimeMillis(), userId));
        buttonK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
                smartReply.suggestReplies(conversation)
                        .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                            @Override
                            public void onSuccess(SmartReplySuggestionResult result) {
                                if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                    // The conversation's language isn't supported, so the
                                    // the result doesn't contain any suggestions.
                                } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                    //for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                                      //  String replyText = suggestion.getText();
                                  //  }
                                    // Task completed successfully
                                    // ...
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });

            }



        }); */
    }

    private void suggestReplies() {
        final FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
        smartReply.suggestReplies(messageList)
                .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onSuccess(SmartReplySuggestionResult result) {
                        linearLayout.removeAllViews();

                        for (SmartReplySuggestion suggestion:result.getSuggestions()){
                            View view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.suggestion,null,false);
                            final TextView reply=view.findViewById(R.id.smartReply);
                            reply.setText(suggestion.getText());


                            reply.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                     String message= editText.getText().toString();
                                    MessagePojo pojo=new MessagePojo();
                                    pojo.setMobile(myMobile);
                                    pojo.setName(myName);
                                    pojo.setMessage(reply.getText().toString());
                                    pojo.setTimestamp(System.currentTimeMillis());
                                    databaseReference.push().setValue(pojo);

                                }
                            });

                            linearLayout.addView(view);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });


    }



    @Override
    protected void onStart() {

        super.onStart();
        FirebaseRecyclerAdapter<MessagePojo,MyViewHolder> recyclerAdapter=new FirebaseRecyclerAdapter<MessagePojo, MyViewHolder>(adapter) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i, @NonNull MessagePojo messagePojo) {
                myViewHolder.message.setText(messagePojo.getMessage());
                myViewHolder.name.setText(messagePojo.getName(myName));

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if (viewType==1){
                    view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.send,parent,false);
                }else {
                    view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.reciever,parent,false);
                }
                return  new MyViewHolder(view);
            }

            @Override
            public int getItemViewType(int position) {

                MessagePojo pojo= getItem(position);
                if(pojo.getMobile().equals(myMobile)){
                    return 1;

                }else {
                    return 2;
                }
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                recyclerView.scrollToPosition((getItemCount() -1));
            }
        };


        recyclerAdapter.startListening();
        recyclerView.setAdapter(recyclerAdapter);

    }


    class  MyViewHolder extends RecyclerView.ViewHolder{

        TextView name,message;

        public MyViewHolder(@NonNull View itemView) {


            super(itemView);
            name=findViewById(R.id.name);
            message=findViewById(R.id.message);

        }
    }
}


