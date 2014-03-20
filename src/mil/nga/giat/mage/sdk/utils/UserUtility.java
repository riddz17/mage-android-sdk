package mil.nga.giat.mage.sdk.utils;

import java.util.Date;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class UserUtility {

	private UserUtility() {
	}

	private static UserUtility userUtility;
	private static Context mContext;

	public static UserUtility getInstance(final Context context) {
		if (context == null) {
			return null;
		}
		if (userUtility == null) {
			userUtility = new UserUtility();
		}
		mContext = context;
		return userUtility;
	}

	public synchronized final Boolean isTokenExpired() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPreferences.getString("token", "").trim().isEmpty()) {
			return true;
		}
		String tokenExpirationDateString = sharedPreferences.getString("tokenExpirationDate", "");
		if (!tokenExpirationDateString.isEmpty()) {
			Span s = Chronic.parse(tokenExpirationDateString);
			if (s != null) {
				return new Date().after(new Date(s.getBegin() * 1000));
			}
		}
		return true;
	}

	public synchronized final void clearTokenInformation() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = sharedPreferences.edit();
		editor.putString("token", "").commit();
		editor.putString("tokenExpirationDate", "").commit();
	}
}