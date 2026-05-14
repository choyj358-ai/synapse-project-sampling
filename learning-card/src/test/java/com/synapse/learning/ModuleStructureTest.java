package com.synapse.learning;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleStructureTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(LearningCardApplication.class).verify();
    }
}
