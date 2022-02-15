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

import javax.inject.Inject;
import javax.inject.Singleton;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.CustomerSearchRequest;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>$END$</p>
 *
 * @author isv
 */
@Singleton
@Path("/private/customers")
public class CustomerResource {

    private static final Logger log = LoggerFactory.getLogger(CustomerResource.class);

    private static final String sessionCredentials = "credentials";
    private CustomerDAO customerDAO;
    private UnsecureDAO unsecureDAO;
    private UserDAO userDAO;

    public CustomerResource() {
    }

    /**
     * <p>Constructs new <code>CustomerResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public CustomerResource(CustomerDAO customerDAO, UnsecureDAO unsecureDAO, UserDAO userDAO) {
        this.customerDAO = customerDAO;
        this.unsecureDAO = unsecureDAO;
        this.userDAO = userDAO;
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response getAllCustomers() {
        try {
            return Response.OK(this.customerDAO.getAllCustomers());
        } catch (Exception e) {
            log.error("Unexpected error when searching for all customer accounts for", e);
            return Response.INTERNAL_ERROR();
        }
    }

    @GET
    @Path("/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response searchCustomers(@PathParam("value") String value) {
        try {
            return Response.OK(this.customerDAO.getAllCustomersByValue(value));
        } catch (Exception e) {
            log.error("Unexpected error when searching customer accounts for: {}", value, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response searchCustomers(CustomerSearchRequest request) {
        try {
            PaginatedData<Customer> customers = this.customerDAO.searchCustomers(request);
            return Response.OK(customers);
        } catch (Exception e) {
            log.error("Unexpected error when searching for customer accounts matching: {}", request, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomer(Customer customer) {
        try {
            Customer dbCustomer = customerDAO.getCustomerByName(customer.getName());
            if (dbCustomer != null && !dbCustomer.getId().equals(customer.getId())) {
                return Response.DUPLICATE_ENTITY("error.duplicate.customer.name");
            }
            if (customer.getEmail() != null && !customer.getEmail().trim().equals("")) {
                dbCustomer = customerDAO.getCustomerByEmail(customer.getEmail());
                if (dbCustomer != null && !dbCustomer.getId().equals(customer.getId())) {
                    return Response.DUPLICATE_ENTITY("error.duplicate.email");
                }

                User dbUser = unsecureDAO.findByEmail(customer.getEmail());
                if (dbUser != null && (customer.getId() == null || dbUser.getCustomerId() != customer.getId())) {
                    return Response.DUPLICATE_ENTITY("error.duplicate.email");
                }
            }

            if (customer.getId() == null) {
                String adminCredentials = this.customerDAO.insertCustomer(customer);
                Map<String, String> result = new HashMap<>();
                result.put("adminCredentials", adminCredentials);
                return Response.OK(result);
            } else {
                this.customerDAO.updateCustomer(customer);
                return Response.OK();
            }
        } catch (Exception e) {
            log.error("Unexpected error when saving customer account {}", customer, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeCustomer(@PathParam("id") Integer id) {
        try {
            this.customerDAO.removeCustomerById(id);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when deleting customer account #{}", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerForUpdate(@PathParam("id") Integer id) {
        try {
            final Customer customer = this.customerDAO.findByIdForUpdate(id);
            return Response.OK(customer);
        } catch (Exception e) {
            log.error("Unexpected error when loading customer details for update: #{}", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @GET
    @Path("/prefix/{prefix}/used")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validatePrefix(@PathParam("prefix") String prefix) {
        try {
            final boolean prefixUsed = this.customerDAO.isPrefixUsed(prefix);
            return Response.OK(prefixUsed);
        } catch (Exception e) {
            log.error("Unexpected error when checking customer prefix {} for usage", prefix, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @GET
    @Path("/impersonate/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response impersonateCustomer(@PathParam("id") Integer id,
                                        @Context HttpServletRequest req,
                                        @Context HttpServletResponse res) throws IOException {
        try {
            if (SecurityContext.get().isSuperAdmin()) {
                Customer customer = this.customerDAO.findById(id);
                User orgAdmin = this.userDAO.findOrgAdmin(customer.getId());
                if (orgAdmin != null) {
                    HttpSession session = req.getSession( false );
                    if ( session != null ) {
                        session.invalidate();
                    }

                    // If the org admin didn't log in, we need to generate a token for him
                    if (orgAdmin.getAuthToken() == null || orgAdmin.getAuthToken().length() == 0) {
                        // findOrgAdmin() doesn't return password, so we need to get the user's password
                        User user = unsecureDAO.findByLoginOrEmail(orgAdmin.getLogin());
                        user.setAuthToken(PasswordUtil.generateToken());
                        user.setNewPassword(user.getPassword());        // copy value for setUserNewPasswordUnsecure
                        unsecureDAO.setUserNewPasswordUnsecure(user);
                        orgAdmin.setAuthToken(user.getAuthToken());
                    }

                    // Notice: impersonation doesn't work if the password reset token is set
                    // An alternative would be to clear the password reset token, but it seems to be less secure
                    // So let's disable impersonation before the user resets his password by now
                    // The same behavior will be in users/impersonate (called by the org admin)

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
        } catch (Exception e) {
            log.error("Unexpected error when impersonating administrator for customer account: #{}", id, e);
            return Response.INTERNAL_ERROR();
        }
    }
}
