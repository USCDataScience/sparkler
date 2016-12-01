from charms.reactive import when, when_not, set_state


@when_not('sparkler.installed')
def install_sparkler():
    set_state('sparkler.installed')

@when_not('java.ready')
def no_java():

@when_not('spark.joined')
def no_spark():

@when_not('spark.started')
def spark_not_started():

@when_not('solr.joined')
def no_solr():

@when_not('solr.started'):
def solr_not_started():

@when('solr.started')
@when('spark.started')
@when('java.ready')
def configure_sparkler():

@when('sparkler.configured')
def run_sparkler():
