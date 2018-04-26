package com.firebasechatdemotutorial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebasechatdemotutorial.model.Chat;
import com.firebasechatdemotutorial.util.Constants;
import com.firebasechatdemotutorial.util.TakeImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.firebasechatdemotutorial.util.TakeImage.path;

/**
 * Created by mobua01 on 25/4/18.
 */

public class ChatFragment extends Fragment implements TextView.OnEditorActionListener, View.OnClickListener {

    private static final int GALLERY_IMAGE_RESULT = 22;
    private static final int AUDIO_RESULT = 23;
    private static final int VIDEO_RESULT = 24;
    private ProgressDialog mProgressDialog;
    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;
    private ChatRecyclerAdapter mChatRecyclerAdapter;
    private ImageButton mAddImageButton;
    private File filePath;
    private FirebaseStorage firebaseStorage;
    private StorageReference sRef;
    private String image;
    private ImageButton mAddAudioButton;
    private Uri uri;
    private String audio;
    private ImageButton mAddVideoButton;
    private Uri videoUri;
    private String video;
    private ImageButton mAddLocationButton;
    private String type="";

    public static ChatFragment newInstance(String receiver,
                                           String receiverUid,
                                           String firebaseToken) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

   /* @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        bindViews(fragmentView);
        return fragmentView;
    }

    private void bindViews(View view) {
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) view.findViewById(R.id.edit_text_message);

        mAddImageButton = (ImageButton) view.findViewById(R.id.addImagebutton);
        mAddAudioButton = (ImageButton) view.findViewById(R.id.addAudioButton);
        mAddVideoButton = (ImageButton) view.findViewById(R.id.addVideoButton);
        mAddLocationButton = (ImageButton) view.findViewById(R.id.addLocationButton);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firebaseStorage = FirebaseStorage.getInstance();
        initListener();
        init();
    }

    private void initListener() {

        mAddImageButton.setOnClickListener(this);
        mAddAudioButton.setOnClickListener(this);
        mAddVideoButton.setOnClickListener(this);
        mAddLocationButton.setOnClickListener(this);
    }

    private void init() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mETxtMessage.setOnEditorActionListener(this);

        getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                getArguments().getString(Constants.ARG_RECEIVER_UID));
    }

    private void getMessage(String senderUid, String receiverUid) {

        final String room_type_1 = senderUid + "_" + receiverUid;
        final String room_type_2 = receiverUid + "_" + senderUid;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child(Constants.ARG_CHAT_ROOMS).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(room_type_1)) {

                    Log.e("TAG", "getMessageFromFirebaseUser: " + room_type_1 + " exists");

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(Constants.ARG_CHAT_ROOMS)
                            .child(room_type_1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            onGetMessagesSuccess(chat);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            onGetMessagesFailure("Unable to get message: " + databaseError.getMessage());
                        }
                    });
                } else if (dataSnapshot.hasChild(room_type_2)) {
                    Log.e("TAG", "getMessageFromFirebaseUser: " + room_type_2 + " exists");
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(Constants.ARG_CHAT_ROOMS)
                            .child(room_type_2).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            onGetMessagesSuccess(chat);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            onGetMessagesFailure("Unable to get message: " + databaseError.getMessage());
                        }
                    });
                } else {
                    Log.e("TAG", "getMessageFromFirebaseUser: no such room available");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Unable to get message: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onGetMessagesFailure(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();

    }

    private void onGetMessagesSuccess(Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(getActivity(),new ArrayList<Chat>(),videoUri,image);
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage(type);
            mETxtMessage.setText("");
            return true;
        }
        return false;
    }

    private void sendMessage(String type) {
        String message = mETxtMessage.getText().toString();
        String receiver = getArguments().getString(Constants.ARG_RECEIVER);
        String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
        String sender = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String receiverFirebaseToken = getArguments().getString(Constants.ARG_FIREBASE_TOKEN);

        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis(),"28.5355","77.3910",type);

        sendMessageToUser(getActivity(),
                chat,
                receiverFirebaseToken);
    }

    private void sendMessageToUser(Context applicationContext, final Chat chat, final String receiverFirebaseToken) {

        final String room_type_1 = chat.senderUid + "_" + chat.receiverUid;

        final String room_type_2 = chat.receiverUid + "_" + chat.senderUid;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child(Constants.ARG_CHAT_ROOMS).getRef().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(room_type_1)) {
                    Log.e("TAG", "sendMessageToFirebaseUser: " + room_type_1 + " exists");
                    databaseReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).child(String.valueOf(chat.timestamp)).setValue(chat);

                } else if (dataSnapshot.hasChild(room_type_2)) {
                    Log.e("TAG", "sendMessageToFirebaseUser: " + room_type_2 + " exists");
                    databaseReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_2).child(String.valueOf(chat.timestamp)).setValue(chat);

                } else {

                    Log.e("TAG", "sendMessageToFirebaseUser: success");
                    databaseReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).child(String.valueOf(chat.timestamp)).setValue(chat);

                    getMessage(chat.senderUid, chat.receiverUid);

                }
                // send push notification to the receiver
                /*sendPushNotificationToReceiver(chat.sender,
                        chat.message,
                        chat.senderUid,
                        new SharedPrefUtil(getActivity()).getString(Constants.ARG_FIREBASE_TOKEN),
                        receiverFirebaseToken);
                mOnSendMessageListener.onSendMessageSuccess();*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Unable to send message" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_RESULT) {
                path = data.getStringExtra("filePath");
                if (path != null & !path.isEmpty()) {
                    Log.i("Gallery File--->", path);

                    filePath = new File(data.getStringExtra("filePath"));

                    if (filePath.exists()) {

                        Log.d("image", filePath + "");

                        uploadFileToFirebase(filePath);
                    }
                } else {
                    path = "";
                    Toast toast = Toast.makeText(getActivity(), "Image Not exists!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

            if (requestCode == AUDIO_RESULT) {

                //TODO AUDIO WORK
                if (data.getData() != null) {

                    uri = data.getData();

                    if (uri != null) {

                        uploadAudioToFirebase();

                    } else {

                        Toast.makeText(getActivity(), "Audio file not exists", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(getActivity(), "Audio file not found", Toast.LENGTH_SHORT).show();

                }

            }

            if (requestCode == VIDEO_RESULT) {

                videoUri = data.getData();
                uploadVideoToFirebase();
            }

        }
    }

    private void uploadVideoToFirebase() {


        //getting the storage reference
        StorageReference storageReference = firebaseStorage.getReference();
        sRef = storageReference.child("Upload Video").child(Constants.STORAGE_PATH_VIDEO + System.currentTimeMillis());

        //adding the file to reference
        sRef.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(getActivity(), "Video File Uploaded ", Toast.LENGTH_LONG).show();

                        //creating the upload object to store uploaded image details
                        Upload upload = new Upload("Video uploading", taskSnapshot.getDownloadUrl().toString());

                        getVideoDownloadFile();

                       /* //adding an upload to firebase database
                        String uploadId = mDatabase.push().getKey();
                        mDatabase.child(uploadId).setValue(upload);*/
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //displaying the upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });

    }

    private void getVideoDownloadFile() {

        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Toast.makeText(getActivity(), "Downloaded Video Uri" + uri, Toast.LENGTH_SHORT).show();
                video = uri.toString();
                Log.d("video format", uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getActivity(), "Failed to Download Video Uri" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                // Handle any errors
            }
        });

    }

    private void uploadAudioToFirebase() {

        //getting the storage reference
        StorageReference storageReference = firebaseStorage.getReference();
        sRef = storageReference.child("Upload Audio").child(Constants.STORAGE_PATH_AUDIO + System.currentTimeMillis());

        //adding the file to reference
        sRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(getActivity(), "Audio File Uploaded ", Toast.LENGTH_LONG).show();

                        //creating the upload object to store uploaded image details
                        Upload upload = new Upload("Audio uploading", taskSnapshot.getDownloadUrl().toString());

                        getAudioDownloadFile();

                       /* //adding an upload to firebase database
                        String uploadId = mDatabase.push().getKey();
                        mDatabase.child(uploadId).setValue(upload);*/
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //displaying the upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });
    }

    private void getAudioDownloadFile() {

        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Toast.makeText(getActivity(), "Downloaded Audio Uri" + uri, Toast.LENGTH_SHORT).show();
                audio = uri.toString();
                Log.d("audio format", uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getActivity(), "Failed to Download Audio Uri" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                // Handle any errors
            }
        });
    }

    private void uploadFileToFirebase(File filePath) {

        //getting the storage reference
        StorageReference storageReference = firebaseStorage.getReference();
        sRef = storageReference.child("Upload Images").child(Constants.STORAGE_PATH_IMAGE + System.currentTimeMillis());

        //adding the file to reference
        sRef.putFile(Uri.fromFile(new File(filePath.toString())))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(getActivity(), "File Uploaded ", Toast.LENGTH_LONG).show();

                        //creating the upload object to store uploaded image details
                        Upload upload = new Upload("Image uploading", taskSnapshot.getDownloadUrl().toString());

                        getDownloadFile();

                       /* //adding an upload to firebase database
                        String uploadId = mDatabase.push().getKey();
                        mDatabase.child(uploadId).setValue(upload);*/
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //displaying the upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });
    }


    private void getDownloadFile() {

        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Toast.makeText(getActivity(), "Downloaded Uri" + uri, Toast.LENGTH_SHORT).show();
                image = uri.toString();
                Log.d("image jpg", uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getActivity(), "Failed to Download Uri" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                // Handle any errors
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.addImagebutton:
                type = "image";
                Intent intent = new Intent(getActivity(), TakeImage.class);
                intent.putExtra("from", "gallery");
                startActivityForResult(intent, GALLERY_IMAGE_RESULT);
                break;

            case R.id.addAudioButton:
                type = "audio";
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, AUDIO_RESULT);
                break;

            case R.id.addVideoButton:
                type = "video";
                Intent intent_video_upload = new Intent();
                intent_video_upload.setType("video/*");
                intent_video_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent_video_upload, "Select Video"), VIDEO_RESULT);
                break;

            case R.id.addLocationButton:
                type = "location";
               //TODO LOCATION
                break;
        }
    }
}
