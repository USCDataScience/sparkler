/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.kreac.sparkler.plugin.fetcher;

import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.util.TestUtils;
import org.junit.Test;

/**
 *
 * @author michelad
 */
public class FetcherFirefoxTest {
    @Test
    public void testFirefoxHtml() throws Exception {
    	FetcherFirefox fetcherFirefox;
        fetcherFirefox = TestUtils.newInstance(FetcherFirefox.class, "fetcher.firefox");
		Resource resource = new Resource("https://www.hydroone.com", "hydroone.com", TestUtils.JOB_CONTEXT);
    	System.out.println(fetcherFirefox.fetch(resource).getResponseCode());
    }
}
