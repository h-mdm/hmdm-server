package com.hmdm.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.util.CryptoUtil;
import com.hmdm.util.RESTUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.List;

@Singleton
public class MailchimpService {
    private static final Logger logger = LoggerFactory.getLogger(MailchimpService.class);

    private final String baseUrl;
    private final String mailchimpUrl;
    private final String apiKey;
    private String listId;

    @Inject
    public MailchimpService(@Named("base.url") String baseUrl,
                            @Named("mailchimp.url") String mailchimpUrl,
                            @Named("mailchimp.key") String apiKey) {
        this.baseUrl = baseUrl;
        this.mailchimpUrl = mailchimpUrl;
        this.apiKey = apiKey;
    }

    // Fill the listId
    public boolean initialize() {
        if (listId != null) {
            // Already initialized
            return true;
        }
        if (mailchimpUrl.equals("")) {
            // Intentionally not using Mailchimp Service
            return false;
        }
        String location = mailchimpUrl + "/3.0/lists";
        String lists = RESTUtil.send(location, "GET", "Basic " + apiKey, null);
        if (lists != null) {
            try {
                JSONObject object = new JSONObject(lists);
                JSONArray listsArray = object.getJSONArray("lists");
                if (listsArray.length() == 0) {
                    logger.warn("No lists available on Mailchimp! Response: " + lists);
                }
                JSONObject list = listsArray.getJSONObject(0);
                listId = list.getString("id");
                return true;
            } catch (JSONException e) {
                logger.warn("Failed to parse response from Mailchimp REST API " + location + ": " + lists);
                return false;
            }
        } else {
            logger.warn("Failed to call Mailchimp REST API: " + location);
            return false;
        }
    }

    public boolean subscribe(Customer customer, String sourceTag) {
        if (mailchimpUrl.equals("")) {
            // Intentionally not using Mailchimp Service
            return false;
        }
        if (listId == null) {
            logger.warn("Can't subscribe customer " + customer.getName() + ": list not initialized");
            return false;
        }
        if (customer.getEmail() == null || customer.getEmail().trim().equals("")) {
            logger.warn("Can't subscribe customer " + customer.getName() + ": empty email");
            return false;
        }
        String location = mailchimpUrl + "/3.0/lists/" + listId;
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{\"members\":[{\"email_address\":\"");
        requestBody.append(customer.getEmail().trim());
        requestBody.append("\",\"email_type\":\"html\",\"status\":\"subscribed\",");
        if (customer.getLanguage() != null && !customer.getLanguage().trim().equals("")) {
            requestBody.append("\"language\":\"");
            requestBody.append(customer.getLanguage().trim());
            requestBody.append("\",");
        }
        requestBody.append("\"merge_fields\":{\"FNAME\":\"");
        if (customer.getFirstName() != null) {
            requestBody.append(customer.getFirstName().trim());
        }
        requestBody.append("\",\"LNAME\":\"");
        if (customer.getLastName() != null) {
            requestBody.append(customer.getLastName().trim());
        }
        requestBody.append("\",\"ACCOUNT\":\"");
        if (customer.getName() != null) {
            requestBody.append(customer.getName().trim());
        }
        requestBody.append("\",\"SERVER\":\"");
        requestBody.append(baseUrl);
        requestBody.append("\"},\"tags\":[\"");
        requestBody.append(sourceTag);
        requestBody.append("\"]");
        requestBody.append("}]}");

        String result = RESTUtil.send(location, "POST", "Basic " + apiKey, requestBody.toString());
        if (result == null) {
            logger.warn("Can't subscribe customer " + customer.getName() + ": API response failure");
            return false;
        }
        try {
            JSONObject object = new JSONObject(result);
            int totalCreated = object.getInt("total_created");
            if (totalCreated == 1) {
                return true;
            }
            // Some error occurred
            JSONArray errors = object.getJSONArray("errors");
            if (errors.length() > 0) {
                JSONObject error = errors.getJSONObject(0);
                logger.warn("Can't subscribe customer " + customer.getName() + ": " + error.getString("error"));
            } else {
                logger.warn("Can't subscribe customer " + customer.getName() + ": unspecified error");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            logger.warn("Wrong JSON response from " + location + ": " + e.getMessage());
        }
        return false;
    }

    // Send the command to add new tags to customers
    public boolean updateStatus(List<Customer> customers) {
        if (mailchimpUrl.equals("")) {
            // Intentionally not using Mailchimp Service
            return false;
        }
        if (listId == null) {
            logger.warn("Can't update customer status: list not initialized");
            return false;
        }
        if (customers.size() == 0) {
            // Nothing to do
            logger.debug("Updating customer status: empty list");
            return true;
        }

        String baseLocation = mailchimpUrl + "/3.0/lists/" + listId + "/members/";
        for (Customer customer : customers) {
            if (customer.getEmail() == null || customer.getEmail().trim().equals("")) {
                continue;
            }
            String hash = CryptoUtil.getMD5String(customer.getEmail().toLowerCase());
            String location = baseLocation + hash + "/tags";
            String tag = generateTag(customer);
            if (tag == null) {
                logger.debug("Customer " + customer.getName() + " status " + customer.getCustomerStatus() +
                        ": shouldn't be reported to Mailchimp");
                continue;
            }
            StringBuilder requestBody = new StringBuilder();
            requestBody.append("{\"tags\":[{\"name\":\"");
            requestBody.append(tag);
            requestBody.append("\",\"status\":\"active\"}]}");

            String result = RESTUtil.send(location, "POST", "Basic " + apiKey, requestBody.toString());
            if (result == null) {
                logger.warn("Can't update tag for customer " + customer.getName() + ": API response failure");
                continue;
            }
        }

        return true;
    }

    public String generateTag(Customer customer) {
        StringBuilder tag = new StringBuilder();
        if (customer.getCustomerStatus().equals(Customer.CUSTOMER_INACTIVE)) {
            tag.append("inactive");
            if (customer.getInactiveState() == 2) {
                tag.append("_2");
            }
        } else if (customer.getCustomerStatus().equals(Customer.CUSTOMER_PAUSE)) {
            tag.append("pause");
            if (customer.getPauseState() == 2) {
                tag.append("_2");
            }
        } else if (customer.getCustomerStatus().equals(Customer.CUSTOMER_ABANDON)) {
            tag.append("abandon");
            if (customer.getAbandonState() == 2) {
                tag.append("_2");
            }
        } else {
            // Other states shouldn't be notified
            return null;
        }
        if (customer.getLanguage() != null && !customer.getLanguage().trim().equals("")) {
            tag.append("_");
            tag.append(customer.getLanguage());
        }
        return tag.toString();
    }

}
