package study.componentscan.outside;

import org.springframework.stereotype.Component;
import study.componentscan.ComponentScanExperimentTest.IncludedInScan;

import static study.componentscan.ComponentScanExperimentTest.recordConstruction;

@Component("externalService")
class ExternalService {

    ExternalService() {
        recordConstruction("externalService");
    }
}

@IncludedInScan
class ExternalSpecialService {

    ExternalSpecialService() {
        recordConstruction("externalSpecialService");
    }
}