package org.nuxeo.io.writer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.platform.audit.api.AuditLogger;
import org.nuxeo.ecm.platform.audit.api.ExtendedInfo;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.nuxeo.ecm.platform.audit.impl.ExtendedInfoImpl;
import org.nuxeo.ecm.platform.audit.impl.LogEntryImpl;
import org.nuxeo.runtime.api.Framework;

public class DocumentHistoryImporter implements ImportExtension {

    @Override
    public void updateImport(DocumentModel docModel, ExportedDocument xdoc) throws Exception {

        Element auditInfo = xdoc.getDocument().getRootElement().element("auditInfo");
        if (auditInfo != null) {

            List<Element> entries = auditInfo.elements("entry");
            if (entries != null && entries.size() > 0) {

                AuditLogger writer = Framework.getService(AuditLogger.class);
                List<LogEntry> auditEntries = new ArrayList<LogEntry>();

                for (Element entry : entries) {

                    String oldId = entry.attributeValue("id");
                    String category = entry.attributeValue("category");
                    String comment = entry.attributeValue("comment");
                    String lifeCycle = entry.attributeValue("lifeCycle");
                    String docPath = entry.attributeValue("docPath");
                    String docType = entry.attributeValue("docType");
                    String event = entry.attributeValue("event");
                    String principal = entry.attributeValue("principal");
                    String repository = entry.attributeValue("repository");
                    String eventDateGMT = entry.attributeValue("eventDate");
                    String logDateGMT = entry.attributeValue("logDate");

                    LogEntryImpl auditEntry = new LogEntryImpl();
                    auditEntry.setLogDate(new Date(Date.parse(logDateGMT)));
                    auditEntry.setEventDate(new Date(Date.parse(eventDateGMT)));

                    auditEntry.setCategory(category);
                    auditEntry.setComment(comment);
                    auditEntry.setDocLifeCycle(lifeCycle);
                    auditEntry.setDocPath(docPath);
                    auditEntry.setDocType(docType);
                    auditEntry.setDocUUID(docModel.getId());
                    auditEntry.setEventId(event);
                    auditEntry.setPrincipalName(principal);
                    auditEntry.setRepositoryId(repository);

                    Map<String, ExtendedInfo> ex = new HashMap<String, ExtendedInfo>();

                    Element extEs = entry.element("extendedInfos");
                    if (extEs != null) {
                        List<Element> infos = extEs.elements("infos");

                        if (infos != null && infos.size() > 0) {

                            for (Element info : infos) {
                                String key = info.attributeValue("name");
                                String type = info.attributeValue("type");
                                String value = info.getTextTrim();

                                ExtendedInfo ei = null;

                                if ("date".equalsIgnoreCase(type)) {
                                    ei = ExtendedInfoImpl.createExtendedInfo(Date.parse(value));
                                } else if ("long".equalsIgnoreCase(type)) {
                                    ei = ExtendedInfoImpl.createExtendedInfo(Long.parseLong(value));
                                } else if ("double".equalsIgnoreCase(type)) {
                                    ei = ExtendedInfoImpl.createExtendedInfo(Double.parseDouble(value));
                                } else {
                                    ei = ExtendedInfoImpl.createExtendedInfo(value);
                                }
                                ex.put(key, ei);
                            }

                        }
                    }
                    ex.put("oldId", ExtendedInfoImpl.createExtendedInfo(oldId));
                    auditEntry.setExtendedInfos(ex);
                    auditEntries.add(auditEntry);
                }

                if (auditEntries.size() > 0) {
                    writer.addLogEntries(auditEntries);
                }
            }
        }
    }
}
