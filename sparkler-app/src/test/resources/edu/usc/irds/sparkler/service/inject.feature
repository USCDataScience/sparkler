Feature: Basic Crawl

    Scenario: Seeding Sparkler
      When I add http://nutch.apache.org/ and http://tika.apache.org/ to the seed file
      And run the Sparkler ingest method
      Then I should see the files seeded in Solr

    Scenario: Seeding Sparkler With Invalid URL
       When I add xyz://nutch.apache.org/ to the seed file
       And run the Sparkler ingest method
       Then sparkler should tell me my domain name is invalid

    Scenario: Run Crawl
      When I have a seeded Sparkler
      And run the Sparkler crawl method
      Then Sparkler should run a basic ingestion