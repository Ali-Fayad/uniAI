package com.uniai.chat.application.planning;

import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.port.out.GraduateSupportRouteDao;
import com.uniai.chat.application.port.out.GraduateSupportRouteDao.SupportCriteria;
import com.uniai.chat.application.port.out.GraduateSupportRouteDao.SupportRow;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Executes all fixed-schema admissions and support routes. */
public final class GraduateSupportRouteHandler<T> implements GraduateAiRouteHandler<T>{
    private final GraduateAiRoute route; private final Class<T> type; private final GraduateSupportRouteDao dao;
    public GraduateSupportRouteHandler(GraduateAiRoute route,Class<T> type,GraduateSupportRouteDao dao){this.route=route;this.type=type;this.dao=dao;}
    @Override public GraduateAiRoute route(){return route;} @Override public Class<T> argumentType(){return type;}
    @Override public GraduateRouteExecutionResult execute(T a){throw new GraduateRoutePlanningException("Support routes require resolved entity context");}
    @Override public GraduateRouteExecutionResult executeResolved(ResolvedGraduateRoutePlan<T> p){
        SupportCriteria c=criteria(p.arguments(),p.universities()); List<SupportRow> rows=switch(route){
            case GET_ADMISSION_REQUIREMENTS,GET_LANGUAGE_REQUIREMENTS,GET_TEST_REQUIREMENTS,CHECK_ADMISSION_REQUIREMENT,COMPARE_ADMISSION_REQUIREMENTS->dao.findAdmissionRequirements(c);
            case LIST_REQUIRED_DOCUMENTS->dao.findRequiredDocuments(c); case GET_APPLICATION_DEADLINES->dao.findDeadlines(c);
            case LIST_SCHOLARSHIPS->dao.findScholarships(c); case LIST_FINANCIAL_AID->dao.findFinancialAid(c);
            case LIST_PAYMENT_PLANS->dao.findPaymentPlans(c); case LIST_ACCREDITATIONS->dao.findAccreditations(c);
            default->throw new GraduateRoutePlanningException("Unsupported support route: "+route);};
        if(route==GraduateAiRoute.CHECK_ADMISSION_REQUIREMENT)return result(p,"Admission requirement exists: "+!rows.isEmpty(),List.of(),false);
        if(route==GraduateAiRoute.COMPARE_ADMISSION_REQUIREMENTS)return comparison(p,rows);
        return rows(p,rows);
    }
    private SupportCriteria criteria(Object a,List<ResolvedUniversity> u){Values v=Values.from(a,route);return new SupportCriteria(
            u.stream().map(ResolvedUniversity::id).filter(Objects::nonNull).toList(),v.program,v.degree,v.faculty,v.department,v.types,v.year,v.currency,v.term,v.status,v.required,v.limit==null?50:v.limit);}
    private GraduateRouteExecutionResult rows(ResolvedGraduateRoutePlan<T> p,List<SupportRow> rows){StringBuilder b=new StringBuilder(route.name().replace('_',' ')).append(":\n");
        for(SupportRow r:rows){b.append("- University: ").append(r.universityName());add(b,"Program",r.programName());add(b,"Faculty",r.facultyName());add(b,"Department",r.departmentName());add(b,"Scope",r.scopeLevel());add(b,"Type",r.itemType());add(b,"Name",r.name());add(b,"Description",r.description());add(b,"Operator",r.operator());add(b,"Threshold",r.threshold()==null?null:r.threshold()+" "+nullSafe(r.thresholdUnit()));add(b,"Required",r.required()==null?null:r.required().toString());add(b,"Academic year",r.academicYear());add(b,"Amount",r.amount()==null?null:nullSafe(r.currency())+" "+r.amount());add(b,"Date",r.dateFrom()==null?null:r.dateFrom().toString());add(b,"Valid until",r.dateUntil()==null?null:r.dateUntil().toString());add(b,"Status",r.status());add(b,"Details",r.details());b.append('\n');}
        return result(p,b.toString().trim(),citations(rows),rows.isEmpty());}
    private GraduateRouteExecutionResult comparison(ResolvedGraduateRoutePlan<T> p,List<SupportRow> rows){StringBuilder b=new StringBuilder("Admission requirement comparison:\n");for(ResolvedUniversity u:p.universities()){long count=rows.stream().filter(r->r.universityId()==u.id()).count();b.append("- ").append(u.name()).append(" | Matching requirements: ").append(count).append('\n');}return result(p,b.toString().trim(),citations(rows),false);}
    private GraduateRouteExecutionResult result(ResolvedGraduateRoutePlan<T> p,String c,List<GraduateCitation> cites,boolean empty){return new GraduateRouteExecutionResult(route,p.canonicalArguments(),c,cites,List.of(),empty,null);}
    private List<GraduateCitation> citations(List<SupportRow> rows){List<GraduateCitation> c=new ArrayList<>();int i=1;for(SupportRow r:rows)if(text(r.sourceUrl()))c.add(new GraduateCitation("route-support-"+route+"-"+r.id(),"S"+(i++),r.sourceTitle(),r.sourceUrl(),route.name(),r.universityId(),r.universityName(),r.programId(),r.programName()));return List.copyOf(c);}
    private void add(StringBuilder b,String l,String v){if(text(v))b.append(" | ").append(l).append(": ").append(v.replace('\n',' '));}private boolean text(String v){return v!=null&&!v.isBlank();}private String nullSafe(String v){return v==null?"":v;}
    private static final class Values{String program,degree,faculty,department,year,currency,term,status;List<String>types=List.of();Boolean required;Integer limit;
        static Values from(Object a,GraduateAiRoute route){Values v=new Values();
            if(a instanceof GraduateRouteArguments.RequirementArguments x){v.program=x.programName();v.faculty=x.facultyName();v.department=x.departmentName();v.degree=n(x.degreeType());v.types=x.requirementType()==null?List.of():List.of(x.requirementType().name());v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.LanguageRequirementArguments x){v.program=x.programName();v.degree=n(x.degreeType());v.types=List.of("ENGLISH");v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.TestRequirementArguments x){v.program=x.programName();v.degree=n(x.degreeType());v.types=x.tests()==null?List.of():x.tests().stream().map(Enum::name).toList();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.DocumentArguments x){v.program=x.programName();v.faculty=x.facultyName();v.department=x.departmentName();v.types=x.documentType()==null?List.of():List.of(x.documentType());v.required=x.requiredOnly();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.DeadlineArguments x){v.program=x.programName();v.faculty=x.facultyName();v.department=x.departmentName();v.year=x.academicYear();v.types=x.deadlineType()==null?List.of():List.of(x.deadlineType().name());v.term=x.term();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.RequirementExistsArguments x){v.program=x.programName();v.degree=n(x.degreeType());v.types=List.of(x.requirementType().name());v.limit=1;}
            else if(a instanceof GraduateRouteArguments.CompareRequirementArguments x){v.program=x.programName();v.degree=n(x.degreeType());v.types=x.requirementTypes()==null?List.of():x.requirementTypes().stream().map(Enum::name).toList();v.limit=100;}
            else if(a instanceof GraduateRouteArguments.ScholarshipArguments x){v.program=x.programName();v.faculty=x.facultyName();v.department=x.departmentName();v.year=x.academicYear();v.currency=x.currency();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.FinancialAidArguments x){v.program=x.programName();v.faculty=x.facultyName();v.department=x.departmentName();v.year=x.academicYear();v.currency=x.currency();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.PaymentPlanArguments x){v.program=x.programName();v.faculty=x.facultyName();v.department=x.departmentName();v.year=x.academicYear();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.AccreditationArguments x){v.program=x.programName();v.faculty=x.facultyName();v.department=x.departmentName();v.status=x.status();v.limit=x.limit();}
            else throw new GraduateRoutePlanningException("Unsupported support argument type: "+a.getClass().getSimpleName());return v;}private static String n(Enum<?>e){return e==null?null:e.name();}}
}
