package org.asteroidos.sync.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/*
 * Created by David Laundav and contributed by Christian Orthmann
 *
 * Copyright 2013 Daivd Laundav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * References:
 * http://stackoverflow.com/questions/5883938/getting-events-from-calendar
 *
 * Please do not delete the references as they gave inspiration for the implementation
 */


public class CalendarHelper {

    public static Calendar readCalendar(Context context) {
        return readCalendar(context, 1, 0);
    }

    // Use to specify specific the time span
    public static Calendar readCalendar(Context context, int days, int hours) {

        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
         new String[]{ "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" },
         null, null, null)) {

            // Create a set containing all of the calendar IDs available on the phone
            HashSet<String> calendarIds = getCalenderIds(cursor);

            Calendar calendar = new Calendar();

            // Loop over all of the calendars
            for (String id : calendarIds) {

                // Create a builder to define the time span
                Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
                long now = new Date().getTime();

                // create the time span based on the inputs
                ContentUris.appendId(builder, now - (DateUtils.DAY_IN_MILLIS * days) - (DateUtils.HOUR_IN_MILLIS * hours));
                ContentUris.appendId(builder, now + (DateUtils.DAY_IN_MILLIS * days) + (DateUtils.HOUR_IN_MILLIS * hours));

                // Create an event cursor to find all events in the calendar
                Cursor eventCursor = contentResolver.query(builder.build(),
                        new String[]{"title", "begin", "end", "allDay"}, "Events.calendar_id=" + id,
                        null, "startDay ASC, startMinute ASC");

                System.out.println("eventCursor count=" + eventCursor.getCount());

                // If there are actual events in the current calendar, the count will exceed zero
                if (eventCursor.getCount() > 0) {

                    // Move to the first object
                    eventCursor.moveToFirst();

                    // Create an object of CalendarEvent which contains the title, when the event begins and ends,
                    // and if it is a full day event or nota
                    VEvent event = loadEvent(eventCursor);

                    // Adds the first object to the list of events
                    // TODO seperate the events per source calendar, somehow
                    calendar.getComponents().add(event);

                    // While there are more events in the current calendar, move to the next instance
                    while (eventCursor.moveToNext()) {

                        // Adds the object to the list of events
                        event = loadEvent(eventCursor);
                        calendar.getComponents().add(event);

                    }

                    //eventMap.put(id, eventList);

                }
            }

            return calendar;
        }
    }

    // Returns a new instance of the calendar object
    private static VEvent loadEvent(Cursor csr) {
        // "title", "begin", "end", "allDay"
        String title = csr.getString(0);
        Date begin = new Date(csr.getLong(1));
        Date end = new Date(csr.getLong(2));

        // TODO use allDay
        boolean allDay = !csr.getString(3).equals("0");

        return new VEvent(
                new net.fortuna.ical4j.model.Date(begin),
                new net.fortuna.ical4j.model.Date(end),
                title);
    }

    // Creates the list of calendar ids and returns it in a set
    private static HashSet<String> getCalenderIds(Cursor cursor) {

        HashSet<String> calendarIds = new HashSet<String>();

        try
        {

            // If there are more than 0 calendars, continue
            if(cursor.getCount() > 0)
            {

                // Loop to set the id for all of the calendars
                while (cursor.moveToNext()) {

                    String _id = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    Boolean selected = !cursor.getString(2).equals("0");

                    Log.d("CalendarHelper", "Id: " + _id + " Display Name: " + displayName + " Selected: " + selected);
                    calendarIds.add(_id);

                }
            }
        }

        catch(AssertionError ex)
        {
            ex.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return calendarIds;

    }
}