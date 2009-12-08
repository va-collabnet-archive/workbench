/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Feb 14, 2005
 */
package org.dwfa.bpa.util;

import java.io.IOException;

/**
 * @author kec
 * 
 */
public class TimeUtil {

    /**
     * @param buff
     * @param timeRemainingInMs
     */
    public static void appendTimeString(StringBuffer buff, float timeInMs) {
        float timeInSecs = timeInMs / 1000F;
        float timeInMins = timeInMs / (60 * 1000F);
        float timeInHours = timeInMs / (60 * 60 * 1000F);
        float timeInDays = timeInMs / (24 * 60 * 60 * 1000F);
        if (timeInDays > 1) {
            int days = new Float(timeInDays).intValue();
            buff.append(days);
            buff.append(" days ");
            int hours = (new Float((timeInDays - days) * 24)).intValue();
            buff.append(hours);
            buff.append(" hours");
        } else if (timeInHours > 1) {
            int hours = new Float(timeInHours).intValue();
            buff.append(hours);
            buff.append(" hours ");
            int min = (new Float((timeInHours - hours) * 60)).intValue();
            buff.append(min);
            buff.append(" min");

        } else if (timeInMins > 1) {
            int min = new Float(timeInMins).intValue();
            buff.append(min);
            buff.append(" min ");
            int sec = (new Float((timeInMins - min) * 60)).intValue();
            buff.append(sec);
            buff.append(" sec");

        } else {
            int sec = new Float(timeInSecs).intValue();
            buff.append(sec);
            buff.append(" sec");

        }
    }

    /**
     * @return
     * @throws IOException
     */
    public static String getTimeCompleteOrRemaining(MonitorableProcess process, long elapsedTime) throws IOException {
        StringBuffer buff = new StringBuffer();

        if (process.isDone()) {
            buff.append("Complete (total time: ");
            TimeUtil.appendTimeString(buff, elapsedTime);
            buff.append(")");
        } else {
            buff.append("Completed: ");
            float completed = process.getCurrent();
            float totalSize = process.getLengthOfTask();
            float percentComplete = completed / totalSize;
            int percentCompleteInt = new Float(percentComplete * 100).intValue();
            buff.append(percentCompleteInt);
            buff.append("%");
            if (completed > 0) {
                float timeRemainingInMs = elapsedTime / percentComplete - elapsedTime;
                buff.append(" (remaining: ");
                TimeUtil.appendTimeString(buff, timeRemainingInMs);
                buff.append(")");
            }
        }

        return buff.toString();
    }
}
