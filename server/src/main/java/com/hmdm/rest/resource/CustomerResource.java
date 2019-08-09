/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.rest.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>$END$</p>
 *
 * @author isv
 */
@Singleton
@Path("/private/customers")
public class CustomerResource {

    private static final String sessionCredentials = "credentials";
    private CustomerDAO customerDAO;
    private UserDAO userDAO;

    public CustomerResource() {
    }

    /**
     * <p>Constructs new <code>CustomerResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public CustomerResource(CustomerDAO customerDAO, UserDAO userDAO) {
        this.customerDAO = customerDAO;
        this.userDAO = userDAO;
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCustomers() {
        return Response.OK(this.customerDAO.getAllCustomers());
    }

    @GET
    @Path("/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchCustomers(@PathParam("value") String value) {
        return Response.OK(this.customerDAO.getAllCustomersByValue(value));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomer(Customer customer) {
        Customer dbCustomer = this.customerDAO.getCustomerByName(customer.getName());
        if (dbCustomer != null && !dbCustomer.getId().equals(customer.getId())) {
            return Response.ERROR();
        } else {
            if (customer.getId() == null) {
                String adminCredentials = this.customerDAO.insertCustomer(customer);
                Map<String, String> result = new HashMap<>();
                result.put("adminCredentials", adminCredentials);
                return Response.OK(result);
            } else {
                this.customerDAO.updateCustomer(customer);
                return Response.OK();
            }
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeCustomer(@PathParam("id") Integer id) {
        this.customerDAO.removeCustomerById(id);
        return Response.OK();
    }

    @GET
    @Path("/impersonate/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response impersonateCustomer(@PathParam("id") Integer id,
                                        @Context HttpServletRequest req,
                                        @Context HttpServletResponse res) throws IOException {
        if (SecurityContext.get().isSuperAdmin()) {
            Customer customer = this.customerDAO.findById(id);
            User orgAdmin = this.userDAO.findOrgAdmin(customer.getId());
            if (orgAdmin != null) {
                HttpSession session = req.getSession( false );
                if ( session != null ) {
                    session.invalidate();
                }

                orgAdmin.setPassword(null);

                HttpSession userSession = req.getSession(true);
                userSession.setAttribute( sessionCredentials, orgAdmin );

                return Response.OK( orgAdmin );
            } else {
                return Response.ERROR("error.notfound.customer.admin");
            }
        } else {
            return Response.PERMISSION_DENIED();
        }
    }
}
