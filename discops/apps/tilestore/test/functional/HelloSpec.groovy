import geb.spock.GebReportingSpec
  
class HelloSpec extends GebReportingSpec {
    def "go to page"() {
        when:
        go "http://omar.ossim.org"
        then:
        $("title").text() == "OMAR 1.8.19: Login"
    }
}

