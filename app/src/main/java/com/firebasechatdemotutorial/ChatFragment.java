package com.firebasechatdemotutorial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private String type = "";
    private String typeChoose;
    private String downloadedFile;

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
            mChatRecyclerAdapter = new ChatRecyclerAdapter(getActivity(), new ArrayList<Chat>());
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
                System.currentTimeMillis(), "28.5355", "77.3910");

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

                        uploadFileToFirebase(filePath, typeChoose, null);
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

                    Uri uri = data.getData();

                    if (uri != null) {

                        uploadFileToFirebase(null, typeChoose, uri);

                    } else {

                        Toast.makeText(getActivity(), "Audio file not exists", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(getActivity(), "Audio file not found", Toast.LENGTH_SHORT).show();

                }

            }

            if (requestCode == VIDEO_RESULT) {

                Uri videoUri = data.getData();
                uploadFileToFirebase(null, typeChoose, videoUri);
            }

        }
    }

    private void uploadFileToFirebase(File filePath, String typeFile, Uri uri) {
        if (typeFile.equalsIgnoreCase("image_file")) {
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
        } else if (typeFile.equalsIgnoreCase("audio_file")) {//getting the storage reference
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

//                            getAudioDownloadFile();
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

        } else if (typeFile.equalsIgnoreCase("video_file")) {//getting the storage reference
            //getting the storage reference
            StorageReference storageReference = firebaseStorage.getReference();
            sRef = storageReference.child("Upload Video").child(Constants.STORAGE_PATH_VIDEO + System.currentTimeMillis());

            //adding the file to reference
            sRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(getActivity(), "Video File Uploaded ", Toast.LENGTH_LONG).show();

                            //creating the upload object to store uploaded image details
                            Upload upload = new Upload("Video uploading", taskSnapshot.getDownloadUrl().toString());

//                            getVideoDownloadFile();
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
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
    }


    private void getDownloadFile() {

        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Toast.makeText(getActivity(), "Downloaded \n" + uri, Toast.LENGTH_SHORT).show();
                downloadedFile = uri.toString();
                Log.d("uri download", uri.toString());
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

                openAlertDialog();

                break;

        }
    }

    private void openAlertDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final RadioGroup chooseRg = (RadioGroup) dialogView.findViewById(R.id.choose_rg);
        final RadioButton chooseImage = (RadioButton) dialogView.findViewById(R.id.select_image);
        final RadioButton chooseAudio = (RadioButton) dialogView.findViewById(R.id.select_audio);
        final RadioButton chooseVideo = (RadioButton) dialogView.findViewById(R.id.select_video);

        chooseRg.check(chooseRg.getChildAt(0).getId());

        dialogBuilder.setTitle("Custom dialog");
        dialogBuilder.setMessage("Enter text below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                if (chooseImage.isChecked()) {

                    typeChoose = "image_file";
                    Intent intent = new Intent(getActivity(), TakeImage.class);
                    intent.putExtra("from", "gallery");
                    startActivityForResult(intent, GALLERY_IMAGE_RESULT);
                    dialog.dismiss();

                } else if (chooseAudio.isChecked()) {

                    typeChoose = "audio_file";
                    Intent intent_upload = new Intent();
                    intent_upload.setType("audio/*");
                    intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent_upload, AUDIO_RESULT);
                    dialog.dismiss();

                } else if (chooseVideo.isChecked()) {

                    typeChoose = "video_file";
                    Intent intent_video_upload = new Intent();
                    intent_video_upload.setType("video/*");
                    intent_video_upload.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent_video_upload, "Select Video"), VIDEO_RESULT);
                    dialog.dismiss();

                } else {

                    Toast.makeText(getActivity(), "Please select anyone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                dialog.dismiss();
            }
        });

        AlertDialog b = dialogBuilder.create();

        b.show();
    }
}
