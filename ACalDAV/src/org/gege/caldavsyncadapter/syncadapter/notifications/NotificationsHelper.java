package org.gege.caldavsyncadapter.syncadapter.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import de.jdevel.acaldav.R;

//import android.app.PendingIntent;
//import android.content.Intent;
//import android.support.v4.app.TaskStackBuilder;

public class NotificationsHelper {

	/*static SyncLog currentSyncLog = new SyncLog();
         doesn't exist */

    public static void signalSyncErrors(Context context, String title, String text) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setContentText(text);

        // Creates an explicit intent for an Activity in your app
/*		Intent resultIntent = new Intent(context, SyncStatusReportActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(SyncStatusReportActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
*/

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        int mId = 0;
        mNotificationManager.notify(mId, mBuilder.build());
    }

	/*public static SyncLog getCurrentSyncLog() {
                return currentSyncLog;
	}*/

}
