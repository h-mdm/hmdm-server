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

import com.hmdm.persistence.CommonDAO;
import com.hmdm.persistence.PendingSignupDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.PendingSignup;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.Response;
import com.hmdm.rest.json.SignupCompleteRequest;
import com.hmdm.security.SecurityContext;
import com.hmdm.service.EmailService;
import com.hmdm.service.MailchimpService;
import com.hmdm.util.PasswordUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Api(tags = {"Signup"})
@Singleton
@Path("/public/signup")
public class SignupResource {

    private static final Logger logger = LoggerFactory.getLogger(SignupResource.class);

    private String baseUrl;
    private CommonDAO commonDAO;
    private UnsecureDAO unsecureDAO;
    private PendingSignupDAO pendingSignupDAO;
    private EmailService emailService;
    private MailchimpService mailchimpService;
    private String adminEmail;
    private boolean customerSignup;
    private boolean customerSignupCopySettings;
    private Integer[] customerSignupConfigurations;
    private String customerSignupSupportEmail;
    private int customerSignupDeviceLimit;
    private int customerSignupSizeLimit;
    private int customerSignupExpiryDays;
    private int customerSignupDeviceConfig;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public SignupResource() {
    }

    /**
     * <p>Constructs new <code>SignupResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public SignupResource(CommonDAO commonDAO, UnsecureDAO unsecureDAO, PendingSignupDAO pendingSignupDAO,
                          EmailService emailService, MailchimpService mailchimpService,
                          @Named("admin.email") String adminEmail,
                          @Named("base.url") String baseUrl,
                          @Named("customer.signup") boolean customerSignup,
                          @Named("customer.signup.copy.settings") boolean customerSignupCopySettings,
                          @Named("customer.signup.configurations") String customerSignupConfigStr,
                          @Named("customer.signup.support.email") String customerSignupSupportEmail,
                          @Named("customer.signup.device.limit") String customerSignupDeviceLimitStr,
                          @Named("customer.signup.size.limit") String customerSignupSizeLimitStr,
                          @Named("customer.signup.expiry.days") String customerSignupExpiryDaysStr,
                          @Named("customer.signup.device.config") String customerSignupDeviceConfigStr) {
        this.commonDAO = commonDAO;
        this.unsecureDAO = unsecureDAO;
        this.pendingSignupDAO = pendingSignupDAO;
        this.emailService = emailService;
        this.mailchimpService = mailchimpService;
        this.adminEmail = adminEmail;
        this.baseUrl = baseUrl;
        this.customerSignup = customerSignup;
        this.customerSignupCopySettings = customerSignupCopySettings;
        this.customerSignupSupportEmail = customerSignupSupportEmail;
        this.customerSignupDeviceLimit = parseInt(customerSignupDeviceLimitStr, 3);
        this.customerSignupSizeLimit = parseInt(customerSignupSizeLimitStr, 100);
        this.customerSignupExpiryDays = parseInt(customerSignupExpiryDaysStr, 365);
        this.customerSignupDeviceConfig = parseInt(customerSignupDeviceConfigStr, 1);
        String[] configIdStrs = customerSignupConfigStr.split(",");
        List<Integer> list = new LinkedList<>();
        for (String configId : configIdStrs) {
            try {
                Integer n = Integer.parseInt(configId);
                list.add(n);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        this.customerSignupConfigurations = new Integer[list.size()];
        list.toArray(this.customerSignupConfigurations);
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Signup feature",
            notes = "Checks if the customer signup is allowed."
    )
    @GET
    @Deprecated
    @Path("/canSignup")
    @Produces(MediaType.APPLICATION_JSON)
    public Response canSignup() {
        if (emailService.isConfigured() && customerSignup) {
            return Response.OK();
        } else {
            return Response.ERROR();
        }
    }


    // =================================================================================================================
    @ApiOperation(
            value = "Verify email",
            notes = "Check whether the email doesn't exist and start the signup flow"
    )
    @POST
    @Path("/verifyEmail")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyEmail(PendingSignup signupData) {
        signupData.setEmail(signupData.getEmail().toLowerCase());
        // Check email existence
        Customer dbCustomer = unsecureDAO.getCustomerByEmailUnsecure(signupData.getEmail());
        if (dbCustomer != null) {
            return Response.DUPLICATE_ENTITY("signup.email.used");
        }
        User dbUser = unsecureDAO.findByEmail(signupData.getEmail());
        if (dbUser != null) {
            return Response.DUPLICATE_ENTITY("signup.email.used");
        }

        // Protection against email bombing
        PendingSignup pendingSignup = pendingSignupDAO.getByEmail(signupData.getEmail());
        if (pendingSignup != null && pendingSignup.getSignupTime() != null &&
                pendingSignup.getSignupTime() + 60000 > System.currentTimeMillis()) {
            // We allow resending email only after 1 minute
            return Response.DUPLICATE_ENTITY("signup.email.used");
        }

        signupData.setSignupTime(System.currentTimeMillis());
        signupData.setToken(PasswordUtil.generateToken());
        pendingSignupDAO.insert(signupData);

        // Generate and send the signup email
        String subj = emailService.getVerifyEmailSubj(signupData.getLanguage());
        String body = emailService.getVerifyEmailBody(signupData.getLanguage(), signupData.getToken());
        emailService.sendEmail(signupData.getEmail(), subj, body);

        return Response.OK();
    }


    // =================================================================================================================
    @ApiOperation(
            value = "Verify token",
            notes = "Checks if the customer's token is valid."
    )
    @GET
    @Path("/verifyToken/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyToken(@PathParam("token") @ApiParam("Customer's token") String token) {
        PendingSignup signup = pendingSignupDAO.getByToken(token);
        if (signup == null) {
            return Response.OBJECT_NOT_FOUND_ERROR();
        }
        return Response.OK(signup);
    }


    // =================================================================================================================
    @ApiOperation(
            value = "Complete the registration",
            notes = "Create a new customer and notify admins and customer itself."
    )
    @POST
    @Path("/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response complete(SignupCompleteRequest data) {
        try {
            Customer dbCustomer = unsecureDAO.getCustomerByNameUnsecure(data.getName());
            if (dbCustomer != null) {
                return Response.DUPLICATE_ENTITY("error.duplicate.customer.name");
            }

            PendingSignup signup = pendingSignupDAO.getByToken(data.getToken());
            if (signup == null) {
                return Response.OBJECT_NOT_FOUND_ERROR();
            }

            Customer customer = new Customer();
            customer.setName(data.getName().trim());
            if (signup.getLanguage().equals("ru")) {
                customer.setLanguage("ru");
            }
            customer.setEmail(signup.getEmail().toLowerCase().trim());
            customer.setFirstName(data.getFirstName().trim());
            customer.setLastName(data.getLastName().trim());
            String description = data.getCompany() != null ? data.getCompany() : "";
            if (data.getDescription() != null && !data.getDescription().trim().equals("")) {
                if (!description.equals("")) {
                    description += "\n";
                }
                description += data.getDescription().trim();
            }
            customer.setDescription(description);
            customer.setCustomerStatus(Customer.CUSTOMER_NEW);
            customer.setAccountType(Customer.Demo);
            customer.setDeviceLimit(customerSignupDeviceLimit);
            customer.setSizeLimit(customerSignupSizeLimit);
            customer.setExpiryTime(System.currentTimeMillis() + customerSignupExpiryDays * 86400000l);
            customer.setConfigurationIds(customerSignupConfigurations);
            customer.setDeviceConfigurationId(customerSignupDeviceConfig);

            unsecureDAO.signupCustomerUnsecure(customer, data.getPasswd(), customerSignupCopySettings);

            pendingSignupDAO.remove(customer.getEmail());

            // Notify the customer
            emailService.sendEmail(customer.getEmail(),
                    emailService.getSignupCompleteEmailSubj(customer.getLanguage()),
                    emailService.getSignupCompleteEmailBody(customer),
                    customerSignupSupportEmail);

            // Notify the admin
            emailService.sendEmail(adminEmail,
                    emailService.getSignupNotifyEmailSubj(),
                    emailService.getSignupNotifyEmailBody(customer));

            // Notify the support
            if (!customerSignupSupportEmail.equals("")) {
                emailService.sendEmail(customerSignupSupportEmail,
                        emailService.getSignupNotifyEmailSubj(),
                        emailService.getSignupNotifyEmailBody(customer));
            }

            // Save to Mailchimp
            if (mailchimpService.initialize()) {
                mailchimpService.subscribe(customer, "self_signup");
            }

            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when signing up a customer {}", data, e);
            e.printStackTrace();
            return Response.INTERNAL_ERROR();
        }
    }

    private int parseInt(String parameter, int defaultValue) {
        int ret = defaultValue;
        if (parameter != null) {
            try {
                ret = Integer.parseInt(parameter);
            } catch (NumberFormatException e) {
            }
        }
        return ret;
    }

}
