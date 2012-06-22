package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.tdl.vireo.model.NamedSearchFilter;
import org.tdl.vireo.model.Person;
import org.tdl.vireo.model.PersonRepository;
import org.tdl.vireo.model.RoleType;
import org.tdl.vireo.model.SettingsRepository;
import org.tdl.vireo.model.SubmissionRepository;
import org.tdl.vireo.model.jpa.JpaEmailTemplateImpl;
import org.tdl.vireo.search.SearchDirection;
import org.tdl.vireo.search.SearchOrder;
import org.tdl.vireo.security.SecurityContext;

import play.Play;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.libs.Mail;
import play.modules.spring.Spring;
import play.mvc.Http.Response;
import play.mvc.Router;
import play.test.FunctionalTest;

/**
 * Test the FilterTab controller
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
public class FilterTabTest extends AbstractVireoFunctionalTest {
	
	// Spring dependencies
	public static SecurityContext context = Spring.getBeanOfType(SecurityContext.class);
	public static PersonRepository personRepo = Spring.getBeanOfType(PersonRepository.class);
	public static SubmissionRepository subRepo = Spring.getBeanOfType(SubmissionRepository.class);
	public static SettingsRepository settingRepo = Spring.getBeanOfType(SettingsRepository.class);
	
	/**
	 * Test that we can remove and add each type of parameter (except for the
	 * submission date) from the current action filter.
	 * 
	 * To do this the test will run through each parameter type add it to the
	 * filter, confirm that it has been added. Then once all the parameters have
	 * been added it will verify that they are all still present. Finally in the
	 * same order it will remove them one-by-one.
	 */
	@Test
	public void testAddRemoveEachTypeOfFilterParameter() {
		
		// Login as an administrator
		LOGIN();
	
		// Run for both the list and log tabs
		String[] possibleNavs = {"list","log"};
		for (String nav : possibleNavs) {
			
			
			// Get our URLS
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("nav", nav);
			
			final String LIST_URL = (nav.equals("list")) ? Router.reverse("FilterTab.list",routeArgs).url : Router.reverse("FilterTab.log",routeArgs).url;
			final String FILTER_URL = Router.reverse("FilterTab.modifyFilter",routeArgs).url;

			
			
			
			Response response = GET(LIST_URL);
			// Check that there are no filters.
			assertContentMatch("<div class=\"main-heading\">Now filtering By:<\\/div>\\s*<\\/div>\\s*<div class=\"box-body\">\\s*<\\/div>", response);
			
			// Add STATUS: Submitted
			GET(FILTER_URL+"?action=add&type=state&value=Submitted");
			// Add STATUS: In Progress
			GET(FILTER_URL+"?action=add&type=state&value=InProgress");	
			// Add STATUS: Published
			GET(FILTER_URL+"?action=add&type=state&value=Published");
			// Add ASSGINEE: unassigned
			GET(FILTER_URL+"?action=add&type=assignee&value=null");
			// Add ASSGINEE: Billy
			GET(FILTER_URL+"?action=add&type=assignee&value=1");
			// Add GRADUATION SEMESTER: 2010 May
			GET(FILTER_URL+"?action=add&type=semester&year=2010&month=4");
			// Add GRADUATION SEMESTER: 2011 August
			GET(FILTER_URL+"?action=add&type=semester&year=2011&month=7");
			// Add DEPARTMENT: Agricultural Leadership, Education and Communications
			GET(FILTER_URL+"?action=add&type=department&value=Agricultural+Leadership%2C+Education%2C+and+Communications");
			// Add DEPARTMENT: Visualization
			GET(FILTER_URL+"?action=add&type=department&value=Visualization");
			// Add COLLEGE: College of Education and Human Development
			GET(FILTER_URL+"?action=add&type=college&value=College+of+Education+and+Human+Development");
			// Add COLLEGE: College of Science
			GET(FILTER_URL+"?action=add&type=college&value=College+of+Science");
			// Add MAJOR: Accounting
			GET(FILTER_URL+"?action=add&type=major&value=Accounting");
			// Add MAJOR: Zoology
			GET(FILTER_URL+"?action=add&type=major&value=Zoology");
			// Add EMBARGO: none
			GET(FILTER_URL+"?action=add&type=embargo&value=1");
			// Add EMBARGO: Patent Hold
			GET(FILTER_URL+"?action=add&type=embargo&value=3");
			// Add DEGREE: Doctor of Philosophy
			GET(FILTER_URL+"?action=add&type=degree&value=Doctor+of+Philosophy");
			// Add DEGREE: Bachelor of Environmental Design
			GET(FILTER_URL+"?action=add&type=degree&value=Bachelor+of+Environmental+Design");
			// Add DOCUMENT TYPE: Record of Study
			GET(FILTER_URL+"?action=add&type=docType&value=Record+of+Study");
			// Add DOCUMENT TYPE: Thesis
			GET(FILTER_URL+"?action=add&type=docType&value=Thesis");
			// Add DOCUMENT TYPE: Dissertation
			GET(FILTER_URL+"?action=add&type=docType&value=Dissertation");

			// Now that we are at the apex, check that everything is still there.
			response = GET(LIST_URL);
			assertTrue(getContent(response).contains("filter?action=remove&type=state&value=Submitted"));
			assertFalse(getContent(response).contains("filter?action=add&type=state&value=Submitted"));
			assertTrue(getContent(response).contains("filter?action=remove&type=state&value=InProgress"));
			assertFalse(getContent(response).contains("filter?action=add&type=state&value=InProgress"));
			assertTrue(getContent(response).contains("filter?action=remove&type=state&value=Published"));
			assertFalse(getContent(response).contains("filter?action=add&type=state&value=Published"));
			assertTrue(getContent(response).contains("filter?action=remove&type=assignee&value=null"));
			assertFalse(getContent(response).contains("filter?action=add&type=assignee&value=null"));
			assertTrue(getContent(response).contains("filter?action=remove&type=assignee&value=1"));
			assertFalse(getContent(response).contains("filter?action=add&type=assignee&value=1"));
			assertTrue(getContent(response).contains("filter?action=remove&type=semester&year=2010&month=4"));
			assertFalse(getContent(response).contains("filter?action=add&type=semester&year=2010&month=4"));
			assertTrue(getContent(response).contains("filter?action=remove&type=semester&year=2011&month=7"));
			assertFalse(getContent(response).contains("filter?action=add&type=semester&year=2011&month=7"));
			assertTrue(getContent(response).contains("filter?action=remove&type=department&value=Agricultural+Leadership%2C+Education%2C+and+Communications"));
			assertFalse(getContent(response).contains("filter?action=add&type=department&value=Agricultural+Leadership%2C+Education%2C+and+Communications"));
			assertTrue(getContent(response).contains("filter?action=remove&type=department&value=Visualization"));
			assertFalse(getContent(response).contains("filter?action=add&type=department&value=Visualization"));
			assertTrue(getContent(response).contains("filter?action=remove&type=college&value=College+of+Education+and+Human+Development"));
			assertFalse(getContent(response).contains("filter?action=add&type=college&value=College+of+Education+and+Human+Development"));
			assertTrue(getContent(response).contains("filter?action=remove&type=college&value=College+of+Science"));
			assertFalse(getContent(response).contains("filter?action=add&type=college&value=College+of+Science"));
			assertTrue(getContent(response).contains("filter?action=remove&type=major&value=Accounting"));
			assertFalse(getContent(response).contains("filter?action=add&type=major&value=Accounting"));
			assertTrue(getContent(response).contains("filter?action=remove&type=major&value=Zoology"));
			assertFalse(getContent(response).contains("filter?action=add&type=major&value=Zoology"));		
			assertTrue(getContent(response).contains("filter?action=remove&type=embargo&value=1"));
			assertFalse(getContent(response).contains("filter?action=add&type=embargo&value=1"));		
			assertTrue(getContent(response).contains("filter?action=remove&type=embargo&value=3"));
			assertFalse(getContent(response).contains("filter?action=add&type=embargo&value=3"));
			assertTrue(getContent(response).contains("filter?action=remove&type=degree&value=Doctor+of+Philosophy"));
			assertFalse(getContent(response).contains("filter?action=add&type=degree&value=Doctor+of+Philosophy"));
			assertTrue(getContent(response).contains("filter?action=remove&type=degree&value=Bachelor+of+Environmental+Design"));
			assertFalse(getContent(response).contains("filter?action=add&type=degree&value=Bachelor+of+Environmental+Design"));
			assertTrue(getContent(response).contains("filter?action=remove&type=docType&value=Record+of+Study"));
			assertFalse(getContent(response).contains("filter?action=add&type=docType&value=Record+of+Study"));
			assertTrue(getContent(response).contains("filter?action=remove&type=docType&value=Thesis"));
			assertFalse(getContent(response).contains("filter?action=add&type=docType&value=Thesis"));
			assertTrue(getContent(response).contains("filter?action=remove&type=docType&value=Dissertation"));
			assertFalse(getContent(response).contains("filter?action=add&type=docType&value=Dissertation"));
			
			// Remove STATUS: Submitted
			GET(FILTER_URL+"?action=remove&type=state&value=Submitted");
			// Remove STATUS: In Progress
			GET(FILTER_URL+"?action=remove&type=state&value=InProgress");	
			// Remove STATUS: Published
			GET(FILTER_URL+"?action=remove&type=state&value=Published");		
			// Remove ASSGINEE: unassigned
			GET(FILTER_URL+"?action=remove&type=assignee&value=null");
			// Remove ASSGINEE: Billy
			GET(FILTER_URL+"?action=remove&type=assignee&value=1");
			// Remove GRADUATION SEMESTER: 2010 May
			GET(FILTER_URL+"?action=remove&type=semester&year=2010&month=4");
			// Remove GRADUATION SEMESTER: 2011 August
			GET(FILTER_URL+"?action=remove&type=semester&year=2011&month=7");
			// Remove DEPARTMENT: Agricultural Leadership, Education and Communications
			GET(FILTER_URL+"?action=remove&type=department&value=Agricultural+Leadership%2C+Education%2C+and+Communications");
			// Remove DEPARTMENT: Visualization
			GET(FILTER_URL+"?action=remove&type=department&value=Visualization");
			// Remove COLLEGE: College of Education and Human Development
			GET(FILTER_URL+"?action=remove&type=college&value=College+of+Education+and+Human+Development");
			// Remove COLLEGE: College of Science
			GET(FILTER_URL+"?action=remove&type=college&value=College+of+Science");
			// Remove MAJOR: Accounting
			GET(FILTER_URL+"?action=remove&type=major&value=Accounting");
			// Remove MAJOR: Zoology
			GET(FILTER_URL+"?action=remove&type=major&value=Zoology");
			// Remove EMBARGO: none
			GET(FILTER_URL+"?action=remove&type=embargo&value=1");	
			// Remove EMBARGO: Patent Hold
			GET(FILTER_URL+"?action=remove&type=embargo&value=3");
			// Remove DEGREE: Doctor of Philosophy
			GET(FILTER_URL+"?action=remove&type=degree&value=Doctor+of+Philosophy");
			// Remove DEGREE: Bachelor of Environmental Design
			GET(FILTER_URL+"?action=remove&type=degree&value=Bachelor+of+Environmental+Design");
			// Remove DOCUMENT TYPE: Record of Study
			GET(FILTER_URL+"?action=remove&type=docType&value=Record+of+Study");
			// Remove DOCUMENT TYPE: Thesis
			GET(FILTER_URL+"?action=remove&type=docType&value=Thesis");
			// Remove DOCUMENT TYPE: Dissertation
			GET(FILTER_URL+"?action=remove&type=docType&value=Dissertation");
			
			// Finally, check that there are no filters left
			response = GET(LIST_URL);
			assertContentMatch("<div class=\"main-heading\">Now filtering By:<\\/div>\\s*<\\/div>\\s*<div class=\"box-body\">\\s*<\\/div>", response);
		}
		
	}
	
	/**
	 * Test that the submission date works correctly.
	 * 
	 * First we start by using the choose mechanism to narrow down the date to
	 * particular subset of days. Once that is complete we will remove the start
	 * and end dates individually. Finally we will conclude by setting the start
	 * and end dates manually using the range mechanism.
	 */
	@Test
	public void testAddRemoveDateFilter() {
		
		// Login as an administrator
		LOGIN();
		
		// Run for both the list and log tabs
		String[] possibleNavs = {"list","log"};
		for (String nav : possibleNavs) {
			
			// Get our URLS
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("nav", nav);
			final String LIST_URL = (nav.equals("list")) ? Router.reverse("FilterTab.list",routeArgs).url : Router.reverse("FilterTab.log",routeArgs).url;
			final String FILTER_URL = Router.reverse("FilterTab.modifyFilter",routeArgs).url;
			
			Response response = GET(LIST_URL);
			// Check that there are no filters.
			assertContentMatch("<div class=\"main-heading\">Now filtering By:<\\/div>\\s*<\\/div>\\s*<div class=\"box-body\">\\s*<\\/div>", response);
			
			// Narrow the field to 2011
			GET(FILTER_URL+"?action=add&type=rangeChoose&year=2011");
			response = GET(LIST_URL);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeStart\"\\s*>\\s*01/01/2011\\s*</a>",response);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeEnd\"\\s*>\\s*12/31/2011\\s*</a>",response);
	
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011\""));
			assertTrue(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=0\""));
			assertTrue(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=11\""));
			
			// Narrow the field to May
			GET(FILTER_URL+"?action=add&type=rangeChoose&year=2011&month=4");
			response = GET(LIST_URL);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeStart\"\\s*>\\s*05/01/2011\\s*</a>",response);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeEnd\"\\s*>\\s*05/31/2011\\s*</a>",response);
	
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=0\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=11\""));
			assertTrue(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=1\""));
			assertTrue(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=11\""));
			assertTrue(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=21\""));
			
			
			// Narrow the field to days 11-20
			GET(FILTER_URL+"?action=add&type=rangeChoose&year=2011&month=4&days=11");
			response = GET(LIST_URL);
			assertContentMatch("filter\\?action=remove\\&type=rangeStart\"\\s*>\\s*05\\/11\\/2011\\s*</a>",response);
			assertContentMatch("filter\\?action=remove\\&type=rangeEnd\"\\s*>\\s*05\\/20\\/2011\\s*</a>",response);
	
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=0\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=11\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=1\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=11\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=21\""));
			
			// Remove the start date
			GET(FILTER_URL+"?action=remove&type=rangeStart");
			response = GET(LIST_URL);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeEnd\"\\s*>\\s*05/20/2011\\s*</a>",response);
			assertContentMatch("name=\"startDate\"",response);
			
			assertFalse(getContent(response).contains("filter?action=remove&type=rangeStart"));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=0\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=11\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=1\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=11\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=21\""));
			
			
			// Remove the end date
			GET(FILTER_URL+"?action=remove&type=rangeEnd");
			response = GET(LIST_URL);
			assertContentMatch("name=\"startDate\"",response);
			assertContentMatch("name=\"endDate\"",response);
	
			
			assertFalse(getContent(response).contains("filter?action=remove&type=rangeStart\""));
			assertTrue(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=0\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=11\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=1\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=11\""));
			assertFalse(getContent(response).contains("filter?action=add&type=rangeChoose&year=2011&month=4&days=21\""));
			
			
			// Add a manual start date
			Map<String,String> params = new HashMap<String,String>();
			params.put("type", "range");
			params.put("action", "add");
			params.put("startDate", "5/5/2011");
			params.put("endDate", "");
			POST(FILTER_URL,params);
			response = GET(LIST_URL);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeStart\"\\s*>\\s*05/05/2011\\s*</a>",response);
			assertContentMatch("name=\"endDate\"",response);
	
			assertFalse(getContent(response).contains("name=\"startDate\""));
			
			// Add a manual end date
			params.clear();
			params.put("type", "range");
			params.put("action", "add");
			params.put("startDate", "");
			params.put("endDate", "5/7/2011");
			POST(FILTER_URL,params);
			response = GET(LIST_URL);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeStart\"\\s*>\\s*05/05/2011\\s*</a>",response);
			assertContentMatch("href=\"/admin/[^/]*/filter\\?action=remove&type=rangeEnd\"\\s*>\\s*05/07/2011\\s*</a>",response);
			
			assertFalse(getContent(response).contains("name=\"startDate\""));
			assertFalse(getContent(response).contains("name=\"endDate\""));
		}
	}
	
	/**
	 * Test that filters can be saved, cleared, and removed.
	 * 
	 * This will create a big filter with 2 of each type of parameter. Then it
	 * will save the filter. To test that it's actually saved and can be
	 * reloaded the active filter is cleared, followed by loading the previously
	 * saved filter. Everything is checked along the way, before the final step
	 * of deleting the filter.
	 */
	@Test
	public void testSaveClearAndRemoveFilter() {
		
		// Login as an administrator
		LOGIN();
		
		// Run for both the list and log tabs.
		String[] possibleNavs = {"list","log"};
		for (String nav : possibleNavs) {
			
			// Get our URLS
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("nav", nav);
			final String LIST_URL = (nav.equals("list")) ? Router.reverse("FilterTab.list",routeArgs).url : Router.reverse("FilterTab.log",routeArgs).url;
			final String FILTER_URL = Router.reverse("FilterTab.modifyFilter",routeArgs).url;
			
			Response response = GET(LIST_URL);
			// Check that there are no filters.
			assertContentMatch("<div class=\"main-heading\">Now filtering By:<\\/div>\\s*<\\/div>\\s*<div class=\"box-body\">\\s*<\\/div>", response);
			
			// Add Two of each filter type
			GET(FILTER_URL+"?action=add&type=state&value=Submitted");
			response = GET(LIST_URL);
	
			// Check that all the filters were added.
			assertTrue(getContent(response).contains("filter?action=remove&type=state&value=Submitted"));
			
			// Save the filter
			Map<String,String> params = new HashMap<String,String>();
			params.put("action", "save");
			params.put("name", "My Test Filter");
			params.put("public", "true");
			params.put("submit_save", "Save Filter");
			response = POST(FILTER_URL,params);
			
			// Extract the filter id so we don't depend it always being #1
			response = GET(LIST_URL);
			Pattern pattern = Pattern.compile("filter=(\\d+)\">My Test Filter<\\/a>");
			Matcher matcher = pattern.matcher(getContent(response));
			assertTrue(matcher.find());
			Long filterId = Long.valueOf(matcher.group(1));
			
			// Check that the filter was saved to the database.
			NamedSearchFilter filter = subRepo.findSearchFilter(filterId);
			
			assertEquals("My Test Filter", filter.getName());
			assertEquals(1,filter.getStates().size());
			JPA.em().clear();
			
			// Clear the current filter
			GET(FILTER_URL+"?action=clear");
			response = GET(LIST_URL);
			assertContentMatch("<div class=\"main-heading\">Now filtering By:<\\/div>\\s*<\\/div>\\s*<div class=\"box-body\">\\s*<\\/div>", response);
	
			// Load the saved filter back into existance.
			GET(FILTER_URL+"?action=load&filter="+filterId);
			response = GET(LIST_URL);
			
			// Check that all the filters were loaded.
			assertTrue(getContent(response).contains("filter?action=remove&type=state&value=Submitted"));
			
			// Remove the filter
			params.clear();
			params.put("action","manage");
			params.put("remove",String.valueOf(filterId));
			params.put("submit_remove", "Remove Filters");
			POST(FILTER_URL,params);
			
			response = GET(LIST_URL);
			
			// Check that the filter has been removed
			assertFalse(getContent(response).contains("My Test Filter"));
			
			// Check that although the filter has been removed, the active filter has not been chnaged.
			assertTrue(getContent(response).contains("filter?action=remove&type=state&value=Submitted"));
	
			// Verify that the filter was removed in the database
			filter = subRepo.findSearchFilter(filterId);
			
			assertNull(filter);
			JPA.em().clear();
		}
	}
	
	/**
	 * Test the submission display by changing sort orders and directions.
	 */
	@Test
	public void testSearchDisplay() {
		
		// Login as an administrator
		LOGIN();
		
		// Run for both the list and log tabs
		String[] possibleNavs = {"list","log"};
		for (String nav : possibleNavs) {
			
			// Get our URLS
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("nav", nav);
			final String LIST_URL = (nav.equals("list")) ? Router.reverse("FilterTab.list",routeArgs).url : Router.reverse("FilterTab.log",routeArgs).url;
			final String SEARCH_URL = Router.reverse("FilterTab.modifySearch",routeArgs).url;
			
			Response response = null;
			
			// To save test run time instead of searching all possible search orders, we'll just check one.
			//for(SearchOrder order : SearchOrder.values()) {
				SearchOrder order = SearchOrder.SUBMITTER;
			
			
				// Test each column as ascending and decending
				GET(SEARCH_URL+"?orderby="+order.getId());
				
	
				response = GET(LIST_URL);
				String labelName = Messages.get(nav.toUpperCase()+"_COLUMN_"+order.name());
				assertContentMatch("<th class=\"orderby ascending\">\\s*<a href=\"[^\"]*\\?direction=toggle\">"+labelName+"</a>",response);
				
				GET(SEARCH_URL+"?orderby="+order.getId());
				GET(SEARCH_URL+"?direction=toggle");
	
				response = GET(LIST_URL);
				assertContentMatch("<th class=\"orderby descending\">\\s*<a href=\"[^\"]*\\?direction=toggle\">"+labelName+"</a>",response);
				
				GET(SEARCH_URL+"?direction=toggle");
			//}
		}
	}
	
}