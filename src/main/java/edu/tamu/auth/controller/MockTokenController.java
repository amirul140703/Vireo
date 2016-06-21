package edu.tamu.auth.controller;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.framework.aspect.annotation.SkipAop;
import edu.tamu.framework.model.jwt.JWT;
import edu.tamu.framework.util.JwtUtility;

import org.tdl.vireo.config.constant.ConfigurationName;
import org.tdl.vireo.model.repo.ConfigurationRepo;

/** 
 * 
 * 
 * @author
 *
 */
@RestController
@RequestMapping("/mockauth")
public class MockTokenController {

    @Autowired
    private Environment env;
    
    @Autowired
    private JwtUtility jwtUtility;
    
    @Autowired
    private ConfigurationRepo configurationRepo;
        
    @Value("${auth.security.jwt.secret-key}")
    private String secret_key;
    
    @Value("${auth.security.jwt-expiration}")
    private Long expiration;
    
    @Value("${shib.keys}")
    private String[] shibKeys;
    
    private static final Logger logger = Logger.getLogger(MockTokenController.class);
    
    /**
     * Token endpoint. Returns a token with credentials from Shibboleth in payload.
     *
     * @param       params          @RequestParam() Map<String,String>
     * @param       headers         @RequestHeader() Map<String,String>
     *
     * @return      ModelAndView
     *
     * @exception   InvalidKeyException
     * @exception   NoSuchAlgorithmException
     * @exception   IllegalStateException
     * @exception   UnsupportedEncodingException
     * @exception   JsonProcessingException
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws NoSuchPaddingException 
     * 
     */
    @RequestMapping("/token")
    @SkipAop
    public ModelAndView token(@RequestParam() Map<String,String> params, @RequestHeader() Map<String,String> headers) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException, JsonProcessingException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
            	
        String referer = params.get("referer");
        if(referer == null) {
        	System.err.println("No referer in header!!");        	
        }
        
        ModelAndView tokenResponse = null;
        try {
            tokenResponse = new ModelAndView("redirect:" + referer + "?jwt=" + makeToken(params, headers).getTokenAsString());
        } catch (InvalidKeyException | JsonProcessingException | NoSuchAlgorithmException | IllegalStateException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return tokenResponse;
    }
    
    /**
     * Refresh endpoint. Returns a new token with credentials from Shibboleth in payload.
     *
     * @param       params          @RequestParam() Map<String,String>
     * @param       headers         @RequestHeader() Map<String,String>
     *
     * @return      JWT
     *
     * @exception   InvalidKeyException
     * @exception   NoSuchAlgorithmException
     * @exception   IllegalStateException
     * @exception   UnsupportedEncodingException
     * @exception   JsonProcessingException
     * 
     */
    @RequestMapping("/refresh")
    @SkipAop
    public JWT refresh(@RequestParam() Map<String,String> params, @RequestHeader() Map<String,String> headers) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException, JsonProcessingException {            	    	    	
    	return makeToken(params, headers);
    }
    
    /**
     * Constructs a token from selected Shibboleth headers.
     *
     * @param       headers         Map<String, String>
     *
     * @return      JWT
     *
     * @exception   InvalidKeyException
     * @exception   NoSuchAlgorithmException
     * @exception   IllegalStateException
     * @exception   UnsupportedEncodingException
     * @exception   JsonProcessingException
     * 
     */    
    private JWT makeToken(@RequestParam() Map<String,String> params, Map<String, String> headers) throws InvalidKeyException, JsonProcessingException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {       
        JWT newToken = null;
       
        String token = params.get("token");
        
        // get shib headers out of DB
        String netIdHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_NETID, "netid");
        String birthYearHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_BIRTH_YEAR, "birthYear");
        String middleNameHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_MIDDLE_NAME, "middleName");
        String firstNameHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_FIRST_NAME, "firstName");
        String lastNameHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_LAST_NAME, "lastName");
        String emailHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_EMAIL, "email");
        String orcidHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_ORCID, "orcid");
        String institutionalIdentifierHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_INSTITUTIONAL_IDENTIFIER, "uin");
        String permEmailHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_PERMANENT_EMAIL_ADDRESS, "permanentEmailAddress");
        String permPhoneHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_PERMANENT_PHONE_NUMBER, "permanentPhoneNumber");
        String permAddressHeader = configurationRepo.getValue(ConfigurationName.APPLICATION_AUTH_SHIB_ATTRIBUTE_PERMANENT_POSTAL_ADDRESS, "permanentPostalAddress");

        if(token != null) {
    		newToken = jwtUtility.makeToken(jwtUtility.validateJWT(token));
    	} else {

        	newToken = new JWT(secret_key, expiration);
        	
        	String mockUser = params.get("mock");
        	    	
        	if(mockUser != null) {
        		if(mockUser.equals("assumed")) {
    	            for(String k : shibKeys) {
    	                String p = headers.get(env.getProperty("shib."+k, ""));
    	                newToken.makeClaim(k, p);
    	                logger.info("Adding " + k +": " + p + " to JWT.");
    	            }
    	        }
        		else if(mockUser.equals("admin")) {
                    newToken.makeClaim(netIdHeader, "aggieJack");
                    newToken.makeClaim(institutionalIdentifierHeader, "123456789");
                    newToken.makeClaim(lastNameHeader, "Daniels");
                    newToken.makeClaim(firstNameHeader, "Jack");
                    newToken.makeClaim(emailHeader, "aggieJack@tamu.edu");
    	        }
    	        else {
    	        	newToken.makeClaim(netIdHeader, "bobBoring");
    	        	newToken.makeClaim(institutionalIdentifierHeader, "987654321");
    	        	newToken.makeClaim(lastNameHeader, "Boring");
    	        	newToken.makeClaim(firstNameHeader, "Bob");
    	        	newToken.makeClaim(emailHeader, "bobBoring@tamu.edu");
    	        }
        	}
    	}
        
        // For app use
        newToken.makeClaim(netIdHeader,"aggieJack");
        newToken.makeClaim(institutionalIdentifierHeader,"987654321");
        newToken.makeClaim(lastNameHeader,"Daniels");
        newToken.makeClaim(firstNameHeader,"Jack");
        newToken.makeClaim(middleNameHeader,"Be");
        newToken.makeClaim(emailHeader,"aggieJack@tamu.edu");
        newToken.makeClaim(birthYearHeader,"1777");
        newToken.makeClaim(orcidHeader,"123ORCID");
        newToken.makeClaim(permEmailHeader,"permanentaggieJack@tamu.edu");
        newToken.makeClaim(permPhoneHeader,"1234567890");
        newToken.makeClaim(permAddressHeader,"123 The Place 2B");

        return newToken;       
    }

}