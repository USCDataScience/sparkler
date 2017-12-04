package edu.usc.irds.sparkler.service

import cucumber.api.CucumberOptions
import org.junit.runner.RunWith
import cucumber.api.junit.Cucumber

@RunWith(classOf[Cucumber])
@CucumberOptions(plugin =Array("json:target/cucumber.json"))
class RunCukesTest
