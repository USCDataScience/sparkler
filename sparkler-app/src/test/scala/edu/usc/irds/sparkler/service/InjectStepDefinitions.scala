package edu.usc.irds.sparkler.service

import cucumber.api.{PendingException, Scenario}
import cucumber.api.scala.{EN, ScalaDsl}
import org.junit.Assert._

class InjectStepDefinitions extends ScalaDsl with EN {

  When("""^I add http://nutch\.apache\.org/ and http://tika\.apache\.org/ to the seed file$""") { () =>
    //// Write code here that turns the phrase above into concrete actions
    throw new PendingException()
  }
  When("""^run the Sparkler ingest method$""") { () =>
    //// Write code here that turns the phrase above into concrete actions
    throw new PendingException()
  }
  Then("""^I should see the files seeded in Solr$""") { () =>
    //// Write code here that turns the phrase above into concrete actions
    throw new PendingException()
  }

}
