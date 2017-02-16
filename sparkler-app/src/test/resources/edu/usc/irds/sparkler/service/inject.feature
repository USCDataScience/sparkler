Feature: Basic Crawl

    Scenario: Seeding Sparkler
      When I add http://nutch.apache.org/ and http://tika.apache.org/ to the seed file
      And run the Sparkler ingest method
      Then I should see the files seeded in Solr

    Scenario: Seeding Sparkler
       When I add xyz://nutch.apache.org/ to the seed file
       And run the Sparkler ingest method
       Then sparkler should tell me my domain name is invalid