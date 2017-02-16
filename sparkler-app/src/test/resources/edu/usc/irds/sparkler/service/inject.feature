Feature: Basic Crawl

    Scenario: Seeding Sparkler
      When I add http://nutch.apache.org/ and http://tika.apache.org/ to the seed file
      And run the Sparkler ingest method
      Then I should see the files seeded in Solr