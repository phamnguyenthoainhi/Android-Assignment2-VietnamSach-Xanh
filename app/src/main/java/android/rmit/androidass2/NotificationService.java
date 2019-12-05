package android.rmit.androidass2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.rmit.androidass2.R;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationService extends FirebaseMessagingService {
    FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().get("type").equals("invitation")){
            final Intent intent = new Intent(this, SiteDetail.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("id", remoteMessage.getData().get("id"));

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(this)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags = Notification.FLAG_AUTO_CANCEL;

            manager.notify(123, notification);
        }
        else {
            final Intent intent = new Intent(this, ManageSiteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("selectedsiteid", remoteMessage.getData().get("id"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


            Notification notification = new Notification.Builder(this)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags = Notification.FLAG_AUTO_CANCEL;

            manager.notify(123, notification);
        }

    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        System.out.println("NEW TOKEN: "+s);
        SharedPreferences sharedPreferences = getSharedPreferences("id",MODE_PRIVATE);
        String userId = sharedPreferences.getString("uid",null);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (currentuser != null) {
            db.collection("Tokens").document(currentuser.getUid()).set(new UserToken(s))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Token","Successfully updated token.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Token", "Failed to update token");
                        }
                    });
        }

    }
}