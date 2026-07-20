package com.uniai.chat.application.planning;

import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao.AcademicRow;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao.CampusRow;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao.CatalogCriteria;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao.UniversityRow;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao.UniversityStatisticsRow;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Route execution for geography, university, faculty, department, and overview routes. */
public final class GraduateCatalogRouteHandler<T> implements GraduateAiRouteHandler<T> {
    private final GraduateAiRoute route;
    private final Class<T> argumentType;
    private final GraduateCatalogRouteDao dao;

    public GraduateCatalogRouteHandler(GraduateAiRoute route, Class<T> argumentType, GraduateCatalogRouteDao dao) {
        this.route=route; this.argumentType=argumentType; this.dao=dao;
    }
    @Override public GraduateAiRoute route(){return route;}
    @Override public Class<T> argumentType(){return argumentType;}
    @Override public GraduateRouteExecutionResult execute(T arguments){throw new GraduateRoutePlanningException("Catalog routes require resolved entity context");}

    @Override
    public GraduateRouteExecutionResult executeResolved(ResolvedGraduateRoutePlan<T> plan) {
        CatalogCriteria criteria=criteria(plan.arguments(),plan.universities());
        return switch(route){
            case GET_GRADUATE_OVERVIEW, COMPARE_UNIVERSITIES -> statistics(plan,dao.universityStatistics(criteria));
            case LIST_UNIVERSITIES,SEARCH_UNIVERSITIES,GET_UNIVERSITY_DETAILS,LIST_UNIVERSITIES_BY_CITY -> universities(plan,dao.findUniversities(criteria));
            case COUNT_UNIVERSITIES -> scalar(plan,"University count",dao.countUniversities(criteria));
            case LIST_CAMPUSES,SEARCH_CAMPUSES,GET_CAMPUS_DETAILS -> campuses(plan,dao.findCampuses(criteria));
            case CHECK_CAMPUS_EXISTS -> scalar(plan,"Campus exists",dao.countCampuses(criteria)>0);
            case COUNT_CAMPUSES -> scalar(plan,"Campus count",dao.countCampuses(criteria));
            case COMPARE_CAMPUS_COUNTS -> campusComparison(plan,dao.findCampuses(criteria));
            case LIST_FACULTIES,SEARCH_FACULTIES,GET_FACULTY_DETAILS -> academic(plan,dao.findFaculties(criteria));
            case COUNT_FACULTIES -> scalar(plan,"Faculty count",dao.countFaculties(criteria));
            case LIST_DEPARTMENTS,SEARCH_DEPARTMENTS,GET_DEPARTMENT_DETAILS -> academic(plan,dao.findDepartments(criteria));
            case COUNT_DEPARTMENTS -> scalar(plan,"Department count",dao.countDepartments(criteria));
            default -> throw new GraduateRoutePlanningException("Unsupported catalog route: "+route);
        };
    }

    private CatalogCriteria criteria(Object args,List<ResolvedUniversity> universities){
        Values v=Values.from(args);
        return new CatalogCriteria(universities.stream().map(ResolvedUniversity::id).filter(Objects::nonNull).toList(),
                v.query,v.city,v.campus,v.faculty,v.department,v.country,v.limit==null?defaultLimit():v.limit);
    }

    private int defaultLimit(){
        return switch(route){
            case GET_UNIVERSITY_DETAILS,GET_CAMPUS_DETAILS,GET_FACULTY_DETAILS,GET_DEPARTMENT_DETAILS->1;
            case COMPARE_UNIVERSITIES,COMPARE_CAMPUS_COUNTS->10;
            case GET_GRADUATE_OVERVIEW->25;
            default->50;
        };
    }

    private GraduateRouteExecutionResult universities(ResolvedGraduateRoutePlan<T> plan,List<UniversityRow> rows){
        StringBuilder out=new StringBuilder("Universities:\n");
        for(UniversityRow r:rows){out.append("- ").append(r.name()); append(out,"Acronym",r.acronym()); append(out,"Arabic name",r.nameAr()); append(out,"Country",r.country()); out.append('\n');}
        return result(plan,out.toString().trim(),List.of(),rows.isEmpty());
    }
    private GraduateRouteExecutionResult campuses(ResolvedGraduateRoutePlan<T> plan,List<CampusRow> rows){
        StringBuilder out=new StringBuilder("Campuses:\n");
        for(CampusRow r:rows){out.append("- University: ").append(r.universityName()); append(out,"Campus",r.name()); append(out,"Type",r.campusType()); append(out,"City",r.city()); append(out,"Locality",r.locality()); if(r.latitude()!=null&&r.longitude()!=null) append(out,"Coordinates",r.latitude()+", "+r.longitude()); out.append('\n');}
        return result(plan,out.toString().trim(),List.of(),rows.isEmpty());
    }
    private GraduateRouteExecutionResult academic(ResolvedGraduateRoutePlan<T> plan,List<AcademicRow> rows){
        StringBuilder out=new StringBuilder(route.name().contains("DEPARTMENT")?"Departments:\n":"Faculties and schools:\n");
        List<GraduateCitation> citations=new ArrayList<>(); int i=1;
        for(AcademicRow r:rows){out.append("- University: ").append(r.universityName()); append(out,"Name",r.name()); append(out,"Short name",r.shortName()); append(out,"Type",r.type()); append(out,"Faculty",r.facultyName()); append(out,"Notes",r.notes()); out.append('\n'); if(text(r.officialUrl())) citations.add(new GraduateCitation("route-academic-"+r.id(),"S"+(i++),r.name(),r.officialUrl(),r.type(),r.universityId(),r.universityName(),null,r.name()));}
        return result(plan,out.toString().trim(),citations,rows.isEmpty());
    }
    private GraduateRouteExecutionResult statistics(ResolvedGraduateRoutePlan<T> plan,List<UniversityStatisticsRow> rows){
        StringBuilder out=new StringBuilder("University graduate statistics:\n");
        for(UniversityStatisticsRow r:rows) out.append("- ").append(r.universityName()).append(" | Campuses: ").append(r.campusCount()).append(" | Faculties: ").append(r.facultyCount()).append(" | Departments: ").append(r.departmentCount()).append(" | Graduate programs: ").append(r.programCount()).append('\n');
        return result(plan,out.toString().trim(),List.of(),rows.isEmpty());
    }
    private GraduateRouteExecutionResult campusComparison(ResolvedGraduateRoutePlan<T> plan,List<CampusRow> rows){
        Map<Long,Long> counts=new LinkedHashMap<>(); rows.forEach(r->counts.merge(r.universityId(),1L,Long::sum));
        StringBuilder out=new StringBuilder("Campus count comparison:\n");
        for(ResolvedUniversity u:plan.universities()) out.append("- ").append(u.name()).append(": ").append(counts.getOrDefault(u.id(),0L)).append('\n');
        return result(plan,out.toString().trim(),List.of(),false);
    }
    private GraduateRouteExecutionResult scalar(ResolvedGraduateRoutePlan<T> plan,String label,Object value){return result(plan,label+": "+value,List.of(),false);}
    private GraduateRouteExecutionResult result(ResolvedGraduateRoutePlan<T> plan,String context,List<GraduateCitation> citations,boolean empty){return new GraduateRouteExecutionResult(route,plan.canonicalArguments(),context,citations,List.of(),empty,null);}
    private void append(StringBuilder b,String label,String value){if(text(value))b.append(" | ").append(label).append(": ").append(value.replace('\n',' '));}
    private boolean text(String v){return v!=null&&!v.isBlank();}

    private static final class Values{
        String query,city,campus,faculty,department,country; Integer limit;
        static Values from(Object a){Values v=new Values();
            if(a instanceof GraduateRouteArguments.OverviewArguments x){v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.ListUniversitiesArguments x){v.country=x.country();v.city=x.city();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.SearchUniversitiesArguments x){v.query=x.query();v.country=x.country();v.city=x.city();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.UniversityArguments x){}
            else if(a instanceof GraduateRouteArguments.CountUniversitiesArguments x){v.country=x.country();v.city=x.city();}
            else if(a instanceof GraduateRouteArguments.CityArguments x){v.city=x.city();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.CompareUniversitiesArguments x){v.limit=10;}
            else if(a instanceof GraduateRouteArguments.ListCampusesArguments x){v.city=x.city();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.SearchCampusesArguments x){v.query=x.query();v.city=x.city();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.CampusArguments x){v.campus=x.campusName();}
            else if(a instanceof GraduateRouteArguments.CampusExistsArguments x){v.campus=x.campusName();v.city=x.city();}
            else if(a instanceof GraduateRouteArguments.CountCampusesArguments x){v.city=x.city();}
            else if(a instanceof GraduateRouteArguments.CompareCampusCountsArguments x){v.city=x.city();v.limit=200;}
            else if(a instanceof GraduateRouteArguments.ListFacultiesArguments x){v.query=x.query();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.SearchFacultiesArguments x){v.query=x.query();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.FacultyArguments x){v.faculty=x.facultyName();}
            else if(a instanceof GraduateRouteArguments.CountFacultiesArguments x){}
            else if(a instanceof GraduateRouteArguments.ListDepartmentsArguments x){v.faculty=x.facultyName();v.query=x.query();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.SearchDepartmentsArguments x){v.query=x.query();v.faculty=x.facultyName();v.limit=x.limit();}
            else if(a instanceof GraduateRouteArguments.DepartmentArguments x){v.department=x.departmentName();v.faculty=x.facultyName();}
            else if(a instanceof GraduateRouteArguments.CountDepartmentsArguments x){v.faculty=x.facultyName();}
            else throw new GraduateRoutePlanningException("Unsupported catalog argument type: "+a.getClass().getSimpleName());
            return v;}
    }
}
