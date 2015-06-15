package org.nuxeo.io.reader;

import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.platform.audit.api.AuditReader;
import org.nuxeo.ecm.platform.audit.api.ExtendedInfo;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.nuxeo.ecm.platform.audit.impl.ExtendedInfoImpl.BooleanInfo;
import org.nuxeo.ecm.platform.audit.impl.ExtendedInfoImpl.DateInfo;
import org.nuxeo.ecm.platform.audit.impl.ExtendedInfoImpl.DoubleInfo;
import org.nuxeo.ecm.platform.audit.impl.ExtendedInfoImpl.LongInfo;
import org.nuxeo.ecm.platform.audit.impl.ExtendedInfoImpl.StringInfo;
import org.nuxeo.runtime.api.Framework;

public class AuditInfoExportExtension implements ExportExtension {

    @Override
    public void updateExport(DocumentModel docModel, ExportedDocumentImpl result) throws Exception {

        Element auditElement = result.getDocument().getRootElement().addElement("auditInfo");

        AuditReader reader = Framework.getLocalService(AuditReader.class);

        List<LogEntry> entries = reader.getLogEntriesFor(docModel.getId());

        for (LogEntry entry : entries) {

            Element e = auditElement.addElement("entry");

            e.addAttribute("id", entry.getId()+"");
            e.addAttribute("category", entry.getCategory());
            if (entry.getComment()!=null) {
                e.addAttribute("comment", entry.getComment());
            }
            if (entry.getDocLifeCycle()!=null) {
                e.addAttribute("lifeCycle", entry.getDocLifeCycle());
            }
            if (entry.getDocPath()!=null) {
                e.addAttribute("docPath", entry.getDocPath());
            }
            if (entry.getDocType()!=null) {
                e.addAttribute("docType", entry.getDocType());
            }
            e.addAttribute("event", entry.getEventId());
            e.addAttribute("principal", entry.getPrincipalName());
            e.addAttribute("repository", entry.getRepositoryId());
            e.addAttribute("eventDate", entry.getEventDate().toGMTString());
            e.addAttribute("logDate", entry.getLogDate().toGMTString());

            Map<String , ExtendedInfo> ext = entry.getExtendedInfos();

            if (ext!=null && ext.size()>0) {
                Element extEs = e.addElement("extendedInfos");
                for (String key : ext.keySet()) {
                    Element extE = extEs.addElement("infos");
                    extE.addAttribute("name", key);
                    ExtendedInfo info = ext.get(key);

                    if (info.getSerializableValue()!=null ) {
                        if (info instanceof BooleanInfo) {
                            extE.addAttribute("type", "bool");
                            extE.setText(((BooleanInfo) info).getBooleanValue().toString());
                        } else if (info instanceof DateInfo) {
                            extE.addAttribute("type", "date");
                            extE.setText(((DateInfo) info).getDateValue().toGMTString());
                        } else if (info instanceof LongInfo) {
                            extE.addAttribute("type", "long");
                            extE.setText(((LongInfo) info).getLongValue().toString());
                        } else if (info instanceof DoubleInfo) {
                            extE.addAttribute("type", "double");
                            extE.setText(((DoubleInfo) info).getDoubleValue().toString());
                        } else if (info instanceof StringInfo) {
                            extE.addAttribute("type", "string");
                            extE.setText(((StringInfo) info).getStringValue().toString());
                        }
                    }

                }
            }
        }
    }
}
