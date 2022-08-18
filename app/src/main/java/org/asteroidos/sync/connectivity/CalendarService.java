/*
 * This file was originally written by jrtberlin in 2019 and updated in 2022 by Foxite.
 * The original copyright notice is below:
 *
 * Copyright (C) 2019 - Justus Tartz <git@jrtberlin.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.asteroidos.sync.connectivity;

import android.content.Context;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import org.asteroidos.sync.asteroid.IAsteroidDevice;
import org.asteroidos.sync.utils.AsteroidUUIDS;
import org.asteroidos.sync.utils.CalendarHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class CalendarService implements IConnectivityService {
    private final Context mCtx;
    private final IAsteroidDevice mDevice;

    public CalendarService(Context ctx, IAsteroidDevice device) {
        mCtx = ctx;
        mDevice = device;
    }

    @Override
    public void sync() {
        // TODO use alarm
        updateCalendar();
    }

    @Override
    public void unsync() {
        // when you add an alarm as above, dispose of it here
    }

    private void updateCalendar(){
        Calendar calendar = getCalendar();
        mDevice.send(AsteroidUUIDS.CALENDAR_WRT_UUID, serializeCalendar(calendar), CalendarService.this);
    }

    private byte[] serializeCalendar(Calendar calendar) {
        CalendarOutputter outputter = new CalendarOutputter();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            outputter.output(calendar, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            // TODO report error to user somehow or throw
            // We really should be throwing this error.
            e.printStackTrace();
            return null;
        }
    }

    private Calendar getCalendar(){
        return CalendarHelper.readCalendar(mCtx);
    }

    @Override
    public HashMap<UUID, Direction> getCharacteristicUUIDs() {
        HashMap<UUID, Direction> chars = new HashMap<>();
        chars.put(AsteroidUUIDS.CALENDAR_SERVICE_UUID, Direction.TO_WATCH);
        chars.put(AsteroidUUIDS.CALENDAR_WRT_UUID, Direction.TO_WATCH);
        return chars;
    }

    @Override
    public UUID getServiceUUID() {
        return AsteroidUUIDS.CALENDAR_SERVICE_UUID;
    }
}
