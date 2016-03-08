/*
 *     Copyright (C) 2016 Mobile Interactive Systems Research Group
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.fhhagenberg.mint.automate.android.accessibility.service.event;

import at.fhhagenberg.mint.automate.loggingclient.javacore.event.SimpleEvent;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Event when a screen on Android was visited based on accessibility events.
 */
public class AppScreenVisitEvent extends SimpleEvent {
	public static final Id ID = new Id(AppScreenVisitEvent.class);

	private String mPackageName;
	private String mClassName;
	private String mTitle;
	private long mEventTime;
	private int mOrientation;

	public AppScreenVisitEvent(String packageName, String className, String title, long eventTime, int orienation) {
		super(ID);

		mPackageName = packageName;
		mClassName = className;
		mTitle = title;
		mEventTime = eventTime;
		mOrientation = orienation;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public String getClassName() {
		return mClassName;
	}

	public String getTitle() {
		return mTitle;
	}

	public long getEventTime() {
		return mEventTime;
	}

	public int getOrientation() {
		return mOrientation;
	}
}
