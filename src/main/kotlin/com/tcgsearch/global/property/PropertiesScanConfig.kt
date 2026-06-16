package com.tcgsearch.global.property

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(basePackages = ["com.tcgsearch.global.property"])
class PropertiesScanConfig {
}