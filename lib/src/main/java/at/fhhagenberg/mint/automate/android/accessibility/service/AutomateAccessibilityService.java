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

package at.fhhagenberg.mint.automate.android.accessibility.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.Date;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.KernelManagerHelper;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;
import at.fhhagenberg.mint.automate.android.basemanager.appinteraction.event.AppInteractionEvent;
import at.fhhagenberg.mint.automate.android.basemanager.appinteraction.event.AppScreenVisitEvent;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.DebugLogAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.EventAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.Event;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.ManagerException;

/**
 * An Android accessibility service that will start and stop the Logging Kernel as well as send events from the accessibility events.
 */
public class AutomateAccessibilityService extends AccessibilityService {
	private static final String TAG = AutomateAccessibilityService.class.getSimpleName();

	/**
	 * Internal/local broadcast action when the kernel was started.
	 */
	public static final String ACTION_BACKGROUND_SERVICE_STARTED = "at.fhhagenberg.mint.automate.android.accessibility.STARTED";
	/**
	 * Internal/local broadcast action when the kernel was stopped.
	 */
	public static final String ACTION_BACKGROUND_SERVICE_STOPPED = "at.fhhagenberg.mint.automate.android.accessibility.STOPPED";

	/**
	 * Internal/broadcast to disable or enable the kernel.
	 */
	public static final String ACTION_SET_KERNEL_DISABLED_STATE = "at.fhhagenberg.mint.automate.android.accessibility.SET_KERNEL_DISABLED_STATE";

	/**
	 * The boolean extra to set the disabled or enabled state of the kernel.
	 */
	public static final String EXTRA_KERNEL_DISABLED_VALUE = "disabled";

	private static final long COMPLEX_SCROLL_TIMEOUT = 250;

	private static boolean sIsRunning = false;

	private static boolean sIsKernelDisabled = false;

	/**
	 * Check if the service is running.
	 *
	 * @return -
	 */
	public static boolean isRunning() {
		return sIsRunning;
	}

	/**
	 * Check if the Kernel should not be running.
	 *
	 * @return -
	 */
	public static boolean isKernelDisabled() {
		return sIsKernelDisabled;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) || intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
				shutdownKernel();
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				startKernel();
			} else if (intent.getAction().equals(ACTION_SET_KERNEL_DISABLED_STATE)) {
				sIsKernelDisabled = intent.getBooleanExtra(EXTRA_KERNEL_DISABLED_VALUE, true);
				if (sIsKernelDisabled) {
					shutdownKernel();
				} else {
					startKernel();
				}
			}
		}
	};

	private String[] mIncludeOnlyPackageNames;

	private int mCurrentOrienation;
	private long mLastComplexEvent = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		startKernel();
		sIsRunning = true;
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SHUTDOWN);
		filter.addAction(ACTION_SET_KERNEL_DISABLED_STATE);
		registerReceiver(mReceiver, filter);

		try {
			mIncludeOnlyPackageNames = PropertiesHelper.getProperty(((AndroidKernel) KernelBase.getKernel()).getContext(), "accessibility.includeOnly.packageNames", String[].class);
		} catch (Exception e) {
			// TODO: print error
			e.printStackTrace();
		}

		mCurrentOrienation = getResources().getConfiguration().orientation;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);

		shutdownKernel();
		sIsRunning = false;

		super.onDestroy();
	}

	private void initKernel() {
		if (!KernelBase.isInitialized()) {
			try {
				KernelBase.initialize(KernelManagerHelper.initializeKernel(this));
			} catch (ClassCastException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void startKernel() {
		if (sIsKernelDisabled) {
			shutdownKernel();
			return;
		}

		initKernel();
		if (!KernelBase.isKernelUpRunning()) {
			try {
				KernelBase.getKernel().startup();
				LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_BACKGROUND_SERVICE_STARTED));
			} catch (ManagerException e) {
				e.printStackTrace();
			}
		}
	}

	private void shutdownKernel() {
		if (KernelBase.isInitialized() && KernelBase.isKernelUpRunning()) {
			KernelBase.getKernel().shutdown();
			LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_BACKGROUND_SERVICE_STOPPED));
		}
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (!KernelBase.getKernel().isRunning()) {
			return;
		}

		if (event.getPackageName() == null) {
			new DebugLogAction(KernelBase.getKernel(), DebugLogManager.Priority.DEBUG, TAG, "The package of the event is null:" + event).execute();
			return;
		}

		if (!isGlobalEvent(event.getEventType()) && !isPackageIncluded(event.getPackageName().toString())) {
			return;
		}

		switch (event.getEventType()) {
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
				onWindowStateChanged(event);
				break;
			}

			case AccessibilityEvent.TYPE_VIEW_CLICKED:
			case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
			case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
			case AccessibilityEvent.TYPE_VIEW_SELECTED: {
				onSimpleInteraction(event);
				break;
			}

			case AccessibilityEvent.TYPE_VIEW_SCROLLED: {
				onComplexInteraction(event);
				break;
			}

			default: {
				//Ignore
				break;
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mCurrentOrienation = newConfig.orientation;
	}

	private boolean isGlobalEvent(int eventType) {
		return eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
				|| eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
				|| eventType == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED
				|| eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED
				|| eventType == AccessibilityEvent.TYPE_VIEW_SELECTED;
	}

	private boolean isPackageIncluded(String packageName) {
		if (mIncludeOnlyPackageNames == null) {
			return true;
		}

		for (String pkg : mIncludeOnlyPackageNames) {
			if (pkg.endsWith("*") && packageName.startsWith(pkg.substring(0, pkg.length() - 1))) {
				return true;
			} else if (packageName.equals(pkg)) {
				return true;
			}
		}
		return false;
	}

	private void onWindowStateChanged(AccessibilityEvent event) {
		sendEvent(new AppScreenVisitEvent(event.getPackageName().toString(), event.getClassName().toString(), event.getText().toString(), new Date().getTime(), mCurrentOrienation));
	}

	private void onSimpleInteraction(AccessibilityEvent event) {
		sendInteractionEvent(event);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void sendInteractionEvent(AccessibilityEvent event) {
		AccessibilityNodeInfo source = event.getSource();
		if (source != null) {
			Rect screenBounds = new Rect();
			source.getBoundsInScreen(screenBounds);
			Rect parentBounds = new Rect();
			source.getBoundsInParent(parentBounds);
			sendEvent(new AppInteractionEvent(AppInteractionEvent.accessibilityInteractionTypeToInternal(event.getEventType()),
					source.getClassName().toString(),
					source.getText() == null ? null : source.getText().toString(),
					source.getContentDescription() == null ? null : source.getContentDescription().toString(),
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? source.getViewIdResourceName() : null,
					new Date().getTime(),
					screenBounds, parentBounds));
		} else {
			sendEvent(new AppInteractionEvent(AppInteractionEvent.accessibilityInteractionTypeToInternal(event.getEventType()),
					null, null, null, null, new Date().getTime(), null, null));
		}
	}

	private void onComplexInteraction(AccessibilityEvent event) {
		if ((event.getEventTime() - mLastComplexEvent) < COMPLEX_SCROLL_TIMEOUT) {
			mLastComplexEvent = event.getEventTime();
			return;
		}

		sendInteractionEvent(event);
		mLastComplexEvent = event.getEventTime();
	}

	private void sendEvent(Event event) {
		new EventAction(KernelBase.getKernel(), event).execute();
	}

	@Override
	public void onInterrupt() {
	}

	/**
	 * Set this service as an accessibility service if possible (when APK is installed as system app).
	 *
	 * @param context -
	 * @throws Exception
	 */
	public static void setAsAccessibilityService(Context context) throws Exception {
		ContentResolver contentResolver = context.getContentResolver();
		String services = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		String serviceName = context.getPackageName() + "/" + AutomateAccessibilityService.class.getName();
		if (services == null || !services.contains(serviceName)) {
			if (services != null && !services.isEmpty()) {
				services += ":";
			} else {
				services = "";
			}
			Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, services + serviceName);
		}
		Settings.Secure.putInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 1);
	}

	/**
	 * Set this service as an accessibility service if possible (when APK is installed as system app).
	 *
	 * @param context -
	 * @return -
	 */
	public static boolean setAsAccessibilityServiceChecked(Context context) {
		try {
			setAsAccessibilityService(context);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
