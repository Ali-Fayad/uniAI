package com.uniai.chat.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Typed read-only DAO for admissions, documents, deadlines, funding, plans, and accreditation. */
public interface GraduateSupportRouteDao {
    List<SupportRow> findAdmissionRequirements(SupportCriteria criteria);
    List<SupportRow> findRequiredDocuments(SupportCriteria criteria);
    List<SupportRow> findDeadlines(SupportCriteria criteria);
    List<SupportRow> findScholarships(SupportCriteria criteria);
    List<SupportRow> findFinancialAid(SupportCriteria criteria);
    List<SupportRow> findPaymentPlans(SupportCriteria criteria);
    List<SupportRow> findAccreditations(SupportCriteria criteria);

    record SupportCriteria(
            List<Long> universityIds,
            String programName,
            String degreeLevel,
            String facultyName,
            String departmentName,
            List<String> itemTypes,
            String academicYear,
            String currency,
            String term,
            String status,
            Boolean requiredOnly,
            int limit
    ) {
        public SupportCriteria {
            universityIds=universityIds==null?List.of():List.copyOf(universityIds);
            itemTypes=itemTypes==null?List.of():List.copyOf(itemTypes);
            if(limit<1||limit>100)throw new IllegalArgumentException("Support route limit must be between 1 and 100");
        }
    }

    record SupportRow(
            long id,long universityId,String universityName,Long programId,String programName,
            String facultyName,String departmentName,String scopeLevel,String itemType,String name,
            String description,String operator,BigDecimal threshold,String thresholdUnit,Boolean required,
            String academicYear,String currency,BigDecimal amount,LocalDate dateFrom,LocalDate dateUntil,
            String status,String details,String sourceTitle,String sourceUrl
    ){}
}
