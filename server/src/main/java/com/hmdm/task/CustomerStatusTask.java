package com.hmdm.task;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.service.EmailService;
import com.hmdm.service.MailchimpService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CustomerStatusTask implements Runnable {
    private final UnsecureDAO unsecureDAO;
    private final EmailService emailService;
    private final MailchimpService mailchimpService;
    private final String adminEmail;

    private final int INTERVAL_PAUSE_1   = 3;
    private final int INTERVAL_PAUSE_2   = 5;
    private final int INTERVAL_ABANDON_1 = 7;
    private final int INTERVAL_ABANDON_2 = 14;
    private final int INTERVAL_DENIAL    = 14;

    @Inject
    public CustomerStatusTask(UnsecureDAO unsecureDAO,
                              EmailService emailService,
                              MailchimpService mailchimpService,
                              @Named("admin.email") String adminEmail) {
        this.unsecureDAO = unsecureDAO;
        this.emailService = emailService;
        this.mailchimpService = mailchimpService;
        this.adminEmail = adminEmail;
    }

    @Override
    public void run() {
        List<Customer> customers = unsecureDAO.getFollowUpCustomersUnsecure();
        Map<String,Customer> updatedCustomersMap = new HashMap<>();

        for (Customer customer : customers) {
            long now = System.currentTimeMillis();

            if (customer.getLastLoginTime() == null) {
                long daysAfterRegistration = (now - customer.getRegistrationTime()) / 86400000l;
                // If not signed, we change status based on days after registration
                // We compare days in reverse order - this is important.
                if (daysAfterRegistration > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2 +
                        INTERVAL_ABANDON_1 + INTERVAL_ABANDON_2 + INTERVAL_DENIAL) {
                    customer.setCustomerStatus(Customer.CUSTOMER_DENIAL);
                    updatedCustomersMap.put(customer.getName(), customer);
                } else if (daysAfterRegistration > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2 +
                        INTERVAL_ABANDON_1 + INTERVAL_ABANDON_2) {
                    if (customer.getAbandonState() < 2) {
                        customer.setCustomerStatus(Customer.CUSTOMER_ABANDON);
                        customer.setAbandonState(2);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                } else if (daysAfterRegistration > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2 +
                        INTERVAL_ABANDON_1) {
                    if (customer.getAbandonState() < 1) {
                        customer.setCustomerStatus(Customer.CUSTOMER_ABANDON);
                        customer.setAbandonState(1);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                } else if (daysAfterRegistration > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2) {
                    if (customer.getInactiveState() < 2) {
                        customer.setCustomerStatus(Customer.CUSTOMER_INACTIVE);
                        customer.setInactiveState(2);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                } else if (daysAfterRegistration > INTERVAL_PAUSE_1) {
                    if (customer.getInactiveState() < 1) {
                        customer.setCustomerStatus(Customer.CUSTOMER_INACTIVE);
                        customer.setInactiveState(1);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                }
            } else {
                long daysAfterSignIn = (now - customer.getLastLoginTime()) / 86400000l;
                if (customer.getCustomerStatus().equals(Customer.CUSTOMER_NEW) ||
                        customer.getCustomerStatus().equals(Customer.CUSTOMER_INACTIVE)) {
                    // Inactive customer signed in, changing its status
                    customer.setCustomerStatus(Customer.CUSTOMER_ACTIVE);
                    updatedCustomersMap.put(customer.getName(), customer);
                    // Note: the status can change further
                }

                // If not signed, we change status based on days after registration
                // Reverse order for comparison
                if (daysAfterSignIn > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2 +
                        INTERVAL_ABANDON_1 + INTERVAL_ABANDON_2 + INTERVAL_DENIAL) {
                    customer.setCustomerStatus(Customer.CUSTOMER_DENIAL);
                    updatedCustomersMap.put(customer.getName(), customer);
                } else if (daysAfterSignIn > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2 +
                        INTERVAL_ABANDON_1 + INTERVAL_ABANDON_2) {
                    if (customer.getAbandonState() < 2) {
                        customer.setCustomerStatus(Customer.CUSTOMER_ABANDON);
                        customer.setAbandonState(2);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                } else if (daysAfterSignIn > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2 +
                        INTERVAL_ABANDON_1) {
                    if (customer.getAbandonState() < 1) {
                        customer.setCustomerStatus(Customer.CUSTOMER_ABANDON);
                        customer.setAbandonState(1);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                } else if (daysAfterSignIn > INTERVAL_PAUSE_1 + INTERVAL_PAUSE_2) {
                    if (customer.getPauseState() < 2) {
                        customer.setCustomerStatus(Customer.CUSTOMER_PAUSE);
                        customer.setPauseState(2);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                } else if (daysAfterSignIn > INTERVAL_PAUSE_1) {
                    if (customer.getPauseState() < 1) {
                        customer.setCustomerStatus(Customer.CUSTOMER_PAUSE);
                        customer.setPauseState(1);
                        updatedCustomersMap.put(customer.getName(), customer);
                    }
                }
            }
        }

        if (updatedCustomersMap.size() == 0) {
            return;
        }

        // Prepare the list
        List<Customer> updatedCustomers = new LinkedList<>();
        for (Map.Entry<String, Customer> entry : updatedCustomersMap.entrySet()) {
            unsecureDAO.updateCustomerUnsecure(entry.getValue());
            updatedCustomers.add(entry.getValue());
        }

        // Notify the admin
        String adminSubj = "Headwind MDM customer status changed";
        String adminBody = "Hi,<br>\nThe following customers changed their status and notified:<br><br>\n\n";
        for (Customer c : updatedCustomers) {
            adminBody += c.getName() + "/" + c.getEmail() + ", status " + c.getCustomerStatus() + ", states I" +
                    c.getInactiveState() + "/P" + c.getPauseState() + "/A" + c.getAbandonState() + "<br>\n";
        }
        if (!adminEmail.equals("")) {
            // This info is not required now
//            emailService.sendEmail(adminEmail, adminSubj, adminBody);
        }

        // Notify Mailchimp
        if (mailchimpService.initialize()) {
            mailchimpService.updateStatus(updatedCustomers);
        }
    }
}
