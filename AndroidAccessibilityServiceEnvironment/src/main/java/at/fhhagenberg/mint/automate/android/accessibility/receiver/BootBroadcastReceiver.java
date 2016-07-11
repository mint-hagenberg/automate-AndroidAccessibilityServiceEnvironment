/*
 *     Copyright (C) 2016 Research Group Mobile Interactive Systems
 *     Email: mint@fh-hagenberg.at, Website: http://mint.fh-hagenberg.at
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.fhhagenberg.mint.automate.android.accessibility.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import at.fhhagenberg.mint.automate.android.accessibility.service.AutomateAccessibilityService;

/**
 * Boot broadcast receiver that checks the accessibility service permissions (only possible when APK is system app).
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = BootBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			setCorrectPermissions(context);
			startService(context);
		}
	}

	/**
	 * Set the correct permissions for the accessibility service.
	 *
	 * @param context -
	 */
	public static void setCorrectPermissions(Context context) {
		if (!AutomateAccessibilityService.setAsAccessibilityServiceChecked(context)) {
			Log.d(TAG, "Could not set accessibility service");
		}
	}

	/**
	 * Start the accessibility service manually.
	 *
	 * @param context -
	 */
	public static void startService(Context context) {
		context.startService(new Intent(context, AutomateAccessibilityService.class));
	}

	/**
	 * Stop the accessibility service manually.
	 *
	 * @param context -
	 */
	public static void stopService(Context context) {
		context.stopService(new Intent(context, AutomateAccessibilityService.class));
	}
}
