package com.sw3.barber_microservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.repository.BarberRepository;
import com.sw3.barber_microservice.service.KeycloakService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.server-url:}")
    private String keycloakUrl;
    @Value("${keycloak.realm:}")
    private String realm;
    @Value("${keycloak.client-id:}")
    private String clientId;
    @Value("${keycloak.client-secret:}")
    private String clientSecret;
    @Value("${keycloak.barber-role:BARBER}")
    private String barberRole;
    @Value("${keycloak.role-client-id:}")
    private String roleClientId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BarberRepository barberRepository;

    public KeycloakServiceImpl(BarberRepository barberRepository) {
        this.barberRepository = barberRepository;
    }

    @Override
    public String createUserForBarber(BarberDTO barberDto) {
        String token = obtainAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1) search existing user by email
        String searchUrl = String.format("%s/admin/realms/%s/users?email=%s", keycloakUrl, realm, barberDto.getEmail());
        ResponseEntity<JsonNode> searchResp = restTemplate.exchange(searchUrl, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        if (searchResp.getStatusCode().is2xxSuccessful() && searchResp.getBody() != null && searchResp.getBody().size() > 0) {
            String existingId = searchResp.getBody().get(0).get("id").asText();
            barberRepository.findById(barberDto.getId()).ifPresent(b -> {
                if (b.getKeycloakId() == null) {
                    b.setKeycloakId(existingId);
                    barberRepository.save(b);
                }
            });
            return existingId;
        }

        // 2) create user WITHOUT credentials (avoid deprecated formats)
        ObjectNode user = objectMapper.createObjectNode();
        user.put("username", barberDto.getEmail());
        user.put("email", barberDto.getEmail());
        // Keycloak expects 'firstName' and 'lastName' fields
        if (barberDto.getName() != null) {
            user.put("firstName", barberDto.getName());
        }
        if (barberDto.getLastName() != null) {
            user.put("lastName", barberDto.getLastName());
        }
        user.put("enabled", true);

        HttpEntity<String> req = new HttpEntity<>(user.toString(), headers);
        String createUrl = String.format("%s/admin/realms/%s/users", keycloakUrl, realm);
        ResponseEntity<Void> resp = restTemplate.postForEntity(createUrl, req, Void.class);
        if (!resp.getStatusCode().is2xxSuccessful() && resp.getStatusCodeValue() != 201) {
            throw new RuntimeException("Failed to create keycloak user, status=" + resp.getStatusCode());
        }

        String location = resp.getHeaders().getFirst("Location");
        if (location == null) {
            throw new RuntimeException("Keycloak did not return Location header for created user");
        }
        String userId = location.substring(location.lastIndexOf('/') + 1);

        // 3) set password via reset-password endpoint (modern API)
        System.out.println("password: " + barberDto.getPassword());
        String userPassword = barberDto.getPassword();
        setUserPassword(userId, userPassword, token);

        // 4) assign role
        assignRealmRole(userId, token);

        // 4.b) remove any default-roles assigned by the realm so the user only keeps the explicit BARBER role
        removeDefaultRealmRoles(userId, token);

        // 5) persist keycloakId in local Barber
        barberRepository.findById(barberDto.getId()).ifPresent(b -> {
            b.setKeycloakId(userId);
            barberRepository.save(b);
        });

        return userId;
    }

    private void assignRealmRole(String userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1) Try realm role first
        String realmRoleUrl = String.format("%s/admin/realms/%s/roles/%s", keycloakUrl, realm, barberRole);
        try {
            ResponseEntity<JsonNode> roleResp = restTemplate.exchange(realmRoleUrl, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            if (roleResp.getStatusCode().is2xxSuccessful() && roleResp.getBody() != null) {
                JsonNode role = roleResp.getBody();
                ArrayNode roles = objectMapper.createArrayNode();
                ObjectNode r = objectMapper.createObjectNode();
                r.put("id", role.get("id").asText());
                r.put("name", role.get("name").asText());
                roles.add(r);

                String assignUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakUrl, realm, userId);
                ResponseEntity<Void> assignResp = restTemplate.postForEntity(assignUrl, new HttpEntity<>(roles.toString(), headers), Void.class);
                if (!assignResp.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Failed to assign realm role, status=" + assignResp.getStatusCode());
                }
                return;
            }
        } catch (HttpClientErrorException e) {
            // If 404 => role not found as realm role; otherwise rethrow with details
            if (e.getStatusCode().value() != 404) {
                String body = e.getResponseBodyAsString();
                throw new RuntimeException("Failed to fetch realm role from Keycloak, status=" + e.getStatusCode() + ", body=" + body, e);
            }
        }

        // 2) Try client role. Use configured roleClientId if present, otherwise fall back to configured clientId
        String clientToQuery = (roleClientId != null && !roleClientId.isBlank()) ? roleClientId : clientId;
        if (clientToQuery == null || clientToQuery.isBlank()) {
            throw new RuntimeException("No clientId configured to search for client roles");
        }

        try {
            String clientSearchUrl = String.format("%s/admin/realms/%s/clients?clientId=%s", keycloakUrl, realm, clientToQuery);
            ResponseEntity<JsonNode> clientResp = restTemplate.exchange(clientSearchUrl, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            if (!clientResp.getStatusCode().is2xxSuccessful() || clientResp.getBody() == null || clientResp.getBody().size() == 0) {
                throw new RuntimeException("Client not found for clientId=" + clientToQuery);
            }
            String clientUuid = clientResp.getBody().get(0).get("id").asText();

            String clientRoleUrl = String.format("%s/admin/realms/%s/clients/%s/roles/%s", keycloakUrl, realm, clientUuid, barberRole);
            ResponseEntity<JsonNode> clientRoleResp = restTemplate.exchange(clientRoleUrl, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            if (!clientRoleResp.getStatusCode().is2xxSuccessful() || clientRoleResp.getBody() == null) {
                throw new RuntimeException("Failed to fetch client role, status=" + clientRoleResp.getStatusCode());
            }
            JsonNode role = clientRoleResp.getBody();
            ArrayNode roles = objectMapper.createArrayNode();
            ObjectNode r = objectMapper.createObjectNode();
            r.put("id", role.get("id").asText());
            r.put("name", role.get("name").asText());
            roles.add(r);

            String assignClientUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/clients/%s", keycloakUrl, realm, userId, clientUuid);
            ResponseEntity<Void> assignResp = restTemplate.postForEntity(assignClientUrl, new HttpEntity<>(roles.toString(), headers), Void.class);
            if (!assignResp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to assign client role, status=" + assignResp.getStatusCode());
            }
            return;
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            throw new RuntimeException("Failed to assign client role to user " + userId + ", status=" + e.getStatusCode() + ", body=" + body, e);
        }
    }

    /**
     * Remove any realm role mappings whose name starts with "default-roles".
     * This targets the automatic "default-roles-<realm>" assignment so created users
     * won't keep the realm default role and will only have explicit roles like BARBER.
     */
    private void removeDefaultRealmRoles(String userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String mappingsUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakUrl, realm, userId);
        try {
            ResponseEntity<JsonNode> resp = restTemplate.exchange(mappingsUrl, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return; // nothing to remove or failed to fetch
            }

            ArrayNode toRemove = objectMapper.createArrayNode();
            for (JsonNode role : resp.getBody()) {
                String roleName = role.has("name") ? role.get("name").asText() : null;
                if (roleName != null && roleName.startsWith("default-roles")) {
                    ObjectNode r = objectMapper.createObjectNode();
                    r.put("id", role.get("id").asText());
                    r.put("name", roleName);
                    toRemove.add(r);
                }
            }

            if (toRemove.size() > 0) {
                HttpEntity<String> delReq = new HttpEntity<>(toRemove.toString(), headers);
                ResponseEntity<Void> delResp = restTemplate.exchange(mappingsUrl, HttpMethod.DELETE, delReq, Void.class);
                if (!delResp.getStatusCode().is2xxSuccessful()) {
                    // non-fatal: log and continue
                    System.err.println("Warning: failed to remove default realm roles for user " + userId + ", status=" + delResp.getStatusCode());
                }
            }
        } catch (HttpClientErrorException e) {
            // ignore non-fatal errors but log for debugging
            System.err.println("Warning: error removing default realm roles for user " + userId + ": " + e.getMessage());
        }
    }

    private void setUserPassword(String userId, String password, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // If no password provided by caller, generate a reasonable temporary one
        String pwd = (password == null || password.isBlank()) ? java.util.UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12) : password;

        ObjectNode cred = objectMapper.createObjectNode();
        cred.put("type", "password");
        cred.put("value", pwd);
        cred.put("temporary", false);

        String resetUrl = String.format("%s/admin/realms/%s/users/%s/reset-password", keycloakUrl, realm, userId);
        HttpEntity<String> resetReq = new HttpEntity<>(cred.toString(), headers);
        try {
            ResponseEntity<Void> resetResp = restTemplate.exchange(resetUrl, HttpMethod.PUT, resetReq, Void.class);
            if (!resetResp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to set password for user " + userId + ", status=" + resetResp.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            throw new RuntimeException("Failed to set password for user " + userId + ", status=" + e.getStatusCode() + ", body=" + body, e);
        }
    }

    private String obtainAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        org.springframework.util.MultiValueMap<String, String> form = new org.springframework.util.LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, realm);
        ResponseEntity<JsonNode> tokenResp = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(form, headers), JsonNode.class);
        if (!tokenResp.getStatusCode().is2xxSuccessful() || tokenResp.getBody() == null) {
            throw new RuntimeException("Failed to obtain keycloak token: " + tokenResp.getStatusCode());
        }
        return tokenResp.getBody().get("access_token").asText();
    }
}