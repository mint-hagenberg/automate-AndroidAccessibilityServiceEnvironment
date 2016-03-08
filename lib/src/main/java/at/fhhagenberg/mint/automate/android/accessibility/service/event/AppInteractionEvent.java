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

import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;

import at.fhhagenberg.mint.automate.loggingclient.javacore.event.SimpleEvent;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Event for Android app interactions based on accessibility events.
 */
public class AppInteractionEvent extends SimpleEvent {
	public static final Id ID = new Id(AppInteractionEvent.class);

	public enum InteractionType {
		CLICK, CONTEXT_CLICK, LONG_CLICK, SELECTED, SCROLL
	}

	private InteractionType mInteractionType;
	private String mClassName;
	private String mText;
	private String mContentDescription;
	private String mViewIdResourceName;
	private long mEventTime;
	private Rect mScreenBounds;
	private Rect mParentBounds;

	public AppInteractionEvent(InteractionType interactionType, String className, String text, String contentDescription, String viewIdResourceName, long eventTime, Rect screenBounds, Rect parentBounds) {
		super(ID);
		mInteractionType = interactionType;
		mClassName = className;
		mText = text;
		mContentDescription = contentDescription;
		mViewIdResourceName = viewIdResourceName;
		mEventTime = eventTime;
		mScreenBounds = screenBounds;
		mParentBounds = parentBounds;
	}

	public InteractionType getInteractionType() {
		return mInteractionType;
	}

	public String getClassName() {
		return mClassName;
	}

	public String getText() {
		return mText;
	}

	public String getContentDescription() {
		return mContentDescription;
	}

	public String getViewIdResourceName() {
		return mViewIdResourceName;
	}

	public long getEventTime() {
		return mEventTime;
	}

	public Rect getScreenBounds() {
		return mScreenBounds;
	}

	public Rect getParentBounds() {
		return mParentBounds;
	}

	/**
	 * Turn an accessibility event interaction type into the internal types.
	 *
	 * @param eventType -
	 * @return -
	 */
	public static InteractionType accessibilityInteractionTypeToInternal(int eventType) {
		switch (eventType) {
			case AccessibilityEvent.TYPE_VIEW_CLICKED:
				return InteractionType.CLICK;

			case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
				return InteractionType.CONTEXT_CLICK;

			case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
				return InteractionType.LONG_CLICK;

			case AccessibilityEvent.TYPE_VIEW_SELECTED:
				return InteractionType.SELECTED;

			case AccessibilityEvent.TYPE_VIEW_SCROLLED:
				return InteractionType.SCROLL;

			default:
				return null;
		}
	}
}
