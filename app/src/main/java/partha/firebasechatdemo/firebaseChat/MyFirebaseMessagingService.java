/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package partha.firebasechatdemo.firebaseChat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import partha.firebasechatdemo.R;
import partha.firebasechatdemo.RecentMessageActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFMService";

    String from_user_id;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Bundle[{google.sent_time=1481697172070, push_name=message, id=69, from=716045659933, google.message_id=0:1481697172087153%6980c460f9fd7ecd,
        // message=You have a new message from test demo uzi, senderid=716045659933, collapse_key=do_not_collapse}]
        String msg="";
        String push_name="";

        /*Bundle[{google.sent_time=1495118382627, notification_time=2017-05-18 14:39:36, from_user_id=12,
        from=833710522226, type=messaging, badge=1, google.message_id=0:1495118382640353%a442bccff9fd7ecd,
        message=Partha Chatterjee sent you a message, senderid=833710522226, form_type=custom}]*/

        if (remoteMessage == null)
            return;
        if(remoteMessage.getData()!=null) {
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().toString());

            if (remoteMessage.getData().size() > 0) {
                Log.e(TAG, "FooterCountData Payload: " + remoteMessage.getData().toString());
                Log.e(TAG, "remoteMessage: " + remoteMessage.toString());

                try {
                    from_user_id=remoteMessage.getData().get("from_user_id");
                    push_name=remoteMessage.getData().get("type");
                    String message = remoteMessage.getData().get("message");
//                    String badge = remoteMessage.getData().get("badge");
//                    String senderid = remoteMessage.getData().get("senderid");

                    int push_id=0;
//                    push_id=Integer.parseInt(remoteMessage.getData().get("id"));

                    sendNotification(message,push_id,push_name);

                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.getMessage());
                    sendNotification("You have new notification",1,push_name);//just to ensure push is received
                }
            }
            else if(remoteMessage.getNotification().getBody()!=null){
                sendNotification(remoteMessage.getNotification().getBody(),0,push_name);
            }

            //Calling method to generate notification
        }
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody, int id, String type) {
        Intent intent = new Intent(this, RecentMessageActivity.class);
//        if(type.equalsIgnoreCase("meetup_request")) {
            intent.putExtra("type",type);
        //}
      /*  else if (type.equalsIgnoreCase("message")){
            intent = new Intent(this, ActivityMessageListing.class);
        }
        else if (type.equalsIgnoreCase("booking")){
            intent = new Intent(this, BookingListActivity.class);
        }
        else{
            intent = new Intent(this, AupairDashboardActitivty.class);
        }*/

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Nationality")
                .setContentText(messageBody)
//                .setColor(getResources().getColor(R.color.color_pink))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, notificationBuilder.build());

        Intent intent2 = new Intent("fcm_push");

        //put whatever data you want to send, if any
        intent2.putExtra("type", type);
        intent2.putExtra("from_user_id", from_user_id);

        //send broadcast
        sendBroadcast(intent2);
    }
}
