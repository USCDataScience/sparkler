package edu.usc.irds.sparkler.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.Fetcher;

public class BrowserFetcher extends AbstractExtensionPoint implements Fetcher {

	private static final Logger LOG = LoggerFactory.getLogger(BrowserFetcher.class);
	
	@Override
	public String fetch(String webUrl) {
		long start = System.currentTimeMillis();

		JBrowserDriver driver = createBrowserInstance();

		LOG.debug("Time taken to create driver- {}", (System.currentTimeMillis() - start));

		// This will block for the page load and any
		// associated AJAX requests
		driver.get(webUrl);

		int status = driver.getStatusCode();

		// Returns the page source in its current state, including
		// any DOM updates that occurred after page load
		String html = driver.getPageSource();
		quitBrowserInstance(driver);
		
		LOG.debug("Time taken to load {} - {} ",webUrl, (System.currentTimeMillis() - start));
		
		return html ;
	}

	public JBrowserDriver createBrowserInstance() {
		// TODO: Take from property file
		
		return new JBrowserDriver(Settings.builder()
				.timezone(Timezone.AMERICA_NEWYORK)
				.ajaxResourceTimeout(2000)
				.ajaxWait(2000).socketTimeout(2000)
				.connectTimeout(2000).build());
	}

	public void quitBrowserInstance(JBrowserDriver driver) {
		driver.quit();
	}

}
