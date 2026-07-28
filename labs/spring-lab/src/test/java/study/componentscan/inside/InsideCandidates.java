package study.componentscan.inside;

import org.springframework.stereotype.Component;
import study.componentscan.ComponentScanExperimentTest.ExcludedFromScan;
import study.componentscan.ComponentScanExperimentTest.IncludedInScan;

import static study.componentscan.ComponentScanExperimentTest.recordConstruction;

@Component("orderService")
class OrderService {

    OrderService() {
        recordConstruction("orderService");
    }
}

@Component("legacyService")
@ExcludedFromScan
class LegacyService {

    LegacyService() {
        recordConstruction("legacyService");
    }
}

@IncludedInScan
class SpecialService {

    SpecialService() {
        recordConstruction("specialService");
    }
}