package com.uniai.catalog.infrastructure.persistence.adapter;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.infrastructure.persistence.repository.UniversityCatalogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UniversityCatalogRepositoryAdapterTest {

    private RepositoryStub stub;
    private UniversityCatalogRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        stub = new RepositoryStub();
        UniversityCatalogJpaRepository jpaRepository = stub.repository();
        adapter = new UniversityCatalogRepositoryAdapter(jpaRepository);
    }

    @Test
    void findAllShouldPreferInstitutionLevelRowOverCampusRows() {
        UniversityCatalog institutionLevel = university(10L, "American University of Beirut", "AUB", null, null);
        UniversityCatalog mainCampus = university(20L, "American University of Beirut", "AUB", "Main Campus", "Main");
        UniversityCatalog branchCampus = university(30L, "American University of Beirut", "AUB", "Beirut Campus", "Branch");
        stub.allRows = List.of(branchCampus, mainCampus, institutionLevel);

        List<UniversityCatalog> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void findAllShouldPreferMainCampusWhenInstitutionLevelRowIsMissing() {
        UniversityCatalog lowerIdCampus = university(5L, "Beirut Arab University", "BAU", "Branch Campus", "Branch");
        UniversityCatalog mainCampus = university(2L, "Beirut Arab University", "BAU", "Main Campus", "Main");
        UniversityCatalog anotherCampus = university(8L, "Beirut Arab University", "BAU", "Tripoli Campus", "Branch");
        stub.allRows = List.of(lowerIdCampus, anotherCampus, mainCampus);

        List<UniversityCatalog> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void findAllShouldFallbackToLowestIdWhenNoPreferredCampusExists() {
        UniversityCatalog higherId = university(12L, "Lebanese American University", "LAU", "Byblos Campus", "Branch");
        UniversityCatalog lowestId = university(3L, "Lebanese American University", "LAU", "Beirut Campus", "Branch");
        UniversityCatalog middleId = university(7L, "Lebanese American University", "LAU", "Tripoli Campus", "Branch");
        stub.allRows = List.of(higherId, lowestId, middleId);

        List<UniversityCatalog> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
    }

    @Test
    void searchByNameShouldMatchNameAcronymAndArabicNameAndCanonicalizeResults() {
        UniversityCatalog match = university(41L, "American University of Beirut", "AUB", null, null);
        UniversityCatalog campusRow = university(42L, "American University of Beirut", "AUB", "Main Campus", "Main");
        stub.searchRows = List.of(campusRow, match);

        List<UniversityCatalog> result = adapter.searchByName("AUB");

        assertEquals(1, result.size());
        assertEquals(41L, result.get(0).getId());
        assertEquals("AUB", stub.lastSearchArgs.get()[0]);
        assertEquals("AUB", stub.lastSearchArgs.get()[1]);
        assertEquals("AUB", stub.lastSearchArgs.get()[2]);
    }

    private UniversityCatalog university(Long id, String name, String acronym, String campusName, String campusType) {
        return UniversityCatalog.builder()
                .id(id)
                .name(name)
                .acronym(acronym)
                .campusName(campusName)
                .campusType(campusType)
                .build();
    }

    private static final class RepositoryStub implements InvocationHandler {
        private List<UniversityCatalog> allRows = List.of();
        private List<UniversityCatalog> searchRows = List.of();
        private final AtomicReference<Object[]> lastSearchArgs = new AtomicReference<>(new Object[0]);

        UniversityCatalogJpaRepository repository() {
            return (UniversityCatalogJpaRepository) Proxy.newProxyInstance(
                    UniversityCatalogJpaRepository.class.getClassLoader(),
                    new Class<?>[]{UniversityCatalogJpaRepository.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "findAll" -> allRows;
                case "findByNameContainingIgnoreCaseOrAcronymContainingIgnoreCaseOrNameArContainingIgnoreCaseOrderByNameAsc" -> {
                    lastSearchArgs.set(args == null ? new Object[0] : args.clone());
                    yield searchRows;
                }
                case "toString" -> "UniversityCatalogJpaRepositoryStub";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object defaultValue(Class<?> returnType) {
            if (returnType == Void.TYPE) {
                return null;
            }
            if (returnType == Boolean.TYPE) {
                return false;
            }
            if (returnType == Byte.TYPE) {
                return (byte) 0;
            }
            if (returnType == Short.TYPE) {
                return (short) 0;
            }
            if (returnType == Integer.TYPE) {
                return 0;
            }
            if (returnType == Long.TYPE) {
                return 0L;
            }
            if (returnType == Float.TYPE) {
                return 0f;
            }
            if (returnType == Double.TYPE) {
                return 0d;
            }
            return null;
        }
    }
}
