package com.dts.practice.adminrelease;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminReleaseConsumerRulesTest {
    @Test void absentOrMalformedFrozenSnapshotCannotBePrepared() {
        var mapper=new ObjectMapper();var id=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,()->AdminReleaseConsumerService.validate(
            new AdminReleaseConsumerService.Prepare(id,"bank-v2","a".repeat(64),"b".repeat(64),"{}"),mapper));
        assertThrows(IllegalArgumentException.class,()->AdminReleaseConsumerService.validate(
            new AdminReleaseConsumerService.Prepare(id,"bank-v2",null,"b".repeat(64),"data".repeat(200)),mapper));
    }
}
