package com.hmdm.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.mapper.CustomerMapper;
import com.hmdm.rest.json.CustomerSearchRequest;
import java.util.Map;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Checks the SQL and bindings produced by MyBatis without requiring a database. */
class CustomerSearchSqlTest {

    private Configuration configuration() {
        Configuration configuration = new Configuration();
        configuration.getTypeAliasRegistry().registerAlias("Customer", Customer.class);
        configuration.addMapper(CustomerMapper.class);
        return configuration;
    }

    private BoundSql sql(String method, Object parameters) {
        return configuration().getMappedStatement(CustomerMapper.class.getName() + "." + method)
                .getBoundSql(parameters);
    }

    private String predicate(BoundSql sql) {
        String normalized = sql.getSql().replaceAll("\\s+", " ").trim();
        return normalized.substring(normalized.indexOf("WHERE ")).split(" ORDER BY ")[0];
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"alice@example.org", "O'Reilly' OR 1=1 --"})
    void paginationCountsTheSameCustomersAsTheResults(String search) {
        CustomerSearchRequest request = new CustomerSearchRequest();
        request.setSearchValue(search);
        request.setCurrentPage(2);
        request.setPageSize(20);
        for (Integer accountType : new Integer[] {null, 1}) {
            for (String status : new String[] {null, "customer.new", "customer.active"}) {
                request.setAccountType(accountType);
                request.setCustomerStatus(status);
                BoundSql results = sql("searchCustomers", request);
                BoundSql count = sql("countAllCustomers", request);
                assertEquals(predicate(results), predicate(count));
                assertEquals(results.getParameterMappings().stream()
                        .map(p -> p.getProperty())
                        .filter(p -> !p.equals("currentPage") && !p.equals("pageSize")).toList(),
                        count.getParameterMappings().stream().map(p -> p.getProperty()).toList());
                if (search != null && !search.isEmpty()) {
                    assertFalse(results.getSql().contains(search), "Search text must remain a bound parameter");
                }
            }
        }
    }

    @Test
    void customerLookupIncludesPersonalNamesAndEmail() {
        BoundSql lookup = sql("findAllByValue", Map.of("filter", "%alice%"));
        for (String field : new String[] {"name", "description", "firstname", "lastname", "email"}) {
            assertTrue(lookup.getSql().contains("LOWER(" + field + ") LIKE ?"), field);
        }
        assertEquals(5, lookup.getParameterMappings().size());
        assertTrue(lookup.getSql().contains("master = FALSE"));
    }
}
