package mil.nga.giat.mage.sdk.service;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.AttachmentHelper;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import mil.nga.giat.mage.sdk.http.post.MageServerPostRequests;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class AttachmentIntentService extends IntentService {
	
	private static final String LOG_NAME = AttachmentIntentService.class.getName();
	
	public static final String ATTACHMENT_PUSHED = "mil.nga.giat.mage.sdk.service.ATTACHMENT_PUSHED";
	
	public static final String ATTACHMENT_ID = "attachmentId";

	public AttachmentIntentService() {
		super("AttachmentIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!ConnectivityUtility.isOnline(getApplicationContext())) {
			Log.d(LOG_NAME, "Not connected.  Will try later.");
			return;
		}
		Long attachmentId = intent.getLongExtra(ATTACHMENT_ID, -1);
		Log.i(LOG_NAME, "Handling attachment: " + attachmentId);
		try {
			Attachment attachment = ObservationHelper.getInstance(getApplicationContext()).readAttachmentByPrimaryKey(attachmentId);
			if (attachment.getUrl() != null) {
				Log.i(LOG_NAME, "Already pushed attachment " + attachmentId + ", skipping.");
				return;
			}
			Log.d(LOG_NAME, "Staging attachment with id: " + attachment.getId());
			AttachmentHelper.stageForUpload(attachment, getApplicationContext());
			Log.d(LOG_NAME, "Pushing attachment with id: " + attachment.getId());
			attachment = MageServerPostRequests.postAttachment(attachment, getApplicationContext());
			Log.d(LOG_NAME, "Pushed attachment with remote_id: " + attachment.getRemoteId());
			
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ATTACHMENT_PUSHED);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(ATTACHMENT_ID, attachmentId);
			sendBroadcast(broadcastIntent);
			
		} catch (ObservationException oe) {
			Log.e(LOG_NAME, "Error obtaining attachment: " + attachmentId, oe);
		} catch (Exception e) {
			Log.e(LOG_NAME, "Error pushing atachment: " + attachmentId, e);
		}
	}
}
