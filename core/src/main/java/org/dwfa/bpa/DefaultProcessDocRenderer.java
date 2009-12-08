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
package org.dwfa.bpa;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_RenderDocumentation;
import org.dwfa.bpa.process.PropertySpec;

public class DefaultProcessDocRenderer implements I_RenderDocumentation {

    public String getDocumentation(I_EncodeBusinessProcess process) throws Exception {
        StringBuffer docBuffer = new StringBuffer();
        docBuffer.append("<html><head>");

        docBuffer.append("<style type=\"text/css\">");
        docBuffer.append("h1 {color:blue; font-size:18pt; font-style:italic;}");
        docBuffer.append("span.prefix {color:#505050; font-style:normal; font-variant:small-caps; font-weight:bold;}");
        docBuffer.append("</style></head><body>&nbsp;<br>");
        docBuffer.append("<span class=prefix>Process Name: </span>");
        docBuffer.append(process.getName());
        docBuffer.append("<hr size=1 width='75%'><br><span class=prefix>Description: </span>");
        docBuffer.append(process.getProcessDocumentationSource());
        docBuffer.append("<hr size=1 width='75%'><br><span class=prefix>Subject: </span>");
        docBuffer.append(process.getSubject());
        docBuffer.append("<hr size=1 width='75%'><br><span class=prefix>Exported Task Properties: </span>");
        docBuffer.append(getExportedTaskPropertyDocumentation(process));
        docBuffer.append("<hr size=1 width='75%'><br><span class=prefix>Exported Attachments: </span>");
        docBuffer.append(getExportedAttachmentDocumentation(process));
        docBuffer.append("<hr size=1 width='75%'><br><span class=prefix>Internal Attachments: </span>");
        docBuffer.append(getInternalAttachmentDocumentation(process));
        docBuffer.append("</body>");
        return docBuffer.toString();
    }

    private String getInternalAttachmentDocumentation(I_EncodeBusinessProcess process) throws IntrospectionException {
        StringBuffer buff = new StringBuffer();
        boolean noInternalAttachments = true;
        for (PropertyDescriptor pdwt : process.getAllPropertiesBeanInfo().getPropertyDescriptors()) {
            PropertySpec spec = (PropertySpec) pdwt.getValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name());
            if (spec.getType().equals(PropertySpec.SourceType.ATTACHMENT)
                && (process.isPropertyExternal(spec) == false)) {
                buff.append("</p><hr align=left width=\"20%\">");
                noInternalAttachments = false;
                buff.append("<p>&nbsp;&nbsp;<span class=prefix>Attachment key: </span>");
                buff.append(spec.getKey());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>Description: </span>");
                buff.append(spec.getShortDescription());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>External name: </span>");
                buff.append(spec.getExternalName());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>External description: </span>");
                buff.append(spec.getExternalToolTip());
            }
        }
        if (noInternalAttachments) {
            buff.append("<br>none");
        }
        return buff.toString();
    }

    private String getExportedAttachmentDocumentation(I_EncodeBusinessProcess process) throws IntrospectionException {
        StringBuffer buff = new StringBuffer();
        boolean noExportedAttachments = true;
        for (PropertyDescriptor pdwt : process.getBeanInfo().getPropertyDescriptors()) {
            PropertySpec spec = (PropertySpec) pdwt.getValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name());
            if (spec.getType().equals(PropertySpec.SourceType.ATTACHMENT)) {
                buff.append("</p><hr align=left width=\"20%\">");
                noExportedAttachments = false;
                buff.append("<p>&nbsp;&nbsp;<span class=prefix>Attachment key: </span>");
                buff.append(spec.getKey());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>Description: </span>");
                buff.append(spec.getShortDescription());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>External name: </span>");
                buff.append(spec.getExternalName());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>External description: </span>");
                buff.append(spec.getExternalToolTip());
            }
        }
        if (noExportedAttachments) {
            buff.append("<br>none");
        }
        return buff.toString();
    }

    private String getExportedTaskPropertyDocumentation(I_EncodeBusinessProcess process) throws IntrospectionException {
        StringBuffer buff = new StringBuffer();
        boolean noExportedTasks = true;
        for (PropertyDescriptor pdwt : process.getBeanInfo().getPropertyDescriptors()) {
            PropertySpec spec = (PropertySpec) pdwt.getValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name());
            if (spec.getType().equals(PropertySpec.SourceType.TASK)) {
                buff.append("</p><hr align=left width=\"20%\">");
                noExportedTasks = false;
                buff.append("<p>&nbsp;&nbsp;<span class=prefix>Name: </span>");
                buff.append(spec.getKey());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>Description: </span>");
                buff.append(pdwt.getShortDescription());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>External name: </span>");
                buff.append(spec.getExternalName());
                buff.append("<br>&nbsp;&nbsp;<span class=prefix>External description: </span>");
                buff.append(spec.getExternalToolTip());
            }
        }
        if (noExportedTasks) {
            buff.append("<br>none");
        }
        return buff.toString();
    }
}
