package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.port.out.GraduateSupportRouteDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/** Fixed table/column projections for support routes; callers cannot select SQL identifiers. */
@Component
public final class SqlGraduateSupportRouteDao implements GraduateSupportRouteDao {
    private final NamedParameterJdbcTemplate jdbc;
    public SqlGraduateSupportRouteDao(NamedParameterJdbcTemplate jdbc){this.jdbc=jdbc;}

    @Override public List<SupportRow> findAdmissionRequirements(SupportCriteria c){return query(Spec.ADMISSION,c);}
    @Override public List<SupportRow> findRequiredDocuments(SupportCriteria c){return query(Spec.DOCUMENT,c);}
    @Override public List<SupportRow> findDeadlines(SupportCriteria c){return query(Spec.DEADLINE,c);}
    @Override public List<SupportRow> findScholarships(SupportCriteria c){return query(Spec.SCHOLARSHIP,c);}
    @Override public List<SupportRow> findFinancialAid(SupportCriteria c){return query(Spec.FINANCIAL_AID,c);}
    @Override public List<SupportRow> findPaymentPlans(SupportCriteria c){return query(Spec.PAYMENT_PLAN,c);}
    @Override public List<SupportRow> findAccreditations(SupportCriteria c){return query(Spec.ACCREDITATION,c);}

    private List<SupportRow> query(Spec s,SupportCriteria c){
        MapSqlParameterSource p=new MapSqlParameterSource(); StringBuilder w=new StringBuilder();
        if(!c.universityIds().isEmpty()){w.append(" AND x.university_id IN (:universityIds)");p.addValue("universityIds",c.universityIds());}
        if (StringUtils.hasText(c.programName())) {
            w.append("""
                     AND (LOWER(BTRIM(gp.official_degree_name)) = LOWER(BTRIM(:programName))
                       OR LOWER(BTRIM(gp.major)) = LOWER(BTRIM(:programName))
                       OR LOWER(BTRIM(gp.program_key)) = LOWER(BTRIM(:programName))
                       OR EXISTS (SELECT 1 FROM graduate_program_alias gpa
                                  WHERE gpa.program_id = gp.id
                                    AND LOWER(BTRIM(gpa.alias)) = LOWER(BTRIM(:programName))))
                    """);
            p.addValue("programName", c.programName());
        }
        eq(w,p,c.degreeLevel(),"dt.code","degreeLevel"); eq(w,p,c.facultyName(),"fac.name","facultyName"); eq(w,p,c.departmentName(),"dep.name","departmentName");
        if(!c.itemTypes().isEmpty()&&s.typeColumn!=null){w.append(" AND x.").append(s.typeColumn).append(" IN (:itemTypes)");p.addValue("itemTypes",c.itemTypes());}
        if(StringUtils.hasText(c.academicYear())&&s.academicYear){eq(w,p,c.academicYear(),"x.academic_year","academicYear");}
        if(StringUtils.hasText(c.currency())&&s.currencyColumn!=null){eq(w,p,c.currency(),"x."+s.currencyColumn,"currency");}
        if(StringUtils.hasText(c.term())&&s==Spec.DEADLINE){eq(w,p,c.term(),"x.term","term");}
        if(StringUtils.hasText(c.status())&&s==Spec.ACCREDITATION){eq(w,p,c.status(),"x.status","status");}
        if(Boolean.TRUE.equals(c.requiredOnly())&&s==Spec.DOCUMENT)w.append(" AND x.is_optional=FALSE");
        p.addValue("limit",c.limit());
        String sql="SELECT x.id,u.id AS university_id,u.name AS university_name,gp.id AS program_id,"
                +"COALESCE(gp.official_degree_name,gp.major,gp.program_key) AS program_name,fac.name AS faculty_name,dep.name AS department_name,x.scope_level,"
                +s.select+",src.title AS source_title,src.url AS source_url FROM "+s.table+" x JOIN university u ON u.id=x.university_id "
                +"LEFT JOIN graduate_program gp ON gp.id=x.program_id LEFT JOIN degree_type dt ON dt.id=gp.degree_type_id "
                +"LEFT JOIN university_faculty fac ON fac.id=COALESCE(x.faculty_id,gp.faculty_id) LEFT JOIN university_department dep ON dep.id=COALESCE(x.department_id,gp.department_id) "
                +"JOIN source src ON src.id=x.source_id WHERE 1=1 "+w+" ORDER BY LOWER(u.name),x.id LIMIT :limit";
        return jdbc.query(sql,p,(rs,n)->new SupportRow(rs.getLong("id"),rs.getLong("university_id"),rs.getString("university_name"),
                rs.getObject("program_id",Long.class),rs.getString("program_name"),rs.getString("faculty_name"),rs.getString("department_name"),
                rs.getString("scope_level"),rs.getString("item_type"),rs.getString("item_name"),rs.getString("description"),rs.getString("comparison_operator"),
                rs.getBigDecimal("threshold_value"),rs.getString("threshold_unit"),rs.getObject("is_required",Boolean.class),rs.getString("academic_year"),
                rs.getString("currency"),rs.getBigDecimal("amount"),rs.getObject("date_from",java.time.LocalDate.class),rs.getObject("date_until",java.time.LocalDate.class),
                rs.getString("status"),rs.getString("details"),rs.getString("source_title"),rs.getString("source_url")));
    }
    private void eq(StringBuilder w,MapSqlParameterSource p,String v,String expression,String name){if(StringUtils.hasText(v)){w.append(" AND LOWER(BTRIM(").append(expression).append("))=LOWER(BTRIM(:").append(name).append("))");p.addValue(name,v);}}

    private enum Spec{
        ADMISSION("graduate_admission_requirement","requirement_type",false,null,"x.requirement_type AS item_type,NULL::text AS item_name,x.requirement_text AS description,x.comparison_operator,x.threshold_value,x.threshold_unit,x.is_required,NULL::text AS academic_year,NULL::text AS currency,NULL::numeric AS amount,NULL::date AS date_from,NULL::date AS date_until,NULL::text AS status,x.notes AS details"),
        DOCUMENT("graduate_required_document","document_type",false,null,"x.document_type AS item_type,x.document_name AS item_name,x.notes AS description,NULL::text AS comparison_operator,NULL::numeric AS threshold_value,NULL::text AS threshold_unit,(NOT x.is_optional) AS is_required,NULL::text AS academic_year,NULL::text AS currency,NULL::numeric AS amount,NULL::date AS date_from,NULL::date AS date_until,NULL::text AS status,NULL::text AS details"),
        DEADLINE("graduate_admission_deadline","deadline_type",true,null,"x.deadline_type AS item_type,x.term AS item_name,x.note AS description,NULL::text AS comparison_operator,NULL::numeric AS threshold_value,NULL::text AS threshold_unit,NULL::boolean AS is_required,x.academic_year,NULL::text AS currency,NULL::numeric AS amount,x.deadline_date AS date_from,NULL::date AS date_until,NULL::text AS status,NULL::text AS details"),
        SCHOLARSHIP("graduate_scholarship",null,true,"currency","'SCHOLARSHIP' AS item_type,x.name AS item_name,x.description,NULL::text AS comparison_operator,NULL::numeric AS threshold_value,NULL::text AS threshold_unit,NULL::boolean AS is_required,x.academic_year,x.currency,x.amount,NULL::date AS date_from,NULL::date AS date_until,NULL::text AS status,CONCAT_WS(' | ',x.coverage,x.notes) AS details"),
        FINANCIAL_AID("graduate_financial_aid",null,true,"currency","'FINANCIAL_AID' AS item_type,x.name AS item_name,x.description,NULL::text AS comparison_operator,NULL::numeric AS threshold_value,NULL::text AS threshold_unit,NULL::boolean AS is_required,x.academic_year,x.currency,x.amount,NULL::date AS date_from,NULL::date AS date_until,NULL::text AS status,x.notes AS details"),
        PAYMENT_PLAN("graduate_payment_plan",null,true,"down_payment_currency","'PAYMENT_PLAN' AS item_type,x.name AS item_name,x.description,NULL::text AS comparison_operator,x.installments_count::numeric AS threshold_value,'INSTALLMENTS' AS threshold_unit,NULL::boolean AS is_required,x.academic_year,x.down_payment_currency AS currency,x.down_payment_amount AS amount,NULL::date AS date_from,NULL::date AS date_until,NULL::text AS status,CONCAT_WS(' | ',x.interval_label,x.notes) AS details"),
        ACCREDITATION("graduate_accreditation",null,false,null,"'ACCREDITATION' AS item_type,x.name AS item_name,x.notes AS description,NULL::text AS comparison_operator,NULL::numeric AS threshold_value,NULL::text AS threshold_unit,NULL::boolean AS is_required,NULL::text AS academic_year,NULL::text AS currency,NULL::numeric AS amount,x.valid_from AS date_from,x.valid_until AS date_until,x.status,x.authority AS details");
        final String table,typeColumn,currencyColumn,select; final boolean academicYear;
        Spec(String table,String typeColumn,boolean academicYear,String currencyColumn,String select){this.table=table;this.typeColumn=typeColumn;this.academicYear=academicYear;this.currencyColumn=currencyColumn;this.select=select;}
    }
}
